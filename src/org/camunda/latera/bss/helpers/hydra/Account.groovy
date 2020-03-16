package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.Numeric.toFloatSafe
import static org.camunda.latera.bss.utils.StringUtil.capitalize
import static org.camunda.latera.bss.utils.StringUtil.isEmpty

/**
  * Account helper methods collection
  */
trait Account {
  /**
   * Get account data by id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*AccountId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*AccountNumber} {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*AccountCurrencyId} {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*AccountCurrency}   {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*AccountBalanceSum} {@link Double}</li>
   *   <li>{@code homsOrderData*AccountFreeSum}    {@link Double}</li>
   * </ul>
   * @param prefix {@link CharSequence String}. Account prefix. Optional. Default: empty string
   */
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

  /**
   * Get customer account data and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *  <li>{@code homsOrderData*CustomerId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *  <li>{@code homsOrderData*AccountId}         {@link java.math.BigInteger BigInteger}</li>
   *  <li>{@code homsOrderData*AccountNumber}     {@link CharSequence String}</li>
   *  <li>{@code homsOrderData*AccountCurrencyId} {@link java.math.BigInteger BigInteger}</li>
   *  <li>{@code homsOrderData*AccountCurrency}   {@link CharSequence String}</li>
   *  <li>{@code homsOrderData*AccountBalanceSum} {@link Double}</li>
   *  <li>{@code homsOrderData*AccountFreeSum}    {@link Double}</li>
   * </ul>
   * @param prefix         {@link CharSequence String}. Account  prefix. Optional. Default: empty string
   * @param customerPrefix {@link CharSequence String}. Customer prefix. Optional. Default: empty string
   */
  void fetchCustomerAccount(Map input = [:]) {
    Map params = [
      customerPrefix : '',
      prefix         : ''
    ] + input

    String customerPrefix = "${capitalize(params.customerPrefix)}Customer"
    String prefix = "${capitalize(params.prefix)}Account"

    def customerId = order."${customerPrefix}Id"
    if (isEmpty(customerId)) {
      return
    }

    Map account    = hydra.getCustomerAccount(customerId)
    order."${prefix}Id" = account?.n_account_id
    fetchAccount(prefix: params.prefix)
  }

  /**
   * Create customer account and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*CustomerId}        {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*AccountNumber}     {@link CharSequence String}. Optional</li>
   *   <li>{@code homsOrderData*AccountCurrencyId} {@link java.math.BigInteger BigInteger}. Optional if 'AccountCurrency' is set</li>
   *   <li>{@code currencyId}                      {@link java.math.BigInteger BigInteger}. Fallback for 'AccountCurrencyId'</li>
   *   <li>{@code homsOrderData*AccountCurrency}   {@link CharSequence String}. Optional if 'AccountCurrencyId' is set</li>
   *   <li>{@code currency}                        {@link CharSequence String}. Fallback for 'AccountCurrency'</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*AccountId}      {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*AccountCreated} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param prefix         {@link CharSequence String}. Account  prefix. Optional. Default: empty string
   * @param customerPrefix {@link CharSequence String}. Customer prefix. Optional. Default: empty string
   * @return True if customer account was created successfully, false otherwise
   */
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