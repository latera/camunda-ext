package org.camunda.latera.bss.utils
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.latera.bss.utils.StringUtil

class Order {
  static LinkedHashMap getData(DelegateExecution execution) {
    LinkedHashMap data = [:]
    execution.getVariables().each { key, value ->
      if (key =~ /^homsOrderData/ && key != 'homsOrderDataUploadedFile') {
        String dataKey = key.replaceFirst(/^homsOrderData/, "")
        data[StringUtil.decapitalize(dataKey)] = value
      }
    }
    return data
  }

  static void saveData(Map data, DelegateExecution execution) {
    data.each { key, value ->
      if (key != 'uploadedFile') {
        execution.setVariable("homsOrderData${StringUtil.capitalize(key)}", value)
      }
    }
  }
}