package org.infernus.idea.checkstyle.toolwindow;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.infernus.idea.checkstyle.CheckStyleBundle;
import org.infernus.idea.checkstyle.checker.Problem;
import org.infernus.idea.checkstyle.csapi.SeverityLevel;
import org.infernus.idea.checkstyle.toolwindow.nodes.ResultTreeBranchNode;
import org.infernus.idea.checkstyle.toolwindow.nodes.ResultTreeFileNode;
import org.infernus.idea.checkstyle.toolwindow.nodes.ResultTreeNode;
import org.infernus.idea.checkstyle.toolwindow.nodes.ResultTreeProblemNode;
import org.infernus.idea.checkstyle.toolwindow.nodes.ResultTreeRootNode;
import org.infernus.idea.checkstyle.toolwindow.nodes.ResultTreeSeverityNode;
import org.infernus.idea.checkstyle.toolwindow.nodes.ResultTreeTextNode;
import org.infernus.idea.checkstyle.util.Icons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ResultTreeModel extends DefaultTreeModel {

    private static final long serialVersionUID = -2822412125029378656L;

    @NotNull
    private final TogglableTreeNode visibleRootNode;
    private int maxNodeDepth;

    public ResultTreeModel() {
        super(new DefaultMutableTreeNode());

        visibleRootNode = new TogglableTreeNode(new ResultTreeRootNode());
        ((DefaultMutableTreeNode) getRoot()).add(visibleRootNode);
    }

    public void clear() {
        visibleRootNode.removeAllChildren();
        final ResultTreeRootNode nodeInfo = (ResultTreeRootNode) visibleRootNode.getUserObject();
        nodeInfo.setDefaultText();
        nodeStructureChanged(visibleRootNode);
    }

    @NotNull
    public TreeNode getVisibleRoot() {
        return visibleRootNode;
    }

    /**
     * Get the maximum node depth.
     * This is only valid after a call to {@link #setModel}.
     *
     * @return the depth
     */
    public int getMaxNodeDepth() {
        return maxNodeDepth;
    }

    /**
     * Set the root node text.
     * <p>
     * This will trigger a reload on the model, thanks to JTree's lack of support for
     * a node changed event for the root node.
     *
     * @param text the text to display.
     * @param icon the icon to display or {@code null} for none.
     */
    public void setRootText(@NotNull final String text, @Nullable final Icon icon) {
        final ResultTreeRootNode root = (ResultTreeRootNode) visibleRootNode.getUserObject();
        root.setText(text);
        root.setIcon(icon);

        nodeChanged(visibleRootNode);
    }

    /**
     * Set the root node message.
     * <p>
     * This will trigger a reload on the model, thanks to JTree's lack of support for
     * a node changed event for the root node.
     *
     * @param messageKey  the message key to display.
     * @param icon        the icon to display; {@code null} for none.
     * @param messageArgs arguments for the message; {@code null} for none.
     */
    public void setRootMessage(@NotNull final String messageKey,
                               @Nullable final Icon icon,
                               @Nullable final Object... messageArgs) {
        final ResultTreeRootNode root = (ResultTreeRootNode) visibleRootNode.getUserObject();
        root.setText(CheckStyleBundle.message(messageKey, messageArgs));
        root.setIcon(icon);

        nodeChanged(visibleRootNode);
    }

    /**
     * Display only the passed severity levels.
     *
     * @param levels the levels.
     */
    public void filter(@NotNull final List<SeverityLevel> levels) {
        filter(true, levels);
    }

    private void filter(final boolean sendEvents, @NotNull final List<SeverityLevel> levels) {
        final Set<TogglableTreeNode> changedNodes = visibleRootNode.getAllChildren().stream()
                .filter(rootNode -> filter(rootNode, levels))
                .collect(Collectors.toSet());

        if (sendEvents) {
            changedNodes.forEach(this::nodeStructureChanged);
        }
    }

    /**
     * Recursively adjust visibility based on the SeverityLevel.
     *
     * @param parentNode the node
     * @param levels     the levels.
     * @return {@code true} if at least one node changed visibility
     */
    private boolean filter(@NotNull final TogglableTreeNode parentNode,
                           @NotNull final List<SeverityLevel> levels) {
        boolean changed = false;

        for (TogglableTreeNode node : parentNode.getAllChildren()) {
            final ResultTreeNode result = node.getUserObject();
            final boolean desiredVisible;
            if (result instanceof ResultTreeProblemNode) {
                desiredVisible = levels.contains(((ResultTreeProblemNode) result).getProblem().severityLevel());
            } else {
                // It's a branch node.
                if (filter(node, levels)) {
                    changed = true;
                }
                desiredVisible = node.getAllChildren().stream().anyMatch(TogglableTreeNode::isVisible);
            }

            if (node.isVisible() != desiredVisible) {
                node.setVisible(desiredVisible);
                changed = true;
            }
        }

        return changed;
    }

    /**
     * Set the displayed model.
     *
     * @param results         the model.
     * @param groupedBy       how to group the nodes
     * @param flattenModules  {@code true} to use flat Module names, or {@code false} to show a hierarchy.
     * @param flattenPackages {@code true} to use flat Package names, or {@code false} to show a hierarchy.
     * @param levels          the levels to display.
     */
    public void setModel(@Nullable final Map<PsiFile, List<Problem>> results,
                         @NotNull final GroupedBy groupedBy,
                         final boolean flattenModules,
                         final boolean flattenPackages,
                         @NotNull final List<SeverityLevel> levels) {

        visibleRootNode.removeAllChildren();
        final ResultTreeRootNode root = (ResultTreeRootNode) visibleRootNode.getUserObject();
        if (results != null && !results.isEmpty()) {
            switch (groupedBy) {
                case File:
                    createProblemNodes(results, groupByFile(visibleRootNode));
                    break;
                case Module:
                    createProblemNodes(results, groupByModule(visibleRootNode, flattenModules, flattenPackages));
                    break;
                case SourceCheck:
                    createProblemNodes(results, groupByCheck(visibleRootNode, flattenModules, flattenPackages));
                    break;
                case Severity:
                    createProblemNodes(results, groupBySeverity(visibleRootNode, flattenModules, flattenPackages));
                    break;
            }

            root.setTotalResults(results.size());
        } else {
            root.setDefaultText();
        }

        filter(false, levels);
        nodeStructureChanged(visibleRootNode);
    }

    /**
     * Create the parent-node supplier function when grouping by {@link PsiFile}.
     * <p>
     * visibleRootNode -> Class/File -> Problem.
     *
     * @see #createProblemNodes(Map, Function)
     */
    @NotNull
    private Function<Problem, TogglableTreeNode> groupByFile(@NotNull final TogglableTreeNode rootNode) {
        return problem -> getOrCreateFileNode(rootNode, problem);
    }

    /**
     * Create the parent-node supplier function when grouping by {@link PsiFile}.
     * <p>
     * visibleRootNode -> Module -> (flat)Package -> Class/File -> Problem.
     */
    @NotNull
    private Function<Problem, TogglableTreeNode> groupByModule(@NotNull final TogglableTreeNode rootNode,
                                                               final boolean flattenModules,
                                                               final boolean flattenPackages) {
        return problem -> getOrCreateProblemParentNode(rootNode, problem, flattenModules, flattenPackages);
    }

    /**
     * Create the parent-node supplier function when grouping by {@link PsiFile}.
     * <p>
     * visibleRootNode -> Check -> Module -> (flat)Package -> Class/File -> Problem.
     *
     * @see #createProblemNodes(Map, Function)
     */
    @NotNull
    private Function<Problem, TogglableTreeNode> groupByCheck(@NotNull final TogglableTreeNode rootNode,
                                                              final boolean flattenModules,
                                                              final boolean flattenPackages) {
        return problem -> {
            final TogglableTreeNode node = getOrCreateTextNode(rootNode, Icons.CS, problem.sourceCheck());
            return getOrCreateProblemParentNode(node, problem, flattenModules, flattenPackages);
        };
    }

    /**
     * Create the parent-node supplier function when grouping by {@link SeverityLevel}.
     * <p>
     * visibleRootNode -> SeverityLevel -> Module -> (flat)Package -> Class/File -> Problem.
     *
     * @see #createProblemNodes(Map, Function)
     */
    @NotNull
    private Function<Problem, TogglableTreeNode> groupBySeverity(@NotNull final TogglableTreeNode rootNode,
                                                                 final boolean flattenModules,
                                                                 final boolean flattenPackages) {
        return problem -> {
            final TogglableTreeNode node = getOrCreateSeverityNode(rootNode, problem);
            return getOrCreateProblemParentNode(node, problem, flattenModules, flattenPackages);
        };
    }


    /**
     * Find or create the appropriate {@link SeverityLevel} node attached to the given parent.
     *
     * @param parentNode to search
     * @param problem    to find or create the appropriate node for.
     * @return the node
     */
    @NotNull
    private TogglableTreeNode getOrCreateSeverityNode(@NotNull final TogglableTreeNode parentNode,
                                                      @NotNull final Problem problem) {
        final SeverityLevel severityLevel = problem.severityLevel();

        return parentNode.getAllChildren().stream()
                .filter(node -> node.getUserObject() instanceof ResultTreeSeverityNode)
                .filter(node -> severityLevel.equals(((ResultTreeSeverityNode) node.getUserObject()).getSeverityLevel()))
                .findFirst().orElseGet(() -> insertNewNode(parentNode, new ResultTreeSeverityNode(severityLevel)));
    }

    /**
     * Find or create the appropriate module (text) node attached to the given parent.
     *
     * @param parentNode to search
     * @param problem    to find or create the appropriate node for.
     * @param flat       {@code true} to use flat (dotted) names, or {@code false} to show a hierarchy.
     * @return the node
     */
    @NotNull
    private TogglableTreeNode getOrCreateModuleNode(@NotNull final TogglableTreeNode parentNode,
                                                    final @NotNull Problem problem,
                                                    final boolean flat) {

        final Module module = problem.getContainingModule();
        if (module == null) {
            return parentNode;
        }

        final String flatName = module.getName();
        if (flat) {
            return getOrCreateTextNode(parentNode, AllIcons.Nodes.Module, flatName);
        } else {
            TogglableTreeNode parent = parentNode;
            for (final String name : flatName.split("\\.")) {
                parent = getOrCreateTextNode(parent, AllIcons.Nodes.Module, name);
            }
            return parent;
        }
    }

    /**
     * Find or create the appropriate package (text) node attached to the given parent.
     *
     * @param parentNode to search
     * @param problem    to find or create the appropriate node for.
     * @param flat       {@code true} to use flat (dotted) names, or {@code false} to show a hierarchy.
     * @return the node
     */
    @NotNull
    private TogglableTreeNode getOrCreatePackageNodes(@NotNull final TogglableTreeNode parentNode,
                                                      final @NotNull Problem problem,
                                                      final boolean flat) {

        final PsiFile file = problem.getContainingFile();
        if (!(file instanceof PsiJavaFile)) {
            return parentNode;
        }

        final String flatName = ((PsiJavaFile) file).getPackageName();
        if (flat) {
            return getOrCreateTextNode(parentNode, AllIcons.Nodes.Package, flatName);
        } else {
            TogglableTreeNode parent = parentNode;
            for (final String name : flatName.split("\\.")) {
                parent = getOrCreateTextNode(parent, AllIcons.Nodes.Package, name);
            }
            return parent;
        }
    }

    /**
     * Find or create the appropriate plain text node attached to the given parent.
     * <p>
     * Warning: the icon parameter is used as the <strong>type of the node</strong>.
     * i.e. when searching for a node <strong>both the text and the icon</strong> must match.
     *
     * @param parentNode to search
     * @param icon       to use for new nodes / type of this node
     * @param text       the text which will be used to find or create the appropriate node.
     * @return the node
     */
    @NotNull
    private TogglableTreeNode getOrCreateTextNode(@NotNull final TogglableTreeNode parentNode,
                                                  @NotNull final Icon icon,
                                                  @NotNull final String text) {
        return parentNode.getAllChildren().stream()
                .filter(node -> icon.equals(node.getUserObject().getIcon()))
                .filter(node -> node.getUserObject() instanceof ResultTreeTextNode)
                .filter(node -> text.equals(((ResultTreeTextNode) node.getUserObject()).getText()))
                .findFirst()
                .orElseGet(() -> insertNewNode(parentNode, new ResultTreeTextNode(text, icon)));
    }

    /**
     * Find or create the appropriate {@link PsiFile} node attached to the given parent.
     *
     * @param parentNode  to search
     * @param problem     to find or create the appropriate node for.
     * @return the node
     */
    @NotNull
    private TogglableTreeNode getOrCreateFileNode(@NotNull final TogglableTreeNode parentNode,
                                                  final @NotNull Problem problem) {
        final PsiFile file = problem.getContainingFile();
        if (file == null) {
            return parentNode;
        }

        return parentNode.getAllChildren().stream()
                .filter(node -> node.getUserObject() instanceof ResultTreeFileNode)
                .filter(node -> file.equals(((ResultTreeFileNode) node.getUserObject()).getFile()))
                .findFirst()
                .orElseGet(() -> insertNewNode(parentNode, new ResultTreeFileNode(file)));
    }

    /**
     * Insert the given {@link ResultTreeNode} as a new {@link TogglableTreeNode} as a child of the parentNode.
     * We'll group package nodes at the top of the children list followed by other nodes.
     *
     * @param parentNode     to attach the new child to
     * @param resultTreeNode to create a new child for
     * @return the new child
     */
    private TogglableTreeNode insertNewNode(@NotNull final TogglableTreeNode parentNode,
                                            @NotNull final ResultTreeNode resultTreeNode) {

        final TogglableTreeNode treeNode = new TogglableTreeNode(resultTreeNode);
        parentNode.add(treeNode);

        final Icon nodeType = AllIcons.Nodes.Package;

        if (nodeType.equals(resultTreeNode.getIcon())) {
            // (re)sort the package nodes and (re)insert them at the start of the list
            parentNode.getAllChildren().stream()
                    .filter(node -> node.getUserObject() instanceof ResultTreeTextNode)
                    .filter(node -> nodeType.equals(node.getUserObject().getIcon()))
                    // sort Z-A
                    .sorted(Comparator.comparing(
                                    node -> ((ResultTreeTextNode) (((TogglableTreeNode) node).getUserObject()))
                                            .getText())
                                    .reversed())
                    // and then re-inserted at the top, so we'll end up with A-Z packages followed by the other nodes.
                    .forEach(node -> parentNode.insert(node, 0));
        }
        return treeNode;
    }

    /**
     * Find or create the appropriate node attached to the given parent where
     * we will attach the {@link Problem} node to.
     *
     * @param rootNode        to search
     * @param problem         to use for finding the appropriate node
     * @param flattenModules  {@code true} to use flat Module names, or {@code false} to show a hierarchy.
     * @param flattenPackages {@code true} to use flat Package names, or {@code false} to show a hierarchy.
     * @return the node
     */
    @NotNull
    private TogglableTreeNode getOrCreateProblemParentNode(@NotNull final TogglableTreeNode rootNode,
                                                           @NotNull final Problem problem,
                                                           final boolean flattenModules,
                                                           final boolean flattenPackages) {
        final TogglableTreeNode moduleNode = getOrCreateModuleNode(rootNode, problem, flattenModules);
        final TogglableTreeNode packageNode = getOrCreatePackageNodes(moduleNode, problem, flattenPackages);
        return getOrCreateFileNode(packageNode, problem);
    }

    /**
     * Create all {@link Problem} nodes and attach them under the appropriate parent.
     *
     * @param results            the model.
     * @param parentNodeSupplier determines and supplies to actual parent node to attach a problem to
     */
    private void createProblemNodes(@NotNull final Map<PsiFile, List<Problem>> results,
                                    @NotNull final Function<Problem, TogglableTreeNode> parentNodeSupplier) {

        results.values().stream().flatMap(Collection::stream)
                .filter(problem -> problem.severityLevel() != SeverityLevel.Ignore)
                // see Problem class constructor for details on nullability
                .filter(problem -> problem.getContainingFile() != null)
                .sorted(Comparator.comparing(problem -> problem.getContainingFile().getName()))
                .forEach(problem -> {
                    final TogglableTreeNode problemNode = new TogglableTreeNode(new ResultTreeProblemNode(problem));
                    TogglableTreeNode parentNode = parentNodeSupplier.apply(problem);
                    parentNode.add(problemNode);

                    if (problemNode.getLevel() > maxNodeDepth) {
                        maxNodeDepth = problemNode.getLevel();
                    }

                    // adjust the item-count of all parent nodes
                    do {
                        ((ResultTreeBranchNode) (parentNode.getUserObject())).increaseItemCount();
                        final TreeNode parent = parentNode.getParent();
                        // quit going up when we get to our root.
                        if (parent instanceof TogglableTreeNode) {
                            parentNode = (TogglableTreeNode) parent;
                        } else {
                            parentNode = null;
                        }
                    } while (parentNode != null);
                });
    }

    public enum GroupedBy {
        File, Module, SourceCheck, Severity,
    }
}
