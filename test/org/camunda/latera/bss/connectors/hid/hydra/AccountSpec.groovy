package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.connectors.HID
import org.camunda.latera.bss.connectors.hid.Hydra
import org.camunda.latera.bss.utils.DateTimeUtil
import spock.lang.*

class AccountSpec extends Specification {
  @Shared someValue = 123
  @Shared otherValue = 345
  @Shared def hid
  @Shared def now
  @Shared def nowString

  def setup() {
    hid = Mock(HID)
    nowString = '01.01.2020 00:00:00'
    now = DateTimeUtil.parseDateTimeAny(nowString)
  }

  def "#getAccountsBy"() {
    given:
      Hydra hydra = new Hydra(hid)
    when: 'accountId is passed'
      hydra.getAccountsBy(accountId: someValue)
    then: 'value is passed as n_account_id column'
      1 * hid.getTableData({it.where.n_account_id == someValue}, _)
    when: 'subjectId is passed'
      hydra.getAccountsBy(subjectId: someValue)
    then: 'value is passed as n_subject_id column'
      1 * hid.getTableData({it.where.n_subject_id == someValue}, _)
    when: 'accountTypeId is passed'
      hydra.getAccountsBy(accountTypeId: someValue)
    then: 'value is passed as n_account_type_id column'
      1 * hid.getTableData({it.where.n_account_type_id == someValue}, _)
    when: 'bankId is passed'
      hydra.getAccountsBy(bankId: someValue)
    then: 'value is passed as n_bank_id column'
      1 * hid.getTableData({it.where.n_bank_id == someValue}, _)
    when: 'currencyId is passed'
      hydra.getAccountsBy(currencyId: someValue)
    then: 'value is passed as n_currency_id column'
      1 * hid.getTableData({it.where.n_currency_id == someValue}, _)
    when: 'code is passed'
      hydra.getAccountsBy(code: someValue)
    then: 'value is passed as vc_code column'
      1 * hid.getTableData({it.where.vc_code == someValue}, _)
    when: 'name is passed'
      hydra.getAccountsBy(name: someValue)
    then: 'value is passed as vc_name column'
      1 * hid.getTableData({it.where.vc_name == someValue}, _)
    when: 'number is passed'
      hydra.getAccountsBy(number: someValue)
    then: 'value is passed as vc_account column'
      1 * hid.getTableData({it.where.vc_account == someValue}, _)
    when: 'maxOverdraft is passed'
      hydra.getAccountsBy(maxOverdraft: someValue)
    then: 'value is passed as n_max_overdraft column'
      1 * hid.getTableData({it.where.n_max_overdraft == someValue}, _)
    when: 'rem is passed'
      hydra.getAccountsBy(rem: someValue)
    then: 'value is passed as vc_rem column'
      1 * hid.getTableData({it.where.vc_rem == someValue}, _)
    when: 'firmId is passed'
      hydra.getAccountsBy(firmId: someValue)
    then: 'value is passed as n_firm_id column'
      1 * hid.getTableData({it.where.n_firm_id == someValue}, _)
    when: 'firmId is not passed'
      hydra.getAccountsBy([:])
    then: 'default value is passed as n_firm_id column'
      1 * hid.getTableData({it.where.n_firm_id == hydra.firmId}, _)
    when: 'limit is passed'
      hydra.getAccountsBy(limit: someValue)
    then: 'value is passed as limit argument'
      1 * hid.getTableData({it.limit == someValue}, _)
    when: 'limit is not passed'
      hydra.getAccountsBy([:])
    then: 'default value is passed as limit argument'
      1 * hid.getTableData({it.limit == 0}, _)
    when: 'order is passed'
      hydra.getAccountsBy(order: someValue)
    then: 'value is passed as order argument'
      1 * hid.getTableData({it.order == someValue}, _)
    when: 'order is not passed'
      hydra.getAccountsBy([:])
    then: 'default value is passed as order argument'
      1 * hid.getTableData({it.order == null}, _)
  }

  def "#getAccountBy"() {
    given:
      Hydra hydra = new Hydra(hid)
    when: 'accountId is passed'
      hydra.getAccountBy(accountId: someValue)
    then: 'value is passed as n_account_id column'
      1 * hid.getTableData({it.where.n_account_id == someValue}, _)
    when: 'subjectId is passed'
      hydra.getAccountBy(subjectId: someValue)
    then: 'value is passed as n_subject_id column'
      1 * hid.getTableData({it.where.n_subject_id == someValue}, _)
    when: 'accountTypeId is passed'
      hydra.getAccountBy(accountTypeId: someValue)
    then: 'value is passed as n_account_type_id column'
      1 * hid.getTableData({it.where.n_account_type_id == someValue}, _)
    when: 'bankId is passed'
      hydra.getAccountBy(bankId: someValue)
    then: 'value is passed as n_bank_id column'
      1 * hid.getTableData({it.where.n_bank_id == someValue}, _)
    when: 'currencyId is passed'
      hydra.getAccountBy(currencyId: someValue)
    then: 'value is passed as n_currency_id column'
      1 * hid.getTableData({it.where.n_currency_id == someValue}, _)
    when: 'code is passed'
      hydra.getAccountBy(code: someValue)
    then: 'value is passed as vc_code column'
      1 * hid.getTableData({it.where.vc_code == someValue}, _)
    when: 'name is passed'
      hydra.getAccountBy(name: someValue)
    then: 'value is passed as vc_name column'
      1 * hid.getTableData({it.where.vc_name == someValue}, _)
    when: 'number is passed'
      hydra.getAccountBy(number: someValue)
    then: 'value is passed as vc_account column'
      1 * hid.getTableData({it.where.vc_account == someValue}, _)
    when: 'maxOverdraft is passed'
      hydra.getAccountBy(maxOverdraft: someValue)
    then: 'value is passed as n_max_overdraft column'
      1 * hid.getTableData({it.where.n_max_overdraft == someValue}, _)
    when: 'rem is passed'
      hydra.getAccountBy(rem: someValue)
    then: 'value is passed as vc_rem column'
      1 * hid.getTableData({it.where.vc_rem == someValue}, _)
    when: 'firmId is passed'
      hydra.getAccountBy(firmId: someValue)
    then: 'value is passed as n_firm_id column'
      1 * hid.getTableData({it.where.n_firm_id == someValue}, _)
    when: 'firmId is not passed'
      hydra.getAccountBy([:])
    then: 'default value is passed as n_firm_id column'
      1 * hid.getTableData({it.where.n_firm_id == hydra.firmId}, _)
    when: 'limit cannot be not passed'
      hydra.getAccountBy([:])
    then: '1 is passed as limit argument'
      1 * hid.getTableData({it.limit == 1}, _)
    when: 'order is passed'
      hydra.getAccountBy(order: someValue)
    then: 'value is passed as order argument'
      1 * hid.getTableData({it.order == someValue}, _)
    when: 'order is not passed'
      hydra.getAccountBy([:])
    then: 'default value is passed as order argument'
      1 * hid.getTableData({it.order == null}, _)
  }

  def "#getAccount"() {
    given:
      Hydra hydra = new Hydra(hid)
    when: 'accountId is passed'
      hydra.getAccount(someValue)
    then: 'value is passed as n_account_id column'
      1 * hid.getTableFirst({it.where.n_account_id == someValue}, _)
  }

  def "#getSubjectAccounts"() {
    given:
      Hydra hydra = new Hydra(hid)
    when: 'subjectId is passed'
      hydra.getSubjectAccounts(someValue)
    then: 'value is passed as n_subject_id column'
      1 * hid.getTableData({it.where.n_subject_id == someValue}, _)
    when: 'accountTypeId is passed'
      hydra.getSubjectAccounts(someValue, otherValue)
    then: 'value is passed as n_account_type_id column'
      1 * hid.getTableData({it.where.n_account_type_id == otherValue}, _)
    when: 'accountTypeId is not passed'
      hydra.getSubjectAccounts(someValue)
    then: 'default value is passed as n_account_type_id column'
      1 * hid.getTableData({it.where.n_account_type_id == hydra.getDefaultAccountTypeId()}, _)
  }

  def "#getCompanyAccounts"() {
    given:
      Hydra hydra = new Hydra(hid)
    when: 'subjectId is passed'
      hydra.getCompanyAccounts(someValue)
    then: 'value is passed as n_subject_id column'
      1 * hid.getTableData({it.where.n_subject_id == someValue}, _)
    when: 'accountTypeId is passed'
      hydra.getCompanyAccounts(someValue, otherValue)
    then: 'value is passed as n_account_type_id column'
      1 * hid.getTableData({it.where.n_account_type_id == otherValue}, _)
    when: 'accountTypeId is not passed'
      hydra.getCompanyAccounts(someValue)
    then: 'default value is passed as n_account_type_id column'
      1 * hid.getTableData({it.where.n_account_type_id == hydra.getBaseSubjectAccountTypeId()}, _)
  }

  def "#getPersonAccounts"() {
    given:
      Hydra hydra = new Hydra(hid)
    when: 'subjectId is passed'
      hydra.getPersonAccounts(someValue)
    then: 'value is passed as n_subject_id column'
      1 * hid.getTableData({it.where.n_subject_id == someValue}, _)
    when: 'accountTypeId is passed'
      hydra.getPersonAccounts(someValue, otherValue)
    then: 'value is passed as n_account_type_id column'
      1 * hid.getTableData({it.where.n_account_type_id == otherValue}, _)
    when: 'accountTypeId is not passed'
      hydra.getPersonAccounts(someValue)
    then: 'default value is passed as n_account_type_id column'
      1 * hid.getTableData({it.where.n_account_type_id == hydra.getBaseSubjectAccountTypeId()}, _)
  }

  def "#getCustomerAccounts"() {
    given:
      Hydra hydra = new Hydra(hid)
    when: 'subjectId is passed'
      hydra.getCustomerAccounts(someValue)
    then: 'value is passed as n_subject_id column'
      1 * hid.getTableData({it.where.n_subject_id == someValue}, _)
    when: 'accountTypeId is passed'
      hydra.getCustomerAccounts(someValue, otherValue)
    then: 'value is passed as n_account_type_id column'
      1 * hid.getTableData({it.where.n_account_type_id == otherValue}, _)
    when: 'accountTypeId is not passed'
      hydra.getCustomerAccounts(someValue)
    then: 'default value is passed as n_account_type_id column'
      1 * hid.getTableData({it.where.n_account_type_id == hydra.getCustomerAccountTypeId()}, _)
  }

  def "#getCompanyAccount"() {
    given:
      Hydra hydra = new Hydra(hid)
    when: 'subjectId is passed'
      hydra.getCompanyAccount(someValue)
    then: 'value is passed as n_subject_id column'
      1 * hid.getTableData({it.where.n_subject_id == someValue}, _)
    when: 'accountTypeId is passed'
      hydra.getCompanyAccount(someValue, otherValue)
    then: 'value is passed as n_account_type_id column'
      1 * hid.getTableData({it.where.n_account_type_id == otherValue}, _)
    when: 'accountTypeId is not passed'
      hydra.getCompanyAccount(someValue)
    then: 'default value is passed as n_account_type_id column'
      1 * hid.getTableData({it.where.n_account_type_id == hydra.getBaseSubjectAccountTypeId()}, _)
  }

  def "#getPersonAccount"() {
    given:
      Hydra hydra = new Hydra(hid)
    when: 'subjectId is passed'
      hydra.getPersonAccount(someValue)
    then: 'value is passed as n_subject_id column'
      1 * hid.getTableData({it.where.n_subject_id == someValue}, _)
    when: 'accountTypeId is passed'
      hydra.getPersonAccount(someValue, otherValue)
    then: 'value is passed as n_account_type_id column'
      1 * hid.getTableData({it.where.n_account_type_id == otherValue}, _)
    when: 'accountTypeId is not passed'
      hydra.getPersonAccount(someValue)
    then: 'default value is passed as n_account_type_id column'
      1 * hid.getTableData({it.where.n_account_type_id == hydra.getBaseSubjectAccountTypeId()}, _)
  }

  def "#getCustomerAccount"() {
    given:
      Hydra hydra = new Hydra(hid)
    when: 'subjectId is passed'
      hydra.getCustomerAccount(someValue)
    then: 'value is passed as n_subject_id column'
      1 * hid.getTableData({it.where.n_subject_id == someValue}, _)
    when: 'accountTypeId is passed'
      hydra.getCustomerAccount(someValue, otherValue)
    then: 'value is passed as n_account_type_id column'
      1 * hid.getTableData({it.where.n_account_type_id == otherValue}, _)
    when: 'accountTypeId is not passed'
      hydra.getCustomerAccount(someValue)
    then: 'default value is passed as n_account_type_id column'
      1 * hid.getTableData({it.where.n_account_type_id == hydra.getCustomerAccountTypeId()}, _)
  }

  def "#getAccountBalance"() {
    given:
      Hydra hydra = new Hydra(hid)
    when: 'method is called'
      hydra.getAccountBalance(someValue)
    then: 'SI_ACCOUNTS_PKG.GET_ACCOUNT_BALANCE_P is called'
      1 * hid.queryFirst({it.contains("SI_ACCOUNTS_PKG.GET_ACCOUNT_BALANCE_P")}, _)
    when:  'accountTypeId is passed'
      hydra.getAccountBalance(someValue)
    then: 'accountId is passed as num_N_ACCOUNT_ID argument'
      1 * hid.queryFirst({it.replaceAll(/\n/, '').replaceAll(/\s+/, ' ').contains("num_N_ACCOUNT_ID => ${someValue}")}, _)
    when:  'operationDate is passed'
      hydra.getAccountBalance(someValue, now)
    then: 'value is passed as dt_D_OPER argument'
      1 * hid.queryFirst({it.replaceAll(/\n/, '').replaceAll(/\s+/, ' ').contains("dt_D_OPER => TO_DATE('${nowString}'")}, _)
  }

  def "#getAccountBalanceTotal"() {
    given:
      def n_sum_total = 0.01
      LinkedHashMap response = [
          n_account_id       : 123,
          n_sum_bal          : 1.23,
          n_sum_total        : n_sum_total,
          n_sum_reserved_cur : 0.12,
          n_sum_reserved     : 0.12,
          n_sum_overdraft    : 1.00,
          n_sum_free         : 0.01,
          d_bal              : now,
          d_overdraft_end    : now
        ]
      Hydra hydra = new Hydra(hid)
    when: 'method is called'
      def result = hydra.getAccountBalanceTotal(someValue)
    then:
      hid.queryFirst({it.contains('SI_ACCOUNTS_PKG.GET_ACCOUNT_BALANCE_P')}, _) >> response
    and: 'n_sum_total is returned'
      result == n_sum_total
  }

  def "#getAccountFree"() {
    given:
      def n_sum_free = 0.01
      LinkedHashMap response = [
          n_account_id       : 123,
          n_sum_bal          : 1.23,
          n_sum_total        : 1.23,
          n_sum_reserved_cur : 0.12,
          n_sum_reserved     : 0.12,
          n_sum_overdraft    : 1.00,
          n_sum_free         : n_sum_free,
          d_bal              : now,
          d_overdraft_end    : now
        ]
      Hydra hydra = new Hydra(hid)
    when: 'method is called'
      def result = hydra.getAccountFree(someValue)
    then:
      hid.queryFirst({it.contains('SI_ACCOUNTS_PKG.GET_ACCOUNT_BALANCE_P')}, _) >> response
    and: 'n_sum_free is returned'
      result == n_sum_free
  }

  def "#getAccountActualChargeLogsSum"() {
    given:
      def n_charge_logs_sum = 123.00
      List response = [n_charge_logs_sum]
      Hydra hydra = new Hydra(hid)
    when: 'method is called'
      hydra.getAccountActualChargeLogsSum(someValue)
    then: 'SI_ACCOUNTS_PKG_S.GET_ACTUAL_CHARGE_LOGS_AMOUNT is called'
      1 * hid.queryFirst({it.contains("SI_ACCOUNTS_PKG_S.GET_ACTUAL_CHARGE_LOGS_AMOUNT")})
    when: 'accountId is passed'
      def result = hydra.getAccountActualChargeLogsSum(someValue)
    then: 'value is passed as first argument'
      1 * hid.queryFirst({it.replaceAll(/\n/, '').replaceAll(/\s+/, ' ').contains("(${someValue})")}) >> response
    and: 'sum is returned'
      result == n_charge_logs_sum
  }

  def "#getAccountPeriodicAmounts"() {
    given:
      Hydra hydra = new Hydra(hid)
    when: 'method is called'
      hydra.getAccountPeriodicAmounts(someValue)
    then: 'SI_ACCOUNTS_PKG_S.GET_ACCOUNT_PERIODIC_AMOUNTS is called'
      1 * hid.queryDatabase({it.contains("SI_ACCOUNTS_PKG_S.GET_ACCOUNT_PERIODIC_AMOUNTS")}, _)
    when: 'accountId is passed'
      hydra.getAccountPeriodicAmounts(someValue)
    then: 'value is passed as num_N_ACCOUNT_ID argument'
      1 * hid.queryDatabase({it.replaceAll(/\n/, '').replaceAll(/\s+/, ' ').contains("num_N_ACCOUNT_ID => ${someValue}")}, _)
    when: 'operationDate is passed'
      hydra.getAccountPeriodicAmounts(someValue, now)
    then: 'value is passed as dt_D_OPER argument'
      1 * hid.queryDatabase({it.replaceAll(/\n/, '').replaceAll(/\s+/, ' ').contains("dt_D_OPER => TO_DATE('${nowString}'")}, _)
  }
}