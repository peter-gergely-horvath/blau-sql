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

 
package com.github.blausql.ui.util;

import java.util.Map;

import org.springframework.util.Assert;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.listener.WindowAdapter;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.input.Key.Kind;

public class HotKeySupportListener extends WindowAdapter {

    private final Map<Character, Action> hotkeyToActionMap;
    private final boolean closeOnEscape;

    public HotKeySupportListener(Map<Character, Action> hotkeyToActionMap, boolean closeOnEsc) {
        this.hotkeyToActionMap = hotkeyToActionMap;
        this.closeOnEscape = closeOnEsc;
    }


    @Override
    public void onUnhandledKeyboardInteraction(Window window, Key key) {

        final Kind keyKind = key.getKind();

        Assert.notNull(keyKind, "kind retrieved from key is null");

        switch (keyKind) {

            case NormalKey:
                final char characterKey = key.getCharacter();

                Action action;
                action = hotkeyToActionMap.get(Character.toUpperCase(characterKey));
                if (action == null) {
                    action = hotkeyToActionMap.get(Character.toLowerCase(characterKey));
                }

                if (action != null) {
                    action.doAction();
                }

                break;

            case Escape:
                if (closeOnEscape) {
                    window.close();
                }

                break;

            default:
                // nothing to do
                break;
        }
    }

}
