package com.yo1000.demo.saml

import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager
import org.opensaml.saml2.metadata.provider.ResourceBackedMetadataProvider
import org.opensaml.util.SimpleURLCanonicalizer
import org.opensaml.xml.parse.StaticBasicParserPool
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.saml.*
import org.springframework.security.saml.key.JKSKeyManager
import org.springframework.security.saml.key.KeyManager
import org.springframework.security.saml.metadata.*
import org.springframework.security.saml.processor.*
import org.springframework.security.saml.trust.httpclient.TLSProtocolConfigurer
import org.springframework.security.saml.userdetails.SAMLUserDetailsService
import org.springframework.security.saml.util.VelocityFactory
import org.springframework.security.saml.websso.ArtifactResolutionProfileImpl
import org.springframework.security.saml.websso.WebSSOProfileOptions
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.security.web.authentication.logout.LogoutHandler
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler
import java.util.*
import javax.servlet.http.HttpServletRequest

/**
 * @author Ulises Bocchio
 * @author yo1000
 */
@Configuration
@EnableConfigurationProperties(
        SamlMetadataProperties::class
)
class SamlConfig {
    @Bean
    fun samlAuthenticationProvider(samlUserDetailsService: SAMLUserDetailsService): SAMLAuthenticationProvider = SAMLAuthenticationProvider().also {
        it.userDetails = samlUserDetailsService
        it.isForcePrincipalAsString = false
    }

    @Bean
    fun authenticationManager(samlAuthenticationProvider: SAMLAuthenticationProvider): AuthenticationManager = ProviderManager(
            listOf(samlAuthenticationProvider)
    )

    @Bean(initMethod = "initialize")
    fun parserPool(): StaticBasicParserPool = StaticBasicParserPool()

    @Bean
    fun processor(): SAMLProcessorImpl = VelocityFactory.getEngine().let {
        SAMLProcessorImpl(listOf(
                HTTPRedirectDeflateBinding(parserPool()),
                HTTPPostBinding(parserPool(), it),
                HTTPArtifactBinding(
                        parserPool(),
                        it,
                        ArtifactResolutionProfileImpl(HttpClient(MultiThreadedHttpConnectionManager())).also {
                            it.setProcessor(SAMLProcessorImpl(HTTPSOAP11Binding(parserPool())))
                        }
                ),
                HTTPSOAP11Binding(parserPool()),
                HTTPPAOS11Binding(parserPool())
        ))
    }

    @Bean
    fun successLogoutHandler(): SimpleUrlLogoutSuccessHandler = SimpleUrlLogoutSuccessHandler().also {
        it.setDefaultTargetUrl("/")
    }

    @Bean
    fun logoutHandler(): SecurityContextLogoutHandler = SecurityContextLogoutHandler().also {
        it.setClearAuthentication(true)
        //it.setInvalidateHttpSession(true);
    }

    @Bean
    fun samlLogoutFilter(): SAMLLogoutFilter {
        val filter = SAMLLogoutFilter(successLogoutHandler(), arrayOf<LogoutHandler>(logoutHandler()), arrayOf<LogoutHandler>(logoutHandler()))
        filter.filterProcessesUrl = "/saml/logout"
        return filter
    }

    @Bean
    fun samlLogoutProcessingFilter(): SAMLLogoutProcessingFilter = SAMLLogoutProcessingFilter(
            successLogoutHandler(),
            logoutHandler()
    ).also {
        it.filterProcessesUrl = "/saml/SingleLogout"
    }

    @Bean
    fun metadataGeneratorFilter(
            samlMetadataProperties: SamlMetadataProperties,
            metadataGenerator: MetadataGenerator
    ): MetadataGeneratorFilter =
            object : MetadataGeneratorFilter(metadataGenerator) {
                override fun getDefaultBaseURL(request: HttpServletRequest?): String {
                    val builder = StringBuilder()
                            .append(samlMetadataProperties.scheme)
                            .append("://")
                            .append(samlMetadataProperties.serverName)

                    if (samlMetadataProperties.includeServerPortInRequestUrl) {
                        builder.append(":").append(samlMetadataProperties.serverPort)
                    }

                    if (samlMetadataProperties.contextPath.isNotEmpty()) {
                        builder.append(
                                if (samlMetadataProperties.contextPath.startsWith("/"))
                                    samlMetadataProperties.contextPath
                                else
                                    "/${samlMetadataProperties.contextPath}"
                        )
                    }

                    return if (this.isNormalizeBaseUrl)
                        SimpleURLCanonicalizer.canonicalize(builder.toString())
                    else builder.toString()
                }
            }

    @Bean
    fun metadataDisplayFilter(): MetadataDisplayFilter = MetadataDisplayFilter().also {
        it.filterProcessesUrl = "/saml/metadata"
    }

    @Bean
    fun idpMetadataLoader(): BeanFactoryPostProcessor = BeanFactoryPostProcessor { beanFactory ->
        PathMatchingResourcePatternResolver().getResources("classpath:/idp-*.xml").forEach { resource ->
            resource.filename?.let {
                it.substring(it.lastIndexOf("idp-") + 4, it.lastIndexOf(".xml")).let { idpName ->
                    //log.info("Loaded Idp Metadata bean {}: {}", idpName, idpMetadataFile)
                    beanFactory.registerSingleton(
                            idpName,
                            ExtendedMetadataDelegate(
                                    ResourceBackedMetadataProvider(
                                            Timer(true),
                                            SpringResourceWrapperOpenSamlResource(resource)
                                    ).also {
                                        it.parserPool = parserPool()
                                    },
                                    extendedMetadata().clone().also {
                                        it.alias = idpName
                                    }
                            ).also {
                                it.isMetadataTrustCheck = true
                                it.isMetadataRequireSignature = false
                            }
                    )
                }
            }
        }
    }

    @Bean
    fun extendedMetadata(): ExtendedMetadata = ExtendedMetadata().also {
        //set flag to true to present user with IDP Selection screen
        it.isIdpDiscoveryEnabled = true
        it.isRequireLogoutRequestSigned = true
        //it.setRequireLogoutResponseSigned(true);
        it.isSignMetadata = false
    }

    @Bean
    fun metadataGenerator(keyManager: KeyManager): MetadataGenerator = MetadataGenerator().also {
        it.entityId = "localhost-demo"
        it.extendedMetadata = extendedMetadata()
        it.isIncludeDiscoveryExtension = false
        it.setKeyManager(keyManager)
    }

    @Bean
    fun samlProcessingFilter(authenticationManager: AuthenticationManager): SAMLProcessingFilter = SAMLProcessingFilter().also {
        it.setAuthenticationManager(authenticationManager)
        it.setAuthenticationSuccessHandler(successRedirectHandler())
        it.setAuthenticationFailureHandler(authenticationFailureHandler())
        it.filterProcessesUrl = "/saml/SSO"
    }

    @Bean
    fun samlWebSSOHoKProcessingFilter(authenticationManager: AuthenticationManager): SAMLWebSSOHoKProcessingFilter = SAMLWebSSOHoKProcessingFilter().also {
        it.setAuthenticationSuccessHandler(successRedirectHandler())
        it.setAuthenticationManager(authenticationManager)
        it.setAuthenticationFailureHandler(authenticationFailureHandler())
    }

    @Bean
    fun successRedirectHandler(): SavedRequestAwareAuthenticationSuccessHandler = SavedRequestAwareAuthenticationSuccessHandler().also {
        it.setDefaultTargetUrl("/")
    }

    @Bean
    fun authenticationFailureHandler(): SimpleUrlAuthenticationFailureHandler = SimpleUrlAuthenticationFailureHandler().also {
        it.setUseForward(false)
        //it.setDefaultFailureUrl("/error");
    }

    @Bean
    fun samlIDPDiscovery(): SAMLDiscovery = SAMLDiscovery().also {
        it.filterProcessesUrl = "/saml/discovery"
        it.idpSelectionPath = "/idpredirection"
    }

    @Bean
    fun samlEntryPoint(): SAMLEntryPoint = SAMLEntryPoint().also {
        it.setDefaultProfileOptions(WebSSOProfileOptions().also {
            it.isIncludeScoping = false
        })
        it.filterProcessesUrl = "/saml/login"
    }

    @Bean
    fun keystoreFactory(resourceLoader: ResourceLoader): KeystoreFactory =
            KeystoreFactory(resourceLoader)

    @Bean
    fun keyManager(keystoreFactory: KeystoreFactory): KeyManager = JKSKeyManager(
            keystoreFactory.loadKeystore(
                    "classpath:/localhost.cert",
                    "classpath:/localhost.key.der",
                    "localhost",
                    ""
            ),
            mapOf("localhost" to ""),
            "localhost"
    )

    @Bean
    fun tlsProtocolConfigurer(keyManager: KeyManager): TLSProtocolConfigurer = TLSProtocolConfigurer().also {
        it.setKeyManager(keyManager)
    }
}

