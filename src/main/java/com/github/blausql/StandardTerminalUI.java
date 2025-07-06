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

import com.github.blausql.core.util.TextUtils;
import com.github.blausql.ui.DisplayThrowableDialog;
import com.github.blausql.ui.components.WaitDialog;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.TextGUIThread;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

public final class StandardTerminalUI implements TerminalUI {

    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    private final Screen screen;
    private final WindowBasedTextGUI windowBasedTextGUI;

    public StandardTerminalUI() {
        try {
            Terminal terminal = new DefaultTerminalFactory().createTerminal();
            screen = new TerminalScreen(terminal);
            screen.startScreen();

            windowBasedTextGUI = new MultiWindowTextGUI(screen);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void showWindowCenter(Window w) {
        w.setHints(Collections.singletonList(Window.Hint.CENTERED));
        windowBasedTextGUI.addWindowAndWait(w);
    }


    @Override
    public void showWindowFullScreen(Window w) {
        w.setHints(Collections.singletonList(Window.Hint.FULL_SCREEN));
        windowBasedTextGUI.addWindowAndWait(w);
    }


    @Override
    public void showErrorMessageFromThrowable(Throwable throwable) {

        DisplayThrowableDialog dialog = new DisplayThrowableDialog(
                "Error occurred", "Could not connect", throwable, this);

        showWindowCenter(dialog);
    }

    @Override
    public void showErrorMessageFromString(String errorMessage) {

        showErrorMessageFromString("Error", errorMessage);
    }

    @Override
    public File showFileSelectorDialog(
            final String title, final String description, final String actionLabel) {

        return new FileDialogBuilder()
                .setTitle(title)
                .setDescription(description)
                .setActionLabel(actionLabel)
                .build()
                .showDialog(windowBasedTextGUI);
    }

    @Override
    public void showErrorMessageFromString(String dialogTitle, String errorMessage) {
        final int columns = windowBasedTextGUI.getScreen().getTerminalSize().getColumns();
        final int maxLineLen = columns - LINE_SIZE_DIFF;

        String multilineErrorMsgString = TextUtils.breakLine(errorMessage, maxLineLen);

        showMessageBox(dialogTitle, multilineErrorMsgString);
    }

    @Override
    public void showMessageBox(String title, String messageText) {
        showMessageBox(title, messageText, MessageDialogButton.OK);
    }


    @Override
    public MessageDialogButton showMessageBox(String title,
                                              String messageText,
                                              MessageDialogButton firstButton,
                                              MessageDialogButton... additionalButtons) {

        MessageDialogBuilder messageDialogBuilder = new MessageDialogBuilder()
                .setTitle(title)
                .setText(messageText);

        messageDialogBuilder
                .addButton(firstButton);

        for (MessageDialogButton messageDialogButton : additionalButtons) {
            messageDialogBuilder
                    .addButton(messageDialogButton);
        }

        return messageDialogBuilder.build().showDialog(windowBasedTextGUI);
    }


    @Override
    public Window showWaitDialog(String title, String text) {
        return showWaitDialog(title, text, null);
    }

    @Override
    public WaitDialog showWaitDialog(String title, String text, Runnable onCancel) {

        WaitDialog w = WaitDialog.showDialog(windowBasedTextGUI, title, text, onCancel);

        runInGUIThread(() -> showWindowCenter(w));

        return w;

    }

    @Override
    public void runInGUIThread(Runnable runnable) {
        TextGUIThread guiThread = windowBasedTextGUI.getGUIThread();

        if (guiThread == null) {
            // should never occur
            throw new IllegalStateException("guiThread is null");
        }

        guiThread.invokeLater(runnable);
    }

    public void close() throws IOException {

        if (isClosed.get()) {
            throw new IllegalStateException("closed already");
        }

        try {

            screen.stopScreen();

        } finally {
            isClosed.set(true);
        }
    }

    @Override
    public WindowBasedTextGUI getWindowBasedTextGUI() {
        return windowBasedTextGUI;
    }
}
