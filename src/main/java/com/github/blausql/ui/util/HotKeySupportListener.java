package com.github.blausql.ui.util;

import java.util.Map;

import org.springframework.util.Assert;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.listener.WindowAdapter;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.input.Key.Kind;

public class HotKeySupportListener extends WindowAdapter {
	
	private final Map<Character, Action> hotkeyToActionMap;
	private final boolean closeOnEscape;
	
	public HotKeySupportListener(Map<Character, Action> hotkeyToActionMap, boolean closeOnEsc) {
		 this.hotkeyToActionMap = hotkeyToActionMap;
		 this.closeOnEscape = closeOnEsc;
	}
	
	
	@Override
	public void onUnhandledKeyboardInteraction(Window window, Key key) {

		final Kind keyKind = key.getKind();

		Assert.notNull(keyKind, "kind retrieved from key is null");

		switch (keyKind) {

		case NormalKey:
			final char characterKey = key.getCharacter();
			
			Action action;
			action = hotkeyToActionMap.get(Character.toUpperCase(characterKey));
			if(action == null) {
				action = hotkeyToActionMap.get(Character.toLowerCase(characterKey));
			}
			
			if(action != null) {
				action.doAction();
			}
			
			break;
			
		case Escape:
			if(closeOnEscape) {
				window.close();
			}
			
			break;

		default:
			// nothing to do
			break;
		}
	}

}
