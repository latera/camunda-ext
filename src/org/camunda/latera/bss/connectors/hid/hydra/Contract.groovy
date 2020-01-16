package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.DateTimeUtil.local
import static org.camunda.latera.bss.utils.MapUtil.keysList
import static org.camunda.latera.bss.utils.StringUtil.capitalize
import static org.camunda.latera.bss.utils.StringUtil.isEmpty
import static org.camunda.latera.bss.utils.StringUtil.notEmpty
import static org.camunda.latera.bss.utils.Oracle.encodeFlag
import java.time.temporal.Temporal

trait Contract {
  private static String  CONTRACTS_TABLE        = 'SD_V_CONTRACTS'
  private static String  CONTRACTS_TREE_MV      = 'SD_MV_CONTRACTS_TREE'
  private static String  BASE_CONTRACTS_TREE_MV = 'SD_MV_BASE_CONTRACTS_TREE'
  private static String  CONTRACT_TYPE          = 'DOC_TYPE_SubscriberContract'
  private static String  CONTRACT_APP_TYPE      = 'DOC_TYPE_ContractAPP'
  private static String  ADD_AGREEMENT_TYPE     = 'DOC_TYPE_AddAgreement'
  private static String  DEFAULT_CONTRACT_WORKFLOW         = 'WFLOW_SubscriberContract'
  private static Integer DEFAULT_CONTRACT_WORKFLOW_ID      = 10021
  private static String  DEFAULT_CONTRACT_APP_WORKFLOW     = 'WFLOW_ContractAPP'
  private static Integer DEFAULT_CONTRACT_APP_WORKFLOW_ID  = 20021
  private static String  DEFAULT_ADD_AGREEMENT_WORKFLOW    = 'WFLOW_AddAgreement'
  private static Integer DEFAULT_ADD_AGREEMENT_WORKFLOW_ID = 130021

  String getContractsTable() {
    return CONTRACTS_TABLE
  }

  String getContractsTreeMV() {
    return CONTRACTS_TREE_MV
  }

  String getBaseContractsTreeMV() {
    return BASE_CONTRACTS_TREE_MV
  }

  String getContractType() {
    return CONTRACT_TYPE
  }

  Number getContractTypeId() {
    return getRefIdByCode(CONTRACT_TYPE)
  }

  String getDefaultContractWorkflow() {
    return DEFAULT_CONTRACT_WORKFLOW
  }

  Number getDefaultContractWorkflowId() {
    return DEFAULT_CONTRACT_WORKFLOW_ID
  }

  Map getContract(def docId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return hid.getTableFirst(getContractsTable(), where: where)
  }

  List getContractsBy(Map input) {
    input.docId = input.docId ?: input.contractId
    if (!input.docTypeId) {
      input.docTypeId = getContractTypeId()
    }
    return getDocumentsBy(input)
  }

  Map getContractBy(Map input) {
    input.docId = input.docId ?: input.contractId
    if (!input.docTypeId) {
      input.docTypeId = getContractTypeId()
    }
    return getDocumentBy(input)
  }

  Boolean isContract(def entityOrEntityType) {
    if (entityOrEntityType == null) {
      return false
    }

    Number entityIdOrEntityTypeId = toIntSafe(entityOrEntityType)
    if (entityIdOrEntityTypeId != null) {
      return entityIdOrEntityTypeId == getContractTypeId() || getDocument(docOrDocTypeId)?.n_doc_type_id == getContractTypeId()
    } else {
      return entityType == getContractType()
    }
  }

  private Map putContract(Map input) {
    LinkedHashMap defaultParams = [
      docId       : null,
      docTypeId   : getContractTypeId(),
      parentDocId : null,
      workflowId  : getDefaultContractWorkflowId(),
      providerId  : getFirmId(),
      receiverId  : null,
      stateId     : getDocumentStateActualId(),
      beginDate   : local(),
      endDate     : null,
      number      : null
    ]
    String _docTypeName = ''

    try {
      LinkedHashMap existingContract = [:]
      if (isEmpty(input.docId) && notEmpty(input.contractId)) {
        input.docId = input.contractId
      }
      if (isEmpty(input.parentDocId) && notEmpty(input.baseContractId)) {
        input.parentDocId = input.baseContractId
      }

      if (notEmpty(input.docId)) {
        LinkedHashMap contract = getContract(input.docId)
        existingContract = [
          docId       : contract.n_doc_id,
          docTypeId   : contract.n_doc_type_id,
          parentDocId : contract.n_par_doc_id,
          workflowId  : contract.n_workflow_id,
          providerId  : contract.n_provider_id,
          receiverId  : contract.n_receiver_id,
          stateId     : contract.n_doc_state_id,
          beginDate   : contract.d_begin,
          endDate     : contract.d_end,
          number      : contract.vc_doc_no
        ]
      }
      LinkedHashMap params = mergeParams(defaultParams, existingContract + input)

      if (params.docTypeId == getContractTypeId()) {
        _docTypeName = 'contract'
      } else if (params.docTypeId == getContractAppTypeId()) {
        _docTypeName = 'contract app'
      } else if (params.docTypeId == getAddAgreementTypeId()) {
        _docTypeName = 'add agreement'
      }
      String docTypeName = capitalize(_docTypeName)

      List paramsNames = keysList(defaultParams) - ['parentDocId', 'providerId', 'receiverId', 'number']
      LinkedHashMap result = [:]
      if (isEmpty(input.docId) && params.subMap(paramsNames) == defaultParams.subMap(paramsNames) && isEmpty(params.number)) {
        logger.info("Creating new ${_docTypeName} with params ${params}")
        result = hid.execute('SI_USERS_PKG.CREATE_CONTRACT', [
          num_N_USER_ID          : params.receiverId,
          num_N_FIRM_ID          : params.providerId,
          num_N_BASE_CONTRACT_ID : params.parentDocId,
          num_N_CONTRACT_ID      : null
        ])
        result.num_N_DOC_ID = result.num_N_CONTRACT_ID
        logger.info("   ${docTypeName} ${result.num_N_DOC_ID} was created successfully!")
      } else {
        logger.info("${params.docId ? 'Updating' : 'Creating'} ${_docTypeName} with params ${params}")
        result = hid.execute('SD_CONTRACTS_PKG.SD_CONTRACTS_PUT', [
          num_N_DOC_ID        : params.docId,
          num_N_DOC_TYPE_ID   : params.docTypeId,
          num_N_PARENT_DOC_ID : params.parentDocId,
          num_N_WORKFLOW_ID   : params.workflowId,
          dt_D_BEGIN          : params.beginDate,
          dt_D_END            : params.endDate,
          vch_VC_DOC_NO       : params.number
        ])
        logger.info("   ${docTypeName} ${result.num_N_DOC_ID} was put successfully!")

        if (params.providerId) {
          Boolean providerAdded = addDocumentSubject(
            result.num_N_DOC_ID,
            subjectId  : params.providerId,
            accountId  : params.providerAccountId,
            roleId     : getProviderRoleId(),
            workflowId : params.workflowId
          )
          if (!providerAdded) {
            throw new Exception("Cannot set provider ${params.providerId} for document ${result.num_N_DOC_ID}")
          }
        }
        if (params.receiverId) {
          Boolean receiverAdded = addDocumentSubject(
            result.num_N_DOC_ID,
            subjectId  : params.receiverId,
            accountId  : params.receiverAccountId,
            roleId     : getReceiverRoleId(),
            workflowId : params.workflowId
          )
          if (!receiverAdded) {
            throw new Exception("Cannot set receiver ${params.receiverId} for document ${result.num_N_DOC_ID}")
          }
        }

        if (params.memberId || params.memberAccountId) {
          Boolean memberAdded = addDocumentSubject(
            result.num_N_DOC_ID,
            subjectId  : params.memberId,
            accountId  : params.memberAccountId,
            roleId     : getMemberRoleId(),
            workflowId : params.workflowId
          )
          if (!memberAdded) {
            throw new Exception("Cannot set member ${params.memberId} for document ${result.num_N_DOC_ID}")
          }
        }

        if (params.managerId || params.managerAccountId) {
          Boolean managerAdded = addDocumentSubject(
            result.num_N_DOC_ID,
            subjectId  : params.managerId,
            accountId  : params.managerAccountId,
            roleId     : getManagerRoleId(),
            workflowId : params.workflowId
          )
          if (!managerAdded) {
            throw new Exception("Cannot set manager ${params.managerId} for document ${result.num_N_DOC_ID}")
          }
        }

        Boolean contractActualized = actualizeDocument(result.num_N_DOC_ID)
        if (!contractActualized) {
          throw new Exception("Cannot actualize document ${result.num_N_DOC_ID}")
        }

        logger.info("   ${docTypeName} ${result.num_N_DOC_ID} was ${params.docId ? 'updated' : 'created'} successfully!")
      }
      return result
    } catch (Exception e){
      logger.error("   Error while ${input.docId ? 'updating' : 'creating'} ${_docTypeName}!")
      logger.error_oracle(e)
      return null
    }
  }

  Map createContract(Map input = [:], def customerId) {
    input.remove('docId')
    input.remove('contractId')
    return putContract(input + [receiverId: customerId])
  }

  Map updateContract(Map input = [:], def docId) {
    return updateContract(input + [docId: docId])
  }

  Map addContractTag(Map input) {
    return addDocumentTag(input)
  }

  Map addContractTag(def docId, CharSequence tag) {
    return addContractTag(docId: docId, tag: tag)
  }

  Map addContractTag(Map input = [:], def docId) {
    return addContractTag(input + [docId: docId])
  }

  Boolean deleteContractTag(def docTagId) {
    return deleteDocumentTag(docTagId)
  }

  Boolean deleteContractTag(Map input) {
    return deleteDocumentTag(input)
  }

  Boolean deleteContractTag(def docId, CharSequence tag) {
    return deleteContractTag(docId: docId, tag: tag)
  }

  Boolean dissolveContract(Map input) {
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
        b_CheckInvoices : encodeFlag(params.checkInvoices)
      ])
      logger.info("   Contract ${contract.num_N_DOC_ID} was dissolved successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while dissolving contract!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean dissolveContract(def docId, Temporal endDate = local(), Boolean checkInvoices = false) {
    return dissolveContract(docId: docId, endDate: endDate, checkInvoices: checkInvoices)
  }

  String getContractAppsTable() {
    return getContractsTable()
  }

  String getContractAppType() {
    return CONTRACT_APP_TYPE
  }

  Number getContractAppTypeId() {
    return getRefIdByCode(CONTRACT_APP_TYPE)
  }

  String getDefaultContractAppWorkflow() {
    return DEFAULT_CONTRACT_APP_WORKFLOW
  }

  Number getDefaultContractAppWorkflowId() {
    return DEFAULT_CONTRACT_APP_WORKFLOW_ID
  }

  Map getContractApp(def docId) {
    return getContract(docId)
  }

  List getContractAppsBy(Map input) {
    input.docId = input.docId ?: input.contractId
    if (!input.docTypeId) {
      input.docTypeId = getContractAppTypeId()
    }
    input.receiverId = null
    input.providerId = null
    return getDocumentsBy(input)
  }

  Map getContractAppBy(Map input) {
    input.docId = input.docId ?: input.contractId
    if (!input.docTypeId) {
      input.docTypeId = getContractAppTypeId()
    }
    input.receiverId = null
    input.providerId = null
    return getDocumentBy(input)
  }

  Boolean isContractApp(def entityOrEntityType) {
    if (entityOrEntityType == null) {
      return false
    }

    Number entityIdOrEntityTypeId = toIntSafe(entityOrEntityType)
    if (entityIdOrEntityTypeId != null) {
      return entityIdOrEntityTypeId == getContractAppTypeId() || getDocument(docOrDocTypeId)?.n_doc_type_id == getContractAppTypeId()
    } else {
      return entityType == getContractAppType()
    }
  }

  private Map putContractApp(Map input) {
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

  Map createContractApp(Map input = [:], def contractId) {
    input.remove('docId')
    input.remove('contractAppId')
    return putContractApp(input + [contractId: contractId])
  }

  Map updateContractApp(Map input = [:], def docId) {
    return putContractApp(input + [docId: docId])
  }
  
  Map addContractAppTag(Map input) {
    return addDocumentTag(input)
  }

  Map addContractAppTag(def docId, CharSequence tag) {
    return addContractAppTag(doc: docId, tag: tag)
  }

  Map addContractAppTag(Map input = [:], def docId) {
    return addContractAppTag(input + [doc: docId])
  }

  Boolean deleteContractAppTag(def docTagId) {
    return deleteDocumentTag(docTagId)
  }

  Boolean deleteContractAppTag(Map input) {
    return deleteDocumentTag(input)
  }

  Boolean deleteContractAppTag(def docId, CharSequence tag) {
    return deleteContractAppTag(docId: docId, tag: tag)
  }

  Boolean dissolveContractApp(Map input) {
    return dissolveContract(input)
  }

  Boolean dissolveContractApp(def docId, Temporal endDate = local(), Boolean checkInvoices = false) {
    return dissolveContract(docId: docId, endDate: endDate, checkInvoices: checkInvoices)
  }

  String getAddAgreementsTable() {
    return getContractsTable()
  }

  String getAddAgreementType() {
    return ADD_AGREEMENT_TYPE
  }

  Number getAddAgreementTypeId() {
    return getRefIdByCode(ADD_AGREEMENT_TYPE)
  }

  String getDefaultAddAgreementWorkflow() {
    return DEFAULT_ADD_AGREEMENT_WORKFLOW
  }

  Number getDefaultAddAgreementWorkflowId() {
    return DEFAULT_ADD_AGREEMENT_WORKFLOW_ID
  }

  Map getAddAgreement(def docId) {
    return getContract(docId)
  }

  List getAddAgreementsBy(Map input) {
    input.docId = input.docId ?: input.contractId
    if (!input.docTypeId) {
      input.docTypeId = getAddAgreementTypeId()
    }
    input.receiverId = null
    input.providerId = null
    return getDocumentsBy(input)
  }

  Map getAddAgreementBy(Map input) {
    input.docId = input.docId ?: input.contractId
    if (!input.docTypeId) {
      input.docTypeId = getAddAgreementTypeId()
    }
    input.receiverId = null
    input.providerId = null
    return getDocumentBy(input)
  }

  Boolean isAddAgreement(def entityOrEntityType) {
    if (entityOrEntityType == null) {
      return false
    }

    Number entityIdOrEntityTypeId = toIntSafe(entityOrEntityType)
    if (entityIdOrEntityTypeId != null) {
      return entityIdOrEntityTypeId == getAddAgreementTypeId() || getDocument(docOrDocTypeId)?.n_doc_type_id == getAddAgreementTypeId()
    } else {
      return entityType == getAddAgreementType()
    }
  }

  private Map putAddAgreement(Map input) {
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

  Map createAddAgreement(Map input = [:], def contractId) {
    input.remove('docId')
    input.remove('agreementId')
    input.remove('addAgreementId')
    return putAddAgreement(input + [contractId: contractId])
  }

  Map updateAddAgreement(Map input = [:], def docId) {
    return putAddAgreement(input + [docId: docId])
  }

  Map addAddAgreementTag(Map input) {
    return addDocumentTag(input)
  }

  Map addAddAgreementTag(def docId, CharSequence tag) {
    return addAddAgreementTag(doc: docId, tag: tag)
  }

  Map addAddAgreementTag(Map input = [:], def docId) {
    return addAddAgreementTag(input + [doc: docId])
  }

  Boolean deleteAddAgreementTag(def docTagId) {
    return deleteDocumentTag(docTagId)
  }

  Boolean deleteAddAgreementTag(Map input) {
    return deleteDocumentTag(input)
  }

  Boolean deleteAddAgreementTag(def docId, CharSequence tag) {
    return deleteAddAgreementTag(docId: docId, tag: tag)
  }

  Boolean dissolveAddAgreement(Map input) {
    return dissolveContract(input)
  }

  Boolean dissolveAddAgreement(def docId, Temporal endDate = local(), Boolean checkInvoices = false) {
    return dissolveContract(docId: docId, endDate: endDate, checkInvoices: checkInvoices)
  }

  Boolean refreshBaseContracts(CharSequence method = 'C') {
    return refreshMaterialView(getBaseContractsTreeMV(), method)
  }

  Boolean refreshContracts(CharSequence method = 'C') {
    return refreshContractsTree(method)
  }

  Boolean refreshContractsTree(CharSequence method = 'C') {
    return refreshMaterialView(getContractsTreeMV(), method)
  }
}