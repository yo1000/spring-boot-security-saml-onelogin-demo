package com.yo1000.demo.saml

import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * @author Ulises Bocchio
 * @author yo1000
 */
@Configuration
class FilterCleanupConfig {
    @Bean
    fun removeUnwantedAutomaticFilterRegistration(): BeanDefinitionRegistryPostProcessor {
        return object : BeanDefinitionRegistryPostProcessor {
            override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {}

            override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
                if (beanFactory !is DefaultListableBeanFactory)
                    throw IllegalArgumentException("BeanFactory is invalid")

                beanFactory.getBeanNamesForType(javax.servlet.Filter::class.java)
                        .filter(setOf(
                                "samlEntryPoint",
                                "samlFilter",
                                "samlIDPDiscovery",
                                "metadataDisplayFilter",
                                "samlWebSSOHoKProcessingFilter",
                                "samlProcessingFilter",
                                "samlLogoutProcessingFilter",
                                "samlLogoutFilter",
                                "metadataGeneratorFilter"
                        )::contains)
                        .forEach {
                            beanFactory.registerBeanDefinition(
                                    "${it}FilterRegistrationBean",
                                    BeanDefinitionBuilder
                                            .genericBeanDefinition(FilterRegistrationBean::class.java)
                                            .setScope(BeanDefinition.SCOPE_SINGLETON)
                                            .addConstructorArgReference(it)
                                            .addConstructorArgValue(arrayOf<ServletRegistrationBean<*>>())
                                            .addPropertyValue("enabled", false)
                                            .beanDefinition
                            )
                        }
            }
        }
    }
}
