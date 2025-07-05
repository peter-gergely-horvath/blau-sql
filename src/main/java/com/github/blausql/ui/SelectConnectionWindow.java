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
import com.github.blausql.core.connection.ConnectionDefinition;
import com.github.blausql.ui.components.ApplicationWindow;
import com.googlecode.lanterna.gui2.*;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;


abstract class SelectConnectionWindow extends ApplicationWindow {

    private final Map<Character, ConnectionDefinition> hotKeyMap = new ConcurrentHashMap<>();

    SelectConnectionWindow(String title, List<ConnectionDefinition> connectionDefinitions, TerminalUI terminalUI) {
        super(title, terminalUI);

        addWindowListener(new HotKeyWindowListener());

        Panel panel = new Panel();

        panel.addComponent(new Button("CANCEL (ESC)", new Runnable() {

            public void run() {
                SelectConnectionWindow.this.close();
            }
        }));


        if (connectionDefinitions.isEmpty()) {
            panel.addComponent(new Label("No connections defined"));
            panel.addComponent(new Label("Go to Manage Connections menu and define connections first!"));
        } else {

            for (final ConnectionDefinition connectionDefinition : connectionDefinitions) {
                String connectionName;

                Character hotkey = connectionDefinition.getHotkey();
                if (hotkey != null) {
                    connectionName = String.format("[%s] %s", hotkey, connectionDefinition.getConnectionName());

                    hotKeyMap.put(hotkey, connectionDefinition);
                } else {
                    connectionName = connectionDefinition.getConnectionName();
                }

                panel.addComponent(new Button(connectionName, new Runnable() {

                    public void run() {
                        SelectConnectionWindow.this.onConnectionSelected(connectionDefinition);
                    }
                }));
            }
        }

        setComponent(panel);
    }

    private final class HotKeyWindowListener extends WindowListenerAdapter {

        @Override
        public void onUnhandledInput(Window basePane, KeyStroke keyStroke, AtomicBoolean hasBeenHandled) {

            Character character = keyStroke.getCharacter();
            if (character != null) {

                ConnectionDefinition connectionDefinition = hotKeyMap.get(Character.toUpperCase(character));
                if (connectionDefinition == null) {
                    connectionDefinition = hotKeyMap.get(Character.toLowerCase(character));
                }

                if (connectionDefinition != null) {
                    SelectConnectionWindow.this.onConnectionSelected(connectionDefinition);
                }
            }

            if (KeyType.Escape.equals(keyStroke.getKeyType())) {
                SelectConnectionWindow.this.close();
            }
        }
    }

    protected abstract void onConnectionSelected(
            ConnectionDefinition connectionDefinition);

}
