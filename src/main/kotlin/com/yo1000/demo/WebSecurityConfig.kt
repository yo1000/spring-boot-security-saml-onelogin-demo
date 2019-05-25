package com.yo1000.demo

import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.saml.*
import org.springframework.security.saml.metadata.MetadataDisplayFilter
import org.springframework.security.saml.metadata.MetadataGeneratorFilter
import org.springframework.security.web.authentication.logout.LogoutFilter
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.security.web.context.HttpSessionSecurityContextRepository

/**
 *
 * @author yo1000
 */
@Configuration
class WebSecurityConfig(
        private val userRepository: UserRepository,
        private val samlLogoutFilter: SAMLLogoutFilter,
        private val samlLogoutProcessingFilter: SAMLLogoutProcessingFilter,
        private val metadataDisplayFilter: MetadataDisplayFilter,
        private val metadataGeneratorFilter: MetadataGeneratorFilter,
        private val samlProcessingFilter: SAMLProcessingFilter,
        private val samlWebSSOHoKProcessingFilter: SAMLWebSSOHoKProcessingFilter,
        private val samlEntryPoint: SAMLEntryPoint,
        private val samlDiscovery: SAMLDiscovery,
        private val authenticationManager: AuthenticationManager
) : WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity) {
        http.securityContext()
                .securityContextRepository(HttpSessionSecurityContextRepository())

        http.httpBasic()
                .disable()

        http.csrf()
                .disable()

        http.addFilterAfter(metadataGeneratorFilter, BasicAuthenticationFilter::class.java)
                .addFilterAfter(metadataDisplayFilter, metadataGeneratorFilter::class.java)
                .addFilterAfter(samlEntryPoint, metadataDisplayFilter::class.java)
                .addFilterAfter(samlProcessingFilter, samlEntryPoint::class.java)
                .addFilterAfter(samlWebSSOHoKProcessingFilter, samlProcessingFilter::class.java)
                .addFilterAfter(samlLogoutProcessingFilter, samlWebSSOHoKProcessingFilter::class.java)
                .addFilterAfter(samlDiscovery, samlLogoutProcessingFilter::class.java)
                .addFilterAfter(samlLogoutFilter, LogoutFilter::class.java)

        http.authorizeRequests()
                .antMatchers("/saml/**", "/idpredirection").permitAll()
                .mvcMatchers(HttpMethod.GET, "/").hasAnyAuthority("USER")
                .anyRequest().authenticated()

        http.exceptionHandling()
                .authenticationEntryPoint(samlEntryPoint)

        http.logout()
                .disable()
    }

    override fun configure(web: WebSecurity) {
        web.ignoring().antMatchers("/css/**", "/js/**")
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(UserDetailsService { userRepository.findByUsername(it) })
    }

    override fun authenticationManager(): AuthenticationManager = authenticationManager
}