package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.DateTimeUtil.dayBegin

trait Person {
  private static String PERSONS_TABLE         = 'SI_V_PERSONS'
  private static String PERSONS_PRIVATE_TABLE = 'SI_V_PERSONS_PRIVATE'
  private static String PERSON_TYPE           = 'SUBJ_TYPE_Company'

  def getPersonType() {
    return PERSON_TYPE
  }

  def getPersonTypeId() {
    return getRefIdByCode(getPersonType())
  }

  def getPersonsTable() {
    return PERSONS_TABLE
  }

  def getPersonsPrivateTable() {
    return PERSONS_PRIVATE_TABLE
  }

  LinkedHashMap getPerson(def personId) {
    LinkedHashMap where = [
      n_subject_id: personId
    ]
    return hid.getTableFirst(getPersonsTable(), where: where)
  }

  List getPersonsBy(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      personId   : null,
      regionId   : null,
      creatorId  : null,
      name       : null,
      code       : null,
      firstName  : null,
      secondName : null,
      lastName   : null,
      birthDate  : null,
      inn        : null,
      opfId      : null,
      sexId      : null,
      groupId    : null,
      firmId     : getFirmId(),
      stateId    : getSubjectStateOnId()
    ], input)
    LinkedHashMap where = [:]

    if (params.personId) {
      where.n_person_id = params.personId
    }
    if (params.regionId) {
      where.n_region_id = params.regionId
    }
    if (params.creatorId) {
      where.n_creator_id = params.creatorId
    }
    if (params.name) {
      where.vc_name = params.name
    }
    if (params.code) {
      where.vc_code = params.code
    }
    if (params.firstName) {
      where.vc_first_name = params.firstName
    }
    if (params.secondName) {
      where.vc_second_name = params.secondName
    }
    if (params.lastName) {
      where.vc_surname = params.lastName
    }
    if (params.birthDate) {
      where.d_birth = params.birthDate
    }
    if (params.inn) {
      where.vc_inn = params.inn
    }
    if (params.sexId) {
      where.n_sex_id = params.sexId
    }
    if (params.opfId) {
      where.n_opf_id = params.opfId
    }
    if (params.groupId) {
      where.n_subj_group_id = params.groupId
    }
    if (params.firmId) {
      where.n_firm_id = params.firmId
    }
    if (params.stateId) {
      where.n_subj_state_id = params.stateId
    }
    return hid.getTableData(getPersonsTable(), where: where)
  }

  LinkedHashMap getPersonBy(LinkedHashMap input) {
    return getPersonsBy(input)?.getAt(0)
  }

  LinkedHashMap getPersonPrivate(def subjectId) {
    LinkedHashMap where = [
      n_subject_id: subjectId
    ]
    return hid.getTableFirst(getPersonsPrivateTable(), where: where)
  }

  Boolean isPerson(String entityType) {
    return entityType == getPersonType()
  }

  Boolean isPerson(def entityIdOrEntityTypeId) {
    return entityIdOrEntityTypeId == getPersonTypeId() || getPerson(entityIdOrEntityTypeId) != null
  }

  LinkedHashMap putPerson(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      personId      : null,
      firstName     : null,
      secondName    : null,
      lastName      : null,
      opfId         : null,
      sexId         : null,
      inn           : null,
      docTypeId     : null,
      docSerial     : null,
      docNumber     : null,
      docDate       : null,
      docDepartment : null,
      docAuthor     : null,
      birthDate     : null,
      birthPlace    : null,
      rem           : null,
      groupId       : null,
      firmId        : getFirmId(),
      stateId       : getSubjectStateOnId()
    ], input)
    try {
      logger.info("Putting person with params ${params}")
      LinkedHashMap person = hid.execute('SI_PERSONS_PKG.SI_PERSONS_PUT', [
        num_N_SUBJECT_ID       : params.personId,
        num_N_FIRM_ID          : params.firmId,
        num_N_SUBJ_STATE_ID    : params.stateId,
        num_N_SUBJ_GROUP_ID    : params.groupId,
        num_N_SEX_ID           : params.sexId,
        num_N_OPF_ID           : params.opfId,
        vch_VC_FIRST_NAME      : params.firstName,
        vch_VC_SURNAME         : params.lastName,
        vch_VC_SECOND_NAME     : params.secondName,
        vch_VC_INN             : params.inn,
        num_N_DOC_AUTH_TYPE_ID : params.docTypeId,
        vch_VC_DOC_SERIAL      : params.docSerial,
        vch_VC_DOC_NO          : params.docNumber,
        dt_D_DOC               : params.docDate   ? dayBegin(params.docDate)   : null,
        vch_VC_DOCUMENT        : params.docAuthor,
        vch_VC_DOC_DEPARTMENT  : params.docDepartment,
        dt_D_BIRTH             : params.birthDate ? dayBegin(params.birthDate) : null,
        vch_VC_BIRTH_PLACE     : params.birthPlace,
        vch_VC_REM             : params.rem
      ])
      logger.info("   Person ${person.num_N_SUBJECT_ID} was put successfully!")
      return person
    } catch (Exception e){
      logger.error("   Error while putting person!")
      logger.error_oracle(e)
      return null
    }
  }

  def getPersonAddParamTypeIdByCode(String code) {
    return getSubjectAddParamTypeIdByCode(code, getCompanyTypeId())
  }

  List getPersonAddParamsBy(LinkedHashMap input) {
    if (input.containsKey('personId')) {
      input.subjectId = input.personId
      input.remove('personId')
    }
    return getSubjectAddParamsBy(input)
  }

  LinkedHashMap getPersonAddParamBy(LinkedHashMap input) {
    if (input.containsKey('personId')) {
      input.subjectId = input.personId
      input.remove('personId')
    }
    return getSubjectAddParamBy(input)
  }

  Boolean putPersonAddParam(LinkedHashMap input) {
    if (input.containsKey('personId')) {
      input.subjectId = input.personId
      input.remove('personId')
    }
    return putSubjectAddParam(input)
  }
}