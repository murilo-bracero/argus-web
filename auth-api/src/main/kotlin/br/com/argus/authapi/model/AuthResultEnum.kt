package br.com.argus.authapi.model

enum class AuthResultEnum {

    AUTHENTICATED,
    NEED_MFA,
    MFA_DENIED,
    LOGIN_DENIED

}