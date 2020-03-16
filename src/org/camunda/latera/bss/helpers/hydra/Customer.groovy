package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.StringUtil.isEmpty
import static org.camunda.latera.bss.utils.StringUtil.capitalize

/**
  * Customer helper methods collection
  */
trait Customer {
  /**
   * Get customer data by id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*CustomerId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*CustomerCode}    {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*CustomerGroupId} {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*CustomerStateId} {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*CustomerFirmId}  {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*BaseSubjectId}   {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * @param prefix        {@link CharSequence String}. Customer prefix. Optional. Default: empty string
   * @param subjectPrefix {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   */
  void fetchCustomer(Map input = [:]) {
    Map params = [
      subjectPrefix : '',
      prefix        : ''
    ] + input

    String subjectPrefix  = "${capitalize(params.subjectPrefix)}BaseSubject"
    String customerPrefix = "${capitalize(params.prefix)}Customer"

    def customerId = order."${customerPrefix}Id"
    if (isEmpty(customerId)) {
      return
    }

    Map customer = hydra.getCustomer(customerId)

    if (isEmpty(order."${subjectPrefix}Id")) {
      order."${subjectPrefix}Id" = customer?.n_base_subject_id
    }
    if (isEmpty(order."${customerPrefix}Code")) {
      order."${customerPrefix}Code" = customer?.vc_code
    }
    if (isEmpty(order."${customerPrefix}GroupId")) {
      order."${customerPrefix}GroupId" = customer?.n_subj_group_id
    }
    if (isEmpty(order."${customerPrefix}FirmId")) {
      order."${customerPrefix}FirmId" = customer?.n_firm_id
    }
    if (isEmpty(order."${customerPrefix}StateId")) {
      order."${customerPrefix}StateId" = customer?.n_subj_state_id
    }
  }

  /**
   * Get customer hid account id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*AccountId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Customer}   Id   {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*CustomerCode}    {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*CustomerGroupId} {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*CustomerStateId} {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*CustomerFirmId}  {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*BaseSubjectId}   {@link java.math.BigInteger BigInteger}</li>
   * @param prefix        {@link CharSequence String}. Customer prefix. Optional. Default: empty string
   * @param accountPrefix {@link CharSequence String}. Account prefix. Optional. Default: empty string
   * @param subjectPrefix {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   */
  void fetchCustomerByAccount(Map input = [:]) {
    Map params = [
      subjectPrefix : '',
      accountPrefix : '',
      prefix        : ''
    ] + input

    String accountPrefix  = "${capitalize(params.accountPrefix)}Account"
    String customerPrefix = "${capitalize(params.prefix)}Customer"

    def accountId = order."${accountPrefix}Id"
    if (isEmpty(accountId)) {
      return
    }

    Map account = hydra.getAccount(accountId)
    order."${customerPrefix}Id" = account?.n_subject_id
    fetchCustomer(input)
  }

  /**
   * Create customer and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubjectId}   {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*CustomerGroupId} {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*CustomerCode}    {@link CharSequence String}. Optional</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*CustomerId}      {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*CustomerCreated} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param prefix        {@link CharSequence String}. Customer prefix. Optional. Default: empty string
   * @param subjectPrefix {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   * @return True if customer was created successfully, false otherwise
   */
  Boolean createCustomer(Map input = [:]) {
    Map params = [
      subjectPrefix : '',
      prefix        : ''
    ] + input

    String subjectPrefix  = "${capitalize(params.subjectPrefix)}BaseSubject"
    String customerPrefix = "${capitalize(params.prefix)}Customer"

    Map customer = hydra.createCustomer(
      baseSubjectId : order."${subjectPrefix}Id",
      code          : order."${customerPrefix}Code",
      groupId       : order."${customerPrefix}GroupId"
    )
    Boolean result = false
    if (customer) {
      order."${customerPrefix}Id" = customer.num_N_SUBJECT_ID
      fetchCustomer(input)
      result = true
    }
    order."${customerPrefix}Created" = result
    return result
  }

  /**
   * Add customer-group bind and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*CustomerId}       {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Customer*GroupId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Customer*Group*BindId}      {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Customer*Group*BindCreated} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param prefix         {@link CharSequence String}.  Customer-group bind prefix. Optional. Default: empty string
   * @param groupPrefix    {@link CharSequence String}.  Group prefix. Optional. Default: empty string
   * @param customerPrefix {@link CharSequence String}.  Customer prefix. Optional. Default: empty string
   * @param isMain         {@link Boolean}. Make group main group of customer. Optional. Default: false
   * @return True if customer-group bind was added successfully, false otherwise
   */
  Boolean addCustomerGroupBind(Map input = [:]) {
    Map params = [
      customerPrefix : '',
      groupPrefix    : '',
      prefix         : '',
      isMain         : false
    ] + input

    String customerPrefix = "${capitalize(params.customerPrefix)}Customer"
    String groupPrefix    = "${customerPrefix}${capitalize(params.groupPrefix)}Group"
    String bindPrefix     = "${groupPrefix}${capitalize(params.prefix)}Bind"

    Map existingGroup = hydra.getCustomerGroupBy(
      customerId : order."${customerPrefix}Id",
      groupId    : order."${groupPrefix}Id"
    )

    Boolean result = false
    if (existingGroup) {
      order."${bindPrefix}Id" = existingGroup.num_N_SUBJ_SUBJECT_ID
      result = true
    } else {
      Map newGroup = hydra.addCustomerGroup(
        order."${customerPrefix}Id",
        groupId : order."${groupPrefix}Id",
        isMain  : params.isMain
      )
      if (newGroup) {
        order."${bindPrefix}Id" = newGroup.num_N_SUBJ_SUBJECT_ID
        result = true
      }
    }
    order."${bindPrefix}Added" = result
    return result
  }

  /**
   * Delete customer-group bind and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Customer*Group*BindId} {@link java.math.BigInteger BigInteger}. Optional if customer id and group id are set instead</li>
   *   <li>{@code homsOrderData*CustomerId}       {@link java.math.BigInteger BigInteger}. Optional if customer-group bind id is set instead</li>
   *   <li>{@code homsOrderData*Customer*GroupId} {@link java.math.BigInteger BigInteger}. Optional if customer-group bind id is set instead</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Customer*Group*BindDeleted} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param prefix         {@link CharSequence String}.  Customer-group bind prefix. Optional. Default: empty string
   * @param groupPrefix    {@link CharSequence String}.  Group prefix. Optional. Default: empty string
   * @param customerPrefix {@link CharSequence String}.  Customer prefix. Optional. Default: empty string
   * @param isMain         {@link Boolean}. Is group is main group of customer. Optional. Default: false
   * @return True if customer-group bind was deleted successfully, false otherwise
   */
  Boolean deleteCustomerGroupBind(Map input = [:]) {
    Map params = [
      customerPrefix : '',
      groupPrefix    : '',
      prefix         : '',
      isMain         : false
    ] + input

    String customerPrefix = "${capitalize(params.customerPrefix)}Customer"
    String groupPrefix = "${customerPrefix}${capitalize(params.groupPrefix)}Group"
    String bindPrefix = "${groupPrefix}${capitalize(params.prefix)}Bind"

    Boolean result = hydra.deleteCustomerGroup(
      subjSubjectId : order."${bindPrefix}Id",
      customerId    : order."${customerPrefix}Id",
      groupId       : order."${groupPrefix}Id",
      isMain        : params.isMain
    )
    order."${bindPrefix}Deleted" = result
    return result
  }

  /**
   * Get customer network service subscription data by price line id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*CustomerId}    {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment*Id}  {@link java.math.BigInteger BigInteger}. Is used only if withEquipment = true</li>
   *   <li>{@code homsOrderData%NetService%Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Customer%NetService%Id}       {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Customer%NetService%Login}    {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Customer%NetService%Password} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * @param prefix           {@link CharSequence String}.  Customer prefix. Optional. Default: empty string
   * @param netServicePrefix {@link CharSequence String}.  Network service name part in variable. Optional. Default: 'NetService'
   * @param equipmentPrefix  {@link CharSequence String}.  Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix  {@link CharSequence String}.  Equipment suffix. Optional. Default: empty string
   * @param withEquipment    {@link Boolean}. Is net service subscription based on equipment id. Optional. Default: false
   */
  void fetchNetServiceAccess(Map input = [:]) {
    Map params = [
      netServicePrefix : '',
      equipmentPrefix  : '',
      equipmentSuffix  : '',
      prefix           : '',
      withEquipment    : false
    ] + input

    String netServicePrefix = capitalize(params.netServicePrefix) ?: 'NetService'
    String equipmentPrefix  = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}"
    String customerPrefix   = "${capitalize(params.prefix)}Customer"

    Map options = [
      customerId   : order."${customerPrefix}Id",
      netServiceId : order."${netServicePrefix}Id"
    ]

    if (params.withEquipment) {
      options.objectId = order."${equipmentPrefix}Id"
    }

    Map access = hydra.getCustomerNetServiceAccessBy(options)
    if (isEmpty(order."${customerPrefix}${netServicePrefix}Id")) {
      order."${customerPrefix}${netServicePrefix}Id"       = access?.n_subj_service_id
    }
    if (isEmpty(order."${customerPrefix}${netServicePrefix}Login")) {
      order."${customerPrefix}${netServicePrefix}Login"    = access?.vc_login ?: access?.vc_login_real
    }
    if (isEmpty(order."${customerPrefix}${netServicePrefix}Password")) {
      order."${customerPrefix}${netServicePrefix}Password" = access?.vc_pass
    }
  }

  /**
   * Get customer application subscription data by price line id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*CustomerId}     {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData%Application%Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Customer%Application%Id}       {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Customer%Application%Login}    {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Customer%Application%Password} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * @param application {@link CharSequence String}.  Application code, e.g. 'NETSERV_ISP_Office'
   * @param prefix      {@link CharSequence String}.  Customer prefix. Optional. Default: empty string
   * @param appPrefix   {@link CharSequence String}.  Application name part in variable. Optional. Default: 'Application'
   */
  void fetchAppAccess(Map input = [:]) {
    Map params = [
      application   : '',
      appPrefix     : '',
      prefix        : ''
    ] + input

    String appPrefix      = capitalize(params.appPrefix) ?: 'Application'
    String customerPrefix = "${capitalize(params.prefix)}Customer"

    Map access = hydra.getCustomerAppAccessBy(
      customerId  : order."${customerPrefix}Id",
      application : params.application
    )
    if (isEmpty(order."${customerPrefix}${appPrefix}Id")) {
      order."${customerPrefix}${appPrefix}Id"       = access?.n_subj_service_id
    }
    if (isEmpty(order."${customerPrefix}${appPrefix}Login")) {
      order."${customerPrefix}${appPrefix}Login"    = access?.vc_login ?: access?.vc_login_real
    }
    if (isEmpty(order."${customerPrefix}${appPrefix}Password")) {
      order."${customerPrefix}${appPrefix}Password" = access?.vc_pass
    }
  }

  /**
   * Get customer self-care subscription data by price line id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*CustomerId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*CustomerSelfCareId}       {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*CustomerSelfCareLogin}    {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*CustomerSelfCarePassword} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * @param prefix    {@link CharSequence String}.  Customer prefix. Optional. Default: empty string
   * @param appPrefix {@link CharSequence String}.  Application prefix. Optional. Default: empty string
   */
  void fetchSelfCareAccess(Map input = [:]) {
    fetchAppAccess(input + [application: 'NETSERV_ARM_Private_Office', appPrefix: "${input.appPrefix ?: ''}SelfCare"])
  }

  /**
   * Add network service subscription to a customer and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*CustomerId}                    {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData%NetService%Id}                {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment*Id}                  {@link java.math.BigInteger BigInteger}. Optional, used only if withEquipment = true</li>
   *   <li>{@code homsOrderData*Customer%NetService%Login}    {@link java.math.BigInteger BigInteger}. Optional</li>
   *   <li>{@code homsOrderData*Customer%NetService%Password} {@link java.math.BigInteger BigInteger}. Optional</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Customer%NetService%Id}          {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Customer%NetService%AccessAdded} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param prefix           {@link CharSequence String}.  Customer prefix. Optional. Default: empty string
   * @param netServicePrefix {@link CharSequence String}.  Network name part in variable. Optional. Default: 'NetService'
   * @param equipmentPrefix  {@link CharSequence String}.  Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix  {@link CharSequence String}.  Equipment suffix. Optional. Default: empty string
   * @param withEquipment    {@link Boolean}. Is net service subscription based on equipment id. Optional. Default: false
   * @return True if network service subsription was added successfully, false otherwise
   */
  Boolean addNetServiceAccess(Map input = [:]) {
    Map params = [
      netServicePrefix : '',
      equipmentPrefix  : '',
      equipmentSuffix  : '',
      prefix           : '',
      withEquipment    : false
    ] + input

    String netServicePrefix = capitalize(params.netServicePrefix) ?: 'NetService'
    String equipmentPrefix  = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}"
    String customerPrefix   = "${capitalize(params.prefix)}Customer"

    Map options = [
      netServiceId : order."${netServicePrefix}Id",
      login        : order."${customerPrefix}${netServicePrefix}Login",
      password     : order."${customerPrefix}${netServicePrefix}Password"
    ]

    if (params.withEquipment) {
      options.objectId = order."${equipmentPrefix}Id"
    }

    Map access = hydra.addCustomerNetServiceAccess(options, order."${customerPrefix}Id")
    Boolean result = false
    if (access) {
      order."${customerPrefix}${netServicePrefix}Id"       = access.num_N_SUBJ_SERV_ID
      order."${customerPrefix}${netServicePrefix}Login"    = access.vch_VC_LOGIN
      order."${customerPrefix}${netServicePrefix}Password" = access.vch_VC_PASS
      result = true
    }
    order."${customerPrefix}${netServicePrefix}AccessAdded" = result
    return result
  }

  /**
   * Add application subscription to a customer and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*CustomerId}                     {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Customer%Application%Login}    {@link java.math.BigInteger BigInteger}. Optional</li>
   *   <li>{@code homsOrderData*Customer%Application%Password} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Customer%Application%Id}          {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Customer%Application%AccessAdded} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param application {@link CharSequence String}.  Application code, e.g. 'NETSERV_ISP_Office'
   * @param prefix      {@link CharSequence String}.  Customer prefix. Optional. Default: empty string
   * @param appPrefix   {@link CharSequence String}.  Application name part in variable. Optional. Default: 'Application'
   * @return True if application subsription was added successfully, false otherwise
   */
  Boolean addAppAccess(Map input = [:]) {
    Map params = [
      application : '',
      appPrefix   : '',
      prefix      : ''
    ] + input

    String appPrefix      = capitalize(params.appPrefix) ?: 'Application'
    String customerPrefix = "${capitalize(params.prefix)}Customer"

    Map access = hydra.addCustomerAppAccess(
      order."${customerPrefix}Id",
      application : params.application,
      login       : order."${customerPrefix}${appPrefix}Login",
      password    : order."${customerPrefix}${appPrefix}Password"
    )

    Boolean result = false
    if (access) {
      order."${customerPrefix}${appPrefix}Id"       = access.num_N_SUBJ_SERV_ID
      order."${customerPrefix}${appPrefix}Login"    = access.vch_VC_LOGIN
      order."${customerPrefix}${appPrefix}Password" = access.vch_VC_PASS
      result = true
    }
    order."${customerPrefix}${appPrefix}AccessAdded" = result
    return result
  }

  /**
   * Add self-care access to a customer and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*CustomerId}                {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Customer*SelfCareLogin}    {@link java.math.BigInteger BigInteger}. Optional</li>
   *   <li>{@code homsOrderData*Customer*SelfCarePassword} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Customer*SelfCareId}          {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Customer*SelfCareAccessAdded} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param prefix    {@link CharSequence String}.  Customer prefix. Optional. Default: empty string
   * @param appPrefix {@link CharSequence String}.  Application prefix. Optional. Default: empty string
   * @return True if self-care access was added successfully, false otherwise
   */
  Boolean addSelfCareAccess(Map input = [:]) {
    Map params = [
      appPrefix : '',
      prefix    : ''
    ] + input

    String selfCarePrefix = "${capitalize(params.appPrefix)}SelfCare"
    String customerPrefix = "${capitalize(params.prefix)}Customer"

    Map access = hydra.addCustomerSelfCareAccess(
      order."${customerPrefix}Id",
      login      : order."${customerPrefix}${selfCarePrefix}Login",
      password   : order."${customerPrefix}${selfCarePrefix}Password"
    )
    Boolean result = false
    if (access) {
      order."${customerPrefix}${selfCarePrefix}Id"       = access.num_N_SUBJ_SERV_ID
      order."${customerPrefix}${selfCarePrefix}Login"    = access.vch_VC_LOGIN
      order."${customerPrefix}${selfCarePrefix}Password" = access.vch_VC_PASS
      result = true
    }

    order."${customerPrefix}${selfCarePrefix}AccessAdded" = result
    return result
  }

  /**
   * Disable customer and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*CustomerId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*CustomerDisabled} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param prefix {@link CharSequence String}. Customer prefix. Optional. Default: empty string
   * @return True if customer was disabled successfully, false otherwise
   */
  Boolean disableCustomer(Map input = [:]) {
    Map params = [
      prefix : ''
    ] + input

    String customerPrefix = "${capitalize(params.prefix)}Customer"

    Boolean result = hydra.disableCustomer(order."${customerPrefix}Id")
    order."${customerPrefix}Disabled" = result
    return result
  }

  /**
   * Get customer additional parameter value and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*CustomerId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Customer*%Param%Id} {@link java.math.BigInteger BigInteger}. if additional parameter value is ref id</li>
   *   <li>{@code homsOrderData*Customer*%Param%}   Any type, if additional parameter is not a ref</li>
   * </ul>
   * @param param          {@link CharSequence String}. Additional parameter short code (=variable part name) ('Param' for 'SUBJ_VAL_Param')
   * @param code           {@link CharSequence String}. Additional parameter full code (if it does not start from 'SUBJ_VAL_')
   * @param customerPrefix {@link CharSequence String}. Customer prefix. Optional. Default: empty string
   * @param prefix         {@link CharSequence String}. Additional parameter prefix. Optional. Default: empty string
   * @return Additional parameter value
   */
  def fetchCustomerAddParam(Map input = [:]) {
    Map params = [
      customerPrefix : '',
      prefix         : '',
      param          : '',
      code           : ''
    ] + input

    String customerPrefix = "${capitalize(params.customerPrefix)}Customer"
    String prefix = capitalize(params.prefix)
    String param  = capitalize(params.param)

    def customerId = order."${customerPrefix}Id"
    if (isEmpty(customerId)) {
      return
    }

    Map addParam = hydra.getSubjectAddParamBy(
      subjectId : customerId,
      param     : params.code ?: "SUBJ_VAL_${param}"
    )
    def (value, valueType) = hydra.getAddParamValue(addParam)
    order."${customerPrefix}${prefix}${params.code ?: param}${valueType == 'refId' ? 'Id': ''}" = value
    return value
  }

  /**
   * Save customer additional parameter value and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*CustomerId}         {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Customer*%Param%Id} {@link java.math.BigInteger BigInteger}. if additional parameter value is ref id</li>
   *   <li>{@code homsOrderData*Customer*%Param%}   Any type, if additional parameter value is not a ref or ref code is used instead</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Customer*%Param%Saved} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param param          {@link CharSequence String}. Additional parameter short code (=variable part name) ('Param' for 'SUBJ_VAL_Param')
   * @param code           {@link CharSequence String}. Additional parameter full code (if it does not start from 'SUBJ_VAL_')
   * @param customerPrefix {@link CharSequence String}. Customer prefix. Optional. Default: empty string
   * @param prefix         {@link CharSequence String}. Additional parameter prefix. Optional. Default: empty string
   * @return True if additional parameter value was saved successfully, false otherwise
   */
  Boolean saveCustomerAddParam(Map input = [:]) {
    Map params = [
      customerPrefix : '',
      prefix         : '',
      param          : '',
      code           : ''
    ] + input

    String customerPrefix = "${capitalize(params.customerPrefix)}Customer"
    String prefix  = capitalize(params.prefix)
    def customerId = order."${customerPrefix}Id"
    def value      = order."${customerPrefix}${prefix}${params.code ?: param}" ?: order."${customerPrefix}${prefix}${params.code ?: param}Id"

    Map addParam = hydra.addCustomerAddParam(
      customerId,
      param : params.code ?: "SUBJ_VAL_${param}",
      value : value
    )
    Boolean result = false
    if (addParam) {
      result = true
    }
    order."${customerPrefix}${prefix}${params.code ?: param}Saved" = result
    return result
  }

  /**
   * Delete base subject additional parameter value and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*CustomerId}         {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Customer*%Param%Id} {@link java.math.BigInteger BigInteger}. if additional parameter value is ref id</li>
   *   <li>{@code homsOrderData*Customer*%Param%}   Any type, if additional parameter value is not a ref or ref code is used instead</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Customer*%Param%Deleted} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param param          {@link CharSequence String}.  Additional parameter short code (=variable part name) ('Param' for 'SUBJ_VAL_Param')
   * @param code           {@link CharSequence String}.  Additional parameter full code (if it does not start from 'SUBJ_VAL_')
   * @param customerPrefix {@link CharSequence String}.  Customer prefix. Optional. Default: empty string
   * @param prefix         {@link CharSequence String}.  Additional parameter prefix. Optional. Default: empty string
   * @param force          {@link Boolean}. For multiple additional parameters. If you need to remove only a value which is equal to one stored in the input execution variable, pass false. Otherwise method will remove additional param value without check
   * @return True if additional parameter value was deleted successfully, false otherwise
   */
  Boolean deleteCustomerAddParam(Map input = [:]) {
    Map params = [
      customerPrefix : '',
      prefix         : '',
      param          : '',
      code           : '',
      force          : false
    ] + input

    String customerPrefix = "${capitalize(params.customerPrefix)}Customer"
    String prefix = capitalize(params.prefix)
    String param  = capitalize(params.param)

    def customerId = order."${customerPrefix}Id"
    def value      = order."${customerPrefix}${prefix}${params.code ?: param}" ?: order."${customerPrefix}${prefix}${params.code ?: param}Id"

    Boolean result = true

    if (params.force) {
      result = hydra.deleteSubjectAddParam(
        subjectId : customerId,
        param     : params.code ?: "SUBJ_VAL_${param}"
      )
    } else {
      result = hydra.deleteSubjectAddParam(
        subjectId : customerId,
        param     : params.code ?: "SUBJ_VAL_${param}",
        value     : value // multiple add param support
      )
    }

    order."${customerPrefix}${prefix}${params.code ?: param}Deleted" = result
    return result
  }
}