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
import com.github.blausql.ui.components.ApplicationWindow;
import com.github.blausql.ui.components.PasswordBox;
import com.github.blausql.ui.hotkey.HotKeyWindowListener;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyType;


import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings("FieldCanBeLocal")
public final class ConnectionSettingsWindow extends ApplicationWindow {

    public static final String EMPTY_VALUE = "";

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
    private final TextBox passwordPasswordBox;

    private final TextBox hotkeyTextBox;
    private final TextBox orderTextBox;

    private final String originalNameOfExistingConnectionDefinition;


    public ConnectionSettingsWindow(TerminalUI terminalUI) {
        this(null, Mode.ADD, terminalUI);
    }

    //CHECKSTYLE.OFF: AvoidInlineConditionals: such conditionals make code here easier to follow
    public ConnectionSettingsWindow(ConnectionDefinition cd, Mode mode, TerminalUI terminalUI) {
        super(mode.description, terminalUI);
        this.dialogMode = mode;

        this.originalNameOfExistingConnectionDefinition =
                dialogMode == Mode.EDIT ? cd.getConnectionName() : null;

        Panel mainPanel = new Panel(new LinearLayout(Direction.VERTICAL));

        connectionNameTextBox = addTextEntryComponents("Connection name", cd,
                ConnectionDefinition::getConnectionName, CONNECTION_NAME_BOX_LEN, mainPanel);

        driverClassTextBox = addTextEntryComponents("Driver class", cd,
                ConnectionDefinition::getDriverClassName, DRIVER_CLASS_BOX_LEN, mainPanel);


        jdbcUrlTextBox = addTextEntryComponents("JDBC URL", cd,
                ConnectionDefinition::getJdbcUrl, JDBC_URL_BOX_LEN, mainPanel);

        loginAutomaticallyCheckBox = new CheckBox("Log in automatically");
        loginAutomaticallyCheckBox.setChecked(
                Optional.ofNullable(cd).map(ConnectionDefinition::getLoginAutomatically).orElse(false));
        mainPanel.addComponent(loginAutomaticallyCheckBox);

        userNameTextBox = addTextEntryComponents("User name", cd,
                ConnectionDefinition::getUserName, USERNAME_BOX_LEN, mainPanel);

        passwordPasswordBox = addPasswordEntryComponents("Password", cd,
                ConnectionDefinition::getPassword, PASSWORD_BOX_LEN, mainPanel);

        hotkeyTextBox = addTextEntryComponents("HotKey to select this connection (ONE character, optional):", cd,
                ConnectionSettingsWindow::getHotKeyString, HOTKEY_BOX_LEN, mainPanel);

        orderTextBox = addTextEntryComponents("Number for ordering in list (number, optional):", cd,
                ConnectionSettingsWindow::getOrderText, HOTKEY_BOX_LEN, mainPanel);

        Panel buttonPanel = getButtonPanel();

        mainPanel.addComponent(new EmptySpace());

        mainPanel.addComponent(buttonPanel);

        setComponent(mainPanel);

        addWindowListener(HotKeyWindowListener.builder()
                .keyType(KeyType.F5).invoke(this::onSaveButtonSelected)
                .keyType(KeyType.Escape).invoke(this::onCancelButtonSelected)
                .build());
    }

    private static TextBox addTextEntryComponents(String label, ConnectionDefinition cd,
                                                  Function<ConnectionDefinition, String> valueFunction,
                                                  int length,
                                                  Panel mainPanel) {

        return addDataEntryComponent(SimpleTextBox::new, label, cd, valueFunction, length, mainPanel);
    }

    private static PasswordBox addPasswordEntryComponents(String label, ConnectionDefinition cd,
                                                          Function<ConnectionDefinition, String> valueFunction,
                                                          int length,
                                                          Panel mainPanel) {

        return addDataEntryComponent(PasswordBox::new, label, cd, valueFunction, length, mainPanel);
    }

    private static <T extends Component> T addDataEntryComponent(BiFunction<String, Integer, T> componentFunction,
                                                                 String label,
                                                                 ConnectionDefinition cd,
                                                                 Function<ConnectionDefinition, String> valueFunction,
                                                                 int length,
                                                                 Panel mainPanel) {

        mainPanel.addComponent(new Label(String.format("%s:", label)));

        String value;
        if (cd == null) {
            value = EMPTY_VALUE;
        } else {
            value = valueFunction.apply(cd);
            if (value == null) {
                value = EMPTY_VALUE;
            }
        }

        T textBox = componentFunction.apply(value, length);
        mainPanel.addComponent(textBox);
        return textBox;
    }

    private static String getHotKeyString(ConnectionDefinition cd) {
        String hotkeyString;
        if (cd != null && cd.getHotkey() != null) {
            Character hotkey = cd.getHotkey();
            hotkeyString = String.valueOf(hotkey);
        } else {
            hotkeyString = null;
        }
        return hotkeyString;
    }

    private static String getOrderText(ConnectionDefinition cd) {
        String orderText;
        if (cd != null && cd.getOrder() != null) {
            orderText = cd.getOrder().toString();
        } else {
            orderText = null;
        }
        return orderText;
    }


    private Panel getButtonPanel() {
        Panel buttonPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        buttonPanel.addComponent(new Button("Save Connection (F5)", this::onSaveButtonSelected));
        buttonPanel.addComponent(new Button("Discard Changes (ESC)", this::onCancelButtonSelected));
        return buttonPanel;
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

            final boolean loginAutomatically = loginAutomaticallyCheckBox.isChecked();

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
            showErrorMessageFromThrowable(e);
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
