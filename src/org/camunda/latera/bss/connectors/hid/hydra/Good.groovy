package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.utils.Oracle
trait Good {
  private static String GOODS_TABLE                = 'SR_V_GOODS'
  private static String GOOD_ADD_PARAMS_TABLE      = 'SR_V_GOOD_VALUES'
  private static String GOOD_ADD_PARAM_TYPES_TABLE = 'SR_V_GOOD_VALUES_TYPE'

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
    if (params.tags) {
      where.t_tags = params.tags
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
}