package br.com.argus.service

interface IdpService {

    fun createUser(email: String, password: String): String

    fun deleteUser(idpId: String)

}