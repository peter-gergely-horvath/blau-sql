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

import java.io.File;

final class PropertyStoreFactory {

    private PropertyStoreFactory() {
        // no instances allowed
    }

    private static final File USER_HOME = new File(System.getProperty("user.home"));

    public static final File STORAGE_DIRECTORY = new File(USER_HOME, ".blauSQL");

    private static final File SETTINGS_PROPERTIES_FILE = new File(STORAGE_DIRECTORY, "settings.properties");

    private static final PropertyStore SETTINGS_PROPERTY_STORE = new PropertyStore(SETTINGS_PROPERTIES_FILE);

    private static final File CONNECTIONS_PROPERTIES_FILE = new File(STORAGE_DIRECTORY, "connections.properties");

    private static final PropertyStore CONNECTIONS_PROPERTY_STORE = new PropertyStore(CONNECTIONS_PROPERTIES_FILE);

    static PropertyStore getSettingsPropertyStore() {
        return SETTINGS_PROPERTY_STORE;
    }


    static PropertyStore getConnectionsPropertyStore() {
        return CONNECTIONS_PROPERTY_STORE;
    }

}
