package org.camunda.latera.bss.connectors.hoper.hydra

trait Company {
  private static LinkedHashMap COMPANY_ENTITY_TYPE = [
    one    : 'organization',
    plural : 'organizations'
  ]

  Map getCompanyEntityType(def id = null) {
    return COMPANY_ENTITY_TYPE + withParent(getSubjectEntityType()) + withId(id)
  }

  private Map getCompanyDefaultParams() {
    return [
      name      : null,
      code      : null,
      opfId     : null,
      inn       : null,
      kpp       : null,
      rem       : null,
      stateId   : null,
      firmId    : getFirmId()
    ]
  }

  private Map getCompanyParamsMap(Map params, Map additionalParams = [:]) {
    LinkedHashMap result = [
      vc_name         : params.name,
      vc_code         : params.code,
      n_opf_id        : params.opfId,
      vc_inn          : params.inn,
      vc_kpp          : params.kpp,
      vc_rem          : params.rem,
      n_subj_state_id : params.stateId,
      n_firm_id       : params.firmId,
      t_tags          : params.tags
    ]
    if (additionalParams) {
      result.additional_values = params.additionalParams
    }
    return result
  }

  private Map getCompanyParams(Map input, Map additionalParams = [:]) {
    LinkedHashMap params = getCompanyDefaultParams() + input
    LinkedHashMap data   = getCompanyParamsMap(params + additionalParams)
    return prepareParams(data)
  }

  List getCompanies(Map input = [:]) {
    LinkedHashMap params = getPaginationDefaultParams() + input
    return getEntities(getCompanyEntityType(), params)
  }

  Map getCompany(def id) {
    return getEntity(getCompanyEntityType(), id)
  }

  Map createCompany(Map input, Map additionalParams = [:]) {
    LinkedHashMap params = getCompanyParams(input, additionalParams)
    return createEntity(getCompanyEntityType(), params)
  }

  Map updateCompany(Map input = [:], def id, Map additionalParams = [:]) {
    LinkedHashMap params = getCompanyParams(input, additionalParams)
    return updateEntity(getCompanyEntityType(), id, params)
  }

  Boolean deleteCompany(def id) {
    return deleteEntity(getCompanyEntityType(), id)
  }
}
