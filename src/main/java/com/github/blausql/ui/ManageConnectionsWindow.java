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
import com.github.blausql.ui.components.CloseOnEscapeKeyPressWindow;
import com.github.blausql.ui.util.HotKeySupportListener;

import com.google.common.collect.ImmutableMap;
import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.component.Button;

@SuppressWarnings("FieldCanBeLocal")
class ManageConnectionsWindow extends CloseOnEscapeKeyPressWindow {


    ManageConnectionsWindow() {

        super("Manage Connections");

        addComponent(new Button("BACK (ESC)", new Action() {

            public void doAction() {
                ManageConnectionsWindow.this.close();
            }
        }));
        addComponent(new Button("[A]dd connection", onAddConnectionButtonSelectedAction));
        addComponent(new Button("[E]dit connection", onEditConnectionButtonSelectedAction));
        addComponent(new Button("[C]opy connection", onCopyConnectionButtonSelectedAction));
        addComponent(new Button("[D]elete connection", onDeleteConnectionButtonSelectedAction));

        addWindowListener(new HotKeySupportListener(
                ImmutableMap.<Character, Action>builder()
                        .put('A', onAddConnectionButtonSelectedAction)
                        .put('E', onEditConnectionButtonSelectedAction)
                        .put('C', onCopyConnectionButtonSelectedAction)
                        .put('D', onDeleteConnectionButtonSelectedAction)
                        .build(), true));
    }


    private final Action onAddConnectionButtonSelectedAction = new Action() {

        public void doAction() {
            ManageConnectionsWindow.this.close();
            TerminalUI.showWindowCenter(new ConnectionSettingsWindow());
        }
    };

    private final Action onCopyConnectionButtonSelectedAction = new Action() {

        public void doAction() {
            ManageConnectionsWindow.this.close();
            TerminalUI.showWindowCenter(new SelectConnectionToCopyWindow());
        }
    };

    private final Action onEditConnectionButtonSelectedAction = new Action() {

        public void doAction() {
            ManageConnectionsWindow.this.close();
            TerminalUI.showWindowCenter(new SelectConnectionToEditWindow());
        }
    };

    private final Action onDeleteConnectionButtonSelectedAction = new Action() {

        public void doAction() {
            ManageConnectionsWindow.this.close();
            TerminalUI.showWindowCenter(new SelectConnectionToDeleteWindow());
        }
    };
}
