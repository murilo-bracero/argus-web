package br.com.argus.authapi.service.impl

import br.com.argus.authapi.service.EncryptionService
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.encrypt.Encryptors
import org.springframework.security.crypto.encrypt.TextEncryptor
import org.springframework.security.crypto.keygen.KeyGenerators
import org.springframework.stereotype.Service
import org.springframework.util.Base64Utils
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import kotlin.math.sign

@Service
class EncryptionServiceImpl(
    @Value("\${security.secret-key}")
    private val secretKey: String,

    @Value("\${security.secret-size}")
    private val secretSize: Int
) : EncryptionService {
    override fun encrypt(raw: String): String {
        val salt = KeyGenerators.string().generateKey()

        return Encryptors.text(secretKey, salt)
            .encrypt(raw) + salt
    }

    override fun decrypt(digest: String): String {
        val salt = digest.substring(digest.length - 16)

        val hash = digest.substring(0, digest.length - 16)

        return Encryptors.text(secretKey,salt)
            .decrypt(hash)
    }

    override fun generateSecret(): String {
        val byteArray = ByteArray(secretSize)
        SecureRandom().nextBytes(byteArray)

        return Base64Utils.encodeToString(byteArray)
    }

    override fun compare(raw: String, digest: String): Boolean {
        TODO("Not yet implemented")
    }
}