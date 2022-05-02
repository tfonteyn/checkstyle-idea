package org.infernus.idea.checkstyle.build;

import java.io.File;
import java.util.Set;

import groovy.lang.Closure;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.testing.Test;
import org.gradle.language.base.plugins.LifecycleBasePlugin;


/**
 * Gradle task that runs the unit tests in 'csaccessTest' against one of the supported Checkstyle versions.
 */
public class CsaccessTestTask
    extends Test {
    public static final String XTEST_GROUP_NAME = "xtest";
    public static final String XTEST_TASK_NAME = "xtest";

    public static final String NAME = "runCsaccessTests";

    public static final String CSVERSION_SYSPROP_NAME = "org.infernus.idea.checkstyle.version";

    private String csVersion = null;


    public CsaccessTestTask() {
        super();
        final Project project = getProject();
        final JavaPluginExtension jpc = project.getExtensions().getByType(JavaPluginExtension.class);
        final SourceSet csaccessTestSourceSet = jpc.getSourceSets().getByName(CustomSourceSetCreator
                .CSACCESSTEST_SOURCESET_NAME);

        dependsOn(project.getTasks().getByName(csaccessTestSourceSet.getClassesTaskName()));

        GradlePluginMain.configureTestTask(this);
        setTestClassesDirs(csaccessTestSourceSet.getOutput().getClassesDirs());
    }


    public static String getTaskName(final String pCheckstyleVersion) {
        return "xtest_" + CheckstyleVersions.toGradleVersion(pCheckstyleVersion);
    }


    public void setCheckstyleVersion(final String pCheckstyleVersion, final boolean isBaseVersion) {
        csVersion = pCheckstyleVersion;
        setDescription("Runs the '" + CustomSourceSetCreator.CSACCESSTEST_SOURCESET_NAME + "' unit tests against a "
                + "Checkstyle " + pCheckstyleVersion + " runtime.");
        getReports().getJunitXml().getRequired().set(false);
        if (isBaseVersion) {
            setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
            getReports().getHtml().getRequired().set(true);
        } else {
            setGroup(XTEST_GROUP_NAME);
            getReports().getHtml().getRequired().set(false);
        }

        // Make the Checkstyle version available to the test cases via a system property.
        configure(new Closure<Void>(this) {
            @Override
            public Void call() {
                systemProperty(CSVERSION_SYSPROP_NAME, pCheckstyleVersion);
                return null;
            }
        });
    }


    /**
     * Overriding getClasspath() in order to set the final classpath is an unusual solution, but it was the only
     * solution which included the classpath entries generated by the IntelliJ plugin creation plugin (which, in my
     * humble opinion, should be considered seriously broken).
     *
     * @return the classpath to use to execute the tests
     */
    @Override
    public FileCollection getClasspath() {

        final FileCollection originalClasspath = super.getClasspath();
        FileCollection effectiveClasspath = null;

        if (originalClasspath != null) {
            final Project project = getProject();
            final JavaPluginExtension jpc = project.getExtensions().getByType(JavaPluginExtension.class);
            final SourceSetContainer sourceSets = jpc.getSourceSets();
            final SourceSet mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
            final SourceSet testSourceSet = sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME);
            final SourceSet csaccessSourceSet = sourceSets.getByName(CustomSourceSetCreator.CSACCESS_SOURCESET_NAME);
            final SourceSet csaccessTestSrcSet = sourceSets.getByName(CustomSourceSetCreator
                    .CSACCESSTEST_SOURCESET_NAME);

            final Dependency csDep = CheckstyleVersions.createCheckstyleDependency(project, csVersion);
            final ConfigurationContainer configurations = project.getConfigurations();
            final Set<File> csJars = configurations.detachedConfiguration(csDep).getFiles();

            effectiveClasspath = project.files(
                    csaccessTestSrcSet.getOutput().getResourcesDir(),
                    csaccessSourceSet.getOutput().getResourcesDir(),
                    mainSourceSet.getOutput().getResourcesDir())
                .plus(csaccessTestSrcSet.getOutput().getClassesDirs())
                .plus(csaccessSourceSet.getOutput().getClassesDirs())
                .plus(mainSourceSet.getOutput().getClassesDirs())
                .plus(project.files(csJars))
                .plus(originalClasspath)
                .minus(testSourceSet.getOutput().getClassesDirs())
                .minus(project.files(testSourceSet.getOutput().getResourcesDir()));

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("--------------------------------------------------------------------------");
                getLogger().debug("Effective classpath of " + getName() + ":");
                for (File f : effectiveClasspath) {
                    getLogger().debug("\t- " + f.getAbsolutePath());
                }
            }
        }
        return effectiveClasspath;
    }
}
