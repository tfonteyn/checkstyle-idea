package org.infernus.idea.checkstyle.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import org.infernus.idea.checkstyle.CheckStylePlugin;
import org.infernus.idea.checkstyle.model.ConfigurationLocation;
import org.infernus.idea.checkstyle.util.ProjectFilePaths;
import org.infernus.idea.checkstyle.util.Strings;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static org.infernus.idea.checkstyle.config.PluginConfigurationBuilder.defaultConfiguration;

@State(name = CheckStylePlugin.ID_PLUGIN, storages = {@Storage("checkstyle-idea.xml")})
public class ProjectConfigurationState implements PersistentStateComponent<ProjectConfigurationState.ProjectSettings> {

    static final String ACTIVE_CONFIG = "active-configuration";
    static final String ACTIVE_CONFIGS_PREFIX = ACTIVE_CONFIG + "-";
    static final String CHECKSTYLE_VERSION_SETTING = "checkstyle-version";
    static final String SCANSCOPE_SETTING = "scanscope";
    static final String SUPPRESS_ERRORS = "suppress-errors";
    static final String COPY_LIBS = "copy-libs";
    static final String THIRDPARTY_CLASSPATH = "thirdparty-classpath";
    static final String SCAN_BEFORE_CHECKIN = "scan-before-checkin";
    static final String LOCATION_PREFIX = "location-";
    static final String PROPERTIES_PREFIX = "property-";

    private final Project project;

    private ProjectSettings projectSettings;

    public ProjectConfigurationState(@NotNull final Project project) {
        this.project = project;

        projectSettings = defaultProjectSettings();
    }

    private ProjectFilePaths projectFilePaths() {
        return ServiceManager.getService(project, ProjectFilePaths.class);
    }

    private ProjectSettings defaultProjectSettings() {
        return ProjectSettings.create(projectFilePaths(), defaultConfiguration(project).build());
    }

    public ProjectSettings getState() {
        return projectSettings;
    }

    public void loadState(@NotNull final ProjectSettings sourceProjectSettings) {
        projectSettings = sourceProjectSettings;
    }

    @NotNull
    PluginConfigurationBuilder populate(@NotNull final PluginConfigurationBuilder builder) {
        Map<String, Object> projectConfiguration = projectSettings.configuration();
        return selectDeserialiser().deserialise(builder, projectConfiguration);
    }

    @NotNull
    private V1ProjectConfigurationStateDeserialiser selectDeserialiser() {
        return new V1ProjectConfigurationStateDeserialiser(project);
    }

    void setCurrentConfig(@NotNull final PluginConfiguration currentPluginConfig) {
        projectSettings = ProjectSettings.create(projectFilePaths(), currentPluginConfig);
    }

    static class ProjectSettings {
        @MapAnnotation
        private Map<String, Object> configuration;

        static ProjectSettings create(@NotNull final ProjectFilePaths projectFilePaths,
                                      @NotNull final PluginConfiguration currentPluginConfig) {
            final Map<String, Object> mapForSerialization = new TreeMap<>();
            mapForSerialization.put(CHECKSTYLE_VERSION_SETTING, currentPluginConfig.getCheckstyleVersion());
            mapForSerialization.put(SCANSCOPE_SETTING, currentPluginConfig.getScanScope().name());
            mapForSerialization.put(SUPPRESS_ERRORS, String.valueOf(currentPluginConfig.isSuppressErrors()));
            mapForSerialization.put(COPY_LIBS, String.valueOf(currentPluginConfig.isCopyLibs()));

            serializeLocations(mapForSerialization, new ArrayList<>(currentPluginConfig.getLocations()));

            String s = serializeThirdPartyClasspath(projectFilePaths, currentPluginConfig.getThirdPartyClasspath());
            if (!Strings.isBlank(s)) {
                mapForSerialization.put(THIRDPARTY_CLASSPATH, s);
            }

            mapForSerialization.put(SCAN_BEFORE_CHECKIN, String.valueOf(currentPluginConfig.isScanBeforeCheckin()));

            serializeActiveLocations(mapForSerialization, new ArrayList<>(currentPluginConfig.getActiveLocationDescriptors()));

            final ProjectSettings projectSettings = new ProjectSettings();
            projectSettings.configuration = mapForSerialization;
            return projectSettings;
        }

        private static void serializeActiveLocations(final Map<String, Object> storage,
                                                     final List<String> activeLocationDescriptors) {
            for (int i = 0; i < activeLocationDescriptors.size(); i++) {
                storage.put(ACTIVE_CONFIGS_PREFIX + i, activeLocationDescriptors.get(i));
            }
        }

        @NotNull
        public Map<String, Object> configuration() {
            return Objects.requireNonNullElseGet(configuration, TreeMap::new);
        }

        private static void serializeLocations(@NotNull final Map<String, Object> storage,
                                               @NotNull final List<ConfigurationLocation> configurationLocations) {
            int index = 0;
            for (final ConfigurationLocation configurationLocation : configurationLocations) {
                storage.put(LOCATION_PREFIX + index, configurationLocation.getDescriptor());
                final Map<String, String> properties = configurationLocation.getProperties();
                if (properties != null) {
                    for (final Map.Entry<String, String> prop : properties.entrySet()) {
                        String value = prop.getValue();
                        if (value == null) {
                            value = "";
                        }
                        storage.put(PROPERTIES_PREFIX + index + "." + prop.getKey(), value);
                    }
                }

                ++index;
            }
        }

        private static String serializeThirdPartyClasspath(@NotNull final ProjectFilePaths projectFilePaths,
                                                           @NotNull final List<String> thirdPartyClasspath) {
            final StringBuilder sb = new StringBuilder();
            for (final String part : thirdPartyClasspath) {
                if (sb.length() > 0) {
                    sb.append(";");
                }
                sb.append(projectFilePaths.tokenise(part));
            }
            return sb.toString();
        }
    }

}
