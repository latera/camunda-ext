package org.camunda.latera.bss.connectors.hoper.hydra

trait Object {
  private static LinkedHashMap OBJECT_ENTITY_TYPE = [
    one    : 'object',
    plural : 'objects'
  ]

  def getObjectEntityType(def id = null) {
    return OBJECT_ENTITY_TYPE + withId(id)
  }
}