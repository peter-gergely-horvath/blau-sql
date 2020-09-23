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

import com.github.blausql.core.connection.ConnectionDefinition;
import com.github.blausql.ui.components.CloseOnEscapeKeyPressWindow;
import com.google.common.collect.ImmutableList;
import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.listener.WindowAdapter;
import com.googlecode.lanterna.input.Key;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

abstract class SelectConnectionWindow extends CloseOnEscapeKeyPressWindow {

    private final Map<Character, ConnectionDefinition> hotKeyMap = new ConcurrentHashMap<>();

    SelectConnectionWindow(String title, List<ConnectionDefinition> connectionDefinitions) {
        super(title);

        addWindowListener(new HotKeyWindowListener());

        addComponent(new Button("CANCEL (ESC)", new Action() {

            public void doAction() {
                SelectConnectionWindow.this.close();
            }
        }));


        if (connectionDefinitions.isEmpty()) {
            addComponent(new Label("No connections defined"));
            addComponent(new Label("Go to Manage Connections menu and define connections first!"));
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

                addComponent(new Button(connectionName, new Action() {

                    public void doAction() {
                        SelectConnectionWindow.this.onConnectionSelected(connectionDefinition);
                    }
                }));
            }
        }
    }

    private final class HotKeyWindowListener extends WindowAdapter {
        @Override
        public void onUnhandledKeyboardInteraction(Window window, Key key) {

            final Key.Kind keyKind = key.getKind();

            Assert.notNull(keyKind, "kind retrieved from key is null");

            if (keyKind == Key.Kind.NormalKey) {
                final char characterKey = key.getCharacter();

                ConnectionDefinition connectionDefinition = hotKeyMap.get(Character.toUpperCase(characterKey));
                if (connectionDefinition == null) {
                    connectionDefinition = hotKeyMap.get(Character.toLowerCase(characterKey));
                }

                if (connectionDefinition != null) {
                    SelectConnectionWindow.this.onConnectionSelected(connectionDefinition);
                }
            }
        }
    }

    protected abstract void onConnectionSelected(
            ConnectionDefinition connectionDefinition);

}
