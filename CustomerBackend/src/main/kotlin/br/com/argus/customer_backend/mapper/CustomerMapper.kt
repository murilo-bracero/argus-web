package br.com.argus.customer_backend.mapper

import br.com.argus.customer_backend.dto.CustomerResponse
import br.com.argus.customer_backend.models.Customer
import org.bson.types.ObjectId

class CustomerMapper {

    companion object {
        fun toDto(model: Customer): CustomerResponse{
            return CustomerResponse(
                objectIdToString(model.id),
                model.cpf,
                model.name,
                model.email,
                model.profilePicUri,
                model.favouriteTechnicians.map { objectIdToString(it) },
                model.devices.map { objectIdToString(it) },
                model.history.map { objectIdToString(it) })
        }

        private fun objectIdToString(objectId: ObjectId): String {
            return objectId.toHexString()
        }
    }

}