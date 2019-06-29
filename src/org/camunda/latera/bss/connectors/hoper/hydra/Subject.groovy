package org.camunda.latera.bss.connectors.hoper.hydra

trait Subject {
  private static LinkedHashMap SUBJECT_ENTITY_TYPE = [
    one    : 'subject',
    plural : 'subjects'
  ]

  Map getSubjectEntityType(def id = null) {
    return SUBJECT_ENTITY_TYPE + withId(id)
  }
}