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
import com.github.blausql.core.connection.DatabaseConnection;
import com.github.blausql.core.connection.StatementResult;
import com.github.blausql.ui.QueryResultWindow;
import com.github.blausql.ui.components.WaitDialog;
import com.github.blausql.ui.util.BackgroundWorker;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.screen.Screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class ExecuteStatementBackgroundWorker extends BackgroundWorker<List<StatementResult>> {

    private final TerminalUI terminalUI;
    private final DatabaseConnection databaseConnection;
    private final List<String> sqlCommands;
    private final Window showWaitDialog;
    private final SqlQueryWindow sqlQueryWindow;
    private final boolean showIndividualResults;

    ExecuteStatementBackgroundWorker(TerminalUI terminalUI,
                                   DatabaseConnection databaseConnection,
                                   List<String> sqlCommands,
                                   WaitDialog showWaitDialog,
                                   SqlQueryWindow sqlQueryWindow) {
        this(terminalUI, databaseConnection, sqlCommands, showWaitDialog, sqlQueryWindow, false);
    }

    ExecuteStatementBackgroundWorker(TerminalUI terminalUI,
                                   DatabaseConnection databaseConnection,
                                   List<String> sqlCommands,
                                   WaitDialog showWaitDialog,
                                   SqlQueryWindow sqlQueryWindow,
                                   boolean showIndividualResults) {
        super(sqlQueryWindow);
        this.terminalUI = terminalUI;
        this.databaseConnection = databaseConnection;
        this.sqlCommands = new ArrayList<>(sqlCommands);
        this.showWaitDialog = showWaitDialog;
        this.sqlQueryWindow = sqlQueryWindow;
        this.showIndividualResults = showIndividualResults;
    }

    @Override
    protected List<StatementResult> doBackgroundTask() {
        WindowBasedTextGUI windowBasedTextGUI = terminalUI.getWindowBasedTextGUI();
        Screen screen = windowBasedTextGUI.getScreen();
        TerminalSize terminalSize = screen.getTerminalSize();
        int limit = terminalSize.getRows() - 2;

        List<StatementResult> results = new ArrayList<>();
        for (String sql : sqlCommands) {
            if (Thread.currentThread().isInterrupted()) {
                throw new RuntimeException("Statement execution was interrupted");
            }
            results.add(databaseConnection.executeStatement(sql, limit));
        }
        return results;
    }

    @Override
    protected void onBackgroundTaskInterrupted(InterruptedException interruptedException) {
        terminalUI.showMessageBox("Interrupted",
                "The statement was aborted. \n"
                        + "You might have to re-connect before you can run a new one.");

        sqlQueryWindow.onStatementCompleted();
    }

    @Override
    protected void onBackgroundTaskFailed(Throwable t) {
        showWaitDialog.close();

        terminalUI.showErrorMessageFromThrowable(t);

        sqlQueryWindow.onStatementCompleted();
    }

    @Override
    protected void onBackgroundTaskCompleted(List<StatementResult> results) {
        showWaitDialog.close();
        sqlQueryWindow.onStatementCompleted();

        int totalUpdated = 0;
        boolean hasResults = false;
        List<Map<String, Object>> combinedResults = new ArrayList<>();

        for (int i = 0; i < results.size(); i++) {
            StatementResult result = results.get(i);
            if (result.isResultSet()) {
                hasResults = true;
                if (showIndividualResults) {
                    String title;
                    if (results.size() == 1) {
                        title = "Query Result";
                    } else {
                        title = String.format("Query Result %d of %d", i + 1, results.size());
                    }
                    terminalUI.showWindowFullScreen(new QueryResultWindow(result.getQueryResult(), title));
                } else {
                    combinedResults.addAll(result.getQueryResult());
                }
            } else {
                totalUpdated += result.getUpdateCount();
            }
        }

        if (hasResults && !showIndividualResults && !combinedResults.isEmpty()) {
            terminalUI.showWindowFullScreen(new QueryResultWindow(combinedResults, "Query Results"));
        }

        if (totalUpdated > 0) {
            String message = String.format("Executed %d statement(s). Total rows affected: %d", 
                results.size(), totalUpdated);
            terminalUI.showMessageBox("Execution Complete", message);
        } else if (!hasResults) {
            terminalUI.showMessageBox("Execution Complete", 
                String.format("Executed %d statement(s)", results.size()));
        }
    }
}
