package com.github.blausql.core.connection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.util.Assert;

public class Database {

	private final AtomicReference<DatabaseConnection> currentConnectionHolder = new AtomicReference<DatabaseConnection>();

	private static final RowMapperResultSetExtractor<Map<String, Object>> ROW_MAPPER_RESULT_SET_EXTRACTOR =
			new RowMapperResultSetExtractor<Map<String, Object>>(new ColumnMapRowMapper());

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

		DatabaseConnection databaseConnection = DatabaseConnection
				.fromConnectionDefinition(cd);

		databaseConnection.estabilish();

		currentConnectionHolder.set(databaseConnection);
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
					ResultSet resultSet = stmt.getResultSet();
					try {

						queryResult = ROW_MAPPER_RESULT_SET_EXTRACTOR
								.extractData(resultSet);

					} catch (SQLException e) {

						throw new RuntimeException(
								"ResultSet processing failed", e);

					} finally {
						resultSet.close();
					}
				} else {
					updateCount = stmt.getUpdateCount();

				}

				return new StatementResult(yieldedResultSet, queryResult,
						updateCount);
			}
		});

	}

	public static class StatementResult {

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

	private static class DatabaseConnection {

		private final JdbcTemplate jdbcTemplate;

		private static DatabaseConnection fromConnectionDefinition(
				ConnectionDefinition cd) {
			return new DatabaseConnection(cd.getDriverClassName(),
					cd.getJdbcUrl(), cd.getUserName(), cd.getPassword());
		}

		private DatabaseConnection(String driverClassName, String url,
				String username, String password) {
			super();

			SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
			dataSource.setDriverClassName(driverClassName);
			dataSource.setUrl(url);
			dataSource.setUsername(username);
			dataSource.setPassword(password);

			dataSource.setSuppressClose(false);

			this.jdbcTemplate = new JdbcTemplate(dataSource);
		}

		private void estabilish() {
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
