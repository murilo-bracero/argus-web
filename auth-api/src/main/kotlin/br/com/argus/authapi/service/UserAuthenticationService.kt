package br.com.argus.authapi.service

import br.com.argus.authapi.model.SystemEnum
import br.com.argus.authapi.model.User

interface UserAuthenticationService {

    fun login(email: String, rawPassword: String, code: String?, system: SystemEnum): User
}