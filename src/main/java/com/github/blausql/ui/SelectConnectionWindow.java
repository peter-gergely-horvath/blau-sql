package com.github.blausql.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.blausql.core.connection.ConnectionDefinition;
import com.github.blausql.core.preferences.ConnectionDefinitionRepository;
import com.github.blausql.ui.components.CloseOnEscapeKeyPressWindow;

import com.google.common.collect.ImmutableList;
import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.listener.WindowAdapter;
import com.googlecode.lanterna.input.Key;
import org.springframework.util.Assert;

public abstract class SelectConnectionWindow extends CloseOnEscapeKeyPressWindow {

    private static final ImmutableList<Character> HOTKEY_CHARACTERS = ImmutableList.<Character>builder().add(
            '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    ).build();


    private final Map<Character, ConnectionDefinition> hotKeyMap = new ConcurrentHashMap<>();

	public SelectConnectionWindow(String title) {
		super(title);

		addWindowListener(new HotKeyWindowListener());

		addComponent(new Button("CANCEL (ESC)", new Action() {

			public void doAction() {
				SelectConnectionWindow.this.close();
			}
		}));
		
		
		List<ConnectionDefinition> connectionDefinitions = ConnectionDefinitionRepository.getInstance().getConnectionDefinitions();

		int index = 0;

		for(final ConnectionDefinition connectionDefinition : connectionDefinitions) {
			String connectionName;

			if (index < HOTKEY_CHARACTERS.size()) {
                Character hotkeyChar = HOTKEY_CHARACTERS.get(index++);

				connectionName = String.format("[%s] %s", hotkeyChar, connectionDefinition.getConnectionName());

                hotKeyMap.put(hotkeyChar, connectionDefinition);

			} else {
				connectionName = connectionDefinition.getConnectionName();
			}

			addComponent(new Button(connectionName, new Action() {

				public void doAction() {
					SelectConnectionWindow.this.onConnectionSelected(connectionDefinition);
				}
			}));
		}
		
	}

	private final class HotKeyWindowListener extends WindowAdapter {
		@Override
		public void onUnhandledKeyboardInteraction(Window window, Key key) {

			final Key.Kind keyKind = key.getKind();

			Assert.notNull(keyKind, "kind retrieved from key is null");

            if(keyKind == Key.Kind.NormalKey) {
                final char characterKey = key.getCharacter();
                Character mapKey = Character.valueOf(characterKey);

                ConnectionDefinition connectionDefinition = hotKeyMap.get(mapKey);
                if (connectionDefinition != null) {
                    SelectConnectionWindow.this.onConnectionSelected(connectionDefinition);
                }
            }
		}
	}

	protected abstract void onConnectionSelected(
			ConnectionDefinition connectionDefinition);

}
