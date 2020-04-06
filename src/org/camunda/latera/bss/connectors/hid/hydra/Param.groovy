package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Oracle.encodeBool
import org.camunda.latera.bss.internal.ParamCache

/**
 * System params specific methods
 */
trait Param {
  private static String PARAMS_TABLE       = 'SS_V_PARS'
  private static String PARAM_VALUES_TABLE = 'SS_V_PARVALUES'

  /**
   * Get params table name
   */
  String getParamsTable() {
    return PARAMS_TABLE
  }

  /**
   * Get param values table name
   */
  String getParamValuesTable() {
    return PARAM_VALUES_TABLE
  }

  /**
   * Get param by id
   * @param paramId {@link java.math.BigInteger BigInteger}
   * @return Param table row
   */
  Map getParam(def paramId) {
    LinkedHashMap where = [
      n_par_id: paramId
    ]
    return hid.getTableFirst(getParamsTable(), where: where)
  }

  /**
   * Search for params by different fields value
   * @param paramId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param paramGroupId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param dataTypeId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param dataType     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param appTypeId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param appType      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param jobId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refName      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refColId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refColName   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refColCode   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refCaption   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refWhere     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refOrderBy   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isEditable   {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isVisible    {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param rem          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param langId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lang         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit        {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order        {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional
   * @return Param table rows
   */
  List<Map> getParamsBy(Map input) {
    LinkedHashMap params = mergeParams([
      paramId      : null,
      paramGroupId : null,
      dataTypeId   : null,
      appTypeId    : null,
      jobId        : null,
      name         : null,
      code         : null,
      refName      : null,
      refColId     : null,
      refColName   : null,
      refColCode   : null,
      refCaption   : null,
      refWhere     : null,
      refOrderBy   : null,
      isEditable   : null,
      isVisible    : null,
      rem          : null,
      langId       : getLangId(),
      limit        : 0
    ], input)
    LinkedHashMap where = [:]

    if (params.paramId) {
      where.n_par_id = params.paramId
    }
    if (params.paramGroupId) {
      where.n_pargroup_id = params.paramGroupId
    }
    if (params.dataTypeId) {
      where.n_data_type_id = params.dataTypeId
    }
    if (params.appTypeId) {
      where.n_app_type_id = params.appTypeId
    }
    if (params.jobId) {
      where.n_job_id = params.jobId
    }
    if (params.name) {
      where.vc_name = params.name
    }
    if (params.code) {
      where.vc_code = params.code
    }
    if (params.refName) {
      where.vc_ref_name = params.refName
    }
    if (params.refColId) {
      where.vc_ref_col_id = params.refColId
    }
    if (params.refColName) {
      where.vc_ref_col_name = params.refColName
    }
    if (params.refColCode) {
      where.vc_ref_col_code = params.refColCode
    }
    if (params.refColCaption) {
      where.vc_ref_col_caption = params.refColCaption
    }
    if (params.refColWhere) {
      where.vc_ref_col_where = params.refColWhere
    }
    if (params.refColOrderBy) {
      where.vc_ref_col_orderby = params.refColOrderBy
    }
    if (params.isEditable != null) {
      where.c_can_modify = encodeBool(params.isEditable)
    }
    if (params.isVisible != null) {
      where.c_fl_visible = encodeBool(params.isVisible)
    }
    if (params.langId) {
      where.n_lang_id = params.langId
    }
    if (params.rem) {
      where.vc_rem = params.rem
    }

    List result = hid.getTableData(getParamsTable(), where: where, order: params.order, limit: params.limit)
    if (result) {
      result.each { Map param ->
        ParamCache.instance.put(param.vc_code, param.n_ref_id)
      }
    }
    return result
  }

  /**
   * Search for param by different fields value
   * @param paramId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param paramGroupId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param dataTypeId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param dataType     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param appTypeId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param appType      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param jobId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refName      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refColId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refColName   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refColCode   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refCaption   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refWhere     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refOrderBy   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isEditable   {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isVisible    {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param rem          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param langId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lang         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param order        {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional
   * @return Param table row
   */
  Map getParamBy(Map input) {
    return getParamsBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Get param by code
   * @param code {@link CharSequence String}
   * @return Param table row
   */
  Map getParamByCode(CharSequence code) {
    return getParamBy(code: code)
  }

  /**
   * Get param by name
   * @param name {@link CharSequence String}
   * @return Param table row
   */
  Map getParamByName(CharSequence name) {
    return getParamBy(name: name)
  }

  /**
   * Get param id by code
   * @param code {@link CharSequence String}
   * @return Param id
   */
  Number getParamIdByCode(CharSequence code) {
    def id = ParamCache.instance.get(code)
    if (id) {
      return id
    }

    LinkedHashMap where = [
      vc_code: code
    ]
    id = hid.getTableFirst(getParamsTable(), 'n_par_id', where)
    if (id) {
      return ParamCache.instance.putAndGet(code, id)
    }
    return null
  }

  /**
   * Get param id by name
   * @param name {@link CharSequence String}
   * @return Param id
   */
  Number getParamIdByName(CharSequence name) {
    LinkedHashMap where = [
      vc_name: name
    ]
    return toIntSafe(hid.getTableFirst(getParamsTable(), 'n_par_id', where))
  }

  /**
   * Get param code by id
   * @param id {@link java.math.BigInteger BigInteger}
   * @return Param code
   */
  String getParamCode(def id) {
    String code = ParamCache.instance.getKey(id)
    if (code) {
      return code
    }

    LinkedHashMap where = [
      n_par_id: id
    ]
    code = hid.getTableFirst(getParamsTable(), 'vc_code', where).toString()
    return ParamCache.instance.putAndGetKey(code, id)
  }

  /**
   * Get param name by id
   * @param id {@link java.math.BigInteger BigInteger}
   * @return Param name
   */
  String getParamName(def id) {
    return getParam(id)?.vc_name
  }

  /**
   * Get param value by id
   * @param paramValueId {@link java.math.BigInteger BigInteger}
   * @return Param value table row
   */
  Map getParamValue(def paramValueId) {
    LinkedHashMap where = [
      n_par_value_id: paramValueId
    ]
    return hid.getTableFirst(getParamValuesTable(), where: where)
  }

  /**
   * Prepare param value to save
   * @param paramId {@link java.math.BigInteger BigInteger}. Optional if 'param' is passed
   * @param param   {@link CharSequence String}. Optional if 'paramId' is passed
   * @param code    Alias for 'param'
   * @param value   Any type. Optional
   * @return Param value
   * <pre>
   * {@code
   * [
   *   paramId : _, # param id
   *   bool    : _, # if param is boolean type
   *   number  : _, # if param is number type
   *   string  : _, # if param is string type
   *   date    : _, # if param is date type
   *   refId   : _, # if param is refId type and value can be converted to BigInteger (ref id)
   *   ref     : _  # if param is refId type and value cannot be converted to BigInteger (ref code)
   * ]
   * }
   * </pre>
   */
  Map prepareParamValue(Map input) {
    LinkedHashMap param = null
    if (input.containsKey('param') || input.containsKey('code')) {
      param = getParamByCode(input.param ?: input.code)
      input.paramId = param.n_par_id
      input.remove('param')
      input.remove('code')
    } else if (input.containsKey('paramId')) {
      param = getParam(input.paramId)
    }
    if (input.containsKey('value')) {
      def (valueType, val) = getAddParamDataType(param, input.value)
      input."${valueType}" = val
      input.remove('value')
    }
    return input
  }

  /**
   * Search for param values by different fields value
   * @param paramValueId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param paramId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param param        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code         Alias for 'param'
   * @param subjectId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param appId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param jobId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param date         {@link java.time.Temporal Any date type}. Optional
   * @param number       {@link Double}, {@link Integer}, {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param string       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bool         {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ref          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param value        Any type which is automatically converted to 'date', 'string', 'name', 'bool' or 'refId', see {@link #prepareParamValue(Map)}. Optional
   * @param limit        {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order        {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: [:]
   * @return Param value table rows
   */
  List<Map> getParamValuesBy(Map input) {
    LinkedHashMap params = mergeParams([
      paramValueId : null,
      paramId      : null,
      subjectId    : null,
      appId        : null,
      jobId        : null,
      date         : null,
      string       : null,
      number       : null,
      bool         : null,
      refId        : null,
      limit        : 0
    ], prepareParamValue(input))
    LinkedHashMap where = [:]

    if (params.paramValueId) {
      where.n_par_value_id = params.paramValueId
    }
    if (params.paramId) {
      where.n_par_id = params.paramId
    }
    if (params.subjectId) {
      where.n_subject_id = params.subjectId
    }
    if (params.appId) {
      where.n_application_id = params.appId
    }
    if (params.jobId) {
      where.n_job_id = params.jobId
    }
    if (params.date) {
      where.d_value = params.date
    }
    if (params.string) {
      where.vc_value = params.string
    }
    if (params.number) {
      where.n_value = params.number
    }
    if (params.bool != null) {
      where.c_flag_value = encodeBool(params.bool)
    }
    if (params.refId) {
      where.n_value_id = params.refId
    }

    return hid.getTableData(getParamValuesTable(), where: where, order: params.order, limit: params.limit)
  }

  /**
   * Search for param value by different fields value
   * @param paramValueId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param paramId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param param        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code         Alias for 'param'
   * @param subjectId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param appId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param jobId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param date         {@link java.time.Temporal Any date type}. Optional
   * @param number       {@link Double}, {@link Integer}, {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param string       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bool         {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ref          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param value        Any type which is automatically converted to 'date', 'string', 'name', 'bool' or 'refId', see {@link #prepareParamValue(Map)}. Optional
   * @param limit        {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order        {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: [:]
   * @return Param value table row
   */
  Map getParamValueBy(Map input) {
    List result = getParamValuesBy(input + [limit: 1])
    if (result) {
      return result.getAt(0)
    } else {
      return null
    }
  }
}
