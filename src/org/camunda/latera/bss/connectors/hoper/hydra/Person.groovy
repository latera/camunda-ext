package org.camunda.latera.bss.connectors.hoper.hydra

trait Person {
  private static LinkedHashMap PERSON_ENTITY_TYPE = [
    one    : 'person',
    plural : 'persons'
  ]

  def getPersonEntityType(def id = null) {
    return PERSON_ENTITY_TYPE + withParent(getSubjectEntityType()) + withId(id)
  }

  LinkedHashMap getPersonDefaultParams() {
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

  LinkedHashMap getPersonParamsMap(LinkedHashMap params, LinkedHashMap additionalParams = [:]) {
    def result = [
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

  LinkedHashMap getPersonParams(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    def params = getPersonDefaultParams() + input
    def data   = getPersonParamsMap(params + additionalParams)
    return prepareParams(data)
  }

  List getPersons(LinkedHashMap input = [:]) {
    def params = getPaginationDefaultParams() + input
    return getEntities(getPersonEntityType(), params)
  }

  LinkedHashMap getPerson(def id) {
    return getEntity(getPersonEntityType(), id)
  }

  LinkedHashMap createPerson(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    LinkedHashMap params = getPersonParams(input, additionalParams)
    return createEntity(getPersonEntityType(), params)
  }

  LinkedHashMap updatePerson(def id, LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    LinkedHashMap params = getPersonParams(input, additionalParams)
    return updateEntity(getPersonEntityType(), id, params)
  }

  LinkedHashMap putPerson(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    def personId = input.personId
    input.remove('personId')

    if (personId) {
      return updatePerson(personId, input, additionalParams)
    } else {
      return createPerson(input, additionalParams)
    }
  }

  Boolean deletePerson(def id) {
    return deleteEntity(getPersonEntityType(), id)
  }
}