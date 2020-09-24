package com.github.blausql.core.preferences;

import com.github.blausql.core.storage.StorageService;

import java.io.File;

final class PropertyStoreFactory {

    private PropertyStoreFactory() {
        // no instances allowed
    }

    public static final File STORAGE_DIRECTORY = StorageService.getInstance().getApplicationSettingsDirectory();

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
