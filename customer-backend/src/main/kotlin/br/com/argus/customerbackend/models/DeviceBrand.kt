package br.com.argus.customerbackend.models

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = "device_brands")
class DeviceBrand(
    @Id
    @Field("_id")
    val id: ObjectId = ObjectId.get(),
    val name: String
) {
}