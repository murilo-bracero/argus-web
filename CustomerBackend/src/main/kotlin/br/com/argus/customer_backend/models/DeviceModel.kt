package br.com.argus.customer_backend.models

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = "device_models")
class DeviceModel(

    @Id
    @Field("_id")
    val id: ObjectId = ObjectId.get(),

    val name: String,

    @Field("brand_id")
    val brandId: ObjectId
){
}