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

    void showErrorMessageFromString(String errorMessage);

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
