package org.camunda.latera.bss.connectors.hoper.hydra

trait Customer {
  private static LinkedHashMap CUSTOMER_ENTITY_TYPE = [
    one    : 'customer',
    plural : 'customers'
  ]

  Map getCustomerEntityType(def id = null) {
    return CUSTOMER_ENTITY_TYPE + withParent(getSubjectEntityType()) + withId(id)
  }

  private Map getCustomerDefaultParams() {
    return [
      code          : null,
      baseSubjectId : null,
      groupId       : null,
      groupIds      : null,
      rem           : null,
      firmId        : getFirmId(),
      resellerId    : getResellerId()
    ]
  }

  private Map getCustomerParamsMap(Map params, Map additionalParams = [:]) {
    LinkedHashMap result = [
      vc_code           : params.code,
      n_base_subject_id : params.baseSubjectId,
      n_subj_group_id   : params.groupId,
      group_ids         : params.groupIds,
      vc_rem            : params.rem,
      n_firm_id         : params.firmId,
      t_tags            : params.tags
    ]
    if (hoper.version == 2) {
      result.n_reseller_id = params.resellerId
    }
    if (additionalParams) {
      result.additional_values = params.additionalParams
    }
    return result
  }

  private Map getCustomerParams(Map input, Map additionalParams = [:]) {
    LinkedHashMap params = getCustomerDefaultParams() + input
    LinkedHashMap data   = getCustomerParamsMap(params + additionalParams)
    return prepareParams(data)
  }

  List getCustomers(Map input = [:]) {
    LinkedHashMap params = getPaginationDefaultParams() + input
    return getEntity(getCustomerEntityType(), params)
  }

  Map getCustomer(def id) {
    return getEntity(getCustomerEntityType(), id)
  }

  Map createCustomer(Map input, Map additionalParams = [:]) {
    LinkedHashMap params = getCustomerParams(input, additionalParams)
    return createEntity(getCustomerEntityType(), params)
  }

  Map createCustomer(Map input = [:], def baseSubjectId, Map additionalParams = [:]) {
    createCustomer(input + [baseSubjectId: baseSubjectId], additionalParams)
  }

  Map updateCustomer(Map input = [:], def id, Map additionalParams = [:]) {
    LinkedHashMap params = getCustomerParams(input, additionalParams)
    return updateEntity(getCustomerEntityType(), id, params)
  }

  Boolean deleteCustomer(def id) {
    return deleteEntity(getCustomerEntityType(), id)
  }
}