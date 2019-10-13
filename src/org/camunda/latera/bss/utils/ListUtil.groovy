package org.camunda.latera.bss.utils

import static org.camunda.latera.bss.utils.StringUtil.isString
import static org.camunda.latera.bss.utils.StringUtil.trim
import static org.camunda.latera.bss.utils.StringUtil.forceIsEmpty
import static org.camunda.latera.bss.utils.StringUtil.forceNotEmpty

class ListUtil {
  /**
    Check if input is List or Array
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.ListUtilSpec.html#%23isList"></iframe>
    @param input Any object for type check
  */
  static Boolean isList(def input) {
    return (input instanceof List || input instanceof Object[] || input.getClass().isArray())
  }

  /**
    Check if input is byte[]
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.ListUtilSpec.html#%23isByteArray"></iframe>
    @param input Any object for type check
  */
  static Boolean isByteArray(def input) {
    return (input instanceof byte[])
  }

  /**
    Change string items case of list or array to upper
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.ListUtilSpec.html#%23upperCase"></iframe>
    @param input List[String]
  */
  static List upperCase(List input) {
    List result = []
    input.each { item ->
      if (isString(item)) {
        result << item.toUpperCase()
      } else {
        result << item
      }
    }
    return result
  }

  /**
    Change string items case of list or array to lower
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.ListUtilSpec.html#%23lowerCase"></iframe>
    @param input List[String]
  */
  static List lowerCase(List input) {
    List result = []
    input.each { item ->
      if (isString(item)) {
        result << item.toLowerCase()
      } else {
        result << item
      }
    }
    return result
  }

  /**
    Parse input as list value.
    <p>
    '[...]' became list, [...] returned unchanged, non-list values became a list with just one item.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.ListUtilSpec.html#%23parse"></iframe>
    @param input List, null, String with JSON array or Any value
  */
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

  /**
    Remove null items, coerse '', 'null' and 'NULL' items of list as null, return others unchanged.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.ListUtilSpec.html#%23nvl"></iframe>
    @param input List[String]
  */
  static List nvl(List input) {
    List result = []
    input.each { def item ->
      if (item != null) {
        if (forceIsEmpty(item)) {
          result += null
        } else {
          result += item
        }
      }
    }
    return result
  }

  /**
    Return list without null, '', 'null' and 'NULL' items.
    <p>
    Like #nvl(List), but remove more items.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.ListUtilSpec.html#%23forceNvl"></iframe>
    @param input List[String]
  */
  static List forceNvl(List input) {
    List result = []
    input.each { def item ->
      if (forceNotEmpty(item)) {
        result += item
      }
    }
    return result
  }
}

