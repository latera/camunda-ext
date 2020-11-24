package org.camunda.latera.bss.utils

import static org.camunda.latera.bss.utils.StringUtil.isString
import static org.camunda.latera.bss.utils.StringUtil.trim
import static org.camunda.latera.bss.utils.StringUtil.forceIsEmpty
import static org.camunda.latera.bss.utils.StringUtil.forceNotEmpty
import static org.camunda.latera.bss.utils.StringUtil.camelize
import static org.camunda.latera.bss.utils.StringUtil.snakeCase
import static org.camunda.latera.bss.utils.ListUtil.isList

class MapUtil {
  /**
    Check if input is Map
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="${docBaseUrl}/test-reports/org.camunda.latera.bss.utils.MapUtilSpec.html#%23isMap"></iframe>
    @param input Any object for type check
  */
  static Boolean isMap(def input) {
    return (input instanceof Map)
  }

  /**
    Parse input as map value.
    <p>
    '{...}' became list, [...:...] returned unchanged, non-map values became an empty map [:]
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="${docBaseUrl}/test-reports/org.camunda.latera.bss.utils.MapUtilSpec.html#%23parse"></iframe>
    @param input Map, null or String with JSON map
  */
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

  /**
    Return list of map keys.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="${docBaseUrl}/test-reports/org.camunda.latera.bss.utils.MapUtilSpec.html#%23keysList"></iframe>
    @param input Map
  */
  static List keysList(Map input) {
    return input == null ? [] : (input.keySet() as String[])
  }

  /**
    Get keys count from map.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="${docBaseUrl}/test-reports/org.camunda.latera.bss.utils.MapUtilSpec.html#%23keysCount"></iframe>
    @param input Map
  */
  static Integer keysCount(Map input) {
    return keysList(input).size() ?: 0
  }

  /**
    Merge 2 maps into one.
    <p>
    The second map values replaces or adds values to the first one using the appropriate keys.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="${docBaseUrl}/test-reports/org.camunda.latera.bss.utils.MapUtilSpec.html#%23merge"></iframe>
    @param first Primary map
    @param second Secondary map
  */
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

  /**
    Merge 2 maps into one.
    <p>
    If the second map values is not null, it replaces or adds values to the first one using the appropriate keys.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="${docBaseUrl}/test-reports/org.camunda.latera.bss.utils.MapUtilSpec.html#%23mergeNotNull"></iframe>
    @param first Primary map
    @param second Secondary map
    @see #merge(Map,Map)
  */
  static Map mergeNotNull(Map first, Map second) {
    LinkedHashMap result = [:]

    // Convert keys from GString to String
    first  = merge([:], first)
    second = merge([:], second)
    // Get all keys from both maps
    Map both = merge(first, second)
    both.each { def key, def value ->
      if (first[key] != null && second[key] == null) {
        result[key] = first[key]
      } else {
        result[key] = second[key]
      }
    }
    return result
  }

  /**
    Convert map keys to camelCase.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="${docBaseUrl}/test-reports/org.camunda.latera.bss.utils.MapUtilSpec.html#%23camelizeKeys+with+Map+input+-+without+firstUpper"></iframe>
    @param input Map
    @param firstUpper If true return first letter capital otherwise not
  */
  static Map camelizeKeys(Map input, Boolean firstUpper = false) {
    LinkedHashMap result = [:]
    input.each { def key, def value ->
      result[camelize(key, firstUpper)] = value
    }
    return result
  }

  /**
    Convert map keys to camelCase.
    <p>
    Same as #camelizeKeys(Map,Boolean), but for List[Map] input
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="${docBaseUrl}/test-reports/org.camunda.latera.bss.utils.MapUtilSpec.html#%23camelizeKeys+with+List+input+-+without+firstUpper"></iframe>
    @param input List[Map]
    @param firstUpper If true return first letter capital otherwise not
    @see #camelizeKeys(Map,Boolean)
  */
  static List camelizeKeys(List input, Boolean firstUpper = false) {
    List result = []
    input.each { def it ->
      if (isMap(it)) {
        result << camelizeKeys(it, firstUpper)
      } else {
        result << it
      }
    }
    return result
  }

  /**
    Convert map keys to camelCase.
    <p>
    Same as #camelizeKeys(Map,Boolean), but recursive.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="${docBaseUrl}/test-reports/org.camunda.latera.bss.utils.MapUtilSpec.html#%23deepCamelizeKeys+with+Map+input+-+without+firstUpper"></iframe>
    @param input Map
    @param firstUpper If true return first letter capital otherwise not
    @see #deepCamelizeKeys(Map,Boolean)
  */
  static Map deepCamelizeKeys(Map input, Boolean firstUpper = false) {
    LinkedHashMap result = [:]
    input.each { def key, def value ->
      if (isMap(value) || isList(value)) {
        result[camelize(key, firstUpper)] = deepCamelizeKeys(value, firstUpper)
      } else {
        result[camelize(key, firstUpper)] = value
      }
    }
    return result
  }

  /**
    Convert map keys to camelCase.
    <p>
    Same as #deepCamelizeKeys(Map,Boolean), but for List[Map] input.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="${docBaseUrl}/test-reports/org.camunda.latera.bss.utils.MapUtilSpec.html#%23deepCamelizeKeys+with+List+input+-+without+firstUpper"></iframe>
    @param input Map
    @param firstUpper If true return first letter capital otherwise not
    @see #camelizeKeys(Map,Boolean)
  */
  static List deepCamelizeKeys(List input, Boolean firstUpper = false) {
    List result = []
    input.each { def it ->
      if (isMap(it) || isList(it)) {
        result << deepCamelizeKeys(it, firstUpper)
      } else {
        result << it
      }
    }
    return result
  }

  /**
    Convert map keys to snake_case.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="${docBaseUrl}/test-reports/org.camunda.latera.bss.utils.MapUtilSpec.html#%23snakeCaseKeys+with+Map+input"></iframe>
    @param input Map
    @param firstUpper If true return first letter capital otherwise not
  */
  static Map snakeCaseKeys(Map input) {
    LinkedHashMap result = [:]
    input.each { def key, def value ->
      result[snakeCase(key)] = value
    }
    return result
  }

  /**
    Convert map keys to snake_case.
    <p>
    Same as #snakeCaseKeys(Map), but for List[Map] input
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="${docBaseUrl}/test-reports/org.camunda.latera.bss.utils.MapUtilSpec.html#%23snakeCaseKeys+with+List+input"></iframe>
    @param input List[Map]
    @see #snakeCaseKeys(Map)
  */
  static List snakeCaseKeys(List input) {
    List result = []
    input.each { def it ->
      if (isMap(it)) {
        result << snakeCaseKeys(it)
      } else {
        result << it
      }
    }
    return result
  }

  /**
    Convert map keys to snake_case.
    <p>
    Same as #snakeCaseKeys(Map), but recursive
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="${docBaseUrl}/test-reports/org.camunda.latera.bss.utils.MapUtilSpec.html#%23deepSnakeCaseKeys+with+Map+input"></iframe>
    @param input Map
    @see #snakeCaseKeys(Map)
  */
  static Map deepSnakeCaseKeys(Map input) {
    LinkedHashMap result = [:]
    input.each { def key, def value ->
      if (isMap(value) || isList(value)) {
        result[snakeCase(key)] = deepSnakeCaseKeys(value)
      } else {
        result[snakeCase(key)] = value
      }
    }
    return result
  }

  /**
    Convert map keys to snake_case.
    <p>
    Same as #deepSnakeCaseKeys(Map), but for List[Map] input
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="${docBaseUrl}/test-reports/org.camunda.latera.bss.utils.MapUtilSpec.html#%23deepSnakeCaseKeys+with+List+input"></iframe>
    @param input Map
    @see #deepSnakeCaseKeys(Map)
  */
  static List deepSnakeCaseKeys(List input) {
    List result = []
    input.each { def it ->
      if (isMap(it) || isList(it)) {
        result << deepSnakeCaseKeys(it)
      } else {
        result << it
      }
    }
    return result
  }

  /**
    Remove null values from map, coerse '', 'null' and 'NULL' values as null, return other values and all keys unchanged.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="${docBaseUrl}/test-reports/org.camunda.latera.bss.utils.MapUtilSpec.html#%23nvl"></iframe>
    @param input Map
  */
  static Map nvl(Map input) {
    LinkedHashMap result = [:]
    input.each { def key, def value ->
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

  /**
    Remove null, '', 'null' and 'NULL' values from map, return other values and all keys unchanged.
    <p>
    Like #nvl(Map), but remove more items.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="${docBaseUrl}/test-reports/org.camunda.latera.bss.utils.MapUtilSpec.html#%23forceNvl"></iframe>
    @param input Map
  */
  static Map forceNvl(Map input) {
    LinkedHashMap result = [:]
    input.each { def key, def value ->
      if (forceNotEmpty(value)) {
        result[key] = value
      }
    }
    return result
  }
}

