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

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public final class DatabaseConnection {

    private static final RowMapperResultSetExtractor<Map<String, Object>> ROW_MAPPER_RESULT_SET_EXTRACTOR =
            new RowMapperResultSetExtractor<>(new ColumnMapRowMapper());

    private final JdbcTemplate jdbcTemplate;

    static DatabaseConnection fromConnectionDefinition(
            ConnectionDefinition cd) {
        return new DatabaseConnection(cd.getDriverClassName(),
                cd.getJdbcUrl(), cd.getUserName(), cd.getPassword());
    }

    DatabaseConnection(String driverClassName, String url, String username, String password) {

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

    void establishConnection() {
        try {

            ((SingleConnectionDataSource) jdbcTemplate.getDataSource()).initConnection();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to establish connection", e);
        }
    }


    public StatementResult executeStatement(final String sql, int limit) {

        jdbcTemplate.setMaxRows(limit);

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


    public void disconnect() {
        ((SingleConnectionDataSource) jdbcTemplate.getDataSource()).resetConnection();
    }
}
