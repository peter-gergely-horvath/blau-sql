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
import com.github.blausql.core.connection.ConnectionDefinition;
import com.github.blausql.core.preferences.ConfigurationRepository;
import com.github.blausql.core.preferences.ConnectionDefinitionRepository;
import com.github.blausql.core.preferences.LoadException;
import com.github.blausql.ui.components.ActionButton;
import com.github.blausql.ui.util.DefaultErrorHandlerAction;
import com.github.blausql.ui.util.HotKeySupportListener;
import com.google.common.collect.ImmutableMap;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.Window;

import java.util.List;

public class MainMenuWindow extends Window {

    public MainMenuWindow() {

        super(Constants.APPLICATION_BANNER);

        addComponent(connectToDatabaseButton);
        addComponent(manageConnectionButton);
        addComponent(setApplicationClasspathButton);
        addComponent(manageBookmarkedSqlFilesButton);
        addComponent(aboutButton);

        addComponent(quitApplicationButton);

        addWindowListener(new HotKeySupportListener(
                ImmutableMap.<Character, Action>builder()
                        .put('C', connectToDatabaseButton)
                        .put('M', manageConnectionButton)
                        .put('B', manageBookmarkedSqlFilesButton)
                        .put('S', setApplicationClasspathButton)
                        .put('A', aboutButton)
                        .put('Q', quitApplicationButton)
                        .build(), false));
    }



    private final ActionButton connectToDatabaseButton =
            new ActionButton("[C]onnect to database", new DefaultErrorHandlerAction() {

                public void doActionWithErrorHandler() throws LoadException {

                    List<ConnectionDefinition> connectionDefinitions =
                            ConnectionDefinitionRepository.getInstance().getConnectionDefinitions();

                    TerminalUI.showWindowCenter(new SelectConnectionForQueryWindow(connectionDefinitions));
                }
            });

    private final ActionButton manageConnectionButton = new ActionButton("[M]anage Connections", new Action() {

        public void doAction() {
            TerminalUI.showWindowCenter(new ManageConnectionsWindow());
        }

    });

    private final ActionButton setApplicationClasspathButton = new ActionButton("[S]et Classpath",
            new DefaultErrorHandlerAction() {

        public void doActionWithErrorHandler() throws LoadException {

            String[] classpath = ConfigurationRepository.getInstance().getClasspath();

            TerminalUI.showWindowCenter(new SetClasspathWindow(classpath));
        }

    });

    private final ActionButton manageBookmarkedSqlFilesButton = new ActionButton("[B]ookmarked SQL Files",
            new DefaultErrorHandlerAction() {

                public void doActionWithErrorHandler() throws LoadException {

                    TerminalUI.showWindowCenter(new ManageBookmarkedSqlFilesWindow());
                }

            });

    private final ActionButton aboutButton = new ActionButton("[A]bout", new Action() {

        public void doAction() {
            TerminalUI.showMessageBox("About BlauSQL", Constants.ABOUT_TEXT);
        }
    });

    private final ActionButton quitApplicationButton = new ActionButton("[Q]uit Application", new Action() {

        public void doAction() {
            Main.exitApplication(0);

        }
    });
}
