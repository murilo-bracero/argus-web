package br.com.argus.authapi.service

import br.com.argus.authapi.model.SystemEnum
import br.com.argus.authapi.model.UserCredential

interface CredentialsService {

    fun createCredential(cred: UserCredential)

    fun remove(userId: String, system: SystemEnum)

    fun find(userId: String, system: SystemEnum): UserCredential

    fun update(newCreds: UserCredential): String?

}