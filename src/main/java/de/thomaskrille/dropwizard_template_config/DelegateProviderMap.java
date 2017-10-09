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

class DelegateProviderMap extends ForwardingMap<String, Object> {

    private final Map<String, TemplateConfigVariablesProvider> providers;

    private Map<String, Object> delegate;

    DelegateProviderMap(TemplateConfigVariablesProvider... providers) {
        this.providers = Stream.of(Objects.requireNonNull(providers))
                               .collect(Collectors.toMap(TemplateConfigVariablesProvider::getNamespace,
                                                         Function.identity(),
                                                         (l, r) -> l,
                                                         LinkedHashMap::new));
    }

    @Override
    public Object get(@Nullable Object key) {
        Object value = providers.get(key);
        if (value == null) {
            value = delegate().get(key);
        }
        return value;
    }

    @Override
    public boolean containsKey(@Nullable Object key) {
        return providers.containsKey(key) || super.containsKey(key);
    }

    @Override
    public boolean isEmpty() {
        return providers.isEmpty();
    }

    @Override
    protected Map<String, Object> delegate() {
        if (delegate == null) {
            Map<String, Object> values = new HashMap<>();
            providers.values().forEach(p -> values.put(p.getNamespace(), p.getVariables()));
            providers.values().stream().map(TemplateConfigVariablesProvider::getVariables).forEach(values::putAll);
            delegate = values;
        }
        return delegate;
    }

}
