package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Numeric.toIntStrict
import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import static org.camunda.latera.bss.utils.Numeric.toFloatSafe
import static org.camunda.latera.bss.utils.Oracle.decodeBool

trait AddParam {

  /**
   * Get additional parameter data type
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.connectors.hid.hydra.AddParamSpec#getAddParamDataType%28Map%29"></iframe>
   * @param param {@link Map} with additional parameter type row
   * @return 'string', 'bool', 'number', 'date' or 'refId'
   */
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

  /**
   * Get additional parameter data type with value
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.connectors.hid.hydra.AddParamSpec#getAddParamDataType%28Map%2Cdef%29"></iframe>
   * @param param {@link Map} with additional parameter type row
   * @param value Any type
   * @return Tuple['string'|'bool'|'number'|'date'|'refId'|'ref', value]. If ref code passed, 'ref' returned, otherwise 'refId'
   */
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

  /**
   * Get additional parameter value or value+type
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.connectors.hid.hydra.AddParamSpec#getAddParamValue"></iframe>
   * @param value {@link Map} with additional parameter row
   * @param withType Return just converted value or value+type
   * @param visualRefValue If true, return ref value code, otherwise return id. Default: false
   * @return Value converted into proper class or Tuple['string'|'bool'|'number'|'date'|'refId', Value]
   */
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

  /**
   * Get additional parameter value or value+type
   *
   * Overload for names arguments.
   * @see #getAddParamValue(Map, Boolean, Boolean)
   */
  def getAddParamValue(Map input, Map value) {
    LinkedHashMap params = [
      withType       : true,
      visualRefValue : false
    ] + input
    return getAddParamValue(value, params.withType, params.visualRefValue)
  }

  /**
   * Get additional parameter value without type
   *
   * Overload for returning only value.
   * @see #getAddParamValue(Map, Boolean, Boolean)
   */
  def getAddParamValueRaw(Map value, Boolean visualRefValue = false) {
    return getAddParamValue(value, withType: false, visualRefValue: visualRefValue)
  }
}