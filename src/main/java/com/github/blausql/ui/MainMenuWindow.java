/*
 * Copyright (c) 2017 Peter G. Horvath, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 
package com.github.blausql.ui;


import com.github.blausql.Main;
import com.github.blausql.TerminalUI;
import com.github.blausql.core.Constants;
import com.github.blausql.core.preferences.ConfigurationRepository;
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
        addComponent(new Button("[S]et Classpath", onSetApplicationClasspathButtonSelectedAction));
        addComponent(new Button("[A]bout", onAboutButtonSelectedAction));
        addComponent(new Button("[Q]uit Application", onQuitApplicationButtonSelected));

        addWindowListener(new HotKeySupportListener(
                ImmutableMap.<Character, Action>builder()
                        .put('C', onConnectToDatabaseButtonSelectedAction)
                        .put('M', onManageConnectionButtonSelectedAction)
                        .put('S', onSetApplicationClasspathButtonSelectedAction)
                        .put('A', onAboutButtonSelectedAction)
                        .put('Q', onQuitApplicationButtonSelected)
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

    private final Action onSetApplicationClasspathButtonSelectedAction = new Action() {

        public void doAction() {

            String[] classpath = ConfigurationRepository.getInstance().getClasspath();

            TerminalUI.showWindowCenter(new SetClasspathWindow(classpath));
        }

    };

    private final Action onAboutButtonSelectedAction = new Action() {

        public void doAction() {
            TerminalUI.showMessageBox("About BlauSQL", Constants.ABOUT_TEXT);
        }
    };

    private final Action onQuitApplicationButtonSelected = new Action() {

        public void doAction() {
            Main.exitApplication(0);

        }
    };

}
