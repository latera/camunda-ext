package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.utils.DateTimeUtil
import org.camunda.latera.bss.utils.StringUtil
import org.camunda.latera.bss.utils.Oracle
import java.time.LocalDateTime

trait Contract {
  private static String  CONTRACTS_TABLE = 'SD_V_CONTRACTS'
  private static String  CONTRACT_TYPE   = 'DOC_TYPE_SubscriberContract'
  private static String  DEFAULT_CONTRACT_WORKFLOW    = 'WFLOW_SubscriberContract'
  private static Integer DEFAULT_CONTRACT_WORKFLOW_ID = 10021

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
    def defaultParams = [
      docId       : null,
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
    try {
      def paramsNames = (defaultParams.keySet() as List) - ['parentDocId', 'providerId', 'receiverId', 'number']
      LinkedHashMap contract = [:]
      if (params.subMap(paramsNames) == defaultParams.subMap(paramsNames) && StringUtil.isEmpty(params.number)) {
        logger.info("Creating new contract with params ${params}")
        contract = hid.execute('SI_USERS_PKG.CREATE_CONTRACT', [
          num_N_USER_ID          : params.receiverId,
          num_N_FIRM_ID          : params.providerId,
          num_N_BASE_CONTRACT_ID : params.parentDocId,
          num_N_CONTRACT_ID      : null
        ])
        contract.num_N_DOC_ID = contract.num_N_CONTRACT_ID
        logger.info("   Contract ${contract.num_N_DOC_ID} was put successfully!")
      } else {
        logger.info("Putting contract with params ${params}")
        contract = hid.execute('SD_CONTRACTS_PKG.SD_CONTRACTS_PUT', [
          num_N_DOC_ID        : params.docId,
          num_N_DOC_TYPE_ID   : getContractTypeId(),
          num_N_PARENT_DOC_ID : params.parentDocId,
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
      }
      return contract
    } catch (Exception e){
      logger.error("   Error while putting contract!")
      logger.error_oracle(e)
      return null
    }
  }

  LinkedHashMap createContract(LinkedHashMap input) {
    input.remove('docId')
    return putContract(input)
  }

  LinkedHashMap updateContract(def docId, LinkedHashMap input) {
    return putContract(input + [docId: docId])
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

  Boolean dissolveContract(def docId, LocalDateTime endDate = DateTimeUtil.now(), Boolean checkInvoices = false) {
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