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

 
package com.github.blausql.core.classloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public final class ClasspathHelper {


    private ClasspathHelper() {
        // no instances
    }

    public static URL[] convertToURLs(List<String> urlStrings) throws MalformedURLException {

        return urlStrings.stream()
                .map(ClasspathHelper::toURL)
                .toArray(URL[]::new);
    }

    private static URL toURL(String urlString) {
        try {

            return new File(urlString).toURI().toURL();

        } catch (MalformedURLException e) {

            throw new RuntimeException("Failed to covert to URL: " + urlString, e);
        }
    }
}
