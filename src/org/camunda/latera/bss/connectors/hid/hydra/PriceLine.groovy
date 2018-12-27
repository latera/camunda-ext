package org.camunda.latera.bss.connectors.hid.hydra

trait PriceLine {
  List getPriceLines(docId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return this.hid.getTableData('SD_V_PRICE_ORDERS_C', where: where)
  }

  LinkedHashMap getPriceLine(priceLineId) {
    LinkedHashMap where = [
      n_price_line_id: priceLineId
    ]
    return this.hid.getTableFirst('SD_V_PRICE_ORDERS_C', where: where)
  }
}