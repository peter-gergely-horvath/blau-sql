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

import com.github.blausql.TerminalUI;
import com.github.blausql.core.connection.ConnectionDefinition;
import com.github.blausql.core.connection.DatabaseConnection;
import com.github.blausql.core.connection.StatementResult;
import com.github.blausql.core.util.TextUtils;
import com.github.blausql.ui.components.ApplicationWindow;
import com.github.blausql.ui.components.WaitDialog;
import com.github.blausql.ui.util.BackgroundWorker;
import com.github.blausql.ui.hotkey.HotKeyWindowListener;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.input.KeyType;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

final class SqlQueryWindow extends ApplicationWindow {

    private final DatabaseConnection databaseConnection;

    private final class ExecuteStatementBackgroundWorker extends BackgroundWorker<StatementResult> {

        private final String sqlCommand;
        private final Window showWaitDialog;

        private ExecuteStatementBackgroundWorker(String sqlCommand,
                                                 WaitDialog showWaitDialog,
                                                 ApplicationWindow parent) {
            super(parent);
            this.sqlCommand = sqlCommand;
            this.showWaitDialog = showWaitDialog;
        }

        @Override
        protected StatementResult doBackgroundTask() {
            TerminalSize terminalSize = getApplicationTextGUI().getScreen().getTerminalSize();
            int limit = terminalSize.getRows() - 2;

            return databaseConnection.executeStatement(sqlCommand, limit);
        }

        @Override
        protected void onBackgroundTaskInterrupted(InterruptedException interruptedException) {
            showMessageBox("Interrupted",
                    "The statement was aborted. \n"
                            + "You might have to re-connect before you can run a new one.");

            setFocusedInteractable(sqlQueryTextBox);
        }

        @Override
        protected void onBackgroundTaskFailed(Throwable t) {
            showWaitDialog.close();

            showErrorMessageFromThrowable(t);

            setFocusedInteractable(sqlQueryTextBox);
        }

        @Override
        protected void onBackgroundTaskCompleted(StatementResult statementResult) {
            showWaitDialog.close();

            setFocusedInteractable(sqlQueryTextBox);

            if (statementResult.isResultSet()) {

                final List<Map<String, Object>> queryResult = statementResult.getQueryResult();

                showWindowFullScreen(new QueryResultWindow(queryResult));
            } else {

                final int updateCount = statementResult.getUpdateCount();

                final String message = String.format("%s row(s) changed", updateCount);

                showMessageBox("Statement executed", message);
            }
        }
    }

    private final TextBox sqlQueryTextBox;
    private final String connectionName;
    private final String statementSeparator = ";";

    SqlQueryWindow(ConnectionDefinition connectionDefinition,
                   DatabaseConnection databaseConnection,
                   TerminalUI terminalUI) {

        super(String.format(" %s ", connectionDefinition.getConnectionName()), terminalUI);

        connectionName = connectionDefinition.getConnectionName();

        this.databaseConnection = databaseConnection;

        Panel bottomPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        bottomPanel.addComponent(new Label("< Clear(F2) >"));
        bottomPanel.addComponent(new Label("< Save(F5) >"));
        bottomPanel.addComponent(new Label("< Load(F6) >"));
        bottomPanel.addComponent(new Label("< Execute Current(F8) >"));
        bottomPanel.addComponent(new Label("< Execute All(F9) >"));
        bottomPanel.addComponent(new Label("< Exit(ESC) >"));

        TerminalSize desiredSizeForSqlQueryTextBox = getDesiredSizeForSqlQueryTextBox();
        sqlQueryTextBox = new TextBox(desiredSizeForSqlQueryTextBox, "");

        addWindowListener(HotKeyWindowListener.builder()
                .keyType(KeyType.Escape).invoke(this::closeWindow)
                .keyType(KeyType.F2).invoke(this::clearEditor)
                .keyType(KeyType.F5).invoke(this::saveSqlFile)
                .keyType(KeyType.F6).invoke(this::selectSqlFileToLoad)
                .keyType(KeyType.F8).invoke(this::executeQuerySelected)
                .keyType(KeyType.F9).invoke(this::executeQueryAll)
                .build());

        Panel verticalPanel = Panels.vertical(sqlQueryTextBox, bottomPanel);
        setComponent(verticalPanel);

        addWindowListener(new WindowListenerAdapter() {
            @Override
            public void onResized(Window window, TerminalSize oldSize, TerminalSize newSize) {

                TerminalSize desiredSizeForSqlQueryTextBox = getDesiredSizeForSqlQueryTextBox();
                sqlQueryTextBox.setSize(desiredSizeForSqlQueryTextBox);

                final int topAndBottomHeight = -2;

                verticalPanel.setSize(desiredSizeForSqlQueryTextBox.withRelativeRows(topAndBottomHeight));
            }
        });

        setFocusedInteractable(sqlQueryTextBox);
    }

    private TerminalSize getDesiredSizeForSqlQueryTextBox() {

        TerminalSize screenTerminalSize = getApplicationTextGUI().getScreen().getTerminalSize();

        final int sqlQueryTextBoxColumns = screenTerminalSize.getColumns() - 4;
        final int sqlQueryTextBoxRows = screenTerminalSize.getRows() - 2;

        return new TerminalSize(sqlQueryTextBoxColumns, sqlQueryTextBoxRows);
    }

    private void clearEditor() {
        sqlQueryTextBox.setText("");
    }

    private void executeQuerySelected() {

        TerminalPosition cursorLocation = sqlQueryTextBox.getCursorLocation();

        final int cursorLinePosition = cursorLocation.getRow();
        final int lineCount = sqlQueryTextBox.getLineCount();

        LinkedList<String> linesBeforeCursor = getBeforeCursorLines(cursorLinePosition);
        LinkedList<String> lines = new LinkedList<>(linesBeforeCursor);

        boolean processLinesAfterCursorPosition;


        String cursorLine = sqlQueryTextBox.getLine(cursorLinePosition);
        if (cursorLine.contains(statementSeparator)) {

            String lastLine = cursorLine.substring(0, cursorLine.indexOf(statementSeparator));

            lines.add(lastLine);

            processLinesAfterCursorPosition = false;
        } else {

            lines.add(cursorLine);

            processLinesAfterCursorPosition = true;
        }

        if (processLinesAfterCursorPosition) {
            LinkedList<String> afterCursorLines = getAfterCursorLines(cursorLinePosition, lineCount);
            lines.addAll(afterCursorLines);
        }

        String statementToExecute = TextUtils.joinStringsWithNewLine(lines);

        executeQuery(statementToExecute);
    }

    private LinkedList<String> getBeforeCursorLines(int cursorLine) {

        LinkedList<String> lines = new LinkedList<>();

        for (int i = cursorLine - 1; i >= 0; i--) {

            String line = sqlQueryTextBox.getLine(i);

            boolean firstLineOfTheStatement = line.contains(statementSeparator);

            if (firstLineOfTheStatement) {

                int beginIndex = line.lastIndexOf(statementSeparator);

                line = line.substring(beginIndex);

                if (line.trim().equalsIgnoreCase(statementSeparator.trim())) {
                    line = null;
                }
            }

            if (line != null) {
                lines.addFirst(line);
            }

            if (firstLineOfTheStatement) {
                break;
            }
        }

        return lines;
    }

    private LinkedList<String> getAfterCursorLines(int cursorLine, int lineCount) {

        LinkedList<String> lines = new LinkedList<>();

        for (int i = cursorLine + 1; i < lineCount; i++) {

            String line = sqlQueryTextBox.getLine(i);

            boolean lastLineOfTheStatement = line.contains(statementSeparator);

            if (lastLineOfTheStatement) {

                int endIndex = line.indexOf(statementSeparator);

                line = line.substring(0, endIndex);

                if (line.trim().equalsIgnoreCase(statementSeparator.trim())) {
                    line = null;
                }
            }

            if (line != null) {
                lines.addLast(line);
            }

            if (lastLineOfTheStatement) {
                break;
            }
        }

        return lines;
    }

    private void executeQueryAll() {
        executeQuery(sqlQueryTextBox.getText());
    }

    private void closeWindow() {

        MessageDialogButton dialogResult = showMessageBox(
                "Quit now?",
                "Do you want to quit the session now?",
                MessageDialogButton.OK, MessageDialogButton.Cancel);

        if (dialogResult == MessageDialogButton.OK) {
            databaseConnection.disconnect();
            close();
        }
    }

    private void saveSqlFile() {

        File file = showFileSelectorDialog("Save SQL file",
                "Please specify the location to save the file to", "Save");

        if (file != null) {
            String sqlContent = sqlQueryTextBox.getText();

            try {
                Files.writeString(file.toPath(), sqlContent,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            } catch (RuntimeException | IOException e) {
                showErrorMessageFromThrowable(e);
            }
        }
    }

    private void selectSqlFileToLoad() {

        File file = showFileSelectorDialog("Select SQL file to load",
                "Please specify the location to load the file from", "Select");

        if (file != null) {
            try {
                String sqlContent = Files.readString(file.toPath(), StandardCharsets.UTF_8);

                setEditorContent(sqlContent);

            } catch (RuntimeException | IOException e) {
                showErrorMessageFromThrowable(e);
            }
        }
    }

    private void setEditorContent(String content) {

        sqlQueryTextBox.setText(content);
    }

    private void executeQuery(final String sqlCommand) {

        if (sqlCommand.isBlank()) {
            showMessageBox("Empty SQL statement", "No valid SQL statement is specified");
            return;
        }


        final AtomicReference<BackgroundWorker<?>> backgroundWorkerReference = new AtomicReference<>();

        final WaitDialog showWaitDialog = showWaitDialog("Please wait",
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
                new ExecuteStatementBackgroundWorker(sqlCommand, showWaitDialog, this);

        backgroundWorkerReference.set(statementExecutorBackgroundWorker);

        statementExecutorBackgroundWorker.start();
    }
}
