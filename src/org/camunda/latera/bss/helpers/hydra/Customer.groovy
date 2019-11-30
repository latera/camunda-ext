package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.StringUtil.isEmpty
import static org.camunda.latera.bss.utils.StringUtil.capitalize

trait Customer {
  void fetchCustomer(Map input = [:]) {
    Map params = [
      subjectPrefix : '',
      prefix        : ''
    ] + input

    String subjectPrefix  = "${capitalize(params.subjectPrefix)}BaseSubject"
    String customerPrefix = "${capitalize(params.prefix)}Customer"

    def customerId = order."${customerPrefix}Id" ?: [is: 'null']
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

  void fetchCustomerByAccount(Map input = [:]) {
    Map params = [
      subjectPrefix : '',
      accountPrefix : '',
      prefix        : ''
    ] + input

    String accountPrefix  = "${capitalize(params.accountPrefix)}Account"
    String customerPrefix = "${capitalize(params.prefix)}Customer"

    def accountId = order."${accountPrefix}Id" ?: [is: 'null']
    Map account = hydra.getAccount(accountId)
    order."${customerPrefix}Id" = account?.n_subject_id
    fetchCustomer(input)
  }

  Boolean createCustomer(Map input = [:]) {
    Map params = [
      subjectPrefix : '',
      prefix        : ''
    ] + input

    String subjectPrefix  = "${capitalize(params.subjectPrefix)}BaseSubject"
    String customerPrefix = "${capitalize(params.prefix)}Customer"

    Map customer = hydra.putCustomer(
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
      Map newGroup = hydra.putCustomerGroup(
        customerId : order."${customerPrefix}Id",
        groupId    : order."${groupPrefix}Id",
        isMain     : params.isMain
      )
      if (newGroup) {
        order."${bindPrefix}Id" = group.num_N_SUBJ_SUBJECT_ID
        result = true
      }
    }
    order."${bindPrefix}Added" = result
    return result
  }

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

  void fetchSelfCareAccess(Map input = [:]) {
    fetchAppAccess(input + [application: 'NETSERV_ARM_Private_Office', appPrefix: "${input.appPrefix ?: ''}SelfCare"])
  }

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
      customerId   : order."${customerPrefix}Id",
      netServiceId : order."${netServicePrefix}Id",
      login        : order."${customerPrefix}${netServicePrefix}Login",
      password     : order."${customerPrefix}${netServicePrefix}Password"
    ]

    if (params.withEquipment) {
      options.objectId = order."${equipmentPrefix}Id"
    }

    Map access = hydra.putCustomerNetServiceAccess(options)
    Boolean result = false
    if (access) {
      order."${customerPrefix}${netServicePrefix}Login"    = access.vch_VC_LOGIN
      order."${customerPrefix}${netServicePrefix}Password" = access.vch_VC_PASS
      result = true
    }
    order."${customerPrefix}${netServicePrefix}AccessAdded" = result
    return result
  }

  Boolean addAppAccess(Map input = [:]) {
    Map params = [
      application : '',
      appPrefix   : '',
      prefix      : ''
    ] + input

    String appPrefix      = capitalize(params.appPrefix) ?: 'Application'
    String customerPrefix = "${capitalize(params.prefix)}Customer"

    Map access = hydra.putCustomerAppAccess(
      customerId  : order."${customerPrefix}Id",
      application : params.application,
      login       : order."${customerPrefix}${appPrefix}Login",
      password    : order."${customerPrefix}${appPrefix}Password"
    )

    Boolean result = false
    if (access) {
      order."${customerPrefix}${appPrefix}Login"    = access.vch_VC_LOGIN
      order."${customerPrefix}${appPrefix}Password" = access.vch_VC_PASS
      result = true
    }
    order."${customerPrefix}${appPrefix}AccessAdded" = result
    return result
  }

  Boolean addSelfCareAccess(Map input = [:]) {
    Map params = [
      appPrefix : '',
      prefix    : ''
    ] + input

    String selfCarePrefix = "${capitalize(params.appPrefix)}SelfCare"
    String customerPrefix = "${capitalize(params.prefix)}Customer"

    Map access = hydra.putCustomerSelfCareAccess(
      customerId : order."${customerPrefix}Id",
      login      : order."${customerPrefix}${selfCarePrefix}Login",
      password   : order."${customerPrefix}${selfCarePrefix}Password"
    )
    Boolean result = false
    if (access) {
      order."${customerPrefix}${selfCarePrefix}Login"    = access.vch_VC_LOGIN
      order."${customerPrefix}${selfCarePrefix}Password" = access.vch_VC_PASS
      result = true
    }

    order."${customerPrefix}${selfCarePrefix}AccessAdded" = result
    return result
  }

  Boolean disableCustomer(Map input = [:]) {
    Map params = [
      prefix : ''
    ] + input

    String customerPrefix = "${capitalize(params.prefix)}Customer"

    Boolean result = hydra.disableCustomer(order."${customerPrefix}Id")
    order."${customerPrefix}Disabled" = result
    return result
  }

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

    def customerId = order."${customerPrefix}Id" ?: [is: 'null']
    Map addParam = hydra.getSubjectAddParamBy(
      subjectId : customerId,
      param     : params.code ?: "SUBJ_VAL_${param}"
    )
    def (value, valueType) = hydra.getAddParamValue(addParam)
    order."${customerPrefix}${prefix}${params.code ?: param}${valueType == 'refId' ? 'Id': ''}" = value
    return value
  }

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
      customerId : customerId,
      param      : params.code ?: "SUBJ_VAL_${param}",
      value      : value
    )
    Boolean result = false
    if (addParam) {
      result = true
    }
    order."${customerPrefix}${prefix}${params.code ?: param}Saved" = result
    return result
  }

  Boolean deleteCustomerAddParam(Map input = [:]) {
    Map params = [
      customerPrefix : '',
      prefix         : '',
      param          : '',
      code           : '',
      force          : true
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