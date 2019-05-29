package org.camunda.latera.bss.connectors.otrs

import org.camunda.latera.bss.utils.DateTimeUtil
import org.camunda.latera.bss.utils.StringUtil

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

  LinkedHashMap convertKeys(LinkedHashMap input) {
    return StringUtil.camelizeKeys(input, true)
  }

  def convertValue(def value) {
    if (value instanceof Boolean) {
      return value ? 1 : 0
    }
    return value
  }

  LinkedHashMap convertParams(LinkedHashMap input) {
    LinkedHashMap result = [:]
    input.each { key, value ->
      result[key] = convertValue(value)
    }
  }
}