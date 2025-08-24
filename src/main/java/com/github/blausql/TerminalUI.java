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

 
package com.github.blausql;

import com.github.blausql.ui.components.WaitDialog;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;

import java.io.File;

public interface TerminalUI extends AutoCloseable {

    int LINE_SIZE_DIFF = 8;

    void showWindowCenter(Window w);

    void showWindowFullScreen(Window w);

    void showErrorMessageFromThrowable(Throwable throwable);

    File showFileSelectorDialog(
            String title, String description, String actionLabel);

    void showErrorMessageFromString(String dialogTitle, String errorMessage);

    void showMessageBox(String title, String messageText);

    MessageDialogButton showMessageBox(String title,
                                       String messageText,
                                       MessageDialogButton firstButton,
                                       MessageDialogButton... additionalButtons);

    Window showWaitDialog(String title, String text);

    WaitDialog showWaitDialog(String title, String text, Runnable onCancel);

    void runInGUIThread(Runnable runnable);

    WindowBasedTextGUI getWindowBasedTextGUI();
}
