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
import com.github.blausql.ui.components.ApplicationWindow;
import com.github.blausql.ui.hotkey.HotKeyWindowListener;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.input.KeyType;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;


public class HelpWindow extends ApplicationWindow {

    public HelpWindow(Class<? extends ApplicationWindow> windowClass, TerminalUI terminalUI) {
        super("Help (press ESC to close)", terminalUI);

        addWindowListener(HotKeyWindowListener.builder()
                .keyType(KeyType.Escape).invoke(this::close)
                .build());

        TextBox helpTextBox = new TextBox(getDesiredSizeForHelpTextBox(), "");
        helpTextBox.setReadOnly(true);

        helpTextBox.setText(getHelpStringOf(windowClass));

        setComponent(helpTextBox);
    }

    private static String getHelpStringOf(Class<? extends ApplicationWindow> windowClass) {

        String simpleName = windowClass.getSimpleName();

        InputStream resourceAsStream = windowClass.getResourceAsStream(String.format("%s.help.txt", simpleName));
        if (resourceAsStream == null) {
            return "No help is available for: " + windowClass;
        } else {
            return new Scanner(resourceAsStream, StandardCharsets.UTF_8).useDelimiter("\\A").next();
        }
    }

    private TerminalSize getDesiredSizeForHelpTextBox() {

        TerminalSize screenTerminalSize = getApplicationTextGUI().getScreen().getTerminalSize();

        return getDesiredSizeForHelpTextBox(screenTerminalSize);
    }

    private static TerminalSize getDesiredSizeForHelpTextBox(TerminalSize screenTerminalSize) {
        final int sqlQueryTextBoxColumns = screenTerminalSize.getColumns() - 4;
        final int sqlQueryTextBoxRows = screenTerminalSize.getRows() - 2;

        return new TerminalSize(sqlQueryTextBoxColumns, sqlQueryTextBoxRows);
    }
}
