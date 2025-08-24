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


package com.github.blausql.ui;

import com.github.blausql.TerminalUI;
import com.github.blausql.core.connection.ConnectionDefinition;
import com.github.blausql.core.connection.DatabaseConnectionFactory;
import com.github.blausql.core.connection.DatabaseConnection;
import com.github.blausql.ui.components.WaitDialog;
import com.github.blausql.ui.sql.SqlQueryWindow;
import com.github.blausql.ui.util.BackgroundWorker;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

final class SelectConnectionForQueryWindow extends SelectConnectionWindow {

    SelectConnectionForQueryWindow(List<ConnectionDefinition> connectionDefinitions, TerminalUI terminalUI) {
        super("Select connection to Connect to", connectionDefinitions, terminalUI);
    }

    @Override
    protected void onConnectionSelected(
            ConnectionDefinition connectionDefinition) {

        this.close();

        if (!connectionDefinition.getLoginAutomatically()) {

            CredentialsDialog credentialsDialog =
                    new CredentialsDialog(connectionDefinition);

            showWindowCenter(credentialsDialog);

            MessageDialogButton dialogResult = credentialsDialog.getSelectedButton();
            if (dialogResult == MessageDialogButton.Cancel) {

                return;
            }

            ConnectionDefinition actualConnectionDefinition = new ConnectionDefinition(connectionDefinition);

            String userName = credentialsDialog.getUserName();
            actualConnectionDefinition.setUserName(userName);

            String password = credentialsDialog.getPassword();
            actualConnectionDefinition.setPassword(password);

            connectionDefinition = actualConnectionDefinition;
        }

        establishConnection(connectionDefinition);
    }

    private void establishConnection(
            final ConnectionDefinition connectionDefinition) {

        final AtomicReference<Window> waitDialogRef = new AtomicReference<>();
        final BackgroundWorker<DatabaseConnection> backgroundWorker = new BackgroundWorker<>(this) {

            @Override
            protected DatabaseConnection doBackgroundTask() throws InterruptedException {

                return DatabaseConnectionFactory.getDatabaseConnection(connectionDefinition);
            }

            @Override
            protected void onBackgroundTaskInterrupted(InterruptedException interruptedException) {
                // do nothing
            }

            @Override
            protected void onBackgroundTaskFailed(Throwable throwable) {
                closeWaitDialog();

                showErrorMessageFromThrowable(throwable);
            }

            @Override
            protected void onBackgroundTaskCompleted(DatabaseConnection databaseConnection) {
                closeWaitDialog();

                showWindowFullScreen(new SqlQueryWindow(connectionDefinition, databaseConnection, getTerminalUI()));

            }

            private void closeWaitDialog() {
                Window waitDialog = waitDialogRef.get();
                if (waitDialog != null) {
                    waitDialog.close();
                }
            }
        };

        final Window waitDialog = WaitDialog.createDialog(
                "Please wait",
                        "Connecting to " + connectionDefinition.getConnectionName() + "... ",
                        closeThisAndCancelBackgroundWorker(backgroundWorker));

        waitDialogRef.set(waitDialog);

        showInEventThread(waitDialog);

        backgroundWorker.start();
    }

    private void showInEventThread(final Window waitDialog) {
        runInEventThread(new Runnable() {
            @Override
            public void run() {
                showWindowCenter(waitDialog);
            }
        });
    }

    private Runnable closeThisAndCancelBackgroundWorker(final BackgroundWorker<?> backgroundWorker) {
        return new Runnable() {
            @Override
            public void run() {
                SelectConnectionForQueryWindow.this.close();

                backgroundWorker.cancel();
            }
        };
    }
}
