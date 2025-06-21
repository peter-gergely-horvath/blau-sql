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


package com.github.blausql.core.connection;

import com.github.blausql.core.classloader.ClassLoaderFactory;
import com.github.blausql.core.classloader.DelegatingDriver;
import com.github.blausql.core.preferences.ConfigurationRepository;
import com.github.blausql.core.preferences.LoadException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.sql.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class Database {

    private final AtomicReference<DatabaseConnection> currentConnectionHolder = new AtomicReference<>();


    private static final Database INSTANCE = new Database();

    private Database() {
        // no external instances
    }

    public static Database getInstance() {
        return INSTANCE;
    }

    public void establishConnection(ConnectionDefinition cd) {

        DatabaseConnection existingConn = currentConnectionHolder.get();
        if (existingConn != null) {
            throw new IllegalStateException(
                    "Current connection exists: must close it first");
        }

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


            DatabaseConnection databaseConnection = DatabaseConnection.fromConnectionDefinition(cd);

            databaseConnection.establishConnection();

            currentConnectionHolder.set(databaseConnection);


        } catch (MalformedURLException e) {
            throw new RuntimeException(
                    "Malformed URL: " + e.getMessage());

        } catch (ReflectiveOperationException | SQLException e) {
            throw new RuntimeException("Failure loading the JDBC driver", e);
        } catch (LoadException e) {
            throw new RuntimeException("Failure loading Configuration", e);
        } finally {
            Thread.currentThread().setContextClassLoader(originalContextClassLoader);
        }


    }

    private void initDriver(String driverClassName)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, NoSuchMethodException, InvocationTargetException {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        @SuppressWarnings("unchecked")
        Class<Driver> driverClass = (Class<Driver>) Class.forName(driverClassName, true, contextClassLoader);
        Constructor<Driver> declaredConstructor = driverClass.getDeclaredConstructor();
        Driver driver = declaredConstructor.newInstance();
        DriverManager.registerDriver(new DelegatingDriver(driver));
    }

    public void disconnect() {

        DatabaseConnection existingConn = currentConnectionHolder.get();
        if (existingConn == null) {
            throw new IllegalStateException(
                    "No current connection: must establish first");
        }

        existingConn.disconnect();

        currentConnectionHolder.set(null);
    }

    public StatementResult executeStatement(final String sql, int limit) {

        DatabaseConnection databaseConnection = currentConnectionHolder.get();
        if (databaseConnection == null) {
            throw new IllegalStateException("databaseConnection is null");
        }

        return databaseConnection.executeStatement(sql, limit);
    }
}
