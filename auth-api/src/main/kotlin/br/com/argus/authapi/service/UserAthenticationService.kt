package br.com.argus.authapi.service

import br.com.argus.authapi.model.AuthResultEnum
import br.com.argus.authapi.model.SystemEnum
import br.com.argus.authapi.model.User
import java.util.*

interface UserAthenticationService {

    fun login(email: String, rawPassword: String, code: String?, system: SystemEnum): AuthResultEnum

    fun findByToken(token: String): Optional<User>
}