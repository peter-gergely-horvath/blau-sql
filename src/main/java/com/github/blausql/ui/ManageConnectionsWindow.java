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
import com.github.blausql.spi.connections.LoadException;
import com.github.blausql.ui.components.ApplicationWindow;

import com.github.blausql.ui.hotkey.HotKeyWindowListener;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Panels;
import com.googlecode.lanterna.input.KeyType;

import java.util.List;

final class ManageConnectionsWindow extends ApplicationWindow {

    ManageConnectionsWindow(TerminalUI terminalUI) {

        super("Manage Connections", terminalUI);

        Panel mainPanel = Panels.vertical(
                button("BACK (ESC)", this::close),
                button("[A]dd connection", this::onAddConnectionButtonSelected),
                button("[E]dit connection", this::onEditConnectionButtonSelected),
                button("[C]opy connection", this::onCopyConnectionButtonSelected),
                button("[D]elete connection", this::onDeleteConnectionButtonSelected));

        setComponent(mainPanel);

        addWindowListener(HotKeyWindowListener.builder()
                .keyType(KeyType.Escape).invoke(this::close)
                .character('A').invoke(this::onAddConnectionButtonSelected)
                .character('E').invoke(withDefaultExceptionHandler(this::onEditConnectionButtonSelected))
                .character('C').invoke(withDefaultExceptionHandler(this::onCopyConnectionButtonSelected))
                .character('D').invoke(withDefaultExceptionHandler(this::onDeleteConnectionButtonSelected))
                .build());
    }


    private void onAddConnectionButtonSelected() {

        this.close();
        showWindowCenter(new ConnectionSettingsWindow(getTerminalUI()));
    }

    private void onCopyConnectionButtonSelected() throws LoadException {

        this.close();

        List<ConnectionConfiguration> connectionConfigurations =
                ConnectionConfigurationRepositoryFactory.getRepository().getConnectionConfigurations();

        showWindowCenter(new SelectConnectionToCopyWindow(connectionConfigurations, getTerminalUI()));
    }

    private void onEditConnectionButtonSelected() throws LoadException {

        this.close();

        List<ConnectionConfiguration> connectionConfigurations =
                ConnectionConfigurationRepositoryFactory.getRepository().getConnectionConfigurations();

        showWindowCenter(new SelectConnectionToEditWindow(connectionConfigurations, getTerminalUI()));
    }

    private void onDeleteConnectionButtonSelected() throws LoadException {

        this.close();

        List<ConnectionConfiguration> connectionConfigurations =
                ConnectionConfigurationRepositoryFactory.getRepository().getConnectionConfigurations();

        showWindowCenter(new SelectConnectionToDeleteWindow(connectionConfigurations, getTerminalUI()));
    }
}
