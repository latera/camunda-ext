package org.camunda.latera.bss.connectors.hid.hydra

trait Reseller {
  private static String RESELLERS_TABLE = 'SI_V_RESELLERS'
  private static String RESELLER_TYPE   = 'SUBJ_TYPE_Reseller'

  def getResellerType() {
    return RESELLER_TYPE
  }

  def getResellerTypeId() {
    return getRefIdByCode(getResellerType())
  }

  def getResellersTable() {
    return RESELLERS_TABLE
  }

  LinkedHashMap getReseller(def resellerId = getResellerId()) {
    LinkedHashMap where = [
      n_subject_id: resellerId
    ]
    return hid.getTableFirst(getResellersTable(), where: where)
  }

  List getResellersBy(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      resellerId    : null,
      baseSubjectId : null,
      creatorId     : null,
      currencyId    : null,
      name          : null,
      code          : null,
      firmId        : getFirmId(),
      stateId       : getSubjectStateOnId()
    ], input)
    LinkedHashMap where = [:]

    if (params.resellerId) {
      where.n_reseller_id = params.resellerId
    }
    if (params.baseSubjectId) {
      where.n_base_subject_id = params.baseSubjectId
    }
    if (params.creatorId) {
      where.n_creator_id = params.creatorId
    }
    if (params.currencyId) {
      where.n_currency_id = params.currencyId
    }
    if (params.name) {
      where.vc_name = params.name
    }
    if (params.code) {
      where.vc_code = params.code
    }
    if (params.firmId) {
      where.n_firm_id = params.firmId
    }
    if (params.stateId) {
      where.n_subj_state_id = params.stateId
    }
    return hid.getTableData(getResellersTable(), where: where)
  }

  LinkedHashMap getResellerBy(
    LinkedHashMap input
  ) {
    return getResellersBy(input)?.getAt(0)
  }

  Boolean isReseller(String entityType) {
    return entityType == getResellerType()
  }

  Boolean isReseller(def entityIdOrEntityTypeId) {
    return entityIdOrEntityTypeId == getResellerTypeId() || getReseller(entityIdOrEntityTypeId) != null
  }
}