package com.github.blausql.ui;

import com.github.blausql.core.util.TextUtils;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.TextBox;

public class SimpleTextBox extends TextBox {
    public SimpleTextBox(String initialContent, int length) {
        super(new TerminalSize(length, 1), TextUtils.nullToEmptyString(initialContent));
    }

}
