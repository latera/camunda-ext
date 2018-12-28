package org.camunda.latera.bss.connectors.hid.hydra

trait Good {
  private static String GOODS_TABLE = 'SR_V_GOODS'

  LinkedHashMap getGood(def goodId) {
    LinkedHashMap where = [
      n_good_id: goodId
    ]
    return hid.getTableData(GOODS_TABLE, where: where)
  }

  def getGoodValueTypeIdByCode(String code) {
    return hid.queryFirst("""
      SELECT SR_GOODS_PKG_S.GET_GOOD_VALUE_TYPE_ID('${code}') FROM DUAL
    """, false)?.getAt(0)
  }
}