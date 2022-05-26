package br.com.argus.authapi.service

interface EncryptionService {

    fun encrypt(raw: String): String

    fun decrypt(digest: String): String

    fun generateSecret(): String

    fun compare(raw: String, digest: String): Boolean
}