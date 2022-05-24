package br.com.argus.authapi.model

import br.com.argus.authapi.dto.CreateUserCredentialsDTO
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime

@Document(collection = "user_credentials")
class UserCredential(
    @Id
    val id: ObjectId = ObjectId.get(),

    @Indexed(unique = true)
    @Field("user_id")
    val userId: String = "",

    @Indexed(unique = true)
    var secret: String = "",

    var mfaEnabled: Boolean = false,

    var system: SystemEnum = SystemEnum.UNKNOWN,

    var accountExpired: Boolean = false,

    var accountLocked: Boolean = false,

    var credentialsExpired: Boolean = false,

    var isEnabled: Boolean = true,

    var refreshToken: String = "",

    var updatedAt: LocalDateTime = LocalDateTime.now(),

) {
    companion object {
        fun from(dto: CreateUserCredentialsDTO): UserCredential {
            return UserCredential(
                userId = dto.userId,
                system = dto.userSystem
            )
        }
    }
}