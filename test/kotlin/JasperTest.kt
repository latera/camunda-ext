import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.function.Executable
import org.camunda.latera.bss.connectors.JasperReport
import org.camunda.latera.bss.connectors.ExecuteReportParams


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
      val params: ExecuteReportParams = ExecuteReportParams(
        reportUnitUri = "testReport",
        parameters = emptyMap<String, Any>()
      )
      val result = report.executeReport(params)
      assertAll("Should return ExecuteReportParams",
        Executable { assertEquals(result.currentPage, 1) },
        Executable { assertEquals(result.reportURI, "/supermart/details/CustomerDetailReport") },
        Executable { assertEquals(result.requestId, "f3a9805a-4089-4b53-b9e9-b54752f91586") },
        Executable { assertEquals(result.status, "execution") },
        Executable { assertEquals(result.exports[0].id, "html") },
        Executable { assertEquals(result.exports[0].status, "queued") }
      )
    }
}
