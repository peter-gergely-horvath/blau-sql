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

 
package com.github.blausql.ui.sql;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

final class SqlEditorTextBox extends TextBox {

    private static final int TAB_INDENT = 4;

    SqlEditorTextBox(TerminalSize preferredSize, String initialContent) {
        super(preferredSize, initialContent);
    }

    @Override
    public synchronized Result handleKeyStroke(KeyStroke keyStroke) {

        if (keyStroke.getKeyType() == KeyType.Tab) {

            Result result = null;

            for (int i = 0; i < TAB_INDENT; i++) {
                result = super.handleKeyStroke(new KeyStroke(' ', false, false));
            }

            return result;

        } else {
            return super.handleKeyStroke(keyStroke);
        }
    }


}
