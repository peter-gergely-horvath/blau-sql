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
