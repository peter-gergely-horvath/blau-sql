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
import com.github.blausql.core.preferences.ConnectionDefinitionRepository;
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

public abstract class SelectConnectionWindow extends CloseOnEscapeKeyPressWindow {

    private static final ImmutableList<Character> HOTKEY_CHARACTERS = ImmutableList.<Character>builder().add(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    ).build();


    private final Map<Character, ConnectionDefinition> hotKeyMap = new ConcurrentHashMap<>();

    public SelectConnectionWindow(String title) {
        super(title);

        addWindowListener(new HotKeyWindowListener());

        addComponent(new Button("CANCEL (ESC)", new Action() {

            public void doAction() {
                SelectConnectionWindow.this.close();
            }
        }));


        List<ConnectionDefinition> connectionDefinitions =
                ConnectionDefinitionRepository.getInstance().getConnectionDefinitions();

        if (connectionDefinitions.isEmpty()) {
            addComponent(new Label("(no connections defined)"));
        } else {

            int index = 0;

            for (final ConnectionDefinition connectionDefinition : connectionDefinitions) {
                String connectionName;

                if (index < HOTKEY_CHARACTERS.size()) {
                    Character hotkeyChar = HOTKEY_CHARACTERS.get(index++);

                    connectionName = String.format("[%s] %s", hotkeyChar, connectionDefinition.getConnectionName());

                    hotKeyMap.put(hotkeyChar, connectionDefinition);

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
                Character mapKey = Character.valueOf(characterKey);

                ConnectionDefinition connectionDefinition = hotKeyMap.get(mapKey);
                if (connectionDefinition != null) {
                    SelectConnectionWindow.this.onConnectionSelected(connectionDefinition);
                }
            }
        }
    }

    protected abstract void onConnectionSelected(
            ConnectionDefinition connectionDefinition);

}
