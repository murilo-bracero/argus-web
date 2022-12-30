package br.com.argus.customerbackend.models

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime

@Document("customer_support_history")
class SupportHistory(

    @Id
    @Field("_id")
    val id: ObjectId = ObjectId.get(),

    @Field("technician_id")
    val technicianId: ObjectId,

    @Field("device_id")
    val deviceId: ObjectId,

    @Field("visit_timestamp")
    val visitTimestamp: LocalDateTime,

    @Field("solved_timestamp")
    val solvedTimestamp: LocalDateTime,

    val rating: Int
){}
