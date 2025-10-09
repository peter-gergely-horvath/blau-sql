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
import com.github.blausql.ui.components.ApplicationWindow;
import com.googlecode.lanterna.gui2.*;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;


abstract class SelectConnectionWindow extends ApplicationWindow {

    private final Map<Character, ConnectionConfiguration> hotKeyMap = new ConcurrentHashMap<>();

    SelectConnectionWindow(String title, List<ConnectionConfiguration> connectionConfigurations,
                           TerminalUI terminalUI) {
        super(title, terminalUI);

        addWindowListener(new HotKeyWindowListener());

        Panel panel = new Panel();

        panel.addComponent(new Button("CANCEL (ESC)", new Runnable() {

            public void run() {
                SelectConnectionWindow.this.close();
            }
        }));

        panel.addComponent(new EmptySpace());

        if (connectionConfigurations.isEmpty()) {
            panel.addComponent(new Label("(No connection configuration is available)"));
        } else {
            for (final ConnectionConfiguration connectionConfiguration : connectionConfigurations) {

                String buttonText;

                Character hotkey = connectionConfiguration.getHotkey();
                if (hotkey != null) {
                    buttonText = String.format("[%s] %s", hotkey, connectionConfiguration.getConnectionName());

                    hotKeyMap.put(normalizeHotkey(hotkey), connectionConfiguration);
                } else {
                    buttonText = connectionConfiguration.getConnectionName();
                }

                Button button = new Button(buttonText, new Runnable() {

                    public void run() {
                        onConnectionSelected(connectionConfiguration);
                    }
                });

                panel.addComponent(button);
            }
        }

        setComponent(panel);
    }

    class HotKeyWindowListener extends WindowListenerAdapter {

        @Override
        public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {

            if (keyStroke.getKeyType() == KeyType.Character) {
                Character character = keyStroke.getCharacter();

                ConnectionConfiguration connectionConfiguration = hotKeyMap.get(normalizeHotkey(character));

                if (connectionConfiguration != null) {
                    onConnectionSelected(connectionConfiguration);
                    deliverEvent.set(false);
                }

            } else if (keyStroke.getKeyType() == KeyType.Escape) {
                close();
                deliverEvent.set(false);
            }
        }
    }

    private static char normalizeHotkey(Character hotkey) {
        return Character.toUpperCase(hotkey);
    }

    protected abstract void onConnectionSelected(
            ConnectionConfiguration connectionConfiguration);

}
