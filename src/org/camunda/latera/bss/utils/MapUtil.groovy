package org.camunda.latera.bss.utils

import static org.camunda.latera.bss.utils.StringUtil.*

class MapUtil {
  static Boolean isMap(def input) {
    return (input instanceof Map)
  }

  static Map parse(def input) {
    LinkedHashMap result = [:]
    if (input == null) {
      return result
    }

    if (isMap(input)) {
      result = input
    } else if (isString(input)) {
      input = trim(input)
      if (input.startsWith('{') && input.endsWith('}')) {
        result = JSON.from(input)
      }
    }
    return result
  }

  static List keysList(Map input) {
    return (input.keySet() as String[])
  }

  static Integer keysCount(Map input) {
    return keysList(input)?.size() ?: 0
  }

  static Map camelizeKeys(Map input, Boolean firstUpper = false) {
    LinkedHashMap result = [:]
    input.each { key, value ->
      result[camelize(key, firstUpper)] = value
    }
    return result
  }

  static Map snakeCaseKeys(Map input) {
    LinkedHashMap result = [:]
    input.each { key, value ->
      result[snakeCase(key)] = value
    }
    return result
  }

  static Map nvl(Map input) {
    LinkedHashMap result = [:]
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

  static Map forceNvl(Map input) {
    LinkedHashMap result = [:]
    input.each { key, value ->
      if (value != null && value != 'null' && value != 'NULL') {
        result[key] = value
      }
    }
    return result
  }
}

