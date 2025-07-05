package com.github.blausql.ui.components;

import com.github.blausql.TerminalUI;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;

import java.io.File;
import java.util.Objects;

public abstract class ApplicationWindow extends BasicWindow {

    @FunctionalInterface
    protected interface ExceptionHandledAction {
        void execute() throws Exception;
    }


    private final TerminalUI terminalUI;

    protected ApplicationWindow(String title, TerminalUI terminalUI) {
        super(title);

        Objects.requireNonNull(terminalUI, "argument terminalUI can not be null");

        this.terminalUI = terminalUI;
    }

    public final WindowBasedTextGUI getApplicationTextGUI() {
        return terminalUI.getWindowBasedTextGUI();
    }

    protected final TerminalUI getTerminalUI() {
        return terminalUI;
    }


    protected final ActionButton button(String text, ExceptionHandledAction action) {
        return new ActionButton(text, withDefaultExceptionHandler(action));
    }

    protected final Runnable withDefaultExceptionHandler(ExceptionHandledAction action) {
        return () -> {
            try {
                action.execute();
            } catch (Exception e) {
                showErrorMessageFromThrowable(e);
            }
        };
    }

    protected final void showWindowCenter(Window w) {
        terminalUI.showWindowCenter(w);
    }

    protected final void showWindowFullScreen(Window w) {
        terminalUI.showWindowFullScreen(w);
    }


    protected final void showErrorMessageFromThrowable(Throwable throwable) {

        terminalUI.showErrorMessageFromThrowable(throwable);
    }


    protected final void showErrorMessageFromString(String errorMessage) {

        terminalUI.showErrorMessageFromString("Error", errorMessage);
    }

    protected final File showFileSelectorDialog(
            final String title, final String description, final String actionLabel) {

        return terminalUI.showFileSelectorDialog(title, description, actionLabel);
    }

    protected final void showErrorMessageFromString(String dialogTitle, String errorMessage) {
        terminalUI.showErrorMessageFromString(dialogTitle, errorMessage);
    }

    protected final void showMessageBox(String title, String messageText) {
        terminalUI.showMessageBox(title, messageText);
    }


    protected final MessageDialogButton showMessageBox(String title,
                                                 String messageText,
                                                 MessageDialogButton firstButton,
                                                 MessageDialogButton... additionalButtons) {

        return terminalUI.showMessageBox(title, messageText, firstButton, additionalButtons);
    }


    protected final Window showWaitDialog(String title, String text) {
        return terminalUI.showWaitDialog(title, text);
    }

    protected final WaitDialog showWaitDialog(String title, String text, Runnable onCancel) {

        return terminalUI.showWaitDialog(title, text, onCancel);
    }

    protected final void runInEventThread(Runnable runnable) {

        terminalUI.runInGUIThread(runnable);
    }
}
