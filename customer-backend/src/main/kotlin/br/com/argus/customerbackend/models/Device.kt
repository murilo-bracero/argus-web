package br.com.argus.customerbackend.models

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDate

@Document(collection = "devices")
class Device(

    @Id
    @Field("_id")
    val id: ObjectId = ObjectId.get(),

    val name: String,

    @Field("first_owner")
    val firstOwner: Boolean,

    @Field("purchased_at")
    val purchasedAt: LocalDate,

    val model: ObjectId
){
}