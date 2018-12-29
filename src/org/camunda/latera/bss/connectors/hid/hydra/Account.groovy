package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.utils.DateTimeUtil

trait Account {
  private static String ACCOUNTS_TABLE       = 'SI_V_SUBJ_ACCOUNTS'
  private static String DEFAULT_ACCOUNT_TYPE = 'ACC_TYPE_Personal'

  def getAccountsTable() {
    return ACCOUNTS_TABLE
  }

  def getDefaultAccountTypeId() {
    return getRefIdByCode(DEFAULT_ACCOUNT_TYPE)
  }

  List getAccounts(
    def accountId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    LinkedHashMap where = [
      n_account_id: accountId
    ]
    if (accountTypeId) {
      where.n_account_type_id = accountTypeId
    }
    return hid.getTableData(getAccountsTable(), where: where)
  }

  List getSubjectAccounts(
    def subjectId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    LinkedHashMap where = [
      n_subject_id: subjectId
    ]
    if (accountTypeId) {
      where.n_account_type_id = accountTypeId
    }
    return hid.getTableData(getAccountsTable(), where: where)
  }

  LinkedHashMap getAccount(
    def accountId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    return getAccounts(accountId, accountTypeId)?.getAt(0)
  }

  LinkedHashMap getSubjectAccount(
    def subjectId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    return getSubjectAccounts(subjectId, accountTypeId)
  }

  def putCustomerAccount(LinkedHashMap input) {
    def params = mergeParams([
      accountId  :  null,
      subjectId  :  null,
      currencyId :  getDefaultCurrencyId(),
      name       :  null,
      code       :  null,
      number     :  null
    ], input)
    try {
      logger.info("Putting account number ${params.number}, name ${parms.name}, code ${params.code} and currency ${params.currencyId} to customer ${params.subjectId}")

      LinkedHashMap account = hid.execute('SI_ACCOUNTS_PKG.CUSTOMER_ACCOUNT_PUT', [
        num_N_ACCOUNT_ID  : params.accountId,
        num_N_CUSTOMER_ID : params.subjectId,
        num_N_CURRENCY_ID : params.currencyId,
        vch_VC_NAME       : params.name,
        vch_VC_CODE       : params.code,
        vch_VC_ACCOUNT    : params.number
      ])
      logger.info("   Account ${account.num_N_ACCOUNT_ID} was put successfully!")
      return account
    } catch (Exception e){
      logger.error("Error while putting account")
      logger.error(e)
    }
  }

  void processAccount(
    def accountId,
    def beginDate = DateTimeUtil.now(),
    def endDate   = null
  ) {
    try {
      logger.info("Processing account ${accountId}")
      hid.execute('SD_CHARGE_LOGS_CHARGING_PKG.PROCESS_ACCOUNT', [
        num_N_ACCOUNT_ID : accountId,
        dt_D_OPER        : beginDate,
        dt_D_OPER_END    : endDate
      ])
      logger.info("   Account processed successfully!")
    } catch (Exception e){
      logger.error("Error while processing account!")
      logger.error(e)
    }
  }

  void processAccount(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      accountId : null,
      beginDate : DateTimeUtil.now(),
      endDate   : null
    ], input)
    processAccount(params.accountId, params.beginDate, params.endDate)
  }
}