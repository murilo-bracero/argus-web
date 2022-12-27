package br.com.argus.customer_backend.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "idp")
data class IdpProperties(
    val appName: String,
    val serviceAccount: String,
    val tokenUri: String
)