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


package com.github.blausql.ui;

import com.github.blausql.DialogResult;
import com.github.blausql.TerminalUI;
import com.github.blausql.core.connection.ConnectionDefinition;
import com.github.blausql.core.connection.Database;
import com.github.blausql.core.connection.StatementResult;
import com.github.blausql.core.util.ExceptionUtils;
import com.github.blausql.ui.util.BackgroundWorker;
import com.github.blausql.ui.util.HotKeyWindowListener;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyType;


import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

final class SqlQueryWindow extends BasicWindow {

    private final TextBox sqlQueryTextBox;
    private final String connectionName;

    private final Database database = Database.getInstance();

    SqlQueryWindow(ConnectionDefinition connectionDefinition) {
        super(String.format(" %s ", connectionDefinition.getConnectionName()));

        connectionName = connectionDefinition.getConnectionName();

        Panel bottomPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        bottomPanel.addComponent(new Label("< Clear(F2) >"));
        bottomPanel.addComponent(new Label("< Save(F5) >"));
        bottomPanel.addComponent(new Label("< Load(F6) >"));
        bottomPanel.addComponent(new Label("< Execute(F9) >"));
        bottomPanel.addComponent(new Label("< Exit(ESC) >"));

        TerminalSize desiredSizeForSqlQueryTextBox = getDesiredSizeForSqlQueryTextBox();
        sqlQueryTextBox = new TextBox(desiredSizeForSqlQueryTextBox, "");

        addWindowListener(HotKeyWindowListener.builder()
                .keyType(KeyType.Escape).invoke(this::closeWindow)
                .keyType(KeyType.F2).invoke(this::clearEditor)
                .keyType(KeyType.F5).invoke(this::saveSqlFile)
                .keyType(KeyType.F6).invoke(this::selectSqlFileToLoad)
                .keyType(KeyType.F9).invoke(this::executeQuery)
                .build());

        setComponent(Panels.vertical(sqlQueryTextBox, bottomPanel));
        setFocusedInteractable(sqlQueryTextBox);
    }

    private static TerminalSize getDesiredSizeForSqlQueryTextBox() {

        TerminalSize screenTerminalSize = TerminalUI.getTerminalSize();

        final int sqlQueryTextBoxColumns = screenTerminalSize.getColumns() - 4;
        final int sqlQueryTextBoxRows = screenTerminalSize.getRows() - 2;

        return new TerminalSize(sqlQueryTextBoxColumns, sqlQueryTextBoxRows);
    }

    private void clearEditor() {
        sqlQueryTextBox.setText("");
    }

    private void executeQuery() {
        executeQuery(sqlQueryTextBox.getText());
    }

    private void closeWindow() {

        DialogResult dialogResult = TerminalUI.showMessageBox(
                "Quit now?",
                "Do you want to quit the session now?",
                DialogButtons.OK_CANCEL);

        if (dialogResult == DialogResult.OK) {
            database.disconnect();
            SqlQueryWindow.this.close();
        }
    }

    private void saveSqlFile() {

        File file = TerminalUI.showFileSelectorDialog("Save SQL file",
                "Please specify the location to save the file to", "Save");

        if (file != null) {
            String sqlContent = sqlQueryTextBox.getText();

            try {
                Files.writeString(file.toPath(), sqlContent,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            } catch (RuntimeException | IOException e) {
                TerminalUI.showErrorMessageFromThrowable(e);
            }
        }
    }

    private void selectSqlFileToLoad() {

        File file = TerminalUI.showFileSelectorDialog("Select SQL file to load",
                "Please specify the location to load the file from", "Select");

        if (file != null) {
            try {
                String sqlContent = Files.readString(file.toPath(), StandardCharsets.UTF_8);

                setEditorContent(sqlContent);

            } catch (RuntimeException | IOException e) {
                TerminalUI.showErrorMessageFromThrowable(e);
            }
        }
    }

    private void setEditorContent(String content) {

        sqlQueryTextBox.setText(content);
    }

    private void executeQuery(final String sqlCommand) {

        final AtomicReference<BackgroundWorker<?>> backgroundWorkerReference = new AtomicReference<>();

        final Window showWaitDialog = TerminalUI.showWaitDialog("Please wait",
                String.format("Executing statement against %s ...", connectionName), new Runnable() {
                    @Override
                    public void run() {
                        BackgroundWorker<?> backgroundWorker = backgroundWorkerReference.get();
                        if (backgroundWorker != null) {
                            backgroundWorker.cancel();
                        }
                    }
                });

        BackgroundWorker<StatementResult> statementExecutorBackgroundWorker =
                getStatementExecutorBackgroundWorker(sqlCommand, showWaitDialog);

        backgroundWorkerReference.set(statementExecutorBackgroundWorker);

        statementExecutorBackgroundWorker.start();

    }

    private BackgroundWorker<StatementResult> getStatementExecutorBackgroundWorker(
            final String sqlCommand, final Window showWaitDialog) {

        return new BackgroundWorker<StatementResult>() {

                @Override
                protected StatementResult doBackgroundTask() {
                    TerminalSize terminalSize = TerminalUI.getTerminalSize();
                    int limit = terminalSize.getRows() - 2;

                    return database.executeStatement(sqlCommand, limit);
                }

                @Override
                protected void onBackgroundTaskFailed(Throwable t) {
                    showWaitDialog.close();

                    if (ExceptionUtils.causesContainAnyType(t,
                            new Class[]{InterruptedException.class, InterruptedIOException.class})) {

                        TerminalUI.showMessageBox("Interrupted",
                                "The statement was aborted. \n"
                                        + "You might have to re-connect before you can run a new one.");
                    } else {
                        TerminalUI.showErrorMessageFromThrowable(t);
                    }

                    setFocusedInteractable(sqlQueryTextBox);
                }

                @Override
                protected void onBackgroundTaskCompleted(StatementResult statementResult) {
                    showWaitDialog.close();

                    setFocusedInteractable(sqlQueryTextBox);

                    if (statementResult.isResultSet()) {

                        final List<Map<String, Object>> queryResult = statementResult.getQueryResult();

                        TerminalUI.showWindowFullScreen(new QueryResultWindow(queryResult));
                    } else {

                        final int updateCount = statementResult.getUpdateCount();

                        final String message = String.format("%s row(s) changed", updateCount);

                        TerminalUI.showMessageBox("Statement executed", message);
                    }
                }
            };
    }

}
