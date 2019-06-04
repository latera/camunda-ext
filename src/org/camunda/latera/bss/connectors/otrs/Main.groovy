package org.camunda.latera.bss.connectors.otrs

import org.camunda.latera.bss.utils.DateTimeUtil
import org.camunda.latera.bss.utils.MapUtil

trait Main {
  LinkedHashMap prepareParams(LinkedHashMap input) {
    return MapUtil.nvl(input)
  }

  LinkedHashMap convertKeys(LinkedHashMap input) {
    return MapUtil.camelizeKeys(input, true)
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