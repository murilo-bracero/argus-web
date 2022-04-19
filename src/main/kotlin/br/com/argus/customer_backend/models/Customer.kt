package br.com.argus.customer_backend.models

import br.com.argus.customer_backend.dto.CustomerRequestDTO
import br.com.argus.customer_backend.dto.CustomerResponseDTO
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.security.crypto.password.PasswordEncoder

@Document(collection = "customers")
class Customer(
    @Id
    val id: ObjectId = ObjectId.get(),

    @Indexed(unique = true)
    val cpf: String,

    val name: String,

    @Indexed(unique = true)
    val email: String,

    val password: String,

    val profilePicUri: String,

    val favs: Map<String, String> = HashMap()
) {
    companion object {
        fun from(dto: CustomerRequestDTO, pe: PasswordEncoder): Customer {
            return Customer(
                ObjectId.get(),
                dto.cpf,
                dto.name,
                dto.email,
                pe.encode(dto.password),
                dto.profilePicUri,
                HashMap()
            )
        }
    }

    fun to(): CustomerResponseDTO {
        return CustomerResponseDTO(
            id.toHexString(),
            cpf,
            name,
            email,
            profilePicUri,
            favs
        )
    }
}