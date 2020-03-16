package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Constants.SUBJ_TYPE_Reseller

/**
  * Reseller specific methods
  */
trait Reseller {
  private static String RESELLERS_TABLE = 'SI_V_RESELLERS'

  /**
   * Get resellers table name
   */
  String getResellersTable() {
    return RESELLERS_TABLE
  }

  /**
   * Get reseller subject type ref code
   */
  String getResellerType() {
    return getRefCode(getResellerTypeId())
  }

  /**
   * Get reseller subject type ref id
   */
  Number getResellerTypeId() {
    return SUBJ_TYPE_Reseller
  }

  /**
   * Get reseller by id
   * @param resellerId {@link java.math.BigInteger BigInteger}. Optional. Default: current reseller id
   * @return Reseller table row
   */
  Map getReseller(def resellerId = getResellerId()) {
    LinkedHashMap where = [
      n_subject_id: resellerId
    ]
    return hid.getTableFirst(getResellersTable(), where: where)
  }

  /**
   * Search for reseller by different fields value
   * @param resellerId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param baseSubjectId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param creatorId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param currencyId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param currency      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param stateId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param state         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param firmId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. Default: current firm id
   * @param tags          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit         {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order         {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: [:]
   * @return Reseller table rows
   */
  List<Map> getResellersBy(Map input) {
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

  /**
   * Search for reseller by different fields value
   * @param resellerId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param baseSubjectId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param creatorId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param currencyId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param currency      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param stateId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param state         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param firmId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. Default: current firm id
   * @param tags          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param order         {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: [:]
   * @return Reseller table row
   */
  Map getResellerBy(Map input) {
    return getResellersBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Check if entity or entity type is reseller
   * @param entityOrEntityType {@link java.math.BigInteger BigInteger} or {@link CharSequence String}. Subject id, subject type ref id or subject type ref code
   * @return True if given value is reseller, false otherwise
   */
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