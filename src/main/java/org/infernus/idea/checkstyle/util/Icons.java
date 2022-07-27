package org.infernus.idea.checkstyle.util;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public final class Icons {

    private static final String BASE = "/org/infernus/idea/checkstyle/images/";
    public static final Icon CS = IconLoader.getIcon(BASE + "cs_16.svg", Icons.class);

    private Icons() {
    }
}
