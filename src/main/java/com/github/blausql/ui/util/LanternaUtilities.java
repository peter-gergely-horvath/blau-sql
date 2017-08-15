package com.github.blausql.ui.util;


import com.github.blausql.TerminalUI;
import com.googlecode.lanterna.gui.Action;

public class LanternaUtilities {
	
    public static void invokeLater(final Runnable runnable) {
    	TerminalUI.runInEventThread(new Action() {

			public void doAction() {
				runnable.run();
			}
    	});
    }

}
