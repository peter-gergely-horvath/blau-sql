package com.github.blausql.core.classloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public final class ClasspathHelper {


    private ClasspathHelper() {
        // no instances
    }

    public static URL[] convertToURLs(String[] urlStrings) throws MalformedURLException {
        URL[] urlList = new URL[urlStrings.length];

        for (int i = 0; i < urlStrings.length; i++) {

            URL url = new File(urlStrings[i]).toURI().toURL();

            urlList[i] = url;
        }

        return urlList;
    }
}
