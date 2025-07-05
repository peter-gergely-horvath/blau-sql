/*
 * Copyright (c) 2017-2025 Peter G. Horvath, All Rights Reserved.
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
import com.github.blausql.ui.components.ApplicationWindow;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.List;

public class DisplayThrowableDialog extends ApplicationWindow {

    private final Component summaryComponent;
    private final Panel detailsPanel;


    private final Panel contentPanel;
    private final TerminalUI terminalUI;
    private final String stackTraceString;


    public DisplayThrowableDialog(String title,
                                  String text,
                                  Throwable throwableToDisplay,
                                  TerminalUI terminalUI) {
        super(title, terminalUI);
        this.terminalUI = terminalUI;

        setHints(List.of(Hint.MODAL));

        summaryComponent = Panels.vertical(new Label(throwableToDisplay.getMessage()))
                .withBorder(Borders.singleLine(String.format(" %s ", text)));

        Panel topPanel = Panels.horizontal(
                new Button("Summary", this::onSummaryButtonSelected),
                new Button("Details", this::onDetailButtonSelected));

        contentPanel = Panels.vertical(summaryComponent);

        stackTraceString = getStackTraceAsString(throwableToDisplay);

        TerminalSize terminalSize = terminalUI.getWindowBasedTextGUI().getScreen().getTerminalSize();

        TerminalSize detailSize = terminalSize
                .withRelativeRows(terminalSize.getRows() / 2)
                .withRelativeColumns(terminalSize.getColumns() / 2);

        TextBox stackTraceTextBox = new TextBox(detailSize, stackTraceString, TextBox.Style.MULTI_LINE);
        stackTraceTextBox.setReadOnly(true);

        detailsPanel = Panels.vertical(stackTraceTextBox);

        Panel buttonPanel = Panels.horizontal(
                new Button("OK", this::onOKButtonSelected),
                new Button("Save details to a file", this::onSaveDetailsToFile));

        Panel mainPanel = Panels.vertical(topPanel, contentPanel, buttonPanel);

        setComponent(mainPanel);
    }

    private static String getStackTraceAsString(Throwable throwableToDisplay) {

        try (StringWriter stringWriter = new StringWriter();
             PrintWriter printWriter = new PrintWriter(stringWriter)) {

            throwableToDisplay.printStackTrace(printWriter);
            return stringWriter.toString();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void onSummaryButtonSelected() {
        contentPanel.removeAllComponents();
        contentPanel.addComponent(summaryComponent);
    }


    private void onDetailButtonSelected() {
        contentPanel.removeAllComponents();
        contentPanel.addComponent(detailsPanel);
    }

    private void onOKButtonSelected() {
        this.close();
    }

    private void onSaveDetailsToFile() {
        File file = terminalUI.showFileSelectorDialog(
                "Save error detail",
                "Please select the location to save the error details",
                "Save");

        if (file != null) {
            try {
                Files.writeString(file.toPath(), stackTraceString);
            } catch (IOException e) {
                terminalUI.showMessageBox("Could not save error details",
                        "An error has occurred saving the error details: \n"
                                + e.getMessage());

            }

        }


    }


}
