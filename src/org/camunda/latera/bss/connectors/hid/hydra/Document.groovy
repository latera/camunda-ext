package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.utils.Oracle

trait Document {
  private static String DOCUMENTS_TABLE                = 'SD_V_DOCUMENTS'
  private static String DOCUMENT_SUBJECTS_TABLE        = 'SI_V_DOC_SUBJECTS'
  private static String DOCUMENT_ADD_PARAMS_TABLE      = 'SD_V_DOC_VALUES'
  private static String DOCUMENT_ADD_PARAM_TYPES_TABLE = 'SS_V_WFLOW_DOC_VALUES_TYPE'
  private static String DEFAULT_DOCUMENT_TYPE          = 'DOC_TYPE_CustomerContract'
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

  def getDocumentsTable() {
    return DOCUMENTS_TABLE
  }

  def getDocumentSubjectsTable() {
    return DOCUMENT_SUBJECTS_TABLE
  }

  def getDocumentAddParamsTable() {
    return DOCUMENT_ADD_PARAMS_TABLE
  }

  def getDocumentAddParamTypesTable() {
    return DOCUMENT_ADD_PARAM_TYPES_TABLE
  }

  def getDefaultDocumentType() {
    return DEFAULT_DOCUMENT_TYPE
  }

  def getDefaultDocumentTypeId() {
    return getRefIdByCode(getDefaultDocumentType())
  }

  def getDocumentStateActual() {
    return DOCUMENT_STATE_ACTUAL
  }

  def getDocumentStateActualId() {
    return getRefIdByCode(getDocumentStateActual())
  }

  def getDocumentStateExecuted() {
    return DOCUMENT_STATE_EXECUTED
  }

  def getDocumentStateExecutedId() {
    return getRefIdByCode(getDocumentStateExecuted())
  }

  def getDocumentStateDraft() {
    return DOCUMENT_STATE_DRAFT
  }

  def getDocumentStateDraftId() {
    return getRefIdByCode(getDocumentStateDraft())
  }

  def getDocumentStateCanceled() {
    return DOCUMENT_STATE_CANCELED
  }

  def getDocumentStateCanceledId() {
    return getRefIdByCode(getDocumentStateCanceled())
  }

  def getDocumentStateClosed() {
    return DOCUMENT_STATE_CLOSED
  }

  def getDocumentStateClosedId() {
    return getRefIdByCode(getDocumentStateClosed())
  }

  def getDocumentStateDissolved() {
    return DOCUMENT_STATE_DISSOLVED
  }

  def getDocumentStateDissolvedId() {
    return getRefIdByCode(getDocumentStateDissolved())
  }

  def getDocumentStateProcessing() {
    return DOCUMENT_STATE_PROCESSING
  }

  def getDocumentStateProcessingId() {
    return getRefIdByCode(getDocumentStateProcessing())
  }

  def getDocumentStatePrepared() {
    return DOCUMENT_STATE_PREPARED
  }

  def getDocumentStatePreparedId() {
    return getRefIdByCode(getDocumentStatePrepared())
  }

  def getProviderRole() {
    return PROVIDER_ROLE
  }

  def getProviderRoleId() {
    return getRefIdByCode(getProviderRole())
  }

  def getReceiverRole() {
    return RECEIVER_ROLE
  }

  def getReceiverRoleId() {
    return getRefIdByCode(getReceiverRole())
  }

  List getDocumentsBy(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      docId         : null,
      docTypeId     : getDefaultDocumentTypeId(),
      parentDocId   : null,
      reasonDocId   : null,
      workflowId    : null,
      providerId    : getFirmId(),
      receiverId    : null,
      stateId       : getDocumentStateActualId(),
      operationDate : null,
      beginDate     : null,
      endDate       : null,
      number        : null,
      tags          : null
    ], input)
    LinkedHashMap where = [:]

    if (params.docId) {
      where.n_doc_id = params.docId
    }
    if (params.docTypeId) {
      where.n_doc_type_id = params.docTypeId
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
    if (params.providerId) {
      where['_EXISTS'] = [
        """
        (SELECT 1
         FROM  ${getDocumentSubjectsTable()} DS
         WHERE DS.N_DOC_ID      = T.N_DOC_ID
         AND   DS.N_DOC_ROLE_ID = ${getProviderRoleId()}
         AND   DS.N_SUBJECT_ID  = ${params.providerId})"""
      ]
    }
    if (params.receiverId) {
      where['__EXISTS'] = [
        """
        (SELECT 1
         FROM  ${getDocumentSubjectsTable()} DS
         WHERE DS.N_DOC_ID      = T.N_DOC_ID
         AND   DS.N_DOC_ROLE_ID = ${getReceiverRoleId()}
         AND   DS.N_SUBJECT_ID  = ${params.receiverId})"""
      ]
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
    if (params.endDate) {
      where.d_end = params.endDate
    }
    if (params.tags) {
      where.t_tags = params.tags
    }
    if (params.operationDate) {
      String oracleDate = Oracle.encodeDateStr(params.operationDate)
      where[oracleDate] = [BETWEEN: "D_BEGIN AND NVL(D_END, ${oracleDate})"]
    }
    def order = [d_begin: 'asc', vc_doc_no: 'asc']
    return hid.getTableData(getDocumentsTable(), where: where, order: order)
  }

  LinkedHashMap getDocumentBy(LinkedHashMap input) {
    return getDocumentsBy(input)?.getAt(0)
  }

  LinkedHashMap getDocument(def docId) {
    def where = [
      n_doc_id: docId
    ]
    return hid.getTableData(getDocumentsTable(), where: where)
  }

  LinkedHashMap getDocumentTypeId(def docId) {
    def where = [
      n_doc_id: docId
    ]
    return hid.getTableData(getDocumentsTable(), 'n_doc_type_id', where)
  }

  def getDocumentWorkflowId(def docId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return hid.getTableFirst(getDocumentsTable(), 'n_workflow_id', where)
  }

  Boolean isDocument(String docType) {
    return entityType.contains('DOC')
  }

  Boolean isDocument(def docIdOrDocTypeId) {
    return getRefCodeById(docIdOrDocTypeId)?.contains('DOC') || getDocument(docIdOrDocTypeId) != null
  }

  Boolean putDocumentSubject(LinkedHashMap input) {
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

  LinkedHashMap addDocumentSubject(LinkedHashMap input) {
    return putDocumentSubject(input)
  }

  LinkedHashMap addDocumentSubject(def docId, LinkedHashMap input) {
    return putDocumentSubject(input + [docId: docId])
  }

  LinkedHashMap getDocumentAddParamType(def paramId) {
    def where = [
      n_doc_value_type_id: paramId
    ]
    return hid.getTableData(getDocumentAddParamTypesTable(), where: where)
  }

  LinkedHashMap getDocumentAddParamTypesBy(LinkedHashMap input) {
    def params = mergeParams([
      docValueTypeId  : null,
      docTypeId       : null,
      dataTypeId      : null,
      code            : null,
      name            : null,
      refTypeId       : null,
      canModify       : null,
      isMulti         : null,
      rem             : null
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
      where.c_can_modify = Oracle.encodeBool(params.canModify)
    }
    if (params.isMulti != null) {
      where.c_fl_multi = Oracle.encodeBool(params.isMulti)
    }
    return hid.getTableData(getDocumentAddParamTypesTable(), where: where)
  }

  LinkedHashMap getDocumentAddParamTypeBy(LinkedHashMap input) {
    return getDocumentAddParamTypesBy(input)?.getAt(0)
  }

  def getDocumentAddParamTypeByCode(String code, def docTypeId = null) {
    return getDocumentAddParamTypeBy(code: code, docTypeId: docTypeId)
  }

  def getDocumentAddParamTypeIdByCode(String code) {
    return getDocumentAddParamTypeByCode(code)?.n_doc_value_type_id
  }

  LinkedHashMap prepareDocumentAddParam(LinkedHashMap input) {
    def param = null
    if (input.containsKey('param')) {
      def docTypeId = input.docTypeId ?: getDocumentTypeId(input.docId)
      param = getDocumentAddParamTypeByCode(input.param.toString(), docTypeId)
      input.paramId = param?.n_doc_value_type_id
      input.remove('param')
    } else if (input.containsKey('paramId')) {
      param = getDocumentAddParamType(input.paramId)
    }
    input.isMultiple = Oracle.decodeBool(param.c_fl_multi)

    if (input.containsKey('value')) {
      def valueType = getAddParamDataType(param)
      input."${valueType}" = input.value
      input.remove('value')
    }
    return input
  }

  List getDocumentAddParamsBy(LinkedHashMap input) {
    def params = mergeParams([
      docId   : null,
      paramId : null,
      date    : null,
      string  : null,
      number  : null,
      bool    : null,
      refId   : null
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
      where.c_fl_value = Oracle.encodeBool(params.bool)
    }
    if (params.refId) {
      where.n_ref_id = params.refId
    }
    return hid.getTableData(getDocumentAddParamsTable(), where: where)
  }

  LinkedHashMap getDocumentAddParamBy(LinkedHashMap input) {
    return getDocumentAddParamsBy(input)?.getAt(0)
  }

  LinkedHashMap putDocumentAddParam(LinkedHashMap input) {
    def params = mergeParams([
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
      def result = hid.execute('SI_DOCUMENTS_PKG.SD_DOC_VALUES_PUT', [
        num_N_DOC_VALUE_ID       : params.docValueId,
        num_N_DOC_ID             : params.docId,
        num_N_DOC_VALUE_TYPE_ID  : params.paramId,
        dt_D_VALUE               : params.date,
        vch_VC_VALUE             : params.string,
        num_N_VALUE              : params.number,
        ch_C_FL_VALUE            : Oracle.encodeBool(params.bool),
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

  LinkedHashMap addDocumentAddParam(LinkedHashMap input) {
    return putDocumentAddParam(input)
  }

  LinkedHashMap addDocumentAddParam(def docId, LinkedHashMap input) {
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

  Boolean deleteDocumentAddParam(LinkedHashMap input) {
    def docValueId = getDocumentAddParamBy(input)?.n_doc_value_id
    return deleteDocumentAddParam(docValueId)
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
    changeDocumentState(docId, getDocumentStateActualId())
  }

  Boolean executeDocument(def docId) {
    changeDocumentState(docId, getDocumentStateExecutedId())
  }

  Boolean cancelDocument(def docId) {
    changeDocumentState(docId, getDocumentStateCanceledId())
  }

  Boolean closeDocument(def docId) {
    changeDocumentState(docId, getDocumentStateClosedId())
  }

  Boolean dissolveDocument(def docId) {
    changeDocumentState(docId, getDocumentStateDissolvedId())
  }
}