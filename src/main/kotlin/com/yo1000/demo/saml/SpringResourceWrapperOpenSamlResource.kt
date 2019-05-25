package com.yo1000.demo.saml

import org.joda.time.DateTime
import org.springframework.core.io.Resource
import java.io.InputStream

/**
 * @author Ulises Bocchio
 * @author yo1000
 */
class SpringResourceWrapperOpenSamlResource(
        private val springDelegate: Resource
) : OpenSamlResource {
    init {
        exists()
    }

    override fun getLocation(): String = springDelegate.getURL().toString()
    override fun exists(): Boolean = springDelegate.exists()
    override fun getLastModifiedTime(): DateTime = DateTime(springDelegate.lastModified())
    override fun getInputStream(): InputStream = springDelegate.inputStream

    override fun hashCode(): Int = location.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SpringResourceWrapperOpenSamlResource
        return location == other.location
    }
}

typealias OpenSamlResource = org.opensaml.util.resource.Resource
