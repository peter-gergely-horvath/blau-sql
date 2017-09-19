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

 
package com.github.blausql.ui.components;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.component.Button;

import java.util.Objects;


public final class ActionButton extends Button implements Action {

    private final Action delegateAction;

    public ActionButton(String text, Action onPressEvent) {
        super(text, onPressEvent);

        Objects.requireNonNull(onPressEvent, "argument onPressEvent cannot be null");
        this.delegateAction = onPressEvent;
    }


    @Override
    public void doAction() {
        delegateAction.doAction();
    }
}
