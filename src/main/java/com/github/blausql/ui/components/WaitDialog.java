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

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;


/**
 * Inspired by com.googlecode.lanterna.gui.dialog.WaitingDialog,
 * adding the ability to cancel
 */
public final class WaitDialog extends DialogWindow {


    private final class CloseWaitDialogAndInvokeRunnable implements Runnable {

        private final Runnable delegate;

        private CloseWaitDialogAndInvokeRunnable(Runnable delegate) {
            this.delegate = delegate;
        }

        @Override
        public void run() {
            WaitDialog.this.close();
            delegate.run();
        }
    }


    private WaitDialog(String title, String text, Runnable runnable) {
        super(title);

        Panel topPanel = Panels.horizontal(
                new Label(text),
                new EmptySpace(new TerminalSize(2, 1)),
                AnimatedLabel.createClassicSpinningLine(),
                new EmptySpace(new TerminalSize(2, 1)));

        Button cancelButton = new Button("Cancel", new CloseWaitDialogAndInvokeRunnable(runnable));

        Panel mainPanel = Panels.vertical(topPanel, new EmptySpace(), cancelButton);

        setComponent(mainPanel);
    }

    @Override
    public Object showDialog(WindowBasedTextGUI textGUI) {
        showDialog(textGUI, true);
        return null;
    }

    /**
     * Displays the waiting dialog and optionally blocks until another thread closes it
     *
     * @param textGUI          GUI to add the dialog to
     * @param blockUntilClosed If {@code true}, the method call will block until another thread calls {@code close()} on
     *                         the dialog, otherwise the method call returns immediately
     */
    public void showDialog(WindowBasedTextGUI textGUI, boolean blockUntilClosed) {
        textGUI.addWindow(this);

        if (blockUntilClosed) {
            //Wait for the window to close, in case the window manager doesn't honor the MODAL hint
            waitUntilClosed();
        }
    }

    /**
     * Creates a new waiting dialog
     *
     * @param title Title of the waiting dialog
     * @param text  Text to display on the waiting dialog
     * @return Created waiting dialog
     */
    public static WaitDialog createDialog(String title, String text, Runnable onCancelRunnable) {
        return new WaitDialog(title, text, onCancelRunnable);
    }

    /**
     * Creates and displays a waiting dialog without blocking for it to finish
     *
     * @param textGUI GUI to add the dialog to
     * @param title   Title of the waiting dialog
     * @param text    Text to display on the waiting dialog
     * @return Created waiting dialog
     */
    public static WaitDialog showDialog(WindowBasedTextGUI textGUI,
                                        String title,
                                        String text,
                                        Runnable onCancelRunnable) {

        WaitDialog waitingDialog = createDialog(title, text, onCancelRunnable);
        waitingDialog.showDialog(textGUI, false);
        return waitingDialog;
    }
}
