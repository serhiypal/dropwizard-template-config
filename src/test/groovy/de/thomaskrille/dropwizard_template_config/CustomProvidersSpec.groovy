package de.thomaskrille.dropwizard_template_config

import org.apache.commons.io.IOUtils
import spock.lang.Specification

import static org.hamcrest.CoreMatchers.containsString

class CustomProvidersSpec extends Specification {

    def TestCustomProvider environmentProvider = TestCustomProvider.forEnv()
    def TestCustomProvider systemPropertiesProvider = TestCustomProvider.forSys()
    def TestCustomProvider customProviderA = new TestCustomProvider("providerA")
    def TestCustomProvider customProviderB = new TestCustomProvider("providerB")
    def TemplateConfigBundleConfiguration templateConfigBundleConfiguration = new TemplateConfigBundleConfiguration()
            .addCustomProvider(customProviderA)
            .addCustomProvider(customProviderB)

    def TemplateConfigurationSourceProvider templateConfigurationSourceProvider =
            new TemplateConfigurationSourceProvider(new TestConfigSourceProvider(),
                    templateConfigBundleConfiguration,
                    systemPropertiesProvider,
                    environmentProvider)

    def 'replacing custom variables inline works'() {
        given:
        def config = '''database:
                          driverClass: org.postgresql.Driver
                          user: ${DB_USER}
                          password: ${DB_PASSWORD}
                          url: jdbc:postgresql://${providerA.DB_HOST}:${providerB.DB_PORT}/my-app-db'''
        customProviderA.putVariable('DB_USER', 'user')
        customProviderB.putVariable('DB_PASSWORD', 'password')
        customProviderA.putVariable('DB_HOST', 'db-host')
        customProviderB.putVariable('DB_PORT', '12345')

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

    def 'custom variables have precedence over environment variables'() {
        given:
        def config = '''database:
                          driverClass: org.postgresql.Driver
                          user: ${DB_USER}
                          password: ${DB_PASSWORD}
                          url: jdbc:postgresql://${providerA.DB_HOST}:${providerB.DB_PORT}/my-app-db'''
        environmentProvider.putVariable('DB_USER', 'bad_user')
        customProviderA.putVariable('DB_USER', 'good_user')
        customProviderB.putVariable('DB_PASSWORD', 'password')
        customProviderA.putVariable('DB_HOST', 'db-host')
        customProviderB.putVariable('DB_PORT', '12345')

        when:
        InputStream parsedConfig = templateConfigurationSourceProvider.open(config)
        String parsedConfigAsString = IOUtils.toString(parsedConfig)

        then:
        parsedConfigAsString containsString('database:')
        parsedConfigAsString containsString('driverClass: org.postgresql.Driver')
        parsedConfigAsString containsString('user: good_user')
        parsedConfigAsString containsString('password: password')
        parsedConfigAsString containsString('url: jdbc:postgresql://db-host:12345/my-app-db')
    }

    def 'parses json string correctly'() {
        given:
        def config = '''my_keys:
                          <#assign my_keys = providerA.my_keys?eval>
                          <#list my_keys?keys as my_key>
                          ${my_key}: ${my_keys[my_key]}
                          </#list>'''
        customProviderA.putVariable('my_keys', '{ "key1": "secret1", "key2": "secret2" } ')

        when:
        InputStream parsedConfig = templateConfigurationSourceProvider.open(config)
        String parsedConfigAsString = IOUtils.toString(parsedConfig)

        then:
        parsedConfigAsString containsString('my_keys:')
        parsedConfigAsString containsString('  key1: secret1')
        parsedConfigAsString containsString('  key2: secret2')
    }

}
