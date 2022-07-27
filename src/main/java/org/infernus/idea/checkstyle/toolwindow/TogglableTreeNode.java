package org.infernus.idea.checkstyle.toolwindow;

import org.infernus.idea.checkstyle.toolwindow.nodes.ResultTreeNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Tree node with toggleable visibility.
 */
public class TogglableTreeNode extends DefaultMutableTreeNode {
    private static final long serialVersionUID = -4490734768175672868L;

    private boolean visible = true;

    public TogglableTreeNode(@NotNull final ResultTreeNode userObject) {
        super(userObject);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    @Override
    @NotNull
    public ResultTreeNode getUserObject() {
        return (ResultTreeNode) Objects.requireNonNull(super.getUserObject());
    }

    @NotNull
    List<TogglableTreeNode> getAllChildren() {
        if (children != null) {
            return children.stream()
                    .map(child -> (TogglableTreeNode) child)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public TreeNode getChildAt(final int index) {
        int realIndex = -1;
        int visibleIndex = -1;

        for (final TogglableTreeNode child : getAllChildren()) {
            if (child.isVisible()) {
                ++visibleIndex;
            }
            ++realIndex;
            if (visibleIndex == index) {
                return children.get(realIndex);
            }
        }

        throw new ArrayIndexOutOfBoundsException("Invalid index: " + index);
    }

    @Override
    public int getChildCount() {
        if (children == null) {
            return 0;
        }
        return (int) getAllChildren().stream()
                .filter(TogglableTreeNode::isVisible)
                .count();
    }
}
