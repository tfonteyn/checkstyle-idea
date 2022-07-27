package org.infernus.idea.checkstyle.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareToggleAction;
import com.intellij.openapi.project.Project;
import org.infernus.idea.checkstyle.toolwindow.ResultTreeModel;
import org.jetbrains.annotations.NotNull;

import static org.infernus.idea.checkstyle.actions.ToolWindowAccess.actOnToolWindowPanel;
import static org.infernus.idea.checkstyle.actions.ToolWindowAccess.getFromToolWindowPanel;
import static org.infernus.idea.checkstyle.actions.ToolWindowAccess.toolWindow;

public class GroupByModule extends DumbAwareToggleAction {

    @Override
    public boolean isSelected(@NotNull final AnActionEvent event) {
        final Project project = getEventProject(event);
        if (project == null) {
            return false;
        }

        final Boolean selected = getFromToolWindowPanel(toolWindow(project), panel ->
                panel.isGroupedBy(ResultTreeModel.GroupedBy.Module));
        if (selected != null) {
            return selected;
        }
        return false;
    }

    @Override
    public void setSelected(@NotNull final AnActionEvent event, final boolean selected) {
        final Project project = getEventProject(event);
        if (project == null) {
            return;
        }

        actOnToolWindowPanel(toolWindow(project), panel -> {
            if (selected) {
                panel.setGroupedBy(ResultTreeModel.GroupedBy.Module);
            }
        });
    }
}
