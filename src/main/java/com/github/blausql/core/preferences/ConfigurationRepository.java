/*
 * Copyright (c) 2017-2020 Peter G. Horvath, All Rights Reserved.
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

import com.github.blausql.core.util.TextUtils;

import java.io.IOException;
import java.util.Properties;

public final class ConfigurationRepository {

    private static final PropertyStore SETTINGS_PROPERTY_STORE = PropertyStoreFactory.getSettingsPropertyStore();

    public static ConfigurationRepository getInstance() {
        return INSTANCE;
    }

    private static final ConfigurationRepository INSTANCE = new ConfigurationRepository();

    private static final String CLASSPATH_SEPARATOR_CHAR = "|";

    private static class Keys {
        private static final String CLASSPATH = "classpath";
    }

    public void saveClasspath(String[] entries) throws SaveException {

        try {
            Properties properties = SETTINGS_PROPERTY_STORE.loadProperties();

            String classpathString = TextUtils.joinStringsWithSeparator(CLASSPATH_SEPARATOR_CHAR, entries);

            properties.put(Keys.CLASSPATH, classpathString);

            SETTINGS_PROPERTY_STORE.persistProperties(properties);

        } catch (IOException e) {
            throw new SaveException("Failed to save configuration", e);
        }
    }


    public String[] getClasspath() throws LoadException {

        try {
            Properties properties = SETTINGS_PROPERTY_STORE.loadProperties();

            String classpath = properties.getProperty(Keys.CLASSPATH, "");

            return classpath.split("\\" + CLASSPATH_SEPARATOR_CHAR);

        } catch (IOException e) {
            throw new LoadException("Failed to read configuration", e);
        }
    }

}
