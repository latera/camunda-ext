package org.camunda.latera.bss.connectors.hid.hydra

trait Document {
  private static String DOCUMENTS_TABLE            = 'SD_V_DOCUMENTS'
  private static String DOCUMENT_VALUES_TABLE      = 'SD_V_DOC_VALUES'
  private static String DOCUMENT_VALUE_TYPES_TABLE = 'SS_V_WFLOW_DOC_VALUES_TYPE'
  private static String DEFAULT_DOCUMENT_TYPE      = 'DOC_TYPE_Contract'
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

  List getDocument(
    def docId,
    def docTypeId = null,
    def docStateId = null
  ) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    if (docTypeId) {
      where.n_doc_type_id = docTypeId
    }
    if (docStateId) {
      where.n_doc_state_id = docStateId
    }
    return hid.getTableData(getDocumentsTable(), where: where)
  }

  List getDocument(
    LinkedHashMap options,
    def docId
  ) {
    LinkedHashMap params = mergeParams([
      docTypeId  : null,
      docStateId : null
    ], options)

    return getDocument(docId, params.docTypeId, params.docStateId)
  }

  List getDocument(
    LinkedHashMap input
  ) {
    LinkedHashMap params = mergeParams([
      docId      : null,
      docTypeId  : null,
      docStateId : null
    ], options)

    return getDocument(params, params.docId)
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
    return hid.getTableFirst(getDocumentsValueTypesTable(), 'n_workflow_id', where)
  }

  void putDocumentSubject(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      docId         :  null,
      subjectId     :  null,
      roleId        :  null,
      workflowId    :  null
    ], input)
    if (params.workflowId == null) {
      params.workflowId = getDocumentWorkflowId(params.docId)
    }
    try {
      logger.info("Putting subject ${params.subjectId} as role ${params.roleId} to document ${params.docId} with workflow ${workflowId}")
      hid.execute('SD_DOCUMENTS_PKG.PUT_DOC_SUBJECT', [
        num_N_DOC_ID      : docId,
        num_N_DOC_ROLE_ID : roleId,
        num_N_SUBJECT_ID  : subjectId,
        num_N_WORKFLOW_ID : workflowId
      ])
      logger.info("   Subject role was put successfully!")
    } catch (Exception e){
      logger.error("Error while putting subject role!")
      logger.error(e)
    }
  }

  void changeDocumentState(
    def docId,
    def docStateId
  ) {
    try {
      logger.info("Changing document ${docId} state to ${docStateId}")
      hid.execute('SD_DOC_STATES_PKG.SD_DOCUMENTS_CHANGE_STATE', [
        num_N_DOC_ID           : docId,
        num_N_New_DOC_STATE_ID : docStateId
      ])
      logger.info("   Document state was changed successfully!")
    } catch (Exception e){
      logger.error("Error while changing document state!")
      logger.error(e)
    }
  }

  void actualizeDocument(def docId) {
    changeDocumentState(docId, getDocumentStateActualId())
  }

  void executeDocument(def docId) {
    changeDocumentState(docId, getDocumentStateExecutedId())
  }

  void cancelDocument(def docId) {
    changeDocumentState(docId, getDocumentStateCanceledId())
  }

  void closeDocument(def docId) {
    changeDocumentState(docId, getDocumentStateClosedId())
  }

  void dissolveDocument(def docId) {
    changeDocumentState(docId, getDocumentStateDissolvedId())
  }
}