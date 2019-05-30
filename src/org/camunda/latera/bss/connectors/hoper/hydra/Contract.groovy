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

  def getContractEntityType(def parentType, def id = null) {
    return CONTRACT_ENTITY_TYPE + withParent(parentType) + withId(id)
  }

  def getCustomerContractEntityType(def customerId, def id = null) {
    return getContractEntityType(getCustomerEntityType(customerId), id)
  }

  def getContractTypeId() {
    return CONTRACT_TYPE
  }

  def getContractAppTypeId() {
    return CONTRACT_APP_TYPE
  }

  def getAddAgreementTypeId() {
    return ADD_AGREEMENT_TYPE
  }

  def getDefaultContractWorkflowId() {
    return DEFAULT_CONTRACT_WORKFLOW_ID
  }

  LinkedHashMap getContractDefaultParams() {
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

  LinkedHashMap getContractParamsMap(LinkedHashMap params) {
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

  LinkedHashMap getContractParams(LinkedHashMap input) {
    def params = getContractDefaultParams() + input
    def data   = getContractParamsMap(params)
    return nvlParams(data)
  }

  List getCustomerContracts(def customerId, LinkedHashMap input = [:]) {
    def params = getPaginationDefaultParams() + input
    return getEntities(getCustomerContractEntityType(customerId), params)
  }

  LinkedHashMap getCustomerContract(def customerId, def contractId) {
    return getEntity(getCustomerContractEntityType(customerId), contractId)
  }

  LinkedHashMap createCustomerContract(def customerId, LinkedHashMap input = [:]) {
    LinkedHashMap params = getContractParams(input)
    params.remove('n_doc_state_id')

    def result = createEntity(getCustomerContractEntityType(customerId), params)
    if (input.stateId && result.n_doc_id) {
      result = updateCustomerContract(customerId, result.n_doc_id, input)
    }
    return result
  }

  LinkedHashMap updateCustomerContract(def customerId, def contractId, LinkedHashMap input) {
    LinkedHashMap params = getContractParams(input)
    return updateEntity(getCustomerContractEntityType(customerId), contractId, params)
  }

  LinkedHashMap putCustomerContract(def customerId, LinkedHashMap input) {
    def contractId = input.contractId
    input.remove('contractId')

    if (contractId) {
      return updateCustomerContract(customerId, contractId, input)
    } else {
      return createCustomerContract(customerId, input)
    }
  }

  Boolean deleteCustomerContract(def customerId, def contractId) {
    return deleteEntity(getCustomerContractEntityType(customerId), contractId)
  }
}