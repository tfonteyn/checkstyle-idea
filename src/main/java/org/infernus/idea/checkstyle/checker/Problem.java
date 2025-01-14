package org.infernus.idea.checkstyle.checker;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.infernus.idea.checkstyle.CheckStyleBundle;
import org.infernus.idea.checkstyle.csapi.SeverityLevel;
import org.infernus.idea.checkstyle.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Problem implements Comparable<Problem> {
    private final PsiElement target;
    private final SeverityLevel severityLevel;
    private final int line;
    private final int column;
    private final String sourceName;
    private final String message;
    private final boolean afterEndOfLine;
    private final boolean suppressErrors;
    private final PsiFile file;

    public Problem(@NotNull final PsiElement target,
                   @NotNull final String message,
                   @NotNull final SeverityLevel severityLevel,
                   final int line,
                   final int column,
                   final String sourceName,
                   final boolean afterEndOfLine,
                   final boolean suppressErrors) {
        this.target = target;
        this.message = message;
        this.severityLevel = severityLevel;
        this.line = line;
        this.column = column;
        this.sourceName = sourceName;
        this.afterEndOfLine = afterEndOfLine;
        this.suppressErrors = suppressErrors;

        // Calling this in the constructor *should* make certain that the file is never 'null'
        // Tests have shown that "target.getContainingFile()" CAN return 'null' if used at a later time.
        // We presume this was after the user made a change to their project and thus made the file unavailable.
        // i.e. the meta-data was no longer valid, but still displayed in the ToolWindow.
        file = this.target.getContainingFile();
    }

    @NotNull
    public ProblemDescriptor toProblemDescriptor(final InspectionManager inspectionManager,
                                                 final boolean onTheFly) {
        return inspectionManager.createProblemDescriptor(target,
                                                         CheckStyleBundle.message("inspection.message", message()),
                                                         quickFixes(), problemHighlightType(), onTheFly, afterEndOfLine);
    }

    @Nullable
    private LocalQuickFix[] quickFixes() {
        if (sourceName != null) {
            return new LocalQuickFix[]{new SuppressForCheckstyleFix(shortenClassName(sourceName))};
        }
        return null;
    }

    @Nullable
    public Module getContainingModule() {
        return ModuleUtil.findModuleForFile(file);
    }

    /**
     * Get the containing file for this problem.
     * <p>
     * This <strong>can</strong> return {@code null}. See this class constructor notes for details.
     *
     * @return the file
     */
    @Nullable
    public PsiFile getContainingFile() {
        return file;
    }

    @NotNull
    public String message() {
        return message;
    }

    @NotNull
    public String sourceCheck() {
        if (sourceName != null) {
            return shortenClassName(sourceName);
        }
        return CheckStyleBundle.message("plugin.results.unknown-source");
    }

    @NotNull
    private String shortenClassName(@NotNull final String className) {
        final int lastPackageIndex = className.lastIndexOf(".");
        if (lastPackageIndex >= 0) {
            return className
                    .substring(lastPackageIndex + 1)
                    .replaceFirst("Check$", "");
        }
        return className;
    }

    @NotNull
    public SeverityLevel severityLevel() {
        return severityLevel;
    }

    public int line() {
        return line;
    }

    public int column() {
        return column;
    }

    @NotNull
    private ProblemHighlightType problemHighlightType() {
        if (!suppressErrors) {
            switch (severityLevel()) {
                case Error:
                    return ProblemHighlightType.GENERIC_ERROR;
                case Info:
                    return ProblemHighlightType.WEAK_WARNING;
                case Ignore:
                    return ProblemHighlightType.INFORMATION;
                case Warning:
                default:
                    return ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
            }
        }
        return ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
    }

    @Override
    public int compareTo(@NotNull final Problem other) {
        int lineComparison = Integer.compare(this.line, other.line);
        if (lineComparison == 0) {
            int columnComparison = Integer.compare(this.column, other.column);
            if (columnComparison == 0) {
                int severityComparison = -this.severityLevel.compareTo(other.severityLevel);
                if (severityComparison == 0) {
                    return Objects.compare(this.message, other.message);
                }
                return severityComparison;
            }
            return columnComparison;
        }
        return lineComparison;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Problem problem = (Problem) o;

        if (line != problem.line) {
            return false;
        }
        if (column != problem.column) {
            return false;
        }
        if (afterEndOfLine != problem.afterEndOfLine) {
            return false;
        }
        if (suppressErrors != problem.suppressErrors) {
            return false;
        }
        if (!target.equals(problem.target)) {
            return false;
        }
        if (severityLevel != problem.severityLevel) {
            return false;
        }
        return message.equals(problem.message);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(target, severityLevel, line, column,
                                      sourceName, message, afterEndOfLine, suppressErrors);
    }
}
