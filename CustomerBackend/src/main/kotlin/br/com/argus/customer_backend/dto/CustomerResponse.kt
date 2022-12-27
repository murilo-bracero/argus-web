package br.com.argus.customer_backend.dto

data class CustomerResponse (
  val id: String,
  val cpf: String,
  val name: String,
  val email: String,
  val profilePicUri: String,
  val favs: List<String> = ArrayList(),
  val devices: List<String> = ArrayList(),
  val history: List<String> = ArrayList()
) {
}