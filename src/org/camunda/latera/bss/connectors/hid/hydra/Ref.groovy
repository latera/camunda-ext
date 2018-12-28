package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.logging.Logging
trait Ref {
  private static LinkedHashMap REFS_CACHE = [null: null]
  private static String REFS_TABLE = 'SI_V_REF'

  def getRefIdByCode(String code) {
    if (REFS_CACHE.containsKey(code)) {
      return REFS_CACHE[code]
    } else {
      def logger = new Logging()

      def refId = hid.queryFirst("""
      SELECT SI_REF_PKG_S.GET_ID_BY_CODE('${code}') FROM DUAL
      """, false)
      
      logger.log(refId)
      
      refId = refId.getAt(0)

      if (refId) {
        REFS_CACHE[code] = refId
      }
      return refId
    }
  }

  String getRefCodeById(def id) {
    if (REFS_CACHE.containsValue(code)) {
      return REFS_CACHE.find{it.value == code}?.key
    } else {
      String refCode = hid.queryFirst("""
      SELECT SI_REF_PKG_S.GET_CODE_BY_ID(${id}) FROM DUAL
      """, false)?.getAt(0)

      if (refCode) {
        REFS_CACHE[refCode] = id
      }
      return refCode
    }
  }
}