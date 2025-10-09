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

import java.util.List;

final class SelectConnectionToCopyWindow extends SelectConnectionWindow {

    SelectConnectionToCopyWindow(List<ConnectionConfiguration> connectionConfigurations, TerminalUI terminalUI) {
        super("Select Connection to Copy", connectionConfigurations, terminalUI);
    }

    @Override
    protected void onConnectionSelected(
            final ConnectionConfiguration connectionConfiguration) {

        ConnectionConfiguration copyOfConnectionConfiguration = new ConnectionConfiguration(connectionConfiguration);

        String connectionName = copyOfConnectionConfiguration.getConnectionName();

        copyOfConnectionConfiguration.setConnectionName("Copy of " + connectionName);

        close();

        showWindowCenter(new ConnectionSettingsWindow(
                copyOfConnectionConfiguration, ConnectionSettingsWindow.Mode.COPY, getTerminalUI()));
    }
}
