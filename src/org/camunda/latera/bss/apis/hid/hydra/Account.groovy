package org.camunda.latera.bss.apis.hid.hydra

trait Account {
  static accountsTable = 'SI_V_SUBJ_ACCOUNTS'
  static defaultAccountType = 'ACC_TYPE_Personal'

  List getAccounts(
    def accountId,
    def accountTypeId = this.getRefIdByCode(this.defaultAccountType)
  ) {
    LinkedHashMap where = [
      n_account_id: accountId
    ]
    if (accountTypeId) {
      where.n_account_type_id = accountTypeId
    }
    return this.hid.getTableData(this.accountsTable, where: where)
  }

  List getSubjectAccounts(
    def subjectId,
    def accountTypeId = this.getRefIdByCode(this.defaultAccountType)
  ) {
    LinkedHashMap where = [
      n_subject_id: subjectId
    ]
    if (accountTypeId) {
      where.n_account_type_id = accountTypeId
    }
    return this.hid.getTableData(this.accountsTable, where: where)
  }

  LinkedHashMap getAccount(
    def accountId,
    def accountTypeId = this.getRefIdByCode(this.defaultAccountType)
  ) {
    return this.getAccounts(accountId, accountTypeId)?.getAt(0)
  }

  LinkedHashMap getSubjectAccount(
    def subjectId,
    def accountTypeId = this.getRefIdByCode(this.defaultAccountType)
  ) {
    return this.getCustomerAccounts(subjectId, accountTypeId)
  }
}