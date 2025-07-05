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

import java.util.List;

final class SelectConnectionToEditWindow extends SelectConnectionWindow {

    SelectConnectionToEditWindow(List<ConnectionDefinition> connectionDefinitions, TerminalUI terminalUI) {
        super("Select Connection to Edit", connectionDefinitions, terminalUI);
    }

    @Override
    protected void onConnectionSelected(
            final ConnectionDefinition cd) {

        close();

        showWindowCenter(
                new ConnectionSettingsWindow(cd, ConnectionSettingsWindow.Mode.EDIT, getTerminalUI()));
    }
}
