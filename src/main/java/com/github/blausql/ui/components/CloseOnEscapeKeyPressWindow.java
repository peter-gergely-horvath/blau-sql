package com.github.blausql.ui.components;

import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.listener.WindowAdapter;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.input.Key.Kind;

public class CloseOnEscapeKeyPressWindow extends Window  {

	public CloseOnEscapeKeyPressWindow(String title) {
		super(title);
		
		addWindowListener(new WindowAdapter() {
			
			@Override
			public void onUnhandledKeyboardInteraction(
					Window window, Key key) {
				
				if(Kind.Escape.equals(key.getKind())) {
					CloseOnEscapeKeyPressWindow.this.close();
				}
			}
			
		});
		
		
		
		
	}

}
