package org.camunda.latera.bss.connectors.hoper.hydra

trait Main {
  LinkedHashMap nvlParams(LinkedHashMap input) {
    def params = [:]
    input.each { key, value ->
      if (value != null) {
        if (value == 'null' || value == 'NULL') {
          params[key] = null
        } else {
          params[key] = value
        }
      }
    }

    return params
  }
}