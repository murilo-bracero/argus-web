package br.com.argus.authapi.dto

import br.com.argus.authapi.annotations.OriginSystemValid
import br.com.argus.authapi.model.SystemEnum
import br.com.argus.authapi.model.UserCredential
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import javax.validation.constraints.NotNull

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class UpdateUserCredentialsDTO(
    val mfaEnabled: Boolean?,
    val accountExpired: Boolean?,
    val accountLocked: Boolean?,
    val credentialsExpired: Boolean?,
    val isEnabled: Boolean?
) {
    fun fill(userCredential: UserCredential) {
        userCredential.mfaEnabled = mfaEnabled ?: userCredential.mfaEnabled
        userCredential.accountExpired = accountExpired ?: userCredential.accountExpired
        userCredential.accountLocked = accountLocked ?: userCredential.accountLocked
        userCredential.credentialsExpired = credentialsExpired ?: userCredential.credentialsExpired
        userCredential.isEnabled = isEnabled ?: userCredential.isEnabled
    }
}
