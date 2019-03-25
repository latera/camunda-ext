package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.utils.DateTimeUtil
import org.camunda.latera.bss.utils.Oracle
import java.time.LocalDateTime

trait Contract {
  private static String CONTRACTS_TABLE = 'SD_V_CONTRACTS'
  private static String CONTRACT_TYPE   = 'DOC_TYPE_SubscriberContract'
  private static String DEFAULT_CONTRACT_WORKFLOW = 'WFLOW_SubscriberContract'

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
    return getRefIdByCode(DEFAULT_CONTRACT_WORKFLOW)
  }

  LinkedHashMap getContract(def docId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return hid.getTableFirst(getContractsTable(), where: where)
  }

  List getContractsBy(LinkedHashMap input) {
    if (!input.docTypeId) {
      input.docTypeId = getContractTypeId()
    }
    return getDocumentsBy(input)
  }

  LinkedHashMap getContractBy(LinkedHashMap input) {
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
    LinkedHashMap params = mergeParams([
      docId       : null,
      parentDocId : null,
      reasonDocId : null,
      workflowId  : getDefaultContractWorkflowId(),
      providerId  : getFirmId(),
      receiverId  : null,
      stateId     : getDocumentStateActualId(),
      beginDate   : DateTimeUtil.now(),
      endDate     : null,
      number      : null
    ], input)
    try {
      logger.info("Putting contract with number ${params.number} parent ${params.parentDocId}, begin date ${params.beginDate} and end date ${params.endDate}")
      LinkedHashMap contract = hid.execute('SD_CONTRACTS_PKG.SD_CONTRACTS_PUT', [
        num_N_DOC_ID        : params.docId,
        num_N_DOC_TYPE_ID   : getContractTypeId(),
        num_N_PARENT_DOC_ID : params.parentDocId,
        num_N_REASON_DOC_ID : params.reasonDocId,
        num_N_WORKFLOW_ID   : params.workflowId,
        dt_D_BEGIN          : params.beginDate,
        dt_D_END            : params.endDate,
        vch_VC_DOC_NO       : params.number
      ])
      logger.info("   Contract ${contract.num_N_DOC_ID} was put successfully!")

      putDocumentSubject([
        docId      : contract.num_N_DOC_ID,
        subjectId  : params.providerId,
        roleId     : getProviderRoleId(),
        workflowId : params.workflowId
      ])
      putDocumentSubject([
        docId      : contract.num_N_DOC_ID,
        subjectId  : params.receiverId,
        roleId     : getReceiverRoleId(),
        workflowId : params.workflowId
      ])
      actualizeDocument(contract.num_N_DOC_ID)
      return contract
    } catch (Exception e){
      logger.error("   Error while putting contract!")
      logger.error_oracle(e)
      return null
    }
  }

  Boolean dissolveContract(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      docId          : null,
      endDate        : null,
      checkInvoices : false
    ], input)
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

  boolean dissolveContract(def docId, LocalDateTime endDate = DateTimeUtil.now(), Boolean checkInvoices = false) {
    return dissolveContract(docId: docId, endDate: endDate, checkInvoices: checkInvoices)
  }
}