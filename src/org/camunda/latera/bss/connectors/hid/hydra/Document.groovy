package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Oracle.encodeDateStr
import static org.camunda.latera.bss.utils.Oracle.encodeBool
import static org.camunda.latera.bss.utils.Oracle.decodeBool
import static org.camunda.latera.bss.utils.DateTimeUtil.dayBegin
import static org.camunda.latera.bss.utils.StringUtil.isEmpty
import static org.camunda.latera.bss.utils.StringUtil.notEmpty
import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import static org.camunda.latera.bss.utils.MapUtil.nvl
import static org.camunda.latera.bss.utils.Constants.ENTITY_TYPE_Document
import static org.camunda.latera.bss.utils.Constants.DOC_STATE_Actual
import static org.camunda.latera.bss.utils.Constants.DOC_STATE_Executed
import static org.camunda.latera.bss.utils.Constants.DOC_STATE_Draft
import static org.camunda.latera.bss.utils.Constants.DOC_STATE_Canceled
import static org.camunda.latera.bss.utils.Constants.DOC_STATE_Closed
import static org.camunda.latera.bss.utils.Constants.DOC_STATE_Dissolved
import static org.camunda.latera.bss.utils.Constants.DOC_STATE_Processing
import static org.camunda.latera.bss.utils.Constants.DOC_STATE_Prepared
import static org.camunda.latera.bss.utils.Constants.SUBJ_ROLE_Provider
import static org.camunda.latera.bss.utils.Constants.SUBJ_ROLE_Receiver
import static org.camunda.latera.bss.utils.Constants.SUBJ_ROLE_Member
import static org.camunda.latera.bss.utils.Constants.SUBJ_ROLE_Manager

trait Document {
  private static String DOCUMENTS_TABLE                = 'SD_V_DOCUMENTS'
  private static String DOCUMENT_SUBJECTS_TABLE        = 'SI_V_DOC_SUBJECTS'
  private static String DOCUMENT_ADD_PARAMS_TABLE      = 'SD_V_DOC_VALUES'
  private static String DOCUMENT_ADD_PARAM_TYPES_TABLE = 'SS_V_WFLOW_DOC_VALUES_TYPE'
  private static String DOCUMENT_BINDS_TABLE           = 'SD_V_DOC_DOCUMENTS'
  private static String DOCUMENTS_MV                   = 'SD_MV_DOCUMENTS'
  private static String DOCUMENT_ADD_PARAMS_MV         = 'SD_MV_DOC_VALUES'

  String getDocumentsTable() {
    return DOCUMENTS_TABLE
  }

  String getDocumentSubjectsTable() {
    return DOCUMENT_SUBJECTS_TABLE
  }

  String getDocumentAddParamsTable() {
    return DOCUMENT_ADD_PARAMS_TABLE
  }

  String getDocumentAddParamTypesTable() {
    return DOCUMENT_ADD_PARAM_TYPES_TABLE
  }

  String getDocumentBindsTable() {
    return DOCUMENT_BINDS_TABLE
  }

  String getDocumentsMV() {
    return DOCUMENTS_MV
  }

  String getDocumentAddParamsMV() {
    return DOCUMENT_ADD_PARAMS_MV
  }

  String getDocumentEntityType() {
    return getRefCode(getDocumentEntityTypeId())
  }

  Number getDocumentEntityTypeId() {
    return ENTITY_TYPE_Document
  }

  String getDocumentStateActual() {
    return getRefCode(getDocumentStateActualId())
  }

  Number getDocumentStateActualId() {
    return DOC_STATE_Actual
  }

  String getDocumentStateExecuted() {
    return getRefCode(getDocumentStateExecutedId())
  }

  Number getDocumentStateExecutedId() {
    return DOC_STATE_Executed
  }

  String getDocumentStateDraft() {
    return getRefCode(getDocumentStateDraftId())
  }

  Number getDocumentStateDraftId() {
    return DOC_STATE_Draft
  }

  String getDocumentStateCanceled() {
    return getRefCode(getDocumentStateCanceledId())
  }

  Number getDocumentStateCanceledId() {
    return DOC_STATE_Canceled
  }

  String getDocumentStateClosed() {
    return getRefCode(getDocumentStateClosedId())
  }

  Number getDocumentStateClosedId() {
    return DOC_STATE_Closed
  }

  String getDocumentStateDissolved() {
    return getRefCode(getDocumentStateDissolvedId())
  }

  Number getDocumentStateDissolvedId() {
    return DOC_STATE_Dissolved
  }

  String getDocumentStateProcessing() {
    return getRefCode(getDocumentStateProcessingId())
  }

  Number getDocumentStateProcessingId() {
    return DOC_STATE_Processing
  }

  String getDocumentStatePrepared() {
    return getRefCode(getDocumentStatePreparedId())
  }

  Number getDocumentStatePreparedId() {
    return DOC_STATE_Prepared
  }

  String getProviderRole() {
    return getRefCode(getProviderRoleId())
  }

  Number getProviderRoleId() {
    return SUBJ_ROLE_Provider
  }

  String getReceiverRole() {
    return getRefCode(getReceiverRoleId())
  }

  Number getReceiverRoleId() {
    return SUBJ_ROLE_Receiver
  }

  String getMemberRole() {
    return getRefCode(getMemberRoleId())
  }

  Number getMemberRoleId() {
    return SUBJ_ROLE_Member
  }

  String getManagerRole() {
    return getRefCode(getManagerRoleId())
  }

  Number getManagerRoleId() {
    return SUBJ_ROLE_Manager
  }

  Map getDocument(def docId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return hid.getTableFirst(getDocumentsTable(), where: where)
  }

  private String subSelectForRole(Map inp = [:], def roleId) {
    LinkedHashMap pars = [
      where  : [:],
      column : 'n_subject_id'
    ] + inp
    pars.where.n_doc_id      = ['=': 'T.N_DOC_ID']
    pars.where.n_doc_role_id = roleId
    return hid.prepareTableQuery(getDocumentSubjectsTable(), fields: pars.column, where: pars.where, tableAlias: 'DS', asMap: false)
  }

  List getDocumentsBy(Map input) {
    LinkedHashMap params = mergeParams([
      docId         : null,
      docTypeId     : null,
      parentDocId   : null,
      reasonDocId   : null,
      workflowId    : null,
      providerId    : getFirmId(),
      receiverId    : null,
      memberId      : null,
      managerId     : null,
      stateId       : ['not in': [getDocumentStateCanceledId()]],
      operationDate : null,
      beginDate     : null,
      endDate       : null,
      number        : null,
      tags          : null,
      limit         : 0,
      order         : [d_begin: 'desc', vc_doc_no: 'desc']
    ], input)
    LinkedHashMap where  = [:]
    LinkedHashMap fields = ['*': null]

    if (params.docId) {
      where.n_doc_id = params.docId
    }
    if (params.docTypeId || params.typeId) {
      where.n_doc_type_id = params.docTypeId ?: params.typeId
    }
    if (params.parentDocId) {
      where.n_parent_doc_id = params.parentDocId
    }
    if (params.reasonDocId) {
      where.n_reason_doc_id = params.reasonDocId
    }
    if (params.workflowId) {
      where.n_workflow_id = params.workflowId
    }
    if (params.providerId || params.providerAccountId) {
      fields.n_provider_id         = subSelectForRole(getProviderRoleId(), where: [rownum: ['<=': 1]])
      fields.n_provider_account_id = subSelectForRole(getProviderRoleId(), where: [rownum: ['<=': 1]], column: 'n_account_id')
      where['_EXISTS']             = subSelectForRole(getProviderRoleId(), where: nvl(n_subject_id: params.providerId, n_account_id: params.providerAccountId))
    }
    if (params.receiverId || params.receiverAccountId) {
      fields.n_receiver_id         = subSelectForRole(getReceiverRoleId(), where: [rownum: ['<=': 1]])
      fields.n_receiver_account_id = subSelectForRole(getReceiverRoleId(), where: [rownum: ['<=': 1]], column: 'n_account_id')
      where['__EXISTS']            = subSelectForRole(getReceiverRoleId(), where: nvl(n_subject_id: params.receiverId, n_account_id: params.receiverAccountId))
    }
    if (params.memberId || params.memberAccountId) {
      fields.n_member_id           = subSelectForRole(getMemberRoleId(),   where: [rownum: ['<=': 1]])
      fields.n_member_account_id   = subSelectForRole(getMemberRoleId(),   where: [rownum: ['<=': 1]], column: 'n_account_id')
      where['___EXISTS']           = subSelectForRole(getMemberRoleId(),   where: nvl(n_subject_id: params.memberId, n_account_id: params.memberAccountId))
    }
    if (params.managerId || params.managerAccountId) {
      fields.n_manager_id          = subSelectForRole(getManagerRoleId(),  where: [rownum: ['<=': 1]])
      fields.n_manager_account_id  = subSelectForRole(getManagerRoleId(),  where: [rownum: ['<=': 1]], column: 'n_account_id')
      where['____EXISTS']          = subSelectForRole(getManagerRoleId(),  where: nvl(n_subject_id: params.managerId, n_account_id: params.managerAccountId))
    }
    if (params.stateId) {
      where.n_doc_state_id = params.stateId
    }
    if (params.number) {
      where.vc_doc_no = params.number
    }
    if (params.name) {
      where.vc_doc_name = params.name
    }
    if (params.code) {
      where.vc_doc_code = params.code
    }
    if (params.beginDate) {
      where.d_begin = params.beginDate
    }
    if (params.docDate) {
      where.d_doc = dayBegin(params.docDate)
    }
    if (params.docTime) {
      where.d_time = params.docTime
    }
    if (params.endDate) {
      where.d_end = params.endDate
    }
    if (params.tags) {
      where += prepareEntityTagQuery('N_DOC_ID', params.tags)
    }
    if (params.operationDate) {
      String oracleDate = encodeDateStr(params.operationDate)
      where[oracleDate] = [between: "d_begin and nvl(d_end, ${oracleDate})"]
    }
    return hid.getTableData(getDocumentsTable(), where: where, order: params.order, limit: params.limit)
  }

  Map getDocumentBy(Map input) {
    return getDocumentsBy(input + [limit: 1])?.getAt(0)
  }

  Number getDocumentTypeId(def docId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return toIntSafe(hid.getTableFirst(getDocumentsTable(), 'n_doc_type_id', where))
  }

  Number getDocumentWorkflowId(def docId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return toIntSafe(hid.getTableFirst(getDocumentsTable(), 'n_workflow_id', where))
  }

  Boolean isDocument(def entityOrEntityType) {
    if (entityOrEntityType == null) {
      return false
    }

    Number entityIdOrEntityTypeId = toIntSafe(entityOrEntityType)
    if (entityIdOrEntityTypeId != null) {
      return getRefCodeById(entityIdOrEntityTypeId)?.contains('DOC') || getDocument(entityIdOrEntityTypeId) != null || entityIdOrEntityTypeId == getDocumentEntityTypeId()
    } else {
      return entityOrEntityType.contains('DOC') || entityOrEntityType == getDocumentEntityType()
    }
  }

  private Map putDocument(Map input) {
    LinkedHashMap defaultParams = [
      docId       : null,
      docTypeId   : null,
      workflowId  : null,
      parentDocId : null,
      reasonDocId : null,
      prevDocId   : null,
      stornoDocId : null,
      docDate     : null,
      docTime     : null,
      number      : null,
      name        : null,
      code        : null,
      rem         : null,
      beginDate   : null,
      endDate     : null,
      firmId      : getFirmId()
    ]
    try {
      LinkedHashMap existingDocument = [:]
      if (notEmpty(input.docId)) {
        LinkedHashMap document = getDocument(input.docId)
        defaultParams += [
          docTypeId   : document.n_doc_type_id,
          workflowId  : document.n_workflow_id,
          parentDocId : document.n_parent_doc_id,
          reasonDocId : document.n_reason_doc_id,
          prevDocId   : document.n_prev_doc_Id,
          stornoDocId : document.n_storno_doc_id,
          docDate     : document.d_doc,
          docTime     : document.d_time,
          number      : document.vc_doc_no,
          name        : document.vc_name,
          code        : document.vc_code,
          rem         : document.vc_rem,
          beginDate   : document.d_begin,
          endDate     : document.d_end,
          firmId      : document.n_firm_id
        ]
      }
      LinkedHashMap params = mergeParams(defaultParams, existingDocument + input)

      logger.info("${params.docId ? 'Updating' : 'Creating'} document with params ${params}")
      LinkedHashMap result = hid.execute('SD_DOCUMENTS_PKG.SD_DOCUMENTS_PUT', [
        num_N_DOC_ID        : params.docId,
        num_N_DOC_TYPE_ID   : params.docTypeId,
        num_N_FIRM_ID       : params.firmId,
        num_N_PARENT_DOC_ID : params.parentDocId,
        num_N_REASON_DOC_ID : params.reasonDocId,
        num_N_PREV_DOC_ID   : params.prevDocId,
        num_N_STORNO_DOC_ID : params.stornoDocId,
        dt_D_DOC            : dayBegin(params.docDate),
        dt_D_TIME           : params.docTime,
        vch_VC_DOC_NO       : params.number,
        vch_VC_NAME         : params.name,
        vch_VC_CODE         : params.code,
        vch_VC_REM          : params.rem,
        dt_D_BEGIN          : params.beginDate,
        dt_D_END            : params.endDate,
        num_N_WORKFLOW_ID   : params.workflowId
      ])
      logger.info("   Document ${result.num_N_DOC_ID} was ${params.docId ? 'updated' : 'created'} successfully!")
      return result
    } catch (Exception e){
      logger.error("   Error while updating or creating document value!")
      logger.error_oracle(e)
      return null
    }
  }

  Map createDocument(Map input) {
    input.remove('docId')
    return putDocument(input)
  }

  Map updateDocument(Map input = [:], def docId) {
    return putDocument(input + [docId: docId])
  }

  Map getDocumentSubject(def docSubjectId) {
    LinkedHashMap where = [
      n_doc_subject_id: docSubjectId
    ]
    return hid.getTableFirst(getDocumentSubjectsTable(), where: where)
  }

  List getDocumentSubjectsBy(Map input) {
    LinkedHashMap params = mergeParams([
      docSubjectId  : null,
      docId         : null,
      roleId        : null,
      subjectId     : null,
      accountId     : null,
      limit         : 0
    ], input)
    LinkedHashMap where = [:]

    if (params.docSubjectId) {
      where.n_doc_subject_id = params.docSubjectId
    }
    if (params.docId) {
      where.n_doc_id = params.docId
    }
    if (params.roleId) {
      where.n_doc_role_id = params.roleId
    }
    if (params.subjectId) {
      where.n_subject_id = params.subjectId
    }
    if (params.accountId) {
      where.n_account_id = params.accountId
    }
    return hid.getTableData(getDocumentSubjectsTable(), where: where, order: params.order, limit: params.limit)
  }

  Map getDocumentSubjectBy(Map input) {
    return getDocumentSubjectsBy(input + [limit: 1])?.getAt(0)
  }

  Map getDocumentProviderBy(Map input) {
    return getDocumentSubjectBy(input + [roleId: getProviderRoleId()])
  }

  Map getDocumentProvider(def docId) {
    return getDocumentProviderBy(docId: docId)
  }

  Map getDocumentReceiverBy(Map input) {
    return getDocumentSubjectBy(input + [roleId: getReceiverRoleId()])
  }

  Map getDocumentReceiver(def docId) {
    return getDocumentReceiverBy(docId: docId)
  }

  Map getDocumentMemberBy(Map input) {
    return getDocumentSubjectBy(input + [roleId: getMemberRoleId()])
  }

  Map getDocumentMember(def docId) {
    return getDocumentMemberBy(docId: docId)
  }

  Map getDocumentManagerBy(Map input) {
    return getDocumentSubjectBy(input + [roleId: getManagerRoleId()])
  }

  Map getDocumentManager(def docId) {
    return getDocumentManagerBy(docId: docId)
  }

  Boolean putDocumentSubject(Map input) {
    LinkedHashMap params = mergeParams([
      docId      : null,
      subjectId  : null,
      roleId     : null,
      workflowId : null
    ], input)
    if (params.workflowId == null) {
      params.workflowId = getDocumentWorkflowId(params.docId)
    }
    try {
      logger.info("Putting subject ${params.subjectId} as role ${params.roleId} to document ${params.docId} with workflow ${params.workflowId}")
      hid.execute('SD_DOCUMENTS_PKG.PUT_DOC_SUBJECT', [
        num_N_DOC_ID      : params.docId,
        num_N_DOC_ROLE_ID : params.roleId,
        num_N_SUBJECT_ID  : params.subjectId,
        num_N_WORKFLOW_ID : params.workflowId
      ])
      logger.info("   Subject role was put successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while putting subject role!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean addDocumentSubject(Map input = [:], def docId) {
    return putDocumentSubject(input + [docId: docId])
  }

  Map getDocumentAddParamType(def paramId) {
    LinkedHashMap where = [
      n_doc_value_type_id: paramId
    ]
    return hid.getTableFirst(getDocumentAddParamTypesTable(), where: where)
  }

  List getDocumentAddParamTypesBy(Map input) {
    LinkedHashMap params = mergeParams([
      docValueTypeId  : null,
      docTypeId       : null,
      dataTypeId      : null,
      code            : null,
      name            : null,
      refTypeId       : null,
      canModify       : null,
      isMulti         : null,
      rem             : null,
      limit           : 0
    ], input)
    LinkedHashMap where = [:]

    if (params.docValueTypeId || params.paramId) {
      where.n_doc_value_type_id = params.docValueTypeId ?: params.paramId
    }
    if (params.docTypeId) {
      where.n_doc_type_id = params.docTypeId
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
    return hid.getTableData(getDocumentAddParamTypesTable(), where: where, order: params.order, limit: params.limit)
  }

  Map getDocumentAddParamTypeBy(Map input) {
    return getDocumentAddParamTypesBy(input + [limit: 1])?.getAt(0)
  }

  Map getDocumentAddParamTypeByCode(CharSequence code, def docTypeId = null) {
    return getDocumentAddParamTypeBy(code: code, docTypeId: docTypeId)
  }

  Number getDocumentAddParamTypeIdByCode(CharSequence code) {
    return toIntSafe(getDocumentAddParamTypeByCode(code)?.n_doc_value_type_id)
  }

  Map prepareDocumentAddParam(Map input) {
    LinkedHashMap param = null
    if (input.containsKey('param')) {
      def docTypeId = input.docTypeId ?: getDocumentTypeId(input.docId)
      param = getDocumentAddParamTypeByCode(input.param.toString(), docTypeId)
      input.paramId = param.n_doc_value_type_id
      input.remove('param')
    } else if (input.containsKey('paramId')) {
      param = getDocumentAddParamType(input.paramId)
    }
    input.isMultiple = decodeBool(param.c_fl_multi)

    if (input.containsKey('value')) {
      def (valueType, val) = getAddParamDataType(param, input.value)
      input."${valueType}" = val
      input.remove('value')
    }
    return input
  }

  List getDocumentAddParamsBy(Map input) {
    LinkedHashMap params = mergeParams([
      docValueId : null,
      docId      : null,
      paramId    : null,
      date       : null,
      string     : null,
      number     : null,
      bool       : null,
      refId      : null,
      limit      : 0
    ], prepareDocumentAddParam(input))
    LinkedHashMap where = [:]

    if (params.docValueId) {
      where.n_doc_value_id = params.docValueId
    }
    if (params.docId) {
      where.n_doc_id = params.docId
    }
    if (params.paramId) {
      where.n_doc_value_type_id = params.paramId
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
    return hid.getTableData(getDocumentAddParamsTable(), where: where, order: params.order, limit: params.limit)
  }

  Map getDocumentAddParamBy(Map input) {
    return getDocumentAddParamsBy(input + [limit: 1])?.getAt(0)
  }

  private Map putDocumentAddParam(Map input) {
    LinkedHashMap params = mergeParams([
      docValueId : null,
      docId      : null,
      paramId    : null,
      date       : null,
      string     : null,
      number     : null,
      bool       : null,
      refId      : null
    ], prepareDocumentAddParam(input))
    try {
      if (!params.docValueId && !params.isMultiple) {
        params.docValueId = getDocumentAddParamBy(
          docId   : input.docId,
          paramId : input.paramId
        )?.n_doc_value_id
      }

      logger.info("${params.docValueId ? 'Updating' : 'Creating'} document additional value with params ${params}")
      LinkedHashMap result = hid.execute('SD_DOCUMENTS_PKG.SD_DOC_VALUES_PUT', [
        num_N_DOC_VALUE_ID       : params.docValueId,
        num_N_DOC_ID             : params.docId,
        num_N_DOC_VALUE_TYPE_ID  : params.paramId,
        dt_D_VALUE               : params.date,
        vch_VC_VALUE             : params.string,
        num_N_VALUE              : params.number,
        ch_C_FL_VALUE            : encodeBool(params.bool),
        num_N_REF_ID             : params.refId
      ])
      logger.info("   Document additional value was ${params.docValueId ? 'updating' : 'created'} successfully!")
      return result
    } catch (Exception e){
      logger.error("   Error while ${input.docValueId ? 'updating' : 'creating'} document additional value!")
      logger.error_oracle(e)
      return null
    }
  }

  Map addDocumentAddParam(Map input = [:], def docId) {
    return putDocumentAddParam(input + [docId: docId])
  }

  Boolean deleteDocumentAddParam(def docValueId) {
    try {
      logger.info("Deleting document additional value id ${docValueId}")
      hid.execute('SI_DOCUMENTS_PKG.SD_DOC_VALUES_DEL', [
        num_N_DOC_VALUE_ID : docValueId
      ])
      logger.info("   Document additional value was deleted successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while deleting document additional value!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean deleteDocumentAddParam(Map input) {
    def docValueId = getDocumentAddParamBy(input)?.n_doc_value_id
    return deleteDocumentAddParam(docValueId)
  }

  Map getDocumentBind(def docDocumentId) {
    LinkedHashMap where = [
      n_doc_document_id: docDocumentId
    ]
    return hid.getTableFirst(getDocumentBindsTable(), where: where)
  }

  List getDocumentBindsBy(Map input) {
    LinkedHashMap params = mergeParams([
      docDocumentId : null,
      bindTypeId    : null,
      docId         : null,
      docBindId     : null,
      lineNumber    : null,
      limit         : 0
    ], input)
    LinkedHashMap where = [:]

    if (params.docDocumentId || params.bindId) {
      where.n_doc_document_id = params.docDocumentId ?: params.bindId
    }
    if (params.bindTypeId || params.docBindTypeId) {
      where.n_doc_bind_type_id = params.bindTypeId ?: params.docBindTypeId
    }
    if (params.docId) {
      where.n_doc_id = params.docId
    }
    if (params.docBindId || params.bindDocId) {
      where.n_doc_bind_id = params.docBindId ?: params.bindDocId
    }
    if (params.lineNumber) {
      where.n_line_no = params.lineNumber
    }
    return hid.getTableData(getDocumentBindsTable(), where: where, order: params.order, limit: params.limit)
  }

  Map getDocumentBindBy(Map input) {
    return getDocumentBindsBy(input + [limit: 1])?.getAt(0)
  }

  private Map putDocumentBind(Map input) {
    LinkedHashMap defaultParams = [
      docDocumentId : null,
      bindTypeId    : null,
      docId         : null,
      docBindId     : null,
      lineNumber    : null
    ]
    try {
      if (isEmpty(input.docDocumentId) && notEmpty(input.bindId)) {
        input.docDocumentId = input.bindId
      }
      if (isEmpty(input.bindTypeId) && notEmpty(input.docBindTypeId)) {
        input.bindTypeId = input.docBindTypeId
      }
      if (isEmpty(input.docBindId) && notEmpty(input.bindDocId)) {
        input.docBindId = input.bindDocId
      }

      if (notEmpty(input.docDocumentId)) {
        LinkedHashMap existBind = getDocumentBind(input.docDocumentId)
        defaultParams += [
          docDocumentId : existBind.n_doc_document_id,
          bindTypeId    : existBind.n_doc_bind_type_id,
          docId         : existBind.n_doc_id,
          docBindId     : existBind.n_doc_bind_id,
          lineNumber    : existBind.n_line_no
        ]
      }
      LinkedHashMap params = mergeParams(defaultParams, input)

      logger.info("${params.docDocumentId ? 'Updating' : 'Creating'} doc-doc bind with params ${params}")
      LinkedHashMap bind = hid.execute('SD_DOCUMENTS_PKG.SD_DOC_DOCUMENTS_PUT', [
        num_N_DOC_DOCUMENT_ID    : params.docDocumentId,
        num_N_DOC_BIND_TYPE_ID   : params.bindTypeId,
        num_N_DOC_ID             : params.docId,
        num_N_DOC_BIND_ID        : params.docBindId,
        num_N_LINE_NO            : params.lineNumber
      ])
      logger.info("   Doc-doc bind id ${bind.num_N_DOC_DOCUMENT_ID} was ${params.docDocumentId ? 'updated' : 'created'} successfully!")
      return bind
    } catch (Exception e){
      logger.error("   Error while ${input.docDocumentId ? 'updating' : 'creating'} new doc-doc bind!")
      logger.error_oracle(e)
      return null
    }
  }

  Map addDocumentBind(Map input = [:], def docId) {
    return putDocumentBind(input + [docId: docId])
  }

  Boolean deleteDocumentBind(def docDocumentId) {
    try {
      logger.info("Deleting doc-doc bind id ${docDocumentId}")
      hid.execute('SI_DOCUMENTS_PKG.SD_DOC_DOCUMENTS_DEL', [
        num_N_DOC_DOCUMENT_ID : docDocumentId
      ])
      logger.info("   Doc-doc bind was deleted successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while deleting doc-doc bind!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean deleteDocumentBind(Map input) {
    def docDocumentId = getDocumentBind(input)?.n_doc_document_id
    return deleteDocumentBind(docDocumentId)
  }

  Boolean changeDocumentState(
    def docId,
    def stateId
  ) {
    try {
      logger.info("Changing document ${docId} state to ${stateId}")
      hid.execute('SD_DOC_STATES_PKG.SD_DOCUMENTS_CHANGE_STATE', [
        num_N_DOC_ID           : docId,
        num_N_New_DOC_STATE_ID : stateId
      ])
      logger.info("   Document state was changed successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while changing document state!")
      logger.error_oracle(e)
      return false
    }
  }

  Map addDocumentTag(Map input) {
    input.entityId = input.docId
    input.remove('docId')
    return addEntityTag(input)
  }

  Map addDocumentTag(def docId, CharSequence tag) {
    return addDocumentTag(docId: docId, tag: tag)
  }

  Map addDocumentTag(Map input = [:], def docId) {
    return addDocumentTag(input + [docId: docId])
  }

  Boolean deleteDocumentTag(def docTagId) {
    return deleteEntityTag(docTagId)
  }

  Boolean deleteDocumentTag(Map input) {
    input.entityId = input.docId
    input.remove('docId')
    return deleteEntityTag(input)
  }

  Boolean deleteDocumentTag(def docId, CharSequence tag) {
    return deleteEntityTag(docId: docId, tag: tag)
  }

  Boolean actualizeDocument(def docId) {
    return changeDocumentState(docId, getDocumentStateActualId())
  }

  Boolean executeDocument(def docId) {
    return changeDocumentState(docId, getDocumentStateExecutedId())
  }

  Boolean cancelDocument(def docId) {
    return changeDocumentState(docId, getDocumentStateCanceledId())
  }

  Boolean closeDocument(def docId) {
    return changeDocumentState(docId, getDocumentStateClosedId())
  }

  Boolean dissolveDocument(def docId) {
    return changeDocumentState(docId, getDocumentStateDissolvedId())
  }

  Boolean refreshDocuments(CharSequence method = 'C') {
    return refreshMaterialView(getDocumentsMV(), method)
  }

  Boolean refreshDocumentAddParams(CharSequence method = 'C') {
    return refreshMaterialView(getDocumentAddParamsMV(), method)
  }
}