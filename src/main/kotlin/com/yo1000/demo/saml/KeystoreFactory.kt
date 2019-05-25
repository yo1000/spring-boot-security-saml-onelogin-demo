package com.yo1000.demo.saml

import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.ResourceLoader
import org.springframework.util.StreamUtils
import java.security.KeyFactory
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec

/**
 * @author Ulises Bocchio
 * @author yo1000
 */
class KeystoreFactory(
        private val resourceLoader: ResourceLoader = DefaultResourceLoader()
) {
    fun loadKeystore(certResourceLocation: String, privateKeyResourceLocation: String, alias: String, keyPassword: String): KeyStore = createEmptyKeystore().let {
        addKeyToKeystore(
                it,
                loadCert(certResourceLocation),
                loadPrivateKey(privateKeyResourceLocation),
                alias,
                keyPassword
        )
        it
    }

    fun addKeyToKeystore(keyStore: KeyStore, cert: X509Certificate, privateKey: RSAPrivateKey, alias: String, password: String) = keyStore.setEntry(
            alias,
            KeyStore.PrivateKeyEntry(privateKey, arrayOf<Certificate>(cert)),
            KeyStore.PasswordProtection(password.toCharArray())
    )

    fun createEmptyKeystore(): KeyStore = KeyStore.getInstance("JKS").also {
        it.load(null, "".toCharArray())
    }

    fun loadCert(certLocation: String): X509Certificate = CertificateFactory.getInstance("X509").generateCertificate(
            resourceLoader.getResource(certLocation).inputStream
    ) as X509Certificate

    fun loadPrivateKey(privateKeyLocation: String): RSAPrivateKey = KeyFactory.getInstance("RSA").generatePrivate(
            PKCS8EncodedKeySpec(
                    StreamUtils.copyToByteArray(
                            resourceLoader.getResource(privateKeyLocation).inputStream
                    )
            )
    ) as RSAPrivateKey
}
