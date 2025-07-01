package com.github.blausql.ui.util;

import com.github.blausql.TerminalUI;
import com.github.blausql.ui.components.ActionButton;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;

import java.util.Collections;

public abstract class ApplicationWindow extends BasicWindow {

    @FunctionalInterface
    protected interface ExceptionHandledAction {
        void execute() throws Exception;
    }


    protected ApplicationWindow(String title) {
        super(title);
    }


    protected ActionButton button(String text, ExceptionHandledAction action) {
        return new ActionButton(text, withDefaultExceptionHandler(action));
    }

    protected Runnable withDefaultExceptionHandler(ExceptionHandledAction action) {
        return () -> {
            try {
                action.execute();
            } catch (Exception e) {
                TerminalUI.showErrorMessageFromThrowable(e);
            }
        };
    }

    public void showWindowCenter(Window w) {
        w.setHints(Collections.singletonList(Window.Hint.CENTERED));
        getTextGUI().addWindowAndWait(w);
    }

    public void showWindowFullScreen(Window w) {
        w.setHints(Collections.singletonList(Window.Hint.FULL_SCREEN));
        getTextGUI().addWindowAndWait(w);
    }


    protected void showMessageBox(String title, String messageText) {
        showMessageBox(title, messageText, MessageDialogButton.OK);
    }


    protected MessageDialogButton showMessageBox(String title,
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

        return messageDialogBuilder.build().showDialog(getTextGUI());
    }


}
