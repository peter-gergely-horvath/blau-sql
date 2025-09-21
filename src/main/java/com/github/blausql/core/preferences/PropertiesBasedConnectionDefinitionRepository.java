/*
 * Copyright (c) 2017-2025 Peter G. Horvath, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.blausql.core.preferences;

import com.github.blausql.core.connection.ConnectionDefinition;
import com.github.blausql.spi.connections.ConnectionDefinitionRepository;
import com.github.blausql.spi.connections.LoadException;
import com.github.blausql.spi.connections.SaveException;

import java.io.IOException;
import java.util.*;

/**
 * Default implementation of {@link ConnectionDefinitionRepository} that stores connection definitions
 * in a properties file.
 */
// CHECKSTYLE.OFF: AvoidInlineConditionals: here, they simplify trivial methods
final class PropertiesBasedConnectionDefinitionRepository implements ConnectionDefinitionRepository {

    private static final String STATEMENT_SEPARATOR = ";";

    private static final char PROPERTY_SEPARATOR = '.';

    private static final class ConnectionPropertyReference {

        private final String connectionName;
        private final String propertyName;

        private ConnectionPropertyReference(String connectionName, String propertyName) {
            this.connectionName = connectionName;
            this.propertyName = propertyName;
        }

        private static ConnectionPropertyReference fromString(String key) {
            int propertySeparatorIndex = key.lastIndexOf(PROPERTY_SEPARATOR);
            if (propertySeparatorIndex == -1) {
                throw new IllegalStateException("Property Separator not found: " + key);
            }

            final String connectionDefinitionName = key.substring(0, propertySeparatorIndex);
            final String propertyName = key.substring(propertySeparatorIndex + 1);

            return new ConnectionPropertyReference(connectionDefinitionName, propertyName);
        }

        @Override
        public String toString() {
            return "ConnectionPropertyReference{"
                    + "connectionName='" + connectionName + '\''
                    + ", propertyName='" + propertyName + '\''
                    + '}';
        }
    }



    private static final PropertyStore CONNECTIONS_PROPERTY_STORE = PropertyStoreFactory.getConnectionsPropertyStore();

    private static final Comparator<ConnectionDefinition> CONNECTION_DEFINITION_COMPARATOR =
            (left, right) -> {
                if (left == null || right == null) {
                    return 0;
                }

                // If order is provided, sort them accordingly;
                // otherwise, sort based on names
                Integer leftOrder = left.getOrder();
                Integer rightOrder = right.getOrder();
                if (leftOrder != null && rightOrder != null) {
                    return leftOrder.compareTo(rightOrder);
                }

                String leftConnectionName = left.getConnectionName();
                String rightConnectionName = right.getConnectionName();

                if (leftConnectionName == null || rightConnectionName == null) {
                    return 0;
                }

                return leftConnectionName.compareTo(rightConnectionName);
            };

    @Override
    public List<ConnectionDefinition> getConnectionDefinitions() throws LoadException {
        try {
            LinkedHashMap<String, ConnectionDefinition> map = new LinkedHashMap<>();
            Properties properties = CONNECTIONS_PROPERTY_STORE.loadProperties();

            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                final String key = entry.getKey().toString();
                final String value = entry.getValue().toString();

                ConnectionPropertyReference propertyReference = ConnectionPropertyReference.fromString(key);

                ConnectionDefinition cd = map.computeIfAbsent(
                        propertyReference.connectionName, ConnectionDefinition::new);

                PropertyMapping propertyMapping = PropertyMapping.valueOf(propertyReference.propertyName);
                propertyMapping.setValue(cd, value);
            }

            ArrayList<ConnectionDefinition> connectionDefinitions = new ArrayList<>(map.values());
            connectionDefinitions.sort(CONNECTION_DEFINITION_COMPARATOR);

            return connectionDefinitions;

        } catch (IOException e) {
            throw new LoadException("Failed to load connection definitions", e);
        }
    }

    @Override
    public void saveConnectionDefinition(ConnectionDefinition cd) throws SaveException {

        Objects.requireNonNull(cd, "Argument cd cannot be null");

        if (cd.getConnectionName() == null || cd.getConnectionName().isBlank()) {
            throw new SaveException("A connection must have a non-empty name");
        }

        Character hotkey = cd.getHotkey();
        if (hotkey != null) {
            checkNoExistingConnectionDefinitionUsesTheHotKey(cd, hotkey);
        }

        try {
            Properties properties = CONNECTIONS_PROPERTY_STORE.loadProperties();

            // Remove all existing properties for this connection
            properties.keySet().removeIf(key -> ConnectionPropertyReference.fromString(key.toString())
                    .connectionName.equalsIgnoreCase(cd.getConnectionName()));

            // Add the updated properties
            for (PropertyMapping propertyMapping : PropertyMapping.values()) {
                propertyMapping.putPropertyKeyValue(cd, properties);
            }

            CONNECTIONS_PROPERTY_STORE.persistProperties(properties);

        } catch (IOException e) {
            throw new SaveException("Failed to save connection definition", e);
        }
    }

    private void checkNoExistingConnectionDefinitionUsesTheHotKey(ConnectionDefinition cd, Character hotkey)
            throws SaveException {

        List<ConnectionDefinition> existingConnectionDefinitions;
        try {
            existingConnectionDefinitions = getConnectionDefinitions();
        } catch (LoadException e) {
            // unlike righteous error handling, this allows recovering from issues, e.g. a corrupted config file.
            existingConnectionDefinitions = Collections.emptyList();
        }

        for (ConnectionDefinition existingConnectionDefinition : existingConnectionDefinitions) {

            Character existingHotkey = existingConnectionDefinition.getHotkey();

            if (existingHotkey != null
                    && !Objects.equals(existingConnectionDefinition.getConnectionName(), cd.getConnectionName())
                    && Objects.equals(
                        Character.toUpperCase(hotkey),
                        Character.toUpperCase(existingConnectionDefinition.getHotkey()))) {

                String connectionName = existingConnectionDefinition.getConnectionName();

                throw new SaveException("Hotkey '" + hotkey + "' is already used by: " + connectionName);
            }
        }
    }

    @Override
    public void deleteConnectionDefinitionByName(String connectionName) {
        try {
            boolean foundInProperties = false;
            Properties properties = CONNECTIONS_PROPERTY_STORE.loadProperties();

            Iterator<Object> iterator = properties.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next().toString();

                if (ConnectionPropertyReference.fromString(key).connectionName.equals(connectionName)) {
                    iterator.remove();
                    foundInProperties = true;
                }
            }

            if (!foundInProperties) {
                throw new IllegalStateException("Connection definition not found:" + connectionName);
            }

            CONNECTIONS_PROPERTY_STORE.persistProperties(properties);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete connection definition", e);
        }
    }

    @Override
    public ConnectionDefinition findConnectionDefinitionByName(String connectionName) throws LoadException {

        List<ConnectionDefinition> connections = getConnectionDefinitions();

        for (ConnectionDefinition connection : connections) {
            if (connectionName.equalsIgnoreCase(connection.getConnectionName())) {
                return connection;
            }
        }
        return null;
    }

    private enum PropertyMapping {
        ConnectionName {
            @Override
            String getValue(ConnectionDefinition cd) {
                return cd.getConnectionName();
            }

            @Override
            void setValue(ConnectionDefinition cd, String value) {
                cd.setConnectionName(value);
            }
        },
        DriverClassName {
            @Override
            String getValue(ConnectionDefinition cd) {
                return cd.getDriverClassName();
            }

            @Override
            void setValue(ConnectionDefinition cd, String value) {
                cd.setDriverClassName(value);
            }
        },
        JdbcUrl {
            @Override
            String getValue(ConnectionDefinition cd) {
                return cd.getJdbcUrl();
            }

            @Override
            void setValue(ConnectionDefinition cd, String value) {
                cd.setJdbcUrl(value);
            }
        },
        UserName {
            @Override
            String getValue(ConnectionDefinition cd) {
                return cd.getUserName();
            }

            @Override
            void setValue(ConnectionDefinition cd, String value) {
                cd.setUserName(value);
            }
        },
        Password {
            @Override
            String getValue(ConnectionDefinition cd) {
                return cd.getPassword();
            }

            @Override
            void setValue(ConnectionDefinition cd, String value) {
                cd.setPassword(value);
            }
        },
        LoginAutomatically {
            @Override
            String getValue(ConnectionDefinition cd) {
                return Boolean.toString(cd.getLoginAutomatically());
            }

            @Override
            void setValue(ConnectionDefinition cd, String value) {
                cd.setLoginAutomatically(Boolean.parseBoolean(value));
            }
        },
        StatementSeparator {
            @Override
            String getValue(ConnectionDefinition cd) {
                String theStatementSeparator = cd.getStatementSeparator();
                return theStatementSeparator != null ? theStatementSeparator : STATEMENT_SEPARATOR;
            }

            @Override
            void setValue(ConnectionDefinition cd, String value) {
                cd.setStatementSeparator(value);
            }
        },
        Hotkey {
            @Override
            String getValue(ConnectionDefinition cd) {
                Character hotkey = cd.getHotkey();
                return hotkey != null ? hotkey.toString() : "";
            }

            @Override
            void setValue(ConnectionDefinition cd, String value) {
                cd.setHotkey(value != null && !value.isEmpty() ? value.charAt(0) : null);
            }
        },
        Order {
            @Override
            String getValue(ConnectionDefinition cd) {
                Integer order = cd.getOrder();
                return order != null ? order.toString() : "";
            }

            @Override
            void setValue(ConnectionDefinition cd, String value) {
                cd.setOrder(value != null && !value.isEmpty() ? Integer.valueOf(value) : null);
            }
        };

        abstract String getValue(ConnectionDefinition cd);

        abstract void setValue(ConnectionDefinition cd, String value);

        void putPropertyKeyValue(ConnectionDefinition cd, Properties properties) {
            String value = getValue(cd);
            if (value != null && !value.isEmpty()) {
                properties.setProperty(cd.getConnectionName() + "." + name(), value);
            }
        }
    }
}
// CHECKSTYLE.ON

