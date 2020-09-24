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
import com.github.blausql.core.connection.Database;
import com.github.blausql.core.connection.StatementResult;
import com.github.blausql.core.sqlfile.SqlFile;
import com.github.blausql.core.sqlfile.SqlFileRepository;
import com.github.blausql.core.util.ExceptionUtils;
import com.github.blausql.ui.util.BackgroundWorker;
import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.Interactable;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.component.EditArea;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.input.Key.Kind;
import com.googlecode.lanterna.terminal.TerminalSize;

import java.io.InterruptedIOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

final class SqlCommandWindow extends Window {

    private EditArea sqlEditArea;
    private final String connectionName;

    private SqlFileRepository sqlFileRepository = SqlFileRepository.getInstance();
    private Database database = Database.getInstance();

    private final class SqlEditArea extends EditArea {

        private SqlEditArea(TerminalSize terminalSize, String text) {
            super(terminalSize, text);
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
                    && Character.toUpperCase(key.getCharacter()) == 'R'
                    && key.isCtrlPressed()) {

                String data = this.getData();
                if (data != null) {
                    setEditorContent(" ");
                }

                return super.keyboardInteraction(new Key(Kind.Backspace));
            }

            return super.keyboardInteraction(key);
        }
    }

    SqlCommandWindow(ConnectionDefinition connectionDefinition) {
        super(connectionDefinition.getConnectionName());

        connectionName = connectionDefinition.getConnectionName();

        initComponents("");
    }

    private void initComponents(String text) {
        Panel bottomPanel = new Panel(new Border.Invisible(), Panel.Orientation.HORISONTAL);

        bottomPanel.addComponent(new Label("< Execute(CTRL+E) >"));
        bottomPanel.addComponent(new Label("< Clear(CTRL+R) >"));
        bottomPanel.addComponent(new Label("< Save(CTRL+S) >"));
        bottomPanel.addComponent(new Label("< Load(CTRL+L) >"));
        bottomPanel.addComponent(new Label("< Exit(ESC) >"));

        TerminalSize screenTerminalSize = TerminalUI.getTerminalSize();

        final int sqlEditorPanelColumns = screenTerminalSize.getColumns() - 4;
        final int sqlEditorPanelRows = screenTerminalSize.getRows() - 2;

        sqlEditArea = new SqlEditArea(new TerminalSize(sqlEditorPanelColumns, sqlEditorPanelRows), text);

        addComponent(sqlEditArea);

        addComponent(bottomPanel);
    }

    private void executeQuery() {
        executeQuery(sqlEditArea.getData());
    }

    private void closeWindow() {
        database.disconnect();

        SqlCommandWindow.this.close();
    }

    public void onKeyPressed(Key key) {

        if (Kind.NormalKey.equals(key.getKind())
                && Character.toUpperCase(key.getCharacter()) == 'E'
                && key.isCtrlPressed()) {

            executeQuery();

        } else if (Kind.NormalKey.equals(key.getKind())
                && Character.toUpperCase(key.getCharacter()) == 'S'
                && key.isCtrlPressed()) {

            saveSqlFile();

        } else if (Kind.NormalKey.equals(key.getKind())
                && Character.toUpperCase(key.getCharacter()) == 'L'
                && key.isCtrlPressed()) {

            selectSqlFileToLoad();

        } else if (Kind.Escape.equals(key.getKind())) {

            closeWindow();

        } else {
            super.onKeyPressed(key);
        }
    }

    private void saveSqlFile() {
        String fileName = TerminalUI.showTextInputDialog("Save as Bookmark",
                "Please enter the name for this SQL bookmark", "", 0);

        if (fileName != null) {
            String sqlContent = sqlEditArea.getData();

            saveSqlFile(fileName, sqlContent);
        }
    }

    private void saveSqlFile(final String fileName, final String sqlContent) {

        try {
            String sqlFileName = fileName;

            if (!sqlFileName.toLowerCase(Locale.ENGLISH).endsWith(".sql")) {
                sqlFileName = sqlFileName + ".sql";
            }

            SqlFile sqlFile = new SqlFile(sqlFileName, sqlContent);

            this.sqlFileRepository.saveSqlFile(sqlFile);

        } catch (RuntimeException e) {
            TerminalUI.showErrorMessageFromThrowable(e);
        }
    }

    private void selectSqlFileToLoad() {

        List<String> fileNames = SqlFileRepository.getInstance().listSqlFileNames();

        TerminalUI.showWindowCenter(new ListSelectorWindow<String>(
                "Select bookmarked SQL to load",
                "No bookmark found",
                fileNames) {

            @Override
            protected void onEntrySelected(String fileName) {

                try {
                    final String content =
                            SqlCommandWindow.this.sqlFileRepository.getFileContentBySqlFileName(fileName);

                    setEditorContent(content);

                } catch (RuntimeException e) {
                    TerminalUI.showErrorMessageFromThrowable(e);
                }
            }
        });
    }

    private void setEditorContent(String content) {
        removeAllComponents();

        initComponents(content);
    }

    private void executeQuery(final String sqlCommand) {

        final AtomicReference<BackgroundWorker<?>> backgroundWorkerReference = new AtomicReference<>();

        final Window showWaitDialog = TerminalUI.showWaitDialog("Please wait",
                String.format("Executing statement against %s ...", connectionName), new Action() {
                    @Override
                    public void doAction() {
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
            };
    }

}
