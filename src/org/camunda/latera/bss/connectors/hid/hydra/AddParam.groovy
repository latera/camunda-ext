package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Numeric.toIntStrict
import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import static org.camunda.latera.bss.utils.Numeric.toFloatSafe
import static org.camunda.latera.bss.utils.Oracle.decodeBool

trait AddParam {
  String getAddParamDataType(Map param) {
    Number typeId = toIntSafe(param.n_data_type_id)
    if (typeId == getStringTypeId()) {
      return 'string'
    } else if (typeId == getBooleanTypeId()) {
      return 'bool'
    } else if (typeId == getIntegerTypeId() || typeId == getFloatTypeId()) {
      return 'number'
    } else if (typeId == getDateTypeId()) {
      return 'date'
    } else if (typeId == getRefTypeId()) {
      return 'refId'
    }
  }

  List getAddParamDataType(Map param, def value) {
    String dataType = getAddParamDataType(param)
    if (dataType == 'refId') {
      def val = toIntStrict(value)
      if (val != null) {
        return ['refId', val]
      } else {
        return ['ref', value]
      }
    } else {
      return [dataType, value]
    }
  }

  def getAddParamValue(Map value, Boolean withType = true, Boolean visualRefValue = false) {
    Map param = [:]
    def val = null
    if (value.n_doc_value_type_id) {
      param = getDocumentAddParamType(value.n_doc_value_type_id)
    } else if (value.n_subj_value_type_id) {
      param = getSubjectAddParamType(value.n_subj_value_type_id)
    } else if (value.n_good_value_type_id) {
      param = getGoodAddParamType(value.n_good_value_type_id)
    }

    String dataType = getAddParamDataType(param)
    if (dataType == 'string') {
      val = value.vc_value
    } else if (dataType == 'bool') {
      val = decodeBool(value.c_fl_value)
    } else if (dataType == 'number') {
      val = value.n_value?.replace(',', '.')
      if (val?.contains('.')) {
        val = toFloatSafe(val)
      } else {
        val = toIntSafe(val)
      }
    } else if (dataType == 'date') {
      val = value.d_value
    } else if (dataType == 'refId') {
      if (visualRefValue) {
        val = value.vc_visual_value
      } else {
        val = toIntSafe(value.n_ref_id)
      }
    } else {
      val = value.vc_visual_value
    }
    if (withType) {
      return [val, dataType]
    } else {
      return val
    }
  }

  def getAddParamValue(Map input, Map value) {
    LinkedHashMap params = [
      withType       : true,
      visualRefValue : false
    ] + input
    return getAddParamValue(value, params.withType, params.visualRefValue)
  }

  def getAddParamValueRaw(Map value, Boolean visualRefValue = false) {
    return getAddParamValue(value, withType: false, visualRefValue: visualRefValue)
  }
}