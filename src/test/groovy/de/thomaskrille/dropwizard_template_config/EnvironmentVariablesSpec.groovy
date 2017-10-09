package de.thomaskrille.dropwizard_template_config

import org.apache.commons.io.IOUtils
import spock.lang.Specification

import static org.hamcrest.CoreMatchers.containsString
import static org.hamcrest.CoreMatchers.isA

class EnvironmentVariablesSpec extends Specification {

    def TestCustomProvider environmentProvider = TestCustomProvider.forEnv()

    def TemplateConfigurationSourceProvider templateConfigurationSourceProvider =
            new TemplateConfigurationSourceProvider(new TestConfigSourceProvider(),
                    new TemplateConfigBundleConfiguration(),
                    environmentProvider,
                    Providers.fromSystemProperties())

    def 'replacing an environment variable works'() throws Exception {
        given:
        def config = '''server:
                          type: simple
                          connector:
                            type: http
                            port: ${PORT}'''

        environmentProvider.putVariable('PORT', '8080')

        when:
        def parsedConfig = templateConfigurationSourceProvider.open(config)
        def parsedConfigAsString = IOUtils.toString(parsedConfig)

        then:
        parsedConfigAsString containsString('server:')
        parsedConfigAsString containsString('type: http')
        parsedConfigAsString containsString('port: 8080')
    }

    def 'using a missing environment variable honors default value'() throws Exception {
        given:
        def config = '''server:
                          type: simple
                          connector:
                            type: http
                            port: ${PORT!8080}'''

        when:
        InputStream parsedConfig = templateConfigurationSourceProvider.open(config)
        String parsedConfigAsString = IOUtils.toString(parsedConfig)

        then:
        parsedConfigAsString containsString('server:')
        parsedConfigAsString containsString('type: http')
        parsedConfigAsString containsString('port: 8080')
    }

    def 'using a missing environment variable without default value fails'() throws Exception {
        given:
        def config = '''server:
                          type: simple
                          connector:
                            type: http
                            port: ${PORT}'''

        when:
        templateConfigurationSourceProvider.open(config)

        then:
        def exception = thrown(IllegalStateException)
        def exceptionsCause = exception.cause
        exceptionsCause isA(freemarker.core.InvalidReferenceException)
    }

    def 'can use env prefix'() throws Exception {
        given:
        def config = '''server:
                          type: simple
                          connector:
                            type: http
                            port: ${env.PORT}'''

        environmentProvider.putVariable('PORT', '8080')

        when:
        def parsedConfig = templateConfigurationSourceProvider.open(config)
        def parsedConfigAsString = IOUtils.toString(parsedConfig)

        then:
        parsedConfigAsString containsString('server:')
        parsedConfigAsString containsString('type: http')
        parsedConfigAsString containsString('port: 8080')
    }


}
