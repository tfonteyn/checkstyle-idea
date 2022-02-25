package org.infernus.idea.checkstyle.model;

import com.intellij.openapi.project.Project;
import com.intellij.packageDependencies.DependencyValidationManager;
import com.intellij.psi.search.scope.packageSet.NamedScope;
import com.intellij.psi.search.scope.packageSet.NamedScopeManager;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class NamedScopeHelper {

    public static Optional<NamedScope> getAnyScope(Project project) {
        final NamedScopeManager myLocalScopesManager = NamedScopeManager.getInstance(project);
        final DependencyValidationManager mySharedScopesManager = DependencyValidationManager.getInstance(project);

        return Stream.concat(
                        Arrays.stream(myLocalScopesManager.getScopes()),
                        Arrays.stream(mySharedScopesManager.getScopes()))
                .findAny();
    }

    @Nullable
    public static NamedScope getScopeById(Project project, String id) {
        //TODO local before shared or shared before local?
        final NamedScopeManager myLocalScopesManager = NamedScopeManager.getInstance(project);
        final DependencyValidationManager mySharedScopesManager = DependencyValidationManager.getInstance(project);

        final NamedScope sharedScopeOrNull = mySharedScopesManager.getScope(id);

        return sharedScopeOrNull != null ? sharedScopeOrNull : myLocalScopesManager.getScope(id);
    }

}