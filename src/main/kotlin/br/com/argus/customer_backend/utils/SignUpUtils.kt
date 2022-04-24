package br.com.argus.customer_backend.utils

fun isCpfValid(cpf: String): Boolean{

    if(cpf.length != 11) return false

    if(cpf.reversed() == cpf) return false

    val digits = IntArray(11)
    var mult1 = 0
    var mult2 = 0
    var j = 10

    for (i in 0..10) {
        digits[i] = Integer.valueOf(cpf[i].toString())
    }

    for (i in 0..9) {
        if (j >= 2) {
            mult1 += digits[i] * j
        }
        j--
    }

    val secdigit1 = mult1 * 10 % 11
    if (secdigit1.toString() == digits[9].toString()) {
        j = 11
        for (i in 0..9) {
            if (j >= 2) {
                mult2 += digits[i] * j
            }
            j--
        }
        val secdigit2 = mult2 * 10 % 11
        if (secdigit2.toString() == digits[10].toString()) {
            return true
        }
    }

    return false
}