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
import com.github.blausql.core.preferences.ConnectionDefinitionRepository;

import com.google.common.base.Objects;
import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.component.CheckBox;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.PasswordBox;
import com.googlecode.lanterna.gui.component.TextBox;

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

    private final Mode dialogMode;

    private final TextBox connectionNameTextBox;
    private final TextBox driverClassTextBox;
    private final TextBox jdbcUrlTextBox;

    private final CheckBox loginAutomaticallyCheckBox;

    private final TextBox userNameTextBox;
    private final PasswordBox passwordPasswordBox;

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

        if (dialogMode == Mode.EDIT) {
            this.originalNameOfExistingConnectionDefinition = cd.getConnectionName();

        } else {
            this.originalNameOfExistingConnectionDefinition = null;
        }

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
        jdbcUrlTextBox =
                new TextBox(cd != null ? cd.getJdbcUrl() : null, JDBC_URL_BOX_LEN);
        addComponent(jdbcUrlTextBox);

        loginAutomaticallyCheckBox =
                new CheckBox("Log in automatically", cd != null && cd.getLoginAutomatically());
        addComponent(loginAutomaticallyCheckBox);

        addComponent(new Label("User name:"));
        userNameTextBox =
                new TextBox(cd != null ? cd.getUserName() : null, USERNAME_BOX_LEN);
        addComponent(userNameTextBox);

        addComponent(new Label("Password:"));
        passwordPasswordBox = new PasswordBox(cd != null ? cd.getPassword() : null, PASSWORD_BOX_LEN);
        addComponent(passwordPasswordBox);

        addComponent(new Button("SAVE CONNECTION", onSaveConnectionButtonSelectedAction));
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


            ConnectionDefinition connectionDefinition = new ConnectionDefinition(
                    connectionName,
                    jdbcDriverClassName,
                    jdbcUrl,
                    loginAutomatically,
                    userName,
                    password);

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
        } catch (Exception e) {
            TerminalUI.showErrorMessageFromThrowable(e);
        }

    }

    private void updateConnectionDefinition(ConnectionDefinition connectionDefinitionToUpdate) {
        String connectionName = connectionDefinitionToUpdate.getConnectionName();

        final boolean nameChanged = !Objects.equal(connectionName, originalNameOfExistingConnectionDefinition);

        ConnectionDefinitionRepository.getInstance()
                .saveConnectionDefinition(connectionDefinitionToUpdate);

        if (nameChanged) {
            ConnectionDefinitionRepository.getInstance()
                    .deleteConnectionDefinitionByName(originalNameOfExistingConnectionDefinition);
        }


    }

    private void saveConnectionDefinition(ConnectionDefinition connectionDefinitionToSave) {

        String connectionName = connectionDefinitionToSave.getConnectionName();

        ConnectionDefinition existingConnectionDefinition =
                ConnectionDefinitionRepository.getInstance()
                        .findConnectionDefinitionByName(connectionName);

        if (existingConnectionDefinition != null) {
            throw new IllegalStateException("Connection with name '" + connectionName + "' already exists");
        }

        ConnectionDefinitionRepository.getInstance()
                .saveConnectionDefinition(connectionDefinitionToSave);

    }
}
