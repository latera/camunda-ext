package org.camunda.latera.bss.utils
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

class JSON {
  static String to(Object obj) {
    return JsonOutput.toJson(obj)
  }

  static Object from(String json) {
    return new JsonSlurper().parseText(json)
  }
}