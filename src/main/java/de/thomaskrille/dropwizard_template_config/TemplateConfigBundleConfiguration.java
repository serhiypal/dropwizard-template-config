package de.thomaskrille.dropwizard_template_config;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * The configuration for a {@link TemplateConfigBundle}
 */
public class TemplateConfigBundleConfiguration {

    private Charset charset = StandardCharsets.UTF_8;
    private String resourceIncludePath;
    private String fileIncludePath;
    private String outputPath;
    private Set<TemplateConfigVariablesProvider> customProviders = new LinkedHashSet<>();

    /**
     * Get the configured charset (Default: UTF-8)
     */
    public Charset charset() {
        return charset;
    }

    /**
     * Set the {@link Charset} used to load, process, and output the config template
     *
     * <p>The default is UTF-8.
     */
    public TemplateConfigBundleConfiguration charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * Get the configured resource include path (Default: None)
     */
    public Optional<String> resourceIncludePath() {
        return Optional.ofNullable(resourceIncludePath);
    }

    /**
     * Get the configured file include path (Default: None)
     */
    public Optional<String> fileIncludePath() {
        return Optional.ofNullable(fileIncludePath);
    }

    /**
     * Get the configured output path for the processed config (Default: None)
     */
    public Optional<String> outputPath() {
        return Optional.ofNullable(outputPath);
    }

    /**
     * Get the set of custom providers used to add variables to the configuration template (Default: Empty Set)
     */
    public Set<TemplateConfigVariablesProvider> customProviders() {
        return customProviders;
    }

    /**
     * Set the resource path to include config snippets from
     *
     * <p>Must not be {@code null}. By default there's no value set.
     * Only one of {@code resourceIncludePath} or {@code fileIncludePath}
     * may be specified.
     *
     * @throws IllegalStateException if fileIncludePath is set
     */
    public TemplateConfigBundleConfiguration resourceIncludePath(String path) {
        if (fileIncludePath != null) {
            throw new IllegalStateException(
                "A value for fileIncludePath is already present; " +
                "only one of resourceIncludePath or fileIncludePath may be specified."
            );
        }
        this.resourceIncludePath = path;
        return this;
    }

    /**
     * Set the file path to include config snippets from
     *
     * <p>Must not be {@code null}. By default there's no value set.
     * Only one of {@code resourceIncludePath} or {@code fileIncludePath}
     * may be specified.
     *
     * @throws IllegalStateException if resourceIncludePath is already set
     */
    public TemplateConfigBundleConfiguration fileIncludePath(String path) {
        if (resourceIncludePath != null) {
            throw new IllegalStateException(
                "A value for resourceIncludePath is already present; " +
                "only one of resourceIncludePath or fileIncludePath may be specified."
            );
        }
        this.fileIncludePath = path;
        return this;
    }

    /**
     * Set the path to output the filled-out config
     *
     * <p>Must not be {@code null}. By default there's no value set.
     */
    public TemplateConfigBundleConfiguration outputPath(String outputPath) {
        this.outputPath = outputPath;
        return this;
    }

    /**
     * Add a custom provider used to add your own variables to the configuration template.
     */
    public TemplateConfigBundleConfiguration addCustomProvider(TemplateConfigVariablesProvider customProvider) {
        this.customProviders.add(customProvider);
        return this;
    }
}
