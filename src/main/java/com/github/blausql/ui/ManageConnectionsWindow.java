package com.github.blausql.ui;

import com.github.blausql.Main;
import com.github.blausql.ui.components.CloseOnEscapeKeyPressWindow;
import com.github.blausql.ui.util.HotKeySupportListener;

import com.google.common.collect.ImmutableMap;
import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.component.Button;

public class ManageConnectionsWindow extends CloseOnEscapeKeyPressWindow {


	public ManageConnectionsWindow() {
		
		super("Manage Connections");

		addComponent(new Button("BACK (ESC)", new Action() {

			public void doAction() {
				ManageConnectionsWindow.this.close();
			}
		}));
		addComponent(new Button("[A]dd connection", onAddConnectionButtonSelectedAction));
		addComponent(new Button("[E]dit connection", onEditConnectionButtonSelectedAction));
		addComponent(new Button("[D]elete connection", onDeleteConnectionButtonSelectedAction));
		
		addWindowListener(new HotKeySupportListener(
				ImmutableMap.<Character, Action>builder()
				.put('A', onAddConnectionButtonSelectedAction)
				.put('E', onEditConnectionButtonSelectedAction)
				.put('D', onDeleteConnectionButtonSelectedAction)
				.build(), true));
	}
	
	
	private final Action onAddConnectionButtonSelectedAction = new Action() {

		public void doAction() {
			ManageConnectionsWindow.this.close();
			Main.UI.showWindowCenter(ConnectionSettingsWindow.createForNewConnectionDefinition());
		}
	};
	private final Action onEditConnectionButtonSelectedAction = new Action() {

		public void doAction() {
			ManageConnectionsWindow.this.close();
			Main.UI.showWindowCenter(new SelectConnectionToEditWindow());
		}
	};
	
	private final Action onDeleteConnectionButtonSelectedAction = new Action() {

		public void doAction() {
			ManageConnectionsWindow.this.close();
			Main.UI.showWindowCenter(new SelectConnectionToDeleteWindow());
		}
	};	
}