package org.camunda.latera.bss.connectors.HydraRest.V2

import org.camunda.latera.bss.HttpClient.HydraRestClient
import io.ktor.client.request.*
import kotlinx.coroutines.*
import java.math.BigDecimal
import java.util.*
import kotlin.collections.LinkedHashMap


class CustomerAccount(
  val url: String,
  val port: Int,
  val token: String,
  val useSSL: Boolean = true
) {
  private val client = HydraRestClient.getClient(this.url, this.port, this.token, this.useSSL)

  data class CreateParams(
    val n_currency_id: Long, // enum
    val vc_code: String? = null,
    val vc_name: String? = null,
    val vc_account: String? = null,
    val vc_rem: String? = null,
    val n_max_credit_limit: BigDecimal? = null)

  data class Credit(
    val n_sum: BigDecimal?,
    val d_end: Date?)

  data class CustomerAccount(
    val n_account_id: Long,
    val n_currency_id: Long,
    val vc_currency_code: String,
    val vc_name: String,
    val vc_code: String,
    val vc_account: String,
    val vc_rem: String?,
    val n_sum_bal: BigDecimal,
    val n_sum_free: BigDecimal,
    val n_max_credit_limit: BigDecimal?,
    val n_sum_reserved_total: BigDecimal,
    val n_sum_reserved: BigDecimal,
    val has_actual_atu: Boolean,
    val permanent_credit_limit: LinkedHashMap<String, BigDecimal?>,
    val temporary_credit_limit: Credit,
    val scheduled_services_credit_limit: Credit,
    val unscheduled_services_credit_limit: Credit,
    val actual_charge_logs_amount: BigDecimal)

  data class CreateResp(
    val account : CustomerAccount)

  fun create(params: CreateParams, customer: Long): CustomerAccount {
    val client = this.client

    val response = runBlocking {
      client.post<CreateResp> {
        url {
          encodedPath = "/rest/v2/subjects/customers/$customer/accounts"
        }

        body = mapOf("account" to params)
      }
    }

    return response.account
  }
}
