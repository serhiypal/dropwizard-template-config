package de.thomaskrille.dropwizard_template_config

import org.apache.commons.io.IOUtils
import spock.lang.Specification

import static org.hamcrest.CoreMatchers.containsString

class AdvancedInterpolationSpec extends Specification {

    def TestCustomProvider environmentProvider = TestCustomProvider.forEnv()
    def TestCustomProvider systemPropertiesProvider = TestCustomProvider.forSys()

    def TemplateConfigurationSourceProvider templateConfigurationSourceProvider =
            new TemplateConfigurationSourceProvider(new TestConfigSourceProvider(),
                    new TemplateConfigBundleConfiguration(),
                    systemPropertiesProvider,
                    environmentProvider)

    def 'replacing an environment variable inline works'() {
        given:
        def config = '''database:
                          driverClass: org.postgresql.Driver
                          user: ${DB_USER}
                          password: ${DB_PASSWORD}
                          url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/my-app-db'''

        environmentProvider.putVariable('DB_USER', 'user')
        environmentProvider.putVariable('DB_PASSWORD', 'password')
        environmentProvider.putVariable('DB_HOST', 'db-host')
        environmentProvider.putVariable('DB_PORT', '12345')

        when:
        InputStream parsedConfig = templateConfigurationSourceProvider.open(config)
        String parsedConfigAsString = IOUtils.toString(parsedConfig)

        then:
        parsedConfigAsString containsString('database:')
        parsedConfigAsString containsString('driverClass: org.postgresql.Driver')
        parsedConfigAsString containsString('user: user')
        parsedConfigAsString containsString('password: password')
        parsedConfigAsString containsString('url: jdbc:postgresql://db-host:12345/my-app-db')
    }

    def 'inserting whole mappings works'() {
        given:
        def config = '''
                server:
                  ${SERVER_TYPE_LINE}
                  connector:
                    ${SERVER_CONNECTOR_TYPE_LINE}
                    port: 8080
                '''

        environmentProvider.putVariable('SERVER_TYPE_LINE', 'type: simple')
        environmentProvider.putVariable('SERVER_CONNECTOR_TYPE_LINE', 'type: http')

        when:
        InputStream parsedConfig = templateConfigurationSourceProvider.open(config)
        String parsedConfigAsString = IOUtils.toString(parsedConfig)

        then:
        parsedConfigAsString containsString('type: simple')
        parsedConfigAsString containsString('type: http')
    }

    def 'environment variables have precedence over system properties'() {
        given:
        def config = '''server:
                          type: simple
                          connector:
                            type: http
                            port: ${port}'''

        environmentProvider.putVariable('port', '8080')
        systemPropertiesProvider.putVariable('port', '8081')

        when:
        def parsedConfig = templateConfigurationSourceProvider.open(config)
        def parsedConfigAsString = IOUtils.toString(parsedConfig)

        then:
        parsedConfigAsString containsString('server:')
        parsedConfigAsString containsString('type: http')
        parsedConfigAsString containsString('port: 8080')
    }

}
