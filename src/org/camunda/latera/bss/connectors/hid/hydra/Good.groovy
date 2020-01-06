package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Oracle.encodeBool
import static org.camunda.latera.bss.utils.Oracle.decodeBool
import static org.camunda.latera.bss.utils.Numeric.toIntSafe

trait Good {
  private static String GOODS_TABLE                = 'SR_V_GOODS'
  private static String GOOD_ADD_PARAMS_TABLE      = 'SR_V_GOOD_VALUES'
  private static String GOOD_ADD_PARAM_TYPES_TABLE = 'SR_V_GOOD_VALUES_TYPE'
  private static String SERV_SCHEMES_TABLE         = 'SR_V_SERV_SCHEMES'
  private static String ENTITY_TYPE_GOOD           = 'ENTITY_TYPE_CatalogItem'
  private static String GOOD_KIND_PRICE_PLAN       = 'Good_Packs'
  private static String GOOD_KIND_SERVICE          = 'GOOD_Serv'
  private static String GOOD_KIND_ADJUSTMENT       = 'Good_Adjustments'
  private static String GOOD_KIND_REALIY_OBJECT    = 'Good_Realty'
  private static String GOOD_KIND_OBJECT           = 'GOOD_Value'
  private static String GOOD_KIND_NET_SERVICE      = 'Good_NetServ'

  String getGoodsTable() {
    return GOODS_TABLE
  }

  String getGoodAddParamsTable() {
    return GOOD_ADD_PARAMS_TABLE
  }

  String getGoodAddParamTypesTable() {
    return GOOD_ADD_PARAM_TYPES_TABLE
  }

  String getGoodEntityType() {
    return ENTITY_TYPE_GOOD
  }

  Number getGoodEntityTypeId() {
    return getRefIdByCode(getGoodEntityType())
  }

  String getPricePlanGoodKind() {
    return GOOD_KIND_PRICE_PLAN
  }

  Number getPricePlanGoodKindId() {
    return getRefIdByCode(getPricePlanGoodKind())
  }

  String getServiceGoodKind() {
    return GOOD_KIND_SERVICE
  }

  Number getServiceGoodKindId() {
    return getRefIdByCode(getServiceGoodKind())
  }

  String getAdjustmentGoodKind() {
    return GOOD_KIND_ADJUSTMENT
  }

  Number getAdjustmentGoodKindId() {
    return getRefIdByCode(getAdjustmentGoodKind())
  }

  String getRealtyGoodKind() {
    return GOOD_KIND_REALIY_OBJECT
  }

  Number getRealtyGoodKindId() {
    return getRefIdByCode(getRealtyGoodKind())
  }

  String getObjectGoodKind() {
    return GOOD_KIND_OBJECT
  }

  Number getObjectGoodKindId() {
    return getRefIdByCode(getObjectGoodKind())
  }

  String getNetServiceGoodKind() {
    return GOOD_KIND_NET_SERVICE
  }

  Number getNetServiceGoodKindId() {
    return getRefIdByCode(getNetServiceGoodKind())
  }

  Map getGood(def goodId) {
    LinkedHashMap where = [
      n_good_id: goodId
    ]
    return hid.getTableFirst(getGoodsTable(), where: where)
  }

  List getGoodsBy(Map input) {
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
    return hid.getTableData(getGoodsTable(), where: where, order: params.order, limit: params.limit)
  }

  Map getGoodBy(Map input) {
    return getGoodsBy(input + [limit: 1])?.getAt(0)
  }

  Map getGoodByCode(CharSequence code) {
    return getGoodBy(code)
  }

  Map getGoodByName(CharSequence name) {
    return getGoodBy(name)
  }

  Map getGoodIdByCode(CharSequence code) {
    return getGoodByCode(code)?.n_good_id
  }

  Map getGoodIdByName(CharSequence name) {
    return getGoodByName(name)?.n_good_id
  }

  Boolean isGood(CharSequence entityOrEntityType) {
    return getGoodByCode(entityOrEntityType) != null || getGoodByName(entityOrEntityType) != null || entityOrEntityType in [getGoodEntityType(), getPricePlanGoodKind(), getAdjustmentGoodKind()]
  }

  Boolean isGood(def entityIdOrEntityTypeId) {
    return entityIdOrEntityTypeId == getGoodEntityTypeId() || toIntSafe(getGood(entityIdOrEntityTypeId)?.n_good_kind_id) in [getGoodEntityTypeId(), getPricePlanGoodKindId(), getAdjustmentGoodKindId()]
  }

  Number getGoodUnitId(def goodId) {
    return toIntSafe(getGood(goodId).n_unit_id)
  }

  Map getGoodAddParamType(def paramId) {
    LinkedHashMap where = [
      n_good_value_type_id: paramId
    ]
    return hid.getTableFirst(getGoodAddParamTypesTable(), where: where)
  }

  List getGoodAddParamTypesBy(Map input) {
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

  Map getGoodAddParamTypeBy(Map input) {
    return getGoodAddParamTypesBy(input + [limit: 1])?.getAt(0)
  }

  Map getGoodAddParamTypeByCode(CharSequence code) {
    return getGoodAddParamTypeBy(code: code)
  }

  Number getGoodAddParamTypeIdByCode(CharSequence code) {
    return toIntSafe(getGoodAddParamTypeByCode(code)?.n_good_value_type_id)
  }

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

  List getGoodAddParamsBy(Map input) {
    LinkedHashMap params = mergeParams([
      goodId  : null,
      paramId : null,
      date    : null,
      string  : null,
      number  : null,
      bool    : null,
      refId   : null
    ], prepareGoodAddParam(input))
    LinkedHashMap where = [:]

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

  Map getGoodAddParamBy(Map input) {
    return getGoodAddParamsBy(input + [limit: 1])?.getAt(0)
  }

  String getServSchemesTable() {
    return SERV_SCHEMES_TABLE
  }

  Map getServScheme(def servSchemeId) {
    LinkedHashMap where = [
      n_serv_scheme_id: servSchemeId
    ]
    return hid.getTableFirst(getServSchemesTable(), where: where)
  }

  List getServSchemesBy(Map input) {
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

  Map getServSchemeBy(Map input) {
    return getServSchemesBy(input + [limit: 1])?.getAt(0)
  }
}