package org.infernus.idea.checkstyle.toolwindow.nodes;

import org.infernus.idea.checkstyle.csapi.SeverityLevel;
import org.jetbrains.annotations.NotNull;

/**
 * The user object for a {@link SeverityLevel} branch node in the tool window.
 */
public class ResultTreeSeverityNode extends ResultTreeBranchNode {

    @NotNull
    private final SeverityLevel severityLevel;

    /**
     * Construct a {@link SeverityLevel} branch node.
     *
     * @param severityLevel the SeverityLevel.
     */
    public ResultTreeSeverityNode(@NotNull final SeverityLevel severityLevel) {
        this.severityLevel = severityLevel;
        setIcon(this.severityLevel.getIcon());
    }

    /**
     * Get the {@link SeverityLevel} associated with this node.
     *
     * @return the SeverityLevel.
     */
    @NotNull
    public SeverityLevel getSeverityLevel() {
        return severityLevel;
    }

    @Override
    @NotNull
    public String getDescription() {
        return getDescription(severityLevel.toString());
    }
}
