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

import java.io.*;
import java.util.Properties;

final class PropertyStore {

    private Properties loadedProperties;
    private final File propertyFile;

    PropertyStore(File propertyFile) {
        this.propertyFile = propertyFile;
    }


    synchronized Properties loadProperties()
            throws IOException {

        if (loadedProperties == null) {
            loadedProperties = new Properties();
            if (propertyFile.exists()) {
                try (FileInputStream inStream = new FileInputStream(propertyFile)) {
                    loadedProperties.load(inStream);
                }
            }
        }

        return loadedProperties;
    }

    synchronized void persistProperties(Properties properties)
            throws IOException {

        if (properties == null) {
            throw new IllegalStateException("Cannot persist null properties");
        }

        File containerDirectory = propertyFile.getParentFile();
        boolean createdParent = containerDirectory.mkdirs();
        if (!createdParent && !containerDirectory.exists()) {
            throw new IOException("Directory does not exist and could not be created: "
                    + containerDirectory.getAbsolutePath());

        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(propertyFile)) {
            properties.store(fileOutputStream, "");
            loadedProperties = properties;
        }

    }
}
