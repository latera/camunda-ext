package org.camunda.latera.bss.connectors.hoper.hydra

trait Contract {
  private static LinkedHashMap CONTRACT_ENTITY_TYPE = [
    one    : 'contract',
    plural : 'contracts'
  ]
  private static Integer CONTRACT_TYPE      = 1002 // 'DOC_TYPE_SubscriberContract'
  private static Integer CONTRACT_APP_TYPE  = 2002 // 'DOC_TYPE_ContractAPP'
  private static Integer ADD_AGREEMENT_TYPE = 13002 // 'DOC_TYPE_AddAgreement'
  private static Integer DEFAULT_CONTRACT_WORKFLOW_ID = 10021 // 'WFLOW_SubscriberContract'

  private Map getContractEntityType(Map parentType, def id = null) {
    return CONTRACT_ENTITY_TYPE + withParent(parentType) + withId(id)
  }

  private Map getCustomerContractEntityType(def customerId, def id = null) {
    return getContractEntityType(getCustomerEntityType(customerId), id)
  }

  Number getContractTypeId() {
    return CONTRACT_TYPE
  }

  Number getContractAppTypeId() {
    return CONTRACT_APP_TYPE
  }

  Number getAddAgreementTypeId() {
    return ADD_AGREEMENT_TYPE
  }

  Number getDefaultContractWorkflowId() {
    return DEFAULT_CONTRACT_WORKFLOW_ID
  }

  private Map getContractDefaultParams() {
    return [
      number      : null,
      providerId  : getFirmId(),
      docTypeId   : getContractTypeId(),
      workflowId  : getDefaultContractWorkflowId(),
      parentDocId : null,
      beginDate   : null,
      endDate     : null,
      stateId     : null,
      rem         : null
    ]
  }

  private Map getContractParamsMap(Map params) {
    return [
      vc_doc_no       : params.number,
      n_provider_id   : params.providerId,
      n_doc_type_id   : params.docTypeId,
      n_workflow_id   : params.workflowId,
      n_parent_doc_id : params.parentDocId,
      d_begin         : params.beginDate,
      d_end           : params.endDate,
      n_doc_state_id  : params.stateId,
      vc_rem          : params.rem
    ]
  }

  private Map getContractParams(Map input) {
    LinkedHashMap params = getContractDefaultParams() + input
    LinkedHashMap data   = getContractParamsMap(params)
    return prepareParams(data)
  }

  List getCustomerContracts(def customerId, Map input = [:]) {
    LinkedHashMap params = getPaginationDefaultParams() + input
    return getEntities(getCustomerContractEntityType(customerId), params)
  }

  Map getCustomerContract(def customerId, def contractId) {
    return getEntity(getCustomerContractEntityType(customerId), contractId)
  }

  Map createCustomerContract(def customerId, Map input = [:]) {
    LinkedHashMap params = getContractParams(input)
    params.remove('n_doc_state_id')

    LinkedHashMap result = createEntity(getCustomerContractEntityType(customerId), params)
    if (input.stateId && result.n_doc_id) {
      result = updateCustomerContract(customerId, result.n_doc_id, input)
    }
    return result
  }

  Map createCustomerContract(Map input) {
    def customerId = input.customerId
    input.remove('customerId')
    return createCustomerContract(customerId, input)
  }

  Map createCustomerContract(Map input, def customerId) {
    return createCustomerContract(customerId, input)
  }

  Map updateCustomerContract(def customerId, def contractId, Map input) {
    LinkedHashMap params = getContractParams(input)
    return updateEntity(getCustomerContractEntityType(customerId), contractId, params)
  }

  Map updateCustomerContract(Map input) {
    def customerId = input.customerId
    input.remove('customerId')
    def contractId = input.contractId
    input.remove('contractId')
    return updateCustomerContract(customerId, contractId, input)
  }

  Map updateCustomerContract(Map input, def customerId, def contractId) {
    return updateCustomerContract(customerId, contractId, input)
  }

  Map putCustomerContract(def customerId, Map input) {
    def contractId = input.contractId
    input.remove('contractId')

    if (contractId) {
      return updateCustomerContract(customerId, contractId, input)
    } else {
      return createCustomerContract(customerId, input)
    }
  }

  Map putCustomerContract(Map input) {
    def customerId = input.customerId
    input.remove('customerId')
    return putCustomerContract(customerId, input)
  }

  Boolean deleteCustomerContract(def customerId, def contractId) {
    return deleteEntity(getCustomerContractEntityType(customerId), contractId)
  }
}