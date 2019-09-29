package org.camunda.latera.bss.connectors.hoper.hydra

private trait Object {
  private static LinkedHashMap OBJECT_ENTITY_TYPE = [
    one    : 'object',
    plural : 'objects'
  ]

  static Map getObjectEntityType(def id = null) {
    return OBJECT_ENTITY_TYPE + withId(id)
  }
}