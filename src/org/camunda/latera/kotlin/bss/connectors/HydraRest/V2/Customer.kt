package org.camunda.latera.bss.connectors.HydraRest.V2

import org.camunda.latera.bss.HttpClient.HydraRestClient
import io.ktor.client.request.*
import kotlinx.coroutines.*
import java.util.*


class Customer(
  val url: String,
  val port: Int,
  val token: String,
  val useSSL: Boolean = true
) {
  private val client = HydraRestClient.getClient(this.url, this.port, this.token, this.useSSL)

  data class CreateParams(
    val vc_code: String,
    val n_base_subject_id: Long,
    val t_tags: Array<String>? = null,
    val n_subj_group_id: Long,
    val group_ids: Array<Int>? = null,
    val vc_rem: String? = null,
    val n_reseller_id: Long? = null,
    val additional_values: Array<Any> = arrayOf<Any>())

  data class Customer(
    val n_subject_id: Long,
    val n_customer_id: Long,
    val n_base_subject_id: Long,
    val vc_base_subject_name: String,
    val n_base_subj_type_id: Long, // сделать enum
    val n_subj_state_id: Long, // сделать enum
    val vc_name: String,
    val vc_code: String,
    val d_created: Date,
    val t_tags: Array<String>,
    val vc_rem: String?,
    val n_firm_id: Long,
    val n_subj_group_id: Long,
    val n_reseller_id: Long?,
    val group_ids: Array<Long>,
    val vc_base_subject_code: String,
    val additional_values: Array<Any>)

  data class CreateResp(
    val customer: Customer)

  fun create(params: CreateParams): Customer {
    val client = this.client

    val response = runBlocking {
      client.post<CreateResp> {
        url {
          encodedPath = "/rest/v2/subjects/customers/"
        }

        body = mapOf("customer" to params)
      }
    }

    return response.customer
  }
}
