package org.infernus.idea.checkstyle.toolwindow.nodes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;


/**
 * Base implementation for the user object for meta-data on branch nodes in the tool window.
 */
public abstract class ResultTreeNodeBase implements ResultTreeNode {

    @Nullable
    private Icon icon;
    @Nullable
    private String tooltip;

    public ResultTreeNodeBase() {
    }

    /**
     * Get the icon for this node, if any.
     *
     * @return the icon for this node, or {@code null} if none.
     */
    @Nullable
    public Icon getIcon() {
        return icon;
    }

    /**
     * Set the icon for this node, if any.
     *
     * @param icon the icon for this node, or {@code null} if none.
     */
    public void setIcon(@Nullable final Icon icon) {
        this.icon = icon;
    }

    /**
     * Get the tooltip for this node, if any.
     *
     * @return the tooltip for this node, or {@code null} if none.
     */
    @Nullable
    public String getTooltip() {
        return tooltip;
    }

    /**
     * Set the tooltip for this node, if any.
     *
     * @param tooltip the tooltip for this node, or {@code null} if none.
     */
    public void setTooltip(@Nullable final String tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    @NotNull
    public String toString() {
        return getDescription();
    }
}
