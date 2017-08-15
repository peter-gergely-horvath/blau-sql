package com.github.blausql.ui;


import com.github.blausql.Main;
import com.github.blausql.TerminalUI;
import com.github.blausql.core.preferences.ConnectionDefinitionRepository;
import com.github.blausql.util.TextUtils;
import org.springframework.util.Assert;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.listener.WindowAdapter;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.input.Key.Kind;

public class MainMenuWindow extends Window {

    private static final String ABOUT_TEXT = TextUtils.separateLines(
            "",
            "Copyright 2017 Peter G. Horvath",
            "",
            "Licensed under the Apache License, Version 2.0 (the \"License\");",
            "you may not use this file except in compliance with the License.",
            "You may obtain a copy of the License at",
            "",
            "    http://www.apache.org/licenses/LICENSE-2.0",
            "",
            "Unless required by applicable law or agreed to in writing, software",
            "distributed under the License is distributed on an \"AS IS\" BASIS,",
            "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.",
            "See the License for the specific language governing permissions and",
            "limitations under the License."
    );

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

        addComponent(new Button("[A]bout", new Action() {

            public void doAction() {
                onAboutButtonSelected();
            }

        }));

        addComponent(new Button("[E]xit Application", new Action() {

            public void doAction() {
                onExitApplicationButtonSelected();

            }
        }));
    }

    private void onConnectToDatabaseButtonSelected() {
        TerminalUI.showWindowCenter(new SelectConnectionForQueryWindow());
    }

    private void onManageConnectionsButtonSelected() {
        TerminalUI.showWindowCenter(new ManageConnectionsWindow());
    }

    private void onAboutButtonSelected() {
        TerminalUI.showMessageBox("About BlauSQL... ", ABOUT_TEXT);
    }

    private void onExitApplicationButtonSelected() {
        Main.exitApplication(0);
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

                        case 'A':
                        case 'a':
                            onAboutButtonSelected();
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