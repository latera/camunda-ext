package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Constants.SUBJ_TYPE_Reseller

trait Reseller {
  private static String RESELLERS_TABLE = 'SI_V_RESELLERS'

  String getResellerType() {
    return getRefCode(getResellerTypeId())
  }

  Number getResellerTypeId() {
    return SUBJ_TYPE_Reseller
  }

  String getResellersTable() {
    return RESELLERS_TABLE
  }

  Map getReseller(def resellerId = getResellerId()) {
    LinkedHashMap where = [
      n_subject_id: resellerId
    ]
    return hid.getTableFirst(getResellersTable(), where: where)
  }

  List getResellersBy(Map input) {
    LinkedHashMap params = mergeParams([
      resellerId    : null,
      baseSubjectId : null,
      creatorId     : null,
      currencyId    : null,
      name          : null,
      code          : null,
      firmId        : getFirmId(),
      stateId       : getSubjectStateOnId(),
      limit         : 0
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
    if (params.tags) {
      where += prepareEntityTagQuery('N_RESELLER_ID', params.tags)
    }
    return hid.getTableData(getResellersTable(), where: where, order: params.order, limit: params.limit)
  }

  Map getResellerBy(Map input) {
    return getResellersBy(input + [limit: 1])?.getAt(0)
  }

  Boolean isReseller(def entityOrEntityType) {
    if (entityOrEntityType == null) {
      return false
    }

    Number entityIdOrEntityTypeId = toIntSafe(entityOrEntityType)
    if (entityIdOrEntityTypeId != null) {
      return entityIdOrEntityTypeId == getResellerTypeId() || getReseller(entityIdOrEntityTypeId) != null
    } else {
      return entityType == getResellerType()
    }
  }
}