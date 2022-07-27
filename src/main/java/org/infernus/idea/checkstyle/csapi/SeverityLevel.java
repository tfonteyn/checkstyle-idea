package org.infernus.idea.checkstyle.csapi;

import com.intellij.icons.AllIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Checkstyle violation severity levels supported by this plugin.
 */
public enum SeverityLevel {
    Ignore(AllIcons.General.Note),
    Info(AllIcons.General.Information),
    Warning(AllIcons.General.Warning),
    Error(AllIcons.General.Error);

    @NotNull
    private final Icon icon;

    SeverityLevel(@NotNull final Icon icon) {
        this.icon = icon;
    }

    public @NotNull Icon getIcon() {
        return icon;
    }
}
