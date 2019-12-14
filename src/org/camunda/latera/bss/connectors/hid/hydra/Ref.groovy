package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import static org.camunda.latera.bss.utils.Oracle.encodeBool
import org.camunda.latera.bss.internal.RefCache

trait Ref {
  private static String REFS_TABLE       = 'SI_V_REF'
  private static String DEFAULT_CURRENCY = 'CURR_Ruble'
  private static String UNKNOWN_UNIT     = 'UNIT_Unknown'
  private static String PIECE_UNIT       = 'UNIT_Piece'

  String getRefsTable() {
    return REFS_TABLE
  }

  Map getRef(def refId) {
    LinkedHashMap where = [
      n_ref_id: refId
    ]
    return hid.getTableFirst(getRefsTable(), where: where)
  }

  List getRefsBy(Map input) {
    LinkedHashMap params = mergeParams([
      refId      : null,
      refTypeId  : null,
      parRefId   : null,
      baseRefId  : null,
      name       : null,
      code       : null,
      string     : null,
      string2    : null,
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
      isManual   : null,
      limit      : 0
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
      where.c_value = encodeBool(params.bool)
    }
    if (params.bool2 != null) {
      where.c_value2 = encodeBool(params.bool2)
    }
    if (params.bool3 != null) {
      where.c_value3 = encodeBool(params.bool3)
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
      where.c_fl_editable = encodeBool(params.isEditable)
    }
    if (params.isManual != null) {
      where.c_fl_manual = encodeBool(params.isManual)
    }

    List result = hid.getTableData(getRefsTable(), where: where, order: params.order, limit: params.limit)
    if (result) {
      result.each { Map ref ->
        RefCache.instance.put(ref.vc_code, ref.n_ref_id)
      }
    }
    return result
  }

  Map getRefBy(Map input) {
    return getRefsBy(input + [limit: 1])?.getAt(0)
  }

  Map getRefByCode(CharSequence code) {
    return getRefBy(code: code)
  }

  Map getRefByName(CharSequence name) {
    return getRefBy(name: name)
  }

  Number getRefIdByCode(CharSequence code) {
    def id = RefCache.instance.get(code)
    if (id) {
      return id
    }

    LinkedHashMap where = [
      vc_code: code
    ]
    id = hid.getTableFirst(getRefsTable(), 'n_ref_id', where)
    if (id == null) {
      id = toIntSafe(hid.queryFirst("SELECT SYS_CONTEXT('CONST', '${code}') FROM DUAL")?.getAt(0))
      if (id != null) {
        code = getRefCodeById(id)
      }
      return id
    } else {
      return RefCache.instance.putAndGet(code, id)
    }
  }

  Number getRefIdByName(CharSequence name) {
    LinkedHashMap where = [
      vc_name: name
    ]
    return toIntSafe(hid.getTableFirst(getRefsTable(), 'n_ref_id', where))
  }

  String getRefCode(def id) {
    String code = RefCache.instance.getKey(id)
    if (code) {
      return code
    }

    LinkedHashMap where = [
      n_ref_id: id
    ]
    code = hid.getTableFirst(getRefsTable(), 'vc_code', where).toString()
    return RefCache.instance.putAndGetKey(code, id)
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

  String getDefaultCurrency() {
    return DEFAULT_CURRENCY
  }

  Number getDefaultCurrencyId() {
    return getRefIdByCode(getDefaultCurrency())
  }

  String getUnknownUnit() {
    return UNKNOWN_UNIT
  }

  Number getUnknownUnitId() {
    return getRefIdByCode(getUnknownUnit())
  }

  String getPieceUnit() {
    return PIECE_UNIT
  }

  Number getPieceUnitId() {
    return getRefIdByCode(getPieceUnit())
  }

  String getOpfCode(def id) {
    String opfCode = getRefCodeById(id)
    if (opfCode == 'REF_Unknown') {
      opfCode = ''
    }
    return opfCode
  }
}
