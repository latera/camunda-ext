package org.camunda.latera.bss.apis.hid.hydra

import org.camunda.latera.bss.utils.Oracle
trait Subject {
  LinkedHashMap getSubject(subjectId) {
    LinkedHashMap where = [
      n_subject_id: subjectId
    ]
    return this.hid.getTableFirst('SI_V_SUBJECTS', where: where)
  }

  def getSubjectTypeId(subjectId) {
    LinkedHashMap where = [
      n_subject_id: subjectId
    ]
    return this.hid.getTableFirst('SI_V_SUBJECTS', fields: 'n_subj_type_id', where: where)
  }

  Boolean isSubject(String entityType) {
    return entityType.contains('SUBJ')
  }

  Boolean isSubject(def entityTypeId) {
    return this.getRefCodeById(entityTypeId)?.contains('SUBJ')
  }

  def getSubjectValueTypeIdByCode(
    String code,
    def    subjTypeId = null
  ) {
    LinkedHashMap where = [
      vc_code: code
    ]
    if (subjTypeId == null) {
      where.n_subj_type_id = subjTypeId
    }
    return this.hid.getTableFirst('SI_V_SUBJ_VALUES_TYPE', fields: 'n_subj_value_type_id', where: where)
  }

  void putSubjectAddParam(LinkedHashMap input) {
    def defaultParams = [
      subjectId :  null,
      paramId   :  null,
      date      :  null,
      string    :  null,
      number    :  null,
      bool      :  null,
      refId     :  null
    ]
    if (input.containsKey('param')) {
      input.paramId = this.getSubjectValueTypeIdByCode(input.param, this.getSubjectTypeId(input.subjectId))
      input.remove('param')
    }
    LinkedHashMap params = this.mergeParams(defaultParams, input)
    try {
      def paramValue = params.date ?: params.string ?: params.number ?: params.bool ?: params.refId
      this.logger.log("Putting additional param ${params.paramId} value ${paramValue} to subject ${params.subjectId}")

      this.hid.execute('SI_SUBJECTS_PKG.PUT_SUBJ_VALUE', [
        num_N_SUBJECT_ID         : params.subjectId,
        num_N_SUBJ_VALUE_TYPE_ID : params.paramId,
        dt_D_VALUE               : params.date,
        vch_VC_VALUE             : params.string,
        num_N_VALUE              : params.number,
        ch_C_FL_VALUE            : Oracle.encodeBool(params.bool),
        num_N_REF_ID             : params.refId
      ])
      this.logger.log("   Additional param value was put successfully!")
    } catch (Exception e){
      this.logger.log("Error while putting additional param!")
      this.logger.log(e)
    }
  }
}