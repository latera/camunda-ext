package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.StringUtil.capitalize
import static org.camunda.latera.bss.utils.StringUtil.decapitalize
import static org.camunda.latera.bss.utils.StringUtil.isEmpty
import static org.camunda.latera.bss.utils.DateTimeUtil.local
import static org.camunda.latera.bss.utils.MapUtil.mergeNotNull
import java.util.regex.Pattern

trait Address {
  void fetchBaseSubjectContacts(Map input = [:]) {
    Map params = [
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : '',
      withId        : false
    ] + input
    fetchBaseSubjectPhone(params)
    fetchBaseSubjectEmail(params)
  }

  void fetchAddress(Map input = [:]) {
    Map params = [
      addrType      : '',
      bindAddrType  : 'Serv',
      isMain        : true,
      operationDate : null,
      beginDate     : null,
      endDate       : null,
      entityPrefix  : '',
      prefix        : '',
      withId        : true
    ] + input

    String entityPrefix = capitalize(params.entityPrefix)
    String prefix       = "${entityPrefix}${capitalize(params.prefix)}"

    String entityId = order."${entityPrefix}Id"
    if (entityId) {
      String entityType = order."${entityPrefix}Type"
      def address = hydra.getEntityAddressBy(
        entityType    : entityType,
        entityId      : entityId,
        operationDate : params.operationDate,
        beginDate     : params.beginDate,
        endDate       : params.endDate,
        addrType      : "ADDR_TYPE_${params.addrType ?: 'FactPlace'}",
        bindAddrType  : "BIND_ADDR_TYPE_${params.bindAddrType}",
        isMain        : params.isMain
      )

      if (address) {
        if (isEmpty(params.addrType)) {
          order."${prefix}${params.bindAddrType}RegionId" = address.n_region_id

          if (params.withId) {
            order."${prefix}${params.bindAddrType}AddressId" = address.n_address_id
          }

          hydra.getAddressFieldNames().each{ name ->
            String part = capitalize(name)
            order."${prefix}${params.bindAddrType}${part}" = address."vc_${name}" ?: address?."n_${name}_no"
          }

          calcAddress(
            bindAddrType : params.bindAddrType,
            entityPrefix : params.entityPrefix,
            prefix       : params.prefix
          )
        } else {
          order."${prefix}${params.addrType}" = address.vc_visual_code
        }
      }
    }
  }

  void fetchBaseSubjectAddress(Map input = [:]) {
    Map params = [
      addrType      : '',
      bindAddrType  : 'Actual',
      isMain        : true,
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : '',
      withId        : false
    ] + input

    fetchAddress(params + [
      entityPrefix : "${params.subjectPrefix}BaseSubject${params.subjectSuffix}"
    ])
  }

  void fetchBaseSubjectPhone(Map input = [:]) {
    Map params = [
      bindAddrType  : 'Notice',
      isMain        : true,
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : '',
      withId        : false
    ] + input

    fetchBaseSubjectAddress(params + [
      addrType : 'Telephone'
    ])
  }

  void fetchBaseSubjectTelephone(Map input = [:]) {
    fetchBaseSubjectPhone(input)
  }

  void fetchBaseSubjectEmail(Map input = [:]) {
    Map params = [
      bindAddrType  : 'Notice',
      isMain        : true,
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : '',
      withId        : false
    ] + input

    fetchBaseSubjectAddress(params + [
      addrType : 'EMail'
    ])
  }

  void fetchEquipmentAddress(Map input = [:]) {
    Map params = [
      addrType        : '',
      bindAddrType    : 'Serv',
      isMain          : true,
      operationDate   : null,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      withId          : false
    ] + input

    fetchAddress(params + [
      entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}",
    ])
  }

  void fetchEquipmentMAC(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      operationDate   : null,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      withId          : false
    ] + input

    fetchEquipmentAddress(params + [
      addrType : 'MAC'
    ])
  }

  void fetchEquipmentIP(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      operationDate   : null,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      withId          : false
    ] + input

    fetchEquipmentAddress(params + [
      addrType : 'IP'
    ])

    String equipmentPrefix     = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}"
    String prefix              = "${equipmentPrefix}${capitalize(params.prefix)}"
    order."${prefix}IPMask"    = hydra.getIPMask(order."${prefix}IP")
    order."${prefix}IPGateway" = hydra.getIPGateway(order."${prefix}IP")
  }

  void fetchEquipmentVLAN(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      operationDate   : null,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      withId          : false
    ] + input

    fetchEquipmentAddress(params + [
      addrType : 'VLAN'
    ])
  }

  void fetchEquipmentSubnet(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      operationDate   : null,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      withId          : false
    ] + input

    fetchEquipmentAddress(params + [
      addrType : 'Subnet'
    ])

    String equipmentPrefix         = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}"
    String prefix                  = "${equipmentPrefix}${capitalize(params.prefix)}"
    order."${prefix}SubnetMask"    = hydra.getSubnetMask(order."${prefix}Subnet")
    order."${prefix}SubnetGateway" = hydra.getSubnetGateway(order."${prefix}Subnet")
  }

  void fetchEquipmentSubnet6(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      operationDate   : null,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      withId          : false
    ] + input

    fetchEquipmentAddress(params + [
      addrType : 'Subnet6'
    ])

    String equipmentPrefix          = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}"
    String prefix                   = "${equipmentPrefix}${capitalize(params.prefix)}"
    order."${prefix}Subnet6Mask"    = hydra.getSubnetMask(order."${prefix}Subnet6")
    order."${prefix}Subnet6Gateway" = hydra.getSubnetGateway(order."${prefix}Subnet6")
  }

  void fetchEquipmentTelephone(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      operationDate   : null,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      withId          : false
    ] + input

    fetchEquipmentAddress(params + [
      addrType : 'Telephone'
    ])
  }

  void fetchEquipmentFreeIP(Map input = [:]) {
    Map params = [
      groupId         : null,
      objectId        : null,
      subnetAddressId : null,
      operationDate   : local(),
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    def addrType = 'IP'
    def prefix   = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}${capitalize(params.prefix)}"

    def address  = hydra.getFreeIP(params)
    if (address) {
      order."${prefix}${addrType}" = address
    }
  }

  void fetchEquipmentFreeSubnet6(Map input = [:]) {
    Map params = [
      groupId         : null,
      objectId        : null,
      subnetAddressId : null,
      operationDate   : local(),
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    def addrType = 'Subnet6'
    def prefix   = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}${capitalize(params.prefix)}"

    def address  = hydra.getFreeIPv6Subnet(params)
    if (address) {
      order."${prefix}${addrType}" = address
    }
  }

  void fetchEquipmentFreeTelephone(Map input = [:]) {
    Map params = [
      groupId         : null,
      objectId        : null,
      telCodeId       : null,
      operationDate   : local(),
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    def addrType = 'Telephone'
    def prefix   = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}${capitalize(params.prefix)}"

    def address  = hydra.getFreePhoneNumber(params)
    if (address) {
      order."${prefix}${addrType}" = address
    }
  }

  void fetchEquipmentFreeSubnet(Map input = [:]) {
    Map params = [
      groupId         : null,
      rootId          : null,
      objectId        : null,
      mask            : null,
      operationDate   : local(),
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    def addrType = 'Subnet'
    def prefix   = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}${capitalize(params.prefix)}"

    def address  = hydra.getFreeSubnet(params)
    if (address) {
      order."${prefix}${addrType}" = address
    }
  }

  Map parseAddress(Map input = [:]) {
    Map params = [
      addrType     : '',
      bindAddrType : 'Actual',
      entityPrefix : '',
      prefix       : ''
    ] + input
    String prefix = ''
    Map result    = [:]

    if (isEmpty(params.addrType)) {
      prefix = "${capitalize(params.entityPrefix)}${capitalize(params.prefix)}${params.bindAddrType}"
      Pattern regexp = Pattern.compile("^${prefix}(RegionId|${hydra.getAddressFieldNames().join('|')})\$", Pattern.CASE_INSENSITIVE)

      result = parseRegion(input)
      order.data.each { name, value ->
        def group = (name =~ regexp)
        if (group.size() > 0) {
          result[decapitalize(group[0][1])] = value
        }
      }
    } else {
      prefix = "${capitalize(params.entityPrefix)}${capitalize(params.prefix)}${params.addrType}"
      result = [
        code: order."${prefix}"
      ]
    }

    return result
  }

  Map parseBaseSubjectAddress(Map input = [:]) {
    Map params = [
      addrType      : '',
      bindAddrType  : 'Actual',
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : ''
    ] + input

    return parseAddress(params + [
      entityPrefix : "${params.subjectPrefix}BaseSubject${params.subjectSuffix}"
    ])
  }

  Map parseEquipmentAddress(Map input = [:]) {
    Map params = [
      addrType        : '',
      bindAddrType    : 'Serv',
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    return parseAddress(params + [
      entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}"
    ])
  }

  Boolean isAddressEmpty(Map input = [:]) {
    Map params = [
      addrType     : '',
      bindAddrType : 'Actual',
      entityPrefix : '',
      prefix       : '',
      write        : true
    ] + input

    Map address    = parseAddress(params)
    Boolean result = hydra.isAddressEmpty(address)

    if (params.write) {
      String prefix = ''
      if (isEmpty(params.addrType)) {
        prefix = "${capitalize(params.entityPrefix)}${capitalize(params.prefix)}${params.bindAddrType}Address"
      } else {
        prefix = "${capitalize(params.entityPrefix)}${capitalize(params.prefix)}${params.addrType}"
      }
      order."${prefix}Empty" = result
    }
    return result
  }

  Boolean notAddressEmpty(Map input = [:]) {
    return !isAddressEmpty(input)
  }

  Boolean isBaseSubjectAddressEmpty(Map input = [:]) {
    Map params = [
      addrType      : '',
      bindAddrType  : 'Actual',
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : '',
      write         : true
    ] + input

    return isAddressEmpty(params + [
      entityPrefix : "${params.subjectPrefix}BaseSubject${params.subjectSuffix}"
    ])
  }

  Boolean notBaseSubjectAddressEmpty(Map input = [:]) {
    return !isBaseSubjectAddressEmpty(input)
  }

  Boolean isBaseSubjectPhoneEmpty(Map input = [:]) {
    Map params = [
      bindAddrType  : 'Notice',
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : '',
      write         : true
    ] + input

    return isBaseSubjectAddressEmpty(params + [
      addrType : 'Telephone'
    ])
  }

  Boolean notBaseSubjectPhoneEmpty(Map input = [:]) {
    return !isBaseSubjectPhoneEmpty(input)
  }

  Boolean isBaseSubjectTelephoneEmpty(Map input = [:]) {
    return isBaseSubjectPhoneEmpty(input)
  }

  Boolean notBaseSubjectTelephoneEmpty(Map input = [:]) {
    return !isBaseSubjectTelephoneEmpty(input)
  }

  Boolean isBaseSubjectEmailEmpty(Map input = [:]) {
    Map params = [
      bindAddrType  : 'Notice',
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : '',
      write         : true
    ] + input

    return isBaseSubjectAddressEmpty(params + [
      addrType : 'EMail'
    ])
  }

  Boolean notBaseSubjectEmailEmpty(Map input = [:]) {
    return !isBaseSubjectEmailEmpty(input)
  }

  Boolean isEquipmentAddressEmpty(Map input = [:]) {
    Map params = [
      addrType        : '',
      bindAddrType    : 'Serv',
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      write           : true
    ] + input

    return isAddressEmpty(params + [
      entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}"
    ])
  }

  Boolean notEquipmentAddressEmpty(Map input = [:]) {
    return !isEquipmentAddressEmpty(input)
  }

  Boolean isEquipmentMACEmpty(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      write           : true
    ] + input

    return isEquipmentAddressEmpty(params + [
      addrType : 'MAC'
    ])
  }

  Boolean notEquipmentMACEmpty(Map input = [:]) {
    return !isEquipmentMACEmpty(input)
  }

  Boolean isEquipmentIPEmpty(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      write           : true
    ] + input

    return isEquipmentAddressEmpty(params + [
      addrType : 'IP'
    ])
  }

  Boolean notEquipmentIPEmpty(Map input = [:]) {
    return !isEquipmentIPEmpty(input)
  }

  Boolean isEquipmentSubnet6Empty(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      write           : true
    ] + input

    return isEquipmentAddressEmpty(params + [
      addrType : 'Subnet6'
    ])
  }

  Boolean notEquipmentSubnet6Empty(Map input = [:]) {
    return !isEquipmentSubnet6Empty(input)
  }

  Boolean isEquipmentVLANEmpty(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      write           : true
    ] + input

    return isEquipmentAddressEmpty(params + [
      addrType : 'VLAN'
    ])
  }

  Boolean notEquipmentVLANEmpty(Map input = [:]) {
    return !isEquipmentVLANEmpty(input)
  }

  Boolean isEquipmentSubnetEmpty(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      write           : true
    ] + input

    return isEquipmentAddressEmpty(params + [
      addrType : 'Subnet'
    ])
  }

  Boolean notEquipmentSubnetEmpty(Map input = [:]) {
    return !isEquipmentSubnetEmpty(input)
  }

  Boolean isEquipmentPhoneEmpty(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      write           : true
    ] + input

    return isEquipmentAddressEmpty(params + [
      addrType : 'Telephone'
    ])
  }

  Boolean notEquipmentPhoneEmpty(Map input = [:]) {
    return !isEquipmentPhoneEmpty(input)
  }

  Boolean isEquipmentTelephoneEmpty(Map input = [:]) {
    return isEquipmentPhoneEmpty(input)
  }

  Boolean notEquipmentTelephoneEmpty(Map input = [:]) {
    return !isEquipmentTelephoneEmpty(input)
  }

  String calcAddress(Map input = [:]) {
    Map params = [
      addrType     : '',
      bindAddrType : 'Actual',
      entityPrefix : '',
      prefix       : '',
      write        : true
    ] + input

    String prefix = ''

    if (isEmpty(params.addrType)) {
      prefix = "${capitalize(params.entityPrefix)}${capitalize(params.prefix)}${params.bindAddrType}"
    } else {
      prefix = "${capitalize(params.entityPrefix)}${capitalize(params.prefix)}${params.addrType}"
    }

    if (isEmpty(params.addrType)) {

      // Some magic to solve CONSULT-2340

      // get current region fields
      Map current_address = parseAddress(params)

      // get previous region fields
      fetchRegion(bindAddrType: params.bindAddrType, entityPrefix: params.entityPrefix, prefix: params.prefix)

      // get previous region fields
      Map previous_address = parseAddress(params)

      // update previous region fields with new ones, but only if they were empty before
      // 'Moscow, Some district' from DB + 'New st.' manual input -> 'Moscow, Some district, New st.'
      // but 'Moscow, Some district' from DB + '[empty] district, New st.' manual input -> still 'Moscow, Some district, New st.'
      // if RegionId if empty, nothing will change
      Map new_address = mergeNotNull(previous_address, current_address)

      // save new region fields to variables
      new_address.each { key, value ->
        String part = capitalize(key)
        order."${prefix}${part}" = value
      }
      // End of magic

      String result = hydra.calcAddress(new_address)
      if (params.write) {
        order."${prefix}Address" = result
      }
      return result
    } else {
      return order."${prefix}"
    }
  }

  String calcBaseSubjectAddress(Map input = [:]) {
    Map params = [
      addrType      : '',
      bindAddrType  : 'Actual',
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : '',
      write         : true
    ] + input

    return calcAddress(params + [
      entityPrefix : "${params.subjectPrefix}BaseSubject${params.subjectSuffix}"
    ])
  }

  String calcEquipmentAddress(Map input = [:]) {
    Map params = [
      addrType        : '',
      bindAddrType    : 'Serv',
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      write           : true
    ] + input

    return calcAddress(params + [
      entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}"
    ])
  }

  Boolean createAddress(Map input = [:]) {
    Map params = [
      addrType           : '',
      bindAddrType       : 'Actual',
      isMain             : true,
      beginDate          : null,
      endDate            : null,
      entityPrefix       : '',
      prefix             : '',
      parentPrefix       : '',
      parentAddrType     : '',
      parentBindAddrType : ''
    ] + input

    String entityPrefix = capitalize(params.entityPrefix)
    String prefix       = "${entityPrefix}${capitalize(params.prefix)}"
    String parentPrefix = capitalize(params.parentPrefix)

    String entityId   = order."${entityPrefix}Id"
    String entityType = order."${entityPrefix}Type"

    Map inp = [
      entityId     : entityId,
      entityType   : entityType,
      bindAddrType : "BIND_ADDR_TYPE_${params.bindAddrType}",
      addrType     : "ADDR_TYPE_${params.addrType ?: 'FactPlace'}",
      isMain       : params.isMain,
      beginDate    : params.beginDate,
      endDate      : params.endDate
    ]
    String prefixId       = ''
    String parentPrefixId = ''

    if (isEmpty(params.addrType)) {
      Pattern pattern = Pattern.compile("^${prefix}${params.bindAddrType}(RegionId|${hydra.getAddressFieldNames().join('|')})\$", Pattern.CASE_INSENSITIVE)
      order.data.each { String key, def value ->
        def group = (key =~ pattern)
        if (group.size() > 0) {
          String item = decapitalize(group[0][1])
          inp[item] = value
        }
      }
      prefixId       = "${params.bindAddrType}Address"
      parentPrefixId = "${params.bindAddrType}${parentPrefix}ParentAddress"
    } else {
      inp.code       = order."${prefix}${params.addrType}"
      prefixId       = "${params.addrType}"
      parentPrefixId = "${params.addrType}${parentPrefix}Parent${params.parentAddrType}"
    }

    // e.g. equipmentIP is set and equipmentIPParentVLAN is filled up with VLAN, so created IP address will bound to VLAN
    def parAddress   = order."${prefix}${parentPrefixId}"
    def parAddressId = order."${prefix}${parentPrefixId}Id"
    if (parAddressId || parAddress) {
      Map parAddressParams = [
        entityId     : entityId,
        addrType     : params.parentAddrType     ? "ADDR_TYPE_${params.parentAddrType}"          : null,
        bindAddrType : params.parentBindAddrType ? "BIND_ADDR_TYPE_${params.parentBindAddrType}" : null
      ]
      if (parAddressId) {
        parAddressParams.addressId = parAddressId
      } else if (parAddress) {
        parAddressParams.code = parAddress
      }
      inp.parEntityAddressId = hydra.getEntityAddressBy(parAddressParams)?.n_obj_address_id
    }

    Boolean result = false
    Map address = hydra.putEntityAddress(inp)
    if (address) {
      order."${prefix}${prefixId}" = address.num_N_ENTITY_ADDRESS_ID
      result = true
    }
    order."${prefix}${prefixId}Created" = result
    return result
  }

  Boolean createBaseSubjectAddress(Map input = [:]) {
    Map params = [
      addrType      : '',
      bindAddrType  : 'Actual',
      isMain        : true,
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : ''
    ] + input

    return createAddress(params + [
      entityPrefix : "${params.subjectPrefix}BaseSubject${params.subjectSuffix}"
    ])
  }

  Boolean createBaseSubjectPhone(Map input = [:]) {
    Map params = [
      bindAddrType  : 'Notice',
      isMain        : true,
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : ''
    ] + input

    return createBaseSubjectAddress(params + [
      addrType : 'Telephone'
    ])
  }

  Boolean createBaseSubjectTelephone(Map input = [:]) {
    return createBaseSubjectPhone(input)
  }

  Boolean createBaseSubjectEmail(Map input = [:]) {
    Map params = [
      bindAddrType  : 'Notice',
      isMain        : true,
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : ''
    ] + input

    return createBaseSubjectAddress(params + [
      addrType : 'EMail'
    ])
  }

  Boolean createEquipmentAddress(Map input = [:]) {
    Map params = [
      addrType        : '',
      bindAddrType    : 'Serv',
      isMain          : true,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    return createAddress(params + [
      entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}"
    ])
  }

  Boolean createEquipmentMAC(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    return createEquipmentAddress(params + [
      addrType : 'MAC'
    ])
  }

  Boolean createEquipmentIP(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    return createEquipmentAddress(params + [
      addrType : 'IP'
    ])
  }

  Boolean createEquipmentSubnet6(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    return createEquipmentAddress(params + [
      addrType : 'Subnet6'
    ])
  }

  Boolean createEquipmentVLAN(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    return createEquipmentAddress(params + [
      addrType : 'VLAN'
    ])
  }

  Boolean createEquipmentSubnet(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    return createEquipmentAddress(params + [
      addrType : 'Subnet'
    ])
  }

  Boolean createEquipmentPhone(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    return createEquipmentAddress(params + [
      addrType : 'Telephone'
    ])
  }

  Boolean createEquipmentTelephone(Map input = [:]) {
    return createEquipmentPhone(input)
  }

  Boolean assignEquipmentFreeIP(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      groupId         : null,
      objectId        : null,
      subnetAddressId : null,
      operationDate   : local(),
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    fetchEquipmentFreeIP(params)
    return createEquipmentIP(params)
  }

  Boolean assignEquipmentFreeSubnet6(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      groupId         : null,
      objectId        : null,
      subnetAddressId : null,
      operationDate   : local(),
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    fetchEquipmentFreeSubnet6(params)
    return createEquipmentSubnet6(params)
  }

  Boolean assignEquipmentFreeTelephone(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      groupId         : null,
      telCodeId       : null,
      operationDate   : local(),
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    fetchEquipmentFreeTelephone(params)
    return createEquipmentTelephone(params)
  }

  Boolean assignEquipmentFreeSubnet(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      groupId         : null,
      rootId          : null,
      mask            : null,
      operationDate   : local(),
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    fetchEquipmentFreeSubnet(params)
    return createEquipmentSubnet(params)
  }

  Boolean closeAddress(Map input = [:]) {
    Map params = [
      addrType      : '',
      bindAddrType  : 'Actual',
      isMain        : true,
      endDate       : null,
      entityPrefix  : '',
      prefix        : ''
    ] + input

    def addrType      = params.addrType
    def bindAddrType  = params.bindAddrType
    def isMain        = params.isMain
    def endDate       = params.endDate
    def entityPrefix  = capitalize(params.entityPrefix)
    def prefix        = "${entityPrefix}${capitalize(params.prefix)}"

    String entityId   = order."${entityPrefix}Id"
    String entityType = order."${entityPrefix}Type"

    String prefixId     = ''
    String prefixCode   = ''
    String prefixClosed = ''
    if (isEmpty(addrType)) {
      prefixId     = "${bindAddrType}AddressId" //baseSubjectJurAddressId. Other address fields are not used because we don't actually want to search for address by regionId, entrance, floor and flat
      prefixClosed = "${bindAddrType}AddressClosed" //baseSubjectJurAddressClosed
    } else {
      prefixCode   = addrType           //equipmentSubnet
      prefixId     = "${addrType}Id"    // or equipmentSubnetId
      prefixClosed = "${addrType}Closed" // equipmentSubnetClosed
    }

    def addressId   = order."${prefix}${prefixId}"
    def addressCode = order."${prefix}${prefixCode}"
    Boolean result = hydra.closeEntityAddress(
      entityId     : entityId,
      entityType   : entityType,
      addressId    : addressId,
      bindAddrType : "BIND_ADDR_TYPE_${bindAddrType}",
      addrType     : "ADDR_TYPE_${addrType ?: 'FactPlace'}",
      code         : addressCode,
      isMain       : isMain,
      endDate      : endDate
    )

    order."${prefix}${prefixClosed}" = result
    return result
  }

  Boolean closeEquipmentAddress(Map input = [:]) {
    Map params = [
      addrType        : '',
      bindAddrType    : 'Serv',
      isMain          : true,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    return closeAddress(params + [
      entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}"
    ])
  }

  Boolean closeEquipmentMAC(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    return closeEquipmentAddress(params + [
      addrType : 'MAC',
    ])
  }

  Boolean closeEquipmentIP(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    return closeEquipmentAddress(params + [
      addrType : 'IP',
    ])
  }

  Boolean closeEquipmentSubnet6(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    return closeEquipmentAddress(params + [
      addrType : 'Subnet6',
    ])
  }

  Boolean closeEquipmentVLAN(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    return closeEquipmentAddress(params + [
      addrType : 'VLAN'
    ])
  }

  Boolean closeEquipmentSubnet(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    return closeEquipmentAddress(params + [
      addrType : 'Subnet'
    ])
  }

  Boolean closeEquipmentTelephone(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    return closeEquipmentAddress(params + [
      addrType : 'Telephone'
    ])
  }

  Boolean deleteAddress(Map input = [:]) {
    Map params = [
      addrType      : '',
      bindAddrType  : 'Actual',
      isMain        : true,
      entityPrefix  : '',
      prefix        : ''
    ] + input

    def addrType      = params.addrType
    def bindAddrType  = params.bindAddrType
    def isMain        = params.isMain
    def entityPrefix  = capitalize(params.entityPrefix)
    def prefix        = "${entityPrefix}${capitalize(params.prefix)}"

    String entityId   = order."${entityPrefix}Id"
    String entityType = order."${entityPrefix}Type"

    String prefixId      = ''
    String prefixCode    = ''
    String prefixDeleted = ''
    if (isEmpty(addrType)) {
      prefixId      = "${bindAddrType}AddressId"
      prefixDeleted = "${bindAddrType}AddressDeleted"
    } else {
      prefixCode    = addrType
      prefixId      = "${addrType}Id"
      prefixDeleted = "${addrType}Deleted"
    }

    def addressId   = order."${prefix}${prefixId}"
    def addressCode = order."${prefix}${prefixCode}"
    Boolean result = hydra.deleteEntityAddress(
      entityId     : entityId,
      entityType   : entityType,
      addressId    : addressId,
      bindAddrType : "BIND_ADDR_TYPE_${bindAddrType}",
      addrType     : "ADDR_TYPE_${addrType ?: 'FactPlace'}",
      code         : addressCode,
      isMain       : isMain
    )

    order."${prefix}${prefixDeleted}" = result
    return result
  }

  Boolean deleteBaseSubjectAddress(Map input = [:]) {
    Map params = [
      addrType     : '',
      bindAddrType  : 'Actual',
      isMain        : true,
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : ''
    ] + input

    return deleteAddress(params + [
      entityPrefix : "${params.subjectPrefix}BaseSubject${params.subjectSuffix}"
    ])
  }

  Boolean deleteBaseSubjectPhone(Map input = [:]) {
    Map params = [
      bindAddrType  : 'Notice',
      isMain        : true,
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : ''
    ] + input

    return deleteBaseSubjectAddress(params + [
      addrType : 'Telephone'
    ])
  }

  Boolean deleteBaseSubjectTelephone(Map input = [:]) {
    deleteBaseSubjectPhone(input)
  }

  Boolean deleteBaseSubjectEmail(Map input = [:]) {
    Map params = [
      bindAddrType  : 'Notice',
      isMain        : true,
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : ''
    ] + input

    return deleteBaseSubjectAddress(params + [
      addrType : 'EMail'
    ])
  }

  Boolean deleteEquipmentAddress(Map input = [:]) {
    Map params = [
      addrType        : '',
      bindAddrType    : 'Serv',
      isMain          : true,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    return deleteAddress(params + [
      entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}"
    ])
  }

  Boolean deleteEquipmentMAC(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    return deleteEquipmentAddress(params + [
      addrType: 'MAC'
    ])
  }

  Boolean deleteEquipmentIP(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    return deleteEquipmentAddress(params + [
      addrType: 'IP'
    ])
  }

  Boolean deleteEquipmentSubnet6(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    return deleteEquipmentAddress(params + [
      addrType: 'Subnet6'
    ])
  }

  Boolean deleteEquipmentVLAN(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    return deleteEquipmentAddress(params + [
      addrType: 'VLAN'
    ])
  }

  Boolean deleteEquipmentSubnet(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    return deleteEquipmentAddress(params + [
      addrType: 'Subnet'
    ])
  }

  Boolean deleteEquipmentTelephone(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    return deleteEquipmentAddress(params + [
      addrType: 'Telephone'
    ])
  }
}