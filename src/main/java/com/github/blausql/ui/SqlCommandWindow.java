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

import com.github.blausql.TerminalUI;
import com.github.blausql.core.connection.ConnectionDefinition;
import com.github.blausql.core.connection.Database;
import com.github.blausql.core.connection.StatementResult;
import com.github.blausql.ui.util.BackgroundWorker;
import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.Interactable;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.component.EditArea;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.input.Key.Kind;
import com.googlecode.lanterna.terminal.TerminalSize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class SqlCommandWindow extends Window {

    private final EditArea sqlEditArea;
    private final String connectionName;

    private final Map<Character, String> clipboards = new HashMap<>();

    private final class SqlEditArea extends EditArea {

        private SqlEditArea(TerminalSize terminalSize) {
            super(terminalSize);
        }

        public Interactable.Result keyboardInteraction(Key key) {
            if (key.getKind() == Kind.Tab) {
                // Turn a TAB into 4 characters
                Key spaceCharacter = new Key(' ');

                super.keyboardInteraction(spaceCharacter);
                super.keyboardInteraction(spaceCharacter);
                super.keyboardInteraction(spaceCharacter);
                return super.keyboardInteraction(spaceCharacter);

            } else if (Kind.NormalKey.equals(key.getKind())
                    && (key.getCharacter() == 'L' || key.getCharacter() == 'l')
                    && key.isCtrlPressed()) {

                String data = this.getData();
                if (data != null) {
                    int length = data.length();
                    // A messy work-around for a framework bug, where
                    // simply setting the data to empty string
                    // causes the EditArea to fail to detect changes.
                    for(int i=0; i<length; i++) {
                        super.keyboardInteraction(new Key(Kind.Backspace));
                    }
                }

                return super.keyboardInteraction(new Key(Kind.Backspace));
            }

            return super.keyboardInteraction(key);
        }
    }

    SqlCommandWindow(ConnectionDefinition connectionDefinition) {
        super(connectionDefinition.getConnectionName());

        connectionName = connectionDefinition.getConnectionName();

        Panel bottomPanel = new Panel(new Border.Invisible(), Panel.Orientation.HORISONTAL);

        bottomPanel.addComponent(new Label("< Execute(CTRL+E) >"));
        bottomPanel.addComponent(new Label("< Clear(CTRL+L) >"));
        bottomPanel.addComponent(new Label("< History(CTRL+H) >"));
        bottomPanel.addComponent(new Label("< Exit(ESC) >"));

        TerminalSize screenTerminalSize = TerminalUI.getTerminalSize();

        final int sqlEditorPanelColumns = screenTerminalSize.getColumns() - 4;
        final int sqlEditorPanelRows = screenTerminalSize.getRows() - 2;

        sqlEditArea = new SqlEditArea(new TerminalSize(sqlEditorPanelColumns, sqlEditorPanelRows));

        addComponent(sqlEditArea);
        addComponent(bottomPanel);
    }

    private void executeQuery() {
        executeQuery(sqlEditArea.getData());
    }

    private void clearBuffer() {
        sqlEditArea.setData("");
    }

    private void closeWindow() {
        Database.getInstance().disconnect();

        SqlCommandWindow.this.close();
    }

    public void onKeyPressed(Key key) {

        if (Kind.NormalKey.equals(key.getKind())
                && (key.getCharacter() == 'E' || key.getCharacter() == 'e')
                && key.isCtrlPressed()) {

            executeQuery();

        } else if (Kind.NormalKey.equals(key.getKind())
                && (key.getCharacter() == 'H' || key.getCharacter() == 'h')
                && key.isCtrlPressed()) {

            showHistory();

        } else if (Kind.Escape.equals(key.getKind())) {

            closeWindow();

        } else if (key.isCtrlPressed() && Kind.F1.equals(key.getKind())) {
            char clipboardSelector = key.getKind().getRepresentationKey();

            clipboards.put(clipboardSelector, sqlEditArea.getData());



        } else {
            super.onKeyPressed(key);
        }
    }

    private void showHistory() {
        TerminalUI.showWindowCenter(new SqlHistoryWindow());
    }

    private void executeQuery(final String sqlCommand) {

        final Window showWaitDialog = TerminalUI.showWaitDialog("Please wait",
                String.format("Executing statement against %s ...", connectionName));

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

                if (statementResult.isResultSet()) {

                    final List<Map<String, Object>> queryResult = statementResult.getQueryResult();

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
