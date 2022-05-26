package br.com.argus.authapi.dto

class CustomerResponseDTO(
    override val id: String = "",
    override val cpf: String = "",
    override val email: String = "",
    override val name: String = "",
    override val profilePicUri: String = "",
    val favs: HashMap<String, Boolean> = HashMap()
): UserResponseDTO() {
}