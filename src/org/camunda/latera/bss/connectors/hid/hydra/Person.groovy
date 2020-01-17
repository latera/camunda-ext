package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import static org.camunda.latera.bss.utils.DateTimeUtil.dayBegin
import static org.camunda.latera.bss.utils.StringUtil.isEmpty
import static org.camunda.latera.bss.utils.StringUtil.notEmpty

trait Person {
  private static String PERSONS_TABLE         = 'SI_V_PERSONS'
  private static String PERSONS_PRIVATE_TABLE = 'SI_V_PERSONS_PRIVATE'
  private static String PERSON_TYPE           = 'SUBJ_TYPE_Person'

  String getPersonType() {
    return PERSON_TYPE
  }

  Number getPersonTypeId() {
    return getRefIdByCode(getPersonType())
  }

  String getPersonsTable() {
    return PERSONS_TABLE
  }

  String getPersonsPrivateTable() {
    return PERSONS_PRIVATE_TABLE
  }

  Map getPerson(def personId) {
    LinkedHashMap where = [
      n_subject_id: personId
    ]
    return hid.getTableFirst(getPersonsTable(), where: where)
  }

  List getPersonsBy(Map input) {
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
      stateId    : getSubjectStateOnId(),
      tags       : null,
      limit      : 0
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
      where["TRUNC(d_birth)"] = params.birthDate
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
    if (params.tags) {
      where += prepareEntityTagQuery('N_PERSON_ID', params.tags)
    }
    return hid.getTableData(getPersonsTable(), where: where, order: params.order, limit: params.limit)
  }

  Map getPersonBy(Map input) {
    return getPersonsBy(input + [limit: 1])?.getAt(0)
  }

  Map getPersonByCode(CharSequence code) {
    return getPersonBy(code: code)
  }

  Map getPersonByName(CharSequence name) {
    return getPersonBy(name: name)
  }

  Map getPersonByFio(CharSequence firstName, CharSequence lastName, CharSequence secondName = null) {
    return getPersonBy(firstName: firstName, lastName: lastName, secondName: secondName)
  }

  Map getPersonByFio(Map input) {
    return getPersonByFio(firstName: input.firstName, lastName: input.lastName, secondName: input.secondName)
  }

  Number getPersonIdByCode(CharSequence code) {
    return toIntSafe(getPersonByCode(code)?.n_subject_id)
  }

  Number getPersonIdByName(CharSequence name) {
    return toIntSafe(getPersonByName(name)?.n_subject_id)
  }

  Number getPersonIdByFio(CharSequence firstName, CharSequence lastName, CharSequence secondName = null) {
    return getPersonByFio(firstName, lastName, secondName)
  }

  Number getPersonIdByFio(Map input) {
    return getPersonIdByFio(firstName: input.firstName, lastName: input.lastName, secondName: input.secondName)
  }

  Map getPersonPrivate(def subjectId) {
    LinkedHashMap where = [
      n_subject_id: subjectId
    ]
    return hid.getTableFirst(getPersonsPrivateTable(), where: where)
  }

  List getPersonsPrivateBy(Map input) {
    LinkedHashMap params = mergeParams([
      personId      : null,
      regionId      : null,
      creatorId     : null,
      name          : null,
      code          : null,
      firstName     : null,
      secondName    : null,
      lastName      : null,
      docTypeId     : null,
      docSerial     : null,
      docNumber     : null,
      docDate       : null,
      docDepartment : null,
      docAuthor     : null,
      birthDate     : null,
      birthPlace    : null,
      opfId         : null,
      sexId         : null,
      groupId       : null,
      firmId        : getFirmId(),
      stateId       : getSubjectStateOnId(),
      tags          : null,
      limit         : 0
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
    if (params.docTypeId) {
      where.n_doc_auth_type_id = params.docTypeId
    }
    if (params.docSerial) {
      where.vc_doc_serial = params.docSerial
    }
    if (params.docNumber) {
      where.vc_doc_no = params.docNumber
    }
    if (params.docDate) {
      where.d_doc = params.docDate
    }
    if (params.docDepartment) {
      where.vc_doc_department = params.docDepartment
    }
    if (params.docAuthor) {
      where.vc_document = params.docAuthor
    }
    if (params.birthDate) {
      where["TRUNC(d_birth)"] = params.birthDate
    }
    if (params.birthPlace) {
      where.vc_birth_place = params.birthPlace
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
    if (params.tags) {
      where += prepareEntityTagQuery('N_PERSON_ID', params.tags)
    }
    return hid.getTableData(getPersonsPrivateTable(), where: where, order: params.order, limit: params.limit)
  }

  Map getPersonPrivateBy(Map input) {
    return getPersonsPrivateBy(input + [limit: 1])?.getAt(0)
  }

  Map getPersonPrivateByCode(CharSequence code) {
    return getPersonPrivateBy(code: code)
  }

  Map getPersonPrivateByName(CharSequence name) {
    return getPersonPrivateBy(name: name)
  }

  Map getPersonPrivateByFio(CharSequence firstName, CharSequence lastName, CharSequence secondName = null) {
    return getPersonPrivateBy(firstName: firstName, lastName: lastName, secondName: secondName)
  }

  Map getPersonPrivateByFio(Map input) {
    return getPersonPrivateByFio(firstName: input.firstName, lastName: input.lastName, secondName: input.secondName)
  }

  Boolean isPerson(def entityOrEntityType) {
    if (entityOrEntityType == null) {
      return false
    }

    Number entityIdOrEntityTypeId = toIntSafe(entityOrEntityType)
    if (entityIdOrEntityTypeId != null) {
      return entityIdOrEntityTypeId == getPersonTypeId() || getPerson(entityIdOrEntityTypeId) != null
    } else {
      return entityOrEntityType == getPersonType()
    }
  }

  Map createPerson(Map input) {
    input.remove('personId')
    return putPerson(input)
  }

  Map updatePerson(Map input = [:], def personId) {
    return putPerson(input + [personId: personId])
  }

  private Map putPerson(Map input) {
    LinkedHashMap defaultParams = [
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
    ]
    try {
      if (isEmpty(input.personId) && notEmpty(input.subjectId)) {
        input.personId = input.subjectId
      }

      LinkedHashMap existingPerson = [:]

      if (notEmpty(input.personId)) {
        LinkedHashMap person = getPersonPrivate(input.personId)
        existingPerson = [
          personId      : person.n_person_id,
          firstName     : person.vc_first_name,
          secondName    : person.vc_second_name,
          lastName      : person.vc_surname,
          opfId         : person.n_opf_id,
          sexId         : person.n_sex_id,
          inn           : person.vc_inn,
          docTypeId     : person.n_doc_auth_type_id,
          docSerial     : person.vc_doc_serial,
          docNumber     : person.vc_doc_no,
          docDate       : person.d_doc,
          docDepartment : person.vc_doc_department,
          docAuthor     : person.vc_document,
          birthDate     : person.d_birth,
          birthPlace    : person.vc_birth_place,
          rem           : person.vc_rem,
          groupId       : person.n_subj_group_id,
          firmId        : person.n_firm_id,
          stateId       : person.n_subj_state_id
        ]
      }
      LinkedHashMap params = mergeParams(defaultParams, existingPerson + input)

      logger.info("${params.personId ? 'Updating' : 'Creating'} person with params ${params}")
      LinkedHashMap result = hid.execute('SI_PERSONS_PKG.SI_PERSONS_PUT', [
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
      logger.info("   Person ${result.num_N_SUBJECT_ID} was ${params.personId ? 'updated' : 'created'} successfully!")
      return result
    } catch (Exception e){
      logger.error("   Error while ${input.personId ? 'updating' : 'creating'} person!")
      logger.error_oracle(e)
      return null
    }
  }

  Number getPersonAddParamTypeIdByCode(CharSequence code) {
    return toIntSafe(getSubjectAddParamTypeIdByCode(code, getPersonTypeId()))
  }

  List getPersonAddParamsBy(Map input) {
    if (input.containsKey('personId')) {
      input.subjectId = input.personId
      input.remove('personId')
    }
    return getSubjectAddParamsBy(input)
  }

  Map getPersonAddParamBy(Map input) {
    if (input.containsKey('personId')) {
      input.subjectId = input.personId
      input.remove('personId')
    }
    return getSubjectAddParamBy(input)
  }

  Map addPersonAddParam(Map input = [:], def personId) {
    return addSubjectAddParam(input, personId)
  }

  Map addPersonTag(Map input) {
    input.subjectId = input.subjectId ?: input.personId
    input.remove('personId')
    return addSubjectTag(input)
  }

  Map addPersonTag(def personId, CharSequence tag) {
    return addPersonTag(personId: personId, tag: tag)
  }

  Map addPersonTag(Map input = [:], def personId) {
    return addPersonTag(input + [personId: personId])
  }

  Boolean deletePersonTag(def personTagId) {
    return deleteSubjectTag(personTagId)
  }

  Boolean deletePersonTag(Map input) {
    input.subjectId = input.subjectId ?: input.personId
    input.remove('personId')
    return deleteSubjectTag(input)
  }

  Boolean deletePersonTag(def personId, CharSequence tag) {
    return deletePersonTag(personId: personId, tag: tag)
  }

  Boolean refreshPersons(CharSequence method = 'C') {
    return refreshSubjects(method)
  }
}