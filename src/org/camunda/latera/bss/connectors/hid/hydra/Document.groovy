package org.camunda.latera.bss.connectors.hid.hydra

trait Document {
  def getDocValueTypeIdByCode(String code) {
    LinkedHashMap where = [
      vc_code: code
    ]
    return this.hid.getTableFirst('SS_V_WFLOW_DOC_VALUES_TYPE', fields: 'n_doc_value_type_id', where: where)
  }
}