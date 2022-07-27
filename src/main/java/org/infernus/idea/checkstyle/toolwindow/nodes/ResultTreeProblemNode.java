package org.infernus.idea.checkstyle.toolwindow.nodes;

import org.infernus.idea.checkstyle.CheckStyleBundle;
import org.infernus.idea.checkstyle.checker.Problem;
import org.jetbrains.annotations.NotNull;

/**
 * The user object for a {@link Problem} leaf node in the tool window.
 */
public class ResultTreeProblemNode extends ResultTreeNodeBase {

    @NotNull
    private final Problem problem;

    /**
     * Construct a {@link Problem} leaf node.
     *
     * @param problem the problem.
     */
    public ResultTreeProblemNode(@NotNull final Problem problem) {
        this.problem = problem;
       setIcon(problem.severityLevel().getIcon());
    }

    /**
     * Get the {@link Problem} associated with this node.
     *
     * @return the problem.
     */
    @NotNull
    public Problem getProblem() {
        return problem;
    }

    @Override
    @NotNull
    public String getDescription() {
        return CheckStyleBundle.message("plugin.results.node.problem", problem.message(),
                                        problem.line(), Integer.toString(problem.column()),
                                        problem.sourceCheck());
    }
}
