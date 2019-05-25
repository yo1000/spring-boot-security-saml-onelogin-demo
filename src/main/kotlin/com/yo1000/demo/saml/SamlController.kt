package com.yo1000.demo.saml

import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.saml.SAMLConstants
import org.springframework.security.saml.SAMLDiscovery
import org.springframework.security.saml.metadata.MetadataManager
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.net.URLEncoder
import javax.servlet.http.HttpServletRequest

/**
 * @author Ulises Bocchio
 * @author yo1000
 */
@Controller
@RequestMapping("/idpredirection")
class SamlController(
        private val metadataManager: MetadataManager
) {
    @GetMapping
    fun get(request: HttpServletRequest): String {
        if (!comesFromDiscoveryFilter(request)) {
            throw AuthenticationServiceException("SP Discovery flow not detected")
        }

        if (metadataManager.idpEntityNames.size != 1) {
            throw IllegalStateException("Found multi idp settings")
        }

        val baseUrl = request.getAttribute(SAMLDiscovery.RETURN_URL).toString()
        val idpParam = "${SAMLDiscovery.RETURN_PARAM}=${URLEncoder.encode(metadataManager.idpEntityNames.first(), "UTF-8")}"

        val redirectUrl = if (baseUrl.contains('?')) {
            "$baseUrl&$idpParam"
        } else {
            "$baseUrl?$idpParam"
        }

        return "redirect:$redirectUrl"
    }

    private fun comesFromDiscoveryFilter(request: HttpServletRequest): Boolean {
        return request.getAttribute(SAMLConstants.LOCAL_ENTITY_ID) != null &&
                request.getAttribute(SAMLDiscovery.RETURN_URL) != null &&
                request.getAttribute(SAMLDiscovery.RETURN_PARAM) != null
    }
}