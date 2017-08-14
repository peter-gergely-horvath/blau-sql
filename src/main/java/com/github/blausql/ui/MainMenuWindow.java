package com.github.blausql.ui;

import com.github.blausql.Main;
import com.github.blausql.core.preferences.ConnectionDefinitionRepository;
import org.springframework.util.Assert;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.listener.WindowAdapter;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.input.Key.Kind;

public class MainMenuWindow extends Window {

	public MainMenuWindow() {

		super("BLAU SQL UNIVERSAL DATABASE CLIENT");

		addWindowListener(new HotKeyWindowListener());

		if (ConnectionDefinitionRepository.getInstance()
				.getConnectionDefinitions().size() != 0) {
			addComponent(new Button("[C]onnect to database", new Action() {

				public void doAction() {
					onConnectToDatabaseButtonSelected();
				}

			}));
		}

		addComponent(new Button("[M]anage Connections", new Action() {

			public void doAction() {
				onManageConnectionsButtonSelected();
			}

		}));

		addComponent(new Button("[E]xit Application", new Action() {

			public void doAction() {
				onExitApplicationButtonSelected();

			}
		}));
	}

	private void onConnectToDatabaseButtonSelected() {
		Main.UI.showWindowCenter(new SelectConnectionForQueryWindow());
	}

	private void onManageConnectionsButtonSelected() {
		Main.UI.showWindowCenter(new ManageConnectionsWindow());
	}

	private void onExitApplicationButtonSelected() {
		Main.UI.SCREEN.getScreen().stopScreen();
		System.exit(0);
	}

	private final class HotKeyWindowListener extends WindowAdapter {
		@Override
		public void onUnhandledKeyboardInteraction(Window window, Key key) {

			final Kind keyKind = key.getKind();

			Assert.notNull(keyKind, "kind retrieved from key is null");

			switch (keyKind) {

			case NormalKey:
				final char characterKey = key.getCharacter();

				switch (characterKey) {
				case 'C':
				case 'c':
					onConnectToDatabaseButtonSelected();
					break;

				case 'M':
				case 'm':
					onManageConnectionsButtonSelected();
					break;

				case 'E':
				case 'e':
					onExitApplicationButtonSelected();
				}

				break; // outer switch

			case Escape:
				onExitApplicationButtonSelected();
				break;

			default:
				// nothing to do
				break;
			}
		}
	}
}