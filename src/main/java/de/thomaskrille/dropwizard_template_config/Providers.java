package de.thomaskrille.dropwizard_template_config;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Providers {

    private Providers() {}

    public static TemplateConfigVariablesProvider fromSystemProperties() {
        return new MapVariablesProvider("sys", System.getProperties()
                                                     .stringPropertyNames()
                                                     .stream()
                                                     .collect(Collectors.toMap(Function.identity(), System::getProperty)));
    }

    public static TemplateConfigVariablesProvider fromEnvironmentProperties() {
        return new MapVariablesProvider("env", System.getenv());
    }

    public static TemplateConfigVariablesProvider fromMap(String namespace, Map<String, String> variables) {
        return new MapVariablesProvider(namespace, variables);
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
