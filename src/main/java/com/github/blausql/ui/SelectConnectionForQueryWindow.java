package com.github.blausql.ui;

import com.github.blausql.TerminalUI;
import com.github.blausql.core.connection.ConnectionDefinition;
import com.github.blausql.core.connection.Database;
import com.github.blausql.ui.util.BackgroundWorker;

import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.dialog.DialogResult;

final class SelectConnectionForQueryWindow extends SelectConnectionWindow {
	@Override
	protected void onConnectionSelected(
			ConnectionDefinition connectionDefinition) {

		this.close();

		if (!connectionDefinition.getLoginAutomatically()) {

			CredentialsDialog credentialsDialog = 
					new CredentialsDialog(connectionDefinition);

			TerminalUI.showWindowCenter(credentialsDialog);
			
			if(DialogResult.CANCEL ==
					credentialsDialog.getDialogResult()) {
				
				return;
			}

			ConnectionDefinition actualConnectionDefinition = 
					new ConnectionDefinition(connectionDefinition);

			actualConnectionDefinition.setUserName(
					credentialsDialog.getUserName());
			actualConnectionDefinition.setPassword(
					credentialsDialog.getPassword());
			
			connectionDefinition = actualConnectionDefinition;
		}

		establishConnection(connectionDefinition);
	}

	protected void establishConnection(
			final ConnectionDefinition connectionDefinition) {
		final Window showWaitDialog = TerminalUI.showWaitDialog("Please wait",
				"Connecting to " + connectionDefinition.getConnectionName()
						+ "... ");

		new BackgroundWorker<Void>() {

			@Override
			protected Void doBackgroundTask() {
				Database.getInstance()
						.establishConnection(connectionDefinition);
				return null;
			}

			@Override
			protected void onBackgroundTaskFailed(Throwable t) {
				showWaitDialog.close();
				TerminalUI.showErrorMessageFromThrowable(t);

			}

			@Override
			protected void onBackgroundTaskCompleted(Void result) {
				showWaitDialog.close();
				TerminalUI.showWindowFullScreen(new SqlCommandWindow(connectionDefinition));

			}
		}.start();
	}
}