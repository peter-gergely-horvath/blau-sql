package com.github.blausql.ui.util;


import com.github.blausql.Main;
import com.googlecode.lanterna.gui.Action;

public class LanternaUtilities {
	
    public static void invokeLater(final Runnable runnable) {
    	Main.UI.SCREEN.runInEventThread(new Action() {

			public void doAction() {
				runnable.run();
			}
    	});
    }

}
