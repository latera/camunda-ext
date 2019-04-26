package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.utils.Oracle

trait Document {
  private static String DOCUMENTS_TABLE            = 'SD_V_DOCUMENTS'
  private static String DOCUMENT_SUBJECTS_TABLE    = 'SI_V_DOC_SUBJECTS'
  private static String DOCUMENT_VALUES_TABLE      = 'SD_V_DOC_VALUES'
  private static String DOCUMENT_VALUE_TYPES_TABLE = 'SS_V_WFLOW_DOC_VALUES_TYPE'
  private static String DEFAULT_DOCUMENT_TYPE      = 'DOC_TYPE_CustomerContract'
  private static String DOCUMENT_STATE_ACTUAL      = 'DOC_STATE_Actual'
  private static String DOCUMENT_STATE_EXECUTED    = 'DOC_STATE_Executed'
  private static String DOCUMENT_STATE_DRAFT       = 'DOC_STATE_Draft'
  private static String DOCUMENT_STATE_CANCELED    = 'DOC_STATE_Canceled'
  private static String DOCUMENT_STATE_CLOSED      = 'DOC_STATE_Closed'
  private static String DOCUMENT_STATE_DISSOLVED   = 'DOC_STATE_Dissolved'
  private static String DOCUMENT_STATE_PROCESSING  = 'DOC_STATE_Processing'
  private static String DOCUMENT_STATE_PREPARED    = 'DOC_STATE_Prepared'
  private static String PROVIDER_ROLE = 'SUBJ_ROLE_Provider'
  private static String RECEIVER_ROLE = 'SUBJ_ROLE_Receiver'

  def getDocumentsTable() {
    return DOCUMENTS_TABLE
  }

  def getDocumentSubjectsTable() {
    return DOCUMENT_SUBJECTS_TABLE
  }
  def getDocumentsValuesTable() {
    return DOCUMENT_VALUES_TABLE
  }

  def getDocumentsValueTypesTable() {
    return DOCUMENT_VALUE_TYPES_TABLE
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
    return getDocumentBy(docId: docId)
  }

  Boolean isDocument(String docType) {
    return entityType.contains('DOC')
  }

  Boolean isDocument(def docIdOrDocTypeId) {
    return getRefCodeById(docIdOrDocTypeId)?.contains('DOC') || getDocument(docIdOrDocTypeId) != null
  }

  def getDocumentValueTypeIdByCode(String code) {
    LinkedHashMap where = [
      vc_code: code
    ]
    return hid.getTableFirst(getDocumentsValueTypesTable(), 'n_doc_value_type_id', where)
  }

  def getDocValueTypeIdByCode(String code) {
    return getDocumentValueTypeIdByCode(code)
  }

  def getDocumentWorkflowId(def docId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return hid.getTableFirst(getDocumentsTable(), 'n_workflow_id', where)
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