package de.thomaskrille.dropwizard_template_config;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import io.dropwizard.configuration.ConfigurationSourceProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.stream.Stream;

public class TemplateConfigurationSourceProvider implements ConfigurationSourceProvider {

    private final ConfigurationSourceProvider parentProvider;
    private final TemplateConfigVariablesProvider systemPropertiesProvider;
    private final TemplateConfigVariablesProvider environmentProvider;
    private final TemplateConfigBundleConfiguration configuration;

    TemplateConfigurationSourceProvider(
            final ConfigurationSourceProvider parentProvider,
            final TemplateConfigVariablesProvider environmentProvider,
            final TemplateConfigVariablesProvider systemPropertiesProvider,
            final TemplateConfigBundleConfiguration configuration
    ) {
        this.parentProvider = parentProvider;
        this.environmentProvider = environmentProvider;
        this.systemPropertiesProvider = systemPropertiesProvider;
        this.configuration = configuration;
    }

    @Override
    public InputStream open(final String path) throws IOException {
        try {
            return createConfigurationSourceStream(path);
        } catch (TemplateException e) {
            throw new IllegalStateException("Could not render template.", e);
        }
    }

    private InputStream createConfigurationSourceStream(String path) throws IOException, TemplateException {
        Configuration freemarkerConfiguration = createFreemarkerConfiguration();
        Map<String, Object> dataModel = createDataModel();
        Template configTemplate = createFreemarkerTemplate(path, freemarkerConfiguration);
        byte[] processedConfigTemplate = processTemplate(dataModel, configTemplate);
        writeConfigFile(processedConfigTemplate);
        return new ByteArrayInputStream(processedConfigTemplate);
    }

    private Configuration createFreemarkerConfiguration() throws IOException {
        Configuration freemarkerConfiguration = new Configuration(Configuration.VERSION_2_3_23);
        freemarkerConfiguration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        freemarkerConfiguration.setNumberFormat("computer");
        freemarkerConfiguration.setDefaultEncoding(configuration.charset().name());
        configuration.resourceIncludePath().ifPresent(p -> freemarkerConfiguration.setClassForTemplateLoading(
                             TemplateConfigurationSourceProvider.class, !p.startsWith("/") ? ("/" + p) : p));

        if (!configuration.resourceIncludePath().isPresent()) {
            configuration.fileIncludePath().map(File::new).ifPresent(f -> {
                try {
                    freemarkerConfiguration.setDirectoryForTemplateLoading(f);
                } catch (IOException e) {
                    throw new IllegalStateException("Could not set directory for template loading.", e);
                }
            });
        }
        return freemarkerConfiguration;
    }

    private Map<String, Object> createDataModel() {
        // We populate the dataModel with lowest-priority items first, so that higher-priority
        // items can overwrite existing entries.
        // Lowest priority is a flat copy of Java system properties, then a flat copy of
        // environment variables, then a flat copy of custom variables, and finally the "env", "sys",
        // and custom namespaces.
        return new DelegateProviderMap(Stream.concat(Stream.of(systemPropertiesProvider, environmentProvider),
                                                     configuration.customProviders().stream())
                                             .toArray(TemplateConfigVariablesProvider[]::new));
    }

    private Template createFreemarkerTemplate(String path, Configuration freemarkerConfiguration) throws IOException {
        InputStream configurationSource = parentProvider.open(path);
        InputStreamReader configurationSourceReader = new InputStreamReader(configurationSource, configuration.charset());
        return new Template("config", configurationSourceReader, freemarkerConfiguration);
    }

    private byte[] processTemplate(Map<String, Object> dataModel, Template template) throws TemplateException, IOException {
        ByteArrayOutputStream processedTemplateStream = new ByteArrayOutputStream();
        template.process(dataModel, new OutputStreamWriter(processedTemplateStream, configuration.charset()));
        return processedTemplateStream.toByteArray();
    }

    private void writeConfigFile(byte[] processedTemplateBytes) throws IOException {
        configuration.outputPath().ifPresent(pathString -> {
            try {
                Path path = Paths.get(pathString).toAbsolutePath();
                Files.createDirectories(path.getParent());
                Files.write(path,
                            processedTemplateBytes,
                            StandardOpenOption.WRITE,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                throw new IllegalStateException("Could not write configuration file.", e);
            }
        });
    }
}
