package com.github.blausql.ui.components;

import com.github.blausql.TerminalUI;
import com.github.blausql.core.util.ExceptionUtils;
import com.github.blausql.core.util.TextUtils;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Collections;
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
        return terminalUI.getTextGUI();
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
        w.setHints(Collections.singletonList(Window.Hint.CENTERED));
        getApplicationTextGUI().addWindowAndWait(w);
    }

    public void showWindowFullScreen(Window w) {
        w.setHints(Collections.singletonList(Window.Hint.FULL_SCREEN));
        getApplicationTextGUI().addWindowAndWait(w);
    }


    public void showErrorMessageFromThrowable(Throwable throwable) {

        StringBuilder sb = new StringBuilder();

        final Throwable rootCause = ExceptionUtils.getRootCause(throwable);

        if (rootCause instanceof ClassNotFoundException) {
            sb.append("Class not found: ").append(rootCause.getMessage());
        } else if (throwable instanceof SQLException) {
            sb.append(extractMessageFrom(throwable));
        } else {
            String rootCauseMessage = extractMessageFrom(rootCause);
            if (rootCauseMessage != null && !rootCauseMessage.isBlank()) {
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

    public static String extractMessageFrom(Throwable t) {
        StringBuilder sb = new StringBuilder();

        if (t instanceof SQLException) {
            SQLException sqlEx = (SQLException) t;

            String sqlState = sqlEx.getSQLState();
            if (sqlState != null && !sqlState.isBlank()) {
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


    public void showErrorMessageFromString(String errorMessage) {

        showErrorMessageFromString("Error", errorMessage);
    }

    public File showFileSelectorDialog(
            final String title, final String description, final String actionLabel) {

        return new FileDialogBuilder()
                .setTitle(title)
                .setDescription(description)
                .setActionLabel(actionLabel)
                .build()
                .showDialog(getApplicationTextGUI());
    }

    public void showErrorMessageFromString(String dialogTitle, String errorMessage) {
        final int columns = getApplicationTextGUI().getScreen().getTerminalSize().getColumns();
        final int maxLineLen = columns - TerminalUI.LINE_SIZE_DIFF;

        String multilineErrorMsgString = TextUtils.breakLine(errorMessage, maxLineLen);

        showMessageBox(dialogTitle, multilineErrorMsgString);
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

        return messageDialogBuilder.build().showDialog(getApplicationTextGUI());
    }


    protected Window showWaitDialog(String title, String text) {
        return showWaitDialog(title, text, null);
    }

    protected WaitDialog showWaitDialog(String title, String text, Runnable onCancel) {

        WaitDialog w = WaitDialog.showDialog(getApplicationTextGUI(), title, text, onCancel);

        runInEventThread(() -> showWindowCenter(w));

        return w;

    }

    protected void runInEventThread(Runnable runnable) {
        WindowBasedTextGUI textGUI = getApplicationTextGUI();
        TextGUIThread guiThread = textGUI.getGUIThread();
        guiThread.invokeLater(runnable);
    }


}
