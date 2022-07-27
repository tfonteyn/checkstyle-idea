package org.infernus.idea.checkstyle.toolwindow.nodes;

import org.infernus.idea.checkstyle.CheckStyleBundle;
import org.jetbrains.annotations.NotNull;

/**
 * The user object for meta-data on branch nodes in the tool window.
 */
public abstract class ResultTreeBranchNode extends ResultTreeNodeBase {

    int itemCount;

    public ResultTreeBranchNode() {
    }

    /**
     * Increase the item-count. Used to keep track as/when nodes are created.
     */
    public void increaseItemCount() {
        itemCount++;
    }

    public void setItemCount(final int itemCount) {
        this.itemCount = itemCount;
    }

    @NotNull
    public String getDescription(@NotNull final String text) {
        if (itemCount > 0) {
            return CheckStyleBundle.message("plugin.results.node.info-with-count",
                                            text, itemCount);
        } else {
            return text;
        }
    }
}
