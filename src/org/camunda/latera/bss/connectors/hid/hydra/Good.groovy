package org.camunda.latera.bss.connectors.hid.hydra

trait Good {
  LinkedHashMap getGood(goodId) {
    LinkedHashMap where = [
      n_good_id: goodId
    ]
    return this.hid.getTableData('SR_V_GOODS', where: where)
  }

  def getGoodValueTypeIdByCode(String code) {
    return this.hid.queryFirst("""
      SELECT 'n_good_value_type_id', SR_GOODS_PKG_S.GET_GOOD_VALUE_TYPE_ID('${code}') FROM DUAL
    """)?.n_good_value_type_id
  }
}