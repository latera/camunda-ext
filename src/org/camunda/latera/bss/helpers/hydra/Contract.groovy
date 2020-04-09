package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.StringUtil.capitalize
import static org.camunda.latera.bss.utils.StringUtil.isEmpty
import static org.camunda.latera.bss.utils.DateTimeUtil.local

/**
 * Contract, contract app and add agreement helper methods collection
 */
trait Contract {
  /**
   * Get contract data by id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*ContractId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*ContractNumber} {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*ContractName}   {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*BaseContractId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * @param prefix             {@link CharSequence String}. Contract prefix. Optional. Default: empty string
   * @param baseContractPrefix {@link CharSequence String}. Base contract prefix. Optional. Default: empty string

   */
  void fetchContract(Map input = [:]) {
    Map params = [
      baseContractPrefix : '',
      prefix             : ''
    ] + input

    String baseContractPrefix = "${capitalize(params.baseContractPrefix)}BaseContract"
    String prefix = "${capitalize(params.prefix)}Contract"

    def contractId = order."${prefix}Id"
    if (isEmpty(contractId)) {
      return
    }

    Map contract  = hydra.getContract(contractId)

    order."${prefix}Number" = contract?.vc_doc_no
    order."${prefix}Name"   = contract?.vc_name
    if (isEmpty(order."${baseContractPrefix}Id")) {
      order."${baseContractPrefix}Id" = contract?.n_parent_doc_id
    }
  }

  /**
   * Get customer first contract data by customer id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*CustomerId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*ContractId}     {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*ContractNumber} {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*ContractName}   {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*BaseContractId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * @param prefix             {@link CharSequence String}. Contract prefix. Optional. Default: empty string
   * @param customerPrefix     {@link CharSequence String}. Customer prefix. Optional. Default: empty string
   * @param baseContractPrefix {@link CharSequence String}. Base contract prefix. Optional. Default: empty string
   */
  void fetchCustomerFirstContract(Map input = [:]) {
    Map params = [
      baseContractPrefix : '',
      customerPrefix     : '',
      prefix             : ''
    ] + input

    String customerPrefix = "${capitalize(params.customerPrefix)}Customer"
    String prefix = "${capitalize(params.prefix)}Contract"

    def customerId = order."${customerPrefix}Id"
    if (isEmpty(customerId)) {
      return
    }

    Map contract = hydra.getContractBy(recipientId: customerId, operationDate: local())

    order."${prefix}Id" = contract?.n_doc_id
    fetchContract(prefix: params.prefix, baseContractPrefix: params.baseContractPrefix)
  }

  /**
   * Create contract and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*CustomerId}     {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*BaseContractId} {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*ContractNumber} {@link CharSequence String}. Optional</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*ContractId}      {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*ContractCreated} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param prefix             {@link CharSequence String}. Contract prefix. Optional. Default: empty string
   * @param customerPrefix     {@link CharSequence String}. Customer prefix. Optional. Default: empty string
   * @param baseContractPrefix {@link CharSequence String}. Base contract prefix. Optional. Default: empty string
   * @return True if contract was created successfully, false otherwise
   */
  Boolean createContract(Map input = [:]) {
    Map params = [
      baseContractPrefix : '',
      customerPrefix     : '',
      prefix             : ''
    ] + input

    String baseContractPrefix = "${capitalize(params.baseContractPrefix)}BaseContract"
    String customerPrefix = "${capitalize(params.customerPrefix)}Customer"
    String prefix = "${capitalize(params.prefix)}Contract"

    Map contract = hydra.createContract(
      order."${customerPrefix}Id",
      parentDocId : order."${baseContractPrefix}Id",
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

  /**
   * Dissolve contract and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*ContractId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*ContractDissolveDate} {@link java.time.LocalDateTime LocalDateTime}</li>
   *   <li>{@code homsOrderData*ContractDissolved}    {@link Boolean}. Same as return value</li>
   * </ul>
  * @param prefix  {@link CharSequence String}. Contract prefix. Optional. Default: empty string
   * @param endDate {@link CharSequence String} {@link java.time.Temporal Any date type}. Optional. Default: current datetime
   * @return True if contract was dissolved successfully, false otherwise
   */
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

  /**
   * Get contract additional parameter value and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*ContractId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
    *   <li>{@code homsOrderData*Contract*%Param%Id} {@link java.math.BigInteger BigInteger}. if additional parameter value is ref id</li>

   *   <li>{@code homsOrderData*Contract*%Param%}   Any type, if additional parameter is not a ref</li>
   * </ul>
   * @param param          {@link CharSequence String}. Additional parameter short code (=variable part name) ('Param' for 'DOC_VAL_Param')
   * @param code           {@link CharSequence String}. Additional parameter full code (if it does not start from 'DOC_VAL_')
   * @param contractPrefix {@link CharSequence String}. Contract prefix. Optional. Default: empty string
   * @param prefix         {@link CharSequence String}. Additional parameter prefix. Optional. Default: empty string
   * @return Additional parameter value
   */
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

    def contractId = order."${contractPrefix}Id"
    if (isEmpty(contractId)) {
      return
    }

    Map addParam = hydra.getDocumentAddParamBy(
      docId : contractId,
      param : params.code ?: "DOC_VAL_${param}"
    )
    def (value, valueType) = hydra.getAddParamValue(addParam)
    order."${contractPrefix}${prefix}${params.code ?: param}${valueType == 'refId' ? 'Id': ''}" = value
    return value
  }

  /**
   * Save contract additional parameter value and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*ContractId}         {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Contract*%Param%Id} {@link java.math.BigInteger BigInteger}. if additional parameter value is ref id</li>
   *   <li>{@code homsOrderData*Contract*%Param%}   Any type, if additional parameter value is not a ref or ref code is used instead</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Contract*%Param%Saved} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param param          {@link CharSequence String}. Additional parameter short code (=variable part name) ('Param' for 'DOC_VAL_Param')
   * @param code           {@link CharSequence String}. Additional parameter full code (if it does not start from 'DOC_VAL_')
   * @param contractPrefix {@link CharSequence String}. Contract prefix. Optional. Default: empty string
   * @param prefix         {@link CharSequence String}. Additional parameter prefix. Optional. Default: empty string
   * @return True if additional parameter value was saved successfully, false otherwise
   */
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
      contractId,
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

  /**
   * Delete contract additional parameter value and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*ContractId}         {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Contract*%Param%Id} {@link java.math.BigInteger BigInteger}. if additional parameter value is ref id</li>
   *   <li>{@code homsOrderData*Contract*%Param%}   Any type, if additional parameter value is not a ref or ref code is used instead</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Contract*%Param%Deleted} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param param          {@link CharSequence String}.  Additional parameter short code (=variable part name) ('Param' for 'DOC_VAL_Param')
   * @param code           {@link CharSequence String}.  Additional parameter full code (if it does not start from 'DOC_VAL_')
   * @param contractPrefix {@link CharSequence String}.  Contract prefix. Optional. Default: empty string
   * @param prefix         {@link CharSequence String}.  Additional parameter prefix. Optional. Default: empty string
   * @param force          {@link Boolean}. For multiple additional parameters. If you need to remove only a value which is equal to one stored in the input execution variable, pass false. Otherwise method will remove additional param value without check
   * @return True if additional parameter value was deleted successfully, false otherwise
   */
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

  /**
   * Get contract app data by id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*ContractAppId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*ContractAppNumber} {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*ContractAppName}   {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*ContractId}        {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * @param prefix         {@link CharSequence String}. Contract app prefix. Optional. Default: empty string
   * @param contractPrefix {@link CharSequence String}. Contract prefix. Optional. Default: empty string
   */
  void fetchContractApp(Map input = [:]) {
    Map params = [
      contractPrefix : '',
      prefix         : ''
    ] + input

    String prefix = "${capitalize(params.prefix)}ContractApp"
    String contractPrefix = "${capitalize(params.contractPrefix)}Contract"

    def contractAppId = order."${prefix}Id"
    if (isEmpty(contractAppId)) {
      return contractAppId
    }

    Map contractApp = hydra.getContractApp(contractAppId)

    order."${prefix}Number" = contractApp?.vc_doc_no
    order."${prefix}Name"   = contractApp?.vc_name
    if (isEmpty(order."${contractPrefix}Id")) {
      order."${contractPrefix}Id" = contractApp?.n_parent_doc_id
    }
  }

  /**
   * Create contract app and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*ContractId}        {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*ContractAppNumber} {@link CharSequence String}. Optional</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*ContractAppId}      {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*ContractAppCreated} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param prefix         {@link CharSequence String}. Contract app prefix. Optional. Default: empty string
   * @param contractPrefix {@link CharSequence String}. Contract prefix. Optional. Default: empty string
   * @return True if contract app was created successfully, false otherwise
   */
  Boolean createContractApp(Map input = [:]) {
    Map params = [
      contractPrefix : '',
      prefix         : ''
    ] + input

    String prefix = "${capitalize(params.prefix)}ContractApp"
    String contractPrefix = "${capitalize(params.contractPrefix)}Contract"

    Map contractApp = hydra.createContractApp(
      order."${contractPrefix}Id",
      number : order."${prefix}Number"
    )
    Boolean result = false
    if (contractApp) {
      order."${prefix}Id" = contractApp.num_N_DOC_ID
      result = true
    }
    order."${prefix}Created" = result
    return result
  }

  /**
   * Dissolve contract app and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*ContractAppId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*ContractAppDissolveDate} {@link java.time.LocalDateTime LocalDateTime}</li>
   *   <li>{@code homsOrderData*ContractAppDissolved}    {@link Boolean}. Same as return value</li>
   * </ul>
   * @param prefix  {@link CharSequence String}. Contract app prefix. Optional. Default: empty string
   * @param endDate {@link CharSequence String} {@link java.time.Temporal Any date type}. Optional. Default: current datetime
   * @return True if contract app was dissolved successfully, false otherwise
   */
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

  /**
   * Get contract app additional parameter value and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*ContractAppId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*ContractApp*%Param%Id} {@link java.math.BigInteger BigInteger}. if additional parameter value is ref id</li>

   *   <li>{@code homsOrderData*ContractApp*%Param%}   Any type, if additional parameter is not a ref</li>
   * </ul>
   * @param param             {@link CharSequence String}. Additional parameter short code (=variable part name) ('Param' for 'DOC_VAL_Param')
   * @param code              {@link CharSequence String}. Additional parameter full code (if it does not start from 'DOC_VAL_')
   * @param contractAppPrefix {@link CharSequence String}. Contract prefix. Optional. Default: empty string
   * @param prefix            {@link CharSequence String}. Additional parameter prefix. Optional. Default: empty string
   * @return Additional parameter value
   */
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

    def contractAppId = order."${contractAppPrefix}Id"
    if (isEmpty(contractAppId)) {
      return
    }

    Map addParam = hydra.getDocumentAddParamBy(
      docId : contractAppId,
      param : params.code ?: "DOC_VAL_${param}"
    )
    def (value, valueType) = hydra.getAddParamValue(addParam)
    order."${contractAppPrefix}${prefix}${params.code ?: param}${valueType == 'refId' ? 'Id': ''}" = value
    return value
  }

  /**
   * Save contract app additional parameter value and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*ContractAppId}         {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*ContractApp*%Param%Id} {@link java.math.BigInteger BigInteger}. if additional parameter value is ref id</li>
   *   <li>{@code homsOrderData*ContractApp*%Param%}   Any type, if additional parameter value is not a ref or ref code is used instead</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*ContractApp*%Param%Saved} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param param             {@link CharSequence String}. Additional parameter short code (=variable part name) ('Param' for 'DOC_VAL_Param')
   * @param code              {@link CharSequence String}. Additional parameter full code (if it does not start from 'DOC_VAL_')
   * @param contractAppPrefix {@link CharSequence String}. Contract prefix. Optional. Default: empty string
   * @param prefix            {@link CharSequence String}. Additional parameter prefix. Optional. Default: empty string
   * @return True if additional parameter value was saved successfully, false otherwise
   */
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
      contractAppId,
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

  /**
   * Delete contract app additional parameter value and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*ContractAppId}         {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*ContractApp*%Param%Id} {@link java.math.BigInteger BigInteger}. if additional parameter value is ref id</li>
   *   <li>{@code homsOrderData*ContractApp*%Param%}   Any type, if additional parameter value is not a ref or ref code is used instead</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*ContractApp*%Param%Deleted} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param param             {@link CharSequence String}.  Additional parameter short code (=variable part name) ('Param' for 'DOC_VAL_Param')
   * @param code              {@link CharSequence String}.  Additional parameter full code (if it does not start from 'DOC_VAL_')
   * @param contractAppPrefix {@link CharSequence String}.  Contract prefix. Optional. Default: empty string
   * @param prefix            {@link CharSequence String}.  Additional parameter prefix. Optional. Default: empty string
   * @param force             {@link Boolean}. For multiple additional parameters. If you need to remove only a value which is equal to one stored in the input execution variable, pass false. Otherwise method will remove additional param value without check
   * @return True if additional parameter value was deleted successfully, false otherwise
   */
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

  /**
   * Get add agreement data by id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*AddAgreementId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*AddAgreementNumber} {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*AddAgreementName}   {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*ContractId}         {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * @param prefix         {@link CharSequence String}. Add agreement prefix. Optional. Default: empty string
   * @param contractPrefix {@link CharSequence String}. Contract prefix. Optional. Default: empty string
   */
  void fetchAddAgreement(Map input = [:]) {
    Map params = [
      contractPrefix : '',
      prefix         : ''
    ] + input

    String prefix = "${capitalize(params.prefix)}AddAgreement"
    String contractPrefix = "${capitalize(params.contractPrefix)}Contract"

    def addAgreementId = order."${prefix}Id"
    if (isEmpty(addAgreementId)) {
      return addAgreementId
    }

    Map addAgreement = hydra.getAddAgreement(addAgreementId)

    order."${prefix}Number" = addAgreement?.vc_doc_no
    order."${prefix}Name"   = addAgreement?.vc_name
    if (isEmpty(order."${contractPrefix}Id")) {
      order."${contractPrefix}Id" = addAgreement?.n_parent_doc_id
    }
  }

  /**
   * Create contract app and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*ContractId}         {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*AddAgreementNumber} {@link CharSequence String}. Optional</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*AddAgreementId}      {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*AddAgreementCreated} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param prefix         {@link CharSequence String}. Add agreement prefix. Optional. Default: empty string
   * @param contractPrefix {@link CharSequence String}. Contract prefix. Optional. Default: empty string
   * @return True if add agreement was created successfully, false otherwise
   */
  Boolean createAddAgreement(Map input = [:]) {
    Map params = [
      contractPrefix : '',
      prefix         : ''
    ] + input

    String prefix = "${capitalize(params.prefix)}AddAgreement"
    String contractPrefix = "${capitalize(params.contractPrefix)}Contract"

    Map addAgreement = hydra.createAddAgreement(
      order."${contractPrefix}Id",
      number : order."${prefix}Number"
    )
    Boolean result = false
    if (addAgreement) {
      order."${prefix}Id" = addAgreement.num_N_DOC_ID
      result = true
    }
    order."${prefix}Created" = result
    return result
  }

  /**
   * Dissolve add agreement and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*AddAgreementId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*AddAgreementDissolveDate} {@link java.time.LocalDateTime LocalDateTime}</li>
   *   <li>{@code homsOrderData*AddAgreementDissolved}    {@link Boolean}. Same as return value</li>
   * </ul>
   * @param prefix         {@link CharSequence String}. Add agreement prefix. Optional. Default: empty string
   * @param endDate {@link CharSequence String} {@link java.time.Temporal Any date type}. Optional. Default: current datetime
   * @return True if add agreement was dissolved successfully, false otherwise
   */
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

  /**
   * Get add agreement additional parameter value and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*AddAgreementId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*AddAgreement*%Param%Id} {@link java.math.BigInteger BigInteger}. if additional parameter value is ref id</li>
   *   <li>{@code homsOrderData*AddAgreement*%Param%}   Any type, if additional parameter is not a ref</li>
   * </ul>
   * @param param              {@link CharSequence String}. Additional parameter short code (=variable part name) ('Param' for 'DOC_VAL_Param')
   * @param code               {@link CharSequence String}. Additional parameter full code (if it does not start from 'DOC_VAL_')
   * @param addAgreementPrefix {@link CharSequence String}. Add agreement prefix. Optional. Default: empty string
   * @param prefix             {@link CharSequence String}. Additional parameter prefix. Optional. Default: empty string
   * @return Additional parameter value
   */
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

    def addAgreementId = order."${addAgreementPrefix}Id"
    if (isEmpty(addAgreementId)) {
      return
    }

    Map addParam = hydra.getDocumentAddParamBy(
      docId : addAgreementId,
      param : params.code ?: "DOC_VAL_${param}"
    )
    def (value, valueType) = hydra.getAddParamValue(addParam)
    order."${addAgreementPrefix}${prefix}${params.code ?: param}${valueType == 'refId' ? 'Id': ''}" = value
    return value
  }

  /**
   * Save add agreement additional parameter value and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*AddAgreementId}         {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*AddAgreement*%Param%Id} {@link java.math.BigInteger BigInteger}. if additional parameter value is ref id</li>
   *   <li>{@code homsOrderData*AddAgreement*%Param%}   Any type, if additional parameter value is not a ref or ref code is used instead</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*AddAgreement*%Param%Saved} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param param              {@link CharSequence String}. Additional parameter short code (=variable part name) ('Param' for 'DOC_VAL_Param')
   * @param code               {@link CharSequence String}. Additional parameter full code (if it does not start from 'DOC_VAL_')
   * @param addAgreementPrefix {@link CharSequence String}. Add agreement prefix. Optional. Default: empty string
   * @param prefix             {@link CharSequence String}. Additional parameter prefix. Optional. Default: empty string
   * @return True if additional parameter value was saved successfully, false otherwise
   */
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
      addAgreementId,
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

  /**
   * Delete add agreement additional parameter value and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*AddAgreementId}         {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*AddAgreement*%Param%Id} {@link java.math.BigInteger BigInteger}. if additional parameter value is ref id</li>
   *   <li>{@code homsOrderData*AddAgreement*%Param%}   Any type, if additional parameter value is not a ref or ref code is used instead</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*AddAgreement*%Param%Deleted} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param param              {@link CharSequence String}.  Additional parameter short code (=variable part name) ('Param' for 'DOC_VAL_Param')
   * @param code               {@link CharSequence String}.  Additional parameter full code (if it does not start from 'DOC_VAL_')
   * @param addAgreementPrefix {@link CharSequence String}.  Add agreement prefix. Optional. Default: empty string
   * @param prefix             {@link CharSequence String}.  Additional parameter prefix. Optional. Default: empty string
   * @param force              {@link Boolean}. For multiple additional parameters. If you need to remove only a value which is equal to one stored in the input execution variable, pass false. Otherwise method will remove additional param value without check
   * @return True if additional parameter value was deleted successfully, false otherwise
   */
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