package com.github.blausql.ui.components;

import com.github.blausql.core.util.TextUtils;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.TextBox;

public class PasswordBox extends TextBox {

    public PasswordBox(int length, String initialContent) {
        super(new TerminalSize(length, 1), TextUtils.nullToEmptyString(initialContent));

        setMask('*');
    }
}
