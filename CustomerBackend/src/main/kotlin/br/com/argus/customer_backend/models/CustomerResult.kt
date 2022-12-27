package br.com.argus.customer_backend.models

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.Field

class CustomerResult(
    @Field("_id")
    val id: ObjectId = ObjectId.get(),

    val cpf: String = "",

    val name: String = "",

    @Field("profile_pic_uri")
    val profilePicUri: String = "",

    val address: Address,

    @Field("favourite_technicians")
    val favouriteTechnicians: List<ObjectId> = ArrayList(),

    val devices: List<Device> = ArrayList(),

    val history: List<SupportHistory> = ArrayList()
) {}