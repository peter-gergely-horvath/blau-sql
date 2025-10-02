/*
 * Copyright (c) 2017-2025 Peter G. Horvath, All Rights Reserved.
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


package com.github.blausql.core.connection;

import com.github.blausql.core.classloader.ClassLoaderFactory;
import com.github.blausql.core.classloader.DelegatingDriver;
import com.github.blausql.core.preferences.ConfigurationRepository;
import com.github.blausql.spi.connections.LoadException;

import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.sql.*;
import java.util.List;

public final class DatabaseConnectionFactory {

    private DatabaseConnectionFactory() {
        // no external instances
    }

    public static DatabaseConnection getDatabaseConnection(ConnectionDefinition cd) {

        ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            List<String> classpath = ConfigurationRepository.getInstance().getClasspath();

            if (!classpath.isEmpty()) {

                ClassLoader classLoader = ClassLoaderFactory.getClassLoaderForClasspath(classpath);
                Thread.currentThread().setContextClassLoader(classLoader);
            }

            String driverClassName = cd.getDriverClassName();
            if (driverClassName != null && !driverClassName.isBlank()) {
                initDriver(driverClassName);
            }

            String jdbcUrl = cd.getJdbcUrl();
            String userName = cd.getUserName();
            String password = cd.getPassword();

            Connection connection = DriverManager.getConnection(jdbcUrl, userName, password);

            return new DatabaseConnection(connection);


        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Malformed URL in configured classpath: " + e.getMessage(), e);

        } catch (SQLException e) {
            throw new IllegalStateException("Failure establishing the connection", e);

        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failure loading the JDBC driver", e);

        } catch (LoadException e) {
            throw new IllegalStateException("Failure loading configuration", e);

        } finally {

            Thread.currentThread().setContextClassLoader(originalContextClassLoader);
        }
    }

    private static void initDriver(String driverClassName) throws ReflectiveOperationException, SQLException {

        /*
        Work-around for the limitation of DriverManager, that prevents loading a JDBC driver
        from a custom class loader: we manually register the driver by instantiating it via reflection,
        and wrapping it in a DelegatingDriver, which delegates all calls to the actual driver instance.

        DriverManager perform tasks using the immediate caller's class loader: Guideline 9-9 / ACCESS-9:
        https://www.oracle.com/java/technologies/javase/seccodeguide.html

        This work-around is based on the StackOverflow thread:
        https://stackoverflow.com/questions/288828/how-to-use-a-jdbc-driver-from-an-arbitrary-location/288941
         */

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        Class<?> loadedClass = Class.forName(driverClassName,     true, contextClassLoader);
        if (!Driver.class.isAssignableFrom(loadedClass)) {
            throw new IllegalArgumentException(
                    "The specified driver class does not implement java.sql.Driver: " + driverClassName);
        }

        @SuppressWarnings("unchecked") // we just checked the type above
        Class<Driver> driverClass = (Class<Driver>) loadedClass;
        Constructor<Driver> declaredConstructor = driverClass.getDeclaredConstructor();

        Driver driver = declaredConstructor.newInstance();

        DriverManager.registerDriver(new DelegatingDriver(driver));
    }
}
