package org.camunda.latera.bss.connectors.hid.hydra

trait Account {
  private static String ACCOUNTS_TABLE       = 'SI_V_SUBJ_ACCOUNTS'
  private static String DEFAULT_ACCOUNT_TYPE = 'ACC_TYPE_Personal'

  List getAccounts(
    def accountId,
    def accountTypeId = getRefIdByCode(DEFAULT_ACCOUNT_TYPE)
  ) {
    LinkedHashMap where = [
      n_account_id: accountId
    ]
    if (accountTypeId) {
      where.n_account_type_id = accountTypeId
    }
    return hid.getTableData(ACCOUNTS_TABLE, where: where)
  }

  List getSubjectAccounts(
    def subjectId,
    def accountTypeId = getRefIdByCode(DEFAULT_ACCOUNT_TYPE)
  ) {
    LinkedHashMap where = [
      n_subject_id: subjectId
    ]
    if (accountTypeId) {
      where.n_account_type_id = accountTypeId
    }
    return hid.getTableData(ACCOUNTS_TABLE, where: where)
  }

  LinkedHashMap getAccount(
    def accountId,
    def accountTypeId = getRefIdByCode(DEFAULT_ACCOUNT_TYPE)
  ) {
    return getAccounts(accountId, accountTypeId)?.getAt(0)
  }

  LinkedHashMap getSubjectAccount(
    def subjectId,
    def accountTypeId = getRefIdByCode(DEFAULT_ACCOUNT_TYPE)
  ) {
    return getCustomerAccounts(subjectId, accountTypeId)
  }
}