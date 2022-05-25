package br.com.argus.authapi.model

import br.com.argus.authapi.CreateCredentialsRequest
import br.com.argus.authapi.Credentials
import br.com.argus.authapi.SYSTEM_ENUM
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

    @Field("mfa_enabled")
    var mfaEnabled: Boolean = false,

    var system: SystemEnum = SystemEnum.UNKNOWN,

    @Field("account_expired")
    var accountExpired: Boolean = false,

    @Field("account_locked")
    var accountLocked: Boolean = false,

    @Field("credentials_expired")
    var credentialsExpired: Boolean = false,

    @Field("is_enabled")
    var isEnabled: Boolean = true,

    @Field("refresh_token")
    var refreshToken: String = "",

    @Field("updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

) {
    companion object {
        fun from(dto: CreateCredentialsRequest): UserCredential {
            return UserCredential(
                userId = dto.userId,
                system = SystemEnum.valueOf(dto.system.name)
            )
        }
    }

    fun to(): Credentials {
        val builder = Credentials.newBuilder()
        builder.id = id.toHexString()
        builder.userId = userId
        builder.mfaEnabled = mfaEnabled
        builder.system = SYSTEM_ENUM.valueOf(system.name)
        builder.accountExpired = accountExpired
        builder.accountLocked = accountLocked
        builder.credentialsExpired = credentialsExpired
        builder.isEnabled = isEnabled
        builder.updatedAt = updatedAt.toString()
        return builder.build()
    }
}