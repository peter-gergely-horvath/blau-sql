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
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

public final class ClasspathHelper {


    private ClasspathHelper() {
        // no instances
    }

    public static URL[] convertToURLs(String[] urlStrings) throws MalformedURLException {

        LinkedList<URL> allUrls = new LinkedList<>();

        for (String anUrl : urlStrings) {

            File file = new File(anUrl);

            if (anUrl.endsWith("/")) {
                File[] filesInDirectory = file.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".jar");
                    }
                });

                if (filesInDirectory == null) {
                    throw new IllegalArgumentException("The specified directory contains no .jar files: " + file);
                }

                for (File fileInTheDirectory : filesInDirectory) {
                    allUrls.add(fileInTheDirectory.toURI().toURL());
                }

            } else {
                allUrls.add(file.toURI().toURL());
            }

        }

        return allUrls.toArray(new URL[0]);
    }
}
