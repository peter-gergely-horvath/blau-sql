/*
 * Copyright (c) 2017 Peter G. Horvath, All Rights Reserved.
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
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.util.Assert;

import java.net.MalformedURLException;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public final class Database {

    private final AtomicReference<DatabaseConnection> currentConnectionHolder = new AtomicReference<>();

    private static final RowMapperResultSetExtractor<Map<String, Object>> ROW_MAPPER_RESULT_SET_EXTRACTOR =
            new RowMapperResultSetExtractor<>(new ColumnMapRowMapper());

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
            String[] classpath = ConfigurationRepository.getInstance().getClasspath();

            if (classpath.length != 0) {

                ClassLoader classLoader = ClassLoaderFactory.getClassLoaderForClasspath(classpath);
                Thread.currentThread().setContextClassLoader(classLoader);
            }

            String driverClassName = cd.getDriverClassName();
            if (driverClassName != null && !"".equals(driverClassName.trim())) {
                initDriver(driverClassName);
            }


            DatabaseConnection databaseConnection = DatabaseConnection.fromConnectionDefinition(cd);

            databaseConnection.establishConnection();

            currentConnectionHolder.set(databaseConnection);


        } catch (MalformedURLException e) {
            throw new RuntimeException(
                    "Malformed URL: " + e.getMessage());

        } catch (IllegalAccessException | InstantiationException | SQLException e) {
            throw new RuntimeException("Problem loading the driver", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not load JDBC Driver class: " + e.getMessage(), e);
        } catch (LoadException e) {
            throw new RuntimeException("Failed to load Configuration", e);
        } finally {
            Thread.currentThread().setContextClassLoader(originalContextClassLoader);
        }


    }

    private void initDriver(String driverClassName)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        @SuppressWarnings("unchecked")
        Class<Driver> driverClass = (Class<Driver>) Class.forName(driverClassName, true, contextClassLoader);
        Driver driver = driverClass.newInstance();
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

    public StatementResult executeStatement(final String sql) {

        JdbcTemplate jdbcTemplate = currentConnectionHolder.get().jdbcTemplate;

        return jdbcTemplate.execute(new StatementCallback<StatementResult>() {

            public StatementResult doInStatement(Statement stmt)
                    throws SQLException, DataAccessException {

                List<Map<String, Object>> queryResult = null;
                int updateCount = -1;

                final boolean yieldedResultSet = stmt.execute(sql);
                if (yieldedResultSet) {
                    try (ResultSet resultSet = stmt.getResultSet()) {

                        queryResult = ROW_MAPPER_RESULT_SET_EXTRACTOR.extractData(resultSet);

                    } catch (SQLException e) {

                        throw new RuntimeException("ResultSet processing failed", e);

                    }
                } else {
                    updateCount = stmt.getUpdateCount();

                }

                return new StatementResult(yieldedResultSet, queryResult, updateCount);
            }
        });

    }

    public static final class StatementResult {

        private final boolean isResultSet;
        private final List<Map<String, Object>> queryResult;
        private final int updateCount;

        private StatementResult(boolean isResultSet,
                                List<Map<String, Object>> queryResult, int updateCount) {

            this.isResultSet = isResultSet;
            this.queryResult = queryResult;
            this.updateCount = updateCount;
        }

        public boolean isResultSet() {
            return isResultSet;
        }

        public List<Map<String, Object>> getQueryResult() {
            Assert.isTrue(isResultSet, "Statement yielded update count");
            return queryResult;
        }

        public int getUpdateCount() {
            Assert.isTrue(!isResultSet, "Statement yielded result set");
            return updateCount;
        }
    }

    private static final class DatabaseConnection {

        private final JdbcTemplate jdbcTemplate;

        private static DatabaseConnection fromConnectionDefinition(
                ConnectionDefinition cd) {
            return new DatabaseConnection(cd.getDriverClassName(),
                    cd.getJdbcUrl(), cd.getUserName(), cd.getPassword());
        }

        private DatabaseConnection(String driverClassName, String url, String username, String password) {

            SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
            if (driverClassName != null && !"".equals(driverClassName.trim())) {
                dataSource.setDriverClassName(driverClassName);
            }

            dataSource.setUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(password);

            dataSource.setSuppressClose(false);

            this.jdbcTemplate = new JdbcTemplate(dataSource);
        }

        private void establishConnection() {
            try {

                ((SingleConnectionDataSource) jdbcTemplate.getDataSource()).initConnection();

            } catch (SQLException e) {
                throw new RuntimeException("Failed to establish connection", e);
            }
        }

        private void disconnect() {
            ((SingleConnectionDataSource) jdbcTemplate.getDataSource()).resetConnection();
        }

    }

}
