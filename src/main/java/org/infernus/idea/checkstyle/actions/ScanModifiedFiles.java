package org.infernus.idea.checkstyle.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.infernus.idea.checkstyle.actions.ToolWindowAccess.toolWindow;

/**
 * Scan modified files.
 * <p/>
 * If the project is not setup to use VCS then no files will be scanned.
 */
public class ScanModifiedFiles extends BaseAction {

    private static final Logger LOG = Logger.getInstance(ScanModifiedFiles.class);

    @Override
    public final void actionPerformed(final @NotNull AnActionEvent event) {
        project(event).ifPresent(project -> {
            try {
                List<VirtualFile> affectedFiles = ChangeListManager.getInstance(project).getAffectedFiles();
                if (!affectedFiles.isEmpty()) {
                    staticScanner(project).asyncScanFiles(
                            affectedFiles,
                            getSelectedOverride(toolWindow(project)));
                }
            } catch (Throwable e) {
                LOG.warn("Modified files scan failed", e);
            }
        });
    }

    @Override
    public void update(final @NotNull AnActionEvent event) {
        final Presentation presentation = event.getPresentation();

        project(event).ifPresentOrElse(project -> {
            try {
                presentation.setEnabled(!staticScanner(project).isScanInProgress());

            } catch (Throwable e) {
                LOG.warn("Button update failed.", e);
            }
        }, () -> presentation.setEnabled(false));
    }
}
