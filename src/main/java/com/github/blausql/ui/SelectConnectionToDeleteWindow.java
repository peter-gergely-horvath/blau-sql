package com.github.blausql.ui;

import com.github.blausql.TerminalUI;
import com.github.blausql.core.connection.ConnectionDefinition;
import com.github.blausql.core.preferences.ConnectionDefinitionRepository;
import com.github.blausql.ui.util.BackgroundWorker;

import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.dialog.DialogButtons;
import com.googlecode.lanterna.gui.dialog.DialogResult;

final class SelectConnectionToDeleteWindow extends
		SelectConnectionWindow {
	@Override
	protected void onConnectionSelected(
			final ConnectionDefinition cd) {

		
		
		DialogResult dialogResult = TerminalUI.showMessageBox(
				"Confirm deletion of connection", 
				"Delete connection: " + cd.getConnectionName(),
				DialogButtons.OK_CANCEL);

		if(DialogResult.OK.equals(dialogResult)) {
			final Window showWaitDialog = TerminalUI.showWaitDialog("Please wait",
					"Deleting " + cd.getConnectionName() + "... ");
			
			new BackgroundWorker<Void>() {
			
				@Override
				protected Void doBackgroundTask() {
					ConnectionDefinitionRepository.getInstance()
						.deleteConnectionDefinitionByName(cd.getConnectionName());
					
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
					SelectConnectionToDeleteWindow.this.close();
				}
			}.start();
		} else {
			SelectConnectionToDeleteWindow.this.close();
		}
		
		

	}
}