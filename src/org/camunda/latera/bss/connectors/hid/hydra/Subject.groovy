package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.utils.Oracle

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
    def params = mergeParams([
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
    def where = [
      n_subject_id: subjectId
    ]
    return hid.getTableFirst(getSubjectsTable(), where: where)
  }

  def getSubjectTypeId(def subjectId) {
    def where = [
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

  LinkedHashMap getSubjectAddParamType(def paramId) {
    def where = [
      n_subj_value_type_id: paramId
    ]
    return hid.getTableData(getSubjectAddParamTypesTable(), where: where)
  }

  LinkedHashMap getSubjectAddParamTypesBy(LinkedHashMap input) {
    def params = mergeParams([
      subjValueTypeId : null,
      subjTypeId      : null,
      dataTypeId      : null,
      code            : null,
      name            : null,
      refTypeId       : null,
      canModify       : null,
      isMulti         : null,
      isReadOnly      : null,
      rem             : null
    ], input)
    LinkedHashMap where = [:]

    if (params.subjValueTypeId || params.paramId) {
      where.n_subj_value_type_id = params.subjValueTypeId ?: params.paramId
    }
    if (params.subjTypeId) {
      where.n_subj_type_id = params.subjTypeId
    }
    if (params.dataTypeId) {
      where.n_data_type_id = params.dataTypeId
    }
    if (params.code) {
      where.vc_code = params.code
    }
    if (params.name) {
      where.vc_name = params.name
    }
    if (params.refTypeId || params.refId) {
      where.n_ref_type_id = params.refTypeId ?: params.refId
    }
    if (params.canModify != null) {
      where.c_can_modify = Oracle.encodeBool(params.canModify)
    }
    if (params.isMulti != null) {
      where.c_fl_multi = Oracle.encodeBool(params.isMulti)
    }
    if (params.isReadOnly != null) {
      where.c_fl_read_only = Oracle.encodeBool(params.isReadOnly)
    }
    return hid.getTableData(getSubjectAddParamTypesTable(), where: where)
  }

  LinkedHashMap getSubjectAddParamTypeBy(LinkedHashMap input) {
    return getSubjectAddParamTypesBy(input)?.getAt(0)
  }

  def getSubjectAddParamTypeByCode(String code, def subjTypeId = null) {
    return getSubjectAddParamTypeBy(code: code, subjTypeId: subjTypeId)
  }

  LinkedHashMap prepareSubjectAddParam(LinkedHashMap input) {
    def param = null
    if (input.containsKey('param')) {
      param = getSubjectAddParamTypeByCode(input.param.toString(), getSubjectTypeId(input.subjectId))
      input.paramId = param?.n_subj_value_type_id
      input.remove('param')
    } else if (input.containsKey('paramId')) {
      param = getSubjectAddParamType(input.paramId)
    }
    input.isMultiple = Oracle.decodeBool(param.c_fl_multi)

    if (input.containsKey('value')) {
      def valueType = getAddParamDataType(param)
      input."${valueType}" = input.value
      input.remove('value')
    }
    return input
  }

  List getSubjectAddParamsBy(LinkedHashMap input) {
    def params = mergeParams([
      subjValueId : null,
      subjectId   : null,
      paramId     : null,
      date        : null,
      string      : null,
      number      : null,
      bool        : null,
      refId       : null
    ], prepareSubjectAddParam(input))
    def where = [:]

    if (params.subjValueId) {
      where.n_subj_value_id = params.subjValueId
    }
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
    if (params.number) {
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

  LinkedHashMap putSubjectAddParam(LinkedHashMap input) {
    def params = mergeParams([
      subjValueId : null,
      subjectId   : null,
      paramId     : null,
      date        : null,
      string      : null,
      number      : null,
      bool        : null,
      refId       : null
    ], prepareSubjectAddParam(input))
    try {

      if (!params.subjValueId && !params.isMultiple) {
        params.subjValueId = getSubjectAddParamBy(
          subjectId : input.subjectId,
          paramId   : input.paramId
        )?.n_subj_value_id
      }

      logger.info("${params.subjValueId ? 'Putting' : 'Creating'} subject additional value with params ${params}")
      def result = hid.execute('SI_SUBJECTS_PKG.SI_SUBJ_VALUES_PUT', [
        num_N_SUBJ_VALUE_ID      : params.subjValueId,
        num_N_SUBJECT_ID         : params.subjectId,
        num_N_SUBJ_VALUE_TYPE_ID : params.paramId,
        dt_D_VALUE               : params.date,
        vch_VC_VALUE             : params.string,
        num_N_VALUE              : params.number,
        ch_C_FL_VALUE            : Oracle.encodeBool(params.bool),
        num_N_REF_ID             : params.refId
      ])
      logger.info("   Additional param value was ${params.subjValueId ? 'put' : 'created'} successfully!")
      return result
    } catch (Exception e){
      logger.error("   Error while putting or creating additional param!")
      logger.error_oracle(e)
      return null
    }
  }

  LinkedHashMap addSubjectAddParam(LinkedHashMap input) {
    return putSubjectAddParam(input)
  }

  LinkedHashMap addSubjectAddParam(def subjectId, LinkedHashMap input) {
    return putSubjectAddParam(input + [subjectId: subjectId])
  }

  Boolean deleteSubjectAddParam(def subjValueId) {
    try {
      logger.info("Deleting additional param value id ${subjValueId}")
      hid.execute('SI_SUBJECTS_PKG.SI_SUBJ_VALUES_DEL', [
        num_N_SUBJ_VALUE_ID : subjValueId
      ])
      logger.info("   Additional param value was deleted successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while deleting additional param!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean deleteSubjectAddParam(LinkedHashMap input) {
    def subjValueId = getSubjectAddParamBy(input)?.n_subj_value_id
    return deleteSubjectAddParam(subjValueId)
  }

  List getSubjectGroupsBy(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      subjectId : null,
      groupId   : null,
      isMain    : null
    ], input)
    def where = [:]
    def order = [c_fl_main: 'DESC']

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
    def params = mergeParams([
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

  Boolean addSubjectGroup(LinkedHashMap input) {
    return putSubjectGroup(input)
  }

  Boolean addSubjectGroup(def subjectId, LinkedHashMap input) {
    return putSubjectGroup(input + [subjectId: subjectId])
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