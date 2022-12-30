package br.com.argus.customerbackend.utils

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class SignUpUtilsKtTest {

    @Test
    fun isCpfValidTrueWhenValidCpfWhereGiven() {
        val cpf = "06176720087"
        val res = isCpfValid(cpf)
        assertTrue(res)
    }

    @Test
    fun isCpfValidTrueWhenInvalidCpfWhereGiven() {
        val cpf = "12345678910"
        val res = isCpfValid(cpf)
        assertFalse(res)
    }

    @Test
    fun isCpfValidTrueWhenForbiddenCpfWereGiven() {
        val cpf = "1111111111"
        val res = isCpfValid(cpf)
        assertFalse(res)
    }
}