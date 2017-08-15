package com.github.blausql.ui;

import com.github.blausql.TerminalUI;
import com.github.blausql.core.connection.ConnectionDefinition;

final class SelectConnectionToCopyWindow extends
		SelectConnectionWindow {

    public SelectConnectionToCopyWindow() {
        super("Select Connection to Copy");
    }

    @Override
	protected void onConnectionSelected(
			final ConnectionDefinition cd) {

        ConnectionDefinition copyOfConnectionDefinition = new ConnectionDefinition(cd);

        String connectionName = copyOfConnectionDefinition.getConnectionName();

        copyOfConnectionDefinition.setConnectionName("Copy of " + connectionName);

        SelectConnectionToCopyWindow.this.close();
        TerminalUI.showWindowCenter(
                new ConnectionSettingsWindow(copyOfConnectionDefinition, ConnectionSettingsWindow.Mode.COPY));
	}
}