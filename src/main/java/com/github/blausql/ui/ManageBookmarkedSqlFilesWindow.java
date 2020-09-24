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
import com.github.blausql.core.preferences.LoadException;
import com.github.blausql.core.sqlfile.SqlFileRepository;
import com.github.blausql.ui.components.CloseOnEscapeKeyPressWindow;
import com.github.blausql.ui.util.DefaultErrorHandlerAction;
import com.github.blausql.ui.util.HotKeySupportListener;
import com.google.common.collect.ImmutableMap;
import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.dialog.DialogButtons;
import com.googlecode.lanterna.gui.dialog.DialogResult;

import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
class ManageBookmarkedSqlFilesWindow extends CloseOnEscapeKeyPressWindow {

    private final SqlFileRepository sqlFileRepository = SqlFileRepository.getInstance();

    ManageBookmarkedSqlFilesWindow() {

        super("Manage Bookmarked SQLs");

        addComponent(new Button("BACK (ESC)", new Action() {

            public void doAction() {
                ManageBookmarkedSqlFilesWindow.this.close();
            }
        }));
        addComponent(new Button("[D]elete bookmarked SQL", onDeleteBookmarkedSqlFileSelectedAction));

        addWindowListener(new HotKeySupportListener(
                ImmutableMap.<Character, Action>builder()
                        .put('D', onDeleteBookmarkedSqlFileSelectedAction)
                        .build(), true));
    }


    private final Action onDeleteBookmarkedSqlFileSelectedAction = new DefaultErrorHandlerAction() {

        public void doActionWithErrorHandler() throws LoadException {
            ManageBookmarkedSqlFilesWindow.this.close();

            List<String> fileNames = sqlFileRepository.listSqlFileNames();

            TerminalUI.showWindowCenter(new ListSelectorWindow<String>(
                    "Select bookmark to delete",
                    "No Bookmarks found",
                    fileNames) {
                @Override
                protected void onEntrySelected(final String selectedBookmarkName) {

                    DialogResult dialogResult = TerminalUI.showMessageBox(
                            "Confirm deletion of bookmarked SQL",
                            "Delete bookmarked SQL file: " + selectedBookmarkName,
                            DialogButtons.OK_CANCEL);

                    if (DialogResult.OK.equals(dialogResult)) {

                        try {
                            sqlFileRepository.deleteByFileName(selectedBookmarkName);
                        } catch (RuntimeException e) {
                            TerminalUI.showErrorMessageFromThrowable(e);
                        }
                    }

                    ManageBookmarkedSqlFilesWindow.this.close();
                }
            });
        }
    };
}
