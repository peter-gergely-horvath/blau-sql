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
import com.github.blausql.core.util.ExceptionUtils;
import com.github.blausql.ui.components.ApplicationWindow;
import com.github.blausql.ui.hotkey.HotKeyWindowListener;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

public final class DisplayThrowableDialog extends ApplicationWindow {

    private interface SelectablePanel extends Component {
        void onSelected();
    }

    private final class SummaryPanel extends Panel implements SelectablePanel {

        private final Button closeButton;

        private SummaryPanel(String message) {
            setLayoutManager(new LinearLayout(Direction.VERTICAL));

            closeButton = new Button("Close (ESC)",
                    DisplayThrowableDialog.this::onCloseButtonSelected);

            addComponent(new EmptySpace());
            addComponent(new Label(message));
            addComponent(new EmptySpace());
            addComponent(new EmptySpace());

            addComponent(Panels.horizontal(
                    new EmptySpace(),
                    closeButton,
                    new Button("Show Details (F4)",
                            DisplayThrowableDialog.this::onDetailButtonSelected)));
        }

        @Override
        public void onSelected() {
            setFocusedInteractable(closeButton);
        }
    }

    private final class DetailsPanel extends Panel implements SelectablePanel {

        private final Button closeButton;

        private DetailsPanel(TerminalUI terminalUI) {
            TerminalSize terminalSize = terminalUI.getWindowBasedTextGUI().getScreen().getTerminalSize();

            closeButton = new Button("Close (ESC)",
                    DisplayThrowableDialog.this::onCloseButtonSelected);

            TerminalSize detailSize = terminalSize
                    .withRows(terminalSize.getRows() / 2)
                    .withColumns(terminalSize.getColumns() / 2);


            TextBox stackTraceTextBox = new TextBox(detailSize, stackTraceString, TextBox.Style.MULTI_LINE);
            stackTraceTextBox.setReadOnly(true);

            addComponent(stackTraceTextBox);

            addComponent(new EmptySpace());

            addComponent(
                    Panels.horizontal(
                            new EmptySpace(),
                            closeButton,
                            new Button("Show summary (F3)", DisplayThrowableDialog.this::onSummaryButtonSelected),
                            new Button("Save Details (F5)", DisplayThrowableDialog.this::onSaveDetailsToFile))
            );
        }

        @Override
        public void onSelected() {
            setFocusedInteractable(closeButton);
        }
    }


    private final Panel contentPanel;

    private final SummaryPanel summaryPanel;
    private final DetailsPanel detailsPanel;

    private final String stackTraceString;

    public DisplayThrowableDialog(String title,
                                  String text,
                                  Throwable throwableToDisplay,
                                  TerminalUI terminalUI) {
        super(title, terminalUI);

        setHints(List.of(Hint.MODAL));

        final Throwable rootCause = ExceptionUtils.getRootCause(throwableToDisplay);

        String message = ExceptionUtils.extractMessageFrom(rootCause)
                .or(() -> Optional.ofNullable(text))
                .orElse("Error");

        stackTraceString = ExceptionUtils.getStackTraceAsString(throwableToDisplay);

        summaryPanel = new SummaryPanel(message);

        detailsPanel = new DetailsPanel(terminalUI);

        contentPanel = Panels.vertical(summaryPanel);

        setComponent(contentPanel);

        addWindowListener(HotKeyWindowListener.builder()
                .keyType(KeyType.Escape).invoke(this::onCloseButtonSelected)
                .keyType(KeyType.F3).invoke(this::onSummaryButtonSelected)
                .keyType(KeyType.F4).invoke(this::onDetailButtonSelected)
                .keyType(KeyType.F5).invoke(this::onSaveDetailsToFile)
                .build());
    }

    private void onSummaryButtonSelected() {
        selectPanel(summaryPanel);
    }


    private void onDetailButtonSelected() {
        selectPanel(detailsPanel);
    }


    private void selectPanel(SelectablePanel selectablePanel) {
        contentPanel.removeAllComponents();
        contentPanel.addComponent(selectablePanel);
        selectablePanel.onSelected();
    }

    private void onCloseButtonSelected() {
        this.close();
    }

    private void onSaveDetailsToFile() {
        File file = showFileSelectorDialog(
                "Save error detail",
                "Please select the location to save the error details",
                "Save");

        if (file != null) {
            try {
                Files.writeString(file.toPath(), stackTraceString);
            } catch (IOException e) {
                showMessageBox("Could not save error details",
                        "An error has occurred saving the error details: \n"
                                + e.getMessage());
            }
        }
    }
}
