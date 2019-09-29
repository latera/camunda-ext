package org.camunda.latera.bss.utils

import static org.camunda.latera.bss.utils.StringUtil.isString
import static org.camunda.latera.bss.utils.StringUtil.trim
import static org.camunda.latera.bss.utils.StringUtil.forceIsEmpty
import static org.camunda.latera.bss.utils.StringUtil.forceNotEmpty
import static org.camunda.latera.bss.utils.StringUtil.camelize
import static org.camunda.latera.bss.utils.StringUtil.snakeCase
import static org.camunda.latera.bss.utils.ListUtil.isList

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

  static Map merge(Map first, Map second) {
    LinkedHashMap result = [:]
    [first, second].each { Map item ->
      item.each { def key, def value ->
        if (isString(key)) {
          result[key.toString()] = value
        } else {
          result[key] = value
        }
      }
    }
    return result
  }

  static Map mergeNotNull(Map first, Map second) {
    LinkedHashMap result = [:]
    first.each { key, value ->
      if (value == null && second[key] != null) {
        result[key] = second[key]
      } else {
        result[key] = value
      }
    }
    return result
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

  static List camelizeKeys(List input, Boolean firstUpper = false) {
    List result = []
    input.each { it ->
      if (isMap(it)) {
        result << camelizeKeys(it, firstUpper)
      } else {
        result << it
      }
    }
    return result
  }

  static Map deepCamelizeKeys(Map input, Boolean firstUpper = false) {
    LinkedHashMap result = [:]
    input.each { key, value ->
      if (isMap(value) || isList(value)) {
        result[camelize(key, firstUpper)] = deepCamelizeKeys(value)
      } else {
        result[camelize(key, firstUpper)] = value
      }
    }
    return result
  }

  static List deepCamelizeKeys(List input, Boolean firstUpper = false) {
    List result = []
    input.each { it ->
      if (isMap(it) || isList(it)) {
        result << deepCamelizeKeys(it, firstUpper)
      } else {
        result << it
      }
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

  static List snakeCaseKeys(List input) {
    List result = []
    input.each { it ->
      if (isMap(it)) {
        result << snakeCaseKeys(it)
      } else {
        result << it
      }
    }
    return result
  }

  static Map deepSnakeCaseKeys(Map input) {
    LinkedHashMap result = [:]
    input.each { key, value ->
      if (isMap(value) || isList(value)) {
        result[snakeCase(key)] = deepSnakeCaseKeys(value)
      } else {
        result[snakeCase(key)] = value
      }
    }
    return result
  }

  static List deepSnakeCaseKeys(List input) {
    List result = []
    input.each { it ->
      if (isMap(it) || isList(it)) {
        result << deepSnakeCaseKeys(it)
      } else {
        result << it
      }
    }
    return result
  }

  static Map nvl(Map input) {
    LinkedHashMap result = [:]
    input.each { key, value ->
      if (value != null) {
        if (forceIsEmpty(value)) {
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
      if (forceNotEmpty(value)) {
        result[key] = value
      }
    }
    return result
  }
}

