package de.thomaskrille.dropwizard_template_config;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.google.common.collect.ForwardingMap;

public class ProviderMap extends ForwardingMap<String, Object> {

    private final Map<String, TemplateConfigVariablesProvider> providers;

    public ProviderMap(TemplateConfigVariablesProvider... providers) {
        this.providers = new LinkedHashMap<>();
        Stream.of(providers).forEach(p -> ProviderMap.this.providers.put(p.getNamespace(), p));
    }

    @Override
    protected Map<String, Object> delegate() {
        Map<String, Object> delegate = new HashMap<>();
        providers.forEach((k, v) -> delegate.put(k, v.getVariables()));
        providers.values().stream().map(TemplateConfigVariablesProvider::getVariables).forEach(delegate::putAll);
        return delegate;
    }

}
