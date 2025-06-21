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

import com.github.blausql.DialogResult;
import com.github.blausql.TerminalUI;
import com.github.blausql.core.preferences.LoadException;
import com.github.blausql.core.sqlfile.SqlFileRepository;
import com.github.blausql.ui.components.CloseOnEscapeKeyPressWindow;
import com.github.blausql.ui.util.DefaultErrorHandlerAction;
import com.github.blausql.ui.util.HotKeyWindowListener;

import com.googlecode.lanterna.gui2.Button;


import java.util.List;


@SuppressWarnings("FieldCanBeLocal")
class ManageBookmarkedSqlFilesWindow extends CloseOnEscapeKeyPressWindow {

    private final SqlFileRepository sqlFileRepository = SqlFileRepository.getInstance();

    ManageBookmarkedSqlFilesWindow() {

        super("Manage Bookmarked SQLs");

        addComponent(new Button("BACK (ESC)", new Runnable() {

            public void run() {
                ManageBookmarkedSqlFilesWindow.this.close();
            }
        }));
        addComponent(new Button("[D]elete bookmarked SQL", onDeleteBookmarkedSqlFileSelectedRunnable));

        addWindowListener(HotKeyWindowListener.builder()
                        .character('D').invoke(onDeleteBookmarkedSqlFileSelectedRunnable)
                        .build());
    }


    private final Runnable onDeleteBookmarkedSqlFileSelectedRunnable = new DefaultErrorHandlerAction() {

        public void runWithErrorHandler() throws LoadException {
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
