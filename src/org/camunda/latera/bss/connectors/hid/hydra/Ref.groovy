package org.camunda.latera.bss.connectors.hid.hydra

trait Ref {
  static LinkedHashMap refsCache = [null: null]

  def getRefIdByCode(String code) {
    if (this.refsCache.containsKey(code)) {
      return this.refsCache[code]
    } else {
      def refId = this.hid.queryFirst("""
        SELECT 'n_ref_id', SI_REF_PKG_S.GET_ID_BY_CODE('${code}') 
        FROM   DUAL
      """)?.n_ref_id

      if (refId ) {
        this.refsCache[code] = refId
      }
      return refId
    }
  }

  String getRefCodeById(def id) {
    if (this.refsCache.containsValue(code)) {
      return this.refsCache.find{it.value == code}?.key
    } else {
      String refCode = this.hid.queryFirst("""
        SELECT 'vc_code', SI_REF_PKG_S.GET_CODE_BY_ID('${code}') 
        FROM   DUAL
      """)?.vc_code

      if (refCode) {
        this.refsCache[refCode] = id
      }
      return refCode
    }
  }
}