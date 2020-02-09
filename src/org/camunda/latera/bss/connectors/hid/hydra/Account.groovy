package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Numeric.toFloatSafe
import static org.camunda.latera.bss.utils.DateTimeUtil.local
import static org.camunda.latera.bss.utils.DateTimeUtil.dayEnd
import static org.camunda.latera.bss.utils.Oracle.encodeDateStr
import static org.camunda.latera.bss.utils.Constants.ACC_TYPE_Personal
import static org.camunda.latera.bss.utils.Constants.OVERDRAFT_Manual
import java.time.temporal.Temporal

trait Account {
  private static String ACCOUNTS_TABLE = 'SI_V_SUBJ_ACCOUNTS'
  private static String ACCOUNTS_MV    = 'SI_MV_SUBJ_ACCOUNTS'

  /**
   * Get accounts table name
   */
  String getAccountsTable() {
    return ACCOUNTS_TABLE
  }
  /**
   * Get accounts quick search material view name
   */
  String getAccountsMV() {
    return ACCOUNTS_MV
  }

  /**
   * Get default account type (personal) ref code
   */
  String getDefaultAccountType() {
    return getRefCode(getDefaultAccountTypeId())
  }

  /**
   * Get default account type (personal) ref Id
   */
  Number getDefaultAccountTypeId() {
    return ACC_TYPE_Personal
  }

  /**
   * Get default overdraft reason (manual) ref code
   */
  String getDefaultOverdraftReason() {
    return getRefCode(getDefaultOverdraftReasonId())
  }

  /**
   * Get default overdraft reason (manual) ref Id
   */
  Number getDefaultOverdraftReasonId() {
    return OVERDRAFT_Manual
  }

  /**
   * Search for accounts by different fields value
   * @param accountId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subjectId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param accountTypeId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. default: see {@link #getDefaultAccountTypeId()}
   * @param bankId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param currencyId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param currency      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param number        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param maxOverdraft  {@link java.math.BigDecimal BigDecimal}, {@link java.math.BigInteger BigInteger}. Optional, {@link LinkedHashMap Map} with WHERE clause or SELECT query
   * @param rem           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param firmId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. default: current firm Id
   * @param limit         {@link Integer}. Optional, default: 0 (unlimited)
   * @param order         {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: [:]
   * @return List[Map] of subject account table rows
   */
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

  /**
   * Search for one account by different fields value
   * @param accountId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subjectId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param accountTypeId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: see {@link #getDefaultAccountTypeId()}
   * @param bankId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param currencyId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param currency      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param number        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param maxOverdraft  {@link java.math.BigDecimal BigDecimal}, {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param rem           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param firmId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. default: current firm Id
   * @return Map with subject account table row
   */
  Map getAccountBy(Map input) {
    return getAccountsBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Get account by id
   * @param accountId {@link java.math.BigInteger BigInteger}
   * @return Map with subject account table row or null
   */
  Map getAccount(def accountId) {
    LinkedHashMap where = [
      n_account_id: accountId
    ]
    return hid.getTableFirst(getAccountsTable(), where: where)
  }

  /**
   * Get accounts by subject id
   * @param subjectId     {@link java.math.BigInteger BigInteger}
   * @param accountTypeId {@link java.math.BigInteger BigInteger}. Optional
   * @return Map with subject account table row or null
   */
  List getSubjectAccounts(
    def subjectId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    return getAccountsBy(subjectId: subjectId, accountTypeId: accountTypeId)
  }

  /**
   * Get accounts by company id
   * @param companyId     {@link java.math.BigInteger BigInteger}
   * @param accountTypeId {@link java.math.BigInteger BigInteger}. Optional
   * @return List[Map] of company account table rows
   */
  List getCompanyAccounts(
    def companyId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    return getSubjectAccounts(companyId, accountTypeId)
  }

  /**
   * Get accounts by person id
   * @param personId      {@link java.math.BigInteger BigInteger}
   * @param accountTypeId {@link java.math.BigInteger BigInteger}. Optional
   * @return List[Map] of person account table rows
   */
  List getPersonAccounts(
    def personId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    return getSubjectAccounts(personId, accountTypeId)
  }

  /**
   * Get accounts by customer id
   * @param customerId    {@link java.math.BigInteger BigInteger}
   * @param accountTypeId {@link java.math.BigInteger BigInteger}. Optional
   * @return List[Map] of customer account table rows
   */
  List getCustomerAccounts(
    def customerId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    return getSubjectAccounts(customerId, accountTypeId)
  }

  /**
   * Get first account by subject id
   * @param subjectId     {@link java.math.BigInteger BigInteger}
   * @param accountTypeId {@link java.math.BigInteger BigInteger}. Optional
   * @return Map with subject account table row or null
   */
  Map getSubjectAccount(
    def subjectId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    return getAccountBy(subjectId: subjectId, accountTypeId: accountTypeId)
  }

  /**
   * Get first account by company id
   * @param companyId     {@link java.math.BigInteger BigInteger}
   * @param accountTypeId {@link java.math.BigInteger BigInteger}. Optional
   * @return Map with company account table row or null
   */
  Map getCompanyAccount(
    def companyId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    return getSubjectAccount(companyId, accountTypeId)
  }

  /**
   * Get first account by person id
   * @param personId      {@link java.math.BigInteger BigInteger}
   * @param accountTypeId {@link java.math.BigInteger BigInteger}. Optional
   * @return Map with person account table row or null
   */
  Map getPersonAccount(
    def personId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    return getSubjectAccount(personId, accountTypeId)
  }

  /**
   * Get first account by customer id
   * @param customerId    {@link java.math.BigInteger BigInteger}
   * @param accountTypeId {@link java.math.BigInteger BigInteger}. Optional
   * @return Map with customer account table row or null
   */
  Map getCustomerAccount(
    def customerId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    return getSubjectAccount(customerId, accountTypeId)
  }

  /**
   * Get account by subject id and other field values
   * @param subjectId {@link java.math.BigInteger BigInteger}
   * @see #getAccountBy(Map)
   * @return Map with subject account table row or null
   */
  Map getSubjectAccountBy(Map input = [:], def subjectId) {
    return getAccountBy(input + [subjectId: subjectId])
  }

  /**
   * Get account by company id and other field values
   * @param subjectId {@link java.math.BigInteger BigInteger}
   * @see #getAccountBy(Map)
   * @return Map with company account table row or null
   */
  Map getCompanyAccountBy(Map input = [:], def companyId) {
    return getSubjectAccountBy(input, companyId)
  }

  /**
   * Get account by person id and other field values
   * @param subjectId {@link java.math.BigInteger BigInteger}
   * @see #getAccountBy(Map)
   * @return Map with person account table row or null
   */
  Map getPersonAccountBy(Map input = [:], def personId) {
    return getSubjectAccountBy(input, personId)
  }

  /**
   * Get account by customer id and other field values
   * @param subjectId {@link java.math.BigInteger BigInteger}
   * @see #getAccountBy(Map)
   * @return Map with customer account table row or null
   */
  Map getCustomerAccountBy(Map input = [:], def customerId) {
    return getSubjectAccountBy(input, customerId)
  }

  /**
   * Get account balance info
   * @param accountId     {@link java.math.BigInteger BigInteger}
   * @param operationDate {@link java.time.Temporal Any date type}. Optional. Optional. default: current datetime
   * @return Map with customer account balance data
   */
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

  /**
   * Get account total balance
   * @param accountId     {@link java.math.BigInteger BigInteger}
   * @param operationDate {@link java.time.Temporal Any date type}. Optional. Default: current datetime
   * @return Double with account total sum
   */
  Double getAccountBalanceTotal(
    def accountId,
    Temporal operationDate = local()
  ) {
    return toFloatSafe(getAccountBalance(accountId, operationDate)?.n_sum_total).doubleValue()
  }

  /**
   * Get account free balance
   * @param accountId     {@link java.math.BigInteger BigInteger}
   * @param operationDate {@link java.time.Temporal Any date type}. Optional. Default: current datetime
   * @return Double with account free sum
   */
  Double getAccountFree(
    def accountId,
    Temporal operationDate = local()
  ) {
    return toFloatSafe(getAccountBalance(accountId, operationDate)?.n_sum_free).doubleValue()
  }

  /**
   * Get account actual charge logs total
   * @param accountId {@link java.math.BigInteger BigInteger}
   * @return Double with total sum of account charge logs
   */
  Double getAccountActualChargeLogsSum(def accountId) {
    return toFloatSafe(hid.queryFirst("""
    SELECT SI_ACCOUNTS_PKG_S.GET_ACTUAL_CHARGE_LOGS_AMOUNT(${accountId})
    FROM   DUAL
  """)?.getAt(0)).doubleValue()
  }

  /**
   * Get account rediodic amounts
   * @param accountId     {@link java.math.BigInteger BigInteger}
   * @param operationDate {@link java.time.Temporal Any date type}. Optional. Default: current datetime
   * @return List[Map] with periodic charges from account
   */
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

  /**
   * Create or update customer account
   * @param customerId           {@link java.math.BigInteger BigInteger}. Optional
   * @param accountId            {@link java.math.BigInteger BigInteger}. Optional
   * @param currencyId           {@link java.math.BigInteger BigInteger}. Optional
   * @param currency             {@link CharSequence String}. Optional
   * @param name                 {@link CharSequence String}. Optional
   * @param code                 {@link CharSequence String}. Optional
   * @param number               {@link CharSequence String}. Optional
   * @param permanentOverdraft   {@link java.math.BigDecimal BigDecimal}, {@link java.math.BigInteger BigInteger}. Optional
   * @param temporalOverdraft    {@link java.math.BigDecimal BigDecimal}, {@link java.math.BigInteger BigInteger}. Optional
   * @param temporalOverdraftEnd {@link java.time.Temporal Any date type}. Optional
   * @param maxOverdraft         {@link java.math.BigDecimal BigDecimal}, {@link java.math.BigInteger BigInteger}. Optional
   * @param rem                  {@link CharSequence String}. Optional
   * @return Map with created customer account (in Oracle API procedure notation)
   */
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

  /**
   * Create customer account
   * @param customerId           {@link java.math.BigInteger BigInteger}
   * @param currencyId           {@link java.math.BigInteger BigInteger}. Optional
   * @param currency             {@link CharSequence String}. Optional
   * @param name                 {@link CharSequence String}. Optional
   * @param code                 {@link CharSequence String}. Optional
   * @param number               {@link CharSequence String}. Optional
   * @param permanentOverdraft   {@link java.math.BigDecimal BigDecimal}, {@link java.math.BigInteger BigInteger}. Optional
   * @param temporalOverdraft    {@link java.math.BigDecimal BigDecimal}, {@link java.math.BigInteger BigInteger}. Optional
   * @param temporalOverdraftEnd {@link java.time.Temporal Any date type}. Optional
   * @param maxOverdraft         {@link java.math.BigDecimal BigDecimal}, {@link java.math.BigInteger BigInteger}. Optional
   * @param rem                  {@link CharSequence String}. Optional
   * @return Map with created customer account (in Oracle API procedure notation)
   */
  Map createCustomerAccount(Map input = [:], def customerId) {
    input.remove('accountId')
    return putCustomerAccount(input + [customerId: customerId])
  }

  /**
   * Update customer account
   * @param accountId            {@link java.math.BigInteger BigInteger}
   * @param name                 {@link CharSequence String}. Optional
   * @param code                 {@link CharSequence String}. Optional
   * @param number               {@link CharSequence String}. Optional
   * @param permanentOverdraft   {@link java.math.BigDecimal BigDecimal}, {@link java.math.BigInteger BigInteger}. Optional
   * @param temporalOverdraft    {@link java.math.BigDecimal BigDecimal}, {@link java.math.BigInteger BigInteger}. Optional
   * @param temporalOverdraftEnd {@link java.time.Temporal Any date type}. Optional
   * @param maxOverdraft         {@link java.math.BigDecimal BigDecimal}, {@link java.math.BigInteger BigInteger}. Optional
   * @param rem                  {@link CharSequence String}. Optional
   * @return Map with updated customer account (in Oracle API procedure notation)
   */
  Map updateCustomerAccount(Map input = [:], def accountId) {
    return putCustomerAccount(input + [accountId: accountId])
  }

  /**
   * Create adjustment for account
   * @param accountId     {@link java.math.BigInteger BigInteger}
   * @param docId         {@link java.math.BigInteger BigInteger}
   * @param goodId        {@link java.math.BigInteger BigInteger}
   * @param equipmentId   {@link java.math.BigInteger BigInteger}. Optional
   * @param sum           {@link java.math.BigDecimal BigDecimal}, {@link java.math.BigInteger BigInteger} (above zero) or null (only it sumWoTax is not null)
   * @param sumWoTax      {@link java.math.BigDecimal BigDecimal}, {@link java.math.BigInteger BigInteger} (above zero) or null(only it sum is not null)
   * @param operationDate {@link java.time.Temporal Any date type}. Optional. Default: current datetime
   * @param firmId        {@link java.math.BigInteger BigInteger}. Optional. Default: current firm Id
   * @return True if adjustment was created successfully, false otherwise
   */
  private Boolean putAdjustment(Map input) {
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

  /**
   * Add adjustment for account
   * @param accountId     {@link java.math.BigInteger BigInteger}
   * @param docId         {@link java.math.BigInteger BigInteger}
   * @param goodId        {@link java.math.BigInteger BigInteger}
   * @param equipmentId   {@link java.math.BigInteger BigInteger}. Optional
   * @param sum           {@link java.math.BigDecimal BigDecimal}, {@link java.math.BigInteger BigInteger} (above zero) or null (only it sumWoTax is not null)
   * @param sumWoTax      {@link java.math.BigDecimal BigDecimal}, {@link java.math.BigInteger BigInteger} (above zero) or null (only it sum is not null)
   * @param operationDate {@link java.time.Temporal Any date type}. Optional. Default: current datetime
   * @param firmId        {@link java.math.BigInteger BigInteger}. Optional. Default: current firm Id
   * @return True if adjustment was created successfully, false otherwise
   */
  Boolean addAdjustment(Map input = [:], def accountId) {
    return putAdjustment(input + [accountId: accountId])
  }

  /**
   * Set permanent overdraft for account
   * @param accountId {@link java.math.BigInteger BigInteger}
   * @param sum       {@link java.math.BigDecimal BigDecimal}, {@link java.math.BigInteger BigInteger} (above zero)
   * @param reasonId  {@link java.math.BigInteger BigInteger}. Optional. Default: manual overdraft
   * @param reason    {@link CharSequence String}. Optional
   * @return True if overdraft was set successfully, false otherwise
   */
  private Boolean putPermanentOverdraft(Map input) {
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

  /**
   * Set permanent overdraft for account
   * @param accountId {@link java.math.BigInteger BigInteger}
   * @param sum       {@link java.math.BigDecimal BigDecimal}, {@link java.math.BigInteger BigInteger} (above zero)
   * @param reasonId  {@link java.math.BigInteger BigInteger}. Optional. Default: manual overdraft
   * @param reason    {@link CharSequence String}. Optional
   * @return True if overdraft was set successfully, false otherwise
   */
  Boolean addPermanentOverdraft(Map input, def accountId) {
    return putPermanentOverdraft(input + [accountId: accountId])
  }

  /**
   * Set permanent overdraft for account
   * @param accountId {@link java.math.BigInteger BigInteger}
   * @param sum       {@link java.math.BigDecimal BigDecimal}, {@link java.math.BigInteger BigInteger} (above zero)
   * @param reasonId  {@link java.math.BigInteger BigInteger}. Optional. Default: manual overdraft
   * @return True if overdraft was set successfully, false otherwise
   */
  Boolean addPermanentOverdraft(
    def accountId,
    Double sum  = 0,
    def reasonId = getDefaultOverdraftReasonId()
  ) {
    return putPermanentOverdraft([
      accountId : accountId,
      sum       : sum,
      reasonId  : reasonId
    ])
  }

  /**
   * Delete permanent overdraft from account
   * @param accountId {@link java.math.BigInteger BigInteger}
   * @return True if overdraft was deleted successfully, false otherwise
   */
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

  /**
   * Set temporal overdraft for account
   * @param accountId {@link java.math.BigInteger BigInteger}
   * @param sum       {@link java.math.BigDecimal BigDecimal}, {@link java.math.BigInteger BigInteger} (above zero)
   * @param endDate   {@link java.time.Temporal Any date type}. Optional. Default: current datetime
   * @param reasonId  {@link java.math.BigInteger BigInteger}. Optional. default: manual overdraft
   * @param reason    {@link CharSequence String}. Optional
   * @return True if overdraft was set successfully, false otherwise
   */
  private Boolean putTemporalOverdraft(Map input) {
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

  /**
   * Set temporal overdraft for account
   * @param accountId {@link java.math.BigInteger BigInteger}
   * @param sum       {@link java.math.BigDecimal BigDecimal}, {@link java.math.BigInteger BigInteger} (above zero)
   * @param endDate   {@link java.time.Temporal Any date type}. Optional. Default: current datetime
   * @param reasonId  {@link java.math.BigInteger BigInteger}. Optional. Default: manual overdraft
   * @param reason    {@link CharSequence String}. Optional
   * @return True if overdraft was set successfully, false otherwise
   */
  Boolean addTemporalOverdraft(Map input, def accountId) {
    return putTemporalOverdraft(input + [accountId: accountId])
  }

  /**
   * Set temporal overdraft for account
   * @param accountId {@link java.math.BigInteger BigInteger}
   * @param sum       {@link java.math.BigDecimal BigDecimal}, {@link java.math.BigInteger BigInteger} (above zero)
   * @param endDate   {@link java.time.Temporal Any date type}. Optional. Default: current datetime
   * @param reasonId  {@link java.math.BigInteger BigInteger}. Optional. Default: manual overdraft
   * @return True if overdraft was set successfully, false otherwise
   */
  Boolean addTemporalOverdraft(
    def accountId,
    Double sum = 0,
    Temporal endDate = dayEnd(),
    def reasonId = getDefaultOverdraftReasonId()
  ) {
    return putTemporalOverdraft([
      accountId : accountId,
      sum       : sum,
      endDate   : endDate,
      reasonId  : reasonId
    ])
  }

  /**
   * Delete temporal overdraft from account
   * @param accountId {@link java.math.BigInteger BigInteger}
   * @param sum       {@link java.math.BigDecimal BigDecimal}, {@link java.math.BigInteger BigInteger} (above zero)
   * @param endDate   {@link java.time.Temporal Any date type}. Optional. Default: current datetime
   * @param reasonId  {@link java.math.BigInteger BigInteger}. Optional. Default: manual overdraft
   * @return True if overdraft was set successfully, false otherwise
   */
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

  /**
   * Try to generate invoices for account
   * @param accountId {@link java.math.BigInteger BigInteger}
   * @param beginDate {@link java.time.Temporal Any date type}. Optional. Default: current datetime
   * @param endDate   {@link java.time.Temporal Any date type}. Optional
   * @return True if customer account was processed successfully, false otherwise
   */
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

  /**
   * Refresh customer accounts quick search material view
   * @see Search#refreshMaterialView(CharSequence,CharSequence)
   */
  Boolean refreshAccounts(CharSequence method = 'C') {
    return refreshMaterialView(getAccountsMV(), method)
  }

  /**
   * Refresh customer accounts quick search material view
   * @see #refreshAccounts(CharSequence)
   */
  Boolean refreshCustomerAccounts(CharSequence method = 'C') {
    return refreshAccounts(method)
  }
}
