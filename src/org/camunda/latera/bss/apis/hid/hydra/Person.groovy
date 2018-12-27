package org.camunda.latera.bss.apis.hid.hydra

trait Person {
  LinkedHashMap getPerson(subjectId) {
    LinkedHashMap where = [
      n_subject_id: subjectId
    ]
    return this.hid.getTableFirst('SI_V_PERSONS_PRIVATE', where: where)
  }

  LinkedHashMap getPersonPrivate(subjectId) {
    LinkedHashMap where = [
      n_subject_id: subjectId
    ]
    return this.hid.getTableFirst('SI_V_PERSONS', where: where)
  }

  Boolean isPerson(String subjectType) {
    return subjectType == 'SUBJ_TYPE_Person'
  }

  Boolean isPerson(def subjectTypeId) {
    return subjectTypeId == this.getRefCodeById('SUBJ_TYPE_Person')
  }

  LinkedHashMap putPerson(LinkedHashMap input) {
    LinkedHashMap params = this.mergeParams([
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
      firmId        :  100,
      stateId       :  this.getRefIdByCode('SUBJ_STATE_On')
    ], input)
    try {
      this.logger.log("Putting person named ${params.firstName} ${params.secondName} ${params.lastName} in firm ${params.firmId}")
      LinkedHashMap person = this.hid.execute('SI_PERSONS_PKG.SI_PERSONS_PUT', [
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
      this.logger.log("   Person ${personId.num_N_SUBJECT_ID} was put successfully!")
      return person
    } catch (Exception e){
      this.logger.log("Error while creating person!")
      this.logger.log(e)
      return null
    }
  }
}