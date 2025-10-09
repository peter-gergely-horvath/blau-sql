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

import com.github.blausql.core.connection.ConnectionConfiguration;
import com.github.blausql.spi.connections.ConnectionConfigurationRepository;
import com.github.blausql.spi.connections.DeleteException;
import com.github.blausql.spi.connections.LoadException;
import com.github.blausql.spi.connections.SaveException;

import java.io.IOException;
import java.util.*;

/**
 * Default implementation of {@link ConnectionConfigurationRepository} that stores connection configurations
 * in a properties file.
 */
// CHECKSTYLE.OFF: AvoidInlineConditionals: here, they simplify trivial methods
final class PropertiesBasedConnectionConfigurationRepository implements ConnectionConfigurationRepository {

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

    private static final Comparator<ConnectionConfiguration> CONNECTION_CONFIGURATION_COMPARATOR =
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
    public List<ConnectionConfiguration> getConnectionConfigurations() throws LoadException {
        try {
            LinkedHashMap<String, ConnectionConfiguration> map = new LinkedHashMap<>();
            Properties properties = CONNECTIONS_PROPERTY_STORE.loadProperties();

            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                final String key = entry.getKey().toString();
                final String value = entry.getValue().toString();

                ConnectionPropertyReference propertyReference = ConnectionPropertyReference.fromString(key);

                ConnectionConfiguration connectionConfig = map.computeIfAbsent(
                        propertyReference.connectionName, ConnectionConfiguration::new);

                PropertyMapping propertyMapping = PropertyMapping.valueOf(propertyReference.propertyName);
                propertyMapping.setValue(connectionConfig, value);
            }

            ArrayList<ConnectionConfiguration> connectionConfigurations = new ArrayList<>(map.values());
            connectionConfigurations.sort(CONNECTION_CONFIGURATION_COMPARATOR);

            return connectionConfigurations;

        } catch (IOException e) {
            throw new LoadException("Failed to load connection configurations", e);
        }
    }

    @Override
    public void saveConnectionConfiguration(ConnectionConfiguration connectionConfig) throws SaveException {

        Objects.requireNonNull(connectionConfig, "Argument connectionConfig cannot be null");

        if (connectionConfig.getConnectionName() == null || connectionConfig.getConnectionName().isBlank()) {
            throw new SaveException("A connection must have a non-empty name");
        }

        Character hotkey = connectionConfig.getHotkey();
        if (hotkey != null) {
            checkNoExistingConnectionConfigurationUsesTheHotKey(connectionConfig, hotkey);
        }

        try {
            Properties properties = CONNECTIONS_PROPERTY_STORE.loadProperties();

            // Remove all existing properties for this connection
            properties.keySet().removeIf(key -> ConnectionPropertyReference.fromString(key.toString())
                    .connectionName.equalsIgnoreCase(connectionConfig.getConnectionName()));

            // Add the updated properties
            for (PropertyMapping propertyMapping : PropertyMapping.values()) {
                propertyMapping.putPropertyKeyValue(connectionConfig, properties);
            }

            CONNECTIONS_PROPERTY_STORE.persistProperties(properties);

        } catch (IOException e) {
            throw new SaveException("Failed to save connection configuration", e);
        }
    }

    private void checkNoExistingConnectionConfigurationUsesTheHotKey(
            ConnectionConfiguration configuration, Character hotkey) throws SaveException {

        List<ConnectionConfiguration> existingConnectionConfigurations;
        try {
            existingConnectionConfigurations = getConnectionConfigurations();
        } catch (LoadException e) {
            // unlike righteous error handling, this allows recovering from issues, e.g. a corrupted config file.
            existingConnectionConfigurations = Collections.emptyList();
        }

        for (ConnectionConfiguration existingConfiguration : existingConnectionConfigurations) {

            Character existingHotkey = existingConfiguration.getHotkey();

            if (existingHotkey != null
                    && !Objects.equals(existingConfiguration.getConnectionName(), configuration.getConnectionName())
                    && Objects.equals(
                        Character.toUpperCase(hotkey),
                        Character.toUpperCase(existingConfiguration.getHotkey()))) {

                String connectionName = existingConfiguration.getConnectionName();

                throw new SaveException("Hotkey '" + hotkey + "' is already used by: " + connectionName);
            }
        }
    }

    @Override
    public void deleteConnectionConfigurationByName(String connectionName) throws DeleteException {
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
                throw new IllegalStateException("Connection configuration not found:" + connectionName);
            }

            CONNECTIONS_PROPERTY_STORE.persistProperties(properties);

        } catch (IOException e) {
            throw new DeleteException("Failed to delete connection configuration", e);
        }
    }

    @Override
    public ConnectionConfiguration findConnectionConfigurationByName(String connectionName) throws LoadException {

        List<ConnectionConfiguration> connections = getConnectionConfigurations();

        for (ConnectionConfiguration connection : connections) {
            if (connectionName.equalsIgnoreCase(connection.getConnectionName())) {
                return connection;
            }
        }
        return null;
    }

    private enum PropertyMapping {
        ConnectionName {
            @Override
            String getValue(ConnectionConfiguration connectionConfig) {
                return connectionConfig.getConnectionName();
            }

            @Override
            void setValue(ConnectionConfiguration connectionConfig, String value) {
                connectionConfig.setConnectionName(value);
            }
        },
        DriverClassName {
            @Override
            String getValue(ConnectionConfiguration connectionConfig) {
                return connectionConfig.getDriverClassName();
            }

            @Override
            void setValue(ConnectionConfiguration connectionConfig, String value) {
                connectionConfig.setDriverClassName(value);
            }
        },
        JdbcUrl {
            @Override
            String getValue(ConnectionConfiguration connectionConfig) {
                return connectionConfig.getJdbcUrl();
            }

            @Override
            void setValue(ConnectionConfiguration connectionConfig, String value) {
                connectionConfig.setJdbcUrl(value);
            }
        },
        UserName {
            @Override
            String getValue(ConnectionConfiguration connectionConfig) {
                return connectionConfig.getUserName();
            }

            @Override
            void setValue(ConnectionConfiguration connectionConfig, String value) {
                connectionConfig.setUserName(value);
            }
        },
        Password {
            @Override
            String getValue(ConnectionConfiguration connectionConfig) {
                return connectionConfig.getPassword();
            }

            @Override
            void setValue(ConnectionConfiguration connectionConfig, String value) {
                connectionConfig.setPassword(value);
            }
        },
        LoginAutomatically {
            @Override
            String getValue(ConnectionConfiguration connectionConfig) {
                return Boolean.toString(connectionConfig.getLoginAutomatically());
            }

            @Override
            void setValue(ConnectionConfiguration connectionConfig, String value) {
                connectionConfig.setLoginAutomatically(Boolean.parseBoolean(value));
            }
        },
        StatementSeparator {
            @Override
            String getValue(ConnectionConfiguration connectionConfig) {
                String theStatementSeparator = connectionConfig.getStatementSeparator();
                return theStatementSeparator != null ? theStatementSeparator : STATEMENT_SEPARATOR;
            }

            @Override
            void setValue(ConnectionConfiguration connectionConfig, String value) {
                connectionConfig.setStatementSeparator(value);
            }
        },
        Hotkey {
            @Override
            String getValue(ConnectionConfiguration connectionConfig) {
                Character hotkey = connectionConfig.getHotkey();
                return hotkey != null ? hotkey.toString() : "";
            }

            @Override
            void setValue(ConnectionConfiguration connectionConfig, String value) {
                connectionConfig.setHotkey(value != null && !value.isEmpty() ? value.charAt(0) : null);
            }
        },
        Order {
            @Override
            String getValue(ConnectionConfiguration connectionConfig) {
                Integer order = connectionConfig.getOrder();
                return order != null ? order.toString() : "";
            }

            @Override
            void setValue(ConnectionConfiguration connectionConfig, String value) {
                connectionConfig.setOrder(value != null && !value.isEmpty() ? Integer.valueOf(value) : null);
            }
        };

        abstract String getValue(ConnectionConfiguration connectionConfig);

        abstract void setValue(ConnectionConfiguration connectionConfig, String value);

        void putPropertyKeyValue(ConnectionConfiguration connectionConfig, Properties properties) {
            String value = getValue(connectionConfig);
            if (value != null && !value.isEmpty()) {
                properties.setProperty(connectionConfig.getConnectionName() + "." + name(), value);
            }
        }
    }
}
// CHECKSTYLE.ON
