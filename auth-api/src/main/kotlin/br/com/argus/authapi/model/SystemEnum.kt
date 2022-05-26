package br.com.argus.authapi.model

enum class SystemEnum(val collection: String) {

    CUSTOMER("customers"),
    TECHNICIAN("technicians"),
    UNKNOWN("_");
}