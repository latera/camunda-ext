package org.camunda.latera.bss.utils
import org.camunda.bpm.engine.delegate.DelegateExecution

class Order {
  static LinkedHashMap getData(DelegateExecution execution) {
    LinkedHashMap data = [:]
    for (e in execution.getVariables()) {
      if (e.key =~ /^homsOrderData/) {
        String dataKey = e.key.replaceFirst(/^homsOrderData/, "")
        dataKey = (dataKey.getAt(0).toLowerCase() + dataKey.substring(1)).toString()
          data[dataKey] = e.value
      }
    }
    return data
  }
}