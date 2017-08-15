package com.github.blausql.ui;

import com.github.blausql.TerminalUI;
import com.github.blausql.core.connection.ConnectionDefinition;

final class SelectConnectionToEditWindow extends
		SelectConnectionWindow {
	@Override
	protected void onConnectionSelected(
			final ConnectionDefinition cd) {
		
		SelectConnectionToEditWindow.this.close();
		TerminalUI.showWindowCenter(
				ConnectionSettingsWindow.createForExistingConnectionDefinition(cd));
	}
}