package com.github.blausql.core.classloader;

import org.springframework.util.Assert;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;

public final class ClassLoaderFactory {

    private static final String FILE_SEPARATOR_PROPERTY_NAME = "path.separator";

    private ClassLoaderFactory() {
        // no external instances
    }

    public static ClassLoader getClassLoaderForClasspathString(String classpathString) throws MalformedURLException {

        Assert.notNull(classpathString, "argument classpathString cannot be null");

        String fileSeparator = System.getProperty(FILE_SEPARATOR_PROPERTY_NAME);

        String[] entriesFromClasspathString = classpathString.split(fileSeparator);

        final ArrayList<URL> urlList = new ArrayList<URL>(entriesFromClasspathString.length);

        for (String classpathEntry : entriesFromClasspathString) {

            URL url = new File(classpathEntry).toURI().toURL();

            urlList.add(url);
        }

        ClassLoader classLoader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {

            public ClassLoader run() {
                return new URLClassLoader(urlList.toArray(new URL[urlList.size()]));
            }
        });

        return classLoader;
    }


}
