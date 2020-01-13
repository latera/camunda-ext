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

  Map getContractEntityType(Map parentType, def id = null) {
    return CONTRACT_ENTITY_TYPE + withParent(parentType) + withId(id)
  }

  Map getCustomerContractEntityType(def customerId, def id = null) {
    return getContractEntityType(getCustomerEntityType(customerId), id)
  }

  Integer getContractTypeId() {
    return CONTRACT_TYPE
  }

  Integer getContractAppTypeId() {
    return CONTRACT_APP_TYPE
  }

  Integer getAddAgreementTypeId() {
    return ADD_AGREEMENT_TYPE
  }

  Integer getDefaultContractWorkflowId() {
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

  List getCustomerContracts(Map input = [:], def customerId) {
    LinkedHashMap params = getPaginationDefaultParams() + input
    return getEntities(getCustomerContractEntityType(customerId), params)
  }

  Map createCustomerContract(Map input = [:], def customerId) {
    LinkedHashMap params = getContractParams(input)
    params.remove('n_doc_state_id')

    LinkedHashMap result = createEntity(getCustomerContractEntityType(customerId), params)
    if (input.stateId && result.n_doc_id) {
      result = updateCustomerContract(customerId, result.n_doc_id, input)
    }
    return result
  }

  Map updateCustomerContract(Map input = [:], def customerId, def contractId) {
    LinkedHashMap params = getContractParams(input)
    return updateEntity(getCustomerContractEntityType(customerId), contractId, params)
  }

  Boolean deleteCustomerContract(def customerId, def contractId) {
    return deleteEntity(getCustomerContractEntityType(customerId), contractId)
  }
}