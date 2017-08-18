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

import com.github.blausql.TerminalUI;
import com.github.blausql.core.classloader.ClassLoaderFactory;
import com.github.blausql.core.classloader.ClasspathHelper;
import com.github.blausql.core.connection.Database;
import com.github.blausql.ui.util.BackgroundWorker;
import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.component.EditArea;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.input.Key.Kind;
import com.googlecode.lanterna.terminal.TerminalSize;

import java.net.MalformedURLException;

public class SetClasspathWindow extends Window {

	private final EditArea classpathEditArea;

	public SetClasspathWindow() {
		super("Set JDBC Driver Classpath");
		
		Panel bottomPanel = new Panel(new Border.Bevel(true),
				Panel.Orientation.HORISONTAL);
		Panel verticalPanel = new Panel(new Border.Invisible(),
				Panel.Orientation.VERTICAL);

		bottomPanel.addComponent(new Label(">>> Press TAB >>>"));

		bottomPanel.addComponent(new Button("Save Changes (CTRL+S)", onSaveChangesButtonSelectedAction));
		bottomPanel.addComponent(new Button("Cancel (ESC)", onCancelButtonSelectedAction));

		TerminalSize screenTerminalSize = TerminalUI.getTerminalSize();

		final int sqlEditorPanelColumns = screenTerminalSize.getColumns() - 4;
		final int sqlEditorPanelRows = screenTerminalSize.getRows() - 4;

		classpathEditArea = new EditArea(new TerminalSize(sqlEditorPanelColumns,
				sqlEditorPanelRows));

        verticalPanel.addComponent(new Label("Enter Classpath entries - each on a new line:"));

		verticalPanel.addComponent(classpathEditArea);

		verticalPanel.addComponent(bottomPanel);

		addComponent(verticalPanel);
	}

	private final Action onSaveChangesButtonSelectedAction = new Action() {

		public void doAction() {
            saveClasspath(classpathEditArea.getData());

        }
	};

    private final Action onCancelButtonSelectedAction = new Action() {

        public void doAction() {
            SetClasspathWindow.this.close();
        }

    };
	
	public void onKeyPressed(Key key) {

		if(Kind.NormalKey.equals(key.getKind()) &&
				(key.getCharacter() == 'S' || key.getCharacter() == 's') &&
				key.isCtrlPressed()) {

            onSaveChangesButtonSelectedAction.doAction();


        } else if(Kind.Escape.equals(key.getKind())) {

            onCancelButtonSelectedAction.doAction();


        } else {
			super.onKeyPressed(key);
		}
	}

    protected void saveClasspath(final String newLineSeparatedClasspathString) {
		
		final Window showWaitDialog = TerminalUI.showWaitDialog("Please wait", "Validating Classpath settings ...");

		new BackgroundWorker<Void>() {

			@Override
			protected Void doBackgroundTask() {

                try {
                    String lineSeparator = System.getProperty("line.separator");
                    String pathSeparator = System.getProperty("path.separator");

                    String pathSeparatorSeparatedPathEntries =
                            newLineSeparatedClasspathString.replace(lineSeparator, pathSeparator);


                    ClasspathHelper.getUrlsFromClasspathString(pathSeparatorSeparatedPathEntries);

                    return null;

                } catch (MalformedURLException e) {
                    throw new RuntimeException("Malformed URL : " + e.getMessage());
                }

			}

			@Override
			protected void onBackgroundTaskFailed(Throwable t) {
				showWaitDialog.close();
				TerminalUI.showErrorMessageFromThrowable(t);
				setFocus(classpathEditArea);

			}

			@Override
			protected void onBackgroundTaskCompleted(Void voidResult) {
				showWaitDialog.close();

                SetClasspathWindow.this.close();
			}
		}.start();
		
	}

}
