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

 
package com.github.blausql;

import com.github.blausql.core.util.TextUtils;
import com.github.blausql.ui.components.WaitDialog;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.googlecode.lanterna.TerminalFacade;
import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.dialog.DialogButtons;
import com.googlecode.lanterna.gui.dialog.DialogResult;
import com.googlecode.lanterna.gui.dialog.MessageBox;
import com.googlecode.lanterna.gui.dialog.TextInputDialog;
import com.googlecode.lanterna.terminal.TerminalSize;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;

//CHECKSTYLE.OFF: FinalClass: must be extensible for the testing frameworks
public class TerminalUI {

    private static final int LINE_SIZE_DIFF = 8;

    private TerminalUI() {
        // no instances
    }

    private static GUIScreen getGUIScreen() {
        return LazyHolder.INSTANCE;
    }

    private static class LazyHolder {
        static final GUIScreen INSTANCE = TerminalFacade.createGUIScreen();
    }

    public static void init() {
        getGUIScreen().getScreen().startScreen();
    }

    static void close() {
        getGUIScreen().getScreen().stopScreen();
    }

    public static void showErrorMessageFromThrowable(Throwable throwable) {

        StringBuilder sb = new StringBuilder();

        final Throwable rootCause = Throwables.getRootCause(throwable);

        if (rootCause instanceof ClassNotFoundException) {
            sb.append("Class not found: ").append(rootCause.getMessage());
        } else if (throwable instanceof SQLException) {
            sb.append(extractMessageFrom(throwable));
        } else {
            String rootCauseMessage = extractMessageFrom(rootCause);
            if (!Strings.isNullOrEmpty(rootCauseMessage)) {
                sb.append(rootCauseMessage);
            } else {
                Throwable t = throwable;
                while (t != null) {
                    String extractedMessage = extractMessageFrom(t);
                    if (!extractedMessage.isEmpty()) {
                        if (sb.length() > 0) {
                            sb.append(": ");
                        }
                        sb.append(extractedMessage);
                    }
                    t = t.getCause();
                }

                StringWriter stringWriter = new StringWriter();
                try (PrintWriter pw = new PrintWriter(stringWriter)) {
                    rootCause.printStackTrace(pw);
                }
                String fullStackTrace = stringWriter.toString();
                sb.append(fullStackTrace);
            }
        }

        String theString = sb.toString();

        showErrorMessageFromString(theString);

    }

    public static void showErrorMessageFromString(String errorMessage) {

        showErrorMessageFromString("Error", errorMessage);
    }

    public static void showErrorMessageFromString(String dialogTitle, String errorMessage) {
        final int columns = getGUIScreen().getScreen().getTerminalSize().getColumns();
        final int maxLineLen = columns - LINE_SIZE_DIFF;

        String multilineErrorMsgString = TextUtils.breakLine(errorMessage, maxLineLen);


        MessageBox.showMessageBox(getGUIScreen(), dialogTitle, multilineErrorMsgString);
    }

    public static String extractMessageFrom(Throwable t) {
        StringBuilder sb = new StringBuilder();

        if (t instanceof SQLException) {
            SQLException sqlEx = (SQLException) t;

            String sqlState = sqlEx.getSQLState();
            if (!Strings.isNullOrEmpty(sqlState)) {
                sb.append("SQLState: ").append(sqlState)
                        .append(TextUtils.LINE_SEPARATOR);
            }

            int errorCode = sqlEx.getErrorCode();
            sb.append("Error Code: ").append(errorCode)
                    .append(TextUtils.LINE_SEPARATOR);
        }
        String localizedMessage = t.getLocalizedMessage();
        String message = t.getMessage();

        if (localizedMessage != null && !"".equals(localizedMessage)) {

            sb.append(localizedMessage);

        } else if (message != null && !"".equals(message)) {
            sb.append(message);
        }

        String throwableAsString = sb.toString();

        return throwableAsString.trim();
    }

    public static void showMessageBox(String title, String messageText) {

        MessageBox.showMessageBox(getGUIScreen(), title, messageText);

    }

    public static DialogResult showMessageBox(String title,
                                              String message, DialogButtons buttons) {

        return MessageBox.showMessageBox(getGUIScreen(), title, message, buttons);

    }

    public static String showTextInputDialog(
            final String title, final String description, final String initialText, final int textBoxWidth) {

        return TextInputDialog.showTextInputBox(getGUIScreen(), title, description, initialText, textBoxWidth);

    }

    public static void showWindowCenter(Window w) {
        getGUIScreen().showWindow(w, GUIScreen.Position.CENTER);
    }

    public static void showWindowFullScreen(Window w) {
        getGUIScreen().showWindow(w, GUIScreen.Position.FULL_SCREEN);

    }

    public static Window showWaitDialog(String title, String text) {
        return showWaitDialog(title, text, null);
    }

    public static Window showWaitDialog(String title, String text, Action onCancel) {

        final Window w = new WaitDialog(title, text, onCancel);

        getGUIScreen().runInEventThread(new Action() {

            public void doAction() {
                showWindowCenter(w);
            }
        });

        return w;

    }

    public static void runInEventThread(Action action) {
        getGUIScreen().runInEventThread(action);
    }

    public static TerminalSize getTerminalSize() {
        return getGUIScreen().getScreen().getTerminalSize();
    }
}
//CHECKSTYLE.ON
