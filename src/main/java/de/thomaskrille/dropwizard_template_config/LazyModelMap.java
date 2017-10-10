package de.thomaskrille.dropwizard_template_config;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.ForwardingMap;

/**
 * Model map that instantiates internal map lazily upon request with no specific namespace.
 * Since no namespace means any value of internal providers all of them needs to be traversed and populated into
 * internal map.
 * Otherwise, the request will be directed to the provider referenced by namespace.
 */
class LazyModelMap extends ForwardingMap<String, Object> {

    private final Map<String, TemplateConfigVariablesProvider> providers;

    private Map<String, Object> delegate;

    /**
     * Providers to use internally for resolving values.
     * @param providers
     */
    LazyModelMap(TemplateConfigVariablesProvider... providers) {
        this.providers = Stream.of(Objects.requireNonNull(providers))
                               .collect(Collectors.toMap(TemplateConfigVariablesProvider::getNamespace,
                                                         Function.identity(),
                                                         (l, r) -> l,
                                                         LinkedHashMap::new));
    }

    /**
     * First searches providers, if not found refers internal map
     * @param key to search value for
     * @return found value
     */
    @Override
    public Object get(@Nullable Object key) {
        final Object value = providers.get(key);
        if (value != null) {
            return ((TemplateConfigVariablesProvider) value).getVariables();
        } else {
            return super.get(key);
        }
    }

    /**
     * First checks whether key refers to a namespace then to delegate
     * @param key to check for existance
     * @return true if found, false otherwise
     */
    @Override
    public boolean containsKey(@Nullable Object key) {
        return providers.containsKey(key) || super.containsKey(key);
    }

    /**
     * If providers are empty then nothing needs to be checked
     * @return true if no providers are configured, false otherwise
     */
    @Override
    public boolean isEmpty() {
        return providers.isEmpty();
    }

    /**
     * Instantiates internal map by traversing providers create model of all the key/value pairs with and without
     * namespace
     * @return model map
     */
    @Override
    protected Map<String, Object> delegate() {
        if (delegate == null) {
            Map<String, Object> values = new HashMap<>();
            providers.values().stream().map(TemplateConfigVariablesProvider::getVariables).forEach(values::putAll);
            providers.values().forEach(p -> values.put(p.getNamespace(), p.getVariables()));
            delegate = values;
        }
        return delegate;
    }

}
