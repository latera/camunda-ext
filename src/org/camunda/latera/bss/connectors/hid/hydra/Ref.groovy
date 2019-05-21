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

  LinkedHashMap getRef(def refId) {
    LinkedHashMap where = [
      n_ref_id: refId
    ]
    return hid.getTableFirst(getRefsTable(), where: where)
  }

  List getRefsBy(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      refId      : null,
      refTypeId  : null,
      parRefId   : null,
      baseRefId  : null,
      name       : null,
      code       : null,
      string     : null,
      stirng2    : null,
      string3    : null,
      bool       : null,
      bool2      : null,
      bool3      : null,
      date       : null,
      date2      : null,
      date3      : null,
      number     : null,
      number2    : null,
      number3    : null,
      isEditable : null,
      isManual   : null
    ], input)
    LinkedHashMap where = [:]

    if (params.refId) {
      where.n_ref_id = params.refId
    }
    if (params.refTypeId) {
      where.n_ref_type_id = params.refTypeId
    }
    if (params.parRefId) {
      where.n_par_ref_id = params.parRefId
    }
    if (params.baseRefId) {
      where.n_ref_base_id = params.baseRefId
    }
    if (params.name) {
      where.vc_name = params.name
    }
    if (params.code) {
      where.vc_code = params.code
    }
    if (params.string) {
      where.vc_value = params.string
    }
    if (params.string2) {
      where.vc_value2 = params.string2
    }
    if (params.string3) {
      where.vc_value3 = params.string3
    }
    if (params.bool != null) {
      where.c_value = Oracle.encodeBool(params.bool)
    }
    if (params.bool2 != null) {
      where.c_value2 = Oracle.encodeBool(params.bool2)
    }
    if (params.bool3 != null) {
      where.c_value3 = Oracle.encodeBool(params.bool3)
    }
    if (params.date) {
      where.d_value = date
    }
    if (params.date2) {
      where.d_value2 = date2
    }
    if (params.date3) {
      where.d_value = date3
    }
    if (params.number) {
      where.n_value = number
    }
    if (params.number2) {
      where.n_value = number2
    }
    if (params.number3) {
      where.n_value = number3
    }
    if (params.isEditable != null) {
      where.c_fl_editable = Oracle.encodeBool(params.isEditable)
    }
    if (params.isManual != null) {
      where.c_fl_manual = Oracle.encodeBool(params.isManual)
    }

    def result = hid.getTableData(getRefsTable(), where: where)
    if (result) {
      result.each { ref ->
        if (!REFS_CACHE.containsKey(ref.vc_code)) {
          REFS_CACHE[ref.vc_code] = Numeric.toIntSafe(ref.n_ref_id)
        }
      }
    }
    return result
  }

  LinkedHashMap getRefBy(LinkedHashMap input) {
    return getRefsBy(input)?.getAt(0)
  }

  LinkedHashMap getRefByCode(def code) {
    return getRefBy(code: code)
  }

  LinkedHashMap getRefByName(def code) {
    return getRefBy(name: name)
  }

  def getRefIdByCode(def code) {
    if (REFS_CACHE.containsKey(code)) {
      return REFS_CACHE[code]
    }

    def where = [
      vc_code: code
    ]
    def id = Numeric.toIntSafe(hid.getTableFirst(getRefsTable(), 'n_ref_id', where))
    if (id) {
      REFS_CACHE[code] = id
    }
    return id
  }

  def getRefIdByName(def name) {
    def where = [
      vc_name: name
    ]
    return Numeric.toIntSafe(hid.getTableFirst(getRefsTable(), 'n_ref_id', where))
  }

  String getRefCode(def id) {
    id = Numeric.toIntSafe(id)
    if (REFS_CACHE.containsValue(id)) {
      return REFS_CACHE.find{it.value == id}?.key
    }

    def where = [
      n_ref_id: id
    ]
    def code = hid.getTableFirst(getRefsTable(), 'vc_code', where)
    if (code) {
      REFS_CACHE[code] = id
    }
    return code
  }

  String getRefCodeById(def id) {
    return getRefCode(id)
  }

  String getRefName(def id) {
    return getRef(id)?.vc_name
  }

  String getRefNameById(def id) {
    return getRefName(id)
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