import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.function.Executable
import org.camunda.latera.bss.connectors.Jasper.*


class JasperTest {
    @Test
    @DisplayName("Jasper connector getReportResult")
    fun getReportResult() {
      val report: JasperReport = JasperReport("http://127.0.0.1/", "test_user", "password")
      assertThat(report.getReportResult("123", "123")).isEqualTo("Report result")
    }

    @Test
    @DisplayName("Jasper connector executeReport")
    fun executeReport() {
      val report: JasperReport = JasperReport("http://127.0.0.1/", "test_user", "password")
      val params: ExecuteReportParams = report.createExecuteReportParams(
        linkedMapOf<String, Any>(
          "reportUnitUri" to "testReport",
          "parameters" to linkedMapOf<String, Any>()))

      val result = report.executeReport(params)
      assertAll("Should return ExecuteReportResponse",
        Executable { assertEquals(result.currentPage, 1) },
        Executable { assertEquals(result.reportURI, "/supermart/details/CustomerDetailReport") },
        Executable { assertEquals(result.requestId, "f3a9805a-4089-4b53-b9e9-b54752f91586") },
        Executable { assertEquals(result.status, "execution") },
        Executable { assertEquals(result.exports[0].id, "html") },
        Executable { assertEquals(result.exports[0].status, "queued") }
      )
    }

    @Test
    @DisplayName("Jasper connector getFileFromReport with JS file")
    fun getJSFileFromReport() {
      val report: JasperReport = JasperReport("http://127.0.0.1/", "test_user", "password")
      assertThat(report.getFileFromReport("123", "123", "example.js")).isEqualTo("Report JS attachment")
    }

    @Test
    @DisplayName("Jasper connector getFileFromReport with image file")
    fun getImageFileFromReport() {
      val report: JasperReport = JasperReport("http://127.0.0.1/", "test_user", "password")
      assertThat(report.getFileFromReport("123", "123", "image.png")).isEqualTo("Report image attachment")
    }

    @Test
    @DisplayName("Jasper connector getReportExecutionDetails")
    fun getReportExecutionDetails() {
      val report: JasperReport = JasperReport("http://127.0.0.1/", "test_user", "password")
      val result = report.getReportExecutionDetails("123")
      assertAll("Should return ExecutionDetailsResponse",
        Executable { assertEquals(result.status, "ready") },
        Executable { assertEquals(result.totalPages, 47) },
        Executable { assertEquals(result.requestId, "b487a05a-4989-8b53-b2b9-b54752f998c4") },
        Executable { assertEquals(result.reportURI, "/reports/samples/AllAccounts") },
        Executable { assertEquals(result.exports[0].id, "195a65cb-1762-450a-be2b-1196a02bb625") },
        Executable { assertEquals(result.exports[0].options.outputFormat, "html") },
        Executable { assertEquals(result.exports[0].options.attachmentsPrefix, "./images/") },
        Executable { assertEquals(result.exports[0].options.baseUrl, null) },
        Executable { assertEquals(result.exports[0].options.allowInlineScripts, false) },
        Executable { assertEquals(result.exports[0].status, "ready") },
        Executable { assertEquals(result.exports[0].outputResource.contentType, "text/html") },
        Executable { assertEquals(result.exports[0].attachments[0].contentType, "image/png") },
        Executable { assertEquals(result.exports[0].attachments[0].fileName, "img_0_0_0") },
        Executable { assertEquals(result.exports[1].id, "4bac4889-0e63-4f09-bbe8-9593674f0700") },
        Executable { assertEquals(result.exports[1].options.outputFormat, "html") },
        Executable { assertEquals(result.exports[1].options.attachmentsPrefix, "{contextPath}/rest_v2/reportExecutions/{reportExecutionId}/exports/{exportExecutionId}/attachments/") },
        Executable { assertEquals(result.exports[1].options.baseUrl, "http://localhost:8080/jasperserver-pro") },
        Executable { assertEquals(result.exports[1].options.allowInlineScripts, true) },
        Executable { assertEquals(result.exports[1].status, "ready") },
        Executable { assertEquals(result.exports[1].outputResource.contentType, "text/html") },
        Executable { assertEquals(result.exports[1].attachments[0].contentType, "image/png") },
        Executable { assertEquals(result.exports[1].attachments[0].fileName, "img_0_0_0") }
      )
    }

    @Test
    @DisplayName("Jasper connector getReportExecutionStatus with ready status")
    fun getReportExecutionReadyStatus() {
      val report: JasperReport = JasperReport("http://127.0.0.1/", "test_user", "password")
      val result = report.getReportExecutionStatus("123")
      assertAll("Should return ExecutionStatusResponse with ready status",
        Executable { assertEquals(result.value, "ready") },
        Executable { assertEquals(result.errorDescriptor, null) }
      )
    }

    @Test
    @DisplayName("Jasper connector getReportExecutionStatus with failed status")
    fun getReportExecutionFailedStatus() {
      val report: JasperReport = JasperReport("http://127.0.0.1/", "test_user", "password")
      val result = report.getReportExecutionStatus("234")
      assertAll("Should return ExecutionStatusResponse with failed status",
        Executable { assertEquals(result.value, "failed") },
        Executable { assertEquals(result.errorDescriptor?.message, "Input controls validation failure") },
        Executable { assertEquals(result.errorDescriptor?.errorCode, "input.controls.validation.error") },
        Executable { assertEquals(result.errorDescriptor?.parameters?.get(0), "Specify a valid value for type Integer.") }
      )
    }
}
