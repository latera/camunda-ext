package org.camunda.latera.bss.HttpClient

import org.camunda.latera.bss.HttpClient.JasperClient
import org.camunda.latera.bss.HttpClient.JasperMockClient
import io.ktor.client.HttpClient


object HttpProcessor {
  val testEnv: Boolean = System.getenv("CAMUNDA_EXT_ENV") == "test"

  fun getJasperClient(user: String, password: String, useSSL: Boolean = true) : HttpClient {
    if (this.testEnv) {
      return JasperMockClient.getClient();
    } else {
      return JasperClient.getClient(user, password, useSSL);
    }
  }
}
