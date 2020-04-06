package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Oracle.encodeBool
import static org.camunda.latera.bss.utils.Oracle.decodeBool
import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import static org.camunda.latera.bss.utils.Constants.ENTITY_TYPE_CatalogItem
import static org.camunda.latera.bss.utils.Constants.Good_Packs
import static org.camunda.latera.bss.utils.Constants.GOOD_Serv
import static org.camunda.latera.bss.utils.Constants.Good_Adjustments
import static org.camunda.latera.bss.utils.Constants.Good_Realty
import static org.camunda.latera.bss.utils.Constants.GOOD_Value
import static org.camunda.latera.bss.utils.Constants.Good_NetServ

/**
 * Services (goods) specific methods
 */
trait Good {
  private static String GOODS_TABLE                = 'SR_V_GOODS'
  private static String GOOD_ADD_PARAMS_TABLE      = 'SR_V_GOOD_VALUES'
  private static String GOOD_ADD_PARAM_TYPES_TABLE = 'SR_V_GOOD_VALUES_TYPE'
  private static String SERV_SCHEMES_TABLE         = 'SR_V_SERV_SCHEMES'

  /**
   * Get goods table name
   */
  String getGoodsTable() {
    return GOODS_TABLE
  }

  /**
   * Get goods additional parameter values table name
   */
  String getGoodAddParamsTable() {
    return GOOD_ADD_PARAMS_TABLE
  }

  /**
   * Get goods additional parameter types table name
   */
  String getGoodAddParamTypesTable() {
    return GOOD_ADD_PARAM_TYPES_TABLE
  }

  /**
   * Get good entity type ref code
   */
  String getGoodEntityType() {
    return getRefCode(getGoodEntityTypeId())
  }

  /**
   * Get good entity type ref id
   */
  Number getGoodEntityTypeId() {
    return ENTITY_TYPE_CatalogItem
  }

  /**
   * Get price plan good type ref code
   */
  String getPricePlanGoodKind() {
    return getRefCode(getPricePlanGoodKindId())
  }

  /**
   * Get price plan good type ref id
   */
  Number getPricePlanGoodKindId() {
    return Good_Packs
  }

  /**
   * Get service good type ref code
   */
  String getServiceGoodKind() {
    return getRefCode(getServiceGoodKindId())
  }

  /**
   * Get service good type ref id
   */
  Number getServiceGoodKindId() {
    return GOOD_Serv
  }

  /**
   * Get adjustment good type ref code
   */
  String getAdjustmentGoodKind() {
    return getRefCode(getAdjustmentGoodKindId())
  }

  /**
   * Get adjustment good type ref id
   */
  Number getAdjustmentGoodKindId() {
    return Good_Adjustments
  }

  /**
   * Get realty good type ref code
   */
  String getRealtyGoodKind() {
    return getRefCode(getRealtyGoodKindId())
  }

  /**
   * Get realty good type ref id
   */
  Number getRealtyGoodKindId() {
    return Good_Realty
  }

  /**
   * Get object good type ref code
   */
  String getObjectGoodKind() {
    return getRefCode(getObjectGoodKindId())
  }

  /**
   * Get object good type ref id
   */
  Number getObjectGoodKindId() {
    return GOOD_Value
  }

  /**
   * Get net service good type ref code
   */
  String getNetServiceGoodKind() {
    return getRefCode(getNetServiceGoodKindId())
  }

  /**
   * Get net service good type ref id
   */
  Number getNetServiceGoodKindId() {
    return Good_NetServ
  }

  /**
   * Get good by id
   * @param goodId {@link java.math.BigInteger BigInteger}
   * @return Good table row
   */
  Map getGood(def goodId) {
    LinkedHashMap where = [
      n_good_id: goodId
    ]
    return hid.getTableFirst(getGoodsTable(), where: where)
  }

  /**
   * Search for goods by different fields value
   * @param goodId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param kindId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param kind       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param typeId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param type       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param groupId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param baseGoodId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param unitId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param unit       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isProvider {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isCustomer {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param tags       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit      {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order      {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional
   * @return Good table rows
   */
  List<Map> getGoodsBy(Map input) {
    LinkedHashMap params = mergeParams([
      goodId     : null,
      kindId     : null,
      typeId     : null,
      groupId    : null,
      baseGoodId : null,
      unitId     : null,
      code       : null,
      name       : null,
      isProvider : null,
      isCustomer : null,
      tags       : null,
      limit      : 0
    ], input)
    LinkedHashMap where = [:]

    if (params.goodId) {
      where.n_good_id = params.goodId
    }
    if (params.kindId) {
      where.n_good_kind_id = params.kindId
    }
    if (params.typeId) {
      where.n_good_type_id = params.typeId
    }
    if (params.groupId) {
      where.n_good_group_id = params.groupId
    }
    if (params.baseGoodId) {
      where.n_base_good_id = params.baseGoodId
    }
    if (params.unitId) {
      where.n_unit_id = params.unitId
    }
    if (params.code) {
      where.vc_code = params.code
    }
    if (params.name) {
      where.vc_name = params.name
    }
    if (params.isProvider != null) {
      where.c_fl_provider_equipment = encodeBool(params.isProvider)
    }
    if (params.isCustomer != null) {
      where.c_fl_customer_equipment = encodeBool(params.isCustomer)
    }
    if (params.tags) {
      where += prepareEntityTagQuery('N_GOOD_ID', params.tags)
    }
    return hid.getTableData(getGoodsTable(), where: where, order: params.order, limit: params.limit)
  }

  /**
   * Search for good by different fields value
   * @param goodId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param kindId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param kind       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param typeId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param type       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param groupId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param baseGoodId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param unitId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param unit       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isProvider {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isCustomer {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param tags       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param order      {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional
   * @return Good table rows
   */
  Map getGoodBy(Map input) {
    return getGoodsBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Check if entity or entity type is good
   * @param entityOrEntityType {@link java.math.BigInteger BigInteger} or {@link CharSequence String}. Entity id, entity type ref id or entity type ref code
   * @return True if given value is good, false otherwise
   */
  Boolean isGood(def entityOrEntityType) {
    if (entityOrEntityType == null) {
      return false
    }

    Number entityIdOrEntityTypeId = toIntSafe(entityOrEntityType)
    if (entityIdOrEntityTypeId != null) {
      return entityIdOrEntityTypeId == getGoodEntityTypeId() || toIntSafe(getGood(entityIdOrEntityTypeId)?.n_good_kind_id) in [getGoodEntityTypeId(), getPricePlanGoodKindId(), getAdjustmentGoodKindId()]
    } else {
      return entityOrEntityType in [getGoodEntityType(), getPricePlanGoodKind(), getAdjustmentGoodKind()]
    }
  }

  /**
   * Get good unit id
   * @param goodId {@link java.math.BigInteger BigInteger}
   * @return God unit id
   */
  Number getGoodUnitId(def goodId) {
    return toIntSafe(getGood(goodId).n_unit_id)
  }

  /**
   * Get good additional parameter type by id
   * @param goodValueTypeId {@link java.math.BigInteger BigInteger}
   * @return Good additional parameter table row
   */
  Map getGoodAddParamType(def goodValueTypeId) {
    LinkedHashMap where = [
      n_good_value_type_id: goodValueTypeId
    ]
    return hid.getTableFirst(getGoodAddParamTypesTable(), where: where)
  }

  /**
   * Search for good additional parameter types by different fields value
   * @param goodValueTypeId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param dataTypeId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param dataType        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refTypeId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refType         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param canModify       {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isMulti         {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isObject        {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param rem             {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit           {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order           {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: [:]
   * @return Good additional parameter type table rows
   */
  List<Map> getGoodAddParamTypesBy(Map input) {
    LinkedHashMap params = mergeParams([
      goodValueTypeId : null,
      goodId          : null,
      dataTypeId      : null,
      code            : null,
      name            : null,
      refTypeId       : null,
      canModify       : null,
      isMulti         : null,
      isObject        : null,
      rem             : null,
      limit           : 0
    ], input)
    LinkedHashMap where = [:]

    if (params.goodValueTypeId || params.paramId) {
      where.n_good_value_type_id = params.goodValueTypeId ?: params.paramId
    }
    if (params.goodId) {
      where.n_good_type_id = params.goodId
    }
    if (params.dataTypeId) {
      where.n_data_type_id = params.dataTypeId
    }
    if (params.code) {
      where.vc_code = params.code
    }
    if (params.name) {
      where.vc_name = params.name
    }
    if (params.refTypeId || params.refId) {
      where.n_ref_type_id = params.refTypeId ?: params.refId
    }
    if (params.canModify != null) {
      where.c_can_modify = encodeBool(params.canModify)
    }
    if (params.isMulti != null) {
      where.c_fl_multi = encodeBool(params.isMulti)
    }
    if (params.isObject != null) {
      where.c_fl_object = encodeBool(params.isObject)
    }
    return hid.getTableData(getGoodAddParamTypesTable(), where: where, order: params.order, limit: params.limit)
  }

  /**
   * Search for good additional parameter type by different fields value
   * @param goodValueTypeId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param dataTypeId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param dataType        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refTypeId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refType         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param canModify       {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isMulti         {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isObject        {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param rem             {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param order           {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: [:]
   * @return Good additional parameter type table row
   */
  Map getGoodAddParamTypeBy(Map input) {
    return getGoodAddParamTypesBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Search for good additional parameter type by code
   * @param code {@link CharSequence String}
   * @return Good additional parameter type table row
   */
  Map getGoodAddParamTypeByCode(CharSequence code) {
    return getGoodAddParamTypeBy(code: code)
  }

  /**
   * Search for good additional parameter type id by code
   * @param code {@link CharSequence String}
   * @return Good additional parameter type id
   */
  Number getGoodAddParamTypeIdByCode(CharSequence code) {
    return toIntSafe(getGoodAddParamTypeByCode(code)?.n_good_value_type_id)
  }

  /**
   * Prepare good additional parameter value to save
   * @param paramId {@link java.math.BigInteger BigInteger}. Optional if 'param' is passed
   * @param param   {@link CharSequence String}. Optional if 'paramId' is passed
   * @param value   Any type. Optional
   * @return Additional parameter value as Map
   * <pre>
   * {@code
   * [
   *   paramId : _, # good additional parameter type id
   *   bool    : _, # if additional parameter is boolean type
   *   number  : _, # if additional parameter is number type
   *   string  : _, # if additional parameter is string type
   *   date    : _, # if additional parameter is date type
   *   refId   : _, # if additional parameter is refId type and value can be converted to BigInteger (ref id)
   *   ref     : _  # if additional parameter is refId type and value cannot be converted to BigInteger (ref code)
   * ]
   * }
   * </pre>
   */
  Map prepareGoodAddParam(Map input) {
    LinkedHashMap param = null
    if (input.containsKey('param')) {
      param = getGoodAddParamTypeByCode(input.param.toString())
      input.paramId = param?.n_good_value_type_id
      input.remove('param')
    } else if (input.containsKey('paramId')) {
      param = getGoodAddParamType(input.paramId)
    }
    input.isMultiple = decodeBool(param.c_fl_multi)

    if (input.containsKey('value')) {
      def (valueType, val) = getAddParamDataType(param, input.value)
      input."${valueType}" = val
      input.remove('value')
    }
    return input
  }

  /**
   * Search for good additional parameter values by different fields value
   * @param goodValueId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param paramId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional if 'param' is passed
   * @param param       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional if 'paramId' is passed
   * @param date        {@link java.time.Temporal Any date type}. Optional
   * @param number      {@link Double}, {@link Integer}, {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param string      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bool        {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ref         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param value       Any type which is automatically converted to 'date', 'string', 'name', 'bool' or 'refId', see {@link #prepareGoodAddParam(Map)}. Optional
   * @param limit       {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order       {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: [:]
   * @return Good additional parameter value table rows
   */
  List<Map> getGoodAddParamsBy(Map input) {
    LinkedHashMap params = mergeParams([
      goodValueId : null,
      goodId      : null,
      paramId     : null,
      date        : null,
      string      : null,
      number      : null,
      bool        : null,
      refId       : null,
      limit       : 0
    ], prepareGoodAddParam(input))
    LinkedHashMap where = [:]

    if (params.goodValueId) {
      where.n_good_value_id = params.goodValueId
    }
    if (params.goodId) {
      where.n_good_id = params.goodId
    }
    if (params.paramId) {
      where.n_good_value_type_id = params.paramId
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
      where.c_fl_value = encodeBool(params.bool)
    }
    if (params.refId) {
      where.n_ref_id = params.refId
    }
    return hid.getTableData(getGoodAddParamsTable(), where: where, order: params.order, limit: params.limit)
  }

  /**
   * Search for good additional parameter value by different fields value
   * @param goodValueId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param paramId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional if 'param' is passed
   * @param param       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional if 'paramId' is passed
   * @param date        {@link java.time.Temporal Any date type}. Optional
   * @param number      {@link Double}, {@link Integer}, {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param string      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bool        {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ref         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param value       Any type which is automatically converted to 'date', 'string', 'name', 'bool' or 'refId', see {@link #prepareGoodAddParam(Map)}. Optional
   * @param order       {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: [:]
   * @return Good additional parameter value table row
   */
  Map getGoodAddParamBy(Map input) {
    return getGoodAddParamsBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Get service providing schemes table name
   */
  String getServSchemesTable() {
    return SERV_SCHEMES_TABLE
  }

  /**
   * Get service providing scheme by id
   * @param servSchemeId {@link java.math.BigInteger BigInteger}
   * @return service providing scheme table row
   */
  Map getServScheme(def servSchemeId) {
    LinkedHashMap where = [
      n_serv_scheme_id: servSchemeId
    ]
    return hid.getTableFirst(getServSchemesTable(), where: where)
  }

  /**
   * Search for service providing schemes by different fields value
   * @param servSchemeId           {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code                   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name                   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ratingMethodId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ratingMethod           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param servTypeId             {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param servType               {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param durationTypeId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param durationType           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param durationUnitId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param durationUnit           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param duration               {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param providingTypeId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param providingType          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param reservedUnitId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param reservedUnit           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param reserved               {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subscrPermissionId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subscrPermission       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param unsubscrPermissionId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param unsubscrPermission     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param aggrPeriodUnitId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param aggrPeriodUnit         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param aggrPeriod             {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isArchivingEnabled     {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param expPeriodUnitId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param expPeriodUnit          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param expPeriod              {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isArchiveGroupedByAddr {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param providingPointId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param providingPoint         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param changelogCtrlId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param changelogCtrl          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param pretermCloseReasonIds  {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param servEndChargeTypeId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param servEndChargeType      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param restrictConditionId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param restrictCondition      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param matchingPriorityId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param matchingPriority       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isArchived             {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. Default: false
   * @param limit                  {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order                  {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional
   * @return Service providing scheme table rows
   */
  List<Map> getServSchemesBy(Map input) {
    LinkedHashMap params = mergeParams([
      servSchemeId           : null,
      code                   : null,
      name                   : null,
      ratingMethodId         : null,
      servTypeId             : null,
      durationTypeId         : null,
      durationUnitId         : null,
      duration               : null,
      providingTypeId        : null,
      reservedUnitId         : null,
      reserved               : null,
      subscrPermissionId     : null,
      unsubscrPermissionId   : null,
      aggrPeriodUnitId       : null,
      aggrPeriod             : null,
      isArchivingEnabled     : null,
      expPeriodUnitId        : null,
      expPeriod              : null,
      isArchiveGroupedByAddr : null,
      providingPointId       : null,
      changelogCtrlId        : null,
      pretermCloseReasonIds  : null,
      servEndChargeTypeId    : null,
      restrictConditionId    : null,
      matchingPriorityId     : null,
      isArchived             : false,
      limit                  : 0
    ], input)
    LinkedHashMap where = [:]

    if (params.servSchemeId) {
      where.n_serv_scheme_id = params.servSchemeId
    }
    if (params.code) {
      where.vc_code = params.code
    }
    if (params.name) {
      where.vc_name = params.name
    }
    if (params.ratingMethodId) {
      where.n_rating_method_id = params.ratingMethodId
    }
    if (params.servTypeId) {
      where.n_serv_type_id = params.servTypeId
    }
    if (params.durationTypeId) {
      where.n_duration_id = params.durationTypeId
    }
    if (params.durationUnitId) {
      where.n_duration_unit_id = params.durationUnitId
    }
    if (params.duration) {
      where.n_duration_value = params.duration
    }
    if (params.duration) {
      where.n_duration_value = params.duration
    }
    if (params.duration) {
      where.n_duration_value = params.duration
    }
    if (params.providingTypeId) {
      where.n_providing_type_id = params.providingTypeId
    }
    if (params.reservedUnitId) {
      where.n_reserved_unit_id = params.reservedUnitId
    }
    if (params.reserved) {
      where.n_reserved_value = params.reserved
    }
    if (params.subscrPermissionId) {
      where.n_subscr_permission_id = params.subscrPermissionId
    }
    if (params.unsubscrPermissionId) {
      where.n_unsubscr_permission_id = params.unsubscrPermissionId
    }
    if (params.aggrPeriodUnitId) {
      where.n_aggr_perion_unit_id = params.aggrPeriodUnitId
    }
    if (params.aggrPeriod) {
      where.n_aggr_perion_value = params.aggrPeriod
    }
    if (params.isArchivingEnabled != null) {
      where.c_fl_enable_archiving = encodeBool(params.isArchivingEnabled)
    }
    if (params.expPeriodUnitId) {
      where.n_exp_period_unit_id = params.expPeriodUnitId
    }
    if (params.expPeriod) {
      where.n_exp_period_value = params.expPeriod
    }
    if (params.isArchiveGroupedByAddr != null) {
      where.c_fl_arch_group_addr = encodeBool(params.isArchiveGroupedByAddr)
    }
    if (params.providingPointId) {
      where.n_providion_point_id = params.providingPointId
    }
    if (params.changelogCtrlId) {
      where.n_changelog_ctrl_id = params.changelogCtrlId
    }
    if (params.pretermCloseReasonIds) {
      where.t_preterm_close_reasons_id = params.pretermCloseReasonIds
    }
    if (params.servEndChargeTypeId) {
      where.n_serv_end_charge_type_id = params.servEndChargeTypeId
    }
    if (params.restrictConditionId) {
      where.n_restrict_condition_id = params.restrictConditionId
    }
    if (params.matchingPriorityId) {
      where.n_matching_priority_id = params.matchingPriorityId
    }
    if (params.isArchived != null) {
      where.c_fl_archived = encodeBool(params.isArchived)
    }

    return hid.getTableData(getServSchemesTable(), where: where, order: params.order, limit: params.limit)
  }

  /**
   * Search for service providing scheme by different fields value
   * @param servSchemeId           {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code                   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name                   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ratingMethodId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ratingMethod           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param servTypeId             {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param servType               {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param durationTypeId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param durationType           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param durationUnitId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param durationUnit           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param duration               {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param providingTypeId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param providingType          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param reservedUnitId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param reservedUnit           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param reserved               {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subscrPermissionId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subscrPermission       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param unsubscrPermissionId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param unsubscrPermission     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param aggrPeriodUnitId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param aggrPeriodUnit         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param aggrPeriod             {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isArchivingEnabled     {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param expPeriodUnitId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param expPeriodUnit          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param expPeriod              {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isArchiveGroupedByAddr {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param providingPointId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param providingPoint         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param changelogCtrlId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param changelogCtrl          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param pretermCloseReasonIds  {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param servEndChargeTypeId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param servEndChargeType      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param restrictConditionId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param restrictCondition      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param matchingPriorityId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param matchingPriority       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isArchived             {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. Default: false
   * @param limit                  {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order                  {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional
   * @return Service providing scheme table row
   */
  Map getServSchemeBy(Map input) {
    return getServSchemesBy(input + [limit: 1])?.getAt(0)
  }
}