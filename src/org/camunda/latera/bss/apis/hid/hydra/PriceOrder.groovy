package org.camunda.latera.bss.apis.hid.hydra

trait PriceOrder {
  LinkedHashMap getPriceOrder(docId, stateId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    if (state != null) {
      where.n_doc_state_id = stateId
    }
    return this.hid.getTableData('SD_V_PRICE_ORDERS_T', where: where)
  }
}