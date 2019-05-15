package org.camunda.latera.bss.connectors.hid.hydra
import org.camunda.latera.bss.utils.Numeric

trait Ref {
  private static LinkedHashMap REFS_CACHE = [null: null]
  private static String REFS_TABLE        = 'SI_V_REF'
  private static String DEFAULT_CURRENCY  = 'CURR_Ruble'
  private static String UNKNOWN_UNIT      = 'UNIT_Unknown'
  private static String PIECE_UNIT        = 'UNIT_Piece'

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

  def getDefaultCurrency() {
    return DEFAULT_CURRENCY
  }

  def getDefaultCurrencyId() {
    return getRefIdByCode(getDefaultCurrency())
  }

  def getUnknownUnit() {
    return UNKNOWN_UNIT
  }

  def getUnknownUnitId() {
    return getRefIdByCode(getUnknownUnit())
  }

  def getPieceUnit() {
    return PIECE_UNIT
  }

  def getPieceUnitId() {
    return getRefIdByCode(getPieceUnit())
  }
}