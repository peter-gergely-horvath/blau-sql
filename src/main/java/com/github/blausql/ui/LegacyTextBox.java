package com.github.blausql.ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.TextBox;

public class LegacyTextBox extends TextBox {
    public LegacyTextBox(String initialContent, int passwordBoxLen) {
        super(new TerminalSize(passwordBoxLen, 1), nullToEmptyString(initialContent));
    }

    private static String nullToEmptyString(String initialContent) {
        if (initialContent != null) {
            return initialContent;
        } else {
            return "";
        }
    }
}
