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

 
package com.github.blausql.ui;

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
	private final String connectionName;

	public SqlCommandWindow(ConnectionDefinition connectionDefinition) {
		super(connectionDefinition.getConnectionName());
		
		connectionName = connectionDefinition.getConnectionName();

		Panel bottomPanel = new Panel(new Border.Bevel(true),
				Panel.Orientation.HORISONTAL);
		Panel verticalPanel = new Panel(new Border.Invisible(),
				Panel.Orientation.VERTICAL);

		bottomPanel.addComponent(new Label(">>> Press TAB >>>"));

		bottomPanel.addComponent(new Button("Execute Query (CTRL+E)", onExecuteSqlButtonSelectedAction));
		bottomPanel.addComponent(new Button("Close connection (ESC)", onCloseConnectionButtonSelectedAction));

		TerminalSize screenTerminalSize = TerminalUI.getTerminalSize();

		final int sqlEditorPanelColumns = screenTerminalSize.getColumns() - 4;
		final int sqlEditorPanelRows = screenTerminalSize.getRows() - 4;

		sqlEditArea = new EditArea(new TerminalSize(sqlEditorPanelColumns,
				sqlEditorPanelRows));
		verticalPanel.addComponent(sqlEditArea);

		verticalPanel.addComponent(bottomPanel);

		addComponent(verticalPanel);
	}

	private final Action onExecuteSqlButtonSelectedAction = new Action() {

		public void doAction() {
            executeQuery(sqlEditArea.getData());

        }
	};

    private final Action onCloseConnectionButtonSelectedAction = new Action() {

        public void doAction() {
            Database.getInstance().disconnect();

            SqlCommandWindow.this.close();
        }

    };
	
	public void onKeyPressed(Key key) {
		
		if(Kind.NormalKey.equals(key.getKind()) && 
				(key.getCharacter() == 'E' || key.getCharacter() == 'e') &&   
				key.isCtrlPressed()) {

            executeQuery(sqlEditArea.getData());

        } else if(Kind.Escape.equals(key.getKind())) {

            Database.getInstance().disconnect();

            this.close();

        } else {
			super.onKeyPressed(key);
		}
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

}
