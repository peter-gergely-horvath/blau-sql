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
import com.github.blausql.core.preferences.ConnectionDefinitionRepository;
import com.github.blausql.ui.util.BackgroundWorker;

import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;


import java.util.List;

final class SelectConnectionToDeleteWindow extends SelectConnectionWindow {

    SelectConnectionToDeleteWindow(List<ConnectionDefinition> connectionDefinitions, TerminalUI terminalUI) {
        super("Select Connection to Delete", connectionDefinitions, terminalUI);
    }

    @Override
    protected void onConnectionSelected(
            final ConnectionDefinition cd) {


        MessageDialogButton dialogResult = showMessageBox(
                "Confirm deletion of connection",
                "Delete connection: " + cd.getConnectionName(),
                MessageDialogButton.OK, MessageDialogButton.Cancel);

        if (MessageDialogButton.OK.equals(dialogResult)) {
            final Window showWaitDialog = showWaitDialog("Please wait",
                    "Deleting " + cd.getConnectionName() + "... ");

            new BackgroundWorker<Void>(this) {

                @Override
                protected Void doBackgroundTask() {
                    ConnectionDefinitionRepository.getInstance()
                            .deleteConnectionDefinitionByName(cd.getConnectionName());

                    return null;
                }

                @Override
                protected void onBackgroundTaskInterrupted(InterruptedException interruptedException) {
                    showWaitDialog.close();
                }

                @Override
                protected void onBackgroundTaskFailed(Throwable t) {
                    showWaitDialog.close();
                    showErrorMessageFromThrowable(t);

                }

                @Override
                protected void onBackgroundTaskCompleted(Void result) {
                    showWaitDialog.close();
                    SelectConnectionToDeleteWindow.this.close();
                }
            }.start();
        } else {
            close();
        }
    }
}
