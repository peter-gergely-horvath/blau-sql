package com.github.blausql.ui;

import java.util.List;

import com.github.blausql.core.connection.ConnectionDefinition;
import com.github.blausql.core.preferences.ConnectionDefinitionRepository;
import com.github.blausql.ui.components.CloseOnEscapeKeyPressWindow;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.component.Button;

public abstract class SelectConnectionWindow extends CloseOnEscapeKeyPressWindow {

	public SelectConnectionWindow() {
		super("Select connection");
		
		addComponent(new Button("BACK (ESC)", new Action() {

			public void doAction() {
				SelectConnectionWindow.this.close();
			}
		}));
		
		
		List<ConnectionDefinition> connectionDefinitions = ConnectionDefinitionRepository.getInstance().getConnectionDefinitions();
		
		for(final ConnectionDefinition connectionDefinition : connectionDefinitions) {
			addComponent(new Button(connectionDefinition.getConnectionName(), new Action() {

				public void doAction() {
					SelectConnectionWindow.this.onConnectionSelected(connectionDefinition);
				}
			}));
		}
		
	}

	protected abstract void onConnectionSelected(
			ConnectionDefinition connectionDefinition);

}
