package org.camunda.latera.bss.connectors.hoper.hydra

import static org.camunda.latera.bss.utils.MapUtil.*

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

  List preparePathItems(Map type) {
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
}