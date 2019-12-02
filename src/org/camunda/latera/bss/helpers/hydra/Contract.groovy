package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.StringUtil.capitalize
import static org.camunda.latera.bss.utils.StringUtil.isEmpty
import static org.camunda.latera.bss.utils.DateTimeUtil.local

trait Contract {
  void fetchContract(Map input = [:]) {
    Map params = [
      baseContractPrefix : '',
      prefix             : ''
    ] + input

    String baseContractPrefix = "${capitalize(params.baseContractPrefix)}BaseContract"
    String prefix = "${capitalize(params.prefix)}Contract"

    def contractId = order."${prefix}Id" ?: [is: null]
    Map contract  = hydra.getContract(contractId)

    order."${prefix}Number" = contract?.vc_doc_no
    order."${prefix}Name"   = contract?.vc_name
    if (isEmpty(order."${baseContractPrefix}Id")) {
      order."${baseContractPrefix}Id" = contract?.n_parent_doc_id
    }
  }

  void fetchCustomerFirstContract(Map input = [:]) {
    Map params = [
      baseContractPrefix : '',
      customerPrefix     : '',
      prefix             : ''
    ] + input

    String customerPrefix = "${capitalize(params.customerPrefix)}Customer"
    String prefix = "${capitalize(params.prefix)}Contract"

    def customerId = order."${customerPrefix}Id" ?: [is: 'null']
    Map contract = hydra.getContractBy(receiverId: customerId, operationDate: local())

    order."${prefix}Id" = contract?.n_doc_id
    fetchContract(prefix: params.prefix, baseContractPrefix: params.baseContractPrefix)
  }

  Boolean createContract(Map input = [:]) {
    Map params = [
      baseContractPrefix : '',
      customerPrefix     : '',
      prefix             : ''
    ] + input

    String baseContractPrefix = "${capitalize(params.baseContractPrefix)}BaseContract"
    String customerPrefix = "${capitalize(params.customerPrefix)}Customer"
    String prefix = "${capitalize(params.prefix)}Contract"

    Map contract = hydra.putContract(
      parentDocId : order."${baseContractPrefix}Id",
      receiverId  : order."${customerPrefix}Id",
      number      : order."${prefix}Number"
    )

    Boolean result = false
    if (contract) {
      order."${prefix}Id" = contract.num_N_DOC_ID
      result = true
    }
    order."${prefix}Created" = result
    return result
  }

  Boolean dissolveContract(Map input = [:]) {
    Map params = [
      prefix  : '',
      endDate : local()
    ] + input

    String prefix = "${capitalize(params.prefix)}Contract"

    Boolean result = hydra.dissolveContract(
      docId   : order."${prefix}Id",
      endDate : params.endDate
    )
    if (result) {
      order."${prefix}DissolveDate" = params.endDate
    }
    order."${prefix}Dissolved" = result
    return result
  }

  def fetchContractAddParam(Map input = [:]) {
    Map params = [
      contractPrefix : '',
      prefix         : '',
      param          : '',
      code           : ''
    ] + input

    String contractPrefix = "${capitalize(params.contractPrefix)}Contract"
    String prefix = capitalize(params.prefix)
    String param  = capitalize(params.param)

    def contractId = order."${contractPrefix}Id" ?: [is: 'null']
    Map addParam = hydra.getDocumentAddParamBy(
      docId : contractId,
      param : params.code ?: "DOC_VAL_${param}"
    )
    def (value, valueType) = hydra.getAddParamValue(addParam)
    order."${contractPrefix}${prefix}${params.code ?: param}${valueType == 'refId' ? 'Id': ''}" = value
    return value
  }

  Boolean saveContractAddParam(Map input = [:]) {
    Map params = [
      contractPrefix : '',
      prefix         : '',
      param          : '',
      code           : ''
    ] + input

    String contractPrefix = "${capitalize(params.contractPrefix)}Contract"
    String prefix  = capitalize(params.prefix)
    String param    = capitalize(params.param)
    def contractId = order."${contractPrefix}Id"
    def value      = order."${contractPrefix}${prefix}${params.code ?: param}" ?: order."${contractPrefix}${prefix}${params.code ?: param}Id"

    Map addParam = hydra.addDocumentAddParam(
      docId : contractId,
      param : params.code ?: "DOC_VAL_${param}",
      value : value
    )
    Boolean result = false
    if (addParam) {
      result = true
    }
    order."${contractPrefix}${prefix}${params.code ?: param}Saved" = result
    return result
  }

  Boolean deleteContractAddParam(Map input = [:]) {
    Map params = [
      contractPrefix : '',
      prefix         : '',
      param          : '',
      code           : '',
      force          : false
    ] + input

    String contractPrefix = "${capitalize(params.contractPrefix)}Contract"
    String prefix = capitalize(params.prefix)
    String param  = capitalize(params.param)

    def contractId = order."${contractPrefix}Id"
    def value      = order."${contractPrefix}${prefix}${params.code ?: param}" ?: order."${contractPrefix}${prefix}${params.code ?: param}Id"

    Boolean result = true

    if (params.force) {
      result = hydra.deleteDocumentAddParam(
        docId : contractId,
        param : params.code ?: "DOC_VAL_${param}"
      )
    } else {
      result = hydra.deleteDocumentAddParam(
        docId : contractId,
        param : params.code ?: "DOC_VAL_${param}",
        value : value // multiple add param support
      )
    }

    order."${contractPrefix}${prefix}${params.code ?: param}Deleted" = result
    return result
  }

  Boolean fetchContractApp(Map input = [:]) {
    Map params = [
      contractPrefix : '',
      prefix         : ''
    ] + input

    String prefix = "${capitalize(params.prefix)}ContractApp"
    String contractPrefix = "${capitalize(params.contractPrefix)}Contract"

    Map contractApp = hydra.getContractApp(order."${prefix}Id")

    order."${prefix}Number" = contractApp?.vc_doc_no
    order."${prefix}Name"   = contractApp?.vc_name
    if (isEmpty(order."${contractPrefix}Id")) {
      order."${contractPrefix}Id" = contractApp?.n_parent_doc_id
    }
  }

  Boolean createContractApp(Map input = [:]) {
    Map params = [
      contractPrefix : '',
      prefix         : ''
    ] + input

    String prefix = "${capitalize(params.prefix)}ContractApp"
    String contractPrefix = "${capitalize(params.contractPrefix)}Contract"

    Map contractApp = hydra.putContractApp(
      parentDocId : order."${contractPrefix}Id",
      number      : order."${prefix}Number"
    )
    Boolean result = false
    if (contractApp) {
      order."${prefix}Id" = contractApp.num_N_DOC_ID
      result = true
    }
    order."${prefix}Created" = result
    return result
  }

  Boolean dissolveContractApp(Map input = [:]) {
    Map params = [
      prefix  : '',
      endDate : local()
    ] + input

    String prefix = "${capitalize(params.prefix)}ContractApp"

    Boolean result = hydra.dissolveContractApp(
      docId   : order."${prefix}Id",
      endDate : params.endDate
    )
    if (result) {
      order."${prefix}DissolveDate" = params.endDate
    }
    order."${prefix}Dissolved" = result
    return result
  }

  def fetchContractAppAddParam(Map input = [:]) {
    Map params = [
      contractAppPrefix : '',
      prefix         : '',
      param          : '',
      code           : ''
    ] + input

    String contractAppPrefix = "${capitalize(params.contractAppPrefix)}ContractApp"
    String prefix = capitalize(params.prefix)
    String param  = capitalize(params.param)

    def contractAppId = order."${contractAppPrefix}Id" ?: [is: 'null']
    Map addParam = hydra.getDocumentAddParamBy(
      docId : contractAppId,
      param : params.code ?: "DOC_VAL_${param}"
    )
    def (value, valueType) = hydra.getAddParamValue(addParam)
    order."${contractAppPrefix}${prefix}${params.code ?: param}${valueType == 'refId' ? 'Id': ''}" = value
    return value
  }

  Boolean saveContractAppAddParam(Map input = [:]) {
    Map params = [
      contractAppPrefix : '',
      prefix         : '',
      param          : '',
      code           : ''
    ] + input

    String contractAppPrefix = "${capitalize(params.contractAppPrefix)}ContractApp"
    String prefix     = capitalize(params.prefix)
    String param      = capitalize(params.param)

    def contractAppId = order."${contractAppPrefix}Id"
    def value         = order."${contractAppPrefix}${prefix}${params.code ?: param}" ?: order."${contractAppPrefix}${prefix}${params.code ?: param}Id"

    Map addParam = hydra.addDocumentAddParam(
      docId : contractAppId,
      param : params.code ?: "DOC_VAL_${param}",
      value : value
    )
    Boolean result = false
    if (addParam) {
      result = true
    }
    order."${contractAppPrefix}${prefix}${params.code ?: param}Saved" = result
    return result
  }

  Boolean deleteContractAppAddParam(Map input = [:]) {
    Map params = [
      contractAppPrefix : '',
      prefix            : '',
      param             : '',
      code              : '',
      force             : false
    ] + input

    String contractAppPrefix = "${capitalize(params.contractAppPrefix)}ContractApp"
    String prefix = capitalize(params.prefix)
    String param  = capitalize(params.param)

    def contractAppId = order."${contractAppPrefix}Id"
    def value         = order."${contractAppPrefix}${prefix}${params.code ?: param}" ?: order."${contractAppPrefix}${prefix}${params.code ?: param}Id"

    Boolean result = true

    if (params.force) {
      result = hydra.deleteDocumentAddParam(
        docId : contractAppId,
        param : params.code ?: "DOC_VAL_${param}"
      )
    } else {
      result = hydra.deleteDocumentAddParam(
        docId : contractAppId,
        param : params.code ?: "DOC_VAL_${param}",
        value : value // multiple add param support
      )
    }

    order."${contractAppPrefix}${prefix}${params.code ?: param}Deleted" = result
    return result
  }

  void fetchAddAgreement(Map input = [:]) {
    Map params = [
      contractPrefix : '',
      prefix         : ''
    ] + input

    String prefix = "${capitalize(params.prefix)}AddAgreement"
    String contractPrefix = "${capitalize(params.contractPrefix)}Contract"

    Map addAgreement = hydra.getAddAgreement(order."${prefix}Id")

    order."${prefix}Number" = addAgreement?.vc_doc_no
    order."${prefix}Name"   = addAgreement?.vc_name
    if (isEmpty(order."${contractPrefix}Id")) {
      order."${contractPrefix}Id" = addAgreement?.n_parent_doc_id
    }
  }

  Boolean createAddAgreement(Map input = [:]) {
    Map params = [
      contractPrefix : '',
      prefix         : ''
    ] + input

    String prefix = "${capitalize(params.prefix)}AddAgreement"
    String contractPrefix = "${capitalize(params.contractPrefix)}Contract"

    Map addAgreement = hydra.putAddAgreement(
      parentDocId : order."${contractPrefix}Id",
      number      : order."${prefix}Number"
    )
    Boolean result = false
    if (addAgreement) {
      order."${prefix}Id" = addAgreement.num_N_DOC_ID
      result = true
    }
    order."${prefix}Created" = result
    return result
  }

  Boolean dissolveAddAgreement(Map input = [:]) {
    Map params = [
      prefix  : '',
      endDate : local()
    ] + input

    String prefix = "${capitalize(params.prefix)}AddAgreement"

    Boolean result = hydra.dissolveAddAgreement(
      docId   : order."${prefix}Id",
      endDate : params.endDate
    )
    if (result) {
      order."${prefix}DissolveDate" = params.endDate
    }
    order."${prefix}Dissolved" = result
    return result
  }

  def fetchAddAgreementAddParam(Map input = [:]) {
    Map params = [
      addAgreementPrefix : '',
      prefix             : '',
      param              : '',
      code               : ''
    ] + input

    String addAgreementPrefix = "${capitalize(params.addAgreementPrefix)}AddAgreement"
    String prefix = capitalize(params.prefix)
    String param  = capitalize(params.param)

    def addAgreementId = order."${addAgreementPrefix}Id" ?: [is: 'null']
    Map addParam = hydra.getDocumentAddParamBy(
      docId : addAgreementId,
      param : params.code ?: "DOC_VAL_${param}"
    )
    def (value, valueType) = hydra.getAddParamValue(addParam)
    order."${addAgreementPrefix}${prefix}${params.code ?: param}${valueType == 'refId' ? 'Id': ''}" = value
    return value
  }

  Boolean saveAddAgreementAddParam(Map input = [:]) {
    Map params = [
      addAgreementPrefix : '',
      prefix             : '',
      param              : '',
      code               : ''
    ] + input

    String addAgreementPrefix = "${capitalize(params.addAgreementPrefix)}AddAgreement"
    String prefix = capitalize(params.prefix)
    String param  = capitalize(params.param)

    def addAgreementId = order."${addAgreementPrefix}Id"
    def value      = order."${addAgreementPrefix}${prefix}${params.code ?: param}" ?: order."${addAgreementPrefix}${prefix}${params.code ?: param}Id"

    Map addParam = hydra.addDocumentAddParam(
      docId : addAgreementId,
      param : params.code ?: "DOC_VAL_${param}",
      value : value
    )
    Boolean result = false
    if (addParam) {
      result = true
    }
    order."${addAgreementPrefix}${prefix}${params.code ?: param}Saved" = result
    return result
  }

  Boolean deleteAddAgreementAddParam(Map input = [:]) {
    Map params = [
      addAgreementPrefix : '',
      prefix             : '',
      param              : '',
      code               : '',
      force              : false
    ] + input

    String addAgreementPrefix = "${capitalize(params.addAgreementPrefix)}AddAgreement"
    String prefix = capitalize(params.prefix)
    String param  = capitalize(params.param)

    def addAgreementId = order."${addAgreementPrefix}Id"
    def value          = order."${addAgreementPrefix}${prefix}${params.code ?: param}" ?: order."${addAgreementPrefix}${prefix}${params.code ?: param}Id"

    Boolean result = true

    if (params.force) {
      result = hydra.deleteDocumentAddParam(
        docId : addAgreementId,
        param : params.code ?: "DOC_VAL_${param}"
      )
    } else {
      result = hydra.deleteDocumentAddParam(
        docId : addAgreementId,
        param : params.code ?: "DOC_VAL_${param}",
        value : value // multiple add param support
      )
    }
    order."${addAgreementPrefix}${prefix}${params.code ?: param}Deleted" = result
    return result
  }
}