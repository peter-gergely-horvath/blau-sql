package com.github.blausql.core.classloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public final class ClasspathHelper {

    public static final String FILE_SEPARATOR = System.getProperty("path.separator");


    private ClasspathHelper() {
        // no instnces
    }

    public static URL[] getUrlsFromClasspathString(String classpathString) throws MalformedURLException {

        String[] entriesFromClasspathString = classpathString.split(FILE_SEPARATOR);

        URL[] urlList = new URL[entriesFromClasspathString.length];

        for (int i = 0; i < entriesFromClasspathString.length; i++) {

            URL url = new File(entriesFromClasspathString[i]).toURI().toURL();

            urlList[i] = url;
        }

        return urlList;
    }
}
