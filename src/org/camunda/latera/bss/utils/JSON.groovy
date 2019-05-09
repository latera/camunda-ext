package org.camunda.latera.bss.utils
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import org.camunda.latera.bss.utils.DateTimeUtil

class JSON {
  static def escape(obj) {
    if (obj instanceof Map) {
      def newMap = [:]
      obj.each { k, v ->
        newMap[k] = escape(v)
      }
      return newMap
    } else if (obj instanceof List) {
      def newList = []
      obj.each { item ->
        newList += escape(item)
      }
      return newList
    } else if (DateTimeUtil.isDate(obj)) {
      try {
        return DateTimeUtil.format(obj, DateTimeUtil.ISO_FORMAT)
      } catch(Exception e) {
        return DateTimeUtil.format(obj, DateTimeUtil.ISO_FORMAT_NO_TZ)
      }
    } else {
      return obj
    }
  }

  static String to(Object obj) {
    return JsonOutput.toJson(escape(obj))
  }

  static String pretty(String json) {
    return JsonOutput.prettyPrint(json)
  }

  static String pretty(Object obj) {
    return pretty(to(obj))
  }


  static Object from(String json) {
    return new JsonSlurper().parseText(json)
  }
}