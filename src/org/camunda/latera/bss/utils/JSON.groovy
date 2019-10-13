package org.camunda.latera.bss.utils

import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import static org.camunda.latera.bss.utils.DateTimeUtil.isDate
import static org.camunda.latera.bss.utils.DateTimeUtil.iso
import static org.camunda.latera.bss.utils.ListUtil.isList
import static org.camunda.latera.bss.utils.MapUtil.isMap

class JSON {
  /**
    Convert some class values before get JSON representation.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.JSONSpec.html#%23escape"></iframe>
    @param input Any object
  */
  static def escape(def input) {
    if (isMap(input)) {
      LinkedHashMap newMap = [:]
      input.each { def k, def v ->
        newMap[k] = escape(v)
      }
      return newMap
    } else if (isList(input)) {
      List newList = []
      input.each { def item ->
        newList << escape(item)
      }
      return newList
    } else if (isDate(input)) {
      return iso(input)
    } else if (input instanceof CSV) {
      return input.dataMap
    } else {
      return input
    }
  }

  /**
    Get object JSON representation.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.JSONSpec.html#%23to"></iframe>
    @param input Any object
  */
  static String to(def input) {
    return JsonOutput.toJson(escape(input))
  }

  /**
    Prettify JSON input.
    @param json String with JSON representation
  */
  static String pretty(CharSequence json) {
    return JsonOutput.prettyPrint(json)
  }

  /**
    Get object prettified JSON representation.
    @param input Any object
  */
  static String pretty(def input) {
    return pretty(to(input))
  }

  /**
    Parse JSON value to object
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.JSONSpec.html#%23from"></iframe>
    @param json String with JSON representation
  */
  static def from(CharSequence json) {
    return new JsonSlurper().parseText(json)
  }
}