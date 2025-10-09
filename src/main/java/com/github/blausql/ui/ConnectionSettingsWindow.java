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
import com.github.blausql.core.connection.ConnectionConfiguration;
import com.github.blausql.core.preferences.ConnectionConfigurationRepositoryFactory;

import com.github.blausql.spi.connections.DeleteException;
import com.github.blausql.spi.connections.LoadException;
import com.github.blausql.spi.connections.SaveException;
import com.github.blausql.ui.components.ApplicationWindow;
import com.github.blausql.ui.components.PasswordBox;
import com.github.blausql.ui.components.SimpleTextBox;
import com.github.blausql.ui.hotkey.HotKeyWindowListener;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings("FieldCanBeLocal")
public final class ConnectionSettingsWindow extends ApplicationWindow {

    public enum Mode {
        ADD("Add connection"),
        EDIT("Edit connection"),
        COPY("Copy connection");

        private final String description;

        Mode(String description) {
            this.description = description;
        }
    }

    private static final String EMPTY_VALUE = "";
    private static final String DEFAULT_STATEMENT_SEPARATOR = ";";

    private static final int CONNECTION_NAME_BOX_LEN = 50;
    private static final int DRIVER_CLASS_BOX_LEN = 100;
    private static final int JDBC_URL_BOX_LEN = 150;
    private static final int USERNAME_BOX_LEN = 50;
    private static final int PASSWORD_BOX_LEN = 40;
    private static final int STATEMENT_SEPARATOR_BOX_LEN = 8;
    private static final int HOTKEY_BOX_LEN = 4;
    private static final int ORDER_BOX_LEN = 5;

    private final Mode dialogMode;

    private final TextBox connectionNameTextBox;
    private final TextBox driverClassTextBox;
    private final TextBox jdbcUrlTextBox;

    private final CheckBox loginAutomaticallyCheckBox;

    private final TextBox userNameTextBox;
    private final TextBox passwordPasswordBox;

    private final TextBox statementSeparatorBox;

    private final TextBox hotkeyTextBox;
    private final TextBox orderTextBox;

    private final String originalNameOfExistingConnectionConfiguration;


    public ConnectionSettingsWindow(TerminalUI terminalUI) {
        this(null, Mode.ADD, terminalUI);
    }

    //CHECKSTYLE.OFF: AvoidInlineConditionals: such conditionals make code here easier to follow
    public ConnectionSettingsWindow(ConnectionConfiguration connectionConfiguration, Mode mode, TerminalUI terminalUI) {
        super(mode.description, terminalUI);
        this.dialogMode = mode;

        this.originalNameOfExistingConnectionConfiguration = dialogMode == Mode.EDIT
                ? connectionConfiguration.getConnectionName() : null;

        Panel mainPanel = new Panel(new LinearLayout(Direction.VERTICAL));

        connectionNameTextBox = createConnectionNameField(connectionConfiguration, mainPanel);
        driverClassTextBox = createDriverClassField(connectionConfiguration, mainPanel);
        jdbcUrlTextBox = createJdbcUrlField(connectionConfiguration, terminalUI, mainPanel);
        loginAutomaticallyCheckBox = createLoginAutomaticallyField(connectionConfiguration, mainPanel);
        userNameTextBox = createUserNameField(connectionConfiguration, mainPanel);
        passwordPasswordBox = createPasswordField(connectionConfiguration, mainPanel);
        statementSeparatorBox = createStatementSeparatorField(connectionConfiguration, mode, mainPanel);
        hotkeyTextBox = createHotkeyField(connectionConfiguration, mainPanel);
        orderTextBox = createOrderField(connectionConfiguration, mainPanel);

        Panel buttonPanel = getButtonPanel();
        mainPanel.addComponent(new EmptySpace());
        mainPanel.addComponent(buttonPanel);

        setComponent(mainPanel);
        addWindowListener(getHotKeyWindowListener());
    }

    private static TextBox createConnectionNameField(
            ConnectionConfiguration connectionConfiguration, Panel mainPanel) {

        return addTextEntryComponents("Connection name", connectionConfiguration,
                ConnectionConfiguration::getConnectionName, CONNECTION_NAME_BOX_LEN, mainPanel);
    }

    private static TextBox createDriverClassField(
            ConnectionConfiguration connectionConfiguration, Panel mainPanel) {

        return addTextEntryComponents("Driver class", connectionConfiguration,
                ConnectionConfiguration::getDriverClassName, DRIVER_CLASS_BOX_LEN, mainPanel);
    }

    private static TextBox createJdbcUrlField(
            ConnectionConfiguration connectionConfiguration, TerminalUI terminalUI, Panel mainPanel) {

        return addTextEntryComponents("JDBC URL", connectionConfiguration,
                ConnectionConfiguration::getJdbcUrl, getBoxLength(terminalUI, JDBC_URL_BOX_LEN), mainPanel);
    }

    private static CheckBox createLoginAutomaticallyField(
            ConnectionConfiguration connectionConfiguration, Panel mainPanel) {

        CheckBox checkBox = new CheckBox("Log in automatically");
        checkBox.setChecked(isLoginAutomaticallyEnabled(connectionConfiguration));
        mainPanel.addComponent(checkBox);
        return checkBox;
    }

    private TextBox createUserNameField(
            ConnectionConfiguration connectionConfiguration, Panel mainPanel) {

        return addTextEntryComponents("User name", connectionConfiguration,
                ConnectionConfiguration::getUserName, USERNAME_BOX_LEN, mainPanel);
    }

    private PasswordBox createPasswordField(
            ConnectionConfiguration connectionConfiguration, Panel mainPanel) {

        return addPasswordEntryComponents("Password", connectionConfiguration,
                ConnectionConfiguration::getPassword, PASSWORD_BOX_LEN, mainPanel);
    }

    private TextBox createStatementSeparatorField(
            ConnectionConfiguration connectionConfiguration, Mode mode, Panel mainPanel) {

        TextBox textBox = addTextEntryComponents("Statement separator", connectionConfiguration,
                ConnectionConfiguration::getStatementSeparator, STATEMENT_SEPARATOR_BOX_LEN, mainPanel);

        if (mode == Mode.ADD) {
            textBox.setText(DEFAULT_STATEMENT_SEPARATOR);
        }
        return textBox;
    }

    private TextBox createHotkeyField(
            ConnectionConfiguration connectionConfiguration, Panel mainPanel) {

        return addTextEntryComponents(
                "HotKey to select this connection (ONE character, optional):",
                connectionConfiguration,
                ConnectionSettingsWindow::getHotKeyString, HOTKEY_BOX_LEN, mainPanel);
    }

    private TextBox createOrderField(
            ConnectionConfiguration connectionConfiguration, Panel mainPanel) {

        return addTextEntryComponents(
                "Number for ordering in list (number, optional):",
                connectionConfiguration,
                ConnectionSettingsWindow::getOrderText, ORDER_BOX_LEN, mainPanel);
    }

    private static Boolean isLoginAutomaticallyEnabled(
            ConnectionConfiguration connectionConfiguration) {

        if (connectionConfiguration == null) {
            return false;
        } else {
            return connectionConfiguration.getLoginAutomatically();
        }
    }

    private HotKeyWindowListener getHotKeyWindowListener() {
        return HotKeyWindowListener.builder()
                .keyType(KeyType.F5).invoke(this::onSaveButtonSelected)
                .keyType(KeyType.Escape).invoke(this::onCancelButtonSelected)
                .build();
    }

    private static int getBoxLength(TerminalUI terminalUI, int defaultLength) {

        WindowBasedTextGUI windowBasedTextGUI = terminalUI.getWindowBasedTextGUI();
        Screen screen = windowBasedTextGUI.getScreen();
        TerminalSize terminalSize = screen.getTerminalSize();

        final int uiBordersColumns = 4;

        int columns = terminalSize.getColumns() - uiBordersColumns;

        return Math.min(columns, defaultLength);
    }

    private static TextBox addTextEntryComponents(String label,
                                                  ConnectionConfiguration connectionConfiguration,
                                                  Function<ConnectionConfiguration, String> valueFunction,
                                                  int length,
                                                  Panel mainPanel) {

        return addDataEntryComponent(SimpleTextBox::new, label, connectionConfiguration,
                valueFunction, length, mainPanel);
    }

    private static PasswordBox addPasswordEntryComponents(String label,
                                                          ConnectionConfiguration connectionConfiguration,
                                                          Function<ConnectionConfiguration, String> valueFunction,
                                                          int length,
                                                          Panel mainPanel) {

        return addDataEntryComponent(PasswordBox::new, label, connectionConfiguration,
                valueFunction, length, mainPanel);
    }

    private static <T extends Component> T addDataEntryComponent(
            BiFunction<String, Integer, T> componentFunction,
            String label,
            ConnectionConfiguration connectionConfiguration,
            Function<ConnectionConfiguration, String> valueFunction,
            int length,
            Panel mainPanel) {

        mainPanel.addComponent(new Label(String.format("%s:", label)));

        String value;
        if (connectionConfiguration == null) {
            value = EMPTY_VALUE;
        } else {
            value = valueFunction.apply(connectionConfiguration);
            if (value == null) {
                value = EMPTY_VALUE;
            }
        }

        T textBox = componentFunction.apply(value, length);
        mainPanel.addComponent(textBox);
        return textBox;
    }

    private static String getHotKeyString(ConnectionConfiguration connectionConfiguration) {
        String hotkeyString;
        if (connectionConfiguration != null && connectionConfiguration.getHotkey() != null) {
            Character hotkey = connectionConfiguration.getHotkey();
            hotkeyString = String.valueOf(hotkey);
        } else {
            hotkeyString = null;
        }
        return hotkeyString;
    }

    private static String getOrderText(ConnectionConfiguration connectionConfiguration) {
        String orderText;
        if (connectionConfiguration != null && connectionConfiguration.getOrder() != null) {
            orderText = connectionConfiguration.getOrder().toString();
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
            ConnectionConfiguration connectionConfiguration = buildConnectionConfiguration();

            switch (dialogMode) {
                case ADD:
                case COPY:
                    saveConnectionDefinition(connectionConfiguration);
                    break;

                case EDIT:
                    updateConnectionDefinition(connectionConfiguration);
                    break;

                default:
                    throw new IllegalStateException("Unknown dialogMode: " + dialogMode);
            }
            this.close();

        } catch (RuntimeException | SaveException e) {
            showErrorMessageFromThrowable(e);
        }
    }

    private ConnectionConfiguration buildConnectionConfiguration() {

        final String connectionName = connectionNameTextBox.getText();
        final String jdbcDriverClassName = driverClassTextBox.getText();
        final String jdbcUrl = jdbcUrlTextBox.getText();

        final boolean loginAutomatically = loginAutomaticallyCheckBox.isChecked();

        final String userName = userNameTextBox.getText();
        final String password = passwordPasswordBox.getText();

        final String statementSeparator = statementSeparatorBox.getText();

        final String hotkeyString = hotkeyTextBox.getText();
        final Character hotkey = mapHotKey(hotkeyString);

        final String orderString = orderTextBox.getText();
        final Integer order = mapOrder(orderString);

        return new ConnectionConfiguration(
                connectionName,
                jdbcDriverClassName,
                jdbcUrl,
                loginAutomatically,
                userName,
                password,
                statementSeparator,
                hotkey,
                order);
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

    private void updateConnectionDefinition(ConnectionConfiguration connectionConfigurationToUpdate)
            throws SaveException {

        try {
            String connectionName = connectionConfigurationToUpdate.getConnectionName();

            // In this setup, connection name is the primary key:
            // we can handle renames, but we have to implement it as delete-then-save
            final boolean nameChanged = !Objects.equals(connectionName,
                    originalNameOfExistingConnectionConfiguration);
            if (nameChanged) {
                ConnectionConfigurationRepositoryFactory.getRepository()
                        .deleteConnectionConfigurationByName(originalNameOfExistingConnectionConfiguration);
            }

            ConnectionConfigurationRepositoryFactory.getRepository()
                    .saveConnectionConfiguration(connectionConfigurationToUpdate);

        } catch (DeleteException e) {
            throw new SaveException("Could not delete previous state", e);
        }

    }

    private void saveConnectionDefinition(ConnectionConfiguration connectionConfigurationToSave)
            throws SaveException {

        try {
            String connectionName = connectionConfigurationToSave.getConnectionName();

            ConnectionConfiguration existingConnectionConfiguration =
                    ConnectionConfigurationRepositoryFactory.getRepository()
                            .findConnectionConfigurationByName(connectionName);

            if (existingConnectionConfiguration != null) {
                throw new IllegalStateException("Connection with name '" + connectionName + "' already exists");
            }

            ConnectionConfigurationRepositoryFactory.getRepository()
                    .saveConnectionConfiguration(connectionConfigurationToSave);

        } catch (LoadException e) {
            throw new SaveException("Failed to save connection configuration", e);
        }
    }
}
