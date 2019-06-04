package org.camunda.latera.bss.connectors.hoper.hydra

trait Account {
  private static LinkedHashMap ACCOUNT_ENTITY_TYPE = [
    one    : 'account',
    plural : 'accounts'
  ]
  private static Integer DEFAULT_CURRENCY_ID = 1044 // 'CURR_Ruble'

  def getAccountEntityType(def parentType, def id = null) {
    return ACCOUNT_ENTITY_TYPE + withParent(parentType) + withId(id)
  }

  def getCustomerAccountEntityType(def customerId, def id = null) {
    return getAccountEntityType(getCustomerEntityType(customerId), id)
  }

  def getDefaultCurrencyId() {
    return DEFAULT_CURRENCY_ID
  }

  LinkedHashMap getAccountDefaultParams() {
    return [
      code         : null,
      name         : null,
      number       : null,
      currencyId   : getDefaultCurrencyId(),
      maxOverdraft : null,
      rem          : null
    ]
  }

  LinkedHashMap getAccountParamsMap(LinkedHashMap params) {
    return [
      vc_code            : params.code,
      vc_name            : params.name,
      vc_account         : params.number,
      n_currency_id      : params.currencyId,
      n_max_credit_limit : params.maxOverdraft,
      vc_rem             : params.rem
    ]
  }

  LinkedHashMap getAccountParams(LinkedHashMap input) {
    def params = getAccountDefaultParams() + input
    def data   = getAccountParamsMap(params)
    return prepareParams(data)
  }

  List getCustomerAccounts(def customerId, LinkedHashMap input = [:]) {
    def params = getPaginationDefaultParams() + input
    return getEntities(getCustomerAccountEntityType(customerId), params)
  }

  LinkedHashMap getCustomerAccount(def customerId, def accountId) {
    return getEntity(getCustomerAccountEntityType(customerId), accountId)
  }

  LinkedHashMap createCustomerAccount(def customerId, LinkedHashMap input = [:]) {
    LinkedHashMap params = getAccountParams(input)
    return createEntity(getCustomerAccountEntityType(customerId), params)
  }

  LinkedHashMap updateCustomerAccount(def customerId, def accountId, LinkedHashMap input) {
    LinkedHashMap params = getAccountParams(input)
    return updateEntity(getCustomerAccountEntityType(customerId), accountId, params)
  }

  LinkedHashMap putCustomerAccount(def customerId, LinkedHashMap input) {
    def accountId = input.accountId
    input.remove('accountId')

    if (accountId) {
      return updateCustomerAccount(customerId, accountId, input)
    } else {
      return createCustomerAccount(customerId, input)
    }
  }

  Boolean deleteCustomerAccount(def customerId, def accountId) {
    return deleteEntity(getCustomerAccountEntityType(customerId), accountId)
  }
}