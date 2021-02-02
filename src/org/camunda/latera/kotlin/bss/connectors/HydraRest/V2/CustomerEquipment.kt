package org.camunda.latera.bss.connectors.HydraRest.V2

import org.camunda.latera.bss.HttpClient.HydraRestClient
import io.ktor.client.request.*
import kotlinx.coroutines.*
import java.math.BigDecimal
import java.util.*


class CustomerEquipment(
  val url: String,
  val port: Int,
  val token: String,
  val useSSL: Boolean = true
) {
  private val client = HydraRestClient.getClient(this.url, this.port, this.token, this.useSSL)

  data class CreateParams(
    val n_good_id: Long,
    val vc_code: String? = null)

  data class CustomerEquipment(
    val n_object_id: Long,
    val n_good_id: Long,
    val n_firm_id: Long,
    val n_obj_state_id: Long,
    val vc_name: String,
    val vc_code: String,
    val vc_code_add: String?,
    val vc_rem: String?,
    val n_main_object_id: Long?,
    val n_owner_id: Long,
    val vc_serial_number: String?,
    val vc_inventory_number: String?,
    val d_warranty_end: String?,
    val additional_values: Array<Any>)

  data class CreateResp(
    val equipment : CustomerEquipment)

  fun create(params: CreateParams, customer: Long): CustomerEquipment {
    val client = this.client

    val response = runBlocking {
      client.post<CreateResp> {
        url {
          encodedPath = "/rest/v2/subjects/customers/$customer/equipment"
        }

        body = mapOf("equipment" to params)
      }
    }

    return response.equipment
  }
}
