package org.camunda.latera.bss.util

class Order {
  static LinkedHashMap getData(execution) {
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