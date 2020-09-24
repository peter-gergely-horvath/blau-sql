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
import com.github.blausql.ui.components.WaitDialog;
import com.github.blausql.ui.util.BackgroundWorker;
import com.github.blausql.core.util.ExceptionUtils;
import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.dialog.DialogResult;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

final class SelectConnectionForQueryWindow extends SelectConnectionWindow {

    SelectConnectionForQueryWindow(List<ConnectionDefinition> connectionDefinitions) {
        super("Select connection to Connect to", connectionDefinitions);
    }

    @Override
    protected void onConnectionSelected(
            ConnectionDefinition connectionDefinition) {

        this.close();

        if (!connectionDefinition.getLoginAutomatically()) {

            CredentialsDialog credentialsDialog =
                    new CredentialsDialog(connectionDefinition);

            TerminalUI.showWindowCenter(credentialsDialog);

            DialogResult dialogResult = credentialsDialog.getDialogResult();
            if (dialogResult == DialogResult.CANCEL) {

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
        final BackgroundWorker backgroundWorker = new BackgroundWorker<Void>() {

            @Override
            protected Void doBackgroundTask() {
                Database.getInstance()
                        .establishConnection(connectionDefinition);
                return null;
            }

            @Override
            protected void onBackgroundTaskFailed(Throwable throwable) {
                closeWaitDialog();

                final boolean causedByCancellation = ExceptionUtils.causesContainType(
                        throwable, InterruptedException.class);
                if (!causedByCancellation) {
                    TerminalUI.showErrorMessageFromThrowable(throwable);
                }
            }

            @Override
            protected void onBackgroundTaskCompleted(Void result) {
                closeWaitDialog();

                TerminalUI.showWindowFullScreen(new SqlCommandWindow(connectionDefinition));

            }

            private void closeWaitDialog() {
                Window waitDialog = waitDialogRef.get();
                if (waitDialog != null) {
                    waitDialog.close();
                }
            }
        };

        final Window waitDialog = new WaitDialog("Please wait",
                        "Connecting to " + connectionDefinition.getConnectionName() + "... ",
                        closeThisAndCancelBackgroundWorker(backgroundWorker));

        waitDialogRef.set(waitDialog);

        showInEventThread(waitDialog);

        backgroundWorker.start();
    }

    private static void showInEventThread(final Window waitDialog) {
        TerminalUI.runInEventThread(new Action() {
            @Override
            public void doAction() {
                TerminalUI.showWindowCenter(waitDialog);
            }
        });
    }

    private Action closeThisAndCancelBackgroundWorker(final BackgroundWorker backgroundWorker) {
        return new Action() {
            @Override
            public void doAction() {
                SelectConnectionForQueryWindow.this.close();

                backgroundWorker.cancel();
            }
        };
    }
}
