package org.camunda.latera.bss.HttpClient

import io.ktor.client.request.*
import io.ktor.client.HttpClient
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.*
import io.ktor.client.features.json.JsonFeature
import org.apache.http.ssl.SSLContextBuilder
import org.apache.http.conn.ssl.*

object HydraRestClient {
  fun getClient(hydraHost: String, hydraPort: Int = 443, token: String? = null, useSSL: Boolean = true): HttpClient {
    return HttpClient(Apache) {
      install(JsonFeature) {
        serializer = GsonSerializer{
          disableHtmlEscaping()
        }
      }

      if (!useSSL) {
        engine {
          customizeClient {
            setSSLContext(
                SSLContextBuilder.create()
                    .loadTrustMaterial(TrustAllStrategy())
                    .build()
            )
            setSSLHostnameVerifier(NoopHostnameVerifier())
          }
        }
      }

      defaultRequest {
        host = hydraHost
        port = hydraPort
        if (token != null) {
          header("Authorization", "Token token=$token")
        }
        header("Accept", "application/json")
        header("Content-Type", "application/json")
      }
    }
  }
}
