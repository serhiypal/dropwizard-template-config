package de.thomaskrille.dropwizard_template_config;

import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Dropwizard {@link io.dropwizard.Bundle} that wraps the currently configured
 * {@link io.dropwizard.configuration.ConfigurationSourceProvider} with a
 * {@link de.thomaskrille.dropwizard_template_config.TemplateConfigurationSourceProvider}
 * that allows you to write your {@code config.yaml} as a
 * <a href="http://freemarker.org/">Freemarker</a> template.
 */
public class TemplateConfigBundle implements Bundle {

    private final TemplateConfigBundleConfiguration configuration;

    /**
     * Create a {@link TemplateConfigBundle} using the default configuration.
     */
    public TemplateConfigBundle() {
        this(new TemplateConfigBundleConfiguration());
    }

    /**
     * Create a {@link TemplateConfigBundle} using the given {@link TemplateConfigBundleConfiguration}.
     *
     * @param configuration The configuration for the new bundle. See {@link TemplateConfigBundleConfiguration}.
     */
    public TemplateConfigBundle(final TemplateConfigBundleConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void initialize(final Bootstrap<?> bootstrap) {
        bootstrap.setConfigurationSourceProvider(new TemplateConfigurationSourceProvider(
                bootstrap.getConfigurationSourceProvider(),
                Providers.fromEnvironmentProperties(),
                Providers.fromSystemProperties(),
                configuration
        ));
    }

    @Override
    public void run(final Environment environment) {
        // intentionally left empty
    }

}
