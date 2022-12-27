package br.com.argus.customerbackend.models

import org.springframework.data.mongodb.core.mapping.Field

class Address(
    @Field("public_place")
    val publicPlace: String,

    val number: Int,

    @Field("zip_code")
    val zipCode: String,

    val complement: String
) {

    constructor() : this("", 0, "", "")

}