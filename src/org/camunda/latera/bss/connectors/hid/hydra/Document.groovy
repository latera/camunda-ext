package org.camunda.latera.bss.connectors.hid.hydra

trait Document {
  private static String DOCUMENTS_TABLE            = 'SD_V_DOCUMENTS'
  private static String DOCUMENT_VALUES_TABLE      = 'SD_V_DOC_VALUES'
  private static String DOCUMENT_VALUE_TYPES_TABLE = 'SS_V_WFLOW_DOC_VALUES_TYPE'
  private static String DEFAULT_DOCUMENT_TYPE      = 'DOC_TYPE_Contract'
  private static String DEFAULT_DOCUMENT_STATE     = 'DOC_STATE_Actual'

  def getDefaultDocumentType() {
    return getRefIdByCode(DEFAULT_DOCUMENT_TYPE)
  }

  def getDefaultDocumentState() {
    return getRefIdByCode(DEFAULT_DOCUMENT_STATE)
  }

  List getDocument(
    def docId,
    def docTypeId  = getDefaultDocumentType(),
    def docStateId = getDefaultDocumentState()
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
    return hid.getTableData(DOCUMENTS_TABLE, where: where)
  }

  List getDocument(
    LinkedHashMap options,
    def docId
  ) {
    LinkedHashMap params = mergeParams([
      docTypeId  : getDefaultDocumentType(),
      docStateId : getDefaultDocumentState()
    ], options)

    return getDocument(docId, params.docTypeId, params.docStateId)
  }

  List getDocument(
    LinkedHashMap input
  ) {
    LinkedHashMap params = mergeParams([
      docId      : null,
      docTypeId  : getDefaultDocumentType(),
      docStateId : getDefaultDocumentState()
    ], options)

    return getDocument(params, params.docId)
  }

  def getDocValueTypeIdByCode(String code) {
    LinkedHashMap where = [
      vc_code: code
    ]
    return hid.getTableFirst(DOCUMENT_VALUE_TYPES_TABLE, 'n_doc_value_type_id', where)
  }
}