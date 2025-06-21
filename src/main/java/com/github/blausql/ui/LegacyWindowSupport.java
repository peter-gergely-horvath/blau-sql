package com.github.blausql.ui;

import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Panel;

public class LegacyWindowSupport extends BasicWindow {

    private final Panel panel;

    public LegacyWindowSupport(String title) {
        super(String.format(" %s ", title));

        panel = new Panel();
        setComponent(panel);
    }

    /**
     * Adds the specified component to the default, main panel of the Window
     * @param component the component to add to the default, main panel of the Window
     */
    protected final void addComponent(Component component) {
        panel.addComponent(component);
    }
}
