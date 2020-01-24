package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Numeric.toFloatSafe
import static org.camunda.latera.bss.utils.DateTimeUtil.local
import static org.camunda.latera.bss.utils.DateTimeUtil.dayEnd
import static org.camunda.latera.bss.utils.Oracle.encodeDateStr
import java.time.temporal.Temporal

trait Account {
  private static String ACCOUNTS_TABLE           = 'SI_V_SUBJ_ACCOUNTS'
  private static String ACCOUNTS_MV              = 'SI_MV_SUBJ_ACCOUNTS'
  private static String DEFAULT_ACCOUNT_TYPE     = 'ACC_TYPE_Personal'
  private static String DEFAULT_OVERDRAFT_REASON = 'OVERDRAFT_Manual'

  String getAccountsTable() {
    return ACCOUNTS_TABLE
  }

  String getAccountsMV() {
    return ACCOUNTS_MV
  }

  String getDefaultAccountType() {
    return DEFAULT_ACCOUNT_TYPE
  }

  Number getDefaultAccountTypeId() {
    return getRefIdByCode(getDefaultAccountType())
  }

  String getDefaultOverdraftReason() {
    return DEFAULT_OVERDRAFT_REASON
  }

  Number getDefaultOverdraftReasonId() {
    return getRefIdByCode(getDefaultOverdraftReason())
  }

  List getAccountsBy(Map input) {
    LinkedHashMap params = mergeParams([
      accountId        : null,
      subjectId        : null,
      accountTypeId    : getDefaultAccountTypeId(),
      bankId           : null,
      currencyId       : null,
      code             : null,
      name             : null,
      number           : null,
      maxOverdraft     : null,
      rem              : null,
      firmId           : getFirmId(),
      limit            : 0
    ], input)
    LinkedHashMap where = [:]

    if (params.accountId) {
      where.n_account_id = params.accountId
    }
    if (params.subjectId) {
      where.n_subject_id = params.subjectId
    }
    if (params.accountTypeId) {
      where.n_account_type_id = params.accountTypeId
    }
    if (params.bankId) {
      where.n_bank_id = params.bankId
    }
    if (params.currencyId) {
      where.n_currency_id = params.currencyId
    }
    if (params.code) {
      where.vc_code = params.code
    }
    if (params.name) {
      where.vc_name = params.name
    }
    if (params.number) {
      where.vc_account = params.number
    }
    if (params.maxOverdraft) {
      where.n_max_overdraft = params.maxOverdraft
    }
    if (params.rem) {
      where.vc_rem = params.rem
    }
    if (params.firmId) {
      where.n_firm_id = params.firmId
    }
    return hid.getTableData(getAccountsTable(), where: where, order: params.order, limit: params.limit)
  }

  Map getAccountBy(Map input) {
    return getAccountsBy(input + [limit: 1])?.getAt(0)
  }

  Map getAccount(def accountId) {
    LinkedHashMap where = [
      n_account_id: accountId
    ]
    return hid.getTableFirst(getAccountsTable(), where: where)
  }

  List getSubjectAccounts(
    def subjectId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    return getAccountsBy(subjectId: subjectId, accountTypeId: accountTypeId)
  }

  List getCompanyAccounts(
    def companyId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    return getSubjectAccounts(companyId, accountTypeId)
  }

  List getPersonAccounts(
    def personId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    return getSubjectAccounts(personId, accountTypeId)
  }

  List getCustomerAccounts(
    def customerId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    return getSubjectAccounts(customerId, accountTypeId)
  }

  Map getSubjectAccount(
    def subjectId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    return getAccountBy(subjectId: subjectId, accountTypeId: accountTypeId)
  }

  Map getCompanyAccount(
    def companyId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    return getSubjectAccount(companyId, accountTypeId)
  }

  Map getPersonAccount(
    def personId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    return getSubjectAccount(personId, accountTypeId)
  }

  Map getCustomerAccount(
    def customerId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    return getSubjectAccount(customerId, accountTypeId)
  }

  Map getSubjectAccountBy(Map input = [:], def subjectId) {
    return getAccountBy(input + [subjectId: subjectId])
  }

  Map getCompanyAccountBy(Map input = [:], def companyId) {
    return getSubjectAccountBy(input, companyId)
  }

  Map getPersonAccountBy(Map input = [:], def personId) {
    return getSubjectAccountBy(input, personId)
  }

  Map getCustomerAccountBy(Map input = [:], def customerId) {
    return getSubjectAccountBy(input, customerId)
  }

  Map getAccountBalance(
    def accountId,
    Temporal operationDate = local()
  ) {
    return hid.queryFirst("""
    SELECT
        'n_account_id',       N_ACCOUNT_ID,
        'n_sum_bal',          N_SUM_BAL,
        'n_sum_total',        N_SUM_TOTAL,
        'n_sum_reserved_cur', N_SUM_RESERVED_CUR,
        'n_sum_reserved',     N_SUM_RESERVED,
        'n_sum_overdraft',    N_SUM_OVERDRAFT,
        'n_sum_free',         N_SUM_FREE,
        'd_bal',              D_BAL,
        'd_overdraft_end',    D_OVERDRAFT_END
    FROM
      TABLE(SI_ACCOUNTS_PKG.GET_ACCOUNT_BALANCE_P(
        num_N_ACCOUNT_ID    => ${accountId},
        dt_D_OPER           => ${encodeDateStr(operationDate)}))
  """, true)
  }

  Double getAccountBalanceTotal(
    def accountId,
    Temporal operationDate = local()
  ) {
    return toFloatSafe(getAccountBalance(accountId, operationDate)?.n_sum_total).doubleValue()
  }

  Double getAccountFree(
    def accountId,
    Temporal operationDate = local()
  ) {
    return toFloatSafe(getAccountBalance(accountId, operationDate)?.n_sum_free).doubleValue()
  }

  Double getAccountActualChargeLogsSum(def accountId) {
    return toFloatSafe(hid.queryFirst("""
    SELECT SI_ACCOUNTS_PKG_S.GET_ACTUAL_CHARGE_LOGS_AMOUNT(${accountId})
    FROM   DUAL
  """)?.getAt(0)).doubleValue()
  }

  List getAccountPeriodicAmounts(
    def accountId,
    Temporal operationDate = local()
  ) {
    return hid.queryDatabase("""
    SELECT
        'n_amount',           N_AMOUNT,
        'n_duration_value',   N_DURATION_VALUE,
        'n_duration_unit_id', N_DURATION_UNIT_ID
    FROM
      TABLE(SI_ACCOUNTS_PKG_S.GET_ACCOUNT_PERIODIC_AMOUNTS(
        num_N_ACCOUNT_ID => ${accountId},
        dt_D_OPER        => ${encodeDateStr(operationDate)}))
  """, true)
  }

  private Map putCustomerAccount(Map input) {
    LinkedHashMap params = mergeParams([
      accountId            : null,
      customerId           : null,
      currencyId           : getDefaultCurrencyId(),
      name                 : null,
      code                 : null,
      number               : null,
      permanentOverdraft   : null,
      temporalOverdraft    : null,
      temporalOverdraftEnd : null,
      maxOverdraft         : null,
      rem                  : null
    ], input)
    try {
      logger.info("Putting account with params ${params}")

      LinkedHashMap account = hid.execute('SI_ACCOUNTS_PKG.CUSTOMER_ACCOUNT_PUT', [
        num_N_ACCOUNT_ID          : params.accountId,
        num_N_CUSTOMER_ID         : params.customerId,
        num_N_CURRENCY_ID         : params.currencyId,
        vch_VC_NAME               : params.name,
        vch_VC_CODE               : params.code,
        vch_VC_ACCOUNT            : params.number,
        num_N_PERMANENT_OVERDRAFT : params.permanentOverdraft,
        num_N_TEMPORAL_OVERDRAFT  : params.temporalOverdraft,
        dt_D_TEMP_OVERDRAFT_END   : params.temporalOverdraftEnd,
        num_N_MAX_OVERDRAFT       : params.maxOverdraft,
        vch_VC_REM                : params.rem
      ])
      logger.info("   Account ${account.num_N_ACCOUNT_ID} was put successfully!")
      return account
    } catch (Exception e){
      logger.error("   Error while putting account!")
      logger.error_oracle(e)
      return null
    }
  }

  Map createCustomerAccount(Map input = [:], def customerId) {
    input.remove('accountId')
    return putCustomerAccount(input + [customerId: customerId])
  }

  Map updateCustomerAccount(Map input = [:], def accountId) {
    return putCustomerAccount(input + [accountId: accountId])
  }

  Boolean putAdjustment(Map input) {
    LinkedHashMap params = mergeParams([
      accountId     : null,
      docId         : null,
      goodId        : null,
      equipmentId   : null,
      sum           : null,
      sumWoTax      : null,
      operationDate : null,
      firmId        : getFirmId()
    ], input)
    try {
      logger.info("Putting adjustment with params ${params}")
      LinkedHashMap args = [
        num_N_ACCOUNT_ID  : params.accountId,
        num_N_CONTRACT_ID : params.docId,
        num_N_OBJECT_ID   : params.equipmentId,
        num_N_SERVICE_ID  : params.goodId,
        num_N_SUM         : params.sum,
        num_N_SUM_WO_TAX  : params.sumWoTax,
        dt_D_OPER         : params.operationDate,
        num_N_FIRM_ID     : params.firmId
      ]

      hid.execute('SD_BALANCE_ADJUSTMENTS_PKG.CHARGE_ADJUSTMENT', args)
      logger.info("   Adjustment was put successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while putting adjustment!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean addAdjustment(Map input = [:], def accountId) {
    return putAdjustment(input + [accountId: accountId])
  }

  Boolean putPermanentOverdraft(Map input) {
    LinkedHashMap params = mergeParams([
      accountId : null,
      reasonId  : getDefaultOverdraftReasonId(),
      sum       : 0
    ], input)
    if (params.sum <= 0) {
      logger.info("Trying to add zero sum permanent overdraft - delete it instead")
      return deletePermanentOverdraft(params.accountId)
    }
    try {
      logger.info("Putting permanent overdraft with params ${params}")
      hid.execute('SD_OVERDRAFTS_PKG.SET_PERMANENT_OVERDRAFT', [
        num_N_ACCOUNT_ID      : params.accountId,
        num_N_ISSUE_REASON_ID : params.reasonId,
        num_N_SUM             : params.sum,
      ])
      logger.info("   Permanent overdraft was put successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while putting permanent overdraft!")
      logger.error_oracle(e)
      return false
    }
    return putPermanentOverdraft(params.accountId, params.sum, params.reasonId)
  }

  Boolean putPermanentOverdraft(
    def accountId,
    Double sum = 0,
    def reasonId = getDefaultOverdraftReasonId()
  ) {
    return putPermanentOverdraft(
      accountId : accountId,
      sum       : sum,
      reasonId  : reasonId
    )
  }

  Boolean addPermanentOverdraft(Map input = [:], def accountId) {
    return putPermanentOverdraft(input + [accountId: accountId])
  }

  Boolean deletePermanentOverdraft(def accountId) {
    try {
      logger.info("Deleting permanent overdraft with params ${params}")
      hid.execute('SD_OVERDRAFTS_PKG.UNSET_PERMANENT_OVERDRAFT', [
        num_N_ACCOUNT_ID : accountId
      ])
      logger.info("   Permanent overdraft was deleted successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while deleting permanent overdraft!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean putTemporalOverdraft(Map input) {
    LinkedHashMap params = mergeParams([
      accountId : null,
      sum       : 0,
      endDate   : dayEnd(),
      reasonId  : getDefaultOverdraftReasonId()
    ], input)
    if (params.sum <= 0) {
      logger.info("Trying to add zero sum temporal overdraft - remove it instead")
      return deleteTemporalOverdraft(params.accountId)
    }
    try {
      logger.info("Putting temporal overdraft with params ${params}")
      hid.execute('SD_OVERDRAFTS_PKG.SET_TEMPORAL_OVERDRAFT', [
        num_N_ACCOUNT_ID      : params.accountId,
        num_N_ISSUE_REASON_ID : params.reasonId,
        dt_D_END              : params.endDate,
        num_N_SUM             : params.sum,
      ])
      logger.info("   Temporal overdraft was put successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while putting temporal overdraft!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean addTemporalOverdraft(Map input = [:], def accountId) {
    return putTemporalOverdraft(input + [accountId: accountId])
  }

  Boolean deleteTemporalOverdraft(def accountId) {
    try {
      logger.info("Deleting temporal overdraft with params ${params}")
      hid.execute('SD_OVERDRAFTS_PKG.UNSET_TEMPORAL_OVERDRAFT', [
        num_N_ACCOUNT_ID : accountId
      ])
      logger.info("   Temporal overdraft was deleted successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while deleting temporal overdraft!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean processAccount(Map input = [:], def accountId) {
    LinkedHashMap params = [
      beginDate : local(),
      endDate   : null
    ] + input
    try {
      logger.info("Processing account id ${accountId}")
      hid.execute('SD_CHARGE_LOGS_CHARGING_PKG.PROCESS_ACCOUNT', [
        num_N_ACCOUNT_ID : accountId,
        dt_D_OPER        : params.beginDate,
        dt_D_OPER_END    : params.endDate
      ])
      logger.info("   Account processed successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while processing account!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean refreshAccounts(CharSequence method = 'C') {
    return refreshMaterialView(getAccountsMV(), method)
  }

  Boolean refreshCustomerAccounts(CharSequence method = 'C') {
    return refreshAccounts(method)
  }
}
