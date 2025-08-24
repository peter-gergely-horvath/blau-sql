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

import java.io.IOException;
import java.util.*;

public final class ConnectionDefinitionRepository {

    private static final String PROPERTY_SEPARATOR = "\\.";

    private static final PropertyStore CONNECTIONS_PROPERTY_STORE = PropertyStoreFactory.getConnectionsPropertyStore();

    private static final Comparator<ConnectionDefinition> CONNECTION_DEFINITION_COMPARATOR =
            new Comparator<ConnectionDefinition>() {

                @Override
                public int compare(ConnectionDefinition left, ConnectionDefinition right) {
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
                }
            };

    public static ConnectionDefinitionRepository getInstance() {
        return INSTANCE;
    }

    private static final ConnectionDefinitionRepository INSTANCE = new ConnectionDefinitionRepository();

    public List<ConnectionDefinition> getConnectionDefinitions() throws LoadException {
        try {

            LinkedHashMap<String, ConnectionDefinition> map = new LinkedHashMap<>();

            Properties properties = CONNECTIONS_PROPERTY_STORE.loadProperties();

            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                final String key = entry.getKey().toString();
                final String value = entry.getValue().toString();

                final String[] splitString = key.split(PROPERTY_SEPARATOR);
                if (splitString.length != 2) {
                    throw new IllegalStateException("Unknown property found: " + key);
                }

                final String connectionDefinitionName = splitString[0];
                final String propertyName = splitString[1];

                ConnectionDefinition cd = map.computeIfAbsent(connectionDefinitionName, ConnectionDefinition::new);

                PropertyMapping propertyMapping =
                        PropertyMapping.valueOf(propertyName);
                propertyMapping.setValue(cd, value);
            }

            ArrayList<ConnectionDefinition> connectionDefinitions = new ArrayList<>(map.values());

            Collections.sort(connectionDefinitions, CONNECTION_DEFINITION_COMPARATOR);

            return connectionDefinitions;

        } catch (IOException e) {
            throw new LoadException("Failed to load connection definitions", e);
        }

    }

    public void saveConnectionDefinition(ConnectionDefinition cd) throws SaveException {

        Character hotkey = cd.getHotkey();
        if (hotkey != null) {
            List<ConnectionDefinition> existingConnectionDefinitions;
            try {
                existingConnectionDefinitions = getConnectionDefinitions();
            } catch (LoadException e) {
                existingConnectionDefinitions = Collections.emptyList();
            }

            for (ConnectionDefinition existingConnectionDefinition : existingConnectionDefinitions) {
                if (!Objects.equals(existingConnectionDefinition.getConnectionName(), cd.getConnectionName())
                        && Objects.equals(
                            Character.toUpperCase(hotkey),
                            Character.toUpperCase(existingConnectionDefinition.getHotkey()))) {

                    String connectionName = existingConnectionDefinition.getConnectionName();
                    throw new SaveException("Hotkey '" + hotkey + "' is already used by: " + connectionName);
                }
            }
        }

        try {
            Properties properties = CONNECTIONS_PROPERTY_STORE.loadProperties();

            for (PropertyMapping propertyMapping : PropertyMapping.values()) {
                propertyMapping.putPropertyKeyValue(cd, properties);
            }

            CONNECTIONS_PROPERTY_STORE.persistProperties(properties);
        } catch (IOException e) {
            throw new SaveException("Failed to save connection definition", e);
        }

    }

    public void deleteConnectionDefinitionByName(String connectionName) {

        try {

            boolean foundInProperties = false;

            Properties properties = CONNECTIONS_PROPERTY_STORE.loadProperties();

            Iterator<Object> iterator = properties.keySet().iterator();
            while (iterator.hasNext()) {

                String key = iterator.next().toString();

                if (key.split(PROPERTY_SEPARATOR)[0].equals(connectionName)) {
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


    private enum PropertyMapping {
        connectionName {
            @Override
            String getValue(ConnectionDefinition cd) {
                return cd.getConnectionName();
            }

            @Override
            void setValue(ConnectionDefinition cd, String value) {
                cd.setConnectionName(value);
            }
        },

        driverClassName {
            @Override
            String getValue(ConnectionDefinition cd) {
                return cd.getDriverClassName();
            }

            @Override
            void setValue(ConnectionDefinition cd, String value) {
                cd.setDriverClassName(value);

            }
        },
        jdbcUrl {
            @Override
            String getValue(ConnectionDefinition cd) {
                return cd.getJdbcUrl();
            }

            @Override
            void setValue(ConnectionDefinition cd, String value) {
                cd.setJdbcUrl(value);

            }
        },
        loginAutomatically {
            @Override
            String getValue(ConnectionDefinition cd) {
                return Boolean.toString(cd.getLoginAutomatically());
            }

            @Override
            void setValue(ConnectionDefinition cd, String value) {
                cd.setLoginAutomatically(Boolean.parseBoolean(value));
            }
        },
        userName {
            @Override
            String getValue(ConnectionDefinition cd) {
                return cd.getUserName();
            }

            @Override
            void setValue(ConnectionDefinition cd, String value) {
                cd.setUserName(value);

            }
        },
        password {
            @Override
            String getValue(ConnectionDefinition cd) {
                return cd.getPassword();
            }

            @Override
            void setValue(ConnectionDefinition cd, String value) {
                cd.setPassword(value);

            }
        },
        statementSeparator {
            @Override
            String getValue(ConnectionDefinition cd) {
                return cd.getStatementSeparator();
            }

            @Override
            void setValue(ConnectionDefinition cd, String value) {
                cd.setStatementSeparator(value);

            }
        },
        hotkey {
            @Override
            String getValue(ConnectionDefinition cd) {
                Character hotkey = cd.getHotkey();
                String stringRepresentation;
                if (hotkey != null) {
                    stringRepresentation = new String(new char[]{hotkey});
                } else {
                    stringRepresentation = "";
                }
                return stringRepresentation;
            }

            @Override
            void setValue(ConnectionDefinition cd, String value) {
                if (value != null && value.trim().length() == 1) {
                    char hotkeyChar = value.charAt(0);
                    cd.setHotkey(hotkeyChar);
                } else {
                    cd.setHotkey(null);
                }
            }
        },
        order {
            @Override
            String getValue(ConnectionDefinition cd) {
                Integer order = cd.getOrder();
                if (order != null) {
                    return order.toString();
                } else {
                    return "";
                }
            }

            @Override
            void setValue(ConnectionDefinition cd, String value) {
                if (value == null || value.trim().isEmpty()) {
                    cd.setOrder(null);
                } else {
                    try {
                        int parsedIntValue = Integer.parseInt(value.trim());
                        cd.setOrder(parsedIntValue);

                    } catch (NumberFormatException nfe) {
                        throw new RuntimeException("Could not map value '" + value + "' to Integer", nfe);
                    }
                }
            }
        };

        private String getQualifiedUniquePropertyName(ConnectionDefinition cd) {
            return String.format("%s.%s", cd.getConnectionName(), this.name());
        }

        private void putPropertyKeyValue(ConnectionDefinition cd, Properties properties) {
            if (connectionName != this) {
                String propertyKey = getQualifiedUniquePropertyName(cd);
                String propertyValue = getValue(cd);


                properties.put(propertyKey, propertyValue);
            }
        }

        abstract String getValue(ConnectionDefinition cd);

        abstract void setValue(ConnectionDefinition cd, String value);
    }

    public ConnectionDefinition findConnectionDefinitionByName(
            String connectionName) throws LoadException {

        Objects.requireNonNull(connectionName, "connectionName cannot be null");

        List<ConnectionDefinition> connectionDefinitions = getConnectionDefinitions();
        for (ConnectionDefinition connectionDefinition : connectionDefinitions) {
            if (connectionName.equalsIgnoreCase(connectionDefinition.getConnectionName())) {
                return connectionDefinition;
            }
        }

        return null;
    }

}
