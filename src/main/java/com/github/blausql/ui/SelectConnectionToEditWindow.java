package com.github.blausql.ui;

import com.github.blausql.TerminalUI;
import com.github.blausql.core.connection.ConnectionDefinition;

final class SelectConnectionToEditWindow extends SelectConnectionWindow {

	public SelectConnectionToEditWindow() {
		super("Select Connection to Edit");
	}

	@Override
	protected void onConnectionSelected(
			final ConnectionDefinition cd) {
		
		SelectConnectionToEditWindow.this.close();
        TerminalUI.showWindowCenter(
                new ConnectionSettingsWindow(cd, ConnectionSettingsWindow.Mode.EDIT));
	}
}