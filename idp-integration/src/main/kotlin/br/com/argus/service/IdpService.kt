package br.com.argus.service

import io.smallrye.mutiny.Uni

interface IdpService {

    fun createUser(email: String, password: String): Uni<String>

    fun deleteUser(idpId: String): Uni<Unit>

}