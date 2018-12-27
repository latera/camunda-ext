package org.camunda.latera.bss.connectors.hid.hydra

trait Customer {
  static String CUSTOMERS_TABLE = 'SI_V_USERS'

  LinkedHashMap getCustomer(def customerId) {
    LinkedHashMap where = [
      n_subject_id: subjectId
    ]
    return hid.getTableFirst(CUSTOMERS_TABLE, where: where)
  }
}