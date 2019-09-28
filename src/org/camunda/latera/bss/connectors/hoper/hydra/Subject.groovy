package org.camunda.latera.bss.connectors.hoper.hydra

private trait Subject {
  private static LinkedHashMap SUBJECT_ENTITY_TYPE = [
    one    : 'subject',
    plural : 'subjects'
  ]

  private Map getSubjectEntityType(def id = null) {
    return SUBJECT_ENTITY_TYPE + withId(id)
  }
}