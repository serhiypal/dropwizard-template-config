package de.thomaskrille.dropwizard_template_config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import com.google.common.collect.ForwardingMap;

class DelegateProviderMap extends ForwardingMap<String, Object> {

    private final TemplateConfigVariablesProvider[] providers;

    public DelegateProviderMap(TemplateConfigVariablesProvider... providers) {
        this.providers = Objects.requireNonNull(providers);
    }

    @Override
    protected Map<String, Object> delegate() {
        Map<String, Object> delegate = new HashMap<>();
        Stream.of(providers).forEach(p -> delegate.put(p.getNamespace(), p.getVariables()));
        Stream.of(providers).map(TemplateConfigVariablesProvider::getVariables).forEach(delegate::putAll);
        return delegate;
    }

}
