package org.camunda.latera.bss.connectors.hid.hydra

trait PriceOrder {
  private static String PRICE_ORDERS_TABLE = 'SD_V_PRICE_ORDERS_T'

  LinkedHashMap getPriceOrder(def docId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return hid.getTableData(PRICE_ORDERS_TABLE, where: where)
  }
}