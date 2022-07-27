package org.infernus.idea.checkstyle.toolwindow.nodes;

import com.intellij.icons.AllIcons;
import org.infernus.idea.checkstyle.CheckStyleBundle;
import org.jetbrains.annotations.NotNull;

/**
 * The user object for the <strong>single</strong> root node in the tool window.
 */
public class ResultTreeRootNode extends ResultTreeTextNode {

    /**
     * Construct the root node.
     */
    public ResultTreeRootNode() {
        super(CheckStyleBundle.message("plugin.results.no-scan"), AllIcons.General.Information);
    }

    /**
     * Reset the text, icon,... back to the default.
     */
    public void setDefaultText() {
        setText(CheckStyleBundle.message("plugin.results.no-scan"));
        setIcon(AllIcons.General.Information);
        setItemCount(0);
    }

    /**
     * Called when a scan is finished to update the total number of results.
     *
     * @param totalResults the total
     */
    public void setTotalResults(final int totalResults) {
        setText(CheckStyleBundle.message("plugin.results.scan-results", itemCount, totalResults));
        setIcon(AllIcons.General.Information);
    }

    @Override
    @NotNull
    public String getDescription() {
        return getText();
    }
}
