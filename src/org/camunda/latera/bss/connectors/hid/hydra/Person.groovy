package org.camunda.latera.bss.connectors.hid.hydra

trait Person {
  static String PERSONS_TABLE         = 'SI_V_PERSONS'
  static String PERSONS_PRIVATE_TABLE = 'SI_V_PERSONS_PRIVATE'
  static String PERSON_TYPE           = 'SUBJ_TYPE_Company'

  LinkedHashMap getPerson(def subjectId) {
    LinkedHashMap where = [
      n_subject_id: subjectId
    ]
    return hid.getTableFirst(PERSONS_TABLE, where: where)
  }

  LinkedHashMap getPersonPrivate(def subjectId) {
    LinkedHashMap where = [
      n_subject_id: subjectId
    ]
    return hid.getTableFirst(PERSONS_PRIVATE_TABLE, where: where)
  }

  Boolean isPerson(String subjectType) {
    return subjectType == PERSON_TYPE
  }

  Boolean isPerson(def subjectTypeId) {
    return subjectTypeId == getRefCodeById(PERSON_TYPE)
  }

  LinkedHashMap putPerson(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      id            :  null,
      firstName     :  null,
      secondName    :  null,
      lastName      :  null,
      opfId         :  null,
      sexId         :  null,
      inn           :  null,
      kpp           :  null,
      docTypeId     :  null,
      docSerial     :  null,
      docNumber     :  null,
      docDate       :  null,
      docDepartment :  null,
      docAuthor     :  null,
      birthDate     :  null,
      birthPlace    :  null,
      rem           :  null,
      groupId       :  null,
      firmId        :  DEFAULT_FIRM,
      stateId       :  getRefIdByCode(DEFAULT_SUBJECT_STATE)
    ], input)
    try {
      logger.info("Putting person named ${params.firstName} ${params.secondName} ${params.lastName} in firm ${params.firmId}")
      LinkedHashMap person = hid.execute('SI_PERSONS_PKG.SI_PERSONS_PUT', [
        num_N_SUBJECT_ID       : params.id,
        num_N_FIRM_ID          : params.firmId,
        num_N_SUBJ_STATE_ID    : params.stateId,
        num_N_SUBJ_GROUP_ID    : params.groupId,
        num_N_SEX_ID           : params.sexId,
        num_N_OPF_ID           : params.opfId,
        vch_VC_FIRST_NAME      : params.firstName,
        vch_VC_SURNAME         : params.lastName,
        vch_VC_SECOND_NAME     : params.secondName,
        vch_VC_INN             : params.inn,
        vch_VC_KPP             : params.kpp,
        num_N_DOC_AUTH_TYPE_ID : params.docTypeId,
        vch_VC_DOC_SERIAL      : params.docSerial,
        vch_VC_DOC_NO          : params.docNumber,
        dt_D_DOC               : params.docDate,
        vch_VC_DOCUMENT        : params.docAuthor,
        vch_VC_DOC_DEPARTMENT  : params.docDepartment,
        dt_D_BIRTH             : params.birthDate,
        vch_VC_BIRTH_PLACE     : params.birthPlace,
        vch_VC_REM             : params.rem
      ])
      logger.info("   Person ${personId.num_N_SUBJECT_ID} was put successfully!")
      return person
    } catch (Exception e){
      logger.error("Error while creating person!")
      logger.error(e)
      return null
    }
  }
}