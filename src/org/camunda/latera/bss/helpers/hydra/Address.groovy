package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.StringUtil.capitalize
import static org.camunda.latera.bss.utils.StringUtil.decapitalize
import static org.camunda.latera.bss.utils.StringUtil.isEmpty
import static org.camunda.latera.bss.utils.StringUtil.notEmpty
import static org.camunda.latera.bss.utils.MapUtil.mergeNotNull
import java.util.regex.Pattern

/**
  * Address helper methods collection
  */
trait Address {
  /**
   * Get base subject phone and e-mail by base subject id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubjectId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*EMail}     {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*BaseSubject*Telephone} {@link CharSequence String}</li>
   * </ul>
   * @param prefix        {@link CharSequence String}. Phone and e-mail Address prefix. Optional. Default: empty string
   * @param subjectPrefix {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   * @param withId        {@link Boolean}. Fetch subject address ids or not. Default: false
   */
  void fetchBaseSubjectContacts(Map input = [:]) {
    Map params = [
      subjectPrefix : '',
      prefix        : '',
      withId        : false
    ] + input
    fetchBaseSubjectPhone(params)
    fetchBaseSubjectEmail(params)
  }

  /**
   * Get entity address (street, Telephone, EMail, IP, Subnet, Subnet6, MAC, VLAN) by entity id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*%Entity%*Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <p>
   * {@code if addrType == '' (street address)}:
   * <ul>
   *   <li>other fields - see {@link Region#parseRegion(java.util.Map)}</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Entrance}  {@link CharSequence String}. Entrance number</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Floor}     {@link Integer}. Floor number</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Flat}      {@link CharSequence String}. Flat number</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Address}   {@link CharSequence String}. Calculated address</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%AddressId} {@link CharSequence String}. Entity address id. Set only if withId == true</li>
   * </ul>
   * <p>
   * {@code if addrType != ''}:
   * <ul>
   *   <li>{@code homsOrderData*%Entity%*%AddrType%}   {@link CharSequence String}. Address code</li>
   *   <li>{@code homsOrderData*%Entity%*%AddrType%Id} {@link CharSequence String}. Subject address id. Set only if withId == true</li>
   * </ul>
   * @param prefix        {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param entityPrefix  {@link CharSequence String}. Entity prefix. Optional. Default: empty string
   * @param withId        {@link Boolean}. Fetch entity address ids or not. Default: false
   * @param bindAddrType  {@link CharSequence String}. Entity address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Serv'
   * @param addrType      {@link CharSequence String}. Address type without 'ADDR_TYPE_' part. Optional. Default: empty string (street address)
   * @param beginDate     {@link java.time.Temporal Any date type}. Entity address begin date. Optional. Default: null
   * @param endDate       {@link java.time.Temporal Any date type}. Entity address end date. Optional. Default: null
   * @param operationDate {@link java.time.Temporal Any date type}. Date which should overlap between entity address begin and end dates. Optional. Default: current datetime
   * @param isMain        {@link Boolean}. If true search only main entity addresses, if false search only non-main entity addresses, if null - disable filter. Optional. Default: true
   */
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
    if (isEmpty(entityId)) {
      return
    }

    String entityType = order."${entityPrefix}Type"
    Map address = hydra.getEntityAddressBy(
      entityType    : entityType,
      entityId      : entityId,
      operationDate : params.operationDate,
      beginDate     : params.beginDate,
      endDate       : params.endDate,
      addrType      : "ADDR_TYPE_${params.addrType ?: 'FactPlace'}",
      bindAddrType  : "BIND_ADDR_TYPE_${params.bindAddrType}",
      isMain        : params.isMain
    )

    if (isEmpty(address)) {
      return
    }

    if (isEmpty(params.addrType)) {
      order."${prefix}${params.bindAddrType}RegionId" = address.n_region_id

      if (params.withId) {
        order."${prefix}${params.bindAddrType}AddressId" = address.n_obj_address_id
      }

      hydra.getAddressFieldNames().each{ CharSequence name ->
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

      if (params.withId) {
        order."${prefix}${params.addrType}Id" = address.n_obj_address_id
      }
    }
  }

  /**
   * Get base subject address (street, Telephone, EMail) by his id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>other fields - see {@link Region#parseRegion(java.util.Map)}</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Entrance}  {@link CharSequence String}. Entrance number</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Floor}     {@link Integer}. Floor number</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Flat}      {@link CharSequence String}. Flat number</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Address}   {@link CharSequence String}. Calculated address</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%AddressId} {@link CharSequence String}. Subject address id. Set only if withId == true</li>
   * </ul>
   * @param prefix        {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param subjectPrefix {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   * @param withId        {@link Boolean}. Fetch subject address ids or not. Default: false
   * @param bindAddrType  {@link CharSequence String}. Subject-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param addrType      {@link CharSequence String}. Address type without 'ADDR_TYPE_' part. Optional. Default: empty string (street address)
   * @param isMain        {@link Boolean}. If true search only main subject addresses, if false search only non-main subject addresses, if null - disable filter. Optional. Default: true
   */
  void fetchBaseSubjectAddress(Map input = [:]) {
    Map params = [
      addrType      : '',
      bindAddrType  : 'Actual',
      isMain        : true,
      subjectPrefix : '',
      prefix        : '',
      withId        : false
    ] + input

    fetchAddress(params + [
      entityPrefix : "${params.subjectPrefix}BaseSubject"
    ])
  }

  /**
   * Get base subject phone by his id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*Telephone}   {@link CharSequence String}. Address code</li>
   *   <li>{@code homsOrderData*BaseSubject*TelephoneId} {@link CharSequence String}. Subject address id. Set only if withId == true</li>
   * </ul>
   * @param prefix        {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param subjectPrefix {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   * @param withId        {@link Boolean}. Fetch subject address ids or not. Default: false
   * @param bindAddrType  {@link CharSequence String}. Subject-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Notice'
   * @param isMain        {@link Boolean}. If true search only main subject addresses, if false search only non-main subject addresses, if null - disable filter. Optional. Default: true
   */
  void fetchBaseSubjectPhone(Map input = [:]) {
    Map params = [
      bindAddrType  : 'Notice',
      isMain        : true,
      subjectPrefix : '',
      prefix        : '',
      withId        : false
    ] + input

    fetchBaseSubjectAddress(params + [
      addrType : 'Telephone'
    ])
  }

  /**
   * Get base subject phone by his id and fill up execution variables
   *
   * Alias for {@link #fetchBaseSubjectPhone(java.util.Map)}
   * @see #fetchBaseSubjectPhone(java.util.Map)
   */
  void fetchBaseSubjectTelephone(Map input = [:]) {
    fetchBaseSubjectPhone(input)
  }

  /**
   * Get base subject e-mail by his id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*EMail}   {@link CharSequence String}. Address code</li>
   *   <li>{@code homsOrderData*BaseSubject*EMailId} {@link CharSequence String}. Subject address id. Set only if withId == true</li>
   * </ul>
   * @param prefix        {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param subjectPrefix {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   * @param withId        {@link Boolean}. Fetch subject address ids or not. Default: false
   * @param bindAddrType  {@link CharSequence String}. Subject-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Notice'
   * @param isMain        {@link Boolean}. If true search only main subject addresses, if false search only non-main subject addresses, if null - disable filter. Optional. Default: true
   */
  void fetchBaseSubjectEmail(Map input = [:]) {
    Map params = [
      bindAddrType  : 'Notice',
      isMain        : true,
      subjectPrefix : '',
      prefix        : '',
      withId        : false
    ] + input

    fetchBaseSubjectAddress(params + [
      addrType : 'EMail'
    ])
  }

  /**
   * Get equipment address (street, IP, MAC, VLAN, Subnet, Subnet6, Telephone) by equipment id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>other fields - see {@link Region#parseRegion(java.util.Map)}</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Entrance}  {@link CharSequence String}. Entrance number</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Floor}     {@link Integer}. Floor number</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Flat}      {@link CharSequence String}. Flat number</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Address}   {@link CharSequence String}. Calculated address</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%AddressId} {@link CharSequence String}. Object address id. Set only if withId == true</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param withId          {@link Boolean}. Fetch equipment address ids or not. Default: false
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Serv'
   * @param addrType        {@link CharSequence String}. Address type without 'ADDR_TYPE_' part. Optional. Default: empty string (street address)
   * @param beginDate       {@link java.time.Temporal Any date type}. Object address begin date. Optional. Default: null
   * @param endDate         {@link java.time.Temporal Any date type}. Object address end date. Optional. Default: null
   * @param operationDate   {@link java.time.Temporal Any date type}. Date which should overlap between object address begin and end dates. Optional. Default: current datetime
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses, if null - disable filter. Optional. Default: true
   */
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

  /**
   * Get equipment MAC address by equipment id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**MAC}   {@link CharSequence String}. Address code</li>
   *   <li>{@code homsOrderData*Equipment**MACId} {@link CharSequence String}. Object address id. Set only if withId == true</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param withId          {@link Boolean}. Fetch equipment address ids or not. Default: false
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param beginDate       {@link java.time.Temporal Any date type}. Object address begin date. Optional. Default: null
   * @param endDate         {@link java.time.Temporal Any date type}. Object address end date. Optional. Default: null
   * @param operationDate   {@link java.time.Temporal Any date type}. Date which should overlap between object address begin and end dates. Optional. Default: current datetime
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses, if null - disable filter. Optional. Default: true
   */
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

  /**
   * Get equipment IPv4 address by equipment id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**IP}        {@link CharSequence String}. Address code</li>
   *   <li>{@code homsOrderData*Equipment**IPMask}    {@link CharSequence String}. Parent subnet mask code (in CIDR notation, e.g. '30')</li>
   *   <li>{@code homsOrderData*Equipment**IPGateway} {@link CharSequence String}. Parent subnet gateway code</li>
   *   <li>{@code homsOrderData*Equipment**IPId}      {@link CharSequence String}. Object address id. Set only if withId == true</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param withId          {@link Boolean}. Fetch equipment address ids or not. Default: false
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param beginDate       {@link java.time.Temporal Any date type}. Object address begin date. Optional. Default: null
   * @param endDate         {@link java.time.Temporal Any date type}. Object address end date. Optional. Default: null
   * @param operationDate   {@link java.time.Temporal Any date type}. Date which should overlap between object address begin and end dates. Optional. Default: current datetime
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses, if null - disable filter. Optional. Default: true
   */
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

  /**
   * Get equipment VLAN address by equipment id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**VLAN}   {@link CharSequence String}. Address code</li>
   *   <li>{@code homsOrderData*Equipment**VLANId} {@link CharSequence String}. Object address id. Set only if withId == true</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param withId          {@link Boolean}. Fetch equipment address ids or not. Default: false
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param beginDate       {@link java.time.Temporal Any date type}. Object address begin date. Optional. Default: null
   * @param endDate         {@link java.time.Temporal Any date type}. Object address end date. Optional. Default: null
   * @param operationDate   {@link java.time.Temporal Any date type}. Date which should overlap between object address begin and end dates. Optional. Default: current datetime
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses, if null - disable filter. Optional. Default: true
   */
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

  /**
   * Get equipment IPv4 subnet address by equipment id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Subnet}        {@link CharSequence String}. Address code</li>
   *   <li>{@code homsOrderData*Equipment**SubnetMask}    {@link CharSequence String}. Subnet mask code  (in CIDR notation, e.g. '30')</li>
   *   <li>{@code homsOrderData*Equipment**SubnetGateway} {@link CharSequence String}. Subnet gateway code</li>
   *   <li>{@code homsOrderData*Equipment**SubnetId}      {@link CharSequence String}. Object address id. Set only if withId == true</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param withId          {@link Boolean}. Fetch equipment address ids or not. Default: false
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param beginDate       {@link java.time.Temporal Any date type}. Object address begin date. Optional. Default: null
   * @param endDate         {@link java.time.Temporal Any date type}. Object address end date. Optional. Default: null
   * @param operationDate   {@link java.time.Temporal Any date type}. Date which should overlap between object address begin and end dates. Optional. Default: current datetime
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses, if null - disable filter. Optional. Default: true
   */
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

  /**
   * Get equipment IPv6 subnet address by equipment id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Subnet6}        {@link CharSequence String}. Address code</li>
   *   <li>{@code homsOrderData*Equipment**Subnet6Mask}    {@link CharSequence String}. Subnet mask code (in CIDR notation, e.g. '60')</li>
   *   <li>{@code homsOrderData*Equipment**Subnet6Gateway} {@link CharSequence String}. Subnet gateway code</li>
   *   <li>{@code homsOrderData*Equipment**Subnet6Id}      {@link CharSequence String}. Object address id. Set only if withId == true</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param withId          {@link Boolean}. Fetch equipment address ids or not. Default: false
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param beginDate       {@link java.time.Temporal Any date type}. Object address begin date. Optional. Default: null
   * @param endDate         {@link java.time.Temporal Any date type}. Object address end date. Optional. Default: null
   * @param operationDate   {@link java.time.Temporal Any date type}. Date which should overlap between object address begin and end dates. Optional. Default: current datetime
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses, if null - disable filter. Optional. Default: true
   */
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

  /**
   * Get equipment phone number by equipment id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Telephone}   {@link CharSequence String}. Address code</li>
   *   <li>{@code homsOrderData*Equipment**TelephoneId} {@link CharSequence String}. Object address id. Set only if withId == true</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param withId          {@link Boolean}. Fetch equipment address ids or not. Default: false
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param beginDate       {@link java.time.Temporal Any date type}. Object address begin date. Optional. Default: null
   * @param endDate         {@link java.time.Temporal Any date type}. Object address end date. Optional. Default: null
   * @param operationDate   {@link java.time.Temporal Any date type}. Date which should overlap between object address begin and end dates. Optional. Default: current datetime
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses, if null - disable filter. Optional. Default: true
   */
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

  /**
   * Get free IPv4 address and save it to execution variable
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**IP} {@link CharSequence String}</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param ...             Rest params are mentioned in {@link org.camunda.latera.bss.connectors.hid.hydra.Address#getFreeIPAddress(java.util.Map)}
   * @see org.camunda.latera.bss.connectors.hid.hydra.Address#getFreeIPAddress(java.util.Map)
   */
  void fetchEquipmentFreeIP(Map input = [:]) {
    Map params = [
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

  /**
   * Get free IPv6 subnet and save it to execution variable
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Subnet6} {@link CharSequence String}</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param ...             Rest params are mentioned in {@link org.camunda.latera.bss.connectors.hid.hydra.Address#getFreeIPv6Address(java.util.Map)}
   * @see org.camunda.latera.bss.connectors.hid.hydra.Address#getFreeIPv6Address(java.util.Map)
   */
  void fetchEquipmentFreeSubnet6(Map input = [:]) {
    Map params = [
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

  /**
   * Get free phone number and save it to execution variable
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Telephone} {@link CharSequence String}</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param ...             Rest params are mentioned in {@link org.camunda.latera.bss.connectors.hid.hydra.Address#getFreeTelephoneNumber(java.util.Map)}
   * @see org.camunda.latera.bss.connectors.hid.hydra.Address#getFreeTelephoneNumber(java.util.Map)
   */
  void fetchEquipmentFreePhone(Map input = [:]) {
    Map params = [
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

  /**
   * Get free phone number and save it to execution variable
   *
   * Alias for {@link #fetchEquipmentFreePhone(java.util.Map)}
   * @see #fetchEquipmentFreePhone(java.util.Map)
   */
  Boolean fetchEquipmentFreeTelephone(Map input = [:]) {
    return fetchEquipmentFreePhone(input)
  }

  /**
   * Get free IPv4 subnet and save it to execution variable
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Subnet} {@link CharSequence String}</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param ...             Rest params are mentioned in {@link org.camunda.latera.bss.connectors.hid.hydra.Address#getFreeSubnetAddress(java.util.Map)}
   * @see org.camunda.latera.bss.connectors.hid.hydra.Address#getFreeSubnetAddress(java.util.Map)
   */
  void fetchEquipmentFreeSubnet(Map input = [:]) {
    Map params = [
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

  /**
   * Convert execution variables with entity address to Map representation
   * <p>
   * For internal usage only
   * <p>
   * Input execution variables:
   * <p>
   * {@code if addrType == '' (street address)}:
   * <ul>
   *   <li>other fields - see {@link Region#parseRegion(java.util.Map)}</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Entrance}  {@link CharSequence String}. Entrance number</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Floor}     {@link Integer}. Floor number</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Flat}      {@link CharSequence String}. Flat number</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Address}   {@link CharSequence String}. Calculated address</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%AddressId} {@link CharSequence String}. Entity address id. Set only if withId == true</li>
   * </ul>
   * <p>
   * {@code if addrType != ''}:
   * <ul>
   *   <li>{@code homsOrderData*%Entity%*%AddrType%}  {@link CharSequence String}. Address code</li>
   * </ul>
   * @param prefix       {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param entityPrefix {@link CharSequence String}. Entity prefix. Optional. Default: empty string
   * @param bindAddrType {@link CharSequence String}. Entity-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param addrType     {@link CharSequence String}. Address type without 'ADDR_TYPE_' part. Optional. Default: empty string (street address)
   * @return {@link Region#parseRegion(java.util.Map)} + {@code
   *  [
   *    entrance: '345',
   *    floor: '456',
   *    flat: '567'
   *  ]
   * } {@code if addrType == '' (street address)},
   * <p>
   * {@code
   *  [
   *    code: '127.0.0.1'
   *  ]
   * } otherwise
   */
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
      order.data.each { CharSequence name, def value ->
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

  /**
   * Convert execution variables with base subject address to Map representation
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>other fields - see {@link Region#parseRegion(java.util.Map)}</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Entrance}  {@link CharSequence String}. Entrance number</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Floor}     {@link Integer}. Floor number</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Flat}      {@link CharSequence String}. Flat number</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Address}   {@link CharSequence String}. Calculated address</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%AddressId} {@link CharSequence String}. Entity address id. Set only if withId == true</li>
   * </ul>
   * @param prefix        {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param subjectPrefix {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   * @param bindAddrType  {@link CharSequence String}. Subject-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param addrType      {@link CharSequence String}. Address type without 'ADDR_TYPE_' part. Optional. Default: empty string (street address)
   * @return {@link Region#parseRegion(java.util.Map)} + {@code
   *  [
   *    entrance: '345',
   *    floor: '456',
   *    flat: '567'
   *  ]
   * } {@code if addrType == '' (street address)},
   * <p>
   * {@code
   *  [
   *    code: '127.0.0.1'
   *  ]
   * } otherwise
   */
  Map parseBaseSubjectAddress(Map input = [:]) {
    Map params = [
      addrType      : '',
      bindAddrType  : 'Actual',
      subjectPrefix : '',
      prefix        : ''
    ] + input

    return parseAddress(params + [
      entityPrefix : "${params.subjectPrefix}BaseSubject"
    ])
  }

  /**
   * Convert execution variables with base subject address to Map representation
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>other fields - see {@link Region#parseRegion(java.util.Map)}</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Entrance}  {@link CharSequence String}. Entrance number</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Floor}     {@link Integer}. Floor number</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Flat}      {@link CharSequence String}. Flat number</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Address}   {@link CharSequence String}. Calculated address</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%AddressId} {@link CharSequence String}. Entity address id. Set only if withId == true</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Serv'
   * @param addrType        {@link CharSequence String}. Address type without 'ADDR_TYPE_' part. Optional. Default: empty string (street address)
   * @return {@link Region#parseRegion(java.util.Map)} + {@code
   *  [
   *    entrance: '345',
   *    floor: '456',
   *    flat: '567'
   *  ]
   * } {@code if addrType == '' (street address)},
   * <p>
   * {@code
   *  [
   *    code: '127.0.0.1'
   *  ]
   * } otherwise
   */
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

  /**
   * Determine if execution variables with entity address are empty
   * <p>
   * For internal usage only
   * <p>
   * Input execution variables:
   * <p>
   * {@code if addrType == '' (street address)}:
   * <ul>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Entrance}  {@link CharSequence String}. Entrance number</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Floor}     {@link Integer}. Floor number</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Flat}      {@link CharSequence String}. Flat number</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Address}   {@link CharSequence String}. Calculated address</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%AddressId} {@link CharSequence String}. Entity address id. Set only if withId == true</li>
   * </ul>
   * <p>
   * {@code if addrType != ''}:
   * <ul>
   *   <li>{@code homsOrderData*%Entity%*%AddrType%}  {@link CharSequence String}. Address code</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <p>
   * {@code if addrType == '' (street address)}:
   * <ul>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%AddressEmpty} {@link Boolean}. Same as return value. Set only {@code if write == true}</li>
   * </ul>
   * <p>
   * {@code if addrType != ''}:
   * <ul>
   *   <li>{@code homsOrderData*%Entity%*%AddrType%Empty} {@link Boolean}. Same as return value. Set only {@code if write == true}</li>
   * </ul>
   * @param prefix       {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param entityPrefix {@link CharSequence String}. Entity prefix. Optional. Default: empty string
   * @param bindAddrType {@link CharSequence String}. Entity-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param addrType     {@link CharSequence String}. Address type without 'ADDR_TYPE_' part. Optional. Default: empty string (street address)
   * @param write        {@link CharSequence String}. Save result to execution variable or not. Optional. Default: true
   * @return True if entity address is empty, false otherwise
   */
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

  /**
   * Determine if execution variables with entity address are not empty
   *
   * @return True if entity address is not empty, false otherwise
   * @see #isAddressEmpty(java.util.Map)
   */
  Boolean notAddressEmpty(Map input = [:]) {
    return !isAddressEmpty(input)
  }

  /**
   * Determine if execution variables with base subject address are empty
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Entrance}  {@link CharSequence String}. Entrance number</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Floor}     {@link Integer}. Floor number</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Flat}      {@link CharSequence String}. Flat number</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Address}   {@link CharSequence String}. Calculated address</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%AddressId} {@link CharSequence String}. Entity address id. Set only if withId == true</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%AddressEmpty} {@link Boolean}. Same as return value. Set only {@code if write == true}</li>
   * </ul>
   * @param prefix        {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param subjectPrefix {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   * @param bindAddrType  {@link CharSequence String}. Subject-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param addrType      {@link CharSequence String}. Address type without 'ADDR_TYPE_' part. Optional. Default: empty string (street address)
   * @param write         {@link CharSequence String}. Save result to execution variable or not. Optional. Default: true
   * @return True if base subject address is empty, false otherwise
   */
  Boolean isBaseSubjectAddressEmpty(Map input = [:]) {
    Map params = [
      addrType      : '',
      bindAddrType  : 'Actual',
      subjectPrefix : '',
      prefix        : '',
      write         : true
    ] + input

    return isAddressEmpty(params + [
      entityPrefix : "${params.subjectPrefix}BaseSubject"
    ])
  }

  /**
   * Determine if execution variables with base subject address are empty
   *
   * @return True if base subject address is not empty, false otherwise
   * @see #isBaseSubjectAddressEmpty(java.util.Map)
   */
  Boolean notBaseSubjectAddressEmpty(Map input = [:]) {
    return !isBaseSubjectAddressEmpty(input)
  }

  /**
   * Determine if execution variable with base subject phone number is empty
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*Telephone}  {@link CharSequence String}. Address code</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*TelephoneEmpty} {@link Boolean}. Same as return value. Set only {@code if write == true}</li>
   * </ul>
   * @param prefix        {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param subjectPrefix {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   * @param bindAddrType  {@link CharSequence String}. Subject-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Notice'
   * @param write         {@link CharSequence String}. Save result to execution variable or not. Optional. Default: true
   * @return True if base subject phone number is empty, false otherwise
   */
  Boolean isBaseSubjectPhoneEmpty(Map input = [:]) {
    Map params = [
      bindAddrType  : 'Notice',
      subjectPrefix : '',
      prefix        : '',
      write         : true
    ] + input

    return isBaseSubjectAddressEmpty(params + [
      addrType : 'Telephone'
    ])
  }

  /**
   * Determine if execution variables with base subject phone number is not empty
   *
   * @return True if base subject phone number is not empty, false otherwise
   * @see #isBaseSubjectPhoneEmpty(java.util.Map)
   */
  Boolean notBaseSubjectPhoneEmpty(Map input = [:]) {
    return !isBaseSubjectPhoneEmpty(input)
  }

  /**
   * Determine if execution variables with base subject phone number is empty
   *
   * Alias for {@link #isBaseSubjectPhoneEmpty(java.util.Map)}
   * @see #isBaseSubjectPhoneEmpty(java.util.Map)
   */
  Boolean isBaseSubjectTelephoneEmpty(Map input = [:]) {
    return isBaseSubjectPhoneEmpty(input)
  }

  /**
   * Determine if execution variables with base subject phone number is not empty
   *
   * @return True if base subject phone number is not empty, false otherwise
   * @see #isBaseSubjectTelephoneEmpty(java.util.Map)
   */
  Boolean notBaseSubjectTelephoneEmpty(Map input = [:]) {
    return !isBaseSubjectTelephoneEmpty(input)
  }

  /**
   * Determine if execution variable with base subject e-mail is empty
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*EMail}  {@link CharSequence String}. Address code</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*EMailempty} {@link Boolean}. Same as return value. Set only {@code if write == true}</li>
   * </ul>
   * @param prefix        {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param subjectPrefix {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   * @param bindAddrType  {@link CharSequence String}. Subject-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Notice'
   * @param write         {@link CharSequence String}. Save result to execution variable or not. Optional. Default: true
   * @return True if base subject e-mail is empty, false otherwise
   */
  Boolean isBaseSubjectEmailEmpty(Map input = [:]) {
    Map params = [
      bindAddrType  : 'Notice',
      subjectPrefix : '',
      prefix        : '',
      write         : true
    ] + input

    return isBaseSubjectAddressEmpty(params + [
      addrType : 'EMail'
    ])
  }

  /**
   * Determine if execution variables with base subject e-mail is not empty
   *
   * @return True if base subject e-mail is not empty, false otherwise
   * @see #isBaseSubjectEmailEmpty(java.util.Map)
   */
  Boolean notBaseSubjectEmailEmpty(Map input = [:]) {
    return !isBaseSubjectEmailEmpty(input)
  }

  /**
   * Determine if execution variables with equipment address are empty
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Entrance}  {@link CharSequence String}. Entrance number</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Floor}     {@link Integer}. Floor number</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Flat}      {@link CharSequence String}. Flat number</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Address}   {@link CharSequence String}. Calculated address</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%AddressId} {@link CharSequence String}. Entity address id. Set only if withId == true</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <p>
   * <ul>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%AddressEmpty} {@link Boolean}. Same as return value. Set only {@code if write == true}</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Serv'
   * @param addrType        {@link CharSequence String}. Address type without 'ADDR_TYPE_' part. Optional. Default: empty string (street address)
   * @param write           {@link CharSequence String}. Save result to execution variable or not. Optional. Default: true
   * @return True if equipment address is empty, false otherwise
   */
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

  /**
   * Determine if execution variables with equipment address are empty
   *
   * @return True if equipment address is not empty, false otherwise
   * @see #isEquipmentAddressEmpty(java.util.Map)
   */
  Boolean notEquipmentAddressEmpty(Map input = [:]) {
    return !isEquipmentAddressEmpty(input)
  }

  /**
   * Determine if execution variable with equipment MAC address is empty
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**MAC}  {@link CharSequence String}. Address code</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**MACEmpty} {@link Boolean}. Same as return value. Set only {@code if write == true}</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param write           {@link CharSequence String}. Save result to execution variable or not. Optional. Default: true
   * @return True if equipment MAC address is empty, false otherwise
   */
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

  /**
   * Determine if execution variable with equipment MAC address is not empty
   *
   * @return True if equipment MAC address is not empty, false otherwise
   * @see #isEquipmentMACEmpty(java.util.Map)
   */
  Boolean notEquipmentMACEmpty(Map input = [:]) {
    return !isEquipmentMACEmpty(input)
  }

  /**
   * Determine if execution variable with equipment IP address is empty
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**IP}  {@link CharSequence String}. Address code</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**IPEmpty} {@link Boolean}. Same as return value. Set only {@code if write == true}</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param write           {@link CharSequence String}. Save result to execution variable or not. Optional. Default: true
   * @return True if equipment IP address is empty, false otherwise
   */
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

  /**
   * Determine if execution variable with equipment IP address is not empty
   *
   * @return True if equipment IP address is not empty, false otherwise
   * @see #isEquipmentIPEmpty(java.util.Map)
   */
  Boolean notEquipmentIPEmpty(Map input = [:]) {
    return !isEquipmentIPEmpty(input)
  }

  /**
   * Determine if execution variable with equipment IPv6 subnet is empty
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Subnet6}  {@link CharSequence String}. Address code</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Subnet6Empty} {@link Boolean}. Same as return value. Set only {@code if write == true}</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param write           {@link CharSequence String}. Save result to execution variable or not. Optional. Default: true
   * @return True if equipment IPv6 subnet is empty, false otherwise
   */
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

  /**
   * Determine if execution variable with equipment IPv6 subnet is not empty
   *
   * @return True if equipment IPv6 subnet is not empty, false otherwise
   * @see #isEquipmentSubnet6Empty(java.util.Map)
   */
  Boolean notEquipmentSubnet6Empty(Map input = [:]) {
    return !isEquipmentSubnet6Empty(input)
  }

  /**
   * Determine if execution variable with equipment VLAN address is empty
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**VLAN}  {@link CharSequence String}. Address code</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**VLANEmpty} {@link Boolean}. Same as return value. Set only {@code if write == true}</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param write           {@link CharSequence String}. Save result to execution variable or not. Optional. Default: true
   * @return True if equipment VLAN address is empty, false otherwise
   */
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

  /**
   * Determine if execution variable with equipment VLAN address is not empty
   *
   * @return True if equipment VLAN address is not empty, false otherwise
   * @see #isEquipmentVLANEmpty(java.util.Map)
   */
  Boolean notEquipmentVLANEmpty(Map input = [:]) {
    return !isEquipmentVLANEmpty(input)
  }

  /**
   * Determine if execution variable with equipment IPv4 subnet is empty
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Subnet}  {@link CharSequence String}. Address code</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**SubnetEmpty} {@link Boolean}. Same as return value. Set only {@code if write == true}</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param write           {@link CharSequence String}. Save result to execution variable or not. Optional. Default: true
   * @return True if equipment IPv4 subnet is empty, false otherwise
   */
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

  /**
   * Determine if execution variable with equipment IPv4 subnet is not empty
   *
   * @return True if equipment IPv4 subnet is not empty, false otherwise
   * @see #isEquipmentSubnetEmpty(java.util.Map)
   */
  Boolean notEquipmentSubnetEmpty(Map input = [:]) {
    return !isEquipmentSubnetEmpty(input)
  }

  /**
   * Determine if execution variable with equipment phone number is empty
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Telephone}  {@link CharSequence String}. Address code</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**TelephoneEmpty} {@link Boolean}. Same as return value. Set only {@code if write == true}</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param write           {@link CharSequence String}. Save result to execution variable or not. Optional. Default: true
   * @return True if equipment phone number is empty, false otherwise
   */
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

  /**
   * Determine if execution variable with equipment phone number is not empty
   *
   * @return True if equipment phone number is not empty, false otherwise
   * @see #isEquipmentPhoneEmpty(java.util.Map)
   */
  Boolean notEquipmentPhoneEmpty(Map input = [:]) {
    return !isEquipmentPhoneEmpty(input)
  }

  /**
   * Determine if execution variable with equipment phone number is empty
   *
   * Alias for {@link #isBaseSubjectPhoneEmpty(java.util.Map)}
   * @see #isEquipmentPhoneEmpty(java.util.Map)
   */
  Boolean isEquipmentTelephoneEmpty(Map input = [:]) {
    return isEquipmentPhoneEmpty(input)
  }

  /**
   * Determine if execution variable with equipment phone number is not empty
   *
   * @return True if equipment phone number is not empty, false otherwise
   * @see #isEquipmentTelephoneEmpty(java.util.Map)
   */
  Boolean notEquipmentTelephoneEmpty(Map input = [:]) {
    return !isEquipmentTelephoneEmpty(input)
  }

  /**
   * Calculate full entity address from its parts from execution variables
   * <p>
   * For internal usage only
   * <p>
   * Input execution variables:
   * <p>
   * {@code if addrType == '' (street address)}:
   * <ul>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%RegionId}       {@link java.math.BigInteger BigInteger}. Address region id</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%State}          {@link CharSequence String}. Parent region 'code' field with level 'state'</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%StateType}      {@link CharSequence String}. Parent region type code with level 'state'</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Oblast}         {@link CharSequence String}. Parent region 'code' field with level 'oblast'</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%OblastType}     {@link CharSequence String}. Parent region type code with level 'oblast'</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%District}       {@link CharSequence String}. Parent region 'code' field with level 'district'</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%DistrictType}   {@link CharSequence String}. Parent region type code with level 'district'</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%City}           {@link CharSequence String}. Parent region 'code' field with level 'city'</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%CityType}       {@link CharSequence String}. Parent region type code with level 'city'</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Building}       {@link CharSequence String}. Parent region 'code' field with level 'building'</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%BuildingType}   {@link CharSequence String}. Parent region type code with level 'building'</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Home}           {@link CharSequence String}. Parent region 'home' with level 'building'</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Corpus}         {@link CharSequence String}. Parent region 'corpus' with level 'building'</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Construct}      {@link CharSequence String}. Parent region 'construct' with level 'building'</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Ownership}      {@link CharSequence String}. Parent region 'ownership' with level 'building'</li>
   *   <li>other region hierarchy levels - see {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionHierarchy()}</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Entrance}       {@link CharSequence String}. Entrance number</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Floor}          {@link Integer}. Floor number</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Flat}           {@link CharSequence String}. Flat number</li>
   * </ul>
   * <p>
   * {@code if addrType != ''}:
   * <ul>
   *   <li>{@code homsOrderData*%Entity%*%AddrType%} {@link CharSequence String}. Address code</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <p>
   * {@code if addrType == '' (street address)}:
   * <ul>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Address} {@link CharSequence String}. Same as result value</li>
   * </ul>
   * <p>
   * @param prefix       {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param entityPrefix {@link CharSequence String}. Entity prefix. Optional. Default: empty string
   * @param bindAddrType {@link CharSequence String}. Entity-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param addrType     {@link CharSequence String}. Address type without 'ADDR_TYPE_' part. Optional. Default: empty string (street address)
   * @return String with full address, e.g. {@code 'Россия, г. Москва, ул. Заводская, зд. 1, д. 2, кв. 5'} {@code if addrType == '' (street address)}, otherwise {@code '127.0.0.1'}
   * @see org.camunda.latera.bss.connectors.hid.hydra.Address#calcAddress(java.util.Map)
   */
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
      new_address.each { CharSequence key, def value ->
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

  /**
   * Calculate full base subject address from its parts from execution variables
   * <p>
   * Input execution variables:
   * <ul>
  *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%RegionId}       {@link java.math.BigInteger BigInteger}. Address region id</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%State}          {@link CharSequence String}. Parent region 'code' field with level 'state'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%StateType}      {@link CharSequence String}. Parent region type code with level 'state'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Oblast}         {@link CharSequence String}. Parent region 'code' field with level 'oblast'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%OblastType}     {@link CharSequence String}. Parent region type code with level 'oblast'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%District}       {@link CharSequence String}. Parent region 'code' field with level 'district'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%DistrictType}   {@link CharSequence String}. Parent region type code with level 'district'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%City}           {@link CharSequence String}. Parent region 'code' field with level 'city'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%CityType}       {@link CharSequence String}. Parent region type code with level 'city'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Building}       {@link CharSequence String}. Parent region 'code' field with level 'building'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%BuildingType}   {@link CharSequence String}. Parent region type code with level 'building'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Home}           {@link CharSequence String}. Parent region 'home' with level 'building'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Corpus}         {@link CharSequence String}. Parent region 'corpus' with level 'building'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Construct}      {@link CharSequence String}. Parent region 'construct' with level 'building'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Ownership}      {@link CharSequence String}. Parent region 'ownership' with level 'building'</li>
   *   <li>other region hierarchy levels - see {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionHierarchy()}</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Entrance}       {@link CharSequence String}. Entrance number</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Floor}          {@link Integer}. Floor number</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Flat}           {@link CharSequence String}. Flat number</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Address} {@link CharSequence String}. Same as result value</li>
   * </ul>
   * <p>
   * @param prefix        {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param subjectPrefix {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   * @param bindAddrType  {@link CharSequence String}. Subject-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param addrType      {@link CharSequence String}. Address type without 'ADDR_TYPE_' part. Optional. Default: empty string (street address)
   * @return String with full address, e.g. {@code 'Россия, г. Москва, ул. Заводская, зд. 1, д. 2, кв. 5'}
   * @see org.camunda.latera.bss.connectors.hid.hydra.Address#calcAddress(java.util.Map)
   */
  String calcBaseSubjectAddress(Map input = [:]) {
    Map params = [
      addrType      : '',
      bindAddrType  : 'Actual',
      subjectPrefix : '',
      prefix        : '',
      write         : true
    ] + input

    return calcAddress(params + [
      entityPrefix : "${params.subjectPrefix}BaseSubject"
    ])
  }

  /**
   * Calculate full equipment address from its parts from execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%RegionId}       {@link java.math.BigInteger BigInteger}. Address region id</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%State}          {@link CharSequence String}. Parent region 'code' field with level 'state'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%StateType}      {@link CharSequence String}. Parent region type code with level 'state'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Oblast}         {@link CharSequence String}. Parent region 'code' field with level 'oblast'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%OblastType}     {@link CharSequence String}. Parent region type code with level 'oblast'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%District}       {@link CharSequence String}. Parent region 'code' field with level 'district'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%DistrictType}   {@link CharSequence String}. Parent region type code with level 'district'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%City}           {@link CharSequence String}. Parent region 'code' field with level 'city'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%CityType}       {@link CharSequence String}. Parent region type code with level 'city'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Building}       {@link CharSequence String}. Parent region 'code' field with level 'building'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%BuildingType}   {@link CharSequence String}. Parent region type code with level 'building'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Home}           {@link CharSequence String}. Parent region 'home' with level 'building'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Corpus}         {@link CharSequence String}. Parent region 'corpus' with level 'building'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Construct}      {@link CharSequence String}. Parent region 'construct' with level 'building'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Ownership}      {@link CharSequence String}. Parent region 'ownership' with level 'building'</li>
   *   <li>other region hierarchy levels - see {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionHierarchy()}</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Entrance}       {@link CharSequence String}. Entrance number</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Floor}          {@link Integer}. Floor number</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Flat}           {@link CharSequence String}. Flat number</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Address}        {@link CharSequence String}. Calculated address</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%AddressId}      {@link CharSequence String}. Entity address id. Set only if withId == true</li>
    * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*%BindAddrType%Address} {@link CharSequence String}. Same as result value</li>
   * </ul>
   * <p>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Entity-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Serv'
   * @param addrType        {@link CharSequence String}. Address type without 'ADDR_TYPE_' part. Optional. Default: empty string (street address)
   * @return String with full address, e.g. {@code 'Россия, г. Москва, ул. Заводская, зд. 1, д. 2, кв. 5'}
   * @see org.camunda.latera.bss.connectors.hid.hydra.Address#calcAddress(java.util.Map)
   */
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

  /**
   * Create entity address and fill up execution variables
   * <p>
   * For internal usage only
   * <p>
   * Input execution variables:
   * <p>
   * {@code if addrType == '' (street address)}:
   * <ul>
   *   <li>{@code homsOrderData*%Entity%Id}                              {@link java.math.BigInteger BigInteger}. Entity id</li>
   *   <li>{@code homsOrderData*%Entity%Type}                            {@link java.math.BigInteger BigInteger}. Entity type (ref code). Optional</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%RegionId}         {@link java.math.BigInteger BigInteger}. Address region id. Optional</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%*ParentAddress}   {@link CharSequence String}. Parent address. Optional, used only if 'ParentAddressId' is not set</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%*ParentAddressId} {@link java.math.BigInteger BigInteger}. Parent entity address id. Optional, used only if 'ParentAddress' is not set</li>
   * </ul>
   * <p>
   * {@code if addrType != ''}:
   * <ul>
   *   <li>{@code homsOrderData*%Entity%*%AddrType%}                       {@link CharSequence String}. Address code</li>
   *   <li>{@code homsOrderData*%Entity%*%AddrType%*Parent%ParAddrType%}   {@link CharSequence String}. Parent address (e.g. VLAN address code for IP address, MAC address code for IP address, etc). Optional, used only if '%ParAddrType%Id' is not set</li>
   *   <li>{@code homsOrderData*%Entity%*%AddrType%*Parent%ParAddrType%Id} {@link java.math.BigInteger BigInteger}. Parent entity address id (e.g. VLAN entity address id for IP address, MAC entity address id for IP address, etc). Optional, used only if '%ParAddrType%' is not set</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <p>
   * {@code if addrType == '' (street address)}:
   * <ul>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%AddressId}      {@link java.math.BigInteger BigInteger}. Entity address id</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%AddressCreated} {@link Boolean}. Same as result value</li>
   * </ul>
   * <p>
   * {@code if addrType != ''}:
   * <ul>
   *   <li>{@code homsOrderData*%Entity%*%AddrType%Id} {@link java.math.BigInteger BigInteger}. Entity address id</li>
   *   <li>{@code homsOrderData*%Entity%*%AddrType%Created} {@link Boolean}. Same as result value</li>
   * </ul>
   * @param prefix             {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param entityPrefix       {@link CharSequence String}. Entity prefix. Optional. Default: empty string
   * @param bindAddrType       {@link CharSequence String}. Entity-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param addrType           {@link CharSequence String}. Address type without 'ADDR_TYPE_' part. Optional. Default: empty string (street address)
   * @param beginDate          {@link java.time.Temporal Any date type}. Entity address begin date. Optional. Default: null
   * @param endDate            {@link java.time.Temporal Any date type}. Entity address end date. Optional. Default: null
   * @param isMain             {@link Boolean}. Is entity address main or not. Optional. Default: true
   * @param parentPrefix       {@link CharSequence String}. Parent address prefix. Optional. Default: empty string
   * @param parentBindAddrType {@link CharSequence String}. Entity-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: empty string
   * @param parentAddrType     {@link CharSequence String}. Address type without 'ADDR_TYPE_' part. Optional. Default: empty string (street address)
   * @return True if entity address was created successfully, false otherwise
   */
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
      prefixId = "${params.bindAddrType}Address"
      if (notEmpty(params.bindAddrType)) {
        parentPrefixId = "${params.bindAddrType}${parentPrefix}ParentAddress"
      }
    } else {
      inp.code = order."${prefix}${params.addrType}"
      prefixId = "${params.addrType}"
      if (notEmpty(params.parentAddrType)) {
        parentPrefixId = "${params.addrType}${parentPrefix}Parent${params.parentAddrType}"
      }
    }

    // e.g. equipmentIP is set and equipmentIPParentVLAN is filled up with VLAN, so created IP address will bound to VLAN
    def parAddress   = order."${prefix}${parentPrefixId}"
    def parAddressId = order."${prefix}${parentPrefixId}Id"
    if (notEmpty(parAddressId) || notEmpty(parAddress)) {
      Map parAddressParams = [
        entityId       : entityId,
        entityType     : entityType,
        addrTypeId     : null,
        addrType       : notEmpty(params.parentAddrType)     ? "ADDR_TYPE_${params.parentAddrType}"          : null,
        bindAddrTypeId : null,
        bindAddrType   : notEmpty(params.parentBindAddrType) ? "BIND_ADDR_TYPE_${params.parentBindAddrType}" : null
      ]
      if (notEmpty(parAddressId)) {
        parAddressParams.entityAddressId = parAddressId
      } else if (notEmpty(parAddress)) {
        parAddressParams.code = parAddress
      }
      inp.parEntityAddressId = hydra.getEntityAddressBy(parAddressParams)?.n_obj_address_id
    }

    Boolean result = false
    Map address = hydra.createEntityAddress(inp, entityId)
    if (address) {
      order."${prefix}${prefixId}Id" = address.num_N_ENTITY_ADDRESS_ID
      result = true
    }
    order."${prefix}${prefixId}Created" = result
    return result
  }

  /**
   * Create base subject address and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubjectId}                          {@link java.math.BigInteger BigInteger}. Base subject id</li>
   *   <li>{@code homsOrderData*BaseSubjectType}                        {@link java.math.BigInteger BigInteger}. Base subject type (ref code). Optional</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%RegionId}     {@link java.math.BigInteger BigInteger}. Address region id. Optional</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Entrance}     {@link CharSequence String}. Entrance number</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Floor}        {@link Integer}. Floor number</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Flat}         {@link CharSequence String}. Flat number</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%AddressId} {@link java.math.BigInteger BigInteger}. Subject address id</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%AddressCreated} {@link Boolean}. Same as result value</li>
   * </ul>
   * <p>
   * @param prefix             {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param subjectPrefix      {@link CharSequence String}. Subject prefix. Optional. Default: empty string
   * @param bindAddrType       {@link CharSequence String}. Subject-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param isMain             {@link Boolean}. Is subject address main or not. Optional. Default: true
   * @return True if base subject address was created successfully, false otherwise
   */
  Boolean createBaseSubjectAddress(Map input = [:]) {
    Map params = [
      addrType      : '',
      bindAddrType  : 'Actual',
      isMain        : true,
      subjectPrefix : '',
      prefix        : ''
    ] + input

    return createAddress(params + [
      entityPrefix : "${params.subjectPrefix}BaseSubject"
    ])
  }

  /**
   * Create base subject phone number and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*Id}        {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*BaseSubject*Telephone} {@link CharSequence String}. Address code</li>
   * </ul>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*TelephoneId}      {@link CharSequence String}. Subject address id</li>
   *   <li>{@code homsOrderData*BaseSubject*TelephoneCreated} {@link CharSequence String}. Same as return value</li>
   * </ul>
   * @param prefix        {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param subjectPrefix {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   * @param bindAddrType  {@link CharSequence String}. Subject-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Notice'
   * @param isMain        {@link Boolean}. Is subject address main or not. Optional. Default: true
   * @return True if subject address was saved successfully, false otherwise
   */
  Boolean createBaseSubjectPhone(Map input = [:]) {
    Map params = [
      bindAddrType  : 'Notice',
      isMain        : true,
      subjectPrefix : '',
      prefix        : ''
    ] + input

    return createBaseSubjectAddress(params + [
      addrType : 'Telephone'
    ])
  }

  /**
   * Create base subject phone number and fill up execution variables
   *
   * Alias for {@link #createBaseSubjectPhone(java.util.Map)}
   * @see #createBaseSubjectPhone(java.util.Map)
   */
  Boolean createBaseSubjectTelephone(Map input = [:]) {
    return createBaseSubjectPhone(input)
  }

  /**
   * Create base subject phone number and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*Id}    {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*BaseSubject*EMail} {@link CharSequence String}. Address code</li>
   * </ul>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*EMailId}      {@link CharSequence String}. Subject address id</li>
   *   <li>{@code homsOrderData*BaseSubject*EMailCreated} {@link CharSequence String}. Same as return value</li>
   * </ul>
   * @param prefix        {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param subjectPrefix {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   * @param bindAddrType  {@link CharSequence String}. Subject-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Notice'
   * @param isMain        {@link Boolean}. Is subject address main or not. Optional. Default: true
   * @return True if subject address was saved successfully, false otherwise
   */
  Boolean createBaseSubjectEmail(Map input = [:]) {
    Map params = [
      bindAddrType  : 'Notice',
      isMain        : true,
      subjectPrefix : '',
      prefix        : ''
    ] + input

    return createBaseSubjectAddress(params + [
      addrType : 'EMail'
    ])
  }

  /**
   * Create object address and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id}                              {@link java.math.BigInteger BigInteger}. Equipment id</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%RegionId}         {@link java.math.BigInteger BigInteger}. Address region id</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Entrance}         {@link CharSequence String}. Entrance number</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Floor}            {@link Integer}. Floor number</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Flat}             {@link CharSequence String}. Flat number</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%*ParentAddress}   {@link CharSequence String}. Parent address. Optional, used only if 'ParentAddressId' is not set</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%*ParentAddressId} {@link java.math.BigInteger BigInteger}. Parent object address id. Optional, used only if 'ParentAddress' is not set</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <p>
   * <ul>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%AddressId} {@link java.math.BigInteger BigInteger}. Object address id</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%AddressCreated} {@link Boolean}. Same as result value</li>
   * </ul>
   * @param prefix             {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix    {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix    {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType       {@link CharSequence String}. Equipment-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Serv'
   * @param addrType           {@link CharSequence String}. Address type without 'ADDR_TYPE_' part. Optional. Default: empty string (street address)
   * @param beginDate          {@link java.time.Temporal Any date type}. Equipment address begin date. Optional. Default: null
   * @param endDate            {@link java.time.Temporal Any date type}. Equipment address end date. Optional. Default: null
   * @param isMain             {@link Boolean}. Is object address main or not. Optional. Default: true
   * @param parentPrefix       {@link CharSequence String}. Parent address prefix. Optional. Default: empty string
   * @param parentBindAddrType {@link CharSequence String}. Equipment-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: empty string
   * @param parentAddrType     {@link CharSequence String}. Address type without 'ADDR_TYPE_' part. Optional. Default: empty string (street address)
   * @return True if equipment address was created successfully, false otherwise
   */
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

  /**
   * Create equipment MAC address and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id}                        {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**MAC}                       {@link CharSequence String}. Address code</li>
   *   <li>{@code homsOrderData*Equipment**MAC*Parent%ParAddrType%}   {@link CharSequence String}. Parent address (e.g. VLAN address code for IP address, MAC address code for IP address, etc). Optional, used only if '%ParAddrType%Id' is not set</li>
   *   <li>{@code homsOrderData*Equipment**MAC*Parent%ParAddrType%Id} {@link java.math.BigInteger BigInteger}. Parent object address id (e.g. VLAN entity address id for IP address, MAC entity address id for IP address, etc). Optional, used only if '%ParAddrType%' is not set</li>
   * </ul>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**MACId}      {@link CharSequence String}. Object address id</li>
   *   <li>{@code homsOrderData*Equipment**MACCreated} {@link CharSequence String}. Same as return value</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param beginDate       {@link java.time.Temporal Any date type}. Object address begin date. Optional. Default: null
   * @param endDate         {@link java.time.Temporal Any date type}. Object address end date. Optional. Default: null
   * @param isMain          {@link Boolean}. Is object address main or not. Optional. Default: true
   * @return True if object address was saved successfully, false otherwise
   */
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

  /**
   * Create equipment IPv4 address and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id}                       {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**IP}                       {@link CharSequence String}. Address code</li>
   *   <li>{@code homsOrderData*Equipment**IP*Parent%ParAddrType%}   {@link CharSequence String}. Parent address (e.g. VLAN address code for IP address, MAC address code for IP address, etc). Optional, used only if '%ParAddrType%Id' is not set</li>
   *   <li>{@code homsOrderData*Equipment**IP*Parent%ParAddrType%Id} {@link java.math.BigInteger BigInteger}. Parent object address id (e.g. VLAN entity address id for IP address, MAC entity address id for IP address, etc). Optional, used only if '%ParAddrType%' is not set</li>
   * </ul>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**IPId}      {@link CharSequence String}. Object address id</li>
   *   <li>{@code homsOrderData*Equipment**IPCreated} {@link CharSequence String}. Same as return value</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param beginDate       {@link java.time.Temporal Any date type}. Object address begin date. Optional. Default: null
   * @param endDate         {@link java.time.Temporal Any date type}. Object address end date. Optional. Default: null
   * @param isMain          {@link Boolean}. Is object address main or not. Optional. Default: true
   * @return True if object address was saved successfully, false otherwise
   */
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

  /**
   * Create equipment IPv6 subnet and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id}                            {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**Subnet6}                       {@link CharSequence String}. Address code</li>
   *   <li>{@code homsOrderData*Equipment**Subnet6*Parent%ParAddrType%}   {@link CharSequence String}. Parent address (e.g. VLAN address code for IP address, MAC address code for IP address, etc). Optional, used only if '%ParAddrType%Id' is not set</li>
   *   <li>{@code homsOrderData*Equipment**Subnet6*Parent%ParAddrType%Id} {@link java.math.BigInteger BigInteger}. Parent object address id (e.g. VLAN entity address id for IP address, MAC entity address id for IP address, etc). Optional, used only if '%ParAddrType%' is not set</li>
   * </ul>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Subnet6Id}      {@link CharSequence String}. Object address id</li>
   *   <li>{@code homsOrderData*Equipment**Subnet6Created} {@link CharSequence String}. Same as return value</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param beginDate       {@link java.time.Temporal Any date type}. Object address begin date. Optional. Default: null
   * @param endDate         {@link java.time.Temporal Any date type}. Object address end date. Optional. Default: null
   * @param isMain          {@link Boolean}. Is object address main or not. Optional. Default: true
   * @return True if object address was saved successfully, false otherwise
   */
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

  /**
   * Create equipment VLAN address and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id}                         {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**VLAN}                       {@link CharSequence String}. Address code</li>
   *   <li>{@code homsOrderData*Equipment**VLAN*Parent%ParAddrType%}   {@link CharSequence String}. Parent address (e.g. VLAN address code for IP address, MAC address code for IP address, etc). Optional, used only if '%ParAddrType%Id' is not set</li>
   *   <li>{@code homsOrderData*Equipment**VLAN*Parent%ParAddrType%Id} {@link java.math.BigInteger BigInteger}. Parent object address id (e.g. VLAN entity address id for IP address, MAC entity address id for IP address, etc). Optional, used only if '%ParAddrType%' is not set</li>
   * </ul>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**VLANId}      {@link CharSequence String}. Object address id</li>
   *   <li>{@code homsOrderData*Equipment**VLANCreated} {@link CharSequence String}. Same as return value</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param beginDate       {@link java.time.Temporal Any date type}. Object address begin date. Optional. Default: null
   * @param endDate         {@link java.time.Temporal Any date type}. Object address end date. Optional. Default: null
   * @param isMain          {@link Boolean}. Is object address main or not. Optional. Default: true
   * @return True if object address was saved successfully, false otherwise
   */
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

  /**
   * Create equipment IPv4 subnet and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id}                           {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**Subnet}                       {@link CharSequence String}. Address code</li>
   *   <li>{@code homsOrderData*Equipment**Subnet*Parent%ParAddrType%}   {@link CharSequence String}. Parent address (e.g. VLAN address code for IP address, MAC address code for IP address, etc). Optional, used only if '%ParAddrType%Id' is not set</li>
   *   <li>{@code homsOrderData*Equipment**Subnet*Parent%ParAddrType%Id} {@link java.math.BigInteger BigInteger}. Parent object address id (e.g. VLAN entity address id for IP address, MAC entity address id for IP address, etc). Optional, used only if '%ParAddrType%' is not set</li>
   * </ul>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**SubnetId}      {@link CharSequence String}. Object address id</li>
   *   <li>{@code homsOrderData*Equipment**SubnetCreated} {@link CharSequence String}. Same as return value</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param beginDate       {@link java.time.Temporal Any date type}. Object address begin date. Optional. Default: null
   * @param endDate         {@link java.time.Temporal Any date type}. Object address end date. Optional. Default: null
   * @param isMain          {@link Boolean}. Is object address main or not. Optional. Default: true
   * @return True if object address was saved successfully, false otherwise
   */
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

  /**
   * Create equipment phone number and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id}                              {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**Telephone}                       {@link CharSequence String}. Address code</li>
   *   <li>{@code homsOrderData*Equipment**Telephone*Parent%ParAddrType%}   {@link CharSequence String}. Parent address (e.g. VLAN address code for IP address, MAC address code for IP address, etc). Optional, used only if '%ParAddrType%Id' is not set</li>
   *   <li>{@code homsOrderData*Equipment**Telephone*Parent%ParAddrType%Id} {@link java.math.BigInteger BigInteger}. Parent object address id (e.g. VLAN entity address id for IP address, MAC entity address id for IP address, etc). Optional, used only if '%ParAddrType%' is not set</li>
   * </ul>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**TelephoneId}      {@link CharSequence String}. Object address id</li>
   *   <li>{@code homsOrderData*Equipment**TelephoneCreated} {@link CharSequence String}. Same as return value</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param beginDate       {@link java.time.Temporal Any date type}. Object address begin date. Optional. Default: null
   * @param endDate         {@link java.time.Temporal Any date type}. Object address end date. Optional. Default: null
   * @param isMain          {@link Boolean}. Is object address main or not. Optional. Default: true
   * @return True if object address was saved successfully, false otherwise
   */
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

  /**
   * Create equipment phone number and fill up execution variables
   *
   * Alias for {@link #createEquipmentPhone(Map)}
   * @see #createEquipmentPhone(java.util.Map)
   */
  Boolean createEquipmentTelephone(Map input = [:]) {
    return createEquipmentPhone(input)
  }

  /**
   * Get free IPv4 address, save it to Hydra and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**IP}        {@link CharSequence String}. Address code</li>
   *   <li>{@code homsOrderData*Equipment**IPId}      {@link java.math.BigInteger BigInteger}. Object address id</li>
   *   <li>{@code homsOrderData*Equipment**IPCreated} {@link Boolean}. Same as return value</li>
   * </ul>
   * @see #fetchEquipmentFreeIP
   * @see #createEquipmentIP
   */
  Boolean assignEquipmentFreeIP(Map input = [:]) {
    Map params = [
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    fetchEquipmentFreeIP(params)
    return createEquipmentIP(params)
  }

  /**
   * Get free IPv6 subnet, assign it to equipment and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Subnet6}        {@link CharSequence String}. Address code</li>
   *   <li>{@code homsOrderData*Equipment**Subnet6Id}      {@link java.math.BigInteger BigInteger}. Object address id</li>
   *   <li>{@code homsOrderData*Equipment**Subnet6Created} {@link Boolean}. Same as return value</li>
   * </ul>
   * @see #fetchEquipmentFreeSubnet6
   * @see #createEquipmentSubnet6
   */
  Boolean assignEquipmentFreeSubnet6(Map input = [:]) {
    fetchEquipmentFreeSubnet6(input)
    return createEquipmentSubnet6(input)
  }

  /**
   * Get free phone number, assign it to equipment and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Telephone}        {@link CharSequence String}. Address code</li>
   *   <li>{@code homsOrderData*Equipment**TelephoneId}      {@link java.math.BigInteger BigInteger}. Object address id</li>
   *   <li>{@code homsOrderData*Equipment**TelephoneCreated} {@link Boolean}. Same as return value</li>
   * </ul>
   * @see #fetchEquipmentFreePhone
   * @see #createEquipmentPhone
   */
  Boolean assignEquipmentFreePhone(Map input = [:]) {
    fetchEquipmentFreePhone(input)
    return createEquipmentPhone(input)
  }

  /**
   * Get free phone number, assign it to equipment and fill up execution variables
   *
   * Alias for {@link #assignEquipmentFreePhone(java.util.Map)}
   * @see #assignEquipmentFreePhone(java.util.Map)
   */
  Boolean assignEquipmentFreeTelephone(Map input = [:]) {
    return assignEquipmentFreePhone(input)
  }

  /**
   * Get free IPv4 subnet, assign it to equipment and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Subnet}        {@link CharSequence String}. Address code</li>
   *   <li>{@code homsOrderData*Equipment**SubnetId}      {@link java.math.BigInteger BigInteger}. Object address id</li>
   *   <li>{@code homsOrderData*Equipment**SubnetCreated} {@link Boolean}. Same as return value</li>
   * </ul>
   * @see #fetchEquipmentFreeSubnet
   * @see #createEquipmentSubnet
   */
  Boolean assignEquipmentFreeSubnet(Map input = [:]) {
    fetchEquipmentFreeSubnet(input)
    return createEquipmentSubnet(input)
  }

  /**
   * Close entity address and fill up execution variables
   * <p>
   * For internal usage only
   * <p>
   * Input execution variables:
   * <p>
   * {@code if addrType == '' (street address)}:
   * <ul>
   *   <li>{@code homsOrderData*%Entity%Id}                       {@link java.math.BigInteger BigInteger}. Entity id</li>
   *   <li>{@code homsOrderData*%Entity%Type}                     {@link java.math.BigInteger BigInteger}. Entity type (ref code). Optional</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%AddressId} {@link java.math.BigInteger BigInteger}. Entity address id</li>
   * </ul>
   * <p>
   * {@code if addrType != ''}:
   * <ul>
   *   <li>{@code homsOrderData*%Entity%Id}            {@link java.math.BigInteger BigInteger}. Entity id</li>
   *   <li>{@code homsOrderData*%Entity%Type}          {@link java.math.BigInteger BigInteger}. Entity type (ref code). Optional</li>
   *   <li>{@code homsOrderData*%Entity%*%AddrType%}   {@link CharSequence String}. Address code. Optional if '%AddrType%Id' is set</li>
   *   <li>{@code homsOrderData*%Entity%*%AddrType%Id} {@link java.math.BigInteger BigInteger}. Entity address id. Optional if '%AddrType%' is set</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <p>
   * {@code if addrType == '' (street address)}:
   * <ul>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%AddressClosed} {@link Boolean}. Same as result value</li>
   * </ul>
   * <p>
   * {@code if addrType != ''}:
   * <ul>
   *   <li>{@code homsOrderData*%Entity%*%AddrType%Closed} {@link Boolean}. Same as result value</li>
   * </ul>
   * @param prefix       {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param entityPrefix {@link CharSequence String}. Entity prefix. Optional. Default: empty string
   * @param bindAddrType {@link CharSequence String}. Entity-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param addrType     {@link CharSequence String}. Address type without 'ADDR_TYPE_' part. Optional. Default: empty string (street address)
   * @param endDate      {@link java.time.Temporal Any date type}. Entity address end date. Optional. Default: current datetime
   * @param isMain       {@link Boolean}. If true search only main entity addresses, if false search only non-main entity addresses if null - disable filter. Optional. Default: true
   * @return True if entity address was closed successfully, false otherwise
   */
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

    def entityAddressId = order."${prefix}${prefixId}"
    def addressCode    = order."${prefix}${prefixCode}"
    Boolean result = hydra.closeEntityAddress(
      entityId        : entityId,
      entityType      : entityType,
      entityAddressId : entityAddressId,
      bindAddrType    : "BIND_ADDR_TYPE_${bindAddrType}",
      addrType        : "ADDR_TYPE_${addrType ?: 'FactPlace'}",
      code            : addressCode,
      isMain          : isMain,
      endDate         : endDate
    )

    order."${prefix}${prefixClosed}" = result
    return result
  }

  /**
   * Close entity address and fill up execution variables
   * <p>
   * Input execution variables:
   * <p>
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id}                       {@link java.math.BigInteger BigInteger}. Equipment id</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%AddressId} {@link java.math.BigInteger BigInteger}. Object address id</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%AddressClosed} {@link Boolean}. Same as result value</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Equipment-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Serv'
   * @param endDate         {@link java.time.Temporal Any date type}. Equipment address end date. Optional. Default: current datetime
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses if null - disable filter. Optional. Default: true
   * @return True if equipment address was closed successfully, false otherwise
   */
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

  /**
   * Close equipment MAC address and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id}     {@link java.math.BigInteger BigInteger}. Equipment id</li>
   *   <li>{@code homsOrderData*Equipment**MAC}   {@link CharSequence String}. Address code. Optional if 'MACId' is set</li>
   *   <li>{@code homsOrderData*Equipment**MACId} {@link java.math.BigInteger BigInteger}. Object address id. Optional if 'MAC' is set</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**MACClosed} {@link Boolean}. Same as result value</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Equipment-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param endDate         {@link java.time.Temporal Any date type}. Equipment address end date. Optional. Default: current datetime
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses if null - disable filter. Optional. Default: true
   * @return True if equipment MACaddress was closed successfully, false otherwise
   */
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

  /**
   * Close equipment IPv4 address and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id}    {@link java.math.BigInteger BigInteger}. Equipment id</li>
   *   <li>{@code homsOrderData*Equipment**IP}   {@link CharSequence String}. Address code. Optional if 'IPId' is set</li>
   *   <li>{@code homsOrderData*Equipment**IPId} {@link java.math.BigInteger BigInteger}. Object address id. Optional if 'IP' is set</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**IPClosed} {@link Boolean}. Same as result value</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Equipment-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param endDate         {@link java.time.Temporal Any date type}. Equipment address end date. Optional. Default: current datetime
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses if null - disable filter. Optional. Default: true
   * @return True if equipment IPv4 address was closed successfully, false otherwise
   */
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

  /**
   * Close equipment IPv6 subnet and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id}         {@link java.math.BigInteger BigInteger}. Equipment id</li>
   *   <li>{@code homsOrderData*Equipment**Subnet6}   {@link CharSequence String}. Address code. Optional if 'Subnet6Id' is set</li>
   *   <li>{@code homsOrderData*Equipment**Subnet6Id} {@link java.math.BigInteger BigInteger}. Object address id. Optional if 'Subnet6' is set</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Subnet6Closed} {@link Boolean}. Same as result value</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Equipment-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param endDate         {@link java.time.Temporal Any date type}. Equipment address end date. Optional. Default: current datetime
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses if null - disable filter. Optional. Default: true
   * @return True if equipment IPv6 subnet was closed successfully, false otherwise
   */
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

  /**
   * Close equipment VLAN address and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id}      {@link java.math.BigInteger BigInteger}. Equipment id</li>
   *   <li>{@code homsOrderData*Equipment**VLAN}   {@link CharSequence String}. Address code. Optional if 'VLANId' is set</li>
   *   <li>{@code homsOrderData*Equipment**VLANId} {@link java.math.BigInteger BigInteger}. Object address id. Optional if 'VLAN' is set</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**VLANClosed} {@link Boolean}. Same as result value</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Equipment-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param endDate         {@link java.time.Temporal Any date type}. Equipment address end date. Optional. Default: current datetime
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses if null - disable filter. Optional. Default: true
   * @return True if equipment VLAN address was closed successfully, false otherwise
   */
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

  /**
   * Close equipment IPv4 subnet and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id}        {@link java.math.BigInteger BigInteger}. Equipment id</li>
   *   <li>{@code homsOrderData*Equipment**Subnet}   {@link CharSequence String}. Address code. Optional if 'SubnetId' is set</li>
   *   <li>{@code homsOrderData*Equipment**SubnetId} {@link java.math.BigInteger BigInteger}. Object address id. Optional if 'Subnet' is set</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**SubnetClosed} {@link Boolean}. Same as result value</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Equipment-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param endDate         {@link java.time.Temporal Any date type}. Equipment address end date. Optional. Default: current datetime
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses if null - disable filter. Optional. Default: true
   * @return True if equipment IPv4 subnet was closed successfully, false otherwise
   */
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

  /**
   * Close equipment phone number and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id}           {@link java.math.BigInteger BigInteger}. Equipment id</li>
   *   <li>{@code homsOrderData*Equipment**Telephone}   {@link CharSequence String}. Address code. Optional if 'TelephoneId' is set</li>
   *   <li>{@code homsOrderData*Equipment**TelephoneId} {@link java.math.BigInteger BigInteger}. Object address id. Optional if 'Telephone' is set</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**TelephoneClosed} {@link Boolean}. Same as result value</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Equipment-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param endDate         {@link java.time.Temporal Any date type}. Equipment address end date. Optional. Default: current datetime
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses if null - disable filter. Optional. Default: true
   * @return True if equipment phone number was closed successfully, false otherwise
   */
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

  /**
   * Delete entity address and fill up execution variables
   * <p>
   * For internal usage only
   * <p>
   * Input execution variables:
   * <p>
   * {@code if addrType == '' (street address)}:
   * <ul>
   *   <li>{@code homsOrderData*%Entity%Id}                       {@link java.math.BigInteger BigInteger}. Entity id</li>
   *   <li>{@code homsOrderData*%Entity%Type}                     {@link java.math.BigInteger BigInteger}. Entity type (ref code). Optional</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%AddressId} {@link java.math.BigInteger BigInteger}. Entity address id</li>
   * </ul>
   * <p>
   * {@code if addrType != ''}:
   * <ul>
   *   <li>{@code homsOrderData*%Entity%Id}            {@link java.math.BigInteger BigInteger}. Entity id</li>
   *   <li>{@code homsOrderData*%Entity%Type}          {@link java.math.BigInteger BigInteger}. Entity type (ref code). Optional</li>
   *   <li>{@code homsOrderData*%Entity%*%AddrType%}   {@link CharSequence String}. Address code. Optional if '%AddrType%Id' is set</li>
   *   <li>{@code homsOrderData*%Entity%*%AddrType%Id} {@link java.math.BigInteger BigInteger}. Entity address id. Optional if '%AddrType%' is set</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <p>
   * {@code if addrType == '' (street address)}:
   * <ul>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%AddressDeleted} {@link Boolean}. Same as result value</li>
   * </ul>
   * <p>
   * {@code if addrType != ''}:
   * <ul>
   *   <li>{@code homsOrderData*%Entity%*%AddrType%Deleted} {@link Boolean}. Same as result value</li>
   * </ul>
   * @param prefix       {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param entityPrefix {@link CharSequence String}. Entity prefix. Optional. Default: empty string
   * @param bindAddrType {@link CharSequence String}. Entity-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param addrType     {@link CharSequence String}. Address type without 'ADDR_TYPE_' part. Optional. Default: empty string (street address)
   * @param isMain       {@link Boolean}. If true search only main entity addresses, if false search only non-main entity addresses if null - disable filter. Optional. Default: true
   * @return True if entity address was deleted successfully, false otherwise
   */
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

    def entityAddressId = order."${prefix}${prefixId}"
    def addressCode = order."${prefix}${prefixCode}"
    Boolean result = hydra.deleteEntityAddress(
      entityId        : entityId,
      entityType      : entityType,
      entityAddressId : entityAddressId,
      bindAddrType    : "BIND_ADDR_TYPE_${bindAddrType}",
      addrType        : "ADDR_TYPE_${addrType ?: 'FactPlace'}",
      code            : addressCode,
      isMain          : isMain
    )

    order."${prefix}${prefixDeleted}" = result
    return result
  }

  /**
   * Delete base subject address and fill up execution variables
   * <p>
   * Input execution variables:
   * <p>
   * <ul>
   *   <li>{@code homsOrderData*BaseSubjectId}                       {@link java.math.BigInteger BigInteger}. Base subject id</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%AddressId} {@link java.math.BigInteger BigInteger}. Subject address id</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%AddressDeleted} {@link Boolean}. Same as result value</li>
   * </ul>
   * @param prefix        {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param subjectPrefix {@link CharSequence String}. Base subject suffix. Optional. Default: empty string
   * @param bindAddrType  {@link CharSequence String}. Subject-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param isMain        {@link Boolean}. If true search only main subject addresses, if false search only non-main subject addresses if null - disable filter. Optional. Default: true
   * @return True if base subject address was deleted successfully, false otherwise
   */
  Boolean deleteBaseSubjectAddress(Map input = [:]) {
    Map params = [
      addrType      : '',
      bindAddrType  : 'Actual',
      isMain        : true,
      subjectPrefix : '',
      prefix        : ''
    ] + input

    return deleteAddress(params + [
      entityPrefix : "${params.subjectPrefix}BaseSubject"
    ])
  }

  /**
   * Delete base subject phone number and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubjectId}           {@link java.math.BigInteger BigInteger}. Base subject id</li>
   *   <li>{@code homsOrderData*BaseSubject*Telephone}   {@link CharSequence String}. Address code. Optional if 'TelephoneId' is set</li>
   *   <li>{@code homsOrderData*BaseSubject*TelephoneId} {@link java.math.BigInteger BigInteger}. SUbject address id. Optional if 'Telephone' is set</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*TelephoneDeleted} {@link Boolean}. Same as result value</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param subjectPrefix   {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Equipment-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Notice'
   * @param isMain          {@link Boolean}. If true search only main subject addresses, if false search only non-main subject addresses if null - disable filter. Optional. Default: true
   * @return True if base subject phone number was deleted successfully, false otherwise
   */
  Boolean deleteBaseSubjectPhone(Map input = [:]) {
    Map params = [
      bindAddrType  : 'Notice',
      isMain        : true,
      subjectPrefix : '',
      prefix        : ''
    ] + input

    return deleteBaseSubjectAddress(params + [
      addrType : 'Telephone'
    ])
  }

  /**
   * Delete base subject phone number and fill up execution variables
   *
   * Alias for {@link #deleteBaseSubjectPhone(java.util.Map)}
   * @see #deleteBaseSubjectPhone(java.util.Map)
   */
  Boolean deleteBaseSubjectTelephone(Map input = [:]) {
    deleteBaseSubjectPhone(input)
  }

  /**
   * Delete base subject e-mail address and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubjectId}       {@link java.math.BigInteger BigInteger}. Base subject id</li>
   *   <li>{@code homsOrderData*BaseSubject*EMail}   {@link CharSequence String}. Address code. Optional if 'EMailId' is set</li>
   *   <li>{@code homsOrderData*BaseSubject*EMailId} {@link java.math.BigInteger BigInteger}. SUbject address id. Optional if 'EMail' is set</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*EMailDeleted} {@link Boolean}. Same as result value</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param subjectPrefix   {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Equipment-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Notice'
   * @param isMain          {@link Boolean}. If true search only main subject addresses, if false search only non-main subject addresses if null - disable filter. Optional. Default: true
   * @return True if base subject e-mail address was deleted successfully, false otherwise
   */
  Boolean deleteBaseSubjectEmail(Map input = [:]) {
    Map params = [
      bindAddrType  : 'Notice',
      isMain        : true,
      subjectPrefix : '',
      prefix        : ''
    ] + input

    return deleteBaseSubjectAddress(params + [
      addrType : 'EMail'
    ])
  }

  /**
   * Delete equipment address and fill up execution variables
   * <p>
   * Input execution variables:
   * <p>
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id}                       {@link java.math.BigInteger BigInteger}. Equipment id</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%AddressId} {@link java.math.BigInteger BigInteger}. Object address id</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%AddressDeleted} {@link Boolean}. Same as result value</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Equipment-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Serv'
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses if null - disable filter. Optional. Default: true
   * @return True if equipment address was deleted successfully, false otherwise
   */
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

  /**
   * Delete equipment MAC address and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id}     {@link java.math.BigInteger BigInteger}. Equipment id</li>
   *   <li>{@code homsOrderData*Equipment**MAC}   {@link CharSequence String}. Address code. Optional if 'MACId' is set</li>
   *   <li>{@code homsOrderData*Equipment**MACId} {@link java.math.BigInteger BigInteger}. Object address id. Optional if 'MAC' is set</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**MACDeleted} {@link Boolean}. Same as result value</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Equipment-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses if null - disable filter. Optional. Default: true
   * @return True if equipment MAC address was deleted successfully, false otherwise
   */
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

  /**
   * Delete equipment Iv4P address and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id}    {@link java.math.BigInteger BigInteger}. Equipment id</li>
   *   <li>{@code homsOrderData*Equipment**IP}   {@link CharSequence String}. Address code. Optional if 'IPId' is set</li>
   *   <li>{@code homsOrderData*Equipment**IPId} {@link java.math.BigInteger BigInteger}. Object address id. Optional if 'IP' is set</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**IPDeleted} {@link Boolean}. Same as result value</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Equipment-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses if null - disable filter. Optional. Default: true
   * @return True if equipment IPv4 address was deleted successfully, false otherwise
   */
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

  /**
   * Delete equipment IPv6 subnet and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id}         {@link java.math.BigInteger BigInteger}. Equipment id</li>
   *   <li>{@code homsOrderData*Equipment**Subnet6}   {@link CharSequence String}. Address code. Optional if 'Subnet6Id' is set</li>
   *   <li>{@code homsOrderData*Equipment**Subnet6Id} {@link java.math.BigInteger BigInteger}. Object address id. Optional if 'Subnet6' is set</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Subnet6Deleted} {@link Boolean}. Same as result value</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Equipment-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses if null - disable filter. Optional. Default: true
   * @return True if equipment IPv6 subnet was deleted successfully, false otherwise
   */
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

  /**
   * Delete equipment VLAN address and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id}      {@link java.math.BigInteger BigInteger}. Equipment id</li>
   *   <li>{@code homsOrderData*Equipment**VLAN}   {@link CharSequence String}. Address code. Optional if 'VLANId' is set</li>
   *   <li>{@code homsOrderData*Equipment**VLANId} {@link java.math.BigInteger BigInteger}. Object address id. Optional if 'VLAN' is set</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**VLANDeleted} {@link Boolean}. Same as result value</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Equipment-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses if null - disable filter. Optional. Default: true
   * @return True if equipmentVLAN address was deleted successfully, false otherwise
   */
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

  /**
   * Delete equipment IPv4 subnet and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id}        {@link java.math.BigInteger BigInteger}. Equipment id</li>
   *   <li>{@code homsOrderData*Equipment**Subnet}   {@link CharSequence String}. Address code. Optional if 'SubnetId' is set</li>
   *   <li>{@code homsOrderData*Equipment**SubnetId} {@link java.math.BigInteger BigInteger}. Object address id. Optional if 'Subnet' is set</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**SubnetDeleted} {@link Boolean}. Same as result value</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Equipment-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses if null - disable filter. Optional. Default: true
   * @return True if equipment IPv4 subnet was deleted successfully, false otherwise
   */
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

  /**
   * Delete equipment phone number and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id}           {@link java.math.BigInteger BigInteger}. Equipment id</li>
   *   <li>{@code homsOrderData*Equipment**Telephone}   {@link CharSequence String}. Address code. Optional if 'TelephoneId' is set</li>
   *   <li>{@code homsOrderData*Equipment**TelephoneId} {@link java.math.BigInteger BigInteger}. Object address id. Optional if 'Telephone' is set</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**TelephoneDeleted} {@link Boolean}. Same as result value</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Equipment-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses if null - disable filter. Optional. Default: true
   * @return True if equipment phone number was deleted successfully, false otherwise
   */
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