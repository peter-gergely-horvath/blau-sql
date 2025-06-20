package com.github.blausql.ui;

import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Panel;

public class LegacyWindowSupport extends BasicWindow {

    private final Panel panel;

    public LegacyWindowSupport(String title) {
        super(title);

        panel = new Panel();
        setComponent(panel);
    }

    protected void addComponent(Component component) {
        panel.addComponent(component);
    }



}
