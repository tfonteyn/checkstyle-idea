package org.infernus.idea.checkstyle.toolwindow.nodes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static org.infernus.idea.checkstyle.util.Strings.isBlank;

/**
 * The user object for a text/info branch node in the tool window.
 */
public class ResultTreeTextNode extends ResultTreeBranchNode {

    @NotNull
    private String text;

    /**
     * Construct a generic informational node with the given Icon.
     *
     * @param text the information text.
     * @param icon the icon for the node; or {@code null} for none.
     */
    public ResultTreeTextNode(@NotNull final String text, @Nullable final Icon icon) {
        if (isBlank(text)) {
            throw new IllegalArgumentException("Text may not be null/empty");
        }
        this.text = text;
        setIcon(icon);
    }

    /**
     * Set the text associated with this node.
     *
     * @param text the information text.
     */
    public void setText(@NotNull final String text) {
        if (isBlank(text)) {
            throw new IllegalArgumentException("Text may not be null/empty");
        }
        this.text = text;
    }

    /**
     * Get the text associated with this node.
     *
     * @return the text.
     */
    @NotNull
    public String getText() {
        return text;
    }

    @Override
    @NotNull
    public String getDescription() {
        return getDescription(text);
    }
}
