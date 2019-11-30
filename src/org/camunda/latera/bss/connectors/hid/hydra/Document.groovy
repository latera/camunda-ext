package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Oracle.encodeDateStr
import static org.camunda.latera.bss.utils.Oracle.encodeBool
import static org.camunda.latera.bss.utils.Oracle.decodeBool
import static org.camunda.latera.bss.utils.DateTimeUtil.dayBegin
import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import static org.camunda.latera.bss.utils.MapUtil.nvl

trait Document {
  private static String DOCUMENTS_TABLE                = 'SD_V_DOCUMENTS'
  private static String DOCUMENT_SUBJECTS_TABLE        = 'SI_V_DOC_SUBJECTS'
  private static String DOCUMENT_ADD_PARAMS_TABLE      = 'SD_V_DOC_VALUES'
  private static String DOCUMENT_ADD_PARAM_TYPES_TABLE = 'SS_V_WFLOW_DOC_VALUES_TYPE'
  private static String DOCUMENT_BINDS_TABLE           = 'SD_V_DOC_DOCUMENTS'
  private static String DOCUMENTS_MV                   = 'SD_MV_DOCUMENTS'
  private static String DOCUMENT_ADD_PARAMS_MV         = 'SD_MV_DOC_VALUES'
  private static String DOCUMENT_STATE_ACTUAL          = 'DOC_STATE_Actual'
  private static String DOCUMENT_STATE_EXECUTED        = 'DOC_STATE_Executed'
  private static String DOCUMENT_STATE_DRAFT           = 'DOC_STATE_Draft'
  private static String DOCUMENT_STATE_CANCELED        = 'DOC_STATE_Canceled'
  private static String DOCUMENT_STATE_CLOSED          = 'DOC_STATE_Closed'
  private static String DOCUMENT_STATE_DISSOLVED       = 'DOC_STATE_Dissolved'
  private static String DOCUMENT_STATE_PROCESSING      = 'DOC_STATE_Processing'
  private static String DOCUMENT_STATE_PREPARED        = 'DOC_STATE_Prepared'
  private static String PROVIDER_ROLE                  = 'SUBJ_ROLE_Provider'
  private static String RECEIVER_ROLE                  = 'SUBJ_ROLE_Receiver'
  private static String MEMBER_ROLE                    = 'SUBJ_ROLE_Member'
  private static String MANAGER_ROLE                   = 'SUBJ_ROLE_Manager'

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

  String getDocumentStateActual() {
    return DOCUMENT_STATE_ACTUAL
  }

  Number getDocumentStateActualId() {
    return getRefIdByCode(getDocumentStateActual())
  }

  String getDocumentStateExecuted() {
    return DOCUMENT_STATE_EXECUTED
  }

  Number getDocumentStateExecutedId() {
    return getRefIdByCode(getDocumentStateExecuted())
  }

  String getDocumentStateDraft() {
    return DOCUMENT_STATE_DRAFT
  }

  Number getDocumentStateDraftId() {
    return getRefIdByCode(getDocumentStateDraft())
  }

  String getDocumentStateCanceled() {
    return DOCUMENT_STATE_CANCELED
  }

  Number getDocumentStateCanceledId() {
    return getRefIdByCode(getDocumentStateCanceled())
  }

  String getDocumentStateClosed() {
    return DOCUMENT_STATE_CLOSED
  }

  Number getDocumentStateClosedId() {
    return getRefIdByCode(getDocumentStateClosed())
  }

  String getDocumentStateDissolved() {
    return DOCUMENT_STATE_DISSOLVED
  }

  Number getDocumentStateDissolvedId() {
    return getRefIdByCode(getDocumentStateDissolved())
  }

  String getDocumentStateProcessing() {
    return DOCUMENT_STATE_PROCESSING
  }

  Number getDocumentStateProcessingId() {
    return getRefIdByCode(getDocumentStateProcessing())
  }

  String getDocumentStatePrepared() {
    return DOCUMENT_STATE_PREPARED
  }

  Number getDocumentStatePreparedId() {
    return getRefIdByCode(getDocumentStatePrepared())
  }

  String getProviderRole() {
    return PROVIDER_ROLE
  }

  Number getProviderRoleId() {
    return getRefIdByCode(getProviderRole())
  }

  String getReceiverRole() {
    return RECEIVER_ROLE
  }

  Number getReceiverRoleId() {
    return getRefIdByCode(getReceiverRole())
  }

  String getMemberRole() {
    return MEMBER_ROLE
  }

  Number getMemberRoleId() {
    return getRefIdByCode(getMemberRole())
  }

  String getManagerRole() {
    return MANAGER_ROLE
  }

  Number getManagerRoleId() {
    return getRefIdByCode(getManagerRole())
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
      limit         : 0
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
      where.t_tags = params.tags
    }
    if (params.operationDate) {
      String oracleDate = encodeDateStr(params.operationDate)
      where[oracleDate] = [between: "d_begin and nvl(d_end, ${oracleDate})"]
    }
    LinkedHashMap order = [d_begin: 'desc', vc_doc_no: 'desc']
    return hid.getTableData(getDocumentsTable(), where: where, order: order, limit: params.limit)
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

  Boolean isDocument(CharSequence docType) {
    return docType.contains('DOC')
  }

  Boolean isDocument(def docIdOrDocTypeId) {
    return getRefCodeById(docIdOrDocTypeId)?.contains('DOC') || getDocument(docIdOrDocTypeId) != null
  }

  Map putDocument(Map input) {
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
      if (input.docId) {
        LinkedHashMap doc = getDocument(input.docId)
        defaultParams += [
          docTypeId   : doc.n_doc_type_id,
          workflowId  : doc.n_workflow_id,
          parentDocId : doc.n_parent_doc_id,
          reasonDocId : doc.n_reason_doc_id,
          prevDocId   : doc.n_prev_doc_Id,
          stornoDocId : doc.n_storno_doc_id,
          docDate     : doc.d_doc,
          docTime     : doc.d_time,
          number      : doc.vc_doc_no,
          name        : doc.vc_name,
          code        : doc.vc_code,
          rem         : doc.vc_rem,
          beginDate   : doc.d_begin,
          endDate     : doc.d_end,
          firmId      : doc.n_firm_id
        ]
      }
      LinkedHashMap params = mergeParams(defaultParams, input)

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
      logger.info("   Document was ${params.docId ? 'updated' : 'created'} successfully!")
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

  Map updateDocument(Map input) {
    return putDocument(input)
  }

  Map updateDocument(def docId, Map input) {
    return putDocument(input + [docId: docId])
  }

  Map updateDocument(Map input, def docId) {
    return updateDocument(docId, input)
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
      where.n_subject_id = params.docsubjectIdId
    }
    if (params.accountId) {
      where.n_account_id = params.accountId
    }
    return hid.getTableData(getDocumentSubjectsTable(), where: where, limit: params.limit)
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

  Map addDocumentSubject(Map input) {
    return putDocumentSubject(input)
  }

  Map addDocumentSubject(def docId, Map input) {
    return putDocumentSubject(input + [docId: docId])
  }

  Map addDocumentSubject(Map input, def docId) {
    return addDocumentSubject(docId, input)
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
    return hid.getTableData(getDocumentAddParamTypesTable(), where: where, limit: params.limit)
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
      docId   : null,
      paramId : null,
      date    : null,
      string  : null,
      number  : null,
      bool    : null,
      refId   : null,
      limit   : 0
    ], prepareDocumentAddParam(input))
    LinkedHashMap where = [:]

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
    return hid.getTableData(getDocumentAddParamsTable(), where: where, limit: params.limit)
  }

  Map getDocumentAddParamBy(Map input) {
    return getDocumentAddParamsBy(input + [limit: 1])?.getAt(0)
  }

  Map putDocumentAddParam(Map input) {
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

      logger.info("${params.docValueId ? 'Putting' : 'Creating'} document additional value with params ${params}")
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
      logger.info("   Document additional value was ${params.docValueId ? 'put' : 'created'} successfully!")
      return result
    } catch (Exception e){
      logger.error("   Error while putting or creating document additional value!")
      logger.error_oracle(e)
      return null
    }
  }

  Map addDocumentAddParam(Map input) {
    return putDocumentAddParam(input)
  }

  Map addDocumentAddParam(def docId, Map input) {
    return putDocumentAddParam(input + [docId: docId])
  }

  Map addDocumentAddParam(Map input, def docId) {
    return addDocumentAddParam(docId, input)
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

    if (params.docDocumentId || params.docBindId || params.bindId) {
      where.n_doc_document_id = params.docDocumentId ?: params.docBindId ?: params.bindId
    }
    if (params.bindTypeId || params.docBindTypeId) {
      where.n_doc_id = params.docId ?: params.docBindTypeId
    }
    if (params.docId) {
      where.n_doc_id = params.docId
    }
    if (params.docBindId) {
      where.n_doc_bind_id = params.docBindId
    }
    if (params.lineNumber) {
      where.n_line_no = params.lineNumber
    }
    return hid.getTableData(getDocumentBindsTable(), where: where, limit: params.limit)
  }

  Map getDocumentBindBy(Map input) {
    return getDocumentBindsBy(input + [limit: 1])?.getAt(0)
  }

  Map putDocumentBind(Map input) {
    LinkedHashMap params = mergeParams([
      docDocumentId : null,
      bindTypeId    : null,
      docId         : null,
      docBindId     : null,
      lineNumber    : null
    ], input)
    try {
      logger.info("Putting doc-doc bind with params ${params}")
      LinkedHashMap bind = hid.execute('SD_DOCUMENTS_PKG.SD_DOC_DOCUMENTS_PUT', [
        num_N_DOC_DOCUMENT_ID    : params.docDocumentId ?: params.docBindId ?: params.bindId,
        num_N_DOC_BIND_TYPE_ID   : params.bindTypeId    ?: params.docBindTypeId,
        num_N_DOC_ID             : params.docId,
        num_N_DOC_BIND_ID        : params.docBindId,
        num_N_LINE_NO            : params.lineNumber
      ])
      logger.info("   Doc-doc bind id ${bind.num_N_DOC_DOCUMENT_ID} was put successfully!")
      return bind
    } catch (Exception e){
      logger.error("   Error while putting new doc-doc bind!")
      logger.error_oracle(e)
      return null
    }
  }

  Map addDocumentBind(Map input) {
    return putDocumentBind(input)
  }

  Map addDocumentBind(def docId, Map input) {
    return putDocumentBind(input + [docId: docId])
  }

  Map addDocumentBind(Map input, def docId) {
    return putDocumentBind(docId, input)
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