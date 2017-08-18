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
            throws FileNotFoundException, IOException {

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
            throws FileNotFoundException, IOException {

        if (properties == null) {
            throw new IllegalStateException("Cannot persist null properties");
        }

        propertyFile.getParentFile().mkdirs();

        try (FileOutputStream fileOutputStream = new FileOutputStream(propertyFile)) {
            properties.store(fileOutputStream, "");
            loadedProperties = properties;
        }

    }
}
