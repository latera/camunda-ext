package org.camunda.latera.bss.connectors.hid.hydra

trait AddParam {
  def getAddParamDataType(LinkedHashMap param) {
    def typeId = value.n_data_type_id
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
}