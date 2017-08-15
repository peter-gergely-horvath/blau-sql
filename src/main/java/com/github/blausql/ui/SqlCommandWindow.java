package com.github.blausql.ui;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.github.blausql.TerminalUI;
import com.github.blausql.core.connection.ConnectionDefinition;
import com.github.blausql.core.connection.Database;
import com.github.blausql.core.connection.Database.StatementResult;
import com.github.blausql.ui.util.BackgroundWorker;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.component.EditArea;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.input.Key.Kind;
import com.googlecode.lanterna.terminal.TerminalSize;

public class SqlCommandWindow extends Window {

	private final EditArea sqlEditArea;

	private Button executeSqlButton = new Button("Execute Query (CTRL+E)", new Action() {

		public void doAction() {
			onExecuteQueryButtonSelected();

		}
	});

	private final String connectionName;

	public SqlCommandWindow(ConnectionDefinition connectionDefinition) {
		super(connectionDefinition.getConnectionName());
		
		connectionName = connectionDefinition.getConnectionName();

		Panel bottomPanel = new Panel(new Border.Bevel(true),
				Panel.Orientation.HORISONTAL);
		Panel verticalPanel = new Panel(new Border.Invisible(),
				Panel.Orientation.VERTICAL);

		bottomPanel.addComponent(new Label(">>> Press TAB >>>"));

		bottomPanel.addComponent(executeSqlButton);

		bottomPanel.addComponent(new Button("Close connection (ESC)", new Action() {

			public void doAction() {
				SqlCommandWindow.this.onCloseConnectionButtonSelected();
			}

		}));

		TerminalSize screenTerminalSize = TerminalUI.getTerminalSize();

		final int sqlEditorPanelColumns = screenTerminalSize.getColumns() - 4;
		final int sqlEditorPanelRows = screenTerminalSize.getRows() - 4;

		sqlEditArea = new EditArea(new TerminalSize(sqlEditorPanelColumns,
				sqlEditorPanelRows));
		verticalPanel.addComponent(sqlEditArea);

		verticalPanel.addComponent(bottomPanel);

		addComponent(verticalPanel);
	}
	
	public void onKeyPressed(Key key) {
		
		if(Kind.NormalKey.equals(key.getKind()) && 
				(key.getCharacter() == 'E' || key.getCharacter() == 'e') &&   
				key.isCtrlPressed()) {
			
			onExecuteQueryButtonSelected();
			
		} else if(Kind.Escape.equals(key.getKind())) {
			
			onCloseConnectionButtonSelected();
			
		} else {
			super.onKeyPressed(key);
		}
	}
	
	protected void onExecuteQueryButtonSelected() {
		executeQuery(sqlEditArea.getData());
	}
	
	protected void onCloseConnectionButtonSelected() {
		Database.getInstance().disconnect();

		this.close();
	}

	protected void executeQuery(final String sqlCommand) {
		
		final Window showWaitDialog = TerminalUI.showWaitDialog("Please wait",
				String.format("Executing statement against %s ..." , connectionName));
		
		new BackgroundWorker<StatementResult>() {

			@Override
			protected StatementResult doBackgroundTask() {
				return Database.getInstance().executeStatement(sqlCommand);
			}

			@Override
			protected void onBackgroundTaskFailed(Throwable t) {
				showWaitDialog.close();
				TerminalUI.showErrorMessageFromThrowable(t);
				setFocus(sqlEditArea);

			}

			@Override
			protected void onBackgroundTaskCompleted(StatementResult statementResult) {
				
				showWaitDialog.close();

				setFocus(sqlEditArea);
				
				if(statementResult.isResultSet()) {
					
					final List<Map<String,Object>> queryResult = statementResult.getQueryResult();
					
					TerminalUI.showWindowFullScreen(new QueryResultWindow(queryResult));

				} else {
					
					final int updateCount = statementResult.getUpdateCount();
					
					final String message = String.format("%s row(s) changed", updateCount);
					
					TerminalUI.showMessageBox("Statement executed", message);

				}
			}
		}.start();
		
	}


	protected void onStatementYieldsResults(ResultSet resultSet)
			throws SQLException {
		
		final List<Map<String, String>> queryResult = new LinkedList<Map<String, String>>();
		
		synchronized (queryResult) {
			
			final ResultSetMetaData metaData = resultSet.getMetaData();
			final int columnCount = metaData.getColumnCount();

			ArrayList<String> columnLabels = new ArrayList<String>();

			for (int i = 0; i < columnCount; i++) {
				String columnLabel = metaData.getColumnLabel(i + 1);
				columnLabels.add(columnLabel);
			}

			while (resultSet.next()) {

				HashMap<String, String> lineMap = new HashMap<String, String>();

				for (int i = 0; i < columnCount; i++) {

					String columnLabel = columnLabels.get(i);
					String displayValues = resultSet.getString(i + 1);

					lineMap.put(columnLabel, displayValues);
				}

				queryResult.add(lineMap);
			}


		}
		
	}

	
	protected void onStatementYieldsUpdateCount(int updateCount) {
		

		
		
		
	}



}
