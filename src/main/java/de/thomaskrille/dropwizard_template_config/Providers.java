package de.thomaskrille.dropwizard_template_config;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class with creating methods for commonly used {@link TemplateConfigVariablesProvider}
 */
public class Providers {

    /**
     * Namespace for environment variables
     */
    public static final String ENV_NAMESPACE = "env";

    /**
     * Namespace for system variables
     */
    public static final String SYS_NAMESPACE = "sys";

    private Providers() {}

    /**
     * Creates {@link TemplateConfigVariablesProvider} with map of system properties
     * @return provider with system properties
     */
    public static TemplateConfigVariablesProvider fromSystemProperties() {
        return fromProperties(SYS_NAMESPACE, System.getProperties());
    }

    /**
     * Creates {@link TemplateConfigVariablesProvider} with map of environment properties
     * @return provider with environment properties
     */
    public static TemplateConfigVariablesProvider fromEnvironmentProperties() {
        return new MapVariablesProvider(ENV_NAMESPACE, System.getenv());
    }

    /**
     * Wraps a map into a {@link TemplateConfigVariablesProvider} with given namespace
     * @param namespace to use for provider
     * @param variables to wrap into provider
     * @return provider with given namespace and map
     */
    public static TemplateConfigVariablesProvider fromMap(String namespace, Map<String, String> variables) {
        return new MapVariablesProvider(namespace, Objects.requireNonNull(variables));
    }

    /**
     * Converts properties into a {@link TemplateConfigVariablesProvider} with given namespace
     * @param namespace to use for provider
     * @param properties to wrap into provider
     * @return provider with given namespace and map
     */
    public static TemplateConfigVariablesProvider fromProperties(String namespace, Properties properties) {
        return fromMap(namespace, Objects.requireNonNull(properties)
                                         .stringPropertyNames()
                                         .stream()
                                         .collect(Collectors.toMap(Function.identity(), System::getProperty)));
    }

    /**
     * Converts properties into a {@link TemplateConfigVariablesProvider} with given namespace
     * @param namespace to use for provider
     * @param propertiesURL URL to properties to wrap into provider
     * @return provider with given namespace and map
     */
    public static TemplateConfigVariablesProvider fromProperties(String namespace, URL propertiesURL) {
        try {
            Properties properties = new Properties();
            properties.load(Objects.requireNonNull(propertiesURL).openStream());
            return fromProperties(namespace, properties);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read properties from URL: " + propertiesURL, e);
        }
    }

    /**
     * Adapting variables from {@link Map} and {@link Properties}.
     * Namespace cannot be blank and variables cannot be null
     */
    private static class MapVariablesProvider implements TemplateConfigVariablesProvider {

        private final String namespace;

        private final Map<String, String> variables;

        /**
         * Instantiate provider with map as a source
         * @param namespace name space to return out of the provider
         * @param variables Map of variables to return out the provider
         */
        private MapVariablesProvider(String namespace, Map<String, String> variables) {
            this.namespace = Objects.requireNonNull(namespace, "Namespace cannot be null.");
            if (namespace.trim().isEmpty()) {
                throw new IllegalArgumentException("Namespace cannot be blank.");
            }
            this.variables = Collections.unmodifiableMap(Objects.requireNonNull(variables));
        }

        @Override
        public String getNamespace() {
            return namespace;
        }

        @Override
        public Map<String, String> getVariables() {
            return variables;
        }

    }
}
