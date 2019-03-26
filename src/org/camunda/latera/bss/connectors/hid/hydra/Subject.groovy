package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.utils.Oracle
import org.camunda.latera.bss.utils.DateTimeUtil

trait Subject {
  private static String SUBJECTS_TABLE                = 'SI_V_SUBJECTS'
  private static String SUBJECT_ADD_PARAMS_TABLE      = 'SI_V_SUBJ_VALUES'
  private static String SUBJECT_ADD_PARAM_TYPES_TABLE = 'SI_V_SUBJ_VALUES_TYPE'
  private static String SUBJECT_GROUPS_TABLE          = 'SI_V_SUBJECT_BIND_GROUPS'
  private static String SUBJECT_STATE_ON              = 'SUBJ_STATE_On'
  private static String SUBJECT_STATE_LOCKED          = 'SUBJ_STATE_Locked'
  private static String SUBJECT_STATE_SUSPENDED       = 'SUBJ_STATE_ManuallySuspended'
  private static String SUBJECT_STATE_DISABLED        = 'SUBJ_STATE_Disabled'

  def getSubjectsTable() {
    return SUBJECTS_TABLE
  }

  def getSubjectAddParamsTable() {
    return SUBJECT_ADD_PARAMS_TABLE
  }

  def getSubjectAddParamTypesTable() {
    return SUBJECT_ADD_PARAM_TYPES_TABLE
  }

  def getSubjectGroupsTable() {
    return SUBJECT_GROUPS_TABLE
  }

  def getSubjectStateOn() {
    return SUBJECT_STATE_ON
  }

  def getSubjectStateOnId() {
    return getRefIdByCode(getSubjectStateOn())
  }

  def getSubjectStateLocked() {
    return SUBJECT_STATE_LOCKED
  }

  def getSubjectStateLockedId() {
    return getRefIdByCode(getSubjectStateLocked())
  }

  def getSubjectStateSuspended() {
    return SUBJECT_STATE_SUSPENDED
  }

  def getSubjectStateSuspendedId() {
    return getRefIdByCode(getSubjectStateSuspended())
  }

  def getSubjectStateDisabled() {
    return SUBJECT_STATE_DISABLED
  }

  def getSubjectStateDisabledId() {
    return getRefIdByCode(getSubjectStateDisabled())
  }

  List getSubjectsBy(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      subjectId        : null,
      baseSubjectId    : null,
      parentSubjectId  : null,
      typeId           : null,
      regionId         : null,
      ownerId          : null,
      creatorId        : null,
      name             : null,
      code             : null,
      firmId           : getFirmId(),
      resellerId       : getResellerId(),
      stateId          : getSubjectStateOnId(),
      tags             : null
    ], input)
    LinkedHashMap where = [:]

    if (params.subjectId) {
      where.n_subject_id = params.subjectId
    }
    if (params.baseSubjectId) {
      where.n_base_subject_id = params.baseSubjectId
    }
    if (params.parentSubjectId) {
      where.n_parent_subj_id = params.parentSubjectId
    }
    if (params.typeId) {
      where.n_subj_type_id = params.typeId
    }
    if (params.regionId) {
      where.n_region_id = params.regionId
    }
    if (params.ownerId) {
      where.n_owner_id = params.ownerId
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
    if (params.firmId) {
      where.n_firm_id = params.firmId
    }
    if (params.resellerId) {
      where.n_reseller_id = params.resellerId
    }
    if (params.stateId) {
      where.n_subj_state_id = params.stateId
    }
    if (params.tags) {
      where.t_tags = params.tags
    }
    return hid.getTableData(getSubjectsTable(), where: where)
  }

  LinkedHashMap getSubjectBy(LinkedHashMap input) {
    return getSubjectsBy(input)?.getAt(0)
  }

  LinkedHashMap getSubject(def subjectId) {
    return hid.getTableFirst(getSubjectsTable(), where: [n_subject_id: subjectId]) // Do not use getSubjectBy there just because we need to fetch firmId of existing subject before MAIN.INIT
  }

  def getSubjectTypeId(def subjectId) {
    LinkedHashMap where = [
      n_subject_id: subjectId
    ]
    return hid.getTableFirst(getSubjectsTable(), 'n_subj_type_id', where)
  }

  Boolean isSubject(String entityType) {
    return entityType.contains('SUBJ_TYPE_')
  }

  Boolean isSubject(def entityIdOrEntityTypeId) {
    return getRefCodeById(entityIdOrEntityTypeId)?.contains('SUBJ_TYPE_') || getSubject(entityIdOrEntityTypeId) != null
  }

  Boolean changeSubjectState(
    def subjectId,
    def stateId
  ) {
    try {
      logger.info("Changing subject ${subjectId} state to ${stateId}")
      hid.execute('SI_SUBJECTS_PKG.CHANGE_STATE', [
        num_N_SUBJECT_ID    : subjectId,
        num_N_SUBJ_STATE_ID : stateId
      ])
      logger.info("   Subject state was changed successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while changing subject state!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean enableSubject(def subjectId) {
    changeSubjectState(subjectId, getSubjectStateOnId())
  }

  Boolean suspendSubject(def subjectId) {
    changeSubjectState(subjectId, getSubjectStateSuspendedId())
  }

  Boolean disableSubject(def subjectId) {
    changeSubjectState(subjectId, getSubjectStateDisabledId())
  }

  def getSubjectAddParamTypeIdByCode(
    String code,
    def    subjTypeId = null
  ) {
    LinkedHashMap where = [
      vc_code: code
    ]
    if (subjTypeId) {
      where.n_subj_type_id = subjTypeId
    }
    return hid.getTableFirst(getSubjectAddParamTypesTable(), 'n_subj_value_type_id', where)
  }

  List getSubjectAddParamsBy(LinkedHashMap input) {
    def defaultParams = [
      subjectId : null,
      paramId   : null,
      date      : null,
      string    : null,
      number    : null,
      bool      : null,
      refId     : null
    ]
    if (input.containsKey('param')) {
      input.paramId = getSubjectAddParamTypeIdByCode(input.param.toString(), getSubjectTypeId(input.subjectId))
      input.remove('param')
    }
    LinkedHashMap params = mergeParams(defaultParams, input)
    LinkedHashMap where = [:]

    if (params.subjectId) {
      where.n_subject_id = params.subjectId
    }
    if (params.paramId) {
      where.n_subj_value_type_id = params.paramId
    }
    if (params.date) {
      where.d_value = params.date
    }
    if (params.string) {
      where.vc_value = params.string
    }
    if (params.paramInumberd) {
      where.n_value = params.number
    }
    if (params.bool != null) {
      where.c_fl_value = Oracle.encodeBool(params.bool)
    }
    if (params.refId) {
      where.n_ref_id = params.refId
    }
    return hid.getTableData(getSubjectAddParamsTable(), where: where)
  }

  LinkedHashMap getSubjectAddParamBy(LinkedHashMap input) {
    return getSubjectAddParamsBy(input)?.getAt(0)
  }

  Boolean putSubjectAddParam(LinkedHashMap input) {
    def defaultParams = [
      subjectId : null,
      paramId   : null,
      date      : null,
      string    : null,
      number    : null,
      bool      : null,
      refId     : null
    ]
    if (input.containsKey('param')) {
      input.paramId = getSubjectAddParamTypeIdByCode(input.param.toString(), getSubjectTypeId(input.subjectId))
      input.remove('param')
    }
    if (input.containsKey('value')) {
      def value = input.value
      if (value instanceof Boolean) {
        input.bool   = value
      } else if (value instanceof BigInteger) {
        input.refId  = value
      } else if (value instanceof String) {
        input.string = value
      } else if (DateTimeUtil.isDate(value)) {
        input.date   = value
      } else {
        input.number = value
      }
      input.remove('value')
    }
    LinkedHashMap params = mergeParams(defaultParams, input)
    try {
      def paramValue = params.date ?: params.string ?: params.number ?: params.bool ?: params.refId
      logger.info("Putting additional param ${params.paramId} value ${paramValue} to subject ${params.subjectId}")

      hid.execute('SI_SUBJECTS_PKG.PUT_SUBJ_VALUE', [
        num_N_SUBJECT_ID         : params.subjectId,
        num_N_SUBJ_VALUE_TYPE_ID : params.paramId,
        dt_D_VALUE               : params.date,
        vch_VC_VALUE             : params.string,
        num_N_VALUE              : params.number,
        ch_C_FL_VALUE            : Oracle.encodeBool(params.bool),
        num_N_REF_ID             : params.refId
      ])
      logger.info("   Additional param value was put successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while putting additional param!")
      logger.error_oracle(e)
      return false
    }
  }

  List getSubjectGroupsBy(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      subjectId : null,
      groupId   : null,
      isMain    : null
    ], input)
    LinkedHashMap where = [:]
    LinkedHashMap order = [c_fl_main: 'DESC']

    if (params.subjectId) {
      where.n_subject_id = params.subjectId
    }
    if (params.groupId) {
      where.n_subj_group_id = params.groupId
    }
    if (params.isMain != null) {
      where.c_fl_main = Oracle.encodeBool(params.isMain)
    }
    return hid.getTableData(getSubjectGroupsTable(), where: where, order: order)
  }

  LinkedHashMap getSubjectGroupBy(LinkedHashMap input) {
    return getSubjectGroupsBy(input)?.getAt(0)
  }

  List getSubjectGroups(def subjectId) {
    return getSubjectGroupsBy(subjectId: subjectId)
  }

  LinkedHashMap getSubjectGroup(def subjectId) {
    return getSubjectGroupBy(subjectId: subjectId, isMain: true)
  }

  LinkedHashMap putSubjectGroup(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      subjSubjectId : null,
      subjectId     : null,
      groupId       : null,
      isMain        : null
    ], input)
    try {
      logger.info("Putting subject id ${params.subjectId} group id ${params.groupId} with main flag ${params.isMain}")

      LinkedHashMap subjSubject = hid.execute('SI_SUBJECTS_PKG.SI_SUBJ_SUBJECTS_PUT', [
        num_N_SUBJ_SUBJECT_ID : params.subjSubjectId,
        num_N_SUBJECT_ID      : params.subjectId,
        num_N_SUBJECT_BIND_ID : params.groupId,
        ch_C_FL_MAIN          : Oracle.encodeBool(params.isMain)
      ])
      logger.info("   Subject group was put successfully!")
      return subjSubject
    } catch (Exception e){
      logger.error("   Error while putting subject group!")
      logger.error_oracle(e)
      return null
    }
  }

  Boolean deleteSubjectGroup(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      subjSubjectId : null,
      subjectId     : null,
      groupId       : null,
      isMain        : null
    ], input)
    try {
      if (params.subjSubjectId == null) {
        def group = getSubjectGroupBy(input)
        if (group) {
          params.subjSubjectId = group.n_subj_subject_id
        } else {
          throw new Exception('No group found!')
        }
      }

      logger.info("Deleting subject group id ${subjSubjectId}")
      hid.execute('SI_ADDRESSES_PKG.SI_SUBJ_SUBJECTS_DEL', [
        num_N_SUBJ_SUBJECT_ID : subjSubjectId
      ])
      logger.info("   Subject group was deleted successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while deleting a subject group!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean deleteSubjectGroup(def subjSubjectId) {
    return deleteSubjGroup(subjSubjectId: subjSubjectId)
  }
}