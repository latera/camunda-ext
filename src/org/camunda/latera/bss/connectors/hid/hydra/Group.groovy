package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import static org.camunda.latera.bss.utils.Constants.SUBJ_TYPE_Group

trait Group {
  private static String GROUPS_TABLE = 'SI_V_SUBJ_GROUPS'

  String getGroupsTable() {
    return GROUPS_TABLE
  }

  String getGroupType() {
    return getRefCode(getGroupTypeId())
  }

  Number getGroupTypeId() {
    return SUBJ_TYPE_Group
  }

  Map getGroup(def groupId) {
    LinkedHashMap where = [
      n_subject_id: groupId
    ]
    return hid.getTableFirst(getGroupsTable(), where: where)
  }

  List getGroupsBy(Map input) {
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

  Map getGroupBy(Map input) {
    return getGroupsBy(input + [limit: 1])?.getAt(0)
  }

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