package br.com.argus.authapi.dto

abstract class UserResponseDTO {

    abstract val id: String
    abstract val cpf: String
    abstract val email: String
    abstract val name: String
    abstract val profilePicUri: String

}