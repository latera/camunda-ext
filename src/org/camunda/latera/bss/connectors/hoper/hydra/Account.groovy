package org.camunda.latera.bss.connectors.hoper.hydra

trait Account {
  private static LinkedHashMap ACCOUNT_ENTITY_TYPE = [
    one    : 'account',
    plural : 'accounts'
  ]
  private static Integer DEFAULT_CURRENCY_ID = 1044 // 'CURR_Ruble'

  private Map getAccountEntityType(Map parentType, def id = null) {
    return ACCOUNT_ENTITY_TYPE + withParent(parentType) + withId(id)
  }

  private Map getCustomerAccountEntityType(def customerId, def id = null) {
    return getAccountEntityType(getCustomerEntityType(customerId), id)
  }

  Integer getDefaultCurrencyId() {
    return DEFAULT_CURRENCY_ID
  }

  private Map getAccountDefaultParams() {
    return [
      code         : null,
      name         : null,
      number       : null,
      currencyId   : getDefaultCurrencyId(),
      maxOverdraft : null,
      rem          : null
    ]
  }

  private Map getAccountParamsMap(Map params) {
    return [
      vc_code            : params.code,
      vc_name            : params.name,
      vc_account         : params.number,
      n_currency_id      : params.currencyId,
      n_max_credit_limit : params.maxOverdraft,
      vc_rem             : params.rem
    ]
  }

  private Map getAccountParams(Map input) {
    LinkedHashMap params = getAccountDefaultParams() + input
    LinkedHashMap data   = getAccountParamsMap(params)
    return prepareParams(data)
  }

  List getCustomerAccounts(def customerId, Map input = [:]) {
    LinkedHashMap params = getPaginationDefaultParams() + input
    return getEntities(getCustomerAccountEntityType(customerId), params)
  }

  Map getCustomerAccount(def customerId, def accountId) {
    return getEntity(getCustomerAccountEntityType(customerId), accountId)
  }

  Map createCustomerAccount(def customerId, Map input = [:]) {
    LinkedHashMap params = getAccountParams(input)
    return createEntity(getCustomerAccountEntityType(customerId), params)
  }

  Map createCustomerAccount(Map input) {
    def customerId = input.customerId
    input.remove('customerId')
    return createCustomerAccount(customerId, input)
  }

  Map createCustomerAccount(Map input, def customerId) {
    return createCustomerAccount(customerId, input)
  }

  Map updateCustomerAccount(def customerId, def accountId, Map input) {
    LinkedHashMap params = getAccountParams(input)
    return updateEntity(getCustomerAccountEntityType(customerId), accountId, params)
  }

  Map updateCustomerAccount(Map input) {
    def customerId = input.customerId
    input.remove('customerId')
    def accountId  = input.accountId
    input.remove('accountId')
    return updateCustomerAccount(customerId, accountId, input)
  }

  Map updateCustomerAccount(Map input, def customerId, def accountId) {
    return updateCustomerAccount(customerId, accountId, input)
  }

  Map putCustomerAccount(def customerId, Map input) {
    def accountId = input.accountId
    input.remove('accountId')

    if (accountId) {
      return updateCustomerAccount(customerId, accountId, input)
    } else {
      return createCustomerAccount(customerId, input)
    }
  }

  Map putCustomerAccount(Map input) {
    def customerId = input.customerId
    input.remove('customerId')
    return putCustomerAccount(customerId, input)
  }

  Map putCustomerAccount(Map input, def customerId) {
    return putCustomerAccount(customerId, input)
  }

  Boolean deleteCustomerAccount(def customerId, def accountId) {
    return deleteEntity(getCustomerAccountEntityType(customerId), accountId)
  }
}