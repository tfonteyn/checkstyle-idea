package org.infernus.idea.checkstyle.toolwindow.nodes;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * The user object for a {@link PsiFile} branch node in the tool window.
 */
public class ResultTreeFileNode extends ResultTreeBranchNode {

    @NotNull
    private final PsiFile file;
    @NotNull
    private final String name;

    /**
     * Construct a {@link PsiFile} branch node.
     *
     * @param file the file.
     */
    public ResultTreeFileNode(@NotNull final PsiFile file) {
        this.file = file;

        final ItemPresentation presentation = this.file.getPresentation();
        if (presentation == null) {
            setIcon(file.getFileType().getIcon());
            name = file.getName();
        } else {
            setIcon(presentation.getIcon(false));
            name = Objects.requireNonNullElseGet(presentation.getPresentableText(), file::getName);
        }
    }

    /**
     * Get the {@link PsiFile} associated with this node.
     *
     * @return the file.
     */
    @NotNull
    public PsiFile getFile() {
        return file;
    }

    @Override
    @NotNull
    public String getDescription() {
        return getDescription(name);
    }
}
