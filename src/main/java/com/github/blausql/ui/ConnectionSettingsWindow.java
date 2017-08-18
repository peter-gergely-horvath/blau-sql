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

public class ConnectionSettingsWindow extends Window {

    public enum Mode {
        ADD("Add connection"), EDIT("Edit connection"), COPY("Copy connection");

        private final String description;

        Mode(String description) {
            this.description = description;
        }
    }

    private final Mode dialogMode;

    private final TextBox connectionNameTextBox;
    private final TextBox driverClassTextBox;
    private final TextBox jdbcUrlTextBox;

    private final CheckBox loginAutomaticallyCheckBox;

    private final TextBox userNameTextBox;
    private final PasswordBox passwordPasswordBox;

    private final String originalNameOfExistingConnectionDefinition;


    public ConnectionSettingsWindow() {
        this(null, Mode.ADD);
    }

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
        addComponent(connectionNameTextBox =
                new TextBox(cd != null ? cd.getConnectionName() : null, 50));

        addComponent(new Label("Driver class:"));
        addComponent(driverClassTextBox =
                new TextBox(cd != null ? cd.getDriverClassName() : null, 150));

        addComponent(new Label("JDBC URL:"));
        addComponent(jdbcUrlTextBox =
                new TextBox(cd != null ? cd.getJdbcUrl() : null, 150));

        addComponent(loginAutomaticallyCheckBox =
                new CheckBox("Log in automatically", cd != null ? cd.getLoginAutomatically() : false));

        addComponent(new Label("User name:"));
        addComponent(userNameTextBox =
                new TextBox(cd != null ? cd.getUserName() : null, 50));

        addComponent(new Label("Password:"));
        addComponent(passwordPasswordBox =
                new PasswordBox(cd != null ? cd.getPassword() : null, 40));


        addComponent(new Button("SAVE CONNECTION", new Action() {

            public void doAction() {
                ConnectionSettingsWindow.this.onSaveButtonSelected();
            }
        }));
    }

    protected void onCancelButtonSelected() {
        this.close();
    }

    protected void onSaveButtonSelected() {

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