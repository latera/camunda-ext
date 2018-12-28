package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.utils.Oracle
trait Subject {
  private static String SUBJECTS_TABLE                  = 'SI_V_SUBJECTS'
  private static String SUBJECT_ADDITIONAL_PARAMS_TABLE = 'SI_V_SUBJ_VALUES_TYPE'
  private static String DEFAULT_SUBJECT_STATE           = 'SUBJ_STATE_On'

  LinkedHashMap getSubject(subjectId) {
    LinkedHashMap where = [
      n_subject_id: subjectId
    ]
    return hid.getTableFirst(SUBJECTS_TABLE, where: where)
  }

  def getSubjectTypeId(subjectId) {
    LinkedHashMap where = [
      n_subject_id: subjectId
    ]
    return hid.getTableFirst(SUBJECTS_TABLE, 'n_subj_type_id', where)
  }

  Boolean isSubject(String entityType) {
    return entityType.contains('SUBJ')
  }

  Boolean isSubject(def entityTypeId) {
    return getRefCodeById(entityTypeId)?.contains('SUBJ')
  }

  def getSubjectValueTypeIdByCode(
    String code,
    def    subjTypeId = null
  ) {
    LinkedHashMap where = [
      vc_code: code
    ]
    if (subjTypeId) {
      where.n_subj_type_id = subjTypeId
    }
    return hid.getTableFirst(SUBJECT_ADDITIONAL_PARAMS_TABLE, 'n_subj_value_type_id', where)
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
      input.paramId = getSubjectValueTypeIdByCode(input.param, getSubjectTypeId(input.subjectId))
      input.remove('param')
    }
    LinkedHashMap params = mergeParams(defaultParams, input)
    try {
      def paramValue = params.date ?: params.string ?: params.number ?: params.bool ?: params.refId
      logger.info("Putting additional param ${params.paramId} value ${paramValue} to subject ${params.subjectId}")

      hid.execute('SI_SUBJECTS_PKG.PUT_SUBJ_VALUE', [
        num_N_SUBJECT_ID         : params.subjectId,
        num_N_SUBJ_VALUE_TYPE_ID : params.paramId,
        dt_D_VALUE               : params.date,
        vch_VC_VALUE             : params.string,
        num_N_VALUE              : params.number,
        ch_C_FL_VALUE            : Oracle.encodeBool(params.bool),
        num_N_REF_ID             : params.refId
      ])
      logger.info("   Additional param value was put successfully!")
    } catch (Exception e){
      logger.error("Error while putting additional param!")
      logger.error(e)
    }
  }
}