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
import com.github.blausql.core.preferences.ConnectionDefinitionRepository;


import com.github.blausql.core.preferences.LoadException;
import com.github.blausql.core.preferences.SaveException;
import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.component.CheckBox;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.PasswordBox;
import com.googlecode.lanterna.gui.component.TextBox;

import java.util.Objects;

@SuppressWarnings("FieldCanBeLocal")
public final class ConnectionSettingsWindow extends Window {

    public enum Mode {
        ADD("Add connection"), EDIT("Edit connection"), COPY("Copy connection");

        private final String description;

        Mode(String description) {
            this.description = description;
        }
    }

    private static final int CONNECTION_NAME_BOX_LEN = 50;
    private static final int DRIVER_CLASS_BOX_LEN = 150;
    private static final int JDBC_URL_BOX_LEN = 150;
    private static final int USERNAME_BOX_LEN = 50;
    private static final int PASSWORD_BOX_LEN = 40;
    private static final int HOTKEY_BOX_LEN = 4;
    private static final int ORDER_BOX_LEN = 5;

    private final Mode dialogMode;

    private final TextBox connectionNameTextBox;
    private final TextBox driverClassTextBox;
    private final TextBox jdbcUrlTextBox;

    private final CheckBox loginAutomaticallyCheckBox;

    private final TextBox userNameTextBox;
    private final PasswordBox passwordPasswordBox;

    private final TextBox hotkeyTextBox;
    private final TextBox orderTextBox;

    private final String originalNameOfExistingConnectionDefinition;

    private final Action onSaveConnectionButtonSelectedAction = new Action() {

        public void doAction() {
            ConnectionSettingsWindow.this.onSaveButtonSelected();
        }
    };


    public ConnectionSettingsWindow() {
        this(null, Mode.ADD);
    }

    //CHECKSTYLE.OFF: AvoidInlineConditionals: such conditionals make code here easier to follow
    public ConnectionSettingsWindow(ConnectionDefinition cd, Mode mode) {
        super(mode.description);
        this.dialogMode = mode;

        this.originalNameOfExistingConnectionDefinition =
                dialogMode == Mode.EDIT ? cd.getConnectionName() : null;

        addComponent(new Button("BACK TO MAIN MENU", new Action() {
            public void doAction() {
                ConnectionSettingsWindow.this.onCancelButtonSelected();
            }
        }));

        addComponent(new Label("Connection name:"));
        connectionNameTextBox = new TextBox(cd != null ? cd.getConnectionName() : null, CONNECTION_NAME_BOX_LEN);
        addComponent(connectionNameTextBox);

        addComponent(new Label("Driver class:"));
        driverClassTextBox = new TextBox(cd != null ? cd.getDriverClassName() : null, DRIVER_CLASS_BOX_LEN);
        addComponent(driverClassTextBox);

        addComponent(new Label("JDBC URL:"));
        jdbcUrlTextBox = new TextBox(cd != null ? cd.getJdbcUrl() : null, JDBC_URL_BOX_LEN);
        addComponent(jdbcUrlTextBox);

        loginAutomaticallyCheckBox =
                new CheckBox("Log in automatically", cd != null && cd.getLoginAutomatically());
        addComponent(loginAutomaticallyCheckBox);

        addComponent(new Label("User name:"));
        userNameTextBox = new TextBox(cd != null ? cd.getUserName() : null, USERNAME_BOX_LEN);
        addComponent(userNameTextBox);

        addComponent(new Label("Password:"));
        passwordPasswordBox = new PasswordBox(cd != null ? cd.getPassword() : null, PASSWORD_BOX_LEN);
        addComponent(passwordPasswordBox);

        addComponent(new Label("HotKey to select this connection (ONE character, optional):"));
        hotkeyTextBox = new TextBox(getHotKeyString(cd), HOTKEY_BOX_LEN);
        addComponent(hotkeyTextBox);

        addComponent(new Label("Number for ordering in list (number, optional):"));
        orderTextBox = new TextBox(getOrderText(cd), ORDER_BOX_LEN);
        addComponent(orderTextBox);

        addComponent(new Button("SAVE CONNECTION", onSaveConnectionButtonSelectedAction));
    }

    private String getHotKeyString(ConnectionDefinition cd) {
        String hotkeyString;
        if (cd != null && cd.getHotkey() != null) {
            Character hotkey = cd.getHotkey();
            hotkeyString = new String(new char[]{hotkey});
        } else {
            hotkeyString = null;
        }
        return hotkeyString;
    }

    private String getOrderText(ConnectionDefinition cd) {
        String orderText;
        if (cd != null && cd.getOrder() != null) {
            orderText = cd.getOrder().toString();
        } else {
            orderText = null;
        }
        return orderText;
    }

    //CHECKSTYLE.ON
    private void onCancelButtonSelected() {
        this.close();
    }

    private void onSaveButtonSelected() {
        try {
            final String connectionName = connectionNameTextBox.getText();
            final String jdbcDriverClassName = driverClassTextBox.getText();
            final String jdbcUrl = jdbcUrlTextBox.getText();

            final boolean loginAutomatically = loginAutomaticallyCheckBox.isSelected();

            final String userName = userNameTextBox.getText();
            final String password = passwordPasswordBox.getText();

            final String hotkeyString = hotkeyTextBox.getText();
            final Character hotkey = mapHotKey(hotkeyString);

            final String orderString = orderTextBox.getText();
            final Integer order = mapOrder(orderString);

            ConnectionDefinition connectionDefinition = new ConnectionDefinition(
                    connectionName,
                    jdbcDriverClassName,
                    jdbcUrl,
                    loginAutomatically,
                    userName,
                    password,
                    hotkey,
                    order);

            switch (dialogMode) {

                case ADD:
                case COPY:
                    saveConnectionDefinition(connectionDefinition);
                    break;


                case EDIT:
                    updateConnectionDefinition(connectionDefinition);
                    break;

                default:
                    throw new IllegalStateException("Unknown dialogMode: " + dialogMode);
            }
            this.close();

        } catch (RuntimeException | SaveException e) {
            TerminalUI.showErrorMessageFromThrowable(e);
        }
    }

    private Character mapHotKey(String hotkeyString) {
        final Character hotkey;
        if (hotkeyString.trim().isEmpty()) {
            hotkey = null;
        } else {
            final int aSingleCharacterLengthString = 1;
            if (hotkeyString.trim().length() == aSingleCharacterLengthString) {
                hotkey = hotkeyString.charAt(0);
            } else {
                throw new IllegalArgumentException("The Hotkey must be one character long");
            }
        }
        return hotkey;
    }

    private Integer mapOrder(String orderString) {
        final Integer order;
        try {
            if (orderString.trim().isEmpty()) {
                order = null;
            } else {
                order = Integer.parseInt(orderString.trim());
            }
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Order can only be a number");
        }
        return order;
    }

    private void updateConnectionDefinition(ConnectionDefinition connectionDefinitionToUpdate) throws SaveException {
        String connectionName = connectionDefinitionToUpdate.getConnectionName();

        // In this setup, connection name is the primary key:
        // we can handle renames, but we have to implement it as delete-then-save
        final boolean nameChanged = !Objects.equals(connectionName, originalNameOfExistingConnectionDefinition);
        if (nameChanged) {
            ConnectionDefinitionRepository.getInstance()
                    .deleteConnectionDefinitionByName(originalNameOfExistingConnectionDefinition);
        }

        ConnectionDefinitionRepository.getInstance()
                .saveConnectionDefinition(connectionDefinitionToUpdate);
    }

    private void saveConnectionDefinition(ConnectionDefinition connectionDefinitionToSave) throws SaveException {


        try {

            String connectionName = connectionDefinitionToSave.getConnectionName();

            ConnectionDefinition existingConnectionDefinition = ConnectionDefinitionRepository.getInstance()
                    .findConnectionDefinitionByName(connectionName);

            if (existingConnectionDefinition != null) {
                throw new IllegalStateException("Connection with name '" + connectionName + "' already exists");
            }

            ConnectionDefinitionRepository.getInstance().saveConnectionDefinition(connectionDefinitionToSave);

        } catch (LoadException e) {
            throw new SaveException("Failed to save connection definition", e);
        }



    }
}
