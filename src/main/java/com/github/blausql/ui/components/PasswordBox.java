package com.github.blausql.ui.components;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.TextBox;

public class PasswordBox extends TextBox {

    public PasswordBox(int length, String initialContent) {
        super(new TerminalSize(length, 1), initialContent);

        setMask('*');
    }
}
