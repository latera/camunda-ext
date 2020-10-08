package org.camunda.latera.bss.HttpClient

import org.camunda.latera.bss.connectors.Jasper.*
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
            "/reportExecutions/123/exports/123/attachments/example.js" -> {
              val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Text.JavaScript.toString()))
              respond("Report JS attachment", headers = responseHeaders)
            }
            "/reportExecutions/123/exports/123/attachments/image.png" -> {
              val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Image.Any.toString()))
              respond("Report image attachment", headers = responseHeaders)
            }
            "/reportExecutions/123" -> {
              val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))

              val optionsWithoutBaseUrl: Options = Options(
                outputFormat = "html",
                attachmentsPrefix = "./images/",
                allowInlineScripts = false)
              val options: Options = Options(
                outputFormat = "html",
                attachmentsPrefix = "{contextPath}/rest_v2/reportExecutions/{reportExecutionId}/exports/{exportExecutionId}/attachments/",
                baseUrl = "http://localhost:8080/jasperserver-pro",
                allowInlineScripts = true)
              val outputResource: OutputResource = OutputResource(
                contentType = "text/html")
              val attachments: Array<Attachment> = arrayOf(Attachment(
                contentType = "image/png",
                fileName = "img_0_0_0"))
              val exports: Array<DetailsExport> = arrayOf(
                DetailsExport(
                  id = "195a65cb-1762-450a-be2b-1196a02bb625",
                  options = optionsWithoutBaseUrl,
                  status = "ready",
                  outputResource = outputResource,
                  attachments = attachments),
                DetailsExport(
                  id = "4bac4889-0e63-4f09-bbe8-9593674f0700",
                  options = options,
                  status = "ready",
                  outputResource = outputResource,
                  attachments = attachments))
              val response: ExecutionDetailsResponse = ExecutionDetailsResponse(
                status = "ready",
                totalPages = 47,
                requestId = "b487a05a-4989-8b53-b2b9-b54752f998c4",
                reportURI = "/reports/samples/AllAccounts",
                exports = exports)

              respond(Gson().toJson(response), headers = responseHeaders)
            }
            "/reportExecutions/123/status" -> {
              val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
              val response: ExecutionStatusResponse = ExecutionStatusResponse(
                value = "ready")

              respond(Gson().toJson(response), headers = responseHeaders)
            }
            "/reportExecutions/234/status" -> {
              val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))

              val parameters = arrayOf("Specify a valid value for type Integer.")
              val errorDescriptor: ErrorDescriptor = ErrorDescriptor(
                message = "Input controls validation failure",
                errorCode = "input.controls.validation.error",
                parameters = parameters)
              val response: ExecutionStatusResponse = ExecutionStatusResponse(
                value = "failed",
                errorDescriptor = errorDescriptor)

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
