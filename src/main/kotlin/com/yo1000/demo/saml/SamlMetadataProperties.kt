package com.yo1000.demo.saml

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "saml.metadata")
data class SamlMetadataProperties(
        var scheme: String = "https",
        var serverName: String = "localhost",
        var serverPort: Int = 443,
        var contextPath: String = "",
        var includeServerPortInRequestUrl: Boolean = false
)
