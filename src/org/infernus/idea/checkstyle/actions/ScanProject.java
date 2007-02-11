package org.infernus.idea.checkstyle.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.infernus.idea.checkstyle.CheckStylePlugin;
import org.infernus.idea.checkstyle.CheckStyleConstants;
import org.infernus.idea.checkstyle.toolwindow.ToolWindowPanel;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Action to execute a CheckStyle scan on the current project.
 *
 * @author James Shiell
 * @version 1.0
 */
public class ScanProject extends BaseAction {


    /**
     * {@inheritDoc}
     */
    public void actionPerformed(final AnActionEvent event) {
        final Project project = (Project) event.getDataContext().getData(
                DataConstants.PROJECT);
        if (project == null) {
            return;
        }

        final CheckStylePlugin checkStylePlugin
                = project.getComponent(CheckStylePlugin.class);
        if (checkStylePlugin == null) {
            throw new IllegalStateException("Couldn't get checkstyle plugin");
        }

        final ToolWindow toolWindow = ToolWindowManager.getInstance(
                project).getToolWindow(checkStylePlugin.getToolWindowId());
        toolWindow.activate(null);

        // show progress text
        final ResourceBundle resources = ResourceBundle.getBundle(
                CheckStyleConstants.RESOURCE_BUNDLE);
        final String progressText = resources.getString(
                "plugin.status.in-progress.project");
        ((ToolWindowPanel) toolWindow.getComponent()).setProgressText(
                progressText);

        // find project files
        ProjectRootManager projectRootManager
                = ProjectRootManager.getInstance(project);
        final VirtualFile[] sourceRoots
                = projectRootManager.getContentSourceRoots();

        if (sourceRoots != null && sourceRoots.length > 0) {
            project.getComponent(CheckStylePlugin.class).checkFiles(
                    flattenFiles(sourceRoots), event);
        }
    }

    /**
     * Flatten a nested list of files, returning all files in the array.
     *
     * @param files the top level of files.
     * @return the flattened list of files.
     */
    private List<VirtualFile> flattenFiles(final VirtualFile[] files) {
        final List<VirtualFile> flattened = new ArrayList<VirtualFile>();

        if (files != null) {
            for (final VirtualFile file : files) {
                flattened.add(file);

                if (file.getChildren() != null) {
                    flattened.addAll(flattenFiles(file.getChildren()));
                }
            }
        }

        return flattened;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(final AnActionEvent event) {
        super.update(event);

        final Presentation presentation = event.getPresentation();

        final Project project = (Project) event.getDataContext().getData(
                DataConstants.PROJECT);
        if (project == null) { // check if we're loading...
            presentation.setEnabled(false);
            return;
        }

        final CheckStylePlugin checkStylePlugin
                = project.getComponent(CheckStylePlugin.class);
        if (checkStylePlugin == null) {
            throw new IllegalStateException("Couldn't get checkstyle plugin");
        }

        ProjectRootManager projectRootManager
                = ProjectRootManager.getInstance(project);
        final VirtualFile[] sourceRoots
                = projectRootManager.getContentSourceRoots();

        // disable if no files are selected
        if (sourceRoots == null || sourceRoots.length == 0) {
            presentation.setEnabled(false);

        } else {
            presentation.setEnabled(!checkStylePlugin.isScanInProgress());
        }


    }
}