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
import com.github.blausql.ui.components.CloseOnEscapeKeyPressWindow;
import com.github.blausql.ui.util.DefaultErrorHandlerAction;
import com.github.blausql.ui.util.HotKeySupportListener;

import com.google.common.collect.ImmutableMap;
import com.googlecode.lanterna.gui2.Button;


import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
class ManageConnectionsWindow extends CloseOnEscapeKeyPressWindow {


    ManageConnectionsWindow() {

        super("Manage Connections");

        addComponent(new Button("BACK (ESC)", new Runnable() {

            public void run() {
                ManageConnectionsWindow.this.close();
            }
        }));
        addComponent(new Button("[A]dd connection", onAddConnectionButtonSelectedRunnable));
        addComponent(new Button("[E]dit connection", onEditConnectionButtonSelectedRunnable));
        addComponent(new Button("[C]opy connection", onCopyConnectionButtonSelectedRunnable));
        addComponent(new Button("[D]elete connection", onDeleteConnectionButtonSelectedRunnable));

        addWindowListener(new HotKeySupportListener(
                ImmutableMap.<Character, Runnable>builder()
                        .put('A', onAddConnectionButtonSelectedRunnable)
                        .put('E', onEditConnectionButtonSelectedRunnable)
                        .put('C', onCopyConnectionButtonSelectedRunnable)
                        .put('D', onDeleteConnectionButtonSelectedRunnable)
                        .build(), true));
    }


    private final Runnable onAddConnectionButtonSelectedRunnable = new Runnable() {

        public void run() {
            ManageConnectionsWindow.this.close();
            TerminalUI.showWindowCenter(new ConnectionSettingsWindow());
        }
    };

    private final Runnable onCopyConnectionButtonSelectedRunnable = new DefaultErrorHandlerAction() {

        public void runWithErrorHandler() throws LoadException {
            ManageConnectionsWindow.this.close();

            List<ConnectionDefinition> connectionDefinitions =
                    ConnectionDefinitionRepository.getInstance().getConnectionDefinitions();

            TerminalUI.showWindowCenter(new SelectConnectionToCopyWindow(connectionDefinitions));
        }
    };

    private final Runnable onEditConnectionButtonSelectedRunnable = new DefaultErrorHandlerAction() {

        public void runWithErrorHandler() throws LoadException {
            ManageConnectionsWindow.this.close();

            List<ConnectionDefinition> connectionDefinitions =
                    ConnectionDefinitionRepository.getInstance().getConnectionDefinitions();

            TerminalUI.showWindowCenter(new SelectConnectionToEditWindow(connectionDefinitions));
        }
    };

    private final Runnable onDeleteConnectionButtonSelectedRunnable = new DefaultErrorHandlerAction() {

        public void runWithErrorHandler() throws LoadException {
            ManageConnectionsWindow.this.close();

            List<ConnectionDefinition> connectionDefinitions =
                    ConnectionDefinitionRepository.getInstance().getConnectionDefinitions();

            TerminalUI.showWindowCenter(new SelectConnectionToDeleteWindow(connectionDefinitions));
        }
    };
}
