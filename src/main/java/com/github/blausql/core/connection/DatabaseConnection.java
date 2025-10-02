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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DatabaseConnection {

    private final Connection connection;

    DatabaseConnection(Connection connection) {

        this.connection = connection;
    }

    public StatementResult executeStatement(String sql, int limit) {

        try {
            if (connection.isClosed()) {
                throw new IllegalStateException("Connection is closed");
            }

            try (Statement stmt = connection.createStatement()) {
                if (limit > 0) {
                    stmt.setMaxRows(limit);
                }

                boolean yieldedResultSet = stmt.execute(sql);

                if (yieldedResultSet) {
                    try (ResultSet resultSet = stmt.getResultSet()) {
                        List<Map<String, Object>> queryResult = extractResultSet(resultSet, limit);
                        return new StatementResult(true, queryResult, -1);
                    }
                } else {
                    int updateCount = stmt.getUpdateCount();
                    return new StatementResult(false, null, updateCount);
                }
            }
        } catch (SQLException e) {
            throw new QueryExecutionException(e);
        }
    }

    private List<Map<String, Object>> extractResultSet(ResultSet resultSet, int limit) throws SQLException {
        List<Map<String, Object>> resultList = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        // Resolve column labels once for efficiency
        List<String> columnNames = new ArrayList<>(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnLabel(i);
            if (columnName == null || columnName.isEmpty()) {
                columnName = metaData.getColumnName(i);
            }
            columnNames.add(columnName);
        }

        int processed = 0;
        while (resultSet.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = columnNames.get(i - 1);
                row.put(columnName, resultSet.getObject(i));
            }
            resultList.add(row);

            processed++;
            if (limit > 0 && processed >= limit) {
                break; // Defensive cap in case the driver ignores Statement.setMaxRows
            }
        }

        return resultList;
    }


    public void close() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Error disconnecting from database", e);
            }
        }
    }
}
