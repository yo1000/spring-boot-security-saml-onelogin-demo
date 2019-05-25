package com.yo1000.demo

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 *
 * @author yo1000
 */
class User(
        private val username: String,
        val authorityValues: List<String>
) : UserDetails {
    override fun getUsername(): String = username
    override fun getPassword(): String = ""

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = authorityValues.map {
        SimpleGrantedAuthority(it)
    }.toMutableList()

    override fun isEnabled(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
}
