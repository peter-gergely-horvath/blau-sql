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
import com.github.blausql.core.preferences.ConfigurationRepository;
import com.github.blausql.core.preferences.SaveException;
import com.github.blausql.ui.util.BackgroundWorker;
import com.github.blausql.core.util.TextUtils;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;


import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

final class SetClasspathWindow extends LegacyWindowSupport {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private final TextBox classpathEditArea;

    SetClasspathWindow(String[] classpath) {

        super("Set JDBC Driver Classpath");

        Panel bottomPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        Panel verticalPanel = new Panel(new BorderLayout());

        bottomPanel.addComponent(new Label(">>> Press TAB >>>"));

        bottomPanel.addComponent(new Button("Save Changes (CTRL+S)", onSaveChangesButtonSelectedRunnable));
        bottomPanel.addComponent(new Button("Cancel (ESC)", onCancelButtonSelectedRunnable));

        TerminalSize screenTerminalSize = TerminalUI.getTerminalSize();

        final int sqlEditorPanelColumns = screenTerminalSize.getColumns() - 4;
        final int sqlEditorPanelRows = screenTerminalSize.getRows() - 4;

        String classpathText = TextUtils.joinStringsWithNewLine(classpath);

        TerminalSize preferredSizeForTextArea = new TerminalSize(sqlEditorPanelColumns, sqlEditorPanelRows);


        classpathEditArea = new TextBox(preferredSizeForTextArea, classpathText);


        Label topLabel = new Label("Enter Classpath entries - each on a new line:");

        topLabel.setLayoutData(BorderLayout.Location.TOP);
        verticalPanel.addComponent(topLabel);

        classpathEditArea.setLayoutData(BorderLayout.Location.CENTER);
        verticalPanel.addComponent(classpathEditArea);

        bottomPanel.setLayoutData(BorderLayout.Location.BOTTOM);
        verticalPanel.addComponent(bottomPanel);

        addComponent(verticalPanel);


        addWindowListener(new WindowListenerAdapter() {
            @Override
            public void onUnhandledInput(Window basePane, KeyStroke keyStroke, AtomicBoolean hasBeenHandled) {
                SetClasspathWindow.this.onUnhandledInput(basePane, keyStroke, hasBeenHandled);
            }
        });
    }

    private void onUnhandledInput(Window basePane, KeyStroke keyStroke, AtomicBoolean hasBeenHandled) {

        if (keyStroke.isCtrlDown()) {
            Character character = keyStroke.getCharacter();

            if (character != null) {
                if (character == 'S' || character == 's') {
                    onCancelButtonSelectedRunnable.run();
                    hasBeenHandled.set(true);
                }
            }
        }

        if (KeyType.Escape.equals(keyStroke.getKeyType())) {
            onCancelButtonSelectedRunnable.run();
            hasBeenHandled.set(true);
        }
    }

    private final Runnable onSaveChangesButtonSelectedRunnable = new Runnable() {

        public void run() {
            saveClasspath(classpathEditArea.getText());
        }
    };

    private final Runnable onCancelButtonSelectedRunnable = new Runnable() {

        public void run() {
            SetClasspathWindow.this.close();
        }

    };


    private void saveClasspath(final String newLineSeparatedClasspathString) {

        final Window showWaitDialog = TerminalUI.showWaitDialog("Please wait", "Validating Classpath settings ...");

        new BackgroundWorker<Void>() {

            @Override
            protected Void doBackgroundTask() throws SaveException {


                String[] classPathStrings = newLineSeparatedClasspathString.split(LINE_SEPARATOR);

                for (int i = 0; i < classPathStrings.length; i++) {
                    // the user might enter the classpath entry with leading or trailing spaces,
                    // e.g. " /foo/bar.jar  ", so trim each string to ensure it can be resolved as a path
                    classPathStrings[i] = classPathStrings[i].trim();
                }

                for (String path : classPathStrings) {
                    if (!"".equals(path) && !new File(path).exists()) {
                        throw new RuntimeException("Not found: " + path);
                    }
                }

                ConfigurationRepository.getInstance().saveClasspath(classPathStrings);

                return null;


            }

            @Override
            protected void onBackgroundTaskFailed(Throwable t) {
                showWaitDialog.close();
                TerminalUI.showErrorMessageFromThrowable(t);
                setFocusedInteractable(classpathEditArea);

            }

            @Override
            protected void onBackgroundTaskCompleted(Void voidResult) {
                showWaitDialog.close();

                SetClasspathWindow.this.close();
            }
        }.start();

    }

}
