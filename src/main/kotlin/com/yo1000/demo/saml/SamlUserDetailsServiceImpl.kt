package com.yo1000.demo.saml

import com.yo1000.demo.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.saml.SAMLCredential
import org.springframework.security.saml.userdetails.SAMLUserDetailsService
import org.springframework.stereotype.Service

/**
 *
 * @author yo1000
 */
@Service
class SamlUserDetailsServiceImpl(
        private val userRepository: UserRepository
) : SAMLUserDetailsService {
    companion object {
        private val logger = LoggerFactory.getLogger(SamlUserDetailsServiceImpl::class.java)
    }

    override fun loadUserBySAML(credential: SAMLCredential?): Any = credential?.let {
        logger.debug("credential.attributes {}", it.attributes)
        logger.debug("credential.additionalData {}", it.additionalData)

        it.nameID.value?.let { username ->
            logger.info("Login received for user {}", username)

            userRepository.findByUsername(username)?.authorityValues?.let { authorities ->
                SamlUserDetails(credential, authorities)
            } ?: throw IllegalStateException("user is not exists")
        } ?: throw NullPointerException("username is null")
    } ?: throw NullPointerException("credential is null")
}
