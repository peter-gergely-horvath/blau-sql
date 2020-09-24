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

 
/*
 * Copyright (c) 2016, 2017-2020 Peter G. Horvath, All Rights Reserved.
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
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Version {

    public static final String VERSION_STRING;


    private static final String VERSION_PROPERTIES_FILE = "version.properties";
    private static final String VERSION_STRING_KEY = "version";
    private static final String UNKNOWN_VERSION_STRING = "(unknown)";

    private static final Logger LOGGER = Logger.getLogger(Version.class.getName());

    static {

        Properties properties = new Properties();

        try (InputStream is = Version.class.getResourceAsStream(VERSION_PROPERTIES_FILE)) {

            if (is != null) {

                properties.load(is);

            } else {
                LOGGER.log(Level.SEVERE,
                        "Cannot establish version. File not found on classpath: "
                                + VERSION_PROPERTIES_FILE);
            }

        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE,
                    "Cannot establish version. Could not load properties file from classpath: "
                            + VERSION_PROPERTIES_FILE, t);

        }

        VERSION_STRING = properties.getProperty(Version.VERSION_STRING_KEY, UNKNOWN_VERSION_STRING);
    }

    private Version() {
        // static utility class -- no instances allowed
    }


}
