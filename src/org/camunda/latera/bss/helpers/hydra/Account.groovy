package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.Numeric.toFloatSafe
import static org.camunda.latera.bss.utils.StringUtil.capitalize

trait Account {
  void fetchAccount(Map input = [:]) {
    Map params = [
      prefix : ''
    ] + input

    String prefix = "${capitalize(params.prefix)}Account"

    def accountId = order."${prefix}Id" ?: [is: null]
    Map account   = hydra.getAccount(accountId)
    Map balance   = hydra.getAccountBalance(accountId)
    order."${prefix}Number"     = account?.vc_account
    order."${prefix}CurrencyId" = account?.n_currency_id
    order."${prefix}Currency"   = hydra.getRefCode(account?.n_currency_id)
    order."${prefix}BalanceSum" = toFloatSafe(balance?.n_sum_bal?.replace(',',  '.'), 0.0)
    order."${prefix}FreeSum"    = toFloatSafe(balance?.n_sum_free?.replace(',', '.'), 0.0)
  }

  void fetchCustomerAccount(Map input = [:]) {
    Map params = [
      customerPrefix : '',
      prefix         : ''
    ] + input

    String customerPrefix = "${capitalize(params.customerPrefix)}Customer"
    String prefix = "${capitalize(params.prefix)}Account"

    def customerId = order."${customerPrefix}Id" ?: [is: 'null']
    Map account    = hydra.getCustomerAccount(customerId)
    order."${prefix}Id" = account?.n_account_id
    fetchAccount(prefix: params.prefix)
  }

  Boolean createCustomerAccount(Map input = [:]) {
    Map params = [
      customerPrefix : '',
      prefix         : ''
    ] + input

    String customerPrefix = "${capitalize(params.customerPrefix)}Customer"
    String prefix = "${capitalize(params.prefix)}Account"

    Map account = hydra.createCustomerAccount(
      order."${customerPrefix}Id",
      currencyId : order."${prefix}CurrencyId" ?: execution.getVariable('currencyId'),
      currency   : order."${prefix}Currency"   ?: execution.getVariable('currency'),
      number     : order."${prefix}Number"
    )
    Boolean result = false
    if (account) {
      order."${prefix}Id" = account?.num_N_ACCOUNT_ID
      result = true
    }
    order."${prefix}Created" = result
    return result
  }
}