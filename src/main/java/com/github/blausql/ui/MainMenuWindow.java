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


import com.github.blausql.TerminalUI;
import com.github.blausql.core.Constants;
import com.github.blausql.core.connection.ConnectionDefinition;
import com.github.blausql.core.preferences.ConfigurationRepository;
import com.github.blausql.core.preferences.ConnectionDefinitionRepository;
import com.github.blausql.core.preferences.LoadException;
import com.github.blausql.ui.components.ActionButton;
import com.github.blausql.ui.components.ApplicationWindow;
import com.github.blausql.ui.hotkey.HotKeyWindowListener;
import com.googlecode.lanterna.gui2.Panels;


import java.util.List;

public class MainMenuWindow extends ApplicationWindow {

    public MainMenuWindow(TerminalUI terminalUI) {

        super(String.format(" %s ", Constants.APPLICATION_BANNER), terminalUI);

        ActionButton connectToDatabaseButton =
                button("[C]onnect to database", this::onConnectorToDatabaseButtonSelected);

        ActionButton manageConnectionsButton  =
                button("[M]anage Connections", this::onManageConnectionButtonSelected);

        ActionButton setClasspathButton  =
                button("[S]et Classpath", this::onSetClasspathButtonSelected);

        ActionButton aboutButton  =
                button("[A]bout", this::onAboutButtonSelected);

        ActionButton quitApplicationButton  =
                button("[Q]uit", this::close);

        setComponent(Panels.vertical(
                connectToDatabaseButton,
                manageConnectionsButton,
                setClasspathButton,
                aboutButton,
                quitApplicationButton));

        addWindowListener(HotKeyWindowListener.builder()
                .character('C').invoke(connectToDatabaseButton)
                .character('M').invoke(manageConnectionsButton)
                .character('S').invoke(setClasspathButton)
                .character('A').invoke(aboutButton)
                .character('Q').invoke(quitApplicationButton)
                .build());
    }


    private void onConnectorToDatabaseButtonSelected() throws LoadException {
        List<ConnectionDefinition> connectionDefinitions =
                ConnectionDefinitionRepository.getInstance().getConnectionDefinitions();

        showWindowCenter(new SelectConnectionForQueryWindow(connectionDefinitions, getTerminalUI()));
    }

    private void onManageConnectionButtonSelected() {
        showWindowCenter(new ManageConnectionsWindow(getTerminalUI()));
    }

    private void onSetClasspathButtonSelected() throws LoadException {
        List<String> classpath = ConfigurationRepository.getInstance().getClasspath();

        showWindowFullScreen(new SetClasspathWindow(classpath, getTerminalUI()));
    }

    private void onAboutButtonSelected() {
        showMessageBox("About BlauSQL", Constants.ABOUT_TEXT);
    }
}
