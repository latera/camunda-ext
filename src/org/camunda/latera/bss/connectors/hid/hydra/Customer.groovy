package org.camunda.latera.bss.connectors.hid.hydra

trait Customer {
  private static String CUSTOMERS_TABLE = 'SI_V_USERS'
  private static String CUSTOMER_TYPE   = 'SUBJ_TYPE_User'

  def getCustomersTable() {
    return CUSTOMERS_TABLE
  }

  def getCustomerType() {
    return CUSTOMER_TYPE
  }

  def getCustomerTypeId() {
    return getRefIdByCode(getCustomerType())
  }

  LinkedHashMap getCustomer(def subjectId) {
    LinkedHashMap where = [
      n_subject_id: subjectId
    ]
    return hid.getTableFirst(getCustomersTable(), where: where)
  }

  Boolean isCustomer(String subjectType) {
    return subjectType == getCustomerType()
  }

  Boolean isCustomer(def subjectIdOrSubjectTypeId) {
    return subjectIdOrSubjectTypeId == getCustomerTypeId() || getCustomer(subjectIdOrSubjectTypeId) != null
  }

  LinkedHashMap putCustomer(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      subjectId     : null,
      baseSubjectId : null,
      groupId       : null,
      code          : null,
      rem           : null,
      firmId        : getFirmId(),
      resellerId    : getResellerId(),
      stateId       : getSubjectStateOnId()
    ], input)
    try {
      logger.info("Putting customer with code ${params.code} to base subject ${params.baseSubjectId} with main group ${params.groupId} and state ${params.stateId} in firm ${params.firmId} reseller ${params.resellerId}")
      LinkedHashMap args = [
        num_N_SUBJECT_ID       : params.subjectId,
        num_N_FIRM_ID          : params.firmId,
        num_N_BASE_SUBJECT_ID  : params.baseSubjectId,
        num_N_SUBJ_STATE_ID    : params.stateId,
        num_N_SUBJ_GROUP_ID    : params.groupId,
        vch_VC_CODE            : params.code,
        vch_VC_REM             : params.rem
      ]
      if (params.resellerId) {
        args.num_N_RESELLER_ID = resellerId
      }
      LinkedHashMap customer = hid.execute('SI_USERS_PKG.SI_USERS_PUT', args)
      logger.info("   Customer ${customer.num_N_SUBJECT_ID} was put successfully!")
      return customer
    } catch (Exception e){
      logger.error("Error while putting customer!")
      logger.error(e)
      return null
    }
  }
}