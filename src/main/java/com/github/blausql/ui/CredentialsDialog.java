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

import com.github.blausql.core.connection.ConnectionDefinition;
import com.github.blausql.ui.components.PasswordBox;
import com.github.blausql.ui.hotkey.HotKeyWindowListener;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.input.KeyType;


final class CredentialsDialog extends BasicWindow {

    private static final int USERNAME_BOX_LEN = 20;
    private static final int PASSWORD_BOX_LEN = 20;

    private final TextBox userNameTextBox;
    private final TextBox passwordPasswordBox;

    private MessageDialogButton selectedButton = null;

    CredentialsDialog(ConnectionDefinition cd) {

        super("Enter credentials for " + cd.getConnectionName());

        userNameTextBox = new TextBox(new TerminalSize(USERNAME_BOX_LEN, 1),
                cd.getUserName(), TextBox.Style.SINGLE_LINE);

        passwordPasswordBox = new PasswordBox(cd.getPassword(), PASSWORD_BOX_LEN);

        Button okButton = new Button("OK", this::onOkButtonSelected);
        Button cancelButton = new Button("Cancel", this::onCancelButtonSelected);

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

        Panel mainPanel = Panels.vertical(
                new Label("User name:"),
                userNameTextBox,
                new Label("Password:"),
                passwordPasswordBox,
                new EmptySpace(),
                buttonPanel
        );

        setComponent(mainPanel);

        setFocusedInteractable(okButton);

        addWindowListener(HotKeyWindowListener.builder()
                .keyType(KeyType.Escape).invoke(this::onCancelButtonSelected)
                .build());
    }

    private void onOkButtonSelected() {
        selectedButton = MessageDialogButton.OK;
        close();
    }

    private void onCancelButtonSelected() {
        selectedButton = MessageDialogButton.Cancel;
        close();
    }

    public MessageDialogButton getSelectedButton() {
        return selectedButton;
    }

    public String getUserName() {
        return userNameTextBox.getText();
    }

    public String getPassword() {
        return passwordPasswordBox.getText();
    }
}
