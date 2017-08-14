package com.github.blausql.ui;

import com.github.blausql.Main;
import com.github.blausql.core.connection.ConnectionDefinition;

final class SelectConnectionToEditWindow extends
		SelectConnectionWindow {
	@Override
	protected void onConnectionSelected(
			final ConnectionDefinition cd) {
		
		SelectConnectionToEditWindow.this.close();
		Main.UI.showWindowCenter(
				ConnectionSettingsWindow.createForExistingConnectionDefinition(cd));
	}
}