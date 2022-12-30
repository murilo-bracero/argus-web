package br.com.argus.customerbackend.services.impl

import br.com.argus.customerbackend.config.IdpProperties
import br.com.argus.customerbackend.dto.CreateCredentialsRequest
import br.com.argus.customerbackend.exception.CustomerException
import br.com.argus.customerbackend.services.AuthenticationService
import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@Service
class AuthenticationServiceImpl(
    @Value("\${services.keycloak.createUserUri}") private val createUserUri: String,
    private val idpProperties: IdpProperties,
    private val retryTemplate: RetryTemplate,
    private val restTemplate: RestTemplate
) : AuthenticationService {

    private val log = KotlinLogging.logger {}

    override fun createCredentials(createCredentialsRequest: CreateCredentialsRequest) {
        val token = getToken()

        val userRepresentation = UserRepresentation()
        userRepresentation.email = createCredentialsRequest.email
        userRepresentation.id = createCredentialsRequest.userId
        userRepresentation.isEnabled = true

        val credentialsRepresentation = CredentialRepresentation()
        credentialsRepresentation.type = CredentialRepresentation.PASSWORD
        credentialsRepresentation.value = createCredentialsRequest.password

        userRepresentation.credentials = listOf(credentialsRepresentation)

        try {
            log.info { "[${createCredentialsRequest.userId}] Trying to create user credentials" }
            retryTemplate.execute<Unit, RestClientException> {
                log.info { "[${createCredentialsRequest.userId}] Trying attempt: ${it.retryCount}" }
                createIdpUser(token, userRepresentation)
                log.info { "[${createCredentialsRequest.userId}] Credentials created successfully" }
            }
        } catch (e: Exception){
            log.error { "[${createCredentialsRequest.userId}] Could not create user credentials. $e" }
            throw e
        }
    }

    private fun createIdpUser(token: String, userRepresentation: UserRepresentation): String {
        val httpHeaders = HttpHeaders()
        httpHeaders.setBearerAuth(token)

        val entity = HttpEntity<UserRepresentation>(userRepresentation, httpHeaders)

        val responseEntity = restTemplate.exchange(createUserUri, HttpMethod.POST, entity, Unit::class.java)

        val location = responseEntity.headers.get("Location")

        if(location != null){
            return location.first().split("/").first()
        }

        throw CustomerException("AS001", "Could not get user id from IDP server")
    }

    override fun deleteCredentials(idpId: String) {
        try {
            log.info { "[$idpId] Trying to delete user credentials" }
            retryTemplate.execute<Unit, RestClientException> {
                log.info { "[userId] Trying attempt: ${it.retryCount}" }
                deleteIdpUser(idpId)
                log.info { "[$idpId] Credentials deleted successfully" }
            }
        } catch (e: Exception){
            log.info { "[$idpId] Could not delete credentials" }
            throw e
        }
    }

    private fun deleteIdpUser(idpId: String) {
        val token = getToken()
        val httpHeaders = HttpHeaders()
        httpHeaders.setBearerAuth(token)

        val entity = HttpEntity<UserRepresentation>(httpHeaders)

        val url = "$createUserUri/$idpId"

        restTemplate.exchange(url, HttpMethod.DELETE, entity, Unit::class.java)
    }

    private fun getToken(): String{
        val httpHeaders = HttpHeaders()
        httpHeaders.setBasicAuth(idpProperties.appName, idpProperties.serviceAccount)
        httpHeaders.contentType = APPLICATION_FORM_URLENCODED

        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        map.add("grant_type", "client_credentials")

        val entity = HttpEntity<MultiValueMap<String, String>>(map, httpHeaders)

        val jsonNode = restTemplate.exchange(idpProperties.tokenUri, HttpMethod.POST, entity, JsonNode::class.java).body

        if (jsonNode != null) {
            return jsonNode.get("access_token").asText()
        }

        throw CustomerException("AS002", "Could not get token from IDP service")
    }
}