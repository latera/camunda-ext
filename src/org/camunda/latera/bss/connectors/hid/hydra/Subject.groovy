package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Oracle.encodeBool
import static org.camunda.latera.bss.utils.Oracle.decodeBool
import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import static org.camunda.latera.bss.utils.DateTimeUtil.local
import static org.camunda.latera.bss.utils.Constants.ENTITY_TYPE_Subject
import static org.camunda.latera.bss.utils.Constants.SUBJ_STATE_On
import static org.camunda.latera.bss.utils.Constants.SUBJ_STATE_Locked
import static org.camunda.latera.bss.utils.Constants.SUBJ_STATE_ManuallySuspended
import static org.camunda.latera.bss.utils.Constants.SUBJ_STATE_Disabled
import static org.camunda.latera.bss.utils.Constants.COMMENT_TYPE_Comment

/**
 * Subject specific methods
 */
trait Subject {
  private static String SUBJECTS_TABLE                = 'SI_V_SUBJECTS'
  private static String SUBJECT_ADD_PARAMS_TABLE      = 'SI_V_SUBJ_VALUES'
  private static String SUBJECT_ADD_PARAM_TYPES_TABLE = 'SI_V_SUBJ_VALUES_TYPE'
  private static String SUBJECT_GROUPS_TABLE          = 'SI_V_SUBJECT_BIND_GROUPS'
  private static String SUBJECTS_MV                   = 'SI_MV_SUBJECTS'
  private static String SUBJECT_ADD_PARAMS_MV         = 'SI_MV_SUBJ_VALUES'

  /**
   * Get subjects table name
   */
  String getSubjectsTable() {
    return SUBJECTS_TABLE
  }

  /**
   * Get subject additional parameter values table name
   */
  String getSubjectAddParamsTable() {
    return SUBJECT_ADD_PARAMS_TABLE
  }

  /**
   * Get subject additional parameter types table name
   */
  String getSubjectAddParamTypesTable() {
    return SUBJECT_ADD_PARAM_TYPES_TABLE
  }

  /**
   * Get subject groups table name
   */
  String getSubjectGroupsTable() {
    return SUBJECT_GROUPS_TABLE
  }

  /**
   * Get subjects quick search material view name
   */
  private String getSubjectsMV() {
    return SUBJECTS_MV
  }

  /**
   * Get subject additional parameter values quick search material view name
   */
  private String getSubjectAddParamsMV() {
    return SUBJECT_ADD_PARAMS_MV
  }

  /**
   * Get subject entity type ref code
   */
  String getSubjectEntityType() {
    return getRefCode(getSubjectEntityTypeId())
  }

  /**
   * Get subject entity type ref id
   */
  Number getSubjectEntityTypeId() {
    return ENTITY_TYPE_Subject
  }

  /**
   * Get active subject state ref code
   */
  String getSubjectStateOn() {
    return getRefCode(getSubjectStateOnId())
  }

  /**
   * Get active subject state ref id
   */
  Number getSubjectStateOnId() {
    return SUBJ_STATE_On
  }

  /**
   * Get locked subject state ref code
   */
  String getSubjectStateLocked() {
    return getRefCode(getSubjectStateLockedId())
  }

  /**
   * Get locked subject state ref id
   */
  Number getSubjectStateLockedId() {
    return SUBJ_STATE_Locked
  }

  /**
   * Get suspended subject state ref code
   */
  String getSubjectStateSuspended() {
    return getRefCode(getSubjectStateSuspendedId())
  }

  /**
   * Get suspended subject state ref id
   */
  Number getSubjectStateSuspendedId() {
    return SUBJ_STATE_ManuallySuspended
  }

  /**
   * Get disabled subject state ref code
   */
  String getSubjectStateDisabled() {
    return getRefCode(getSubjectStateDisabledId())
  }

  /**
   * Get disabled subject state ref id
   */
  Number getSubjectStateDisabledId() {
    return SUBJ_STATE_Disabled
  }

  /**
   * Get subject comment type ref code
   */
  String getSubjectCommentType() {
    return getRefCode(getSubjectCommentTypeId())
  }

  /**
   * Get subject comment type ref id
   */
  Number getSubjectCommentTypeId() {
    return COMMENT_TYPE_Comment
  }

  /**
   * Search for subjects by different fields value
   * @param subjectId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param baseSubjectId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parentSubjectId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param typeId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param type            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param regionId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ownerId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param creatorId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param resellerId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. Default: current reseller id
   * @param stateId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param state           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param firmId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. Default: current firm id
   * @param tags            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit           {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order           {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: [:]
   * @return Subject table rows
   */
  List<Map> getSubjectsBy(Map input) {
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
      tags             : null,
      limit            : 0
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
      where += prepareEntityTagQuery('N_SUBJECT_ID', params.tags)
    }
    return hid.getTableData(getSubjectsTable(), where: where, order: params.order, limit: params.limit)
  }

  /**
   * Search for subject by different fields value
   * @param subjectId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param baseSubjectId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parentSubjectId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param typeId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param type            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param regionId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ownerId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param creatorId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param resellerId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. Default: current reseller id
   * @param stateId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param state           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param firmId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. Default: current firm id
   * @param tags            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit           {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order           {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: [:]
   * @return Subject table row
   */
  Map getSubjectBy(Map input) {
    return getSubjectsBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Get subject by id
   * @param subjectId {@link java.math.BigInteger BigInteger}
   * @return Subject table row
   */
  Map getSubject(def subjectId) {
    LinkedHashMap where = [
      n_subject_id: subjectId
    ]
    return hid.getTableFirst(getSubjectsTable(), where: where)
  }

  /**
   * Get subject type id
   * @param subjectId {@link java.math.BigInteger BigInteger}
   * @return Subject type id
   */
  Number getSubjectTypeId(def subjectId) {
    LinkedHashMap where = [
      n_subject_id: subjectId
    ]
    return toIntSafe(hid.getTableFirst(getSubjectsTable(), 'n_subj_type_id', where))
  }

  /**
   * Check if entity or entity type is subject
   * @param entityOrEntityType {@link java.math.BigInteger BigInteger} or {@link CharSequence String}. Entity id, entity type ref id or entity type ref code
   * @return True if given value is subject, false otherwise
   */
  Boolean isSubject(def entityOrEntityType) {
    if (entityOrEntityType == null) {
      return false
    }

    Number entityIdOrEntityTypeId = toIntSafe(entityOrEntityType)
    if (entityIdOrEntityTypeId != null) {
      return getRefCode(entityIdOrEntityTypeId)?.contains('SUBJ_TYPE_') || entityIdOrEntityTypeId == getSubjectEntityTypeId() || getSubject(entityIdOrEntityTypeId) != null
    } else {
      return entityOrEntityType.contains('SUBJ_TYPE_') || entityOrEntityType == getSubjectEntityType()
    }
  }

  /**
   * Change subject state
   * @param subjectId {@link java.math.BigInteger BigInteger}
   * @param stateId   {@link java.math.BigInteger BigInteger}
   * @return True if state was changed successfully, false otherwise
   */
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

  /**
   * Change subject state to Active
   * @param subjectId {@link java.math.BigInteger BigInteger}
   * @return True if state was changed successfully, false otherwise
   */
  Boolean enableSubject(def subjectId) {
    return changeSubjectState(subjectId, getSubjectStateOnId())
  }

  /**
   * Change subject state to Suspended
   * @param subjectId {@link java.math.BigInteger BigInteger}
   * @return True if state was changed successfully, false otherwise
   */
  Boolean suspendSubject(def subjectId) {
    return changeSubjectState(subjectId, getSubjectStateSuspendedId())
  }

  /**
   * Change subject state to Disabled
   * @param subjectId {@link java.math.BigInteger BigInteger}
   * @return True if state was changed successfully, false otherwise
   */
  Boolean disableSubject(def subjectId) {
    return changeSubjectState(subjectId, getSubjectStateDisabledId())
  }

  /**
   * Get subject additional parameter type by id
   * @param subjValueTypeId {@link java.math.BigInteger BigInteger}
   * @return Subject additional parameter table row
   */
  Map getSubjectAddParamType(def subjValueTypeId) {
    LinkedHashMap where = [
      n_subj_value_type_id: subjValueTypeId
    ]
    return hid.getTableFirst(getSubjectAddParamTypesTable(), where: where)
  }

  /**
   * Search for subject additional parameter types by different fields value
   * @param subjValueTypeId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subjTypeId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subjType        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param dataTypeId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param dataType        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refTypeId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refType         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param canModify       {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isMulti         {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isReadOnly      {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param rem             {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit           {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order           {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: [:]
   * @return Subject additional parameter type table rows
   */
  List<Map> getSubjectAddParamTypesBy(Map input) {
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
      rem             : null,
      limit           : 0
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
      where.c_can_modify = encodeBool(params.canModify)
    }
    if (params.isMulti != null) {
      where.c_fl_multi = encodeBool(params.isMulti)
    }
    if (params.isReadOnly != null) {
      where.c_fl_read_only = encodeBool(params.isReadOnly)
    }
    return hid.getTableData(getSubjectAddParamTypesTable(), where: where, order: params.order, limit: params.limit)
  }

  /**
   * Search for subject additional parameter type by different fields value
   * @param subjValueTypeId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subjTypeId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subjType        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param dataTypeId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param dataType        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refTypeId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refType         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param canModify       {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isMulti         {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isReadOnly      {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param rem             {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param order           {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: [:]
   * @return Subject additional parameter type table row
   */
  Map getSubjectAddParamTypeBy(Map input) {
    return getSubjectAddParamTypesBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Search for subject additional parameter type by code
   * @param code       {@link CharSequence String}
   * @param subjTypeId {@link java.math.BigInteger BigInteger} Optional
   * @return Subject additional parameter type table row
   */
  Map getSubjectAddParamTypeByCode(CharSequence code, def subjTypeId = null) {
    return getSubjectAddParamTypeBy(code: code, subjTypeId: subjTypeId)
  }

  /**
   * Prepare subject additional parameter value to save
   * @param paramId    {@link java.math.BigInteger BigInteger}. Optional if 'param' is passed
   * @param param      {@link CharSequence String}. Optional if 'paramId' is passed
   * @param subjectId  {@link java.math.BigInteger BigInteger}. Existing subject id to find additional parameter type. Optional
   * @param subjTypeId {@link java.math.BigInteger BigInteger}. Subject type if to find additional parameter type. Optional
   * @param value      Any type. Optional
   * @return Additional parameter value as Map
   * <pre>
   * {@code
   * [
   *   paramId : _, # doc additional parameter type id
   *   bool    : _, # if additional parameter is boolean type
   *   number  : _, # if additional parameter is number type
   *   string  : _, # if additional parameter is string type
   *   date    : _, # if additional parameter is date type
   *   refId   : _, # if additional parameter is refId type and value can be converted to BigInteger (ref id)
   *   ref     : _  # if additional parameter is refId type and value cannot be converted to BigInteger (ref code)
   * ]
   * }
   * </pre>
   */
  Map prepareSubjectAddParam(Map input) {
    LinkedHashMap param = null
    if (input.containsKey('param')) {
      def subjTypeId = input.subjTypeId ?: getSubjectTypeId(input.subjectId)
      param = getSubjectAddParamTypeByCode(input.param.toString(), subjTypeId)
      input.paramId = param?.n_subj_value_type_id
      input.remove('param')
    } else if (input.containsKey('paramId')) {
      param = getSubjectAddParamType(input.paramId)
    }
    input.isMultiple = decodeBool(param.c_fl_multi)

    if (input.containsKey('value')) {
      def (valueType, val) = getAddParamDataType(param, input.value)
      input."${valueType}" = val
      input.remove('value')
    }
    return input
  }

  /**
   * Search for subject additional parameter values by different fields value
   * @param subjValueId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subjectId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subjTypeId  {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subjType    {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param paramId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional if 'param' is passed
   * @param param       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional if 'paramId' is passed
   * @param date        {@link java.time.Temporal Any date type}. Optional
   * @param number      {@link Double}, {@link Integer}, {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param string      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bool        {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ref         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param value       Any type which is automatically converted to 'date', 'string', 'name', 'bool' or 'refId', see {@link #prepareDocumentAddParam(Map)}. Optional
   * @param limit       {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order       {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: [:]
   * @return Subject additional parameter value table rows
   */
  List<Map> getSubjectAddParamsBy(Map input) {
    LinkedHashMap params = mergeParams([
      subjValueId : null,
      subjectId   : null,
      paramId     : null,
      date        : null,
      string      : null,
      number      : null,
      bool        : null,
      refId       : null,
      limit       : 0
    ], prepareSubjectAddParam(input))
    LinkedHashMap where = [:]

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
      where.c_fl_value = encodeBool(params.bool)
    }
    if (params.refId) {
      where.n_ref_id = params.refId
    }
    return hid.getTableData(getSubjectAddParamsTable(), where: where, order: params.order, limit: params.limit)
  }

  /**
   * Search for subject additional parameter value by different fields value
   * @param subjValueId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subjectId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subjTypeId  {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subjType    {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param paramId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional if 'param' is passed
   * @param param       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional if 'paramId' is passed
   * @param date        {@link java.time.Temporal Any date type}. Optional
   * @param number      {@link Double}, {@link Integer}, {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param string      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bool        {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ref         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param value       Any type which is automatically converted to 'date', 'string', 'name', 'bool' or 'refId', see {@link #prepareDocumentAddParam(Map)}. Optional
   * @param limit       {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order       {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: [:]
   * @return Subject additional parameter value table row
   */
  Map getSubjectAddParamBy(Map input) {
    return getSubjectAddParamsBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Create or update subject additional parameter value
   * @param subjValueId {@link java.math.BigInteger BigInteger}. Optional
   * @param subjectId   {@link java.math.BigInteger BigInteger}. Optional
   * @param paramId     {@link java.math.BigInteger BigInteger}. Optional
   * @param param       {@link CharSequence String}. Optional
   * @param date        {@link java.time.Temporal Any date type}. Optional
   * @param number      {@link Double}, {@link Integer}, {@link java.math.BigInteger BigInteger}. Optional
   * @param string      {@link CharSequence String}. Optional
   * @param bool        {@link Boolean}. Optional
   * @param refId       {@link java.math.BigInteger BigInteger}. Optional
   * @param ref         {@link CharSequence String}. Optional
   * @param value       Any type which is automatically converted to 'date', 'string', 'name', 'bool' or 'refId', see {@link #prepareDocumentAddParam(Map)}. Optional
   * @return Created or updated subject additional parameter value (in Oracle API procedure notation)
   */
  private Map putSubjectAddParam(Map input) {
    LinkedHashMap params = mergeParams([
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
      LinkedHashMap result = hid.execute('SI_SUBJECTS_PKG.SI_SUBJ_VALUES_PUT', [
        num_N_SUBJ_VALUE_ID      : params.subjValueId,
        num_N_SUBJECT_ID         : params.subjectId,
        num_N_SUBJ_VALUE_TYPE_ID : params.paramId,
        dt_D_VALUE               : params.date,
        vch_VC_VALUE             : params.string,
        num_N_VALUE              : params.number,
        ch_C_FL_VALUE            : encodeBool(params.bool),
        num_N_REF_ID             : params.refId
      ])
      logger.info("   Subject additional value was ${params.subjValueId ? 'put' : 'created'} successfully!")
      return result
    } catch (Exception e){
      logger.error("   Error while putting or creating subject additional value!")
      logger.error_oracle(e)
      return null
    }
  }

  /**
   * Create or update subject additional parameter value
   * @param subjectId   {@link java.math.BigInteger BigInteger}
   * @param paramId     {@link java.math.BigInteger BigInteger}. Optional
   * @param param       {@link CharSequence String}. Optional
   * @param date        {@link java.time.Temporal Any date type}. Optional
   * @param number      {@link Double}, {@link Integer}, {@link java.math.BigInteger BigInteger}. Optional
   * @param string      {@link CharSequence String}. Optional
   * @param bool        {@link Boolean}. Optional
   * @param refId       {@link java.math.BigInteger BigInteger}. Optional
   * @param ref         {@link CharSequence String}. Optional
   * @param value       Any type which is automatically converted to 'date', 'string', 'name', 'bool' or 'refId', see {@link #prepareDocumentAddParam(Map)}. Optional
   * @return Created or updated subject additional parameter value (in Oracle API procedure notation)
   */
  Map addSubjectAddParam(Map input = [:], def subjectId) {
    return putSubjectAddParam(input + [subjectId: subjectId])
  }

  /**
   * Delete subject additional parameter value
   * @param subjValueId {@link java.math.BigInteger BigInteger}
   * @return True if subject additional parameter value is deleted successfully, false otherwise
   */
  Boolean deleteSubjectAddParam(def subjValueId) {
    try {
      logger.info("Deleting subject additional value id ${subjValueId}")
      hid.execute('SI_SUBJECTS_PKG.SI_SUBJ_VALUES_DEL', [
        num_N_SUBJ_VALUE_ID : subjValueId
      ])
      logger.info("   Subject additional value was deleted successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while deleting subject additional value!")
      logger.error_oracle(e)
      return false
    }
  }

  /**
   * Delete subject additional parameter value
   *
   * Overload for searching and deleting additional parameter value
   * @see #getSubjectAddParamBy(java.util.Map)
   * @see #deleteSubjectAddParam(def)
   */
  Boolean deleteSubjectAddParam(Map input) {
    def subjValueId = getSubjectAddParamBy(input)?.n_subj_value_id
    return deleteSubjectAddParam(subjValueId)
  }

  /**
   * Search for subject-group binds by different fields value
   * @param subjectId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param groupId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isMain    {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit     {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order     {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: C_FL_MAIN DESC
   * @return Subject-group bind table rows
   */
  List<Map> getSubjectGroupsBy(Map input) {
    LinkedHashMap params = mergeParams([
      subjectId : null,
      groupId   : null,
      isMain    : null,
      limit     : 0,
      order     : [c_fl_main: 'desc']
    ], input)
    LinkedHashMap where = [:]

    if (params.subjectId) {
      where.n_subject_id = params.subjectId
    }
    if (params.groupId) {
      where.n_subj_group_id = params.groupId
    }
    if (params.isMain != null) {
      where.c_fl_main = encodeBool(params.isMain)
    }
    return hid.getTableData(getSubjectGroupsTable(), where: where, order: params.order, limit: params.limit)
  }

  /**
   * Search for subject-group bind by different fields value
   * @param subjectId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param groupId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isMain    {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param order     {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: C_FL_MAIN DESC
   * @return Subject-group bind table row
   */
  Map getSubjectGroupBy(Map input) {
    return getSubjectGroupsBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Get subject-group binds for subject
   * @param subjectId {@link java.math.BigInteger BigInteger}
   * @return Subject-group bind table rows
   */
  List<Map> getSubjectGroups(def subjectId) {
    return getSubjectGroupsBy(subjectId: subjectId)
  }

  /**
   * Get subject-group bind for subject
   * @param subjectId {@link java.math.BigInteger BigInteger}
   * @return Subject-group bind table row
   */
  Map getSubjectGroup(def subjectId) {
    return getSubjectGroupBy(subjectId: subjectId, isMain: true)
  }

  /**
   * Create or update subject-group bind
   * @param subjSubjectId {@link java.math.BigInteger BigInteger}. Optional
   * @param subjectId     {@link java.math.BigInteger BigInteger}. Optional
   * @param groupId       {@link java.math.BigInteger BigInteger}. Optional
   * @param isMain        {@link Boolean}. Optional
   * @return Created or updated subject-group bind (in Oracle API procedure notation)
   */
  private Map putSubjectGroup(Map input) {
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
        ch_C_FL_MAIN          : encodeBool(params.isMain)
      ])
      logger.info("   Subject group was put successfully!")
      return subjSubject
    } catch (Exception e){
      logger.error("   Error while putting subject group!")
      logger.error_oracle(e)
      return null
    }
  }

  /**
   * Created subject-group bind
   * @param subjectId     {@link java.math.BigInteger BigInteger}
   * @param groupId       {@link java.math.BigInteger BigInteger}
   * @param isMain        {@link Boolean}. Optional
   * @return Created subject-group bind (in Oracle API procedure notation)
   */
  Map addSubjectGroup(Map input = [:], def subjectId) {
    return putSubjectGroup(input + [subjectId: subjectId])
  }

  /**
   * Delete subject-group bind
   * @param subjSubjectId {@link java.math.BigInteger BigInteger}
   * @return True if subject-group bind was deleted successfully, false otherwise
   */
  Boolean deleteSubjectGroup(def subjSubjectId) {
    return deleteSubjGroup(subjSubjectId: subjSubjectId)
  }

  /**
   * Delete subject-group bind
   *
   * Overload for searching and deleting subject group
   * @see #getSubjectGroupBy(java.util.Map)
   * @see #deleteSubjectGroup(def)
   */
  Boolean deleteSubjectGroup(Map input) {
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

      logger.info("Deleting subject group id ${params.subjSubjectId}")
      hid.execute('SI_SUBJECTS_PKG.SI_SUBJ_SUBJECTS_DEL', [
        num_N_SUBJ_SUBJECT_ID : params.subjSubjectId
      ])
      logger.info("   Subject group was deleted successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while deleting a subject group!")
      logger.error_oracle(e)
      return false
    }
  }

  /**
   * Create or update subject comment
   * @param lineId        {@link java.math.BigInteger BigInteger}. Optional
   * @param subjectId     {@link java.math.BigInteger BigInteger}. Optional
   * @param typeId        {@link java.math.BigInteger BigInteger}. Optional
   * @param type          {@link CharSequence String}. Optional
   * @param operationDate {@link java.time.Temporal Any date type}. Optional. Default: current datetime
   * @param signalDate    {@link java.time.Temporal Any date type}. Optional
   * @param content       {@link CharSequence String}. Optional
   * @param authorId      {@link java.math.BigInteger BigInteger}. Optional
   */
  private Map putSubjectComment(Map input) {
    LinkedHashMap params = mergeParams([
      lineId        : null,
      subjectId     : null,
      typeId        : getSubjectCommentTypeId(),
      operationDate : local(),
      signalDate    : null,
      content       : null,
      authorId      : null
    ], input)
    try {
      logger.info("Putting subject id ${params.subjectId} comment line ${params.lineId} with content ${params.content} and signal date ${params.signalDate}")

      LinkedHashMap args = [
        num_N_LINE_ID         : params.lineId,
        num_N_SUBJECT_ID      : params.subjectId,
        num_N_COMMENT_TYPE_ID : params.typeId,
        dt_D_OPER             : params.operationDate,
        dt_D_SIGNAL           : params.signalDate,
        clb_CL_COMMENT        : params.content
      ]

      if (params.authorId != null) {
        args.num_N_AUTHOR_ID = params.authorId
      }

      LinkedHashMap subjComment = hid.execute('SI_SUBJECTS_PKG.SI_SUBJ_COMMENTS_PUT',  args)
      logger.info("   Subject comment was put successfully!")
      return subjComment
    } catch (Exception e){
      logger.error("   Error while putting subject comment!")
      logger.error_oracle(e)
      return null
    }
  }

  /**
   * Create subject comment
   * @param subjectId     {@link java.math.BigInteger BigInteger}
   * @param lineId        {@link java.math.BigInteger BigInteger}. Optional
   * @param typeId        {@link java.math.BigInteger BigInteger}. Optional
   * @param type          {@link CharSequence String}. Optional
   * @param operationDate {@link java.time.Temporal Any date type}. Optional. Default: current datetime
   * @param signalDate    {@link java.time.Temporal Any date type}. Optional
   * @param content       {@link CharSequence String}
   * @param authorId      {@link java.math.BigInteger BigInteger}. Optional
   */
  Map addSubjectComment(Map input = [:], def subjectId) {
    return putSubjectComment(input + [subjectId: subjectId])
  }

  /**
   * Delete subject comment
   * @param lineId {@link java.math.BigInteger BigInteger}
   * @return True if subject comment was deleted successfully, false otherwise
   */
  Boolean deleteSubjectComment(def lineId) {
    try {
      logger.info("Deleting subject comment line id ${lineId}")
      hid.execute('SI_SUBJECTS_PKG.SI_SUBJ_COMMENTS_DEL', [
        num_N_LINE_ID : lineId
      ])
      logger.info("   Subject comment was deleted successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while deleting a subject comment!")
      logger.error_oracle(e)
      return false
    }
  }

  /**
   * Add tag to subject
   * @param subjectId {@link java.math.BigInteger BigInteger}
   * @param tagId     {@link java.math.BigInteger BigInteger}. Optional if 'tag' is pased
   * @param tag       {@link CharSequence String}. Optional if 'tagId' is pased
   * @return True if subject tag was added successfully, false otherwise
   */
  Map addSubjectTag(Map input) {
    input.entityId = input.subjectId
    input.remove('subjectId')
    return addEntityTag(input)
  }

  /**
   * Add tag to subject
   *
   * Overload with tag code instead of id
   * @param subjectId {@link java.math.BigInteger BigInteger}
   * @param tag       {@link CharSequence String}
   * @see #addSubjectTag(java.util.Map)
   */
  Map addSubjectTag(def subjectId, CharSequence tag) {
    return addSubjectTag(subjectId: subjectId, tag: tag)
  }

  /**
   * Add tag to subject
   *
   * Overload with mandatory subject id arg
   * @param subjectId {@link java.math.BigInteger BigInteger}
   * @param tagId     {@link java.math.BigInteger BigInteger}. Optional if 'tag' is pased
   * @param tag       {@link CharSequence String}. Optional if 'tagId' is pased
   * @see #addSubjectTag(java.util.Map,def)
   */
  Map addSubjectTag(Map input = [:], def subjectId) {
    return addSubjectTag(input + [subjectId: subjectId])
  }

  /**
   * Delete tag from subject
   * @param subjTagId {@link java.math.BigInteger BigInteger}
   * @return True if subject tag was deleted successfully, false otherwise
   */
  Boolean deleteSubjectTag(def subjTagId) {
    return deleteEntityTag(subjTagId)
  }

  /**
   * Delete tag from subject
   *
   * Overload for names arguments input
   * @param subjectId {@link java.math.BigInteger BigInteger}
   * @param tagId     {@link java.math.BigInteger BigInteger}. Optional if 'tag' is pased
   * @param tag       {@link CharSequence String}. Optional if 'tagId' is pased
   * @see #deleteSubjectTag(def)
   */
  Boolean deleteSubjectTag(Map input) {
    input.entityId = input.subjectId
    input.remove('subjectId')
    return deleteEntityTag(input)
  }

  /**
   * Delete tag from subject
   *
   * Overload with subject id and tag code
   * @param subjectId {@link java.math.BigInteger BigInteger}
   * @param tag       {@link CharSequence String}
   * @see #deleteSubjectTag(java.util.Map)
   */
  Boolean deleteSubjectTag(def subjectId, CharSequence tag) {
    return deleteSubjectTag(subjectId: subjectId, tag: tag)
  }

  /**
   * Refresh subjects quick search material view
   * @see Search#refreshMaterialView(java.lang.CharSequence,java.lang.CharSequence)
   */
  Boolean refreshSubjects(CharSequence method = 'C') {
    return refreshMaterialView(getSubjectsMV(), method)
  }

  /**
   * Refresh subject app params quick search material view
   * @see Search#refreshMaterialView(java.lang.CharSequence,java.lang.CharSequence)
   */
  Boolean refreshSubjectAddParams(CharSequence method = 'C') {
    return refreshMaterialView(getSubjectAddParamsMV(), method)
  }
}