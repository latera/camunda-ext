package org.camunda.latera.bss.connectors.HydraRest.V2

import org.camunda.latera.bss.HttpClient.HydraRestClient
import io.ktor.client.request.*
import kotlinx.coroutines.*
import java.util.*


class CustomerContract(
  val url: String,
  val port: Int,
  val token: String,
  val useSSL: Boolean = true
) {
  private val client = HydraRestClient.getClient(this.url, this.port, this.token, this.useSSL)

  data class CreateParams(
    val n_doc_type_id: Long, // enum
    val n_provider_id: Long,
    val n_firm_id: Long,
    val n_workflow_id: Long,
    val n_parent_doc_id: Long? = null,
    val vc_doc_no: String? = null,
    val d_doc: String? = null,
    val d_begin: String? = null,
    val d_end: String? = null,
    val vc_rem: String? = null)

  data class CustomerContract(
    val n_doc_id: Long,
    val n_doc_type_id: Long, //enum
    val n_doc_state_id: Long, //enum
    val n_parent_doc_id: Long,
    val n_workflow_id: Long,
    val d_doc: String,
    val d_time: String,
    val vc_doc_no: String,
    val d_begin: String,
    val d_end: String?,
    val n_firm_id: Long,
    val vc_rem: String?,
    val n_provider_id: Long,
    val additional_values: Array<Any>?)

  data class CreateResp(
    val contract: CustomerContract)

  fun create(params: CreateParams, customer: Long): CustomerContract {
    val client = this.client

    val response = runBlocking {
      client.post<CreateResp> {
        url {
          encodedPath = "/rest/v2/subjects/customers/$customer/contracts"
        }

        body = mapOf("contract" to params)
      }
    }

    return response.contract
  }

  fun update(params: CustomerContract, customer: Long): CustomerContract {
    val client = this.client

    val response = runBlocking {
      client.put<CreateResp> {
        url {
          encodedPath = "/rest/v2/subjects/customers/$customer/contracts/${params.n_doc_id}"
        }

        body = mapOf("contract" to params)
      }
    }
    return response.contract
  }
}
