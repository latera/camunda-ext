package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.utils.Oracle
trait Good {
  private static String GOODS_TABLE                = 'SR_V_GOODS'
  private static String GOOD_ADD_PARAMS_TABLE      = 'SR_V_GOOD_VALUES'
  private static String GOOD_ADD_PARAM_TYPES_TABLE = 'SR_V_GOOD_VALUES_TYPE'
  private static String SERV_SCHEMES_TABLE         = 'SR_V_SERV_SCHEMES'

  def getGoodsTable() {
    return GOODS_TABLE
  }

  def getGoodAddParamsTable() {
    return GOOD_ADD_PARAMS_TABLE
  }

  def getGoodAddParamTypesTable() {
    return GOOD_ADD_PARAM_TYPES_TABLE
  }

  LinkedHashMap getGood(def goodId) {
    LinkedHashMap where = [
      n_good_id: goodId
    ]
    return hid.getTableData(getGoodsTable(), where: where)
  }

  List getGoodsBy(LinkedHashMap input) {
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
      tags       : null
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
      where.c_fl_provider_equipment = Oracle.encodeBool(params.isProvider)
    }
    if (params.isCustomer != null) {
      where.c_fl_customer_equipment = Oracle.encodeBool(params.isCustomer)
    }
    return hid.getTableData(getGoodsTable(), where: where)
  }

  LinkedHashMap getGoodBy(LinkedHashMap input) {
    return getGoodsBy(input)?.getAt(0)
  }

  def getGoodUnitId(def goodId) {
    LinkedHashMap where = [
      n_good_id: goodId
    ]
    return getGood(goodId).n_unit_id
  }

  LinkedHashMap getGoodAddParamType(def paramId) {
    def where = [
      n_good_value_type_id: paramId
    ]
    return hid.getTableData(getGoodAddParamTypesTable(), where: where)
  }

  LinkedHashMap getGoodAddParamTypesBy(LinkedHashMap input) {
    def params = mergeParams([
      goodValueTypeId : null,
      goodId          : null,
      dataTypeId      : null,
      code            : null,
      name            : null,
      refTypeId       : null,
      canModify       : null,
      isMulti         : null,
      isObject        : null,
      rem             : null
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
      where.c_can_modify = Oracle.encodeBool(params.canModify)
    }
    if (params.isMulti != null) {
      where.c_fl_multi = Oracle.encodeBool(params.isMulti)
    }
    if (params.isObject != null) {
      where.c_fl_object = Oracle.encodeBool(params.isObject)
    }
    return hid.getTableData(getGoodAddParamTypesTable(), where: where)
  }

  LinkedHashMap getGoodAddParamTypeBy(LinkedHashMap input) {
    return getGoodAddParamTypesBy(input)?.getAt(0)
  }

  def getGoodAddParamTypeByCode(String code) {
    return getGoodAddParamTypeBy(code: code)
  }

  def getGoodAddParamTypeIdByCode(String code) {
    return getGoodAddParamTypeByCode(code)?.n_good_value_type_id
  }

  LinkedHashMap prepareGoodAddParam(LinkedHashMap input) {
    def param = null
    if (input.containsKey('param')) {
      param = getGoodAddParamTypeByCode(input.param.toString())
      input.paramId = param?.n_good_value_type_id
      input.remove('param')
    } else if (input.containsKey('paramId')) {
      param = getGoodAddParamType(input.paramId)
    }
    input.isMultiple = Oracle.decodeBool(param.c_fl_multi)

    if (input.containsKey('value')) {
      def valueType = getAddParamDataType(param)
      input."${valueType}" = input.value
      input.remove('value')
    }
    return input
  }

  List getGoodAddParamsBy(LinkedHashMap input) {
    def params = mergeParams([
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
      where.c_fl_value = Oracle.encodeBool(params.bool)
    }
    if (params.refId) {
      where.n_ref_id = params.refId
    }
    return hid.getTableData(getGoodAddParamsTable(), where: where)
  }

  LinkedHashMap getGoodAddParamBy(LinkedHashMap input) {
    return getGoodAddParamsBy(input)?.getAt(0)
  }

  def getServSchemesTable() {
    return SERV_SCHEMES_TABLE
  }

  LinkedHashMap getServSchemes(def servSchemeId) {
    LinkedHashMap where = [
      n_serv_scheme_id: servSchemeId
    ]
    return hid.getTableData(getServSchemesTable(), where: where)
  }

  List getServSchemesBy(LinkedHashMap input) {
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
      isArchived             : false
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
      where.c_fl_enable_archiving = Oracle.encodeBool(params.isArchivingEnabled)
    }
    if (params.expPeriodUnitId) {
      where.n_exp_period_unit_id = params.expPeriodUnitId
    }
    if (params.expPeriod) {
      where.n_exp_period_value = params.expPeriod
    }
    if (params.isArchiveGroupedByAddr != null) {
      where.c_fl_arch_group_addr = Oracle.encodeBool(params.isArchiveGroupedByAddr)
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
      where.c_fl_archived = Oracle.encodeBool(params.isArchived)
    }

    return hid.getTableData(getServSchemesTable(), where: where)
  }

  LinkedHashMap getServSchemeBy(LinkedHashMap input) {
    return getServSchemesBy(input)?.getAt(0)
  }
}