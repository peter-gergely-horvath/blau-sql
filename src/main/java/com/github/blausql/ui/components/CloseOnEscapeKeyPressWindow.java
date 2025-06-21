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


package com.github.blausql.ui.components;

import com.github.blausql.ui.LegacyWindowSupport;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowListenerAdapter;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import java.util.concurrent.atomic.AtomicBoolean;


public abstract class CloseOnEscapeKeyPressWindow extends LegacyWindowSupport {

    protected CloseOnEscapeKeyPressWindow(String title) {
        super(title);

        addWindowListener(new WindowListenerAdapter() {

            @Override
            public void onUnhandledInput(Window basePane, KeyStroke keyStroke, AtomicBoolean hasBeenHandled) {

                KeyType keyType = keyStroke.getKeyType();
                if (keyType != null && keyType.equals(KeyType.Escape)) {

                    hasBeenHandled.set(true);

                    CloseOnEscapeKeyPressWindow.this.close();

                } else {

                    super.onUnhandledInput(basePane, keyStroke, hasBeenHandled);

                }
            }
        });
    }
}
