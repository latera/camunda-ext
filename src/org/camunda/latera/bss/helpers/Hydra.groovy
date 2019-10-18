package org.camunda.latera.bss.helpers

import java.util.regex.Pattern
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.latera.bss.connectors.hid.Hydra
import org.camunda.latera.bss.http.HTTPRestProcessor
import org.camunda.latera.bss.logging.SimpleLogger
import org.camunda.latera.bss.utils.DateTimeUtil
import org.camunda.latera.bss.utils.Numeric
import org.camunda.latera.bss.utils.Oracle
import org.camunda.latera.bss.utils.Order
import org.camunda.latera.bss.utils.StringUtil

class Hydra {
  // TODO: Move to Account trait
  static void fetchAccount(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      prefix : '',
      hydra  : null
    ] + input

    def prefix = StringUtil.capitalize(params.prefix)
    def hydra = params.hydra ?: new Hydra(execution)

    def account = hydra.getAccount(execution.getVariable("homsOrderData${prefix}AccountId"))
    def balance = hydra.getAccountBalance(execution.getVariable("homsOrderData${prefix}AccountId"))
    execution.setVariable("homsOrderData${prefix}AccountNumber",     account?.vc_account)
    execution.setVariable("homsOrderData${prefix}AccountBalanceSum", Numeric.toFloatSafe(balance?.n_sum_bal?.replace(',','.'), 0.0))
    execution.setVariable("homsOrderData${prefix}AccountFreeSum",    Numeric.toFloatSafe(balance?.n_sum_free?.replace(',','.'), 0.0))
  }

  static void fetchCustomerAccount(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      customerPrefix : '',
      prefix         : '',
      hydra          : null
    ] + input

    def customerPrefix = StringUtil.capitalize(params.customerPrefix)
    def prefix = StringUtil.capitalize(params.prefix)
    def hydra = params.hydra ?: new Hydra(execution)

    def account = hydra.getCustomerAccount(execution.getVariable("homsOrderData${customerPrefix}CustomerId"))
    execution.setVariable("homsOrderData${prefix}AccountId", account?.n_account_id)
    fetchAccount(execution, prefix: prefix, hydra: hydra)
  }

  static void createCustomerAccount(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      customerPrefix : '',
      prefix         : '',
      hydra          : null
    ] + input

    def customerPrefix = StringUtil.capitalize(params.customerPrefix)
    def prefix = StringUtil.capitalize(params.prefix)
    def hydra = params.hydra ?: new Hydra(execution)

    def account = hydra.putCustomerAccount(
      customerId : execution.getVariable("homsOrderData${customerPrefix}CustomerId"),
      number     : execution.getVariable("homsOrderData${prefix}AccountNumber")
    )
    if (account) {
      execution.setVariable("homsOrderData${prefix}AccountId", account?.num_N_ACCOUNT_ID)
      execution.setVariable("homsOrderData${prefix}AccountCreated", true)
    }
  }

  // TODO: Move to Address trait
  static void fetchBaseSubjectContacts(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : '',
      hydra         : null
    ] + input
    fetchBaseSubjectPhone(execution, subjectPrefix : params.subjectPrefix,
                                    subjectSuffix : params.subjectSuffix,
                                    prefix        : params.prefix,
                                    hydra         : params.hydra)
    fetchBaseSubjectEmail(execution, subjectPrefix : params.subjectPrefix,
                                    subjectSuffix : params.subjectSuffix,
                                    prefix        : params.prefix,
                                    hydra         : params.hydra)
  }

  static void fetchAddress(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      addrType      : '',
      bindAddrType  : 'Serv',
      isMain        : true,
      operationDate : null,
      beginDate     : null,
      endDate       : null,
      entityPrefix  : '',
      prefix        : '',
      hydra         : null
    ] + input

    def addrType      = params.addrType
    def bindAddrType  = params.bindAddrType
    def isMain        = params.isMain
    def operationDate = params.operationDate
    def beginDate     = params.beginDate
    def endDate       = params.endDate
    def entityPrefix  = "homsOrderData${StringUtil.capitalize(params.entityPrefix)}"
    def _entityPrefix = StringUtil.decapitalize(params.entityPrefix)
    def prefix  = "${entityPrefix}${StringUtil.capitalize(params.prefix)}"
    def _prefix = "${_entityPrefix}${StringUtil.capitalize(params.prefix)}"
    def hydra   = params.hydra ?: new Hydra(execution)

    def orderData     = Order.getData(execution)
    String entityId   = orderData."${_entityPrefix}Id" ?: [is: 'null']
    String entityType = orderData."${_entityPrefix}Type"
    def address = hydra.getEntityAddress(entityType    : entityType,
                                        entityId      : entityId,
                                        operationDate : operationDate,
                                        beginDate     : beginDate,
                                        endDate       : endDate,
                                        addrType      : "ADDR_TYPE_${addrType ?: 'FactPlace'}",
                                        bindAddrType  : "BIND_ADDR_TYPE_${bindAddrType}",
                                        isMain        : isMain)

    if (StringUtil.isEmpty(addrType)) {
      List addressFields = hydra.getAddressItemsNames()

      execution.setVariable("${prefix}${bindAddrType}RegionId", address?.n_region_id)
      (addressFields).each{ name ->
        execution.setVariable("${prefix}${bindAddrType}${StringUtil.capitalize(name)}", address?."vc_${name}" ?: address?."n_${name}_no")
      }
      calcAddress(execution, bindAddrType : params.bindAddrType,
                            entityPrefix : params.entityPrefix,
                            prefix       : params.prefix,
                            hydra        : hydra)
    } else {
      execution.setVariable("${prefix}${addrType}", address?.vc_visual_code)
    }
  }

  static void fetchBaseSubjectAddress(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType  : 'Actual',
      isMain        : true,
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : '',
      hydra         : null
    ] + input

    fetchAddress(execution, addrType     : '',
                            bindAddrType : params.bindAddrType,
                            isMain       : params.isMain,
                            entityPrefix : "${params.subjectPrefix}BaseSubject${params.subjectSuffix}",
                            prefix       : params.prefix,
                            hydra        : params.hydra)
  }

  static void fetchBaseSubjectPhone(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType  : 'Notice',
      isMain        : true,
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : '',
      hydra         : null
    ] + input

    fetchAddress(execution, addrType     : 'Telephone',
                            bindAddrType : params.bindAddrType,
                            isMain       : params.isMain,
                            entityPrefix : "${params.subjectPrefix}BaseSubject${params.subjectSuffix}",
                            prefix       : params.prefix,
                            hydra        : params.hydra)
  }

  static void fetchBaseSubjectTelephone(LinkedHashMap input, DelegateExecution execution) {
    fetchBaseSubjectPhone(input, execution)
  }

  static void fetchBaseSubjectEmail(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType  : 'Notice',
      isMain        : true,
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : '',
      hydra         : null
    ] + input

    fetchAddress(execution, addrType     : 'EMail',
                            bindAddrType : params.bindAddrType,
                            isMain       : params.isMain,
                            entityPrefix : "${params.subjectPrefix}BaseSubject${params.subjectSuffix}",
                            prefix       : params.prefix,
                            hydra        : params.hydra)
  }

  static void fetchEquipmentAddress(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      addrType        : '',
      bindAddrType    : 'Serv',
      isMain          : true,
      operationDate   : null,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    fetchAddress(execution, addrType      : params.addrType,
                            bindAddrType  : params.bindAddrType,
                            isMain        : params.isMain,
                            operationDate : params.operationDate,
                            beginDate     : params.beginDate,
                            endDate       : params.endDate,
                            entityPrefix  : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
                            prefix        : params.prefix,
                            hydra         : params.hydra)
  }

  static void fetchEquipmentMAC(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      operationDate   : null,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    fetchAddress(execution, addrType      : 'MAC',
                            bindAddrType  : params.bindAddrType,
                            isMain        : params.isMain,
                            operationDate : params.operationDate,
                            beginDate     : params.beginDate,
                            endDate       : params.endDate,
                            entityPrefix  : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
                            prefix        : params.prefix,
                            hydra         : params.hydra)
  }

  static void fetchEquipmentIP(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      operationDate   : null,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    fetchAddress(execution, addrType      : 'IP',
                            bindAddrType  : params.bindAddrType,
                            isMain        : params.isMain,
                            operationDate : params.operationDate,
                            beginDate     : params.beginDate,
                            endDate       : params.endDate,
                            entityPrefix  : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
                            prefix        : params.prefix,
                            hydra         : params.hydra)
  }

  static void fetchEquipmentVLAN(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      operationDate   : null,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    fetchAddress(execution, addrType      : 'VLAN',
                            bindAddrType  : params.bindAddrType,
                            isMain        : params.isMain,
                            operationDate : params.operationDate,
                            beginDate     : params.beginDate,
                            endDate       : params.endDate,
                            entityPrefix  : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
                            prefix        : params.prefix,
                            hydra         : params.hydra)
  }

  static void fetchEquipmentSubnet(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      operationDate   : null,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    fetchAddress(execution, addrType      : 'Subnet',
                            bindAddrType  : params.bindAddrType,
                            isMain        : params.isMain,
                            operationDate : params.operationDate,
                            beginDate     : params.beginDate,
                            endDate       : params.endDate,
                            entityPrefix  : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
                            prefix        : params.prefix,
                            hydra         : params.hydra)
  }

  static void fetchEquipmentTelephone(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      operationDate   : null,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    fetchAddress(execution, addrType      : 'Telephone',
                            bindAddrType  : params.bindAddrType,
                            isMain        : params.isMain,
                            operationDate : params.operationDate,
                            beginDate     : params.beginDate,
                            endDate       : params.endDate,
                            entityPrefix  : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
                            prefix        : params.prefix,
                            hydra         : params.hydra)
  }

  static void fetchEquipmentFreeIP(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      groupId         : null,
      subnetAddressId : null,
      operationDate   : DateTimeUtil.now(),
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    def addrType = 'IP'
    def prefix   = "homsOrderData${StringUtil.capitalize(params.equipmentPrefix)}Equipment${StringUtil.capitalize(params.equipmentSuffix)}${StringUtil.capitalize(params.prefix)}"
    def hydra    = params.hydra ?: new Hydra(execution)

    def address = hydra.getFreeIP(groupId         : params.groupId,
                                  subnetAddressId : params.subnetAddressId,
                                  operationDate   : params.operationDate)
    if (address) {
      execution.setVariable("${prefix}${addrType}", address)
    }
  }

  static void fetchEquipmentFreeTelephone(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      groupId         : null,
      telCodeId       : null,
      operationDate   : DateTimeUtil.now(),
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    def addrType = 'Telephone'
    def prefix   = "homsOrderData${StringUtil.capitalize(params.equipmentPrefix)}Equipment${StringUtil.capitalize(params.equipmentSuffix)}${StringUtil.capitalize(params.prefix)}"
    def hydra    = params.hydra ?: new Hydra(execution)

    def address = hydra.getFreePhoneNumber(groupId       : params.groupId,
                                          telCodeId     : params.telCodeId,
                                          operationDate : params.operationDate)
    if (address) {
      execution.setVariable("${prefix}${addrType}", address)
    }
  }

  static void fetchEquipmentFreeSubnet(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      groupId         : null,
      rootId          : null,
      mask            : null,
      operationDate   : DateTimeUtil.now(),
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    def addrType = 'Subnet'
    def prefix   = "homsOrderData${StringUtil.capitalize(params.equipmentPrefix)}Equipment${StringUtil.capitalize(params.equipmentSuffix)}${StringUtil.capitalize(params.prefix)}"
    def hydra    = params.hydra ?: new Hydra(execution)

    def address  = hydra.getFreeSubnet(groupId       : params.groupId,
                                      rootId        : params.rootId,
                                      mask          : params.mask,
                                      operationDate : params.operationDate)
    if (address) {
      execution.setVariable("${prefix}${addrType}", address)
    }
  }

  static void calcAddress(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType : 'Actual',
      entityPrefix : '',
      prefix       : '',
      hydra        : null
    ] + input

    def bindAddrType  = params.bindAddrType
    def entityPrefix  = "homsOrderData${StringUtil.capitalize(params.entityPrefix)}"
    def _entityPrefix = StringUtil.decapitalize(params.entityPrefix)
    def prefix  = "${entityPrefix}${StringUtil.capitalize(params.prefix)}${bindAddrType}"
    def _prefix = "${_entityPrefix}${StringUtil.capitalize(params.prefix)}${bindAddrType}"
    def hydra   = params.hydra ?: new Hydra(execution)
    def regexp  = Pattern.compile("^${_prefix}(.+)\$")
    def new_address      = [:]
    def current_address  = [:]
    def previous_address = [:]

    def orderData = Order.getData(execution)
    orderData.each { name, value ->
      def group = (name =~ regexp)
      if (group.size() > 0) {
        String field = group[0][1]
        String key  = StringUtil.decapitalize(field)
        current_address[key] = value
      }
    }

    // Some magic to solve CONSULT-2340
    fetchRegion(execution, bindAddrType: params.bindAddrType, entityPrefix: params.entityPrefix, prefix: params.prefix, hydra: hydra)
    orderData = Order.getData(execution)
    orderData.each { name, value ->
      def group = (name =~ regexp)
      if (group.size() > 0) {
        String key  = StringUtil.decapitalize(group[0][1])
        previous_address[key] = value
      }
    }
    previous_address.each { k,v ->
      if (v == null && current_address[k] != null) {
        new_address[k] = current_address[k]
      } else {
        new_address[k] = v
      }
    }

    new_address.each { k,v ->
      execution.setVariable("${prefix}${StringUtil.capitalize(k)}", v)
    }
    // End of magic

    execution.setVariable("${prefix}Address", hydra.calcAddress(new_address))
  }

  static void calcBaseSubjectAddress(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType  : 'Actual',
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : '',
      hydra         : null
    ] + input

    calcAddress(execution, bindAddrType : params.bindAddrType,
                          entityPrefix : "${params.subjectPrefix}BaseSubject${params.subjectSuffix}",
                          prefix       : params.prefix,
                          hydra        : params.hydra)
  }

  static void calcEquipmentAddress(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Serv',
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    calcAddress(execution, bindAddrType : params.bindAddrType,
                          entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
                          prefix       : params.prefix,
                          hydra        : params.hydra)
  }

  static void createAddress(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      addrType      : '',
      bindAddrType  : 'Actual',
      isMain        : true,
      beginDate     : null,
      endDate       : null,
      entityPrefix  : '',
      prefix        : '',
      hydra         : null
    ] + input

    def addrType      = params.addrType
    def bindAddrType  = params.bindAddrType
    def isMain        = params.isMain
    def beginDate     = params.beginDate
    def endDate       = params.endDate
    def entityPrefix  = "homsOrderData${StringUtil.capitalize(params.entityPrefix)}"
    def _entityPrefix = StringUtil.decapitalize(params.entityPrefix)
    def prefix  = "${entityPrefix}${StringUtil.capitalize(params.prefix)}"
    def _prefix = "${_entityPrefix}${StringUtil.capitalize(params.prefix)}"
    def hydra   = params.hydra ?: new Hydra(execution)

    def orderData     = Order.getData(execution)
    String entityId   = orderData."${_entityPrefix}Id"
    String entityType = orderData."${_entityPrefix}Type"

    def inp = [
      entityId     : entityId,
      entityType   : entityType,
      bindAddrType : "BIND_ADDR_TYPE_${bindAddrType}",
      addrType     : "ADDR_TYPE_${addrType ?: 'FactPlace'}",
      isMain       : isMain,
      beginDate    : beginDate,
      endDate      : endDate
    ]

    if (StringUtil.isEmpty(addrType)) {
      Pattern pattern = Pattern.compile("^${_prefix}${bindAddrType}(RegionId|Flat|Floor|Entrance)\$")
      orderData.each { key, value ->
        def group = (key =~ pattern)
        if (group.size() > 0) {
          String item = StringUtil.decapitalize(group[0][1])
          inp[item] = value
        }
      }
    } else {
      inp.code = orderData."${_prefix}${addrType}"
    }

    def address = hydra.putEntityAddress(inp)
    if (address) {
      def prefixId = ''
      def prefixCreated = ''
      if (StringUtil.isEmpty(addrType)) {
        prefixId = "${bindAddrType}AddressId"
        prefixCreated = "${bindAddrType}AddressCreated"
      } else {
        prefixId = "${addrType}Id"
        prefixCreated = "${addrType}Created"
      }
      execution.setVariable("${prefix}${prefixId}", address.num_N_ENTITY_ADDRESS_ID)
      execution.setVariable("${prefix}${prefixCreated}", true)
    }
  }

  static void createBaseSubjectAddress(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType  : 'Actual',
      isMain        : true,
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : '',
      hydra         : null
    ] + input

    createAddress(execution, addrType     : '',
                            bindAddrType : params.bindAddrType,
                            isMain       : params.isMain,
                            entityPrefix : "${params.subjectPrefix}BaseSubject${params.subjectSuffix}",
                            prefix       : params.prefix,
                            hydra        : params.hydra)
  }

  static void createBaseSubjectPhone(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType  : 'Notice',
      isMain        : true,
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : '',
      hydra         : null
    ] + input

    createAddress(execution, addrType     : 'Telephone',
                            bindAddrType : params.bindAddrType,
                            isMain       : params.isMain,
                            entityPrefix : "${params.subjectPrefix}BaseSubject${params.subjectSuffix}",
                            prefix       : params.prefix,
                            hydra        : params.hydra)
  }

  static void createBaseSubjectTelephone(LinkedHashMap input, DelegateExecution execution) {
    createBaseSubjectPhone(input, execution)
  }

  static void createBaseSubjectEmail(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType  : 'Notice',
      isMain        : true,
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : '',
      hydra         : null
    ] + input

    createAddress(execution, addrType     : 'EMail',
                            bindAddrType : params.bindAddrType,
                            isMain       : params.isMain,
                            entityPrefix : "${params.subjectPrefix}BaseSubject${params.subjectSuffix}",
                            prefix       : params.prefix,
                            hydra        : params.hydra)
  }

  static void createEquipmentAddress(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Serv',
      isMain          : true,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    createAddress(execution, addrType     : '',
                            bindAddrType : params.bindAddrType,
                            isMain       : params.isMain,
                            beginDate    : params.beginDate,
                            endDate      : params.endDate,
                            entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
                            prefix       : params.prefix,
                            hydra        : params.hydra)
  }

  static void createEquipmentMAC(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    createAddress(execution, addrType     : 'MAC',
                            bindAddrType : params.bindAddrType,
                            isMain       : params.isMain,
                            beginDate    : params.beginDate,
                            endDate      : params.endDate,
                            entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
                            prefix       : params.prefix,
                            hydra        : params.hydra)
  }

  static void createEquipmentIP(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    createAddress(execution, addrType     : 'IP',
                            bindAddrType : params.bindAddrType,
                            isMain       : params.isMain,
                            beginDate    : params.beginDate,
                            endDate      : params.endDate,
                            entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
                            prefix       : params.prefix,
                            hydra        : params.hydra)
  }

  static void createEquipmentVLAN(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    createAddress(execution, addrType     : 'VLAN',
                            bindAddrType : params.bindAddrType,
                            isMain       : params.isMain,
                            beginDate    : params.beginDate,
                            endDate      : params.endDate,
                            entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
                            prefix       : params.prefix,
                            hydra        : params.hydra)
  }

  static void createEquipmentSubnet(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    createAddress(execution, addrType     : 'Subnet',
                            bindAddrType : params.bindAddrType,
                            isMain       : params.isMain,
                            beginDate    : params.beginDate,
                            endDate      : params.endDate,
                            entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
                            prefix       : params.prefix,
                            hydra        : params.hydra)
  }

  static void createEquipmentTelephone(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    createAddress(execution, addrType     : 'Telephone',
                            bindAddrType : params.bindAddrType,
                            isMain       : params.isMain,
                            beginDate    : params.beginDate,
                            endDate      : params.endDate,
                            entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
                            prefix       : params.prefix,
                            hydra        : params.hydra)
  }

  static void assignEquipmentFreeIP(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      groupId         : null,
      subnetAddressId : null,
      operationDate   : DateTimeUtil.now(),
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    fetchEquipmentFreeIP(input, execution)
    createEquipmentIP(input, execution)
  }

  static void assignEquipmentFreeTelephone(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      groupId         : null,
      telCodeId       : null,
      operationDate   : DateTimeUtil.now(),
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    fetchEquipmentFreeTelephone(input, execution)
    createEquipmentTelephone(input, execution)
  }

  static void assignEquipmentFreeSubnet(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      groupId         : null,
      rootId          : null,
      mask            : null,
      operationDate   : DateTimeUtil.now(),
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    fetchEquipmentFreeSubnet(input, execution)
    createEquipmentSubnet(input, execution)
  }

  static void closeAddress(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      addrType      : '',
      bindAddrType  : 'Actual',
      isMain        : true,
      endDate       : null,
      entityPrefix  : '',
      prefix        : '',
      hydra         : null
    ] + input

    def addrType      = params.addrType
    def bindAddrType  = params.bindAddrType
    def isMain        = params.isMain
    def endDate       = params.endDate
    def entityPrefix  = "homsOrderData${StringUtil.capitalize(params.entityPrefix)}"
    def _entityPrefix = StringUtil.decapitalize(params.entityPrefix)
    def prefix  = "${entityPrefix}${StringUtil.capitalize(params.prefix)}"
    def _prefix = "${_entityPrefix}${StringUtil.capitalize(params.prefix)}"
    def hydra   = params.hydra ?: new Hydra(execution)

    def orderData = Order.getData(execution)
    String entityId   = orderData."${_entityPrefix}Id"
    String entityType = orderData."${_entityPrefix}Type"

    def inp = [:]
    def prefixId = ''
    def prefixCode = ''
    def prefixClosed = ''
    if (StringUtil.isEmpty(addrType)) {
      prefixId = "${bindAddrType}AddressId"
      prefixClosed = "${bindAddrType}AddressClosed"
    } else {
      prefixId = "${addrType}Id"
      prefixCode = "${addrType}"
      prefixClosed = "${addrType}Closed"
    }

    def addressId = execution.getVariable("${prefix}${prefixId}")
    def result = hydra.closeEntityAddress(
      entityId     : entityId,
      entityType   : entityType,
      addressId    : addressId,
      bindAddrType : "BIND_ADDR_TYPE_${bindAddrType}",
      addrType     : "ADDR_TYPE_${addrType ?: 'FactPlace'}",
      isMain       : isMain,
      code         : prefixCode,
      endDate      : endDate
    )

    execution.setVariable("${prefix}${prefixClosed}", result)
  }

  static void closeEquipmentAddress(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Serv',
      isMain          : true,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    closeAddress(execution,  addrType     : '',
                            bindAddrType : params.bindAddrType,
                            isMain       : params.isMain,
                            endDate      : params.endDate,
                            entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
                            prefix       : params.prefix,
                            hydra        : params.hydra)
  }

  static void closeEquipmentMAC(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    closeAddress(execution,  addrType     : 'MAC',
                            bindAddrType : params.bindAddrType,
                            isMain       : params.isMain,
                            endDate      : params.endDate,
                            entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
                            prefix       : params.prefix,
                            hydra        : params.hydra)
  }

  static void closeEquipmentIP(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    closeAddress(execution,  addrType     : 'IP',
                            bindAddrType : params.bindAddrType,
                            isMain       : params.isMain,
                            endDate      : params.endDate,
                            entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
                            prefix       : params.prefix,
                            hydra        : params.hydra)
  }

  static void closeEquipmentVLAN(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    closeAddress(execution,  addrType     : 'VLAN',
                            bindAddrType : params.bindAddrType,
                            isMain       : params.isMain,
                            endDate      : params.endDate,
                            entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
                            prefix       : params.prefix,
                            hydra        : params.hydra)
  }

  static void closeEquipmentSubnet(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    closeAddress(execution,  addrType     : 'Subnet',
                            bindAddrType : params.bindAddrType,
                            isMain       : params.isMain,
                            endDate      : params.endDate,
                            entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
                            prefix       : params.prefix,
                            hydra        : params.hydra)
  }

  static void closeEquipmentTelephone(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    closeAddress(execution,  addrType     : 'Telephone',
                            bindAddrType : params.bindAddrType,
                            isMain       : params.isMain,
                            endDate      : params.endDate,
                            entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
                            prefix       : params.prefix,
                            hydra        : params.hydra)
  }

  static void deleteAddress(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      addrType      : '',
      bindAddrType  : 'Actual',
      isMain        : true,
      entityPrefix  : '',
      prefix        : '',
      hydra         : null
    ] + input

    def addrType      = params.addrType
    def bindAddrType  = params.bindAddrType
    def isMain        = params.isMain
    def entityPrefix  = "homsOrderData${StringUtil.capitalize(params.entityPrefix)}"
    def _entityPrefix = StringUtil.capitalize(params._entityPrefix)
    def prefix  = "${entityPrefix}${StringUtil.capitalize(params.prefix)}"
    def _prefix = "${_entityPrefix}${StringUtil.capitalize(params.prefix)}"
    def hydra   = params.hydra ?: new Hydra(execution)

    def orderData     = Order.getData(execution)
    String entityId   = orderData."${_entityPrefix}Id"
    String entityType = orderData."${_entityPrefix}Type"

    def inp           = [:]
    def prefixId      = ''
    def prefixDeleted = ''
    if (StringUtil.isEmpty(addrType)) {
      prefixId = "${bindAddrType}AddressId"
      prefixDeleted = "${bindAddrType}AddressDeleted"
    } else {
      prefixId = "${addrType}Id"
      prefixDeleted = "${addrType}Deleted"
    }

    def addressId = execution.getVariable("${prefix}${prefixId}")
    def result = hydra.deleteEntityAddress(
      entityId     : entityId,
      entityType   : entityType,
      addressId    : addressId,
      bindAddrType : "BIND_ADDR_TYPE_${bindAddrType}",
      addrType     : "ADDR_TYPE_${addrType ?: 'FactPlace'}",
      isMain       : isMain
    )

    execution.setVariable("${prefix}${prefixDeleted}", result)
  }

  static void deleteBaseSubjectAddress(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType  : 'Actual',
      isMain        : true,
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : '',
      hydra         : null
    ] + input

    deleteAddress(execution, addrType     : '',
                            bindAddrType : params.bindAddrType,
                            isMain       : params.isMain,
                            entityPrefix : "${params.subjectPrefix}BaseSubject${params.subjectSuffix}",
                            prefix       : params.prefix,
                            hydra        : params.hydra)
  }

  static void deleteBaseSubjectPhone(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType  : 'Notice',
      isMain        : true,
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : '',
      hydra         : null
    ] + input

    deleteAddress(execution, addrType     : 'Telephone',
                            bindAddrType : params.bindAddrType,
                            isMain       : params.isMain,
                            entityPrefix : "${params.subjectPrefix}BaseSubject${params.subjectSuffix}",
                            prefix       : params.prefix,
                            hydra        : params.hydra)
  }

  static void deleteBaseSubjectTelephone(LinkedHashMap input, DelegateExecution execution) {
    deleteBaseSubjectPhone(input, execution)
  }

  static void deleteBaseSubjectEmail(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType  : 'Notice',
      isMain        : true,
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : '',
      hydra         : null
    ] + input

    deleteAddress(execution, addrType: 'EMail',
                            bindAddrType: params.bindAddrType,
                            isMain: params.isMain,
                            entityPrefix: "${params.subjectPrefix}BaseSubject${params.subjectSuffix}",
                            prefix: params.prefix,
                            hydra: params.hydra)
  }

  static void deleteEquipmentAddress(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Serv',
      isMain          : true,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    deleteAddress(execution, addrType     : '',
                            bindAddrType : params.bindAddrType,
                            isMain       : params.isMain,
                            entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
                            prefix       : params.prefix,
                            hydra        : params.hydra)
  }

  static void deleteEquipmentMAC(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    deleteAddress(execution, addrType     : 'MAC',
                            bindAddrType : params.bindAddrType,
                            isMain       : params.isMain,
                            entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
                            prefix       : params.prefix,
                            hydra        : params.hydra)
  }

  static void deleteEquipmentIP(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    deleteAddress(execution, addrType     : 'IP',
                            bindAddrType : params.bindAddrType,
                            isMain       : params.isMain,
                            entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
                            prefix       : params.prefix,
                            hydra        : params.hydra)
  }

  static void deleteEquipmentVLAN(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    deleteAddress(execution, addrType     : 'VLAN',
                            bindAddrType : params.bindAddrType,
                            isMain       : params.isMain,
                            entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
                            prefix       : params.prefix,
                            hydra        : params.hydra)
  }

  static void deleteEquipmentSubnet(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    deleteAddress(execution, addrType     : 'Subnet',
                            bindAddrType : params.bindAddrType,
                            isMain       : params.isMain,
                            entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
                            prefix       : params.prefix,
                            hydra        : params.hydra)
  }

  static void deleteEquipmentTelephone(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    deleteAddress(execution, addrType     : 'Telephone',
                            bindAddrType : params.bindAddrType,
                            isMain       : params.isMain,
                            entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
                            prefix       : params.prefix,
                            hydra        : params.hydra)
  }

  // TODO: Move to BaseSubject trait
  static void fetchBaseSubject(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      hydra : null
    ] + input

    def logger = new SimpleLogger(execution)
    def hydra = params.hydra ?: new Hydra(execution)
    def orderData = Order.getData(execution)
    def subject = hydra.getSubject(orderData.baseSubjectId)

    def subjType = hydra.getRefCodeById(subject?.n_subj_type_id)
    execution.setVariable("homsOrderDataBaseSubjectType",      subjType)
    execution.setVariable("homsOrderDataBaseSubjectIsCompany", subjType == 'SUBJ_TYPE_Company')
  }

  // TODO: Move to Company trait
  static void fetchCompany(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      subjectPrefix : '',
      prefix        : '',
      hydra         : null
    ] + input

    def subjectPrefix = StringUtil.capitalize(params.subjectPrefix)
    def prefix = StringUtil.capitalize(params.prefix)
    def hydra = params.hydra ?: new Hydra(execution)
    def baseSubjectId = execution.getVariable("homsOrderData${subjectPrefix}BaseSubjectId")
    def company = hydra.getCompany(baseSubjectId)
    def opfCode = hydra.getRefCodeById(company?.n_opf_id)

    def code = "\"${company?.vc_code}\"".replace('""', '"').replace('--', '').trim()
    def name = code ? [company?.n_opf_id ? opfCode : '', code].join(' ').trim() : ''
    execution.setVariable("homsOrderData${prefix}CompanyName",             code)
    execution.setVariable("homsOrderData${subjectPrefix}BaseSubjectCode",  code)
    execution.setVariable("homsOrderData${subjectPrefix}BaseSubjectName",  name)
    execution.setVariable("homsOrderData${subjectPrefix}BaseSubjectINN",   company?.vc_inn)
    execution.setVariable("homsOrderData${subjectPrefix}BaseSubjectKPP",   company?.vc_kpp)
    execution.setVariable("homsOrderData${subjectPrefix}BaseSubjectOPFId", company?.n_opf_id)
    execution.setVariable("homsOrderData${subjectPrefix}CompanyOGRN",      company?.vc_ogrn)
  }

  static void createCompany(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      subjectPrefix : '',
      prefix        : '',
      hydra         : null
    ] + input

    def subjectPrefix = StringUtil.capitalize(params.subjectPrefix)
    def prefix = StringUtil.capitalize(params.prefix)
    def hydra = params.hydra ?: new Hydra(execution)

    def opfId = execution.getVariable("homsOrderData${subjectPrefix}BaseSubjectOPFId")
    def opfCode = hydra.getRefCodeById(opfId)
    def name = [opfId ? opfCode : '', "\"${execution.getVariable("homsOrderData${prefix}CompanyName")}\""].join(' ').trim()

    def company = hydra.putCompany(
      name  : name.toString(),
      code  : execution.getVariable("homsOrderData${prefix}CompanyName"),
      opfId : execution.getVariable("homsOrderData${subjectPrefix}BaseSubjectOPFId"),
      inn   : execution.getVariable("homsOrderData${subjectPrefix}BaseSubjectINN"),
      kpp   : execution.getVariable("homsOrderData${subjectPrefix}BaseSubjectKPP"),
      ogrn  : execution.getVariable("homsOrderData${subjectPrefix}CompanyOGRN")
    )
    if (company) {
      execution.setVariable("homsOrderData${subjectPrefix}BaseSubjectId", company.num_N_SUBJECT_ID)
      execution.setVariable("homsOrderData${subjectPrefix}BaseSubjectCreated", true)
    }
  }

  // TODO: Move to Contract trait
  static void fetchContract(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      baseContractPrefix : '',
      prefix             : '',
      hydra              : null
    ] + input

    def prefix = StringUtil.capitalize(params.prefix)
    def baseContractPrefix = StringUtil.capitalize(params.baseContractPrefix)
    def hydra = params.hydra ?: new Hydra(execution)
    def contract = hydra.getContract(execution.getVariable("homsOrderData${prefix}ContractId"))

    execution.setVariable("homsOrderData${prefix}ContractNumber", contract?.vc_doc_no)
    execution.setVariable("homsOrderData${prefix}ContractName",   contract?.vc_name)
    if (StringUtil.isEmpty(execution.getVariable("homsOrderData${baseContractPrefix}BaseContractId"))) {
      execution.setVariable("homsOrderData${baseContractPrefix}BaseContractId", contract?.n_parent_doc_id)
    }
  }

  static void fetchCustomerFirstContract(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      baseContractPrefix : '',
      customerPrefix     : '',
      prefix             : '',
      hydra              : null
    ] + input

    def baseContractPrefix = StringUtil.capitalize(params.baseContractPrefix)
    def customerPrefix = StringUtil.capitalize(params.customerPrefix)
    def prefix = StringUtil.capitalize(params.prefix)
    def hydra = params.hydra ?: new Hydra(execution)
    def contract = hydra.getContractBy(receiverId: execution.getVariable("homsOrderData${customerPrefix}CustomerId"), operationDate: DateTimeUtil.now())

    execution.setVariable("homsOrderData${prefix}ContractId", contract?.n_doc_id)
    fetchContract(execution, prefix: params.prefix, hydra: hydra)
  }

  static void createContract(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      baseContractPrefix : '',
      customerPrefix     : '',
      prefix             : '',
      hydra              : null
    ] + input

    def baseContractPrefix = StringUtil.capitalize(params.baseContractPrefix)
    def customerPrefix = StringUtil.capitalize(params.customerPrefix)
    def prefix = StringUtil.capitalize(params.prefix)
    def hydra = params.hydra ?: new Hydra(execution)

    def contract = hydra.putContract(
      parentDocId : execution.getVariable("homsOrderData${baseContractPrefix}BaseContractId"),
      receiverId  : execution.getVariable("homsOrderData${customerPrefix}CustomerId"),
      number      : execution.getVariable("homsOrderData${prefix}ContractNumber")
    )
    if (contract) {
      execution.setVariable("homsOrderData${prefix}ContractId", contract.num_N_DOC_ID)
      execution.setVariable("homsOrderData${prefix}ContractCreated", true)
    }
  }

  static void dissolveContract(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      prefix  : '',
      endDate : DateTimeUtil.local(),
      hydra   : null
    ] + input

    def prefix = StringUtil.capitalize(params.prefix)
    def hydra = params.hydra ?: new Hydra(execution)

    def result = hydra.dissolveContract(
      docId   : execution.getVariable("homsOrderData${prefix}ContractId"),
      endDate : params.endDate
    )
    if (result) {
      execution.setVariable("homsOrderData${prefix}ContractDissolveDate", params.endDate.format(DateTimeUtil.ISO_FORMAT))
      execution.setVariable("homsOrderData${prefix}ContractDissolved", true)
    }
  }

  // TODO: Move to Customer trait
  static void fetchCustomer(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      subjectPrefix : '',
      prefix        : '',
      hydra         : null
    ] + input

    def subjectPrefix = StringUtil.capitalize(params.subjectPrefix)
    def prefix = StringUtil.capitalize(params.prefix)
    def hydra = params.hydra ?: new Hydra(execution)
    def customer = hydra.getCustomer(execution.getVariable("homsOrderData${prefix}CustomerId"))

    if (StringUtil.isEmpty(execution.getVariable("homsOrderData${subjectPrefix}BaseSubjectId"))) {
      execution.setVariable("homsOrderData${subjectPrefix}BaseSubjectId", customer?.n_base_subject_id)
    }
    if (StringUtil.isEmpty(execution.getVariable("homsOrderData${prefix}CustomerCode"))) {
      execution.setVariable("homsOrderData${prefix}CustomerCode",         customer?.vc_code)
    }
    if (StringUtil.isEmpty(execution.getVariable("homsOrderData${prefix}CustomerGroupId"))) {
      execution.setVariable("homsOrderData${prefix}CustomerGroupId",      customer?.n_subj_group_id)
    }
    if (StringUtil.isEmpty(execution.getVariable("homsOrderData${prefix}CustomerFirmId"))) {
      execution.setVariable("homsOrderData${prefix}CustomerFirmId",       customer?.n_firm_id)
    }
    if (StringUtil.isEmpty(execution.getVariable("homsOrderData${prefix}CustomerStateId"))) {
      execution.setVariable("homsOrderData${prefix}CustomerStateId",      customer?.n_subj_state_id)
    }
  }

  static void fetchCustomerByAccount(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      subjectPrefix : '',
      accountPrefix : '',
      prefix        : '',
      hydra         : null
    ] + input

    def accountPrefix = StringUtil.capitalize(params.accountPrefix)
    def prefix = StringUtil.capitalize(params.prefix)
    def hydra = params.hydra ?: new Hydra(execution)

    def account = hydra.getAccount(execution.getVariable("homsOrderData${accountPrefix}AccountId"))
    execution.setVariable("homsOrderData${prefix}CustomerId", account?.n_subject_id)
    fetchCustomer(input + [hydra: hydra], execution)
  }

  static void createCustomer(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      subjectPrefix : '',
      prefix        : '',
      hydra         : null
    ] + input

    def subjectPrefix = StringUtil.capitalize(params.subjectPrefix)
    def prefix = StringUtil.capitalize(params.prefix)
    def hydra  = params.hydra ?: new Hydra(execution)

    def customer = hydra.putCustomer(
      baseSubjectId : execution.getVariable("homsOrderData${subjectPrefix}BaseSubjectId"),
      code          : execution.getVariable("homsOrderData${prefix}CustomerCode"),
      groupId       : execution.getVariable("homsOrderData${prefix}CustomerGroupId")
    )
    if (customer) {
      execution.setVariable("homsOrderData${prefix}CustomerId", customer.num_N_SUBJECT_ID)
      execution.setVariable("homsOrderData${prefix}CustomerCreated", true)
      fetchCustomer(execution, subjectPrefix: subjectPrefix, hydra: hydra)
    }
  }

  static void addCustomerGroupBind(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      customerPrefix : '',
      groupPrefix    : '',
      prefix         : '',
      isMain         : false,
      hydra          : null
    ] + input

    def customerPrefix = StringUtil.capitalize(params.customerPrefix)
    def groupPrefix    = StringUtil.capitalize(params.groupPrefix)
    def prefix = StringUtil.capitalize(params.prefix)
    def hydra  = params.hydra ?: new Hydra(execution)


    def existingGroup = hydra.getCustomerGroupBy(
      customerId : execution.getVariable("homsOrderData${customerPrefix}CustomerId"),
      groupId    : execution.getVariable("homsOrderData${customerPrefix}Customer${groupPrefix}GroupId")
    )

    if (existingGroup) {
      execution.setVariable("homsOrderData${customerPrefix}Customer${groupPrefix}Group${prefix}BindId", existingGroup.num_N_SUBJ_SUBJECT_ID)
      execution.setVariable("homsOrderData${customerPrefix}Customer${groupPrefix}Group${prefix}BindAdded", true)
    } else {

      def group = hydra.putCustomerGroup(
        customerId : execution.getVariable("homsOrderData${customerPrefix}CustomerId"),
        groupId    : execution.getVariable("homsOrderData${customerPrefix}Customer${groupPrefix}GroupId"),
        isMain     : params.isMain
      )
      if (group) {
        execution.setVariable("homsOrderData${customerPrefix}Customer${groupPrefix}Group${prefix}BindId", group.num_N_SUBJ_SUBJECT_ID)
        execution.setVariable("homsOrderData${customerPrefix}Customer${groupPrefix}Group${prefix}BindAdded", true)
      }
    }
  }

  static void deleteCustomerGroupBind(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      customerPrefix : '',
      groupPrefix    : '',
      prefix         : '',
      isMain         : false,
      hydra          : null
    ] + input

    def customerPrefix = StringUtil.capitalize(params.customerPrefix)
    def groupPrefix    = StringUtil.capitalize(params.groupPrefix)
    def prefix = StringUtil.capitalize(params.prefix)
    def hydra  = params.hydra ?: new Hydra(execution)

    def group = hydra.deleteCustomerGroup(
      subjSubjectId : execution.getVariable("homsOrderData${customerPrefix}Customer${groupPrefix}Group${prefix}BindId"),
      customerId    : execution.getVariable("homsOrderData${customerPrefix}CustomerId"),
      groupId       : execution.getVariable("homsOrderData${customerPrefix}Customer${groupPrefix}GroupId"),
      isMain        : params.isMain
    )
    execution.setVariable("homsOrderData${customerPrefix}Customer${groupPrefix}Group${prefix}BindDeleted", group)
  }

  static void fetchNetServiceAccess(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      netServicePrefix : '',
      prefix           : '',
      hydra            : null
    ] + input

    def netServicePrefix = StringUtil.capitalize(params.netServicePrefix) ?: 'NetService'
    def prefix = StringUtil.capitalize(params.prefix)
    def hydra = params.hydra ?: new Hydra(execution)

    def access = hydra.getCustomerNetServiceAccessBy(
      customerId   : execution.getVariable("homsOrderData${prefix}CustomerId"),
      netServiceId : execution.getVariable("homsOrderData${netServicePrefix}Id")
    )
    if (StringUtil.isEmpty(execution.getVariable("homsOrderData${prefix}Customer${netServicePrefix}Id"))) {
      execution.setVariable("homsOrderData${prefix}Customer${netServicePrefix}Id",       access?.n_subj_service_id)
    }
    if (StringUtil.isEmpty(execution.getVariable("homsOrderData${prefix}Customer${netServicePrefix}Login"))) {
      execution.setVariable("homsOrderData${prefix}Customer${netServicePrefix}Login",    access?.vc_login_real)
    }
    if (StringUtil.isEmpty(execution.getVariable("homsOrderData${prefix}Customer${netServicePrefix}Password"))) {
      execution.setVariable("homsOrderData${prefix}Customer${netServicePrefix}Password", access?.vc_pass)
    }
  }

  static void fetchAppAccess(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      application   : '',
      appPrefix     : '',
      prefix        : '',
      hydra         : null
    ] + input

    def appPrefix = StringUtil.capitalize(params.appPrefix) ?: 'Application'
    def prefix    = StringUtil.capitalize(params.prefix)
    def hydra     = params.hydra ?: new Hydra(execution)

    def access = hydra.getCustomerAppAccessBy(
      customerId  : execution.getVariable("homsOrderData${prefix}CustomerId"),
      application : params.application
    )
    if (StringUtil.isEmpty(execution.getVariable("homsOrderData${prefix}Customer${appPrefix}Id"))) {
      execution.setVariable("homsOrderData${prefix}Customer${appPrefix}Id",    access?.n_subj_service_id)
    }
    if (StringUtil.isEmpty(execution.getVariable("homsOrderData${prefix}Customer${appPrefix}Login"))) {
      execution.setVariable("homsOrderData${prefix}Customer${appPrefix}Login",    access?.vc_login_real)
    }
    if (StringUtil.isEmpty(execution.getVariable("homsOrderData${prefix}Customer${appPrefix}Password"))) {
      execution.setVariable("homsOrderData${prefix}Customer${appPrefix}Password", access?.vc_pass)
    }
  }

  static void fetchSelfCareAccess(LinkedHashMap input, DelegateExecution execution) {
    fetchAppAccess(input + [application: 'NETSERV_ARM_Private_Office', appPrefix: "${input.appPrefix ?: ''}SelfCare"], execution)
  }

  static void addNetServiceAccess(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      netServicePrefix : '',
      prefix           : '',
      hydra            : null
    ] + input

    def netServicePrefix = StringUtil.capitalize(params.netServicePrefix) ?: 'NetService'
    def prefix = StringUtil.capitalize(params.prefix)
    def hydra = params.hydra ?: new Hydra(execution)

    def access = hydra.putCustomerNetServiceAccess(
      customerId   : execution.getVariable("homsOrderData${prefix}CustomerId"),
      netServiceId : execution.getVariable("homsOrderData${netServicePrefix}Id"),
      login        : execution.getVariable("homsOrderData${prefix}Customer${netServicePrefix}Login"),
      password     : execution.getVariable("homsOrderData${prefix}Customer${netServicePrefix}Password")
    )
    if (access) {
      execution.setVariable("homsOrderData${prefix}Customer${netServicePrefix}Login",    access.vch_VC_LOGIN)
      execution.setVariable("homsOrderData${prefix}Customer${netServicePrefix}Password", access.vch_VC_PASS)
      execution.setVariable("homsOrderData${prefix}Customer${netServicePrefix}AccessAdded", true)
    }
  }

  static void addAppAccess(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      application : '',
      appPrefix   : '',
      prefix      : '',
      hydra       : null
    ] + input

    def appPrefix = StringUtil.capitalize(params.appPrefix) ?: 'Application'
    def prefix    = StringUtil.capitalize(params.prefix)
    def hydra     = params.hydra ?: new Hydra(execution)

    def access = hydra.putCustomerAppAccess(
      customerId  : execution.getVariable("homsOrderData${prefix}CustomerId"),
      application : params.application,
      login       : execution.getVariable("homsOrderData${prefix}Customer${appPrefix}Login"),
      password    : execution.getVariable("homsOrderData${prefix}Customer${appPrefix}Password")
    )
    if (access) {
      execution.setVariable("homsOrderData${prefix}Customer${appPrefix}Login",    access.vch_VC_LOGIN)
      execution.setVariable("homsOrderData${prefix}Customer${appPrefix}Password", access.vch_VC_PASS)
      execution.setVariable("homsOrderData${prefix}Customer${appPrefix}AccessAdded", true)
    }
  }

  static void addSelfCareAccess(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      appPrefix : '',
      prefix    : '',
      hydra     : null
    ] + input

    def appPrefix = StringUtil.capitalize(params.appPrefix)
    def prefix = StringUtil.capitalize(params.prefix)
    def hydra = params.hydra ?: new Hydra(execution)

    def access = hydra.putCustomerSelfCareAccess(
      customerId : execution.getVariable("homsOrderData${prefix}CustomerId"),
      login      : execution.getVariable("homsOrderData${prefix}Customer${appPrefix}SelfCareLogin"),
      password   : execution.getVariable("homsOrderData${prefix}Customer${appPrefix}SelfCarePassword")
    )
    if (access) {
      execution.setVariable("homsOrderData${prefix}Customer${appPrefix}SelfCareLogin",    access.vch_VC_LOGIN)
      execution.setVariable("homsOrderData${prefix}Customer${appPrefix}SelfCarePassword", access.vch_VC_PASS)
      execution.setVariable("homsOrderData${prefix}Customer${appPrefix}SelfCareAccessAdded", true)
    }
  }

  static void disableCustomer(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      prefix : '',
      hydra  : null
    ] + input

    def prefix = StringUtil.capitalize(params.prefix)
    def hydra = params.hydra ?: new Hydra(execution)

    def result = hydra.disableCustomer(execution.getVariable("homsOrderData${prefix}CustomerId"))
    execution.setVariable("homsOrderData${prefix}CustomerDisabled", result)
  }

  static void saveCustomerAddParam(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      prefix : '',
      param  : '',
      hydra  : null
    ] + input

    def prefix = StringUtil.capitalize(params.prefix)
    def param  = StringUtil.capitalize(params.param)
    def hydra  = params.hydra ?: new Hydra(execution)

    def customerId = execution.getVariable("homsOrderData${prefix}CustomerId")
    def value      = execution.getVariable("homsOrderData${prefix}Customer${param}")
    def result = hydra.putCustomerAddParam(
      customerId : customerId,
      param      : "SUBJ_VAL_${param}",
      value      : value
    )
    execution.setVariable("homsOrderData${prefix}Customer${param}Saved", result)
  }

  // TODO: Move to Equipment trait
  static void fetchEquipment(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      prefix : '',
      suffix : '',
      hydra  : null
    ] + input

    def prefix = "homsOrderData${StringUtil.capitalize(params.prefix)}Equipment${StringUtil.capitalize(params.suffix)}"
    def hydra = params.hydra ?: new Hydra(execution)
    def equipment = hydra.getEquipment(execution.getVariable("${prefix}Id"))
    def good = hydra.getGood(equipment?.n_good_id)

    execution.setVariable("${prefix}Name",   equipment?.vc_name)
    execution.setVariable("${prefix}Code",   equipment?.vc_code)
    execution.setVariable("${prefix}Serial", equipment?.vc_serial)
    if (StringUtil.isEmpty(execution.getVariable("${prefix}GoodName"))) {
      execution.setVariable("${prefix}GoodName", good?.vc_name)
    }
    if (StringUtil.isEmpty(execution.getVariable("${prefix}GoodId"))) {
      execution.setVariable("${prefix}GoodId", equipment?.n_good_id)
    }
  }

  static void fetchCustomerFirstEquipment(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      equipmentPrefix : '',
      equipmentSuffix : '',
      customerPrefix  : '',
      customerSuffix  : '',
      hydra           : null
    ] + input

    def customerPrefix  = "homsOrderData${StringUtil.capitalize(params.customerPrefix)}Customer${StringUtil.capitalize(params.customerSuffix)}"
    def equipmentPrefix = "homsOrderData${StringUtil.capitalize(params.equipmentPrefix)}Equipment${StringUtil.capitalize(params.equipmentSuffix)}"
    def hydra = params.hydra ?: new Hydra(execution)

    def customerId = execution.getVariable("${customerPrefix}Id")
    def goodId     = execution.getVariable("${equipmentPrefix}GoodId")
    def equipment  = hydra.getEquipmentBy(
      ownerId: customerId ?: [is: 'null'],
      typeId: goodId
    )

    execution.setVariable("${equipmentPrefix}Id", equipment?.n_object_id)
    fetchEquipment(execution, hydra: hydra, prefix: params.equipmentPrefix)
  }

  static void fetchEquipmentByComponent(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      equipmentPrefix : '',
      equipmentSuffix : '',
      componentPrefix : '',
      componentSuffix : '',
      hydra           : null
    ] + input

    def equipmentPrefix = "homsOrderData${StringUtil.capitalize(params.equipmentPrefix)}Equipment${StringUtil.capitalize(params.equipmentSuffix)}"
    def componentPrefix  = "${equipmentPrefix}${StringUtil.capitalize(params.componentPrefix)}Component${StringUtil.capitalize(params.componentSuffix)}"
    def hydra = params.hydra ?: new Hydra(execution)

    def componentId = execution.getVariable("${componentPrefix}Id")
    def component = hydra.getEquipmentComponentBy(componentId: componentId ?: [is: 'null'])

    execution.setVariable("${equipmentPrefix}Id", component?.n_main_object_id)
    fetchEquipment(execution, hydra: hydra, prefix: params.equipmentPrefix)
  }

  static void fetchEquipmentFirstComponent(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      equipmentPrefix : '',
      componentPrefix : '',
      hydra           : null
    ] + input

    def equipmentPrefix = StringUtil.capitalize(params.equipmentPrefix)
    def componentPrefix = StringUtil.capitalize(params.componentPrefix)
    def hydra = params.hydra ?: new Hydra(execution)

    def equipmentId = execution.getVariable("homsOrderData${equipmentPrefix}EquipmentId")
    def component = hydra.getEquipmentComponentBy(equipmentId: equipmentId ?: [is: 'null'])

    execution.setVariable("homsOrderData${equipmentPrefix}Equipment${componentPrefix}ComponentId",       component?.n_object_id)
    fetchEquipment(execution, prefix: params.equipmentPrefix, suffix: "${componentPrefix}Component", hydra: hydra)
  }

  static void createEquipment(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      customerPrefix : '',
      customerSuffix : '',
      prefix         : '',
      suffix         : '',
      hydra          : null
    ] + input

    def customerPrefix = StringUtil.capitalize(params.customerPrefix)
    def customerSuffix = StringUtil.capitalize(params.customerSuffix)
    def prefix = StringUtil.capitalize(params.prefix)
    def suffix = StringUtil.capitalize(params.suffix)
    def hydra = params.hydra ?: new Hydra(execution)
    def equipment = hydra.putEquipment(
      typeId  : execution.getVariable("homsOrderData${prefix}Equipment${suffix}GoodId"),
      ownerId : execution.getVariable("homsOrderData${customerPrefix}Customer${customerSuffix}Id")
    )
    if (equipment) {
      execution.setVariable("homsOrderData${prefix}Equipment${suffix}Id", equipment.num_N_OBJECT_ID)
      execution.setVariable("homsOrderData${prefix}Equipment${suffix}Created", true)
    }
  }

  static void createEquipmentComponent(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      suffix          : '',
      hydra           : null
    ] + input

    def equipmentPrefix = "homsOrderData${StringUtil.capitalize(params.equipmentPrefix)}Equipment${StringUtil.capitalize(params.equipmentSuffix)}"
    def prefix = "${equipmentPrefix}${StringUtil.capitalize(params.prefix)}Component${StringUtil.capitalize(params.suffix)}"
    def hydra = params.hydra ?: new Hydra(execution)
    def component = hydra.putEquipmentComponent(
      equipmentId : execution.getVariable("${equipmentPrefix}Id"),
      typeId      : execution.getVariable("${prefix}GoodId")
    )
    if (component) {
      execution.setVariable("${prefix}Id", component.num_N_SPEC_OBJECT_ID)
      execution.setVariable("${prefix}Created", true)
    }
  }

  static void deleteEquipment(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      prefix : '',
      suffix : '',
      hydra  : null
    ] + input

    def prefix = "homsOrderData${StringUtil.capitalize(params.prefix)}Equipment${StringUtil.capitalize(params.suffix)}"
    def hydra = params.hydra ?: new Hydra(execution)
    def result = hydra.deleteEquipment(execution.getVariable("${prefix}Id"))
    execution.setVariable("${prefix}Deleted", result)
  }

  static void deactivateEquipment(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      prefix : '',
      suffix : '',
      hydra  : null
    ] + input

    def prefix = "homsOrderData${StringUtil.capitalize(params.prefix)}Equipment${StringUtil.capitalize(params.suffix)}"
    def hydra = params.hydra ?: new Hydra(execution)
    def result = hydra.deactivateEquipment(execution.getVariable("${prefix}Id"))
    execution.setVariable("${prefix}Deactivated", result)
  }

  static void unregisterEquipment(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      prefix : '',
      suffix : '',
      hydra  : null
    ] + input

    def prefix = "homsOrderData${StringUtil.capitalize(params.prefix)}Equipment${StringUtil.capitalize(params.suffix)}"
    def hydra = params.hydra ?: new Hydra(execution)
    def result = hydra.unregisterEquipment(execution.getVariable("${prefix}Id"))
    execution.setVariable("${prefix}Unregistered", result)
  }

  static void fetchEquipmentBind(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindRole        : 'NetConnection',
      withComponent   : true,
      equipmentPrefix : '',
      equipmentSuffix : '',
      componentPrefix : '',
      componentSuffix : '',
      prefix          : '',
      suffix          : '',
      hydra           : null
    ] + input

    def bindRole = params.bindRole
    def withComponent = params.withComponent
    def equipmentPrefix = StringUtil.capitalize(params.equipmentPrefix)
    def equipmentSuffix = StringUtil.capitalize(params.equipmentSuffix)
    def componentPrefix = StringUtil.capitalize(params.componentPrefix)
    def componentSuffix = StringUtil.capitalize(params.componentSuffix)
    def prefix = StringUtil.capitalize(params.prefix)
    def suffix = StringUtil.capitalize(params.suffix)
    def hydra = params.hydra ?: new Hydra(execution)
    def equipmentId     = execution.getVariable("homsOrderData${equipmentPrefix}Equipment${equipmentSuffix}Id")
    def componentId     = execution.getVariable("homsOrderData${equipmentPrefix}Equipment${equipmentSuffix}${componentPrefix}Component${componentSuffix}Id")
    def inp = [
      mainId   : equipmentId,
      bindRole : "OBJOBJ_BIND_TYPE_${bindRole}"
    ]

    if (withComponent) {
      inp.componentId = componentId
    }

    def equipmentBind = hydra.getEquipmentBindBy(inp)
    if (equipmentBind) {
      if (withComponent) {
        execution.setVariable("homsOrderData${equipmentPrefix}Equipment${equipmentSuffix}${componentPrefix}Component${componentSuffix}${prefix}Bind${suffix}Id", equipmentBind.n_obj_object_id)
        execution.setVariable("homsOrderData${equipmentPrefix}Equipment${equipmentSuffix}${componentPrefix}Component${componentSuffix}${prefix}Bind${suffix}EquipmentId", equipmentBind.n_bind_main_obj_id)
        execution.setVariable("homsOrderData${equipmentPrefix}Equipment${equipmentSuffix}${componentPrefix}Component${componentSuffix}${prefix}Bind${suffix}EquipmentComponentId", equipmentBind.n_bind_object_id)
      } else {
        execution.setVariable("homsOrderData${equipmentPrefix}Equipment${equipmentSuffix}${prefix}Bind${suffix}Id", equipmentBind.n_obj_object_id)
        execution.setVariable("homsOrderData${equipmentPrefix}Equipment${equipmentSuffix}${prefix}Bind${suffix}EquipmentId", equipmentBind.n_bind_main_obj_id)
        execution.setVariable("homsOrderData${equipmentPrefix}Equipment${equipmentSuffix}${prefix}Bind${suffix}EquipmentComponentId", equipmentBind.n_bind_object_id)
      }
    }
  }

  static void createEquipmentBind(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindRole        : 'NetConnection',
      withComponent   : true,
      equipmentPrefix : '',
      equipmentSuffix : '',
      componentPrefix : '',
      componentSuffix : '',
      prefix          : '',
      suffix          : '',
      hydra           : null
    ] + input

    def bindRole = params.bindRole
    def withComponent = params.withComponent
    def equipmentPrefix = StringUtil.capitalize(params.equipmentPrefix)
    def equipmentSuffix = StringUtil.capitalize(params.equipmentSuffix)
    def componentPrefix = StringUtil.capitalize(params.componentPrefix)
    def componentSuffix = StringUtil.capitalize(params.componentSuffix)
    def prefix = StringUtil.capitalize(params.prefix)
    def suffix = StringUtil.capitalize(params.suffix)
    def hydra = params.hydra ?: new Hydra(execution)
    def equipmentId     = execution.getVariable("homsOrderData${equipmentPrefix}Equipment${equipmentSuffix}Id")
    def componentId     = execution.getVariable("homsOrderData${equipmentPrefix}Equipment${equipmentSuffix}${componentPrefix}Component${componentSuffix}Id")
    def bindEquipmentId = null
    def bindComponentId = null

    if (withComponent) {
      bindEquipmentId = execution.getVariable("homsOrderData${equipmentPrefix}Equipment${equipmentSuffix}${componentPrefix}Component${componentSuffix}${prefix}Bind${suffix}EquipmentId")
      bindComponentId = execution.getVariable("homsOrderData${equipmentPrefix}Equipment${equipmentSuffix}${componentPrefix}Component${componentSuffix}${prefix}Bind${suffix}EquipmentComponentId")
    } else {
      bindEquipmentId = execution.getVariable("homsOrderData${equipmentPrefix}Equipment${equipmentSuffix}${prefix}Bind${suffix}EquipmentId")
      bindComponentId = execution.getVariable("homsOrderData${equipmentPrefix}Equipment${equipmentSuffix}${prefix}Bind${suffix}EquipmentComponentId")
    }

    def inp = [
      mainId          : equipmentId,
      bindRole        : "OBJOBJ_BIND_TYPE_${bindRole}",
      bindMainId      : bindEquipmentId,
      bindComponentId : bindComponentId
    ]

    if (withComponent) {
      inp.componentId = componentId
    }

    def equipmentBind = hydra.putEquipmentBind(inp)
    if (equipmentBind) {
      if (withComponent) {
        execution.setVariable("homsOrderData${equipmentPrefix}Equipment${equipmentSuffix}${componentPrefix}Component${componentSuffix}${prefix}Bind${suffix}Id", equipmentBind.num_N_OBJ_OBJECT_ID)
        execution.setVariable("homsOrderData${equipmentPrefix}Equipment${equipmentSuffix}${componentPrefix}Component${componentSuffix}${prefix}Bind${suffix}Created", true)
      } else {
        execution.setVariable("homsOrderData${equipmentPrefix}Equipment${equipmentSuffix}${prefix}Bind${suffix}Id", equipmentBind.num_N_OBJ_OBJECT_ID)
        execution.setVariable("homsOrderData${equipmentPrefix}Equipment${equipmentSuffix}${prefix}Bind${suffix}Created", true)
      }
    }
  }

  static void deleteEquipmentBind(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindRole        : 'NetConnection',
      withComponent   : true,
      equipmentPrefix : '',
      equipmentSuffix : '',
      componentPrefix : '',
      componentSuffix : '',
      prefix          : '',
      suffix          : '',
      hydra           : null
    ] + input

    def bindId = null
    def withComponent = params.withComponent
    def equipmentPrefix = StringUtil.capitalize(params.equipmentPrefix)
    def equipmentSuffix = StringUtil.capitalize(params.equipmentSuffix)
    def componentPrefix = StringUtil.capitalize(params.componentPrefix)
    def componentSuffix = StringUtil.capitalize(params.componentSuffix)
    def prefix = StringUtil.capitalize(params.prefix)
    def suffix = StringUtil.capitalize(params.suffix)
    def hydra = params.hydra ?: new Hydra(execution)

    if (withComponent) {
      bindId = execution.getVariable("homsOrderData${equipmentPrefix}Equipment${equipmentSuffix}${componentPrefix}Component${componentSuffix}${prefix}Bind${suffix}Id")
    } else {
      bindId = execution.getVariable("homsOrderData${equipmentPrefix}Equipment${equipmentSuffix}${prefix}Bind${suffix}Id")
    }

    def result = hydra.deleteEquipmentBind(bindId)
    if (result) {
      if (withComponent) {
        execution.setVariable("homsOrderData${equipmentPrefix}Equipment${equipmentSuffix}${componentPrefix}Component${componentSuffix}${prefix}Bind${suffix}Deleted", true)
      } else {
        execution.setVariable("homsOrderData${equipmentPrefix}Equipment${equipmentSuffix}${prefix}Bind${suffix}Deleted", true)
      }
    }
  }

  static void fetchEquipmentByAddress(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      addrType        : '',
      bindAddrType    : 'Serv',
      isMain          : true,
      operationDate   : null,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    def addrType        = params.addrType
    def bindAddrType    = params.bindAddrType
    def isMain          = params.isMain
    def operationDate   = params.operationDate
    def beginDate       = params.beginDate
    def endDate         = params.endDate
    def equipmentPrefix = StringUtil.capitalize(params.equipmentPrefix)
    def equipmentSuffix = StringUtil.capitalize(params.equipmentSuffix)
    def prefix = "homsOrderData${equipmentPrefix}Equipment${equipmentSuffix}"

    def addressId   = null
    def addressCode = null
    if (StringUtil.isEmpty(addrType)) {
      addressId   = execution.getVariable("${prefix}${bindAddrType}AddressId")
      addressCode = execution.getVariable("${prefix}${bindAddrType}Address")
    } else {
      addressId   = execution.getVariable("${prefix}${addrType}Id")
      addressCode = execution.getVariable("${prefix}${addrType}")
    }

    if (addressId == null && addressCode == null) {
      addressCode = [is: 'null']
    }
    def hydra   = params.hydra ?: new Hydra(execution)
    def address = hydra.getEntityAddress(operationDate   : operationDate,
                                        beginDate       : beginDate,
                                        endDate         : endDate,
                                        entityAddressId : addressId,
                                        code            : addressCode, //TODO нормальный поиск по адресам
                                        addrType        : "ADDR_TYPE_${addrType ?: 'FactPlace'}",
                                        bindAddrType    : "BIND_ADDR_TYPE_${bindAddrType}",
                                        isMain          : isMain)
    execution.setVariable("${prefix}Id", address?.n_object_id)
    fetchEquipment(execution, prefix: equipmentPrefix, suffix: equipmentSuffix, hydra: hydra)
  }

  static void fetchEquipmentByMAC(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      operationDate   : null,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    fetchEquipmentByAddress(execution,  addrType        : 'MAC',
                                        bindAddrType    : params.bindAddrType,
                                        isMain          : params.isMain,
                                        operationDate   : params.operationDate,
                                        beginDate       : params.beginDate,
                                        endDate         : params.endDate,
                                        equipmentPrefix : params.equipmentPrefix,
                                        equipmentSuffix : params.equipmentSuffix,
                                        prefix          : params.prefix,
                                        hydra           : params.hydra)
  }

  static void fetchEquipmentByIP(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      operationDate   : null,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    fetchEquipmentByAddress(execution,  addrType        : 'IP',
                                        bindAddrType    : params.bindAddrType,
                                        isMain          : params.isMain,
                                        operationDate   : params.operationDate,
                                        beginDate       : params.beginDate,
                                        endDate         : params.endDate,
                                        equipmentPrefix : params.equipmentPrefix,
                                        equipmentSuffix : params.equipmentSuffix,
                                        prefix          : params.prefix,
                                        hydra           : params.hydra)
  }

  static void fetchEquipmentByVLAN(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      operationDate   : null,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    fetchEquipmentByAddress(execution,  addrType        : 'VLAN',
                                        bindAddrType    : params.bindAddrType,
                                        isMain          : params.isMain,
                                        operationDate   : params.operationDate,
                                        beginDate       : params.beginDate,
                                        endDate         : params.endDate,
                                        equipmentPrefix : params.equipmentPrefix,
                                        equipmentSuffix : params.equipmentSuffix,
                                        prefix          : params.prefix,
                                        hydra           : params.hydra)
  }

  static void fetchEquipmentBySubnet(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      operationDate   : null,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    fetchEquipmentByAddress(execution,  addrType        : 'Subnet',
                                        bindAddrType    : params.bindAddrType,
                                        isMain          : params.isMain,
                                        operationDate   : params.operationDate,
                                        beginDate       : params.beginDate,
                                        endDate         : params.endDate,
                                        equipmentPrefix : params.equipmentPrefix,
                                        equipmentSuffix : params.equipmentSuffix,
                                        prefix          : params.prefix,
                                        hydra           : params.hydra)
  }

  // TODO: Move to Group trait
  static void fetchGroup(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      prefix : '',
      hydra  : null
    ] + input

    def prefix = StringUtil.capitalize(params.prefix)
    def hydra = params.hydra ?: new Hydra(execution)
    def group = hydra.getGroup(execution.getVariable("homsOrderData${prefix}GroupId"))

    execution.setVariable("homsOrderData${prefix}GroupName", group?.vc_name)
  }

  static void fetchCustomerGroup(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      customerPrefix : '',
      prefix         : '',
      hydra          : null
    ] + input
    fetchGroup(execution, prefix: "${params.customerPrefix}Customer${params.prefix}", hydra: params.hydra)
  }

  // TODO: Move to Individual trait
  static void fetchIndividual(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      subjectPrefix : '',
      prefix        : '',
      hydra         : null
    ] + input

    def subjectPrefix = StringUtil.capitalize(params.subjectPrefix)
    def prefix = StringUtil.capitalize(params.prefix)
    def hydra = params.hydra ?: new Hydra(execution)

    def baseSubjectId = execution.getVariable("homsOrderData${subjectPrefix}BaseSubjectId")
    def person = hydra.getPerson(baseSubjectId)
    def personPrivate = hydra.getPersonPrivate(baseSubjectId)
    def opfCode = hydra.getRefCodeById(person?.n_opf_id)

    def code = [person?.vc_surname ?: '', person?.vc_first_name ?: '', person?.vc_second_name ?: ''].join(' ').replace('""', '"').replace('--', '').trim()
    def name = [person?.n_opf_id ? opfCode : '', code].join(' ').trim()
    execution.setVariable("homsOrderData${subjectPrefix}BaseSubjectName",          name)
    execution.setVariable("homsOrderData${subjectPrefix}BaseSubjectCode",          code)
    execution.setVariable("homsOrderData${subjectPrefix}BaseSubjectINN",           person?.vc_inn)
    execution.setVariable("homsOrderData${subjectPrefix}BaseSubjectKPP",           person?.vc_kpp)
    execution.setVariable("homsOrderData${subjectPrefix}BaseSubjectOPFId",         person?.n_opf_id)
    execution.setVariable("homsOrderData${prefix}IndividualFirstName",             person?.vc_first_name)
    execution.setVariable("homsOrderData${prefix}IndividualSecondName",            person?.vc_second_name)
    execution.setVariable("homsOrderData${prefix}IndividualLastName",              person?.vc_surname)
    execution.setVariable("homsOrderData${prefix}IndividualGender",                person?.n_sex_id)
    execution.setVariable("homsOrderData${prefix}IndividualBirthDate",             person?.d_birth ? DateTimeUtil.local(person?.d_birth).format(DateTimeUtil.ISO_FORMAT) : null)
    execution.setVariable("homsOrderData${prefix}IndividualBirthPlace",            personPrivate?.vc_birth_place)
    execution.setVariable("homsOrderData${prefix}IndividualIdentType",             personPrivate?.n_doc_auth_type_id)
    execution.setVariable("homsOrderData${prefix}IndividualIdentSerial",           personPrivate?.vc_doc_serial)
    execution.setVariable("homsOrderData${prefix}IndividualIdentNumber",           personPrivate?.vc_doc_no)
    execution.setVariable("homsOrderData${prefix}IndividualIdentIssuedAuthor",     personPrivate?.vc_document)
    execution.setVariable("homsOrderData${prefix}IndividualIdentIssuedDate",       personPrivate?.d_doc ? DateTimeUtil.local(personPrivate?.d_doc).format(DateTimeUtil.ISO_FORMAT) : null)
    execution.setVariable("homsOrderData${prefix}IndividualIdentIssuedDepartment", personPrivate?.vc_doc_department)
  }

  static void createIndividual(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      subjectPrefix : '',
      prefix        : '',
      hydra         : null
    ] + input

    def subjectPrefix = StringUtil.capitalize(params.subjectPrefix)
    def prefix = StringUtil.capitalize(params.prefix)
    def hydra = params.hydra ?: new Hydra(execution)

    def orderData = Order.getData(execution)
    def individual = hydra.putPerson(
      firstName     : execution.getVariable("homsOrderData${prefix}IndividualFirstName"),
      secondName    : execution.getVariable("homsOrderData${prefix}IndividualSecondName"),
      lastName      : execution.getVariable("homsOrderData${prefix}IndividualLastName"),
      sexId         : execution.getVariable("homsOrderData${prefix}IndividualGender"),
      docTypeId     : execution.getVariable("homsOrderData${prefix}IndividualIdentType"),
      docSerial     : execution.getVariable("homsOrderData${prefix}IndividualIdentSerial"),
      docNumber     : execution.getVariable("homsOrderData${prefix}IndividualIdentNumber"),
      docDate       : StringUtil.notEmpty(execution.getVariable("homsOrderData${prefix}IndividualIdentIssuedDate")) ? DateTimeUtil.parse(execution.getVariable("homsOrderData${prefix}IndividualIdentIssuedDate"), DateTimeUtil.ISO_FORMAT) : null,
      docDepartment : execution.getVariable("homsOrderData${prefix}IndividualIdentIssuedDepartment"),
      docAuthor     : execution.getVariable("homsOrderData${prefix}IndividualIdentIssuedAuthor"),
      inn           : execution.getVariable("homsOrderData${subjectPrefix}BaseSubjectINN"),
      kpp           : execution.getVariable("homsOrderData${subjectPrefix}BaseSubjectKPP"),
      opfId         : execution.getVariable("homsOrderData${subjectPrefix}BaseSubjectOPFId"),
      birthDate     : StringUtil.notEmpty(execution.getVariable("homsOrderData${prefix}IndividualBirthDate")) ? DateTimeUtil.parse(execution.getVariable("homsOrderData${prefix}IndividualBirthDate"), DateTimeUtil.ISO_FORMAT) : null,
      birthPlace    : execution.getVariable("homsOrderData${prefix}IndividualBirthPlace")
    )
    if (individual) {
      execution.setVariable("homsOrderData${subjectPrefix}BaseSubjectId", individual.num_N_SUBJECT_ID)
      execution.setVariable("homsOrderData${subjectPrefix}BaseSubjectCreated", true)
    }
  }

  // TODO: Move to Region trait
  static void fetchRegion(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType : 'Serv',
      entityPrefix : '',
      prefix       : '',
      hydra        : null
    ] + input

    def bindAddrType = params.bindAddrType
    def entityPrefix = "homsOrderData${StringUtil.capitalize(params.entityPrefix)}"
    def prefix = "${entityPrefix}${StringUtil.capitalize(params.prefix)}${bindAddrType}"
    def hydra = params.hydra ?: new Hydra(execution)

    def regionId = execution.getVariable("${prefix}RegionId")
    def data = hydra.getRegionTree(regionId)

    data.each { k,v ->
      execution.setVariable("${prefix}${StringUtil.capitalize(k)}", v)
    }
    def regionLevel    = hydra.getRegionLevelByTypeCode(data?.regionType)
    def regionLevelNum = hydra.getRegionLevelNum(regionLevel)

    execution.setVariable("${prefix}RegionLevel",    regionLevel)
    execution.setVariable("${prefix}RegionLevelNum", regionLevelNum)
  }

  static void fetchBaseSubjectRegion(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType  : 'Actual',
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : '',
      hydra         : null
    ] + input

    fetchRegion(execution, bindAddrType: params.bindAddrType,
                          isMain: params.isMain,
                          entityPrefix: "${params.subjectPrefix}BaseSubject${params.subjectSuffix}",
                          prefix: params.prefix,
                          hydra: params.hydra)
  }

  static void fetchEquipmentRegion(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Serv',
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    fetchRegion(execution, bindAddrType: params.bindAddrType,
                          isMain: params.isMain,
                          entityPrefix: "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
                          prefix: params.prefix,
                          hydra: params.hydra)
  }

  static void createRegionTree(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType : 'Serv',
      prefix       : '',
      hydra        : null
    ] + input

    def bindAddrType  = params.bindAddrType
    def entityPrefix  = "homsOrderData${StringUtil.capitalize(params.entityPrefix)}"
    def _entityPrefix = StringUtil.decapitalize(params.entityPrefix)
    def prefix  = "${entityPrefix}${StringUtil.capitalize(params.prefix)}${bindAddrType}"
    def _prefix = "${_entityPrefix}${StringUtil.capitalize(params.prefix)}${bindAddrType}"
    def hydra = params.hydra ?: new Hydra(execution)
    def inp   = [:]

    def orderData = Order.getData(execution)
    Pattern pattern = Pattern.compile("^${_prefix}(.+)\$")
    orderData.each { key, value ->
      def group = (key =~ pattern)
      if (group.size() > 0) {
        String item = StringUtil.decapitalize(group[0][1])
        inp[item] = value
      }
    }
    def regionId = hydra.createRegionTree(inp)
    if (regionId != 00) {
      execution.setVariable("${prefix}RegionId",      regionId)
      execution.setVariable("${prefix}RegionCreated", true)
    }
  }

  static void createBaseSubjectRegionTree(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType  : 'Actual',
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : '',
      hydra         : null
    ] + input

    createRegionTree(execution, bindAddrType: params.bindAddrType,
                                isMain: params.isMain,
                                entityPrefix: "${params.subjectPrefix}BaseSubject${params.subjectSuffix}",
                                prefix: params.prefix,
                                hydra: params.hydra)
  }

  static void createEquipmentRegionTree(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      bindAddrType    : 'Serv',
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    createRegionTree(execution, bindAddrType: params.bindAddrType,
                                isMain: params.isMain,
                                entityPrefix: "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
                                prefix: params.prefix,
                                hydra: params.hydra)
  }

  // TODO: Move to Reseller trait
  static void fetchReseller(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      hydra : null
    ] + input

    def hydra = params.hydra ?: new Hydra(execution)
    def reseller = hydra.getReseller()
    execution.setVariable("homsOrderDataFirmId",       reseller?.n_firm_id)
    execution.setVariable("homsOrderDataResellerCode", reseller?.vc_code)
    execution.setVariable("homsOrderDataResellerName", reseller?.vc_name)
  }

  static void fetchResellerCustomer(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      hydra : null
    ] + input

    def hydra = params.hydra ?: new Hydra(execution)
    def reseller = hydra.getReseller()
    def resellerCustomer = hydra.getCustomerBy(baseSubjectId: reseller?.n_base_subject_id)
    execution.setVariable("homsOrderDataResellerCustomerId",  resellerCustomer?.n_subject_id)
    fetchCustomer(execution, prefix: 'Reseller', subjectPrefix: 'Reseller', hydra: hydra)
  }

  // TODO: Move to Service trait
  static void fetchService(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      prefix : '',
      hydra  : null
    ] + input

    def prefix = StringUtil.capitalize(params.prefix)
    def hydra = params.hydra ?: new Hydra(execution)
    def priceLineId = execution.getVariable("homsOrderData${prefix}PriceLineId")
    def priceLine = hydra.getPriceLine(priceLineId)

    execution.setVariable("homsOrderData${prefix}ServiceId",    priceLine?.n_good_id)
    execution.setVariable("homsOrderData${prefix}ServiceName",  priceLine?.vc_good_name)
    execution.setVariable("homsOrderData${prefix}ServicePrice", priceLine?.n_price)
    execution.setVariable("homsOrderData${prefix}ServicePriceWoTax", priceLine?.n_price_wo_tax)
  }

  static void fetchSubscription(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      parSubscriptionPrefix  : '',
      prevSubscriptionPrefix : '',
      accountPrefix          : '',
      contractPrefix         : '',
      servicePrefix          : '',
      equipmentPrefix        : '',
      prefix                 : '',
      hydra                  : null
    ] + input

    def parSubscriptionPrefix  = StringUtil.capitalize(params.parSubscriptionPrefix)
    def prevSubscriptionPrefix = StringUtil.capitalize(params.prevSubscriptionPrefix)
    def customerPrefix  = StringUtil.capitalize(params.customerPrefix)
    def accountPrefix   = StringUtil.capitalize(params.accountPrefix)
    def contractPrefix  = StringUtil.capitalize(params.contractPrefix)
    def servicePrefix   = StringUtil.capitalize(params.servicePrefix)
    def equipmentPrefix = StringUtil.capitalize(params.equipmentPrefix)
    def prefix = StringUtil.capitalize(params.prefix)
    def hydra  = params.hydra ?: new Hydra(execution)

    def subscriptionId = execution.getVariable("homsOrderData${equipmentPrefix}Equipment${servicePrefix}Service${prefix}SubscriptionId")
    def subscription = hydra.getSubscription(subscriptionId)

    execution.setVariable("homsOrderData${equipmentPrefix}Equipment${servicePrefix}Service${parSubscriptionPrefix}ParSubscriptionId", subscription?.n_par_subscription_id)
    execution.setVariable("homsOrderData${equipmentPrefix}Equipment${servicePrefix}Service${prevSubscriptionPrefix}PrevSubscriptionId", subscription?.n_prev_subscription_id)
    execution.setVariable("homsOrderData${equipmentPrefix}Equipment${servicePrefix}Service${prefix}SubscriptionBeginDate", subscription?.d_begin ? DateTimeUtil.local(subscription.d_begin).format(DateTimeUtil.ISO_FORMAT) : null)
    execution.setVariable("homsOrderData${equipmentPrefix}Equipment${servicePrefix}Service${prefix}SubscriptionEndDate", subscription?.d_end ? DateTimeUtil.local(subscription.d_end).format(DateTimeUtil.ISO_FORMAT) : null)
    execution.setVariable("homsOrderData${equipmentPrefix}Equipment${servicePrefix}Service${prefix}SubscriptionIsClosed", Oracle.decodeBool(subscription?.c_fl_closed))
    if (StringUtil.isEmpty(execution.getVariable("homsOrderData${accountPrefix}AccountId"))) {
      execution.setVariable("homsOrderData${accountPrefix}AccountId", subscription?.n_account_id)
    }
    if (StringUtil.isEmpty(execution.getVariable("homsOrderData${contractPrefix}ContractId"))) {
      execution.setVariable("homsOrderData${contractPrefix}ContractId", subscription?.n_doc_id)
    }
    if (StringUtil.isEmpty(execution.getVariable("homsOrderData${servicePrefix}ServiceId"))) {
      execution.setVariable("homsOrderData${servicePrefix}ServiceId", subscription?.n_service_id)
    }
    if (StringUtil.isEmpty(execution.getVariable("homsOrderData${equipmentPrefix}EquipmentId"))) {
      execution.setVariable("homsOrderData${equipmentPrefix}EquipmentId", subscription?.n_object_id)
    }

    def priceLine = hydra.hid.queryFirst("""
      SELECT 'n_price_line_id', N_PRICE_LINE_ID
      FROM
          TABLE(SI_SUBSCRIPTIONS_PKG_S.GET_AVAILABLE_SERVICES_P(
            num_N_CONTRACT_ID => ${subscription?.n_doc_id},
            num_N_ACCOUNT_ID  => ${subscription?.n_account_id},
            num_N_OBJECT_ID   => ${subscription?.n_object_id}
          ))
      WHERE N_SERVICE_ID = ${subscription?.n_service_id}
    """, true)
    execution.setVariable("homsOrderData${servicePrefix}PriceLineId", priceLine?.n_price_line_id)
    fetchService(execution, prefix: servicePrefix, hydra: hydra)
  }

  static void fetchServiceSubscription(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      parSubscriptionPrefix  : '',
      prevSubscriptionPrefix : '',
      customerPrefix         : '',
      accountPrefix          : '',
      contractPrefix         : '',
      servicePrefix          : '',
      equipmentPrefix        : '',
      prefix                 : '',
      beginDate              : null,
      endDate                : null,
      operationDate          : DateTimeUtil.local(),
      isClosed               : false,
      onlyParent             : true,
      hydra                  : null
    ] + input

    if (params.operationDate == null && params.beginDate == null) {
      params.beginDate = ['<': Oracle.encodeDateStr(DateTimeUtil.local())]
    }

    def parSubscriptionPrefix  = StringUtil.capitalize(params.parSubscriptionPrefix)
    def prevSubscriptionPrefix = StringUtil.capitalize(params.prevSubscriptionPrefix)
    def customerPrefix  = StringUtil.capitalize(params.customerPrefix)
    def accountPrefix   = StringUtil.capitalize(params.accountPrefix)
    def contractPrefix  = StringUtil.capitalize(params.contractPrefix)
    def servicePrefix   = StringUtil.capitalize(params.servicePrefix)
    def equipmentPrefix = StringUtil.capitalize(params.equipmentPrefix)
    def prefix = StringUtil.capitalize(params.prefix)
    def hydra  = params.hydra ?: new Hydra(execution)

    def customerId   = execution.getVariable("homsOrderData${customerPrefix}CustomerId")
    def accountId    = execution.getVariable("homsOrderData${accountPrefix}AccountId")
    def contractId   = execution.getVariable("homsOrderData${contractPrefix}ContractId")
    def serviceId    = execution.getVariable("homsOrderData${servicePrefix}ServiceId")
    def equipmentId  = execution.getVariable("homsOrderData${equipmentPrefix}EquipmentId")
    def parSubscriptionId = execution.getVariable("homsOrderData${equipmentPrefix}Equipment${servicePrefix}Service${parSubscriptionPrefix}ParSubscriptionId")
    def prevSubscriptionId = execution.getVariable("homsOrderData${equipmentPrefix}Equipment${servicePrefix}Service${prevSubscriptionPrefix}PrevSubscriptionId")
    def subscription = hydra.getSubscriptionBy(
      customerId         : customerId,
      accountId          : accountId,
      docId              : contractId,
      goodId             : serviceId,
      equipmentId        : equipmentId,
      beginDate          : params.beginDate,
      endDate            : params.endDate,
      operationDate      : params.operationDate,
      isClosed           : params.isClosed,
      parSubscriptionId  : parSubscriptionId ?: (params.onlyParent ? ['is null'] : null),
      prevSubscriptionId : prevSubscriptionId
    )

    execution.setVariable("homsOrderData${equipmentPrefix}Equipment${servicePrefix}Service${prefix}SubscriptionId", subscription?.n_subscription_id)

    if (StringUtil.isEmpty("homsOrderData${equipmentPrefix}Equipment${servicePrefix}Service${parSubscriptionPrefix}ParSubscriptionId")) {
      execution.setVariable("homsOrderData${equipmentPrefix}Equipment${servicePrefix}Service${parSubscriptionPrefix}ParSubscriptionId", subscription?.n_par_subscription_id)
    }

    if (StringUtil.isEmpty("homsOrderData${equipmentPrefix}Equipment${servicePrefix}Service${prevSubscriptionPrefix}PrevSubscriptionId")) {
      execution.setVariable("homsOrderData${equipmentPrefix}Equipment${servicePrefix}Service${prevSubscriptionPrefix}PrevSubscriptionId", subscription?.n_prev_subscription_id)
    }
    fetchSubscription(params + [hydra: hydra], execution)
  }

  static void createServiceSubscription(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      parSubscriptionPrefix  : '',
      prevSubscriptionPrefix : '',
      customerPrefix         : '',
      accountPrefix          : '',
      contractPrefix         : '',
      servicePrefix          : '',
      equipmentPrefix        : '',
      prefix                 : '',
      beginDate              : DateTimeUtil.local(),
      endDate                : null,
      payDay                 : null,
      hydra                  : null
    ] + input

    def parSubscriptionPrefix  = StringUtil.capitalize(params.parSubscriptionPrefix)
    def prevSubscriptionPrefix = StringUtil.capitalize(params.prevSubscriptionPrefix)
    def customerPrefix  = StringUtil.capitalize(params.customerPrefix)
    def accountPrefix   = StringUtil.capitalize(params.accountPrefix)
    def contractPrefix  = StringUtil.capitalize(params.contractPrefix)
    def servicePrefix   = StringUtil.capitalize(params.servicePrefix)
    def equipmentPrefix = StringUtil.capitalize(params.equipmentPrefix)
    def prefix = StringUtil.capitalize(params.prefix)
    def hydra  = params.hydra ?: new Hydra(execution)

    def customerId   = execution.getVariable("homsOrderData${customerPrefix}CustomerId")
    def accountId    = execution.getVariable("homsOrderData${accountPrefix}AccountId")
    def contractId   = execution.getVariable("homsOrderData${contractPrefix}ContractId")
    def serviceId    = execution.getVariable("homsOrderData${servicePrefix}ServiceId")
    def equipmentId  = execution.getVariable("homsOrderData${equipmentPrefix}EquipmentId")
    def parSubscriptionId = execution.getVariable("homsOrderData${equipmentPrefix}Equipment${servicePrefix}Service${parSubscriptionPrefix}ParSubscriptionId")
    def prevSubscriptionId = execution.getVariable("homsOrderData${equipmentPrefix}Equipment${servicePrefix}Service${prevSubscriptionPrefix}PrevSubscriptionId")
    def subscription = hydra.putSubscription(
      customerId         : customerId,
      accountId          : accountId,
      docId              : contractId,
      goodId             : serviceId,
      equipmentId        : equipmentId,
      beginDate          : params.beginDate,
      endDate            : params.endDate,
      invoiceEndDate     : params.endDate,
      payDay             : params.payDay,
      parSubscriptionId  : parSubscriptionId,
      prevSubscriptionId : prevSubscriptionId
    )

    if (subscription) {
      execution.setVariable("homsOrderData${equipmentPrefix}Equipment${servicePrefix}Service${prefix}SubscriptionId", subscription.num_N_SUBJ_GOOD_ID)
      execution.setVariable("homsOrderData${equipmentPrefix}Equipment${servicePrefix}Service${prefix}SubscriptionCreated", true)
    }
  }

  static void createOneOffServiceSubscription(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      accountPrefix         : '',
      contractPrefix        : '',
      servicePrefix         : '',
      equipmentPrefix       : '',
      parSubscriptionPrefix : '',
      prefix                : '',
      beginDate             : DateTimeUtil.local(),
      hydra                 : null
    ] + input

    def parSubscriptionPrefix = StringUtil.capitalize(params.parSubscriptionPrefix)
    def accountPrefix   = StringUtil.capitalize(params.accountPrefix)
    def contractPrefix  = StringUtil.capitalize(params.contractPrefix)
    def servicePrefix   = StringUtil.capitalize(params.servicePrefix)
    def equipmentPrefix = StringUtil.capitalize(params.equipmentPrefix)
    def prefix = StringUtil.capitalize(params.prefix)
    def hydra  = params.hydra ?: new Hydra(execution)

    def accountId    = execution.getVariable("homsOrderData${accountPrefix}AccountId")
    def contractId   = execution.getVariable("homsOrderData${contractPrefix}ContractId")
    def serviceId    = execution.getVariable("homsOrderData${servicePrefix}OneOffServiceId")
    def equipmentId  = execution.getVariable("homsOrderData${equipmentPrefix}EquipmentId")
    def parSubscriptionId = execution.getVariable("homsOrderData${equipmentPrefix}Equipment${servicePrefix}Service${parSubscriptionPrefix}ParSubscriptionId")
    def subscription = hydra.putOneOffSubscription(
      accountId         : accountId,
      docId             : contractId,
      goodId            : serviceId,
      equipmentId       : equipmentId,
      parSubscriptionId : parSubscriptionId,
      beginDate         : params.beginDate
    )

    if (subscription) {
      execution.setVariable("homsOrderData${equipmentPrefix}Equipment${servicePrefix}OneOffService${prefix}SubscriptionId", subscription.num_N_SUBSCRIPTION_ID)
      execution.setVariable("homsOrderData${equipmentPrefix}Equipment${servicePrefix}OneOffService${prefix}SubscriptionCreated", true)
    }
  }

  static void createAdjustment(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      accountPrefix   : '',
      contractPrefix  : '',
      servicePrefix   : '',
      equipmentPrefix : '',
      prefix          : '',
      sum             : null,
      sumWoTax        : null,
      operationDate   : DateTimeUtil.local(),
      hydra           : null
    ] + input

    def accountPrefix   = StringUtil.capitalize(params.accountPrefix)
    def contractPrefix  = StringUtil.capitalize(params.contractPrefix)
    def servicePrefix   = StringUtil.capitalize(params.servicePrefix)
    def equipmentPrefix = StringUtil.capitalize(params.equipmentPrefix)
    def prefix = StringUtil.capitalize(params.prefix)
    def hydra  = params.hydra ?: new Hydra(execution)

    def accountId    = execution.getVariable("homsOrderData${accountPrefix}AccountId")
    def contractId   = execution.getVariable("homsOrderData${contractPrefix}ContractId")
    def serviceId    = execution.getVariable("homsOrderData${servicePrefix}AdjustmentServiceId")
    def equipmentId  = execution.getVariable("homsOrderData${equipmentPrefix}EquipmentId")
    def adjustment = hydra.putAdjustment(
      accountId     : accountId,
      docId         : contractId,
      goodId        : serviceId,
      equipmentId   : equipmentId,
      sum           : params.sum,
      sumWoTax      : params.sumWoTax,
      operationDate : params.operationDate
    )

    if (adjustment) {
      execution.setVariable("homsOrderData${equipmentPrefix}Equipment${servicePrefix}Adjustment${prefix}Created", true)
    }
  }

  static void closeServiceSubscription(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      servicePrefix   : '',
      equipmentPrefix : '',
      prefix          : '',
      endDate         : DateTimeUtil.local(),
      hydra           : null
    ] + input

    def servicePrefix   = StringUtil.capitalize(params.servicePrefix)
    def equipmentPrefix = StringUtil.capitalize(params.equipmentPrefix)
    def prefix = StringUtil.capitalize(params.prefix)
    def hydra  = params.hydra ?: new Hydra(execution)

    def subscriptionId = execution.getVariable("homsOrderData${equipmentPrefix}Equipment${servicePrefix}Service${prefix}SubscriptionId")
    def result         = hydra.closeSubscriptionForce(subscriptionId, params.endDate)

    if (result) {
      execution.setVariable("homsOrderData${equipmentPrefix}Equipment${servicePrefix}Service${prefix}SubscriptionCloseDate", params.endDate.format(DateTimeUtil.ISO_FORMAT))
      execution.setVariable("homsOrderData${equipmentPrefix}Equipment${servicePrefix}Service${prefix}SubscriptionClosed", true)
    }
  }

  static void deleteServiceSubscription(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      servicePrefix   : '',
      equipmentPrefix : '',
      prefix          : '',
      hydra           : null
    ] + input

    def servicePrefix   = StringUtil.capitalize(params.servicePrefix)
    def equipmentPrefix = StringUtil.capitalize(params.equipmentPrefix)
    def prefix = StringUtil.capitalize(params.prefix)
    def hydra  = params.hydra ?: new Hydra(execution)

    def subscriptionId = execution.getVariable("homsOrderData${equipmentPrefix}Equipment${servicePrefix}Service${prefix}SubscriptionId")
    def result         = hydra.deleteSubscriptionForce(subscriptionId)

    if (result) {
      execution.setVariable("homsOrderData${equipmentPrefix}Equipment${servicePrefix}Service${prefix}SubscriptionDeleted", true)
    }
  }

  // TODO: Move to Spartial trait
  static void fetchTargetingData(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    def equipmentPrefix = StringUtil.capitalize(params.equipmentPrefix)
    def equipmentSuffix = StringUtil.capitalize(params.equipmentSuffix)
    def prefix = "homsOrderData${equipmentPrefix}Equipment${equipmentSuffix}${StringUtil.capitalize(params.prefix)}"
    def hydra = params.hydra ?: new Hydra(execution)

    def equipmentId   = execution.getVariable("${prefix}Id")
    def stringParams  = ['DirectPolarization', 'DirectRFCC']
    def numericParams = ['Azimuth', 'AntennaPhi', 'AntennaDiameter', 'DirectdB', 'DirectRayNumber', 'DirectEIRP', 'ReversedB', 'ReversedBK', 'ReverseGT']

    (stringParams + numericParams).each { it ->
      def result = hydra.getEquipmentAddParamBy(
        equipmentId : equipmentId ?: [is: 'null'],
        param       : "EQUIP_ADD_PARAM_${it}"
      )
      def value = null
      if (stringParams.contains(it)) {
        value = result?.vc_value
      } else {
        value = result?.n_value?.replace(',', '.')
        if (value?.contains('.')) {
          value = Numeric.toFloatSafe(value)
        } else {
          value = Numeric.toIntSafe(value)
        }
      }
      execution.setVariable("${prefix}${it}", value)
    }
  }

  static void calcTargetingData(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      address         : '',
      bindAddrType    : 'Serv',
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      spartialService : null,
      longtitude      : '',
      latitude        : ''
    ] + input

    def address = params.address
    def longtitude = params.longtitude
    def latitude = params.latitude
    def bindAddrType = params.bindAddrType
    def equipmentPrefix = StringUtil.capitalize(params.equipmentPrefix)
    def equipmentSuffix = StringUtil.capitalize(params.equipmentSuffix)
    def prefix = "homsOrderData${equipmentPrefix}Equipment${equipmentSuffix}${StringUtil.capitalize(params.prefix)}"
    def spartialService = params.spartialService ?: new HTTPRestProcessor(baseUrl:   execution.getVariable('spartialServiceUrl'), execution: execution)
    address = address ?: execution.getVariable("${prefix}${bindAddrType}Address")
    longtitude = longtitude ?: execution.getVariable("${prefix}${bindAddrType}Longtitude")
    latitude = latitude ?: execution.getVariable("${prefix}${bindAddrType}Latitude")
    def antennaDiameter = execution.getVariable("${prefix}AntennaDiameter")
    def body = [address: address, antenna: antennaDiameter]
    if (longtitude) {
      body = [longtitude: longtitude, latitude: latitude, antenna: antennaDiameter]
    }
    def spartialServicePath = execution.getVariable('spartialServicePath')
    def response = spartialService.sendRequest(path: "${spartialServicePath}/targeting_data", body: body, 'post')

    execution.setVariable("${prefix}Longtitude",         response?.longtitude)
    execution.setVariable("${prefix}Latitude",           response?.latitude)
    execution.setVariable("${prefix}Azimuth",            response?.azimuth)
    execution.setVariable("${prefix}AntennaPhi",         response?.phi)
    execution.setVariable("${prefix}DirectdB",           response?.direct?.dB)
    execution.setVariable("${prefix}DirectRayNumber",    response?.direct?.ray)
    execution.setVariable("${prefix}DirectPolarization", response?.direct?.polarization)
    execution.setVariable("${prefix}DirectRFCC",         response?.direct?.rfcc)
    execution.setVariable("${prefix}DirectEIRP",         response?.direct?.eirp)
    execution.setVariable("${prefix}ReversedB",          response?.reverse?.dB)
    execution.setVariable("${prefix}ReversedBK",         response?.reverse?.dBK)
    execution.setVariable("${prefix}ReverseGT",          response?.reverse?.gt)
  }

  static void saveTargetingData(LinkedHashMap input, DelegateExecution execution) {
    def params = [
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      hydra           : null
    ] + input

    def equipmentPrefix = StringUtil.capitalize(params.equipmentPrefix)
    def equipmentSuffix = StringUtil.capitalize(params.equipmentSuffix)
    def prefix = "homsOrderData${equipmentPrefix}Equipment${equipmentSuffix}${StringUtil.capitalize(params.prefix)}"
    def hydra = params.hydra ?: new Hydra(execution)

    def equipmentId   = execution.getVariable("${prefix}Id")
    def stringParams  = ['DirectPolarization', 'DirectRFCC']
    def numericParams = ['Azimuth', 'AntennaPhi', 'AntennaDiameter', 'DirectdB', 'DirectRayNumber', 'DirectEIRP', 'ReversedB', 'ReversedBK', 'ReverseGT']
    def result        = true

    (stringParams + numericParams).each { it ->
      def value = execution.getVariable("${prefix}${it}")
      def inp = [
        equipmentId : equipmentId,
        param       : "EQUIP_ADD_PARAM_${it}"
      ]
      inp[stringParams.contains(it) ? 'string' : 'number'] = value

      def res = hydra.putEquipmentAddParam(inp)
      result = res && result
    }

    execution.setVariable("${prefix}TargetingDataSet", result)
  }
}