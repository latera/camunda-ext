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

  /**
   * Get documents table name
   */
  String getDocumentsTable() {
    return DOCUMENTS_TABLE
  }

  /**
   * Get documents-subjects binds table name
   */
  String getDocumentSubjectsTable() {
    return DOCUMENT_SUBJECTS_TABLE
  }

  /**
   * Get documents add param values table name
   */
  String getDocumentAddParamsTable() {
    return DOCUMENT_ADD_PARAMS_TABLE
  }

  /**
   * Get documents add param types table name
   */
  String getDocumentAddParamTypesTable() {
    return DOCUMENT_ADD_PARAM_TYPES_TABLE
  }

  /**
   * Get document-document binds table name
   */
  String getDocumentBindsTable() {
    return DOCUMENT_BINDS_TABLE
  }

  /**
   * Get documents quick search material view name
   */
  String getDocumentsMV() {
    return DOCUMENTS_MV
  }

  /**
   * Get documents app param values quick search material view name
   */
  String getDocumentAddParamsMV() {
    return DOCUMENT_ADD_PARAMS_MV
  }

  /**
   * Get document entity type ref code
   */
  String getDocumentEntityType() {
    return getRefCode(getDocumentEntityTypeId())
  }

  /**
   * Get document entity type ref id
   */
  Number getDocumentEntityTypeId() {
    return ENTITY_TYPE_Document
  }
  
  /**
   * Get document Actual state ref code
   */
  String getDocumentStateActual() {
    return getRefCode(getDocumentStateActualId())
  }

  /**
   * Get document Actual state ref id
   */
  Number getDocumentStateActualId() {
    return DOC_STATE_Actual
  }

  /**
   * Get document Executed state ref code
   */
  String getDocumentStateExecuted() {
    return getRefCode(getDocumentStateExecutedId())
  }

  /**
   * Get document Executed state ref id
   */
  Number getDocumentStateExecutedId() {
    return DOC_STATE_Executed
  }

  /**
   * Get document Draft state ref id
   */
  String getDocumentStateDraft() {
    return getRefCode(getDocumentStateDraftId())
  }

  /**
   * Get document Draft state ref id
   */
  Number getDocumentStateDraftId() {
    return DOC_STATE_Draft
  }

  /**
   * Get document Canceled state ref code
   */
  String getDocumentStateCanceled() {
    return getRefCode(getDocumentStateCanceledId())
  }

  /**
   * Get document Canceled state ref id
   */
  Number getDocumentStateCanceledId() {
    return DOC_STATE_Canceled
  }

  /**
   * Get document Closed state ref code
   */
  String getDocumentStateClosed() {
    return getRefCode(getDocumentStateClosedId())
  }

  /**
   * Get document Closed state ref id
   */
  Number getDocumentStateClosedId() {
    return DOC_STATE_Closed
  }

  /**
   * Get document Dissolved state ref code
   */
  String getDocumentStateDissolved() {
    return getRefCode(getDocumentStateDissolvedId())
  }

  /**
   * Get document Dissolved state ref id
   */
  Number getDocumentStateDissolvedId() {
    return DOC_STATE_Dissolved
  }

  /**
   * Get document Processing state ref code
   */
  String getDocumentStateProcessing() {
    return getRefCode(getDocumentStateProcessingId())
  }

  /**
   * Get document Processing state ref id
   */
  Number getDocumentStateProcessingId() {
    return DOC_STATE_Processing
  }

  /**
   * Get document Prepared state ref code
   */
  String getDocumentStatePrepared() {
    return getRefCode(getDocumentStatePreparedId())
  }

  /**
   * Get document Prepared state ref id
   */
  Number getDocumentStatePreparedId() {
    return DOC_STATE_Prepared
  }

  /**
   * Get document Provider role ref code
   */
  String getProviderRole() {
    return getRefCode(getProviderRoleId())
  }

  /**
   * Get document Provider role ref id
   */
  Number getProviderRoleId() {
    return SUBJ_ROLE_Provider
  }

  /**
   * Get document Receiver role ref code
   */
  String getReceiverRole() {
    return getRefCode(getReceiverRoleId())
  }

  /**
   * Get document Receiver role ref id
   */
  Number getReceiverRoleId() {
    return SUBJ_ROLE_Receiver
  }

  /**
   * Get document Member role ref code
   */
  String getMemberRole() {
    return getRefCode(getMemberRoleId())
  }

  /**
   * Get document Member role ref id
   */
  Number getMemberRoleId() {
    return SUBJ_ROLE_Member
  }

  /**
   * Get document Manager role ref code
   */
  String getManagerRole() {
    return getRefCode(getManagerRoleId())
  }

  /**
   * Get document Manager role ref id
   */
  Number getManagerRoleId() {
    return SUBJ_ROLE_Manager
  }

  /**
   * Get document by id
   * @param docId {@link java.math.BigInteger BigInteger}
   * @return Map with document table row or null
   */
  Map getDocument(def docId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return hid.getTableFirst(getDocumentsTable(), where: where)
  }

  /**
   * Generate SELECT for document-subject bind filtering
   * @param roleId {@link java.math.BigInteger BigInteger}. Subject role id
   * @param where  {@link LinkedHashMap Map} with WHERE clause. Optional, default: [:]
   * @param column {@link CharSequence String}. Optional, default: 'n_subject_id'
   * @return Map with document table row or null
   */
  private String subSelectForRole(Map inp = [:], def roleId) {
    LinkedHashMap pars = [
      where  : [:],
      column : 'n_subject_id'
    ] + inp
    pars.where.n_doc_id      = ['=': 'T.N_DOC_ID']
    pars.where.n_doc_role_id = roleId
    return hid.prepareTableQuery(getDocumentSubjectsTable(), fields: pars.column, where: pars.where, tableAlias: 'DS', asMap: false)
  }

  /**
   * Search for documents by different fields value
   * @param docId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docTypeId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docType       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parentDocId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param reasonDocId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param workflowId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param providerId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: current firm id
   * @param receiverId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param memberId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param managerId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param stateId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: not canceled
   * @param state         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param operationDate {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: current date time, but only if beginDate and endDate are not set
   * @param beginDate     {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param endDate       {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param number        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param tags          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit         {@link Integer}. Optional, default: 0 (unlimited)
   * @param order         {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: D_BEGIN ASC, VC_DOC_NO DESC
   * @return List[Map] of document table rows
   */
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

  /**
   * Search for one document by different fields value
   * @param docId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docTypeId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docType       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parentDocId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param reasonDocId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param workflowId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param providerId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: current firm id
   * @param receiverId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param memberId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param managerId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param stateId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: not canceled
   * @param state         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param operationDate {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: current date time, but only if beginDate and endDate are not set
   * @param beginDate     {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param endDate       {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param number        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param tags          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param order         {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: D_BEGIN ASC, VC_DOC_NO DESC
   * @return Map with document table rows
   */
  Map getDocumentBy(Map input) {
    return getDocumentsBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Get document type id
   * @param docId {@link java.math.BigInteger BigInteger}
   * @return Document type ref id
   */
  Number getDocumentTypeId(def docId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return toIntSafe(hid.getTableFirst(getDocumentsTable(), 'n_doc_type_id', where))
  }

  /**
   * Get document workflow id
   * @param docId {@link java.math.BigInteger BigInteger}
   * @return Document workflow id
   */
  Number getDocumentWorkflowId(def docId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return toIntSafe(hid.getTableFirst(getDocumentsTable(), 'n_workflow_id', where))
  }

  /**
   * Check if entity or entity type is document
   * @param entityOrEntityType {@link java.math.BigInteger BigInteger} or {@link CharSequence String}. Entity id, entity type ref id or entity type ref code
   * @return True if given value is document, false otherwise
   */
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

  /**
   * Create or update document
   * @param docId       {@link java.math.BigInteger BigInteger}. Optional
   * @param docTypeId   {@link java.math.BigInteger BigInteger}. Optional
   * @param docType     {@link CharSequence String}. Optional
   * @param workflowId  {@link java.math.BigInteger BigInteger}. Optional
   * @param parentDocId {@link java.math.BigInteger BigInteger}. Optional
   * @param reasonDocId {@link java.math.BigInteger BigInteger}. Optional
   * @param prevDocId   {@link java.math.BigInteger BigInteger}. Optional
   * @param stornoDocId {@link java.math.BigInteger BigInteger}. Optional
   * @param docDate     {@link java.time.Temporal Any date type}. Optional
   * @param docTime     {@link java.time.Temporal Any date type}. Optional
   * @param number      {@link CharSequence String}. Optional
   * @param name        {@link CharSequence String}. Optional
   * @param code        {@link CharSequence String}. Optional
   * @param rem         {@link CharSequence String}. Optional
   * @param beginDate   {@link java.time.Temporal Any date type}. Optional
   * @param endDate     {@link java.time.Temporal Any date type}. Optional
   * @param firmId      {@link java.math.BigInteger BigInteger}. Optional, default: current firm id
   * @return Map with created or updated document (in Oracle API procedure notation)
   */
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
        existingDocument += [
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

  /**
   * Ureate or update document
   * @param docTypeId   {@link java.math.BigInteger BigInteger}. Optional
   * @param docType     {@link CharSequence String}. Optional
   * @param workflowId  {@link java.math.BigInteger BigInteger}. Optional
   * @param parentDocId {@link java.math.BigInteger BigInteger}. Optional
   * @param reasonDocId {@link java.math.BigInteger BigInteger}. Optional
   * @param prevDocId   {@link java.math.BigInteger BigInteger}. Optional
   * @param stornoDocId {@link java.math.BigInteger BigInteger}. Optional
   * @param docDate     {@link java.time.Temporal Any date type}. Optional
   * @param docTime     {@link java.time.Temporal Any date type}. Optional
   * @param number      {@link CharSequence String}. Optional
   * @param name        {@link CharSequence String}. Optional
   * @param code        {@link CharSequence String}. Optional
   * @param rem         {@link CharSequence String}. Optional
   * @param beginDate   {@link java.time.Temporal Any date type}. Optional
   * @param endDate     {@link java.time.Temporal Any date type}. Optional
   * @param firmId      {@link java.math.BigInteger BigInteger}. Optional, default: current firm id
   * @return Map with created document (in Oracle API procedure notation)
   */
  Map createDocument(Map input) {
    input.remove('docId')
    return putDocument(input)
  }

  /**
   * Update document
   * @param docId       {@link java.math.BigInteger BigInteger}
   * @param docTypeId   {@link java.math.BigInteger BigInteger}. Optional
   * @param docType     {@link CharSequence String}. Optional
   * @param workflowId  {@link java.math.BigInteger BigInteger}. Optional
   * @param parentDocId {@link java.math.BigInteger BigInteger}. Optional
   * @param reasonDocId {@link java.math.BigInteger BigInteger}. Optional
   * @param prevDocId   {@link java.math.BigInteger BigInteger}. Optional
   * @param stornoDocId {@link java.math.BigInteger BigInteger}. Optional
   * @param docDate     {@link java.time.Temporal Any date type}. Optional
   * @param docTime     {@link java.time.Temporal Any date type}. Optional
   * @param number      {@link CharSequence String}. Optional
   * @param name        {@link CharSequence String}. Optional
   * @param code        {@link CharSequence String}. Optional
   * @param rem         {@link CharSequence String}. Optional
   * @param beginDate   {@link java.time.Temporal Any date type}. Optional
   * @param endDate     {@link java.time.Temporal Any date type}. Optional
   * @param firmId      {@link java.math.BigInteger BigInteger}. Optional, default: current firm id
   * @return Map with created document (in Oracle API procedure notation)
   */
  Map updateDocument(Map input = [:], def docId) {
    return putDocument(input + [docId: docId])
  }

  /**
   * Get document-subject bind by id
   * @param docSubjectId {@link java.math.BigInteger BigInteger}
   * @return Map with document-subject bin table row or null
   */
  Map getDocumentSubject(def docSubjectId) {
    LinkedHashMap where = [
      n_doc_subject_id: docSubjectId
    ]
    return hid.getTableFirst(getDocumentSubjectsTable(), where: where)
  }

  /**
   * Search for document-subject binds by different fields value
   * @param docSubjectId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param roleId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param role         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subjectId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param accountId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param accountId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit          {@link Integer}. Optional, default: 0 (unlimited)
   * @return List[Map] of document-subject bind table rows
   */
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

  /**
   * Search for one document-subject bind by different fields value
   * @param docSubjectId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param roleId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param role         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subjectId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param accountId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param accountId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @return Map with document-subject bind table row
   */
  Map getDocumentSubjectBy(Map input) {
    return getDocumentSubjectsBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Search for provider document-subject bind by different fields value
   * @see #getDocumentSubjectBy(Map)
   */
  Map getDocumentProviderBy(Map input) {
    return getDocumentSubjectBy(input + [roleId: getProviderRoleId()])
  }

  /**
   * Get provider for document by doc id
   * @param docId {@link java.math.BigInteger BigInteger}
   * @return Map with document-subject bind table row
   */
  Map getDocumentProvider(def docId) {
    return getDocumentProviderBy(docId: docId)
  }

  /**
   * Search for receiver document-subject bind by different fields value
   * @see #getDocumentSubjectBy(Map)
   */
  Map getDocumentReceiverBy(Map input) {
    return getDocumentSubjectBy(input + [roleId: getReceiverRoleId()])
  }

  /**
   * Get receiver for document by doc id
   * @param docId {@link java.math.BigInteger BigInteger}
   * @return Map with document-subject bind table row
   */
  Map getDocumentReceiver(def docId) {
    return getDocumentReceiverBy(docId: docId)
  }

  /**
   * Search for member document-subject bind by different fields value
   * @see #getDocumentSubjectBy(Map)
   */
  Map getDocumentMemberBy(Map input) {
    return getDocumentSubjectBy(input + [roleId: getMemberRoleId()])
  }

  /**
   * Get member for document by doc id
   * @param docId {@link java.math.BigInteger BigInteger}
   * @return Map with document-subject bind table row
   */
  Map getDocumentMember(def docId) {
    return getDocumentMemberBy(docId: docId)
  }

  /**
   * Search for manager document-subject bind by different fields value
   * @see #getDocumentSubjectBy(Map)
   */
  Map getDocumentManagerBy(Map input) {
    return getDocumentSubjectBy(input + [roleId: getManagerRoleId()])
  }

  /**
   * Get manager for document by doc id
   * @param docId {@link java.math.BigInteger BigInteger}
   * @return Map with document-subject bind table row
   */
  Map getDocumentManager(def docId) {
    return getDocumentManagerBy(docId: docId)
  }

  /**
   * Create document-subject bind
   * @param docId      {@link java.math.BigInteger BigInteger}
   * @param subjectId  {@link java.math.BigInteger BigInteger}
   * @param roleId     {@link java.math.BigInteger BigInteger}. Optional if 'role' is passed
   * @param role       {@link CharSequence String}. Optional if 'roleId' is passed
   * @param workflowId {@link java.math.BigInteger BigInteger}. Optional if document exist, mandatory if not
   * @return Map with created document-subject bind (in Oracle API procedure notation)
   * @deprecated use {@link #addDocumentSubject(Map,def)}
   */
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

  /**
   * Create document-subject bind
   * @param docId      {@link java.math.BigInteger BigInteger}
   * @param subjectId  {@link java.math.BigInteger BigInteger}
   * @param roleId     {@link java.math.BigInteger BigInteger}. Optional if 'role' is passed
   * @param role       {@link CharSequence String}. Optional if 'roleId' is passed
   * @param workflowId {@link java.math.BigInteger BigInteger}. Optional if document exist, mandatory if not
   * @return Map with created document-subject bind (in Oracle API procedure notation)
   */
  Boolean addDocumentSubject(Map input = [:], def docId) {
    return putDocumentSubject(input + [docId: docId])
  }

  /**
   * Create document-subject bind
   *
   * Overload with positional args
   * @see #addDocumentSubject(Map, def)
   */
  Boolean addDocumentSubject(Map input = [:], def docId, def subjectId, def accountId = null) {
    return putDocumentSubject(input + [docId: docId, subjectId: subjectId, accountId: accountId])
  }

  /**
   * Create document-subject provider
   * @see #addDocumentSubject(Map, def, def)
   */
  Boolean addDocumentProvider(Map input = [:], def docId, def subjectId, def accountId = null) {
    return putDocumentSubject(input + [roleId: getProviderRoleId()], docId, subjectId, accountId)
  }

  /**
   * Create document-subject receiver
   * @see #addDocumentSubject(Map, def, def)
   */
  Boolean addDocumentReceiver(Map input = [:], def docId, def subjectId, def accountId = null) {
    return putDocumentSubject(input + [roleId: getReceiverRoleId()], docId, subjectId, accountId)
  }

  /**
   * Create document-subject member
   * @see #addDocumentSubject(Map, def, def)
   */
  Boolean addDocumentMember(Map input = [:], def docId, def subjectId, def accountId = null) {
    return putDocumentSubject(input + [roleId: getMemberRoleId()], docId, subjectId, accountId)
  }

  /**
   * Create document-subject manager
   * @see #addDocumentSubject(Map, def, def)
   */
  Boolean addDocumentManager(Map input = [:], def docId, def subjectId, def accountId = null) {
    return putDocumentSubject(input + [roleId: getManagerRoleId()], docId, subjectId, accountId)
  }

  /**
   * Get document add param type by id
   * @param docValueTypeId {@link java.math.BigInteger BigInteger}
   * @return Map with document add param table row or null
   */
  Map getDocumentAddParamType(def docValueTypeId) {
    LinkedHashMap where = [
      n_doc_value_type_id: docValueTypeId
    ]
    return hid.getTableFirst(getDocumentAddParamTypesTable(), where: where)
  }

  /**
   * Search for document add param types by different fields value
   * @param docValueTypeId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docTypeId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docType        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param dataTypeId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param dataType       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refTypeId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refType        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param canModify      {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isMulti        {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param rem            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit          {@link Integer}. Optional, default: 0 (unlimited)
   * @param order          {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: [:]
   * @return List[Map] of document add param type table rows
   */
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

  /**
   * Search for one document add param type by different fields value
   * @param docValueTypeId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docTypeId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docType        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param dataTypeId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param dataType       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refTypeId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refType        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param canModify      {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isMulti        {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param rem            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param order          {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: [:]
   * @return Mp with document add param type table row
   */
  Map getDocumentAddParamTypeBy(Map input) {
    return getDocumentAddParamTypesBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Get document add param type by code
   * @param code      {@link CharSequence String}
   * @param docTypeId {@link java.math.BigInteger BigInteger}. Optional
   * @return Map with document add param type table row
   */
  Map getDocumentAddParamTypeByCode(CharSequence code, def docTypeId = null) {
    return getDocumentAddParamTypeBy(code: code, docTypeId: docTypeId)
  }

  /**
   * Get document add param type id by code
   * @param code      {@link CharSequence String}
   * @param docTypeId {@link java.math.BigInteger BigInteger}. Optional
   * @return Document add param type id
   * @deprecated
   */
  Number getDocumentAddParamTypeIdByCode(CharSequence code) {
    return toIntSafe(getDocumentAddParamTypeByCode(code)?.n_doc_value_type_id)
  }

  /**
   * Prepare document add param value to save
   * @param paramId   {@link java.math.BigInteger BigInteger}. Optional if 'param' is passed
   * @param param     {@link CharSequence String}. Optional is 'paramId' is passed
   * @param docId     {@link java.math.BigInteger BigInteger}. Existing document id to find add param type. Optional
   * @param docTypeId {@link java.math.BigInteger BigInteger}. Doc type if to find add param type. Optional
   * @param value     Any type. Optional
   * @return Map with add param value
   * <pre>
   * {@code
   * [
   *   paramId : _, # doc add param type id
   *   bool    : _, # if add param is boolean type
   *   number  : _, # if add param is number type
   *   string  : _, # if add param is string type
   *   date    : _, # if add param is date type
   *   refId   : _, # if add param is refId type and value can be converted to BigInteger (ref id)
   *   ref     : _  # if add param is refId type and value cannot be converted to BigInteger (ref code)
   * ]
   * }
   * </pre>
   */
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

  /**
   * Search for document add param values by different fields value
   * @param docValueId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docTypeId  {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param paramId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional is 'param' is passed
   * @param param      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional is 'paramId' is passed
   * @param date       {@link java.time.Temporal Any date type}. Optional
   * @param number     {@link Double}, {@link Integer}, {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param string     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bool       {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ref        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param value      Any type which is automatically converted to 'date', 'string', 'name', 'bool' or 'refId', see {@link #prepareDocumentAddParam(Map)}. Optional
   * @param limit      {@link Integer}. Optional, default: 0 (unlimited)
   * @param order      {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: [:]
   * @return List[Map] of document add param value table rows
   */
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

  /**
   * Search for one document add param value by different fields value
   * @param docValueId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docTypeId  {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param paramId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional is 'param' is passed
   * @param param      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional is 'paramId' is passed
   * @param date       {@link java.time.Temporal Any date type}. Optional
   * @param number     {@link Double}, {@link Integer}, {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param string     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bool       {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ref        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param value      Any type which is automatically converted to 'date', 'string', 'name', 'bool' or 'refId', see {@link #prepareDocumentAddParam(Map)}. Optional
   * @param order      {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: [:]
   * @return Map with document add param value table row
   */
  Map getDocumentAddParamBy(Map input) {
    return getDocumentAddParamsBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Create or update document add param value
   * @param docValueId {@link java.math.BigInteger BigInteger}. Optional
   * @param docId      {@link java.math.BigInteger BigInteger}. Optional
   * @param paramId    {@link java.math.BigInteger BigInteger}. Optional
   * @param param      {@link CharSequence String}. Optional
   * @param date       {@link java.time.Temporal Any date type}. Optional
   * @param number     {@link Double}, {@link Integer}, {@link java.math.BigInteger BigInteger}. Optional
   * @param string     {@link CharSequence String}. Optional
   * @param bool       {@link Boolean}. Optional
   * @param refId      {@link java.math.BigInteger BigInteger}. Optional
   * @param ref        {@link CharSequence String}. Optional
   * @param value      Any type which is automatically converted to 'date', 'string', 'name', 'bool' or 'refId', see {@link #prepareDocumentAddParam(Map)}. Optional
   * @return Map with created or updated document add param value (in Oracle API procedure notation)
   */
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

  /**
   * Create or update document add param value
   * @param docId   {@link java.math.BigInteger BigInteger}
   * @param paramId {@link java.math.BigInteger BigInteger}. Optional
   * @param param   {@link CharSequence String}. Optional
   * @param date    {@link java.time.Temporal Any date type}. Optional
   * @param number  {@link Double}, {@link Integer}, {@link java.math.BigInteger BigInteger}. Optional
   * @param string  {@link CharSequence String}. Optional
   * @param bool    {@link Boolean}. Optional
   * @param refId   {@link java.math.BigInteger BigInteger}. Optional
   * @param ref     {@link CharSequence String}. Optional
   * @param value   Any type which is automatically converted to 'date', 'string', 'name', 'bool' or 'refId', see {@link #prepareDocumentAddParam(Map)}. Optional
   * @return Map with created or updated document add param value (in Oracle API procedure notation)
   */
  Map addDocumentAddParam(Map input = [:], def docId) {
    return putDocumentAddParam(input + [docId: docId])
  }

  /**
   * Delete document add param value
   * @param docValueId {@link java.math.BigInteger BigInteger}
   * @return True if document add param value was deleted successfully, false otherwise
   */
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

  /**
   * Delete document add param value
   *
   * Overload for searching and deleting add param value
   * @see #getDocumentAddParamBy(Map)
   * @see #deleteDocumentAddParam(def)
   */
  Boolean deleteDocumentAddParam(Map input) {
    def docValueId = getDocumentAddParamBy(input)?.n_doc_value_id
    return deleteDocumentAddParam(docValueId)
  }

  /**
   * Get document-document bind by id
   * @param docDocumentId {@link java.math.BigInteger BigInteger}
   * @return Map with document-document bind table row or null
   */
  Map getDocumentBind(def docDocumentId) {
    LinkedHashMap where = [
      n_doc_document_id: docDocumentId
    ]
    return hid.getTableFirst(getDocumentBindsTable(), where: where)
  }

  /**
   * Search for document-document binds by different fields value
   * @param docDocumentId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bindTypeId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bindType      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docBindId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lineNumber    {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit          {@link Integer}. Optional, default: 0 (unlimited)
   * @param order          {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: [:]
   * @return List[Map] of document-document bind table rows
   */
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

  /**
   * Search for one document-document bind by different fields value
   * @param docDocumentId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bindTypeId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bindType      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docBindId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lineNumber    {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param order          {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: [:]
   * @return Map with of document-document bind table row
   */
  Map getDocumentBindBy(Map input) {
    return getDocumentBindsBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Create or update document-document bind
   * @param docDocumentId {@link java.math.BigInteger BigInteger}. Optional
   * @param bindTypeId    {@link java.math.BigInteger BigInteger}. Optional
   * @param bindType      {@link CharSequence String}. Optional
   * @param docId         {@link java.math.BigInteger BigInteger}. Optional
   * @param docBindId     {@link java.math.BigInteger BigInteger}. Optional
   * @param lineNumber    {@link Integer}. Optional
   * @return Map with created or updated document-document bind (in Oracle API procedure notation)
   */
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

  /**
   * Create document-document bind
   * @param docId      {@link java.math.BigInteger BigInteger}
   * @param docBindId  {@link java.math.BigInteger BigInteger}. Optional
   * @param bindTypeId {@link java.math.BigInteger BigInteger}. Optional
   * @param bindType   {@link CharSequence String}. Optional
   * @param lineNumber {@link Integer}. Optional
   * @return Map with created document-document bind (in Oracle API procedure notation)
   */
  Map addDocumentBind(Map input = [:], def docId) {
    return putDocumentBind(input + [docId: docId])
  }

  /**
   * Create document-document bind
   * @param docId      {@link java.math.BigInteger BigInteger}
   * @param docBindId  {@link java.math.BigInteger BigInteger}
   * @param bindTypeId {@link java.math.BigInteger BigInteger}. Optional
   * @param bindType   {@link CharSequence String}. Optional
   * @param lineNumber {@link Integer}. Optional
   * @return Map with created document-document bind (in Oracle API procedure notation)
   */
  Map addDocumentBind(Map input = [:], def docId, def docBindId) {
    return putDocumentBind(input + [docId: docId, docBindId: docBindId])
  }

  /**
   * Delete document-document bind
   * @param docDocumentId {@link java.math.BigInteger BigInteger}
   * @return True if document-document bind was deleted successfully, false otherwise
   */
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

  /**
   * Delete document-document bind
   *
   * Overload for searching and deleting document-document bind
   * @see #getDocumentBindBy(Map)
   * @see #deleteDocumentBind(def)
   */
  Boolean deleteDocumentBind(Map input) {
    def docDocumentId = getDocumentBind(input)?.n_doc_document_id
    return deleteDocumentBind(docDocumentId)
  }

  /**
   * Change document state
   * @param docId   {@link java.math.BigInteger BigInteger}
   * @param stateId {@link java.math.BigInteger BigInteger}
   * @return True if document state was changed successfully, false otherwise
   */
  Boolean changeDocumentState(def docId, def stateId) {
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

  /**
   * Change document state
   *
   * Overload with state code instead of id
   * @param docId {@link java.math.BigInteger BigInteger}
   * @param state {@link CharSequence String}
   * @see @deleteDocumentBind(def,def)
   */
  Boolean changeDocumentState(def docId, CharSequence state) {
    return changeDocumentState(docId, getRefIdByCode(state))
  }

  /**
   * Add tag to document
   * @param docId {@link java.math.BigInteger BigInteger}
   * @param tagId {@link java.math.BigInteger BigInteger}. Optional if 'tag' is pased
   * @param tag   {@link CharSequence String}. Optional if 'tagId' is pased
   * @return True if document tag was added successfully, false otherwise
   */
  Map addDocumentTag(Map input) {
    input.entityId = input.docId
    input.remove('docId')
    return addEntityTag(input)
  }

  /**
   * Add tag to document
   *
   * Overload with tag code instead of id
   * @param docId {@link java.math.BigInteger BigInteger}
   * @param tag   {@link CharSequence String}
   * @see @addDocumentTag(Map)
   */
  Map addDocumentTag(def docId, CharSequence tag) {
    return addDocumentTag(docId: docId, tag: tag)
  }

  /**
   * Add tag to document
   *
   * Overload with mandatory doc id arg
   * @param docId {@link java.math.BigInteger BigInteger}
   * @param tagId {@link java.math.BigInteger BigInteger}. Optional if 'tag' is pased
   * @param tag   {@link CharSequence String}. Optional if 'tagId' is pased
   * @see @addDocumentTag(Map,def)
   */
  Map addDocumentTag(Map input = [:], def docId) {
    return addDocumentTag(input + [docId: docId])
  }

  /**
   * Delete tag from document
   * @param docTagId {@link java.math.BigInteger BigInteger}
   * @return True if document tag was deleted successfully, false otherwise
   */
  Boolean deleteDocumentTag(def docTagId) {
    return deleteEntityTag(docTagId)
  }

  /**
   * Delete tag from document
   *
   * Overload with named args input
   * @param docId {@link java.math.BigInteger BigInteger}
   * @param tagId {@link java.math.BigInteger BigInteger}. Optional if 'tag' is pased
   * @param tag   {@link CharSequence String}. Optional if 'tagId' is pasednal
   * @see @deleteDocumentTag(def)
   */
  Boolean deleteDocumentTag(Map input) {
    input.entityId = input.docId
    input.remove('docId')
    return deleteEntityTag(input)
  }

  /**
   * Delete tag from document
   *
   * Overload with doc id and tag code
   * @param docId {@link java.math.BigInteger BigInteger}
   * @param tag   {@link CharSequence String}
   * @see @deleteDocumentTag(Map)
   */
  Boolean deleteDocumentTag(def docId, CharSequence tag) {
    return deleteEntityTag(docId: docId, tag: tag)
  }

  /**
   * Change document state to Actual
   * @param docId {@link java.math.BigInteger BigInteger}
   * @return True if document state was chanched successfully, false otherwise
   */
  Boolean actualizeDocument(def docId) {
    return changeDocumentState(docId, getDocumentStateActualId())
  }

  /**
   * Change document state to Executed
   * @param docId {@link java.math.BigInteger BigInteger}
   * @return True if document state was chanched successfully, false otherwise
   */
  Boolean executeDocument(def docId) {
    return changeDocumentState(docId, getDocumentStateExecutedId())
  }

  /**
   * Change document state to Cancel
   * @param docId {@link java.math.BigInteger BigInteger}
   * @return True if document state was chanched successfully, false otherwise
   */
  Boolean cancelDocument(def docId) {
    return changeDocumentState(docId, getDocumentStateCanceledId())
  }

  /**
   * Change document state to Closed
   * @param docId {@link java.math.BigInteger BigInteger}
   * @return True if document state was chanched successfully, false otherwise
   */
  Boolean closeDocument(def docId) {
    return changeDocumentState(docId, getDocumentStateClosedId())
  }

  /**
   * Change document state to Dissolved
   * @param docId {@link java.math.BigInteger BigInteger}
   * @return True if document state was chanched successfully, false otherwise
   */
  Boolean dissolveDocument(def docId) {
    return changeDocumentState(docId, getDocumentStateDissolvedId())
  }

  /**
   * Refresh documents quick search material view
   * @see #refreshMaterialView(CharSequence,CharSequence)
   * @return True if quick search was updated successfully, false otherwise
   */
  Boolean refreshDocuments(CharSequence method = 'C') {
    return refreshMaterialView(getDocumentsMV(), method)
  }

  /**
   * Refresh documents app params quick search material view
   * @see #refreshMaterialView(CharSequence,CharSequence)
   * @return True if quick search was updated successfully, false otherwise
   */
  Boolean refreshDocumentAddParams(CharSequence method = 'C') {
    return refreshMaterialView(getDocumentAddParamsMV(), method)
  }
}