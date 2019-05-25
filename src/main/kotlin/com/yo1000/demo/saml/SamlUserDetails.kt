package com.yo1000.demo.saml

import org.opensaml.saml2.core.Attribute
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.saml.SAMLCredential

/**
 * Default Implementation of {@link UserDetails} for Spring Boot Security SAML. This simple implementation hardly covers all security aspects since it's mostly
 * hardcoded. I.E. accounts are never locked, expired, or disabled, and always eturn the same granted authority "ROLE_USER". Consider implementing your own
 * {@link UserDetails} and {@link SAMLUserDetailsService}.
 *
 * @author Ulises Bocchio
 * @author yo1000
 */
class SamlUserDetails(
        private val samlCredential: SAMLCredential,
        private val authorities: List<String>) : UserDetails {
    val attributes: List<Attribute> = samlCredential.attributes

    override fun isEnabled(): Boolean = true
    override fun getUsername(): String = samlCredential.nameID.value
    override fun isCredentialsNonExpired(): Boolean = true
    override fun getPassword(): String = ""
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = authorities.map {
        SimpleGrantedAuthority(it)
    }.toMutableList()
}
