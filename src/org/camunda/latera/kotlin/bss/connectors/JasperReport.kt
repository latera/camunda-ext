package org.camunda.latera.bss.connectors

import org.camunda.latera.bss.HttpClient.HttpProcessor
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.*

data class Export(
             val id: String,
             val status: String)

data class ExecuteReportParams(
             val reportUnitUri: String,
             val parameters: Map<String, Any>,
             val outputFormat: String = "pdf",
             val freshData: Boolean = false,
             val saveDataSnapshot: Boolean = false,
             val interactive: Boolean = true,
             val allowInlineScripts: Boolean = true,
             val ignorePagination: Boolean? = null,
             val pages: Int? = null,
             val async: Boolean = false,
             val transformerKey: String? = null,
             val attachmentsPrefix: String = "attachments",
             val baseUrl: String? = null)

data class ExecuteReportResponse(
             val currentPage: Int,
             val exports: Array<Export>,
             val reportURI: String,
             val requestId: String,
             val status: String)

class JasperReport(val url: String, val user: String, val password: String) {
  private val baseUrl: java.net.URI = java.net.URI(this.url)

  fun executeReport(params: ExecuteReportParams) : ExecuteReportResponse {
    val executeReportUrl: String = this.baseUrl.resolve("/reportExecutions").toString()

    val client = HttpProcessor.getJasperClient(this.user, this.password)
    val response = runBlocking {
      client.post<ExecuteReportResponse>(executeReportUrl){
        contentType(ContentType.Application.Json)
        body = params
      }
    }

    client.close()
    return response
  }

  fun getReportResult(requestId: String, exportId: String):  String {
    val reportResultUrl: String = this.baseUrl.resolve("/reportExecutions/${requestId}/exports/${exportId}/outputResource").toString()

    val client = HttpProcessor.getJasperClient(this.user, this.password)
    val response = runBlocking {
      client.get<String>(reportResultUrl){
        contentType(ContentType.Application.Json)
      }
    }

    client.close()
    return response
  }
}
