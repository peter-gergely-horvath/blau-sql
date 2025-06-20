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
import com.github.blausql.core.connection.ConnectionDefinition;
import com.github.blausql.ui.components.CloseOnEscapeKeyPressWindow;
import com.github.blausql.ui.components.PasswordBox;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;


final class CredentialsDialog extends CloseOnEscapeKeyPressWindow {

    private static final int USERNAME_BOX_LEN = 20;
    private static final int PASSWORD_BOX_LEN = 20;

    private final TextBox userNameTextBox;
    private final TextBox passwordPasswordBox;

    private DialogResult dialogResult = DialogResult.CANCEL;

    CredentialsDialog(ConnectionDefinition cd) {

        super("Enter credentials for " + cd.getConnectionName());

        addComponent(new Label("User name:"));
        userNameTextBox = new TextBox(new TerminalSize(USERNAME_BOX_LEN, 1),
                cd.getUserName(), TextBox.Style.SINGLE_LINE);
        addComponent(userNameTextBox);

        addComponent(new Label("Password:"));
        passwordPasswordBox = new PasswordBox(PASSWORD_BOX_LEN, cd.getPassword());
        addComponent(passwordPasswordBox);

        Button okButton = new Button("OK", new Runnable() {
            public void run() {
                dialogResult = DialogResult.OK;
                close();
            }
        });
        Button cancelButton = new Button("Cancel", new Runnable() {
            public void run() {
                dialogResult = DialogResult.CANCEL;
                close();
            }
        });

        int labelWidth = userNameTextBox.getPreferredSize().getColumns();
        Panel buttonPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        int leftPadding = 0;
        int buttonsWidth = okButton.getPreferredSize().getColumns()
                + cancelButton.getPreferredSize().getColumns() + 1;
        if (buttonsWidth < labelWidth) {
            leftPadding = (labelWidth - buttonsWidth) / 2;
        }

        if (leftPadding > 0) {
            buttonPanel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        }
        buttonPanel.addComponent(okButton);
        buttonPanel.addComponent(cancelButton);
        addComponent(new EmptySpace());
        addComponent(buttonPanel);

        setFocusedInteractable(okButton);
    }

    public DialogResult getDialogResult() {
        return dialogResult;
    }

    public String getUserName() {
        return userNameTextBox.getText();
    }

    public String getPassword() {
        return passwordPasswordBox.getText();
    }
}
