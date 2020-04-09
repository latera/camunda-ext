package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import static org.camunda.latera.bss.utils.DateTimeUtil.dayBegin
import static org.camunda.latera.bss.utils.StringUtil.isEmpty
import static org.camunda.latera.bss.utils.StringUtil.notEmpty
import static org.camunda.latera.bss.utils.Constants.SUBJ_TYPE_Person

/**
 * Person (individual) specific methods
 */
trait Person {
  private static String PERSONS_TABLE         = 'SI_V_PERSONS'
  private static String PERSONS_PRIVATE_TABLE = 'SI_V_PERSONS_PRIVATE'

  /**
   * Get persons table name
   */
  String getPersonsTable() {
    return PERSONS_TABLE
  }

  /**
   * Get persons private data table name
   */
  String getPersonsPrivateTable() {
    return PERSONS_PRIVATE_TABLE
  }

  /**
   * Get person subject type ref code
   */
  String getPersonType() {
    return getRefCode(getPersonTypeId())
  }

  /**
   * Get person subject type ref id
   */
  Number getPersonTypeId() {
    return SUBJ_TYPE_Person
  }

  /**
   * Get person by id
   * @param personIdd {@link java.math.BigInteger BigInteger}
   * @return Person table row
   */
  Map getPerson(def personId) {
    LinkedHashMap where = [
      n_subject_id: personId
    ]
    return hid.getTableFirst(getPersonsTable(), where: where)
  }

  /**
   * Search for persons by different fields value
   * @param personId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param regionId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param creatorId  {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param firstName  {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param secondName {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lastName   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param birthDate  {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param inn        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param opfId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param opf        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param sexId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param sex        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param groupId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param stateId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param state      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param firmId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. Default: current firm id
   * @param tags       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit      {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order      {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: [:]
   * @return Person table rows
   */
  List<Map> getPersonsBy(Map input) {
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

  /**
   * Search for person by different fields value
   * @param personId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param regionId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param creatorId  {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param firstName  {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param secondName {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lastName   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param birthDate  {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param inn        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param opfId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param opf        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param groupId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param stateId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param state      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param firmId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. Default: current firm id
   * @param tags       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param order      {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: [:]
   * @return Person table row
   */
  Map getPersonBy(Map input) {
    return getPersonsBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Get person private data by id
   * @param companyId {@link java.math.BigInteger BigInteger}
   * @return Person private data table row
   */
  Map getPersonPrivate(def subjectId) {
    LinkedHashMap where = [
      n_subject_id: subjectId
    ]
    return hid.getTableFirst(getPersonsPrivateTable(), where: where)
  }

  /**
   * Search for persons private data by different fields value
   * @param personId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param regionId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param creatorId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param firstName     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param secondName    {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lastName      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docTypeId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docType       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docSerial     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docNumber     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docDate       {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docDepartment {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docAuthor     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param birthDate     {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param birthPlace    {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param opfId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param opf           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param sexId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param sex           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param groupId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param stateId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param state         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param firmId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. Default: current firm id
   * @param tags          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit         {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order         {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: [:]
   * @return Person private data table rows
   */
  List<Map> getPersonsPrivateBy(Map input) {
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

  /**
   * Search for person private data by different fields value
   * @param personId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param regionId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param creatorId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param firstName     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param secondName    {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lastName      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docTypeId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docType       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docSerial     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docNumber     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docDate       {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docDepartment {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docAuthor     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param birthDate     {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param birthPlace    {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param opfId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param opf           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param sexId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param sex           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param groupId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param stateId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param state         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param firmId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. Default: current firm id
   * @param tags          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit         {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order         {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: [:]
   * @return Person private data table row
   */
  Map getPersonPrivateBy(Map input) {
    return getPersonsPrivateBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Check if entity or entity type is person
   * @param entityOrEntityType {@link java.math.BigInteger BigInteger} or {@link CharSequence String}. Subject id, subject type ref id or subject type ref code
   * @return True if given value is person, false otherwise
   */
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

  /**
   * Create person
   * @param firstName     {@link CharSequence String}. Optional
   * @param secondName    {@link CharSequence String}. Optional
   * @param lastName      {@link CharSequence String}. Optional
   * @param opfId         {@link java.math.BigInteger BigInteger}. Optional
   * @param opf           {@link CharSequence String}. Optional
   * @param sexId         {@link java.math.BigInteger BigInteger}. Optional
   * @param sex           {@link CharSequence String}. Optional
   * @param inn           {@link CharSequence String}. Optional
   * @param docTypeId     {@link java.math.BigInteger BigInteger}. Optional
   * @param docType       {@link CharSequence String}. Optional
   * @param docSerial     {@link CharSequence String}. Optional
   * @param docNumber     {@link CharSequence String}. Optional
   * @param docDate       {@link java.time.Temporal Any date type}. Optional
   * @param docDepartment {@link CharSequence String}. Optional
   * @param docAuthor     {@link CharSequence String}. Optional
   * @param birthDate     {@link java.time.Temporal Any date type}. Optional
   * @param birthPlace    {@link CharSequence String}. Optional
   * @param rem           {@link CharSequence String}. Optional
   * @param groupId       {@link java.math.BigInteger BigInteger}. Optional
   * @param firmId        {@link java.math.BigInteger BigInteger}. Optional. Default: current firm id
   * @param stateId       {@link java.math.BigInteger BigInteger}. Optional. Default: active subject state
   * @param state         {@link CharSequence String}. Optional
   * @return Created person private data (in Oracle API procedure notation)
   */
  Map createPerson(Map input) {
    input.remove('personId')
    return putPerson(input)
  }

  /**
   * Update person
   * @param personId      {@link java.math.BigInteger BigInteger}
   * @param firstName     {@link CharSequence String}. Optional
   * @param secondName    {@link CharSequence String}. Optional
   * @param lastName      {@link CharSequence String}. Optional
   * @param opfId         {@link java.math.BigInteger BigInteger}. Optional
   * @param opf           {@link CharSequence String}. Optional
   * @param sexId         {@link java.math.BigInteger BigInteger}. Optional
   * @param sex           {@link CharSequence String}. Optional
   * @param inn           {@link CharSequence String}. Optional
   * @param docTypeId     {@link java.math.BigInteger BigInteger}. Optional
   * @param docType       {@link CharSequence String}. Optional
   * @param docSerial     {@link CharSequence String}. Optional
   * @param docNumber     {@link CharSequence String}. Optional
   * @param docDate       {@link java.time.Temporal Any date type}. Optional
   * @param docDepartment {@link CharSequence String}. Optional
   * @param docAuthor     {@link CharSequence String}. Optional
   * @param birthDate     {@link java.time.Temporal Any date type}. Optional
   * @param birthPlace    {@link CharSequence String}. Optional
   * @param rem           {@link CharSequence String}. Optional
   * @param groupId       {@link java.math.BigInteger BigInteger}. Optional
   * @param firmId        {@link java.math.BigInteger BigInteger}. Optional. Default: current firm id
   * @param stateId       {@link java.math.BigInteger BigInteger}. Optional. Default: active subject state
   * @param state         {@link CharSequence String}. Optional
   * @return Updated person private data (in Oracle API procedure notation)
   */
  Map updatePerson(Map input = [:], def personId) {
    return putPerson(input + [personId: personId])
  }

  /**
   * Create or update person
   * @param personId      {@link java.math.BigInteger BigInteger}. Optional
   * @param firstName     {@link CharSequence String}. Optional
   * @param secondName    {@link CharSequence String}. Optional
   * @param lastName      {@link CharSequence String}. Optional
   * @param opfId         {@link java.math.BigInteger BigInteger}. Optional
   * @param opf           {@link CharSequence String}. Optional
   * @param sexId         {@link java.math.BigInteger BigInteger}. Optional
   * @param sex           {@link CharSequence String}. Optional
   * @param inn           {@link CharSequence String}. Optional
   * @param docTypeId     {@link java.math.BigInteger BigInteger}. Optional
   * @param docType       {@link CharSequence String}. Optional
   * @param docSerial     {@link CharSequence String}. Optional
   * @param docNumber     {@link CharSequence String}. Optional
   * @param docDate       {@link java.time.Temporal Any date type}. Optional
   * @param docDepartment {@link CharSequence String}. Optional
   * @param docAuthor     {@link CharSequence String}. Optional
   * @param birthDate     {@link java.time.Temporal Any date type}. Optional
   * @param birthPlace    {@link CharSequence String}. Optional
   * @param rem           {@link CharSequence String}. Optional
   * @param groupId       {@link java.math.BigInteger BigInteger}. Optional
   * @param firmId        {@link java.math.BigInteger BigInteger}. Optional. Default: current firm id
   * @param stateId       {@link java.math.BigInteger BigInteger}. Optional. Default: active subject state
   * @param state         {@link CharSequence String}. Optional
   * @return Created person (in Oracle API procedure notation)
   */
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

  /**
   * Get additional parameter type id by code
   * @param code {@link CharSequence String}
   * @return Additional parameter type id
   */
  Number getPersonAddParamTypeIdByCode(CharSequence code) {
    return toIntSafe(getSubjectAddParamTypeIdByCode(code, getPersonTypeId()))
  }

  /**
   * Search for person additional parameters by different fields value
   * @see Subject#getSubjectAddParamsBy(java.util.Map)
   */
  List getPersonAddParamsBy(Map input) {
    if (input.containsKey('personId')) {
      input.subjectId = input.personId
      input.remove('personId')
    }
    return getSubjectAddParamsBy(input)
  }

  /**
   * Search for person additional parameter by different fields value
   * @see Subject#getSubjectAddParamBy(java.util.Map)
   */
  Map getPersonAddParamBy(Map input) {
    if (input.containsKey('personId')) {
      input.subjectId = input.personId
      input.remove('personId')
    }
    return getSubjectAddParamBy(input)
  }

  /**
   * Add person additional parameter value
   * @see Subject#addSubjectAddParam(java.util.Map)
   */
  Map addPersonAddParam(Map input = [:], def personId) {
    return addSubjectAddParam(input, personId)
  }

  /**
   * Add tag to person
   * @see Subject#addSubjectTag(java.util.Map)
   */
  Map addPersonTag(Map input) {
    input.subjectId = input.subjectId ?: input.personId
    input.remove('personId')
    return addSubjectTag(input)
  }

  /**
   * Add tag to person
   * @see Subject#addSubjectTag(def, java.lang.CharSequence)
   */
  Map addPersonTag(def personId, CharSequence tag) {
    return addPersonTag(personId: personId, tag: tag)
  }

  /**
   * Add tag to person
   * @see Subject#addSubjectTag(java.util.Map, def)
   */
  Map addPersonTag(Map input = [:], def personId) {
    return addPersonTag(input + [personId: personId])
  }

  /**
   * Delete tag from person
   * @see Subject#deleteSubjectTag(def)
   */
  Boolean deletePersonTag(def personTagId) {
    return deleteSubjectTag(personTagId)
  }

  /**
   * Delete tag from person
   * @see Subject#deleteSubjectTag(java.util.Map)
   */
  Boolean deletePersonTag(Map input) {
    input.subjectId = input.subjectId ?: input.personId
    input.remove('personId')
    return deleteSubjectTag(input)
  }

  /**
   * Delete tag from person
   * @see Subject#deleteSubjectTag(def, java.lang.CharSequence)
   */
  Boolean deletePersonTag(def personId, CharSequence tag) {
    return deletePersonTag(personId: personId, tag: tag)
  }

  /**
   * Refresh persons quick search material view
   * @see Search#refreshMaterialView(java.lang.CharSequence, java.lang.CharSequence)
   */
  Boolean refreshPersons(CharSequence method = 'C') {
    return refreshSubjects(method)
  }
}