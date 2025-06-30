/*
 * Copyright (c) 2017-2020 Peter G. Horvath, All Rights Reserved.
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
import com.github.blausql.ui.util.HotKeyWindowListener;


import java.util.List;

public class MainMenuWindow extends LegacyWindowSupport {

    public MainMenuWindow() {

        super(Constants.APPLICATION_BANNER);

        addComponent(connectToDatabaseButton);
        addComponent(manageConnectionButton);
        addComponent(setApplicationClasspathButton);
        addComponent(aboutButton);

        addComponent(quitApplicationButton);

        addWindowListener(HotKeyWindowListener.builder()
                .character('C').invoke(connectToDatabaseButton)
                .character('M').invoke(manageConnectionButton)
                .character('S').invoke(setApplicationClasspathButton)
                .character('A').invoke(aboutButton)
                .character('Q').invoke(quitApplicationButton)
                .build());
    }


    private final ActionButton connectToDatabaseButton =
            new ActionButton("[C]onnect to database", new DefaultErrorHandlerAction() {

                public void runWithErrorHandler() throws LoadException {

                    List<ConnectionDefinition> connectionDefinitions =
                            ConnectionDefinitionRepository.getInstance().getConnectionDefinitions();

                    TerminalUI.showWindowCenter(new SelectConnectionForQueryWindow(connectionDefinitions));
                }
            });

    private final ActionButton manageConnectionButton = new ActionButton("[M]anage Connections", new Runnable() {

        public void run() {
            TerminalUI.showWindowCenter(new ManageConnectionsWindow());
        }

    });

    private final ActionButton setApplicationClasspathButton = new ActionButton("[S]et Classpath",
            new DefaultErrorHandlerAction() {

                public void runWithErrorHandler() throws LoadException {

                    List<String> classpath = ConfigurationRepository.getInstance().getClasspath();

                    TerminalUI.showWindowFullScreen(new SetClasspathWindow(classpath));
                }

            });

    private final ActionButton aboutButton = new ActionButton("[A]bout", new Runnable() {

        public void run() {
            TerminalUI.showMessageBox("About BlauSQL", Constants.ABOUT_TEXT);
        }
    });

    private final ActionButton quitApplicationButton = new ActionButton("[Q]uit Application", new Runnable() {

        public void run() {
            close();
        }
    });
}
