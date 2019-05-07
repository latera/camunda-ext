package org.camunda.latera.bss.connectors.hoper.hydra

trait Main {
  LinkedHashMap nvlParams(LinkedHashMap input) {
    def params = [:]
    input.each { key, value ->
      if (value != null) {
        params[key] = value
      }
    }

    return params
  }
}