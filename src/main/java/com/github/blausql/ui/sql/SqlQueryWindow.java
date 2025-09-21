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

package com.github.blausql.ui.sql;

import com.github.blausql.TerminalUI;
import com.github.blausql.core.Constants;
import com.github.blausql.core.connection.ConnectionDefinition;
import com.github.blausql.core.connection.DatabaseConnection;
import com.github.blausql.ui.HelpWindow;
import com.github.blausql.ui.components.ApplicationWindow;
import com.github.blausql.ui.components.WaitDialog;
import com.github.blausql.ui.util.BackgroundWorker;
import com.github.blausql.core.util.TextUtils;
import com.github.blausql.ui.hotkey.HotKeyWindowListener;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.gui2.menu.Menu;
import com.googlecode.lanterna.gui2.menu.MenuBar;
import com.googlecode.lanterna.gui2.menu.MenuItem;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SqlQueryWindow extends ApplicationWindow {

    private static final String SEPARATOR = "----------";

    private final DatabaseConnection databaseConnection;

    private final TextBox sqlQueryTextBox;
    private final String connectionName;
    private final String statementSeparator;

    private final AtomicReference<BackgroundWorker<?>> backgroundWorkerReference = new AtomicReference<>();
    private final Menu fileMenu;

    public SqlQueryWindow(ConnectionDefinition connectionDefinition,
                          DatabaseConnection databaseConnection,
                          TerminalUI terminalUI) {

        super(String.format(" %s ", connectionDefinition.getConnectionName()), terminalUI);

        connectionName = connectionDefinition.getConnectionName();
        statementSeparator = connectionDefinition.getStatementSeparator();

        this.databaseConnection = databaseConnection;

        Panel bottomPanel = Panels.horizontal(
                new Label("F1: Help"),
                new Separator(Direction.VERTICAL),
                new Label("F7: Execute Each"),
                new Separator(Direction.VERTICAL),
                new Label("F8: Execute Current"),
                new Separator(Direction.VERTICAL),
                new Label("F9: Execute All"),
                new Separator(Direction.VERTICAL),
                new Label("12: Menu"),
                new Separator(Direction.VERTICAL),
                new Label("ESC: Exit"));

        TerminalSize desiredSizeForSqlQueryTextBox = getDesiredSizeForSqlQueryTextBox();
        sqlQueryTextBox = new SqlEditorTextBox(desiredSizeForSqlQueryTextBox, "");

        addWindowListener(HotKeyWindowListener.builder()
                .keyType(KeyType.Escape).invoke(this::closeWindow)
                .keyType(KeyType.F1).invoke(this::displayHelp)
                .keyType(KeyType.F2).invoke(this::clearEditor)
                .keyType(KeyType.F5).invoke(this::saveSqlFile)
                .keyType(KeyType.F6).invoke(this::selectSqlFileToLoad)
                .keyType(KeyType.F7).invoke(this::executeQueryEach)
                .keyType(KeyType.F8).invoke(this::executeQueryAtCursor)
                .keyType(KeyType.F9).invoke(this::executeQueryAll)
                .keyType(KeyType.F12).invoke(this::openMenu)
                .build());

        MenuBar menubar = new MenuBar();

        fileMenu = createFileMenu();
        menubar.add(fileMenu);
        menubar.add(createExecuteMenu());
        menubar.add(createHelpMenu());

        Panel verticalPanel = Panels.vertical(menubar, sqlQueryTextBox, bottomPanel);
        setComponent(verticalPanel);

        setFocusedInteractable(sqlQueryTextBox);
    }

    private Menu createFileMenu() {
        final Menu menu;
        menu = new Menu("File");
        menu.add(new MenuItem("Clear editor content  (F2)", this::clearEditor));
        menu.add(new MenuItem(SEPARATOR).setEnabled(false));
        menu.add(new MenuItem("Save SQL to file...   (F5)", this::saveSqlFile));
        menu.add(new MenuItem("Load SQL from file... (F6)", this::selectSqlFileToLoad));
        menu.add(new MenuItem(SEPARATOR).setEnabled(false));
        menu.add(new MenuItem("Exit                  (ESC)", this::closeWindow));
        return menu;
    }

    private Menu createExecuteMenu() {
        final Menu menuExecute;
        menuExecute = new Menu("Execute");
        menuExecute.add(new MenuItem("Execute each statement       (F7)", this::executeQueryEach));
        menuExecute.add(new MenuItem("Execute statement at cursor  (F8)", this::executeQueryAtCursor));
        menuExecute.add(new MenuItem("Execute all content at once  (F9)", this::executeQueryAll));
        return menuExecute;
    }

    private Menu createHelpMenu() {
        final Menu menuExecute;
        menuExecute = new Menu("Help");
        menuExecute.add(new MenuItem("Show Help (F1)", this::displayHelp));
        menuExecute.add(new MenuItem(SEPARATOR).setEnabled(false));
        menuExecute.add(new MenuItem("About", this::displayAbout));
        return menuExecute;
    }

    private TerminalSize getDesiredSizeForSqlQueryTextBox() {

        TerminalSize screenTerminalSize = getApplicationTextGUI().getScreen().getTerminalSize();

        return getDesiredSizeForSqlQueryTextBox(screenTerminalSize);
    }

    private static TerminalSize getDesiredSizeForSqlQueryTextBox(TerminalSize screenTerminalSize) {
        final int sqlQueryTextBoxColumns = screenTerminalSize.getColumns() - 4;
        final int sqlQueryTextBoxRows = screenTerminalSize.getRows() - 2;

        return new TerminalSize(sqlQueryTextBoxColumns, sqlQueryTextBoxRows);
    }

    private void displayHelp() {
        getTerminalUI().showWindowFullScreen(new HelpWindow(SqlQueryWindow.class, getTerminalUI()));
    }

    private void displayAbout() {
        showMessageBox("About BlauSQL", Constants.ABOUT_TEXT);
    }


    private void openMenu() {
        setFocusedInteractable(fileMenu);
        fileMenu.handleInput(new KeyStroke(KeyType.Enter));
    }


    private void clearEditor() {
        sqlQueryTextBox.setText("");
        setFocusedInteractable(sqlQueryTextBox);
    }


    private void executeQueryEach() {
        String sqlContent = sqlQueryTextBox.getText();

        if (sqlContent.isEmpty()) {
            showMessageBox("Empty SQL statement", "No valid SQL statement is specified");
            return;
        }

        List<String> statements = Stream.of(sqlContent.split(statementSeparator))
                .filter(s -> !s.trim().isEmpty())
                .collect(Collectors.toList());

        executeStatements(statements);
    }

    private void executeQueryAtCursor() {

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

        executeStatement(statementToExecute);
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
        executeStatement(sqlQueryTextBox.getText());
    }

    private void closeWindow() {

        MessageDialogButton dialogResult = showMessageBox(
                "Quit now?",
                "Do you want to quit the session now?",
                MessageDialogButton.OK, MessageDialogButton.Cancel);

        if (dialogResult == MessageDialogButton.OK) {
            databaseConnection.close();
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

    private void executeStatement(final String sqlStatement) {

        if (sqlStatement.isEmpty()) {
            showMessageBox("Empty SQL statement", "No valid SQL statement is specified");
            return;
        }

        executeStatements(Collections.singletonList(sqlStatement));
    }

    private void executeStatements(List<String> statements) {

        String waitMessage;

        if (statements.size() == 1) {
            waitMessage = String.format("Executing statement against %s ...", connectionName);
        } else {
            waitMessage = String.format("Executing %d statement(s) against %s ...",
                    statements.size(), connectionName);
        }

        final WaitDialog showWaitDialog = showWaitDialog("Please wait", waitMessage, this::cancelBackgroundOperation);

        ExecuteStatementBackgroundWorker statementExecutorBackgroundWorker =
                new ExecuteStatementBackgroundWorker(
                        getTerminalUI(), databaseConnection, statements, showWaitDialog, this, true);

        startBackgroundTask(statementExecutorBackgroundWorker);
    }

    private void startBackgroundTask(BackgroundWorker<?> statementExecutorBackgroundWorker) {
        backgroundWorkerReference.set(statementExecutorBackgroundWorker);

        statementExecutorBackgroundWorker.start();
    }

    private void cancelBackgroundOperation() {
        BackgroundWorker<?> backgroundWorker = backgroundWorkerReference.get();
        if (backgroundWorker != null) {
            backgroundWorker.cancel();
        }
    }

    void onStatementCompleted() {
        setFocusedInteractable(sqlQueryTextBox);
    }
}
