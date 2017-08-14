package com.github.blausql;

import java.sql.SQLException;

import com.github.blausql.ui.MainMenuWindow;
import com.github.blausql.util.TextUtils;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.googlecode.lanterna.TerminalFacade;
import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.dialog.DialogButtons;
import com.googlecode.lanterna.gui.dialog.DialogResult;
import com.googlecode.lanterna.gui.dialog.MessageBox;
import com.googlecode.lanterna.gui.dialog.WaitingDialog;

public class Main {

	public static void main(String[] args) {

		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable e) {
				UI.SCREEN.getScreen().stopScreen();

				System.err.format("--- UNHANDLED EXCEPTION in Thread '%s': exiting the JVM! --- %n", t.getName());

				e.printStackTrace();

				System.exit(1);
			}
		});

		UI.init();

		UI.showWindowCenter(new MainMenuWindow());
	}

	public static class UI {

		public static final GUIScreen SCREEN = TerminalFacade.createGUIScreen();

		private static void init() {
			UI.SCREEN.getScreen().startScreen();
		}

		public static void showErrorMessageFromThrowable(Throwable throwable) {
			throwable.printStackTrace(); // ensure exception appears at least on
											// console
			StringBuilder sb = new StringBuilder();

			final Throwable rootCause = Throwables.getRootCause(throwable);
			
			if(rootCause instanceof ClassNotFoundException) {
				sb.append(rootCause.toString());
			} else if (rootCause instanceof SQLException) {
				sb.append(extractMessageFrom(rootCause));
			} else if (throwable instanceof SQLException) {
				sb.append(extractMessageFrom(throwable));
			} else {
				String rootCauseMessage = extractMessageFrom(rootCause);
				if (!Strings.isNullOrEmpty(rootCauseMessage)) {
					sb.append(rootCauseMessage);
				} else {
					Throwable t = throwable;
					while (t != null) {
						sb.append(": ").append(extractMessageFrom(t));
						t = t.getCause();
					}
				}
			}

			String theString = sb.toString();

			final int columns = Main.UI.SCREEN.getScreen().getTerminalSize()
					.getColumns();
			final int maxLineLen = columns - 8;

			String multilineErrorMsgString = TextUtils.breakLine(theString,
					maxLineLen);

			MessageBox.showMessageBox(Main.UI.SCREEN, "Error",
					multilineErrorMsgString);

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

			MessageBox.showMessageBox(Main.UI.SCREEN, title, messageText);

		}
		
		public static DialogResult showMessageBox(String title,
	            String message, DialogButtons buttons) {

			return MessageBox.showMessageBox(Main.UI.SCREEN, title, message, buttons);

		}

		public static void showWindowCenter(Window w) {
			Main.UI.SCREEN.showWindow(w, GUIScreen.Position.CENTER);
		}

		public static void showWindowFullScreen(Window w) {
			Main.UI.SCREEN.showWindow(w, GUIScreen.Position.FULL_SCREEN);

		}

		public static Window showWaitDialog(String title, String text) {

			final Window w = new WaitingDialog(title, text);

			Main.UI.SCREEN.runInEventThread(new Action() {

				public void doAction() {
					UI.showWindowCenter(w);
				}
			});

			return w;

		}

	}

}
