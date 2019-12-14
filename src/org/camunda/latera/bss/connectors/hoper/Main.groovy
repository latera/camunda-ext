package org.camunda.latera.bss.connectors.hoper.hydra

import static org.camunda.latera.bss.utils.MapUtil.nvl
import static org.camunda.latera.bss.utils.MapUtil.keysList

trait Main {
  Map withId(def id = null) {
    if (id) {
      return [id: id]
    }
    return [:]
  }

  Map withParent(Map parent = null) {
    if (parent) {
      return [parent: parent]
    }
    return [:]
  }

  Map prepareParams(Map input) {
    return nvl(input)
  }

  private List preparePathItems(Map type) {
    List result = []
    if (type.id) {
      result = [type.id] + result
    }
    if (type.plural) {
      result = [type.plural] + result
    }
    if (type.parent) {
      result = preparePathItems(type.parent) + result
    }
    return result
  }

  String preparePath(Map type, def id = null) {
    List result = preparePathItems(type + withId(id))
    return result.join('/')
  }

  Map suppressBase64(Map input) {
    Map params = input + [:]
    List inputKeys = keysList(input)

    if ('base64_content' in inputKeys) {
      params = input + [base64_content: '*binary data*']
    }

    if ('content' in inputKeys) {
      params = input + [content: '*binary data*']
    }

    return params
  }
}