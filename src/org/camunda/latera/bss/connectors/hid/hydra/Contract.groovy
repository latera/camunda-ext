package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.utils.DateTimeUtil
import org.camunda.latera.bss.utils.StringUtil
import org.camunda.latera.bss.utils.Oracle
import java.time.LocalDateTime

trait Contract {
  private static String  CONTRACTS_TABLE    = 'SD_V_CONTRACTS'
  private static String  CONTRACT_TYPE      = 'DOC_TYPE_SubscriberContract'
  private static String  CONTRACT_APP_TYPE  = 'DOC_TYPE_ContractAPP'
  private static String  ADD_AGREEMENT_TYPE = 'DOC_TYPE_AddAgreement'
  private static String  DEFAULT_CONTRACT_WORKFLOW         = 'WFLOW_SubscriberContract'
  private static Integer DEFAULT_CONTRACT_WORKFLOW_ID      = 10021
  private static String  DEFAULT_CONTRACT_APP_WORKFLOW     = 'WFLOW_ContractAPP'
  private static Integer DEFAULT_CONTRACT_APP_WORKFLOW_ID  = 20021
  private static String  DEFAULT_ADD_AGREEMENT_WORKFLOW    = 'WFLOW_AddAgreement'
  private static Integer DEFAULT_ADD_AGREEMENT_WORKFLOW_ID = 130021

 def getContractsTable() {
    return CONTRACTS_TABLE
  }

  def getContractType() {
    return CONTRACT_TYPE
  }

  def getContractTypeId() {
    return getRefIdByCode(CONTRACT_TYPE)
  }

  def getDefaultContractWorkflow() {
    return DEFAULT_CONTRACT_WORKFLOW
  }

  def getDefaultContractWorkflowId() {
    return DEFAULT_CONTRACT_WORKFLOW_ID
  }

  LinkedHashMap getContract(def docId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return hid.getTableFirst(getContractsTable(), where: where)
  }

  List getContractsBy(LinkedHashMap input) {
    input.docId = input.docId ?: input.contractId
    if (!input.docTypeId) {
      input.docTypeId = getContractTypeId()
    }
    return getDocumentsBy(input)
  }

  LinkedHashMap getContractBy(LinkedHashMap input) {
    input.docId = input.docId ?: input.contractId
    if (!input.docTypeId) {
      input.docTypeId = getContractTypeId()
    }
    return getDocumentBy(input)
  }

  Boolean isContract(String entityType) {
    return entityType == getContractType()
  }

  Boolean isContract(def entityIdOrEntityTypeId) {
    return entityIdOrEntityTypeId == getContractTypeId() || getDocument(docOrDocTypeId).n_doc_type_id == getContractTypeId()
  }

  LinkedHashMap putContract(LinkedHashMap input) {
    def defaultParams = [
      docId       : null,
      docTypeId   : getContractTypeId(),
      parentDocId : null,
      workflowId  : getDefaultContractWorkflowId(),
      providerId  : getFirmId(),
      receiverId  : null,
      stateId     : getDocumentStateActualId(),
      beginDate   : DateTimeUtil.now(),
      endDate     : null,
      number      : null
    ]
    LinkedHashMap params = mergeParams(defaultParams, input)
    params.docId         = params.docId ?: params.contractId
    params.parentDocId   = params.parentDocId ?: params.baseContractId

    def _docTypeName = ''
    if (params.docTypeId == getContractTypeId()) {
      _docTypeName = 'contract'
    } else if (params.docTypeId == getContractAppTypeId()) {
      _docTypeName = 'contract app'
    } else if (params.docTypeId == getAddAgreementTypeId()) {
      _docTypeName = 'add agreement'
    }
    def docTypeName = StringUtil.capitalize(_docTypeName)

    try {
      def paramsNames = (defaultParams.keySet() as List) - ['parentDocId', 'providerId', 'receiverId', 'number']
      LinkedHashMap contract = [:]
      if (params.subMap(paramsNames) == defaultParams.subMap(paramsNames) && StringUtil.isEmpty(params.number)) {
        logger.info("Creating new ${_docTypeName} with params ${params}")
        contract = hid.execute('SI_USERS_PKG.CREATE_CONTRACT', [
          num_N_USER_ID          : params.receiverId,
          num_N_FIRM_ID          : params.providerId,
          num_N_BASE_CONTRACT_ID : params.parentDocId,
          num_N_CONTRACT_ID      : null
        ])
        contract.num_N_DOC_ID = contract.num_N_CONTRACT_ID
        logger.info("   ${docTypeName} ${contract.num_N_DOC_ID} was put successfully!")
      } else {
        logger.info("Putting ${_docTypeName} with params ${params}")
        contract = hid.execute('SD_CONTRACTS_PKG.SD_CONTRACTS_PUT', [
          num_N_DOC_ID        : params.docId,
          num_N_DOC_TYPE_ID   : params.docTypeId,
          num_N_PARENT_DOC_ID : params.parentDocId,
          num_N_WORKFLOW_ID   : params.workflowId,
          dt_D_BEGIN          : params.beginDate,
          dt_D_END            : params.endDate,
          vch_VC_DOC_NO       : params.number
        ])
        logger.info("   ${docTypeName} ${contract.num_N_DOC_ID} was put successfully!")

        if (params.providerId) {
          putDocumentSubject([
            docId      : contract.num_N_DOC_ID,
            subjectId  : params.providerId,
            roleId     : getProviderRoleId(),
            workflowId : params.workflowId
          ])
        }
        if (params.receiverId) {
          putDocumentSubject([
            docId      : contract.num_N_DOC_ID,
            subjectId  : params.receiverId,
            roleId     : getReceiverRoleId(),
            workflowId : params.workflowId
          ])
        }
        actualizeDocument(contract.num_N_DOC_ID)
      }
      return contract
    } catch (Exception e){
      logger.error("   Error while putting ${_docTypeName}!")
      logger.error_oracle(e)
      return null
    }
  }

  LinkedHashMap createContract(LinkedHashMap input) {
    input.remove('docId')
    input.remove('contractId')
    return putContract(input)
  }

  LinkedHashMap updateContract(def docId, LinkedHashMap input) {
    return putContract(input + [docId: docId])
  }

  Boolean dissolveContract(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      docId         : null,
      endDate       : null,
      checkInvoices : false
    ], input)
    params.docId = params.docId ?: params.contractId
    try {
      logger.info("Dissolving contract id ${params.docId} with date ${params.endDate}")
      LinkedHashMap contract = hid.execute('SD_CONTRACTS_PKG.SD_CONTRACTS_DISSOLVE', [
        num_N_DOC_ID    : params.docId,
        dt_D_END        : params.endDate,
        b_CheckInvoices : Oracle.encodeFlag(params.checkInvoices)
      ])
      logger.info("   Contract ${contract.num_N_DOC_ID} was dissolved successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while dissolving contract!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean dissolveContract(def docId, LocalDateTime endDate = DateTimeUtil.now(), Boolean checkInvoices = false) {
    return dissolveContract(docId: docId, endDate: endDate, checkInvoices: checkInvoices)
  }

  def getContractAppsTable() {
    return getContractsTable()
  }

  def getContractAppType() {
    return CONTRACT_APP_TYPE
  }

  def getContractAppTypeId() {
    return getRefIdByCode(CONTRACT_APP_TYPE)
  }

  def getDefaultContractAppWorkflow() {
    return DEFAULT_CONTRACT_APP_WORKFLOW
  }

  def getDefaultContractAppWorkflowId() {
    return DEFAULT_CONTRACT_APP_WORKFLOW_ID
  }

  LinkedHashMap getContractApp(def docId) {
    return getContract(docId)
  }

  List getContractAppsBy(LinkedHashMap input) {
    input.docId = input.docId ?: input.contractId
    if (!input.docTypeId) {
      input.docTypeId = getContractAppTypeId()
    }
    input.receiverId = null
    input.providerId = null
    return getDocumentsBy(input)
  }

  LinkedHashMap getContractAppBy(LinkedHashMap input) {
    input.docId = input.docId ?: input.contractId
    if (!input.docTypeId) {
      input.docTypeId = getContractAppTypeId()
    }
    input.receiverId = null
    input.providerId = null
    return getDocumentBy(input)
  }

  Boolean isContractApp(String entityType) {
    return entityType == getContractAppType()
  }

  Boolean isContractApp(def entityIdOrEntityTypeId) {
    return entityIdOrEntityTypeId == getContractAppTypeId() || getDocument(docOrDocTypeId).n_doc_type_id == getContractAppTypeId()
  }

  LinkedHashMap putContractApp(LinkedHashMap input) {
    LinkedHashMap params = [
      docTypeId  : getContractAppTypeId(),
      workflowId : getDefaultContractAppWorkflowId()
    ] + input
    params.docId       = params.docId       ?: params.contractAppId
    params.parentDocId = params.parentDocId ?: params.contractId
    params.providerId  = null
    params.receiverId  = null
    params.remove('contractId')
    return putContract(params)
  }

  LinkedHashMap createContractApp(LinkedHashMap input) {
    input.remove('docId')
    input.remove('contractAppId')
    return putContractApp(input)
  }

  LinkedHashMap updateContractApp(def docId, LinkedHashMap input) {
    return putContractApp(input + [docId: docId])
  }

  Boolean dissolveContractApp(LinkedHashMap input) {
    return dissolveContract(input)
  }

  Boolean dissolveContractApp(def docId, LocalDateTime endDate = DateTimeUtil.now(), Boolean checkInvoices = false) {
    return dissolveContract(docId: docId, endDate: endDate, checkInvoices: checkInvoices)
  }

  def getAddAgreementsTable() {
    return getContractsTable()
  }

  def getAddAgreementType() {
    return ADD_AGREEMENT_TYPE
  }

  def getAddAgreementTypeId() {
    return getRefIdByCode(ADD_AGREEMENT_TYPE)
  }

  def getDefaultAddAgreementWorkflow() {
    return DEFAULT_ADD_AGREEMENT_WORKFLOW
  }

  def getDefaultAddAgreementWorkflowId() {
    return DEFAULT_ADD_AGREEMENT_WORKFLOW_ID
  }

  LinkedHashMap getAddAgreement(def docId) {
    return getContract(docId)
  }

  List getAddAgreementsBy(LinkedHashMap input) {
    input.docId = input.docId ?: input.contractId
    if (!input.docTypeId) {
      input.docTypeId = getAddAgreementTypeId()
    }
    input.receiverId = null
    input.providerId = null
    return getDocumentsBy(input)
  }

  LinkedHashMap getAddAgreementBy(LinkedHashMap input) {
    input.docId = input.docId ?: input.contractId
    if (!input.docTypeId) {
      input.docTypeId = getAddAgreementTypeId()
    }
    input.receiverId = null
    input.providerId = null
    return getDocumentBy(input)
  }

  Boolean isAddAgreement(String entityType) {
    return entityType == getAddAgreementType()
  }

  Boolean isAddAgreement(def entityIdOrEntityTypeId) {
    return entityIdOrEntityTypeId == getAddAgreementTypeId() || getDocument(docOrDocTypeId).n_doc_type_id == getAddAgreementTypeId()
  }

  LinkedHashMap putAddAgreement(LinkedHashMap input) {
    LinkedHashMap params = [
      docTypeId  : getAddAgreementTypeId(),
      workflowId : getDefaultAddAgreementWorkflowId()
    ] + input
    params.docId       = params.docId       ?: params.addAgreementId ?: params.agreementId
    params.parentDocId = params.parentDocId ?: params.contractId
    params.providerId  = null
    params.receiverId  = null
    params.remove('contractId')
    return putContract(params)
  }

  LinkedHashMap createAddAgreement(LinkedHashMap input) {
    input.remove('docId')
    input.remove('agreementId')
    input.remove('addAgreementId')
    return putAddAgreement(input)
  }

  LinkedHashMap updateAddAgreement(def docId, LinkedHashMap input) {
    return putAddAgreement(input + [docId: docId])
  }

  Boolean dissolveAddAgreement(LinkedHashMap input) {
    return dissolveContract(input)
  }

  Boolean dissolveAddAgreement(def docId, LocalDateTime endDate = DateTimeUtil.now(), Boolean checkInvoices = false) {
    return dissolveContract(docId: docId, endDate: endDate, checkInvoices: checkInvoices)
  }

  Boolean refreshContractsTree() {
    try {
      logger.info("Refreshing contracts tree")
      hid.execute('UTILS_PKG_S.REFRESH_MATERIALIZED_VIEW', [
        vch_VC_VIEW_NAME : 'SD_MV_CONTRACTS_TREE'
      ])
      logger.info("   Contracts tree was refreshed successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while refreshing contracts tree!")
      logger.error_oracle(e)
      return false
    }
  }
}