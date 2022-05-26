package br.com.argus.authapi.dto

class TechnicianResponseDTO(
    override val id: String,
    override val cpf: String,
    override val email: String,
    override val name: String,
    override val profilePicUri: String
) : UserResponseDTO() {
}