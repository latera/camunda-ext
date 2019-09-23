package org.camunda.latera.bss.utils

import static org.camunda.latera.bss.utils.StringUtil.isString
import static org.camunda.latera.bss.utils.StringUtil.trim
import static org.camunda.latera.bss.utils.StringUtil.forceIsEmpty
import static org.camunda.latera.bss.utils.StringUtil.forceNotEmpty
import org.camunda.latera.bss.utils.JSON

class ListUtil {
  static Boolean isList(def input) {
    return (input instanceof List || input instanceof Object[])
  }

  static Boolean isByteArray(def input) {
    return (input instanceof byte[])
  }

  static List parse(def input) {
    List result = []
    if (input == null) {
      return result
    }

    if (isList(input)) {
      result = input
    } else if (isString(input)) {
      input = trim(input)
      if (input.startsWith('[') && input.endsWith(']')) {
        result = JSON.from(input)
      } else {
        result = [input]
      }
    } else {
      result = [input]
    }
    return result
  }

  static List nvl(List input) {
    List result = []
    input.each { item ->
      if (item != null) {
        if (forceIsEmpty(item)) {
          result += null
        } else {
          result += value
        }
      }
    }
    return result
  }

  static List forceNvl(List input) {
    List result = []
    input.each { item ->
      if (forceNotEmpty(item)) {
        result += value
      }
    }
    return result
  }
}

