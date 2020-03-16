package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import static org.camunda.latera.bss.utils.Constants.SUBJ_TYPE_Group

/**
  * Subject group specific methods
  */
trait Group {
  private static String GROUPS_TABLE = 'SI_V_SUBJ_GROUPS'

  /**
   * Get groups table name
   */
  String getGroupsTable() {
    return GROUPS_TABLE
  }

  /**
   * Get group subject type ref code
   */
  String getGroupType() {
    return getRefCode(getGroupTypeId())
  }

  /**
   * Get group subject type ref id
   */
  Number getGroupTypeId() {
    return SUBJ_TYPE_Group
  }

  /**
   * Get group by id
   * @param groupId {@link java.math.BigInteger BigInteger}
   * @return Group table row
   */
  Map getGroup(def groupId) {
    LinkedHashMap where = [
      n_subject_id: groupId
    ]
    return hid.getTableFirst(getGroupsTable(), where: where)
  }

  /**
   * Search for groups by different fields value
   * @param groupId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subjectTypeId  {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subjectType    {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param creatorId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param stateId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param state          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param firmId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. Default: current firm id
   * @param limit          {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order          {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: [:]
   * @return Group table rows
   */
  List<Map> getGroupsBy(Map input) {
    LinkedHashMap params = mergeParams([
      groupId       : null,
      subjectTypeId : null,
      creatorId     : null,
      name          : null,
      code          : null,
      firmId        : getFirmId(),
      stateId       : getSubjectStateOnId(),
      limit         : 0
    ], input)
    LinkedHashMap where = [:]

    if (params.groupId) {
      where.n_subject_id = params.groupId
    }
    if (params.subjectTypeId) {
      where.n_grp_subj_type_id = params.subjectTypeId
    }
    if (params.creatorId) {
      where.n_creator_id = params.creatorId
    }
    if (params.name) {
      where.vc_name = params.name
    }
    if (params.code) {
      where.vc_code = params.code
    }
    if (params.groupId) {
      where.n_subj_group_id = params.groupId
    }
    if (params.resellerId) {
      where.n_reseller_id = params.resellerId
    }
    if (params.stateId) {
      where.n_subj_state_id = params.stateId
    }
    return hid.getTableData(getGroupsTable(), where: where, order: params.order, limit: params.limit)
  }

  /**
   * Search for group by different fields value
   * @param groupId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subjectTypeId  {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subjectType    {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param creatorId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param stateId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param state          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param firmId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. Default: current firm id
   * @param order          {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: [:]
   * @return Group table row
   */
  Map getGroupBy(Map input) {
    return getGroupsBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Check if entity or entity type is group
   * @param entityOrEntityType {@link java.math.BigInteger BigInteger} or {@link CharSequence String}. Subject id, subject type ref id or subject type ref code
   * @return True if given value is group, false otherwise
   */
  Boolean isGroup(def entityOrEntityType) {
    if (entityOrEntityType == null) {
      return false
    }

    Number entityIdOrEntityTypeId = toIntSafe(entityOrEntityType)
    if (entityIdOrEntityTypeId != null) {
      return entityIdOrEntityTypeId == getGroupTypeId() || getGroup(entityIdOrEntityTypeId) != null
    } else {
      return entityOrEntityType == getGroupType()
    }
  }
}