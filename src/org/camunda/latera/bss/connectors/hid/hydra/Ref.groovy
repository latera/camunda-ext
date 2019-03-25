package org.camunda.latera.bss.connectors.hid.hydra
import org.camunda.latera.bss.utils.Numeric

trait Ref {
  private static LinkedHashMap REFS_CACHE = [null: null]
  private static String REFS_TABLE = 'SI_V_REF'

  def getRefsTable() {
    return REFS_TABLE
  }

  def getRefIdByCode(def code) {
    if (REFS_CACHE.containsKey(code)) {
      return REFS_CACHE[code]
    } else {
      def refId = Numeric.toIntSafe(hid.queryFirst("""
      SELECT SI_REF_PKG_S.GET_ID_BY_CODE('${code.toString()}') FROM DUAL
      """)?.getAt(0))

      if (refId) {
        REFS_CACHE[code] = refId
      }
      return refId
    }
  }

  String getRefCodeById(def id) {
    id = Numeric.toIntSafe(id)
    if (REFS_CACHE.containsValue(id)) {
      return REFS_CACHE.find{it.value == id}?.key
    } else {
      String refCode = hid.queryFirst("""
      SELECT SI_REF_PKG_S.GET_CODE_BY_ID(${id}) FROM DUAL
      """)?.getAt(0)

      if (refCode) {
        REFS_CACHE[refCode] = id
      }
      return refCode
    }
  }

  String getRefNameById(def id) {
    String refName = hid.queryFirst("""
      SELECT SI_REF_PKG_S.GET_NAME_BY_ID(${id}) FROM DUAL
    """)?.getAt(0)
    return refName
  }

  def getDefaultCurrencyId() {
    return getRefIdByCode('CURR_Ruble')
  }

  def getUnknownUnitId() {
    return getRefIdByCode('UNIT_Unknown')
  }

  def getPieceUnitId() {
    return getRefIdByCode('UNIT_Piece')
  }
}