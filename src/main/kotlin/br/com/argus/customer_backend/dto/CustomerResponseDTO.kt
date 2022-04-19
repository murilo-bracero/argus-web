package br.com.argus.customer_backend.dto

class CustomerResponseDTO (
  val id: String,
  val cpf: String,
  val name: String,
  val email: String,
  val profilePicUri: String,
  val favs: Map<String, String> = HashMap()
)