package org.camunda.latera.bss.connectors.hid.hydra

trait PriceLine {
  private static String PRICE_LINES_TABLE = 'SD_V_PRICE_ORDERS_C'

  List getPriceLines(def docId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return hid.getTableData(PRICE_LINES_TABLE, where: where)
  }

  LinkedHashMap getPriceLine(def priceLineId) {
    LinkedHashMap where = [
      n_price_line_id: priceLineId
    ]
    return hid.getTableFirst(PRICE_LINES_TABLE, where: where)
  }
}