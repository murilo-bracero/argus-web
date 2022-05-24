package br.com.argus.authapi.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class User(
    @Id
    val id: ObjectId = ObjectId.get(),

    @Indexed(unique = true)
    val email: String,

    @Field("password")
    val hashPassword: String,
) : UserDetails {

    @Transient
    var credentials: UserCredential = UserCredential()

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        TODO("Not yet implemented")
    }

    override fun getPassword(): String {
        return hashPassword
    }

    override fun getUsername(): String {
        return email
    }

    override fun isAccountNonExpired(): Boolean {
        return !credentials.accountExpired
    }

    override fun isAccountNonLocked(): Boolean {
        return !credentials.accountLocked
    }

    override fun isCredentialsNonExpired(): Boolean {
        return !credentials.credentialsExpired
    }

    override fun isEnabled(): Boolean {
        return credentials.isEnabled
    }
}