package org.camunda.latera.bss.connectors.otrs

import static org.camunda.latera.bss.utils.MapUtil.camelizeKeys

private trait Main {
  private Map prepareParams(Map input) {
    return nvl(input)
  }

  private Map convertKeys(Map input) {
    return camelizeKeys(input, true)
  }

  private def convertValue(def value) {
    if (value instanceof Boolean) {
      return value ? 1 : 0
    }
    return value
  }

  private Map convertParams(Map input) {
    LinkedHashMap result = [:]
    input.each { key, value ->
      result[key] = convertValue(value)
    }
  }
}