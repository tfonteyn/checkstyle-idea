package org.infernus.idea.checkstyle.toolwindow.nodes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface ResultTreeNode {

    /**
     * Get the description of this node. This is the full text which will be displayed in the tree view.
     *
     * @return the description of this node.
     */
    @NotNull
    String getDescription();

    /**
     * Get the tooltip for this node, if any.
     *
     * @return the tooltip for this node, or {@code null} if none.
     */
    @Nullable
    String getTooltip();

    /**
     * Set the tooltip for this node, if any.
     *
     * @param tooltip the tooltip for this node, or null if none.
     */
    void setTooltip(@Nullable String tooltip);

    @Nullable
    Icon getIcon();

    /**
     * Get the node's icon when in an expanded state.
     *
     * @return the node's icon when in an expanded state.
     */
    @Nullable
    default Icon getExpandedIcon() {
        return getIcon();
    }

    /**
     * Get the node's icon when in a collapsed state.
     *
     * @return the node's icon when in a collapsed state.
     */
    @Nullable
    default Icon getCollapsedIcon() {
        return getIcon();
    }
}
