import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.camunda.latera.bss.connectors.HOMS
import org.camunda.latera.bss.testing.TestExecution

class HomsTest {
    @Test
    @DisplayName("First test")
    fun firstTest() {
      print("first test")
      assertThat("Test").isEqualTo("Test")
    }
}
