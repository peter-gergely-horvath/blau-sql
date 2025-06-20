package com.github.blausql.ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.TextBox;

public class PasswordBox extends TextBox {
    public PasswordBox(String initialContent, int passwordBoxLen) {
        super(new TerminalSize(passwordBoxLen, 1), initialContent != null ? initialContent : "");
    }
}
