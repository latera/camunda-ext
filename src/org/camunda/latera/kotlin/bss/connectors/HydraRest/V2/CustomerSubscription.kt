package org.camunda.latera.bss.connectors.HydraRest.V2

import org.camunda.latera.bss.HttpClient.HydraRestClient
import io.ktor.client.request.*
import kotlinx.coroutines.*
import java.util.*


class CustomerSubscription(
  val url: String,
  val port: Int,
  val token: String,
  val useSSL: Boolean = true
) {
  private val client = HydraRestClient.getClient(this.url, this.port, this.token, this.useSSL)

  data class CreateParams(
    val n_service_id: Long,
    val n_account_id: Long,
    val n_object_id: Long,
    val n_contract_id: Long,
    val d_begin: Date? = null)

  data class CustomerSubscription(
    val n_subscription_id: Long,
    val n_service_id: Long,
    val n_customer_id: Long,
    val n_account_id: Long,
    val n_object_id: Long,
    val n_quant: Long?,
    val n_unit_id: Long?,
    val d_begin: Date,
    val d_end: Date?,
    val c_fl_closed: String,
    val n_line_no: Long,
    val n_par_subscription_id: Long?,
    val n_prev_subscription_id: Long?,
    val n_cl_creating_state_id: Long, //enum
    val n_contract_id: Long)

  data class CreateResp(
    val subscription : CustomerSubscription)

  fun create(params: CreateParams, customer: Long): CustomerSubscription {
    val client = this.client

    val response = runBlocking {
      client.post<CreateResp> {
        url {
          encodedPath = "/rest/v2/subjects/customers/$customer/subscriptions"
        }

        body = mapOf("subscription" to params)
      }
    }

    return response.subscription
  }
}
