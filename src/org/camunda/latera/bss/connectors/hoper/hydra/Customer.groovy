package org.camunda.latera.bss.connectors.hoper.hydra

trait Customer {
  private static LinkedHashMap CUSTOMER_ENTITY_TYPE = [
    one    : 'customer',
    plural : 'customers'
  ]

  def getCustomerEntityType(def id = null) {
    return CUSTOMER_ENTITY_TYPE + withParent(getSubjectEntityType()) + withId(id)
  }

  LinkedHashMap getCustomerDefaultParams() {
    return [
      code          : null,
      baseSubjectId : null,
      groupId       : null,
      groupIds      : null,
      rem           : null,
      firmId        : getFirmId()
    ]
  }

  LinkedHashMap getCustomerParamsMap(LinkedHashMap params, LinkedHashMap additionalParams = [:]) {
    def result = [
      vc_code           : params.code,
      n_base_subject_id : params.baseSubjectId,
      n_subj_group_id   : params.groupId,
      group_ids         : params.groupIds,
      vc_rem            : params.rem,
      n_firm_id         : params.firmId,
      t_tags            : params.tags
    ]
    if (additionalParams) {
      result.additional_values = params.additionalParams
    }
    return result
  }

  LinkedHashMap getCustomerParams(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    def params = getCustomerDefaultParams() + input
    def data   = getCustomerParamsMap(params + additionalParams)
    return nvlParams(data)
  }

  List getCustomers(LinkedHashMap input = [:]) {
    def params = getPaginationDefaultParams() + input
    return getEntity(getCustomerEntityType(), params)
  }

  LinkedHashMap getCustomer(def id) {
    return getEntity(getCustomerEntityType(), id)
  }

  LinkedHashMap createCustomer(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    LinkedHashMap params = getCustomerParams(input, additionalParams)
    return createEntity(getCustomerEntityType(), params)
  }

  LinkedHashMap updateCustomer(def id, LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    LinkedHashMap params = getCustomerParams(input, additionalParams)
    return updateEntity(getCustomerEntityType(), id, params)
  }

  LinkedHashMap putCustomer(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    def customerId = input.customerId
    input.remove('customerId')

    if (customerId) {
      return updateCustomer(customerId, input, additionalParams)
    } else {
      return createCustomer(input, additionalParams)
    }
  }

  Boolean deleteCustomer(def id) {
    return deleteEntity(getCustomerEntityType(), id)
  }
}