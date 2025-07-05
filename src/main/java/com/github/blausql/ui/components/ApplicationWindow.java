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


    protected final TerminalUI terminalUI;

    protected ApplicationWindow(String title, TerminalUI terminalUI) {
        super(title);

        Objects.requireNonNull(terminalUI, "argument terminalUI can not be null");

        this.terminalUI = terminalUI;
    }

    public final WindowBasedTextGUI getApplicationTextGUI() {
        return terminalUI.getWindowBasedTextGUI();
    }

    protected TerminalUI getTerminalUI() {
        return terminalUI;
    }


    protected ActionButton button(String text, ExceptionHandledAction action) {
        return new ActionButton(text, withDefaultExceptionHandler(action));
    }

    protected Runnable withDefaultExceptionHandler(ExceptionHandledAction action) {
        return () -> {
            try {
                action.execute();
            } catch (Exception e) {
                showErrorMessageFromThrowable(e);
            }
        };
    }

    public void showWindowCenter(Window w) {
        terminalUI.showWindowCenter(w);
    }

    public void showWindowFullScreen(Window w) {
        terminalUI.showWindowFullScreen(w);
    }


    public void showErrorMessageFromThrowable(Throwable throwable) {

        terminalUI.showErrorMessageFromThrowable(throwable);
    }


    public void showErrorMessageFromString(String errorMessage) {

        terminalUI.showErrorMessageFromString("Error", errorMessage);
    }

    public File showFileSelectorDialog(
            final String title, final String description, final String actionLabel) {

        return terminalUI.showFileSelectorDialog(title, description, actionLabel);
    }

    public void showErrorMessageFromString(String dialogTitle, String errorMessage) {
        terminalUI.showErrorMessageFromString(dialogTitle, errorMessage);
    }

    protected void showMessageBox(String title, String messageText) {
        terminalUI.showMessageBox(title, messageText);
    }


    protected MessageDialogButton showMessageBox(String title,
                                                 String messageText,
                                                 MessageDialogButton firstButton,
                                                 MessageDialogButton... additionalButtons) {

        return terminalUI.showMessageBox(title, messageText, firstButton, additionalButtons);
    }


    protected Window showWaitDialog(String title, String text) {
        return terminalUI.showWaitDialog(title, text);
    }

    protected WaitDialog showWaitDialog(String title, String text, Runnable onCancel) {

        return terminalUI.showWaitDialog(title, text, onCancel);
    }

    protected void runInEventThread(Runnable runnable) {

        terminalUI.runInGUIThread(runnable);
    }
}
