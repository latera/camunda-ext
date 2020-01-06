package org.camunda.latera.bss.connectors.hoper.hydra

trait Person {
  private static LinkedHashMap PERSON_ENTITY_TYPE = [
    one    : 'person',
    plural : 'persons'
  ]

  Map getPersonEntityType(def id = null) {
    return PERSON_ENTITY_TYPE + withParent(getSubjectEntityType()) + withId(id)
  }

  private Map getPersonDefaultParams() {
    return [
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
      stateId       : null,
      firmId        : getFirmId()
    ]
  }

  private Map getPersonParamsMap(Map params, Map additionalParams = [:]) {
    LinkedHashMap result = [
      vc_first_name     : params.firstName,
      vc_second_name    : params.secondName,
      vc_surname        : params.lastName,
      n_opf_id          : params.opfId,
      n_sex_id          : params.sexId,
      vc_inn            : params.inn,
      vc_doc_serial     : params.docSerial,
      vc_doc_no         : params.docNumber,
      d_doc             : params.docDate,
      vc_doc_department : params.docDepartment,
      vc_document       : params.docAuthor,
      d_birth           : params.birthDate,
      vc_birth_place    : params.birthPlace,
      vc_pens_insurance : params.pensInsurance,
      vc_med_insurance  : params.medInsurance,
      vc_rem            : params.rem,
      n_subj_state_id   : params.stateId,
      n_firm_id         : params.firmId,
      t_tags            : params.tags
    ]
    if (additionalParams) {
      result.additional_values = params.additionalParams
    }
    return result
  }

  private Map getPersonParams(Map input, Map additionalParams = [:]) {
    LinkedHashMap params = getPersonDefaultParams() + input
    LinkedHashMap data   = getPersonParamsMap(params + additionalParams)
    return prepareParams(data)
  }

  List getPersons(Map input = [:]) {
    LinkedHashMap params = getPaginationDefaultParams() + input
    return getEntities(getPersonEntityType(), params)
  }

  Map getPerson(def id) {
    return getEntity(getPersonEntityType(), id)
  }

  Map createPerson(Map input, Map additionalParams = [:]) {
    LinkedHashMap params = getPersonParams(input, additionalParams)
    return createEntity(getPersonEntityType(), params)
  }

  Map updatePerson(Map input = [:], Map additionalParams = [:]) {
    def id = input.id ?: input.personId
    input.remove('id')
    input.remove('personId')
    return updatePerson(id, input, additionalParams)
  }

  Boolean deletePerson(def id) {
    return deleteEntity(getPersonEntityType(), id)
  }
}