package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.DateTimeUtil.local
import static org.camunda.latera.bss.utils.MapUtil.keysList
import static org.camunda.latera.bss.utils.StringUtil.capitalize
import static org.camunda.latera.bss.utils.StringUtil.isEmpty
import static org.camunda.latera.bss.utils.StringUtil.notEmpty
import static org.camunda.latera.bss.utils.Oracle.encodeFlag
import static org.camunda.latera.bss.utils.Constants.DOC_TYPE_SubscriberContract
import static org.camunda.latera.bss.utils.Constants.DOC_TYPE_ContractAPP
import static org.camunda.latera.bss.utils.Constants.DOC_TYPE_AddAgreement
import static org.camunda.latera.bss.utils.Constants.WFLOW_SubscriberContract
import static org.camunda.latera.bss.utils.Constants.WFLOW_ContractAPP
import static org.camunda.latera.bss.utils.Constants.WFLOW_AddAgreement
import java.time.temporal.Temporal

/**
 * Contract, contract app and add agreement specific methods
 */
trait Contract {
  private static String CONTRACTS_TABLE        = 'SD_V_CONTRACTS'
  private static String CONTRACTS_TREE_MV      = 'SD_MV_CONTRACTS_TREE'
  private static String BASE_CONTRACTS_TREE_MV = 'SD_MV_BASE_CONTRACTS_TREE'

  /**
   * Get contracts table name
   */
  String getContractsTable() {
    return CONTRACTS_TABLE
  }

  /**
   * Get contracts tree material view
   */
  private String getContractsTreeMV() {
    return CONTRACTS_TREE_MV
  }

  /**
   * Get base contracts tree material view
   */
  private String getBaseContractsTreeMV() {
    return BASE_CONTRACTS_TREE_MV
  }

  /**
   * Get contract document type ref code
   */
  String getContractType() {
    return getRefCode(getContractTypeId())
  }

  /**
   * Get contract document type ref id
   */
  Number getContractTypeId() {
    return DOC_TYPE_SubscriberContract
  }

  /**
   * Get contract document default workflow code
   */
  String getDefaultContractWorkflow() {
    return getRefCode(getDefaultContractWorkflowId())
  }

  /**
   * Get contract document default workflow id
   */
  Number getDefaultContractWorkflowId() {
    return WFLOW_SubscriberContract
  }

  /**
   * Get contract by id
   * @param docId {@link java.math.BigInteger BigInteger}
   * @return Contract table row
   */
  Map getContract(def docId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return hid.getTableFirst(getContractsTable(), where: where)
  }

  /**
   * Search for contracts by different fields value
   * @see Document#getDocumentsBy(java.util.Map)
   */
  List getContractsBy(Map input) {
    input.docId = input.docId ?: input.contractId
    if (!input.docTypeId) {
      input.docTypeId = getContractTypeId()
    }
    return getDocumentsBy(input)
  }

  /**
   * Search for one contract by different fields value
   * @see Document#getDocumentBy(java.util.Map)
   */
  Map getContractBy(Map input) {
    input.docId = input.docId ?: input.contractId
    if (!input.docTypeId) {
      input.docTypeId = getContractTypeId()
    }
    return getDocumentBy(input)
  }

  /**
   * Check if entity or entity type is contract
   * @param entityOrEntityType {@link java.math.BigInteger BigInteger} or {@link CharSequence String}. Document id, document type ref id or document type ref code
   * @return True if given value is contract, false otherwise
   */
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

  /**
   * Create or update contract
   * @param docId          {@link java.math.BigInteger BigInteger}. Optional
   * @param contractId     Alias for docId
   * @param parentDocId    {@link java.math.BigInteger BigInteger}. Optional
   * @param baseContractId Alias for parentDocId
   * @param workflowId     {@link java.math.BigInteger BigInteger}. Optional. Default: contract default workflow
   * @param providerId     {@link java.math.BigInteger BigInteger}. Optional. Default: current firm id
   * @param receiverId     {@link java.math.BigInteger BigInteger}. Optional
   * @param beginDate      {@link java.time.Temporal Any date type}. Optional
   * @param endDate        {@link java.time.Temporal Any date type}. Optional
   * @param number         {@link CharSequence String}. Optional
   * @return Created or updated contract (in Oracle API procedure notation)
   */
  private Map putContract(Map input) {
    LinkedHashMap defaultParams = [
      docId              : null,
      docTypeId          : getContractTypeId(),
      parentDocId        : null,
      workflowId         : getDefaultContractWorkflowId(),
      providerId         : getFirmId(),
      providerAccountId  : null,
      recipientId        : null,
      recipientAccountId : null,
      memberId           : null,
      memberAccountId    : null,
      managerId          : null,
      managerAccountId   : null,
      beginDate          : local(),
      endDate            : null,
      number             : null
    ]
    String _docTypeName = ''

    try {
      LinkedHashMap existingContract = [:]
      if (isEmpty(input.docId) && notEmpty(input.contractId)) {
        input.docId = input.contractId
        input.remove('contractId')
      }
      if (isEmpty(input.parentDocId) && notEmpty(input.baseContractId)) {
        input.parentDocId = input.baseContractId
        input.remove('baseContractId')
      }

      if (notEmpty(input.docId)) {
        LinkedHashMap contract = getContract(input.docId)
        existingContract = [
          docId       : contract.n_doc_id,
          docTypeId   : contract.n_doc_type_id,
          parentDocId : contract.n_par_doc_id,
          workflowId  : contract.n_workflow_id,
          providerId  : contract.n_provider_id,
          recipientId : contract.n_recipient_id,
          beginDate   : contract.d_begin,
          endDate     : contract.d_end,
          number      : contract.vc_doc_no
        ]
      }
      LinkedHashMap params = mergeParams(defaultParams, existingContract + fallbackReceiverToRecipient(input))

      if (params.docTypeId == getContractTypeId()) {
        _docTypeName = 'contract'
      } else if (params.docTypeId == getContractAppTypeId()) {
        _docTypeName = 'contract app'
      } else if (params.docTypeId == getAddAgreementTypeId()) {
        _docTypeName = 'add agreement'
      }
      String docTypeName = capitalize(_docTypeName)

      List paramsNames = keysList(defaultParams) - ['parentDocId', 'providerId', 'recipientId', 'number']
      LinkedHashMap result = [:]
      if (isEmpty(input.docId) && params.subMap(paramsNames) == defaultParams.subMap(paramsNames) && isEmpty(params.number)) {
        logger.info("Creating new ${_docTypeName} with params ${params}")
        result = hid.execute('SI_USERS_PKG.CREATE_CONTRACT', [
          num_N_USER_ID          : params.recipientId,
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

        if (params.providerId || params.providerAccountId) {
          Boolean providerAdded = addDocumentProvider(
            result.num_N_DOC_ID,
            params.providerId,
            params.providerAccountId,
            workflowId : params.workflowId
          )
          if (!providerAdded) {
            throw new Exception("Cannot set provider ${params.providerId} for document ${result.num_N_DOC_ID}")
          }
        }
        if (params.recipientId || params.recipientAccountId) {
          Boolean recipientAdded = addDocumentRecipient(
            result.num_N_DOC_ID,
            params.recipientId,
            params.recipientAccountId,
            workflowId : params.workflowId
          )
          if (!recipientAdded) {
            throw new Exception("Cannot set recipient ${params.recipientId} for document ${result.num_N_DOC_ID}")
          }
        }

        if (params.memberId || params.memberAccountId) {
          Boolean memberAdded = addDocumentMember(
            result.num_N_DOC_ID,
            params.memberId,
            params.memberAccountId,
            workflowId : params.workflowId
          )
          if (!memberAdded) {
            throw new Exception("Cannot set member ${params.memberId} for document ${result.num_N_DOC_ID}")
          }
        }

        if (params.managerId || params.managerAccountId) {
          Boolean managerAdded = addDocumentManager(
            result.num_N_DOC_ID,
            params.managerId,
            params.managerAccountId,
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

  /**
   * Create contract
   * @param customerId     {@link java.math.BigInteger BigInteger}
   * @param baseContractId {@link java.math.BigInteger BigInteger}. Optional
   * @param workflowId     {@link java.math.BigInteger BigInteger}. Optional. Default: contract default workflow
   * @param providerId     {@link java.math.BigInteger BigInteger}. Optional. Default: current firm id
   * @param beginDate      {@link java.time.Temporal Any date type}. Optional
   * @param endDate        {@link java.time.Temporal Any date type}. Optional
   * @param number         {@link CharSequence String}. Optional
   * @return Created contract (in Oracle API procedure notation)
   */
  Map createContract(Map input = [:], def customerId) {
    input.remove('docId')
    input.remove('contractId')
    return putContract(input + [recipientId: customerId])
  }

  /**
   * Update contract
   * @param docId          {@link java.math.BigInteger BigInteger}
   * @param baseContractId {@link java.math.BigInteger BigInteger}. Optional
   * @param workflowId     {@link java.math.BigInteger BigInteger}. Optional. Default: contract default workflow
   * @param beginDate      {@link java.time.Temporal Any date type}. Optional
   * @param endDate        {@link java.time.Temporal Any date type}. Optional
   * @param number         {@link CharSequence String}. Optional
   * @return Updated contract (in Oracle API procedure notation)
   */
  Map updateContract(Map input = [:], def docId) {
    return putContract(input + [docId: docId])
  }

  /**
   * Add tag to contract
   * @see Document#addDocumentTag(java.util.Map)
   */
  Map addContractTag(Map input) {
    return addDocumentTag(input)
  }

  /**
   * Add tag to contract
   * @see Document#addDocumentTag(def,java.lang.CharSequence)
   */
  Map addContractTag(def docId, CharSequence tag) {
    return addContractTag(docId: docId, tag: tag)
  }

  /**
   * Add tag to contract
   * @see Document#addDocumentTag(java.util.Map,def)
   */
  Map addContractTag(Map input = [:], def docId) {
    return addContractTag(input + [docId: docId])
  }

  /**
   * Delete tag from contract
   * @see Document#deleteDocumentTag(def)
   */
  Boolean deleteContractTag(def docTagId) {
    return deleteDocumentTag(docTagId)
  }

  /**
   * Delete tag from contract
   * @see Document#deleteDocumentTag(java.util.Map)
   */
  Boolean deleteContractTag(Map input) {
    return deleteDocumentTag(input)
  }

  /**
   * Delete tag from contract
   * @see Document#deleteDocumentTag(def,java.lang.CharSequence)
   */
  Boolean deleteContractTag(def docId, CharSequence tag) {
    return deleteContractTag(docId: docId, tag: tag)
  }
  
  /**
   * Change contract state to Dissolved
   * @param docId           {@link java.math.BigInteger BigInteger}
   * @param endDate         {@link java.time.Temporal Any date type}. Optional. Default: currrent datetime
   * @param checkChargeLogs {@link CharSequence String}. True if there should be no actual charge logs on contract app, false to disable such check. Optional. Default: false
   * @return True if contract was successfully dissolved, false otherwise
   */
  Boolean dissolveContract(Map input) {
    LinkedHashMap params = mergeParams([
      docId           : null,
      endDate         : null,
      checkChargeLogs : false
    ], input)
    params.docId = params.docId ?: params.contractId
    try {
      logger.info("Dissolving contract id ${params.docId} with date ${params.endDate}")
      LinkedHashMap contract = hid.execute('SD_CONTRACTS_PKG.SD_CONTRACTS_DISSOLVE', [
        num_N_DOC_ID      : params.docId,
        dt_D_END          : params.endDate,
        b_CheckChargeLogs : encodeFlag(params.checkChargeLogs)
      ])
      logger.info("   Contract ${contract.num_N_DOC_ID} was dissolved successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while dissolving contract!")
      logger.error_oracle(e)
      return false
    }
  }

  /**
   * Change contract state to Dissolved
   *
   * Overload with positional arguments
   * @see #dissolveContract(java.util.Map)
   */
  Boolean dissolveContract(def docId, Temporal endDate = local(), Boolean checkChargeLogs = false) {
    return dissolveContract(docId: docId, endDate: endDate, checkChargeLogs: checkChargeLogs)
  }

  /**
   * Get contract apps table name
   */
  String getContractAppsTable() {
    return getContractsTable()
  }

  /**
   * Get contract app document type ref code
   */
  String getContractAppType() {
    return getRefCode(getContractAppTypeId())
  }

  /**
   * Get contract app document type ref id
   */
  Number getContractAppTypeId() {
    return DOC_TYPE_ContractAPP
  }

  /**
   * Get contract app document default workflow code
   */
  String getDefaultContractAppWorkflow() {
    return getRefCode(getDefaultContractAppWorkflowId())
  }

  /**
   * Get contract app document default workflow id
   */
  Number getDefaultContractAppWorkflowId() {
    return WFLOW_ContractAPP
  }

  /**
   * Get contract app by id
   * @param docId {@link java.math.BigInteger BigInteger}
   * @return Contract app table row
   */
  Map getContractApp(def docId) {
    return getContract(docId)
  }

  /**
   * Search for contract apps by different fields value
   * @see Document#getDocumentsBy(java.util.Map)
   */
  List getContractAppsBy(Map input) {
    input.docId = input.docId ?: input.contractAppId
    if (!input.docTypeId) {
      input.docTypeId = getContractAppTypeId()
    }
    input.recipientId = null
    input.providerId  = null
    return getDocumentsBy(input)
  }

  /**
   * Search for one contract app by different fields value
   * @see Document#getDocumentBy(java.util.Map)
   */
  Map getContractAppBy(Map input) {
    input.docId = input.docId ?: input.contractAppId
    if (!input.docTypeId) {
      input.docTypeId = getContractAppTypeId()
    }
    input.recipientId = null
    input.providerId  = null
    return getDocumentBy(input)
  }

  /**
   * Check if entity or entity type is contract app
   * @param entityOrEntityType {@link java.math.BigInteger BigInteger} or {@link CharSequence String}. Document id, document type ref id or document type ref code
   * @return True if given value is contract app, false otherwise
   */
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

  /**
   * Create or update contract app
   * @param docId          {@link java.math.BigInteger BigInteger}. Optional
   * @param contractAppId  Alias for docId
   * @param parentDocId    {@link java.math.BigInteger BigInteger}. Optional
   * @param contractId     Alias for parentDocId
   * @param workflowId     {@link java.math.BigInteger BigInteger}. Optional. Default: contract app default workflow
   * @param beginDate      {@link java.time.Temporal Any date type}. Optional
   * @param endDate        {@link java.time.Temporal Any date type}. Optional
   * @param number         {@link CharSequence String}. Optional
   * @return Created or updated contract app (in Oracle API procedure notation)
   */
  private Map putContractApp(Map input) {
    LinkedHashMap params = [
      docTypeId  : getContractAppTypeId(),
      workflowId : getDefaultContractAppWorkflowId()
    ] + input
    params.docId       = params.docId       ?: params.contractAppId
    params.parentDocId = params.parentDocId ?: params.contractId
    params.providerId  = null
    params.recipientId = null
    params.remove('contractId')
    return putContract(params)
  }

  /**
   * Create contract app
   * @param contractId     {@link java.math.BigInteger BigInteger}
   * @param workflowId     {@link java.math.BigInteger BigInteger}. Optional. Default: contract app default workflow
   * @param beginDate      {@link java.time.Temporal Any date type}. Optional
   * @param endDate        {@link java.time.Temporal Any date type}. Optional
   * @param number         {@link CharSequence String}. Optional
   * @return Created contract app (in Oracle API procedure notation)
   */
  Map createContractApp(Map input = [:], def contractId) {
    input.remove('docId')
    input.remove('contractAppId')
    return putContractApp(input + [contractId: contractId])
  }

  /**
   * Update contract app
   * @param docId          {@link java.math.BigInteger BigInteger}
   * @param contractId     {@link java.math.BigInteger BigInteger}. Optional
   * @param workflowId     {@link java.math.BigInteger BigInteger}. Optional. Default: contract app default workflow
   * @param beginDate      {@link java.time.Temporal Any date type}. Optional
   * @param endDate        {@link java.time.Temporal Any date type}. Optional
   * @param number         {@link CharSequence String}. Optional
   * @return Updated contract app (in Oracle API procedure notation)
   */
  Map updateContractApp(Map input = [:], def docId) {
    return putContractApp(input + [docId: docId])
  }

  /**
   * Add tag to contract app
   * @see Document#addDocumentTag(java.util.Map)
   */
  Map addContractAppTag(Map input) {
    return addDocumentTag(input)
  }

  /**
   * Add tag to contract app
   * @see Document#addDocumentTag(def,java.lang.CharSequence)
   */
  Map addContractAppTag(def docId, CharSequence tag) {
    return addContractAppTag(doc: docId, tag: tag)
  }

  /**
   * Add tag to contract app
   * @see Document#addDocumentTag(java.util.Map,def)
   */
  Map addContractAppTag(Map input = [:], def docId) {
    return addContractAppTag(input + [doc: docId])
  }

  /**
   * Delete tag from contract app
   * @see Document#deleteDocumentTag(def)
   */
  Boolean deleteContractAppTag(def docTagId) {
    return deleteDocumentTag(docTagId)
  }

  /**
   * Delete tag from contract app
   * @see Document#deleteDocumentTag(java.util.Map)
   */
  Boolean deleteContractAppTag(Map input) {
    return deleteDocumentTag(input)
  }

  /**
   * Delete tag from contract app
   * @see Document#deleteDocumentTag(def,java.lang.CharSequence)
   */
  Boolean deleteContractAppTag(def docId, CharSequence tag) {
    return deleteContractAppTag(docId: docId, tag: tag)
  }

  /**
   * Change contract app state to Dissolved
   * @param docId           {@link java.math.BigInteger BigInteger}
   * @param endDate         {@link java.time.Temporal Any date type}. Optional. Default: currrent datetime
   * @param checkChargeLogs {@link CharSequence String}. True if there should be no actual charge logs on contract, false to disable such check. Optional. Default: false
   * @return True if contract was successfully dissolved, false otherwise
   */
  Boolean dissolveContractApp(Map input) {
    return dissolveContract(input)
  }

  /**
   * Change contract app state to Dissolved
   *
   * Overload with positional arguments
   * @see #dissolveContractApp(java.util.Map)
   */
  Boolean dissolveContractApp(def docId, Temporal endDate = local(), Boolean checkChargeLogs = false) {
    return dissolveContract(docId: docId, endDate: endDate, checkChargeLogs: checkChargeLogs)
  }

  /**
   * Get add agreements table name
   */
  String getAddAgreementsTable() {
    return getContractsTable()
  }

  /**
   * Get add agreement document type ref code
   */
  String getAddAgreementType() {
    return getRefCode(getAddAgreementTypeId())
  }

  /**
   * Get add agreement document type ref id
   */
  Number getAddAgreementTypeId() {
    return DOC_TYPE_AddAgreement
  }

  /**
   * Get add agreement document default workflow code
   */
  String getDefaultAddAgreementWorkflow() {
    return getRefCode(getDefaultAddAgreementWorkflowId())
  }

  /**
   * Get add agreement document default workflow id
   */
  Number getDefaultAddAgreementWorkflowId() {
    return WFLOW_AddAgreement
  }

  /**
   * Get add agreement by id
   * @param docId {@link java.math.BigInteger BigInteger}
   * @return Add agreement table row
   */
  Map getAddAgreement(def docId) {
    return getContract(docId)
  }

  /**
   * Search for add agreements by different fields value
   * @see Document#getDocumentsBy(java.util.Map)
   */
  List getAddAgreementsBy(Map input) {
    input.docId = input.docId ?: input.contractId
    if (!input.docTypeId) {
      input.docTypeId = getAddAgreementTypeId()
    }
    input.recipientId = null
    input.providerId  = null
    return getDocumentsBy(input)
  }

  /**
   * Search for one add agreement by different fields value
   * @see Document#getDocumentBy(java.util.Map)
   */
  Map getAddAgreementBy(Map input) {
    input.docId = input.docId ?: input.contractId
    if (!input.docTypeId) {
      input.docTypeId = getAddAgreementTypeId()
    }
    input.recipientId = null
    input.providerId  = null
    return getDocumentBy(input)
  }

  /**
   * Check if entity or entity type is add agreement
   * @param entityOrEntityType {@link java.math.BigInteger BigInteger} or {@link CharSequence String}. Document id, document type ref id or document type ref code
   * @return True if given value is add agreement, false otherwise
   */
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

  /**
   * Create or update add agreement
   * @param docId          {@link java.math.BigInteger BigInteger}. Optional
   * @param addAgreementId Alias for docId
   * @param agreementId    Alias for docId
   * @param parentDocId    {@link java.math.BigInteger BigInteger}. Optional
   * @param contractId     Alias for parentDocId
   * @param workflowId     {@link java.math.BigInteger BigInteger}. Optional. Default: add agreement default workflow
   * @param beginDate      {@link java.time.Temporal Any date type}. Optional
   * @param endDate        {@link java.time.Temporal Any date type}. Optional
   * @param number         {@link CharSequence String}. Optional
   * @return Created or updated add agreement (in Oracle API procedure notation)
   */
  private Map putAddAgreement(Map input) {
    LinkedHashMap params = [
      docTypeId  : getAddAgreementTypeId(),
      workflowId : getDefaultAddAgreementWorkflowId()
    ] + input
    params.docId       = params.docId       ?: params.addAgreementId ?: params.agreementId
    params.parentDocId = params.parentDocId ?: params.contractId
    params.recipientId = null
    params.providerId  = null
    params.remove('contractId')
    return putContract(params)
  }

  /**
   * Create add agreement
   * @param contractId {@link java.math.BigInteger BigInteger}
   * @param workflowId {@link java.math.BigInteger BigInteger}. Optional
   * @param beginDate  {@link java.time.Temporal Any date type}. Optional
   * @param endDate    {@link java.time.Temporal Any date type}. Optional
   * @param number     {@link CharSequence String}. Optional
   * @return Created add agreement (in Oracle API procedure notation)
   */
  Map createAddAgreement(Map input = [:], def contractId) {
    input.remove('docId')
    input.remove('agreementId')
    input.remove('addAgreementId')
    return putAddAgreement(input + [contractId: contractId])
  }


  /**
   * Update add agreement
   * @param docId      {@link java.math.BigInteger BigInteger}
   * @param contractId {@link java.math.BigInteger BigInteger}. Optional
   * @param workflowId {@link java.math.BigInteger BigInteger}. Optional
   * @param beginDate  {@link java.time.Temporal Any date type}. Optional
   * @param endDate    {@link java.time.Temporal Any date type}. Optional
   * @param number     {@link CharSequence String}. Optional
   * @return Updated add agreement (in Oracle API procedure notation)
   */
  Map updateAddAgreement(Map input = [:], def docId) {
    return putAddAgreement(input + [docId: docId])
  }

  /**
   * Add tag to add agreement
   * @see Document#addDocumentTag(java.util.Map)
   */
  Map addAddAgreementTag(Map input) {
    return addDocumentTag(input)
  }

  /**
   * Add tag to add agreement
   * @see Document#addDocumentTag(def,java.lang.CharSequence)
   */
  Map addAddAgreementTag(def docId, CharSequence tag) {
    return addAddAgreementTag(doc: docId, tag: tag)
  }

  /**
   * Add tag to add agreement
   * @see Document#addDocumentTag(java.util.Map,def)
   */
  Map addAddAgreementTag(Map input = [:], def docId) {
    return addAddAgreementTag(input + [doc: docId])
  }

  /**
   * Delete tag from add agreement
   * @see Document#deleteDocumentTag(def)
   */
  Boolean deleteAddAgreementTag(def docTagId) {
    return deleteDocumentTag(docTagId)
  }

  /**
   * Delete tag from add agreement
   * @see Document#deleteDocumentTag(java.util.Map)
   */
  Boolean deleteAddAgreementTag(Map input) {
    return deleteDocumentTag(input)
  }

  /**
   * Delete tag from add agreement
   * @see Document#deleteDocumentTag(def,java.lang.CharSequence)
   */
  Boolean deleteAddAgreementTag(def docId, CharSequence tag) {
    return deleteAddAgreementTag(docId: docId, tag: tag)
  }

  /**
   * Change add agreement state to Dissolved
   * @param docId           {@link java.math.BigInteger BigInteger}
   * @param endDate         {@link java.time.Temporal Any date type}. Optional. Default: currrent datetime
   * @param checkChargeLogs {@link CharSequence String}. True if there should be no actual charge logs on add agreement, false to disable such check. Optional. Default: false
   * @return True if contract was successfully dissolved, false otherwise
   */
  Boolean dissolveAddAgreement(Map input) {
    return dissolveContract(input)
  }

  /**
   * Change add agreement state to Dissolved
   *
   * Overload with positional arguments
   * @see #dissolveAddAgreement(java.util.Map)
   */
  Boolean dissolveAddAgreement(def docId, Temporal endDate = local(), Boolean checkChargeLogs = false) {
    return dissolveContract(docId: docId, endDate: endDate, checkChargeLogs: checkChargeLogs)
  }

  /**
   * Refresh base contracts tree material view
   * @see Search#refreshMaterialView(java.lang.CharSequence,java.lang.CharSequence)
   */
  Boolean refreshBaseContracts(CharSequence method = 'C') {
    return refreshMaterialView(getBaseContractsTreeMV(), method)
  }

  /**
   * Refresh contracts tree material view
   * @see Search#refreshMaterialView(java.lang.CharSequence,java.lang.CharSequence)
   */
  Boolean refreshContracts(CharSequence method = 'C') {
    return refreshContractsTree(method)
  }

  /**
   * Refresh contracts tree material view
   * @see #refreshContracts(java.lang.CharSequence)
   */
  Boolean refreshContractsTree(CharSequence method = 'C') {
    return refreshMaterialView(getContractsTreeMV(), method)
  }
}