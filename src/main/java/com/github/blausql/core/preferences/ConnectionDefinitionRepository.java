/*
 * Copyright (c) 2017 Peter G. Horvath, All Rights Reserved.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.github.blausql.core.connection.ConnectionDefinition;

import com.google.common.base.Preconditions;

public class ConnectionDefinitionRepository {

	private static final String PROPERTY_SEPARATOR = "\\.";
	
	private static final String PROPERTY_FILE_NAME = ".blauSQL.properties";
	
	private static final String PROPERTY_FILE_PATH = String.format("%s%s%s", 
			System.getProperty("user.home"),
			System.getProperty("file.separator"),
			PROPERTY_FILE_NAME);
	
	private static final File PROPERTY_FILE = new File(PROPERTY_FILE_PATH);

	public static ConnectionDefinitionRepository getInstance() {
		return InstanceHolder.INSTANCE;
	}

	private static final class InstanceHolder {
		private static final ConnectionDefinitionRepository INSTANCE = new ConnectionDefinitionRepository();
	}
	
	public List<ConnectionDefinition> getConnectionDefinitions() {
		try {

			LinkedHashMap<String, ConnectionDefinition> map = new LinkedHashMap<String, ConnectionDefinition>();

			Properties properties = PropertyStore.loadProperties();

			for (Map.Entry<Object, Object> entry : properties.entrySet()) {
				final String key = entry.getKey().toString();
				final String value = entry.getValue().toString();

				final String[] splitString = key.split(PROPERTY_SEPARATOR);
				if (splitString.length != 2) {
					throw new IllegalStateException("Unknown property found: "
							+ key);
				}

				final String connectionDefinitionName = splitString[0];
				final String propertyName = splitString[1];

				ConnectionDefinition cd = map.get(connectionDefinitionName);
				if (cd == null) {
					cd = new ConnectionDefinition(connectionDefinitionName, null, null, false, null, null);
					map.put(connectionDefinitionName, cd);
				}

				PropertyMapping propertyMapping = 
						PropertyMapping.valueOf(propertyName);
				propertyMapping.setValue(cd, value);
			}

			return new ArrayList<ConnectionDefinition>(map.values());

		} catch (Exception e) {
			throw new RuntimeException("Failed to load connection definitions", e);
		}
	}

	public void saveConnectionDefinition(ConnectionDefinition cd) {

		try {
			Properties properties = PropertyStore.loadProperties();

			for (PropertyMapping propertyMapping : PropertyMapping.values()) {
				propertyMapping.putPropertyKeyValue(cd, properties);
			}

			PropertyStore.persistProperties();
		} catch (Exception e) {
			throw new RuntimeException("Failed to save connection definition", e);
		}

	}

	public void updateConnectionDefinition(ConnectionDefinition cd) {

		saveConnectionDefinition(cd);
	}

	public void deleteConnectionDefinitionByName(String connectionName) {

		try {
			
			boolean foundInProperties = false;

			Properties properties = PropertyStore.loadProperties();

			for (Iterator<Object> itetator = properties.keySet().iterator(); 
					itetator.hasNext();) {
				
				String key = itetator.next().toString();

				if (key.split(PROPERTY_SEPARATOR)[0].equals(connectionName)) {
					itetator.remove();
					foundInProperties = true;
				}
			}
			
			if(!foundInProperties) {
				throw new IllegalStateException("Connection definition not found:" + connectionName);
			}

			PropertyStore.persistProperties();
		} catch (Exception e) {
			throw new RuntimeException("Failed to delete connection definition", e);
		}

	}
	
	
	private static final class PropertyStore {

		private static Properties loadedProperties;

		private synchronized static Properties loadProperties()
				throws FileNotFoundException, IOException {

			if(loadedProperties == null) {
				loadedProperties = new Properties();
				if (PROPERTY_FILE.exists()) {
					loadedProperties.load(new FileInputStream(PROPERTY_FILE));
				}
			}
			
			return loadedProperties;
		}

		private synchronized static void persistProperties()
				throws FileNotFoundException, IOException {

			if(loadedProperties == null) {
				throw new IllegalStateException("Cannot persist properties: not loaded yet");
			}
			
			loadedProperties.store(new FileOutputStream(PROPERTY_FILE),
							"BlauSQL universal database client connection configurations");
		}
	}

	private static enum PropertyMapping {
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
		};
		
		private String getQualifiedUniquePropertyName(ConnectionDefinition cd) {
			return String.format("%s.%s", cd.getConnectionName(), PropertyMapping.this.name());
		}
		
		private void putPropertyKeyValue(ConnectionDefinition cd, Properties properties) {
			if(PropertyMapping.connectionName != PropertyMapping.this) {
				String propertyKey = this.getQualifiedUniquePropertyName(cd);
				String propertyValue = this.getValue(cd);
				
				
				properties.put(propertyKey, propertyValue);
			}
		}

		abstract String getValue(ConnectionDefinition cd);

		abstract void setValue(ConnectionDefinition cd, String value);
	}

	public ConnectionDefinition findConnectionDefinitionByName(
			String connectionName) {
		
		Preconditions.checkNotNull(connectionName, "connectionName cannot be null");
		
		List<ConnectionDefinition> connectionDefinitions = getConnectionDefinitions();
		for (ConnectionDefinition connectionDefinition : connectionDefinitions) {
			if(connectionName.equalsIgnoreCase(connectionDefinition.getConnectionName())) {
				return connectionDefinition;
			}
		}
		
		return null;
	}

}
