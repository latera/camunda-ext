package org.camunda.latera.bss.connectors.HydraRest.V2

import org.camunda.latera.bss.HttpClient.HydraRestClient
import io.ktor.client.request.*
import kotlinx.coroutines.*


class Login(
  val url: String,
  val port: Int,
  val useSSL: Boolean = true
) {
  private val client = HydraRestClient.getClient(this.url, this.port, null, this.useSSL)

  data class LoginParams(
    val login: String,
    val password: String,
    val app_code: String? = null)


  data class LoginResponse(
    val session: Session
  )

  data class Session(
    val token: String
  )

  fun login(params: LoginParams): LoginResponse {
    val client = this.client

    val response = runBlocking {
      client.post<LoginResponse> {
        url {
          encodedPath = "/rest/v2/login"
        }

        body = mapOf ("session" to params)
      }
    }

    return response
  }
}
