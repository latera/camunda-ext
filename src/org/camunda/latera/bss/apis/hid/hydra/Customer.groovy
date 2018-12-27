package org.camunda.latera.bss.apis.hid.hydra

trait Customer {
  LinkedHashMap getCustomer(customerId) {
    LinkedHashMap where = [
      n_subject_id: subjectId
    ]
    return this.hid.getTableFirst('SI_V_USERS', where: where)
  }
}