package org.camunda.latera.bss.connectors.hoper.hydra

trait Company {
  private static LinkedHashMap COMPANY_ENTITY_TYPE_V1 = [
    parent : 'subjects',
    one    : 'company',
    plural : 'companies'
  ]

  private static LinkedHashMap COMPANY_ENTITY_TYPE_V2 = [
    parent : 'subjects',
    one    : 'organization',
    plural : 'organizations'
  ]

  def getCompanyEntityType() {
    if (hoper.version == 1) {
      return COMPANY_ENTITY_TYPE_V1
    }else if (hoper.version == 2) {
      return COMPANY_ENTITY_TYPE_V2
    }
  }

  LinkedHashMap getCompanyDefaultParams() {
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

  LinkedHashMap getCompanyParamsMap(LinkedHashMap params, LinkedHashMap additionalParams = [:]) {
    def result = [
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

  LinkedHashMap getCompanyParams(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    def params = getCompanyDefaultParams() + input
    def data   = getCompanyParamsMap(params + additionalParams)
    return nvlParams(data)
  }

  List getCompanies(LinkedHashMap input = [:]) {
    def params = getPaginationDefaultParams() + input
    return getEntities(getCompanyEntityType(), params)
  }

  LinkedHashMap getCompany(def id) {
    return getEntity(getCompanyEntityType(), id)
  }
  LinkedHashMap createCompany(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    LinkedHashMap params = getCompanyParams(input, additionalParams)
    return createEntity(getCompanyEntityType(), params)
  }

  LinkedHashMap updateCompany(def id, LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    LinkedHashMap params = getCompanyParams(input, additionalParams)
    return updateEntity(getCompanyEntityType(), id, params)
  }

  LinkedHashMap putCompany(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    def companyId = input.companyId
    input.remove('companyId')

    if (companyId) {
      return updateCompany(companyId, input, additionalParams)
    } else {
      return createCompany(input, additionalParams)
    }
  }

  Boolean deleteCompany(def id) {
    return deleteEntity(getCompanyEntityType(), id)
  }
}