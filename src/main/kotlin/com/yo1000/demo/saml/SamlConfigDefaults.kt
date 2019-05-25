package com.yo1000.demo.saml

import org.opensaml.saml2.metadata.provider.MetadataProvider
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.saml.SAMLBootstrap
import org.springframework.security.saml.context.SAMLContextProvider
import org.springframework.security.saml.context.SAMLContextProviderLB
import org.springframework.security.saml.log.SAMLDefaultLogger
import org.springframework.security.saml.metadata.CachingMetadataManager
import org.springframework.security.saml.parser.ParserPoolHolder
import org.springframework.security.saml.websso.*

/**
 * @author Ulises Bocchio
 */
@Configuration
@EnableConfigurationProperties(
        SamlMetadataProperties::class
)
class SamlConfigDefaults {
    @Bean
    fun samlBootstrap(): SAMLBootstrap {
        return SAMLBootstrap()
    }

    @Bean
    fun parserPoolHolder(): ParserPoolHolder {
        return ParserPoolHolder()
    }

    @Bean
    fun contextProvider(
            samlMetadataProperties: SamlMetadataProperties
    ): SAMLContextProvider = SAMLContextProviderLB().also {
        it.setScheme(samlMetadataProperties.scheme)
        it.setServerName(samlMetadataProperties.serverName)
        it.setServerPort(samlMetadataProperties.serverPort)
        it.setContextPath(samlMetadataProperties.contextPath)
        it.setIncludeServerPortInRequestURL(samlMetadataProperties.includeServerPortInRequestUrl)
    }

    @Bean
    fun samlLogger(): SAMLDefaultLogger {
        return SAMLDefaultLogger()
    }

    @Bean
    fun webSSOprofileConsumer(): WebSSOProfileConsumer {
        return WebSSOProfileConsumerImpl()
    }

    @Bean
    fun hokWebSSOprofileConsumer(): WebSSOProfileConsumerHoKImpl {
        return WebSSOProfileConsumerHoKImpl()
    }

    @Bean
    fun webSSOprofile(): WebSSOProfile {
        return WebSSOProfileImpl()
    }

    @Bean
    fun ecpProfile(): WebSSOProfileECPImpl {
        return WebSSOProfileECPImpl()
    }

    @Bean
    fun hokWebSSOProfile(): WebSSOProfileHoKImpl {
        return WebSSOProfileHoKImpl()
    }

    @Bean
    fun logoutProfile(): SingleLogoutProfile {
        return SingleLogoutProfileImpl()
    }

    @Bean
    fun metadataManager(metadataProviders: List<MetadataProvider>): CachingMetadataManager {
        return CachingMetadataManager(metadataProviders)
    }
}
