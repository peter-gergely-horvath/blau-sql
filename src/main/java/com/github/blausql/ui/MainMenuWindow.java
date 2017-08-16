package com.github.blausql.ui;


import com.github.blausql.Main;
import com.github.blausql.TerminalUI;
import com.github.blausql.core.Constants;
import com.github.blausql.ui.util.HotKeySupportListener;
import com.google.common.collect.ImmutableMap;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.component.Button;

public class MainMenuWindow extends Window {

    public MainMenuWindow() {

        super("BLAU SQL UNIVERSAL DATABASE CLIENT");

        addComponent(new Button("[C]onnect to database", onConnectToDatabaseButtonSelectedAction));
        addComponent(new Button("[M]anage Connections", onManageConnectionButtonSelectedAction));
        addComponent(new Button("[A]bout", onAboutButtonSelectedAction));
        addComponent(new Button("[E]xit Application", onExitApplicationButtonSelected));

        addWindowListener(new HotKeySupportListener(
                ImmutableMap.<Character, Action>builder()
                        .put('C', onConnectToDatabaseButtonSelectedAction)
                        .put('M', onManageConnectionButtonSelectedAction)
                        .put('A', onAboutButtonSelectedAction)
                        .put('E', onExitApplicationButtonSelected)
                        .build(), false));
    }

    private final Action onConnectToDatabaseButtonSelectedAction = new Action() {

        public void doAction() {
            TerminalUI.showWindowCenter(new SelectConnectionForQueryWindow());
        }
    };

    private final Action onManageConnectionButtonSelectedAction = new Action() {

        public void doAction() {
            TerminalUI.showWindowCenter(new ManageConnectionsWindow());
        }

    };

    private final Action onAboutButtonSelectedAction = new Action() {

        public void doAction() {
            TerminalUI.showMessageBox("About BlauSQL", Constants.ABOUT_TEXT);
        }
    };

    private final Action onExitApplicationButtonSelected = new Action() {

        public void doAction() {
            Main.exitApplication(0);

        }
    };

}