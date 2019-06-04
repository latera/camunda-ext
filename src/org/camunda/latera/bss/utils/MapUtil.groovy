package org.camunda.latera.bss.utils

import org.camunda.latera.bss.utils.StringUtil

class MapUtil {
  static Boolean isMap(def input) {
    return (input instanceof Map)
  }

  static LinkedHashMap parse(def input) {
    LinkedHashMap result = [:]
    if (input == null) {
      return result
    }

    if (isMap(input)) {
      result = input
    } else if (StringUtil.isString(input)) {
      input = StringUtil.trim(input)
      if (input.startsWith('{') && input.endsWith('}')) {
        result = JSON.from(input)
      }
    }
    return result
  }

  static List keysList(LinkedHashMap input) {
    return (input.keySet() as String[])
  }

  static Integer keysCount(LinkedHashMap input) {
    return keysList(input)?.size() ?: 0
  }

  static LinkedHashMap camelizeKeys(LinkedHashMap input, Boolean firstUpper = false) {
    def result = [:]
    input.each { key, value ->
      result[StringUtil.camelize(key, firstUpper)] = value
    }
    return result
  }

  static LinkedHashMap snakeCaseKeys(LinkedHashMap input) {
    def result = [:]
    input.each { key, value ->
      result[StringUtil.snakeCase(key)] = value
    }
    return result
  }

  static LinkedHashMap nvl(LinkedHashMap input) {
    def result = [:]
    input.each { key, value ->
      if (value != null) {
        if (value == 'null' || value == 'NULL') {
          result[key] = null
        } else {
          result[key] = value
        }
      }
    }
    return result
  }

  static LinkedHashMap forceNvl(LinkedHashMap input) {
    def result = [:]
    input.each { key, value ->
      if (value != null && value != 'null' && value != 'NULL') {
        result[key] = value
      }
    }
    return result
  }
}

