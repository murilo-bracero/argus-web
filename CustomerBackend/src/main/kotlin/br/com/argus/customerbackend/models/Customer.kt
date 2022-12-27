package br.com.argus.customerbackend.models

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = "customers")
class Customer(

    @Id
    @Field("_id")
    val id: ObjectId = ObjectId.get(),

    @Indexed(unique = true)
    val cpf: String = "",

    val name: String = "",

    @Field("phone_number")
    val phoneNumber: String = "",

    val email: String = "",

    @Field("profile_pic_uri")
    val profilePicUri: String = "",

    val address: Address = Address(),

    @Field("favourite_technicians")
    val favouriteTechnicians: List<ObjectId> = ArrayList(),

    @Field("idp_id")
    val idpId: String = "",

    val devices: List<ObjectId> = ArrayList(),

    val history: List<ObjectId> = ArrayList()
)