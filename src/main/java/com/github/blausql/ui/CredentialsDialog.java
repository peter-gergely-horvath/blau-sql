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

import com.github.blausql.core.connection.ConnectionDefinition;
import com.github.blausql.ui.components.CloseOnEscapeKeyPressWindow;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.component.EmptySpace;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.component.PasswordBox;
import com.googlecode.lanterna.gui.component.TextBox;
import com.googlecode.lanterna.gui.dialog.DialogResult;

public class CredentialsDialog extends CloseOnEscapeKeyPressWindow {

	private final TextBox userNameTextBox;
	private final PasswordBox passwordPasswordBox;
	
	private DialogResult dialogResult = DialogResult.CANCEL;
	
	public CredentialsDialog(ConnectionDefinition cd) {
		
		super("Enter credentials for " + cd.getConnectionName());

		addComponent(new Label("User name:"));
		addComponent(userNameTextBox =
				new TextBox(cd.getUserName(), 20));

		addComponent(new Label("Password:"));
		addComponent(passwordPasswordBox =
				 new PasswordBox(cd.getPassword(), 20));
		
        Button okButton = new Button("OK", new Action() {
            public void doAction()
            {
                dialogResult = DialogResult.OK;
                close();
            }
        });
        Button cancelButton = new Button("Cancel", new Action() {
            public void doAction()
            {
                dialogResult = DialogResult.CANCEL;
                close();
            }
        });
        
        int labelWidth = userNameTextBox.getPreferredSize().getColumns();
        Panel buttonPanel = new Panel(new Border.Invisible(), Panel.Orientation.HORISONTAL);
        int leftPadding = 0;
        int buttonsWidth = okButton.getPreferredSize().getColumns() +
                cancelButton.getPreferredSize().getColumns() + 1;
        if(buttonsWidth < labelWidth)
            leftPadding = (labelWidth - buttonsWidth) / 2;
        if(leftPadding > 0)
            buttonPanel.addComponent(new EmptySpace(leftPadding, 1));
        buttonPanel.addComponent(okButton);
        buttonPanel.addComponent(cancelButton);
        addComponent(new EmptySpace());
        addComponent(buttonPanel);
        setFocus(okButton);
	}
	
	public DialogResult getDialogResult() {
		return dialogResult;
	}
	
	public String getUserName() {
		return userNameTextBox.getText();
	}
	
	public String getPassword() {
		return passwordPasswordBox.getText();
	}
}