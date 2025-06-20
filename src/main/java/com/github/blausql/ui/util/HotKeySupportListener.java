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

 
package com.github.blausql.ui.util;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowListenerAdapter;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;


public final class HotKeySupportListener extends WindowListenerAdapter {

    private final Map<Character, Runnable> hotkeyToRunnableMap;
    private final boolean closeOnEscape;

    public HotKeySupportListener(Map<Character, Runnable> hotkeyToRunnableMap, boolean closeOnEsc) {
        this.hotkeyToRunnableMap = hotkeyToRunnableMap;
        this.closeOnEscape = closeOnEsc;
    }

    @Override
    public void onUnhandledInput(Window basePane, KeyStroke keyStroke, AtomicBoolean hasBeenHandled) {

        KeyType keyType = keyStroke.getKeyType();
        Character character = keyStroke.getCharacter();

        if (closeOnEscape && KeyType.Escape.equals(keyType) ) {
            basePane.close();

            hasBeenHandled.set(true);

        } else if (character != null) {
            Runnable runnable = hotkeyToRunnableMap.get(Character.toUpperCase(character));
            if (runnable == null) {
                runnable = hotkeyToRunnableMap.get(Character.toLowerCase(character));
            }

            if (runnable != null) {
                runnable.run();

                hasBeenHandled.set(true);
            }
        }
    }
}
