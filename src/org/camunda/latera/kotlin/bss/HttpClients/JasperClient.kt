package org.camunda.latera.bss.HttpClient

import io.ktor.client.request.*
import io.ktor.client.HttpClient
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.*
import io.ktor.client.features.json.JsonFeature

object JasperClient {
  fun getClient(user: String, passwd: String): HttpClient {
    return HttpClient(Apache) {
      install(JsonFeature) {
        serializer = GsonSerializer{
          disableHtmlEscaping()
        }
      }
      install(Auth) {
        basic {
          username = user
          password = passwd
        }
      }
    }
  }
}
