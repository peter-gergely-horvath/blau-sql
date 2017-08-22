/*
 * Copyright (c) 2016, 2017 Peter G. Horvath, All Rights Reserved.
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

 
package com.github.blausql;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public final class Version {

    private static final int VERSION_MAJOR;
    private static final int VERSION_MINOR;
    private static final int VERSION_PATCH;

    private static final String VERSION_PROPERTIES = "version.properties";

    private static final String VERSION = "version";

    private static final String VERSION_SEPARATOR = "\\.";

    private static final int ZERO_VERSION_NUMBER = 0;

    private static final int VERSION_INDEX_MAJOR = 0;
    private static final int VERSION_INDEX_MINOR = 1;
    private static final int VERSION_INDEX_PATCH = 2;

    private static final Logger LOGGER = Logger.getLogger(Version.class.getName());

    static {
        Properties props = null;

        String versionString = tryGetVersionStringFromManifest();
        if (versionString == null) {
            props = tryLoadPropertiesFromClasspath();
        }

        versionString = tryGetVersionStringFromProperties(props);

        if (versionString != null) {

            String[] splitVersionString = versionString.split(VERSION_SEPARATOR);

            VERSION_MAJOR = safeParseToVersion(splitVersionString, VERSION_INDEX_MAJOR);
            VERSION_MINOR = safeParseToVersion(splitVersionString, VERSION_INDEX_MINOR);
            VERSION_PATCH = safeParseToVersion(splitVersionString, VERSION_INDEX_PATCH);

        } else {
            VERSION_MAJOR = ZERO_VERSION_NUMBER;
            VERSION_MINOR = ZERO_VERSION_NUMBER;
            VERSION_PATCH = ZERO_VERSION_NUMBER;
        }


    }

    private Version() {
        // static utility class -- no instances allowed
    }

    public static String getVersionString() {
        return String.format("%s.%s.%s", VERSION_MAJOR, VERSION_MINOR, VERSION_PATCH);
    }

    private static int safeParseToVersion(String[] versionStrings, int index) {
        try {
            if (versionStrings.length <= index) {
                return ZERO_VERSION_NUMBER;
            }

            return Integer.parseInt(versionStrings[index]);
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    private static String tryGetVersionStringFromManifest() {
        String version = null;

        Package aPackage = Version.class.getPackage();
        if (aPackage != null) {
            version = aPackage.getImplementationVersion();
            if (version == null) {
                version = aPackage.getSpecificationVersion();
            }
        }

        return version;
    }

    private static Properties tryLoadPropertiesFromClasspath() {

        Properties properties = null;

        try (InputStream is = Version.class.getResourceAsStream(VERSION_PROPERTIES)) {

            if (is != null) {
                properties = new Properties();
                properties.load(is);
            }

        } catch (Throwable t) {
            LOGGER.warning("Could not load properties file: " + t.getMessage());
        }

        return properties;
    }

    private static String tryGetVersionStringFromProperties(Properties props) {

        String returnValue = null;

        if (props != null) {
            returnValue = props.getProperty(Version.VERSION, null);
        }

        return returnValue;
    }


}
