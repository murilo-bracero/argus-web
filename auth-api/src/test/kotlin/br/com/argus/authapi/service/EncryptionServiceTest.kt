package br.com.argus.authapi.service

import br.com.argus.authapi.service.impl.EncryptionServiceImpl
import com.ninjasquad.springmockk.MockkBean
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class EncryptionServiceTest {

    private lateinit var encryptionService: EncryptionServiceImpl

    private val secretKey = "supersecretkey"
    private val secretSize = 16

    @BeforeEach
    fun setUp(){
        encryptionService = EncryptionServiceImpl(secretKey, secretSize)
    }

    @Test
    fun `should encrypt data correctly based on key given`(){
        val raw = "test_encrypt"

        val encrypted = encryptionService.encrypt(raw)

        assertNotEquals("", encrypted)

        assertEquals(raw, encryptionService.decrypt(encrypted))
    }

}
