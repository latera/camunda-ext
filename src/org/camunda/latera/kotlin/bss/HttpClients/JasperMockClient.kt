package org.camunda.latera.bss.HttpClient

import org.camunda.latera.bss.connectors.ExecuteReportResponse
import org.camunda.latera.bss.connectors.Export
import io.ktor.client.request.*
import io.ktor.client.HttpClient
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.engine.mock.*
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.*
import com.google.gson.*;

object JasperMockClient {
  fun getClient(): HttpClient {
    return HttpClient(MockEngine) {
      engine {
        addHandler { request ->
          when (request.url.fullPath) {
            "/reportExecutions/123/exports/123/outputResource" -> {
              val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Text.Html.toString()))
              respond("Report result", headers = responseHeaders)
            }
            "/reportExecutions" -> {
              val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))

              val exports: Array<Export> = arrayOf(Export(id = "html", status = "queued"))
              val response: ExecuteReportResponse = ExecuteReportResponse(
                currentPage = 1,
                reportURI = "/supermart/details/CustomerDetailReport",
                requestId = "f3a9805a-4089-4b53-b9e9-b54752f91586",
                status = "execution",
                exports = exports)

              respond(Gson().toJson(response), headers = responseHeaders)
            }
            else -> {
              val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Text.Plain.toString()))
              respond("Wrong request", headers = responseHeaders)
            }
          }
        }
      }

      install(JsonFeature) {
        serializer = GsonSerializer{
          disableHtmlEscaping()
        }
      }
    }
  }
}
