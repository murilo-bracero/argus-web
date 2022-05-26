package br.com.argus.authapi.service

import br.com.argus.authapi.service.impl.EncryptionServiceImpl
import com.ninjasquad.springmockk.MockkBean
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SpringExtension::class)
class EncryptionServiceTest {

    @Autowired
    private lateinit var encryptionService: EncryptionServiceImpl

    @Test
    fun `should encrypt data correctly based on key given`(){
        val raw = "test_encrypt"

        val encrypted = encryptionService.encrypt(raw)

        assertNotEquals("", encrypted)

        assertEquals(raw, encryptionService.decrypt(encrypted))
    }

}
