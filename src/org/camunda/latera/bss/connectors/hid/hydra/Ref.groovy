package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import static org.camunda.latera.bss.utils.Oracle.encodeBool
import static org.camunda.latera.bss.utils.Constants.CURR_Ruble
import static org.camunda.latera.bss.utils.Constants.REF_Unknown
import static org.camunda.latera.bss.utils.Constants.UNIT_Unknown
import static org.camunda.latera.bss.utils.Constants.UNIT_Piece
import org.camunda.latera.bss.utils.Constants
import org.camunda.latera.bss.internal.RefCache

trait Ref {
  private static String REFS_TABLE = 'SI_V_REF'

  /**
   * Get refs table name
   */
  String getRefsTable() {
    return REFS_TABLE
  }

  /**
   * Get ref by id
   * @param refId {@link java.math.BigInteger BigInteger}
   * @return Map with ref table row or null
   */
  Map getRef(def refId) {
    LinkedHashMap where = [
      n_ref_id: refId
    ]
    return hid.getTableFirst(getRefsTable(), where: where)
  }

  /**
   * Search for refs by different fields value
   * @param refId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param typeId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param type       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parRefId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parRef     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param baseRefId  {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param baseRef    {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param string     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param string2    {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param string3    {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bool       {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bool2      {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bool3      {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param date       {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param date2      {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param date3      {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param number     {@link Double}, {@link Integer}, {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param number2    {@link Double}, {@link Integer}, {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param number3    {@link Double}, {@link Integer}, {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isEditable {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isManual   {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit      {@link Integer}. Optional, default: 0 (unlimited)
   * @param order      {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional
   * @return List[Map] of ref table rows
   */
  List getRefsBy(Map input) {
    LinkedHashMap params = mergeParams([
      refId      : null,
      typeId     : null,
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
    if (params.refTypeId ?: params.typeId) {
      where.n_ref_type_id = params.refTypeId ?: params.typeId
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

  /**
   * Search for ref by different fields value
   * @param refId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param typeId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param type       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parRefId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parRef     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param baseRefId  {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param baseRef    {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param string     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param string2    {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param string3    {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bool       {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bool2      {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bool3      {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param date       {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param date2      {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param date3      {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param number     {@link Double}, {@link Integer}, {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param number2    {@link Double}, {@link Integer}, {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param number3    {@link Double}, {@link Integer}, {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isEditable {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isManual   {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param order      {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional
   * @return Map with ref table row
   */
  Map getRefBy(Map input) {
    return getRefsBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Get ref by code
   * @param code {@link CharSequence String}
   * @return Map with ref table row
   */
  Map getRefByCode(CharSequence code) {
    return getRefBy(code: code)
  }

  /**
   * Get ref by name
   * @param name {@link CharSequence String}
   * @return Map with ref table row
   */
  Map getRefByName(CharSequence name) {
    return getRefBy(name: name)
  }

  /**
   * Get ref id by code
   * @param code {@link CharSequence String}
   * @return Ref id
   */
  Number getRefIdByCode(CharSequence code) {
    def id = Constants.getConstantByCode(code)
    if (id) {
      return id
    }

    id = RefCache.instance.get(code)
    if (id) {
      return id
    }

    LinkedHashMap where = [
      vc_code: code
    ]
    id = hid.getTableFirst(getRefsTable(), 'n_ref_id', where)
    if (id == null) {
      try {
        id = toIntSafe(hid.queryFirst("SELECT SYS_CONTEXT('CONST', '${code}') FROM DUAL")?.getAt(0))
      } catch (Exception e) {}
    }

    if (id != null) {
      return RefCache.instance.putAndGet(code, id)
    }
    return null
  }

  /**
   * Get ref id by name
   * @param name {@link CharSequence String}
   * @return Ref id
   */
  Number getRefIdByName(CharSequence name) {
    LinkedHashMap where = [
      vc_name: name
    ]
    return toIntSafe(hid.getTableFirst(getRefsTable(), 'n_ref_id', where))
  }

  /**
   * Get ref code by id
   * @param id {@link java.math.BigInteger BigInteger}
   * @return Ref code
   */
  String getRefCode(def id) {
    String code = Constants.getConstantCode(id)
    if (code) {
      return code
    }

    code = RefCache.instance.getKey(id)
    if (code) {
      return code
    }

    LinkedHashMap where = [
      n_ref_id: id
    ]
    code = hid.getTableFirst(getRefsTable(), 'vc_code', where).toString()
    return RefCache.instance.putAndGetKey(code, id)
  }

  /**
   * Get ref code by id
   *
   * Alias for {#link #getRefCode(def)}
   */
  String getRefCodeById(def id) {
    return getRefCode(id)
  }

  /**
   * Get ref name by id
   * @param id {@link java.math.BigInteger BigInteger}
   * @return Ref name
   */
  String getRefName(def id) {
    return getRef(id)?.vc_name
  }

  /**
   * Get ref name by id
   *
   * Alias for {#link #getRefName(def)}
   */
  String getRefNameById(def id) {
    return getRefName(id)
  }

  /**
   * Get default currency ref id
   */
  String getDefaultCurrency() {
    return getRefCode(getDefaultCurrencyId())
  }

  /**
   * Get default currency ref code
   */
  Number getDefaultCurrencyId() {
    return CURR_Ruble
  }

  /**
   * Get unknown ref code
   */
  String getRefUnknown() {
    return getRefCode(getRefUnknownId())
  }

  /**
   * Get unknown ref id
   */
  Number getRefUnknownId() {
    return REF_Unknown
  }

  /**
   * Get unknown unit ref code
   */
  String getUnknownUnit() {
    return getRefCode(getUnknownUnitId())
  }

  /**
   * Get unknown unit ref id
   */
  Number getUnknownUnitId() {
    return UNIT_Unknown
  }

  /**
   * Get piece unit ref code
   */
  String getPieceUnit() {
    return getRefCode(getPieceUnitId())
  }

  /**
   * Get piece unit ref id
   */
  Number getPieceUnitId() {
    return UNIT_Piece
  }

  /**
   * Get opf code by id
   */
  String getOpfCode(def id) {
    String opfCode = getRefCodeById(id)
    if (opfCode == getRefUnknown()) {
      return ''
    }
    return opfCode
  }
}
