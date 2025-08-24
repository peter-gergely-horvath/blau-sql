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
import com.github.blausql.core.preferences.ConfigurationRepository;
import com.github.blausql.spi.connections.SaveException;
import com.github.blausql.ui.components.ApplicationWindow;
import com.github.blausql.ui.util.BackgroundWorker;
import com.github.blausql.ui.hotkey.HotKeyWindowListener;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;

import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.input.KeyType;


import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

final class SetClasspathWindow extends ApplicationWindow {

    private final ActionListBox driverJarFilesActionList;

    private boolean thereAreUnsavedChanges = false;

    SetClasspathWindow(List<String> classpath, TerminalUI terminalUI) {

        super("Set JDBC Driver Classpath", terminalUI);

        Button addFileToClasspathButton = new Button("Add file to Classpath", this::addNewJarFileButtonSelected);

        TerminalSize terminalSize = getApplicationTextGUI().getScreen().getTerminalSize();
        TerminalSize size = getDesiredDriverJarFilesActionListSize(terminalSize);
        driverJarFilesActionList = new ActionListBox(size);

        Border borderOfDriverJarFilesActionList = Borders.singleLine();
        borderOfDriverJarFilesActionList.setComponent(driverJarFilesActionList);
        classpath.forEach(classpathEntry ->
                driverJarFilesActionList.addItem(classpathEntry, this::onEntrySelectedForEditing));


        Button saveChangeButton = new Button("Save Changes (CTRL+S)", this::onSaveChangesButtonSelected);
        Button discardChangesButton = new Button("Discard Changes (CTRL+R)", this::onCancelButtonSelected);

        Panel bottomButtonBar = Panels.horizontal(saveChangeButton, discardChangesButton);

        setComponent(Panels.vertical(addFileToClasspathButton, borderOfDriverJarFilesActionList, bottomButtonBar));

        addWindowListener(HotKeyWindowListener.builder()
                .character('S').ctrlDown().invoke(this::onSaveChangesButtonSelected)
                .character('R').ctrlDown().invoke(this::onCancelButtonSelected)
                .keyType(KeyType.Escape).invoke(this::onCancelButtonSelected)
                .build());

        addWindowListener(new WindowListenerAdapter() {

            @Override
            public void onResized(Window window, TerminalSize oldSize, TerminalSize newSize) {

                driverJarFilesActionList.setSize(getDesiredDriverJarFilesActionListSize(newSize));
            }
        });
    }


    private static TerminalSize getDesiredDriverJarFilesActionListSize(TerminalSize terminalSize) {
        return new TerminalSize(terminalSize.getColumns() - 2, terminalSize.getRows() - 2);
    }

    private void addNewJarFileButtonSelected() {
        File file = showFileSelectorDialog(
                "Select JDBC Driver JAR file",
                "Please select the JDBC Driver JAR file",
                "Select");

        if (file != null) {
            driverJarFilesActionList.addItem(file.getAbsolutePath(), this::onEntrySelectedForEditing);

            thereAreUnsavedChanges = true;
        }
    }


    private void onEntrySelectedForEditing() {

        int selectedIndex = driverJarFilesActionList.getSelectedIndex();

        Runnable selectedRunnable = driverJarFilesActionList.getItemAt(selectedIndex);

        if (selectedRunnable != null) {
            // weird, but the component implementation overrides toString
            // with the value of the item label, so this is the way
            String selectedFile = selectedRunnable.toString();

            MessageDialogButton dialogResult = showMessageBox("Remove this file?",
                    "Do you want to remove the file: \n" + selectedFile,
                    MessageDialogButton.OK, MessageDialogButton.Cancel);

            if (dialogResult == MessageDialogButton.OK) {

                driverJarFilesActionList.removeItem(selectedIndex);

                thereAreUnsavedChanges = true;
            }
        }
    }


    private void onSaveChangesButtonSelected() {

        List<String> classpathEntries = driverJarFilesActionList.getItems().stream()
                .map(Objects::toString)
                .collect(Collectors.toList());

        saveClasspath(classpathEntries);
    }

    private void onCancelButtonSelected() {

        if (thereAreUnsavedChanges) {

            MessageDialogButton dialogResult = showMessageBox("Discard changes?",
                    "Classpath settings are not saved yet: \ndo you want to discard all changes?",
                    MessageDialogButton.OK, MessageDialogButton.Cancel);

            if (dialogResult == MessageDialogButton.Cancel) {
                return;
            }
        }

        close();
    }


    private void saveClasspath(List<String> classPathStrings) {

        final Window showWaitDialog = showWaitDialog("Please wait", "Validating Classpath settings ...");

        new BackgroundWorker<Void>(this) {

            @Override
            protected Void doBackgroundTask() throws SaveException {

                for (String path : classPathStrings) {
                    if (!"".equals(path) && !new File(path).exists()) {
                        throw new RuntimeException("Not found: " + path);
                    }
                }

                ConfigurationRepository.getInstance().saveClasspath(classPathStrings);

                return null;


            }

            @Override
            protected void onBackgroundTaskInterrupted(InterruptedException interruptedException) {
                showWaitDialog.close();
            }

            @Override
            protected void onBackgroundTaskFailed(Throwable t) {
                showWaitDialog.close();
                showErrorMessageFromThrowable(t);
            }

            @Override
            protected void onBackgroundTaskCompleted(Void voidResult) {
                showWaitDialog.close();

                SetClasspathWindow.this.close();
            }
        }.start();

    }

}
