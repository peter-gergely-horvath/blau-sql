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

import com.github.blausql.ui.components.CloseOnEscapeKeyPressWindow;
import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.component.Label;

import java.util.List;

abstract class ListSelectorWindow<T> extends CloseOnEscapeKeyPressWindow {

    ListSelectorWindow(String title, String emptyListMessage, List<T> list) {
        super(title);

        addComponent(new Button("CANCEL (ESC)", new Action() {

            public void doAction() {
                ListSelectorWindow.this.close();
            }
        }));


        if (list.isEmpty()) {
            addComponent(new Label(emptyListMessage));
        } else {
            for (final T entry : list) {

                addComponent(new Button(entry.toString(), new Action() {

                    public void doAction() {
                        ListSelectorWindow.this.close();

                        ListSelectorWindow.this.onEntrySelected(entry);
                    }
                }));
            }
        }
    }

    protected abstract void onEntrySelected(T selected);
}
