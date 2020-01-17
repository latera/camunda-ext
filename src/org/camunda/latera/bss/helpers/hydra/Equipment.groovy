package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.StringUtil.capitalize
import static org.camunda.latera.bss.utils.StringUtil.isEmpty

trait Equipment {
  void fetchEquipment(Map input = [:]) {
    Map params = [
      prefix : '',
      suffix : ''
    ] + input

    String prefix = "${capitalize(params.prefix)}Equipment${capitalize(params.suffix)}"

    Map equipment = hydra.getEquipment(order."${prefix}Id")
    Map good = hydra.getGood(equipment?.n_good_id)

    order."${prefix}Name"   = equipment?.vc_name
    order."${prefix}Code"   = equipment?.vc_code
    order."${prefix}Serial" = equipment?.vc_serial
    if (isEmpty(order."${prefix}GoodName")) {
      order."${prefix}GoodName" = good?.vc_name
    }
    if (isEmpty(order."${prefix}GoodId")) {
      order."${prefix}GoodId" = equipment?.n_good_id
    }
  }

  void fetchCustomerFirstEquipment(Map input = [:]) {
    Map params = [
      equipmentPrefix : '',
      equipmentSuffix : '',
      customerPrefix  : '',
      customerSuffix  : ''
    ] + input

    String customerPrefix  = "${capitalize(params.customerPrefix)}Customer${capitalize(params.customerSuffix)}"
    String equipmentPrefix = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}"

    def customerId = order."${customerPrefix}Id"
    def goodId     = order."${equipmentPrefix}GoodId"
    Map equipment  = hydra.getEquipmentBy(
      ownerId: customerId ?: [is: 'null'],
      typeId: goodId
    )

    order."${equipmentPrefix}Id" = equipment?.n_object_id
    fetchEquipment(prefix: params.equipmentPrefix)
  }

  void fetchEquipmentByComponent(Map input = [:]) {
    Map params = [
      equipmentPrefix : '',
      equipmentSuffix : '',
      componentPrefix : '',
      componentSuffix : ''
    ] + input

    String equipmentPrefix = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}"
    String componentPrefix = "${equipmentPrefix}${capitalize(params.componentPrefix)}Component${capitalize(params.componentSuffix)}"

    def componentId = order."${componentPrefix}Id" ?: [is: 'null']
    Map component = hydra.getEquipmentComponentBy(componentId: componentId)

    order."${equipmentPrefix}Id" = component?.n_main_object_id
    fetchEquipment(prefix: params.equipmentPrefix)
  }

  void fetchEquipmentFirstComponent(Map input = [:]) {
    Map params = [
      typeId          : null,
      goodSpecId      : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      componentPrefix : '',
      componentSuffix : ''
    ] + input

    String equipmentPrefix = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}"
    String componentPrefix = "${capitalize(params.componentPrefix)}Component${capitalize(params.componentSuffix)}"

    def equipmentId = order."${equipmentPrefix}Id"?: [is: 'null']
    Map component = hydra.getEquipmentComponentBy(equipmentId: equipmentId, typeId: params.typeId, goodSpecId: params.goodSpecId)

    order."${equipmentPrefix}${componentPrefix}Id" = component?.n_object_id
    fetchEquipment(prefix: params.equipmentPrefix, suffix: "${capitalize(params.equipmentSuffix)}${componentPrefix}")
  }

  Boolean createEquipment(Map input = [:]) {
    Map params = [
      customerPrefix : '',
      customerSuffix : '',
      prefix         : '',
      suffix         : ''
    ] + input

    String customerPrefix = "${capitalize(params.customerPrefix)}Customer${capitalize(params.customerSuffix)}"
    String prefix = "${capitalize(params.prefix)}Equipment${capitalize(params.suffix)}"

    Map equipment = hydra.createEquipment(
      ownerId : order."${customerPrefix}Id",
      typeId  : order."${prefix}GoodId"
    )
    Boolean result = false
    if (equipment) {
      order."${prefix}Id" = equipment.num_N_OBJECT_ID
      result = true
    }
    order."${prefix}Created" = result
    return result
  }

  Boolean createEquipmentComponent(Map input = [:]) {
    Map params = [
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      suffix          : ''
    ] + input

    String equipmentPrefix = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}"
    String prefix = "${equipmentPrefix}${capitalize(params.prefix)}Component${capitalize(params.suffix)}"

    Map component = hydra.createEquipmentComponent(
      order."${equipmentPrefix}Id",
      typeId      : order."${prefix}GoodId"
    )
    Boolean result = false
    if (component) {
      order."${prefix}Id" = component.num_N_SPEC_OBJECT_ID
      result = true
    }
    order."${prefix}Created" = result
    return result
  }

  Boolean deleteEquipment(Map input = [:]) {
    Map params = [
      prefix : '',
      suffix : ''
    ] + input

    String prefix = "${capitalize(params.prefix)}Equipment${capitalize(params.suffix)}"

    Boolean result = hydra.deleteEquipment(order."${prefix}Id")
    order."${prefix}Deleted" = result
    return result
  }

  Boolean deactivateEquipment(Map input = [:]) {
    Map params = [
      prefix : '',
      suffix : ''
    ] + input

    String prefix = "${capitalize(params.prefix)}Equipment${capitalize(params.suffix)}"

    Boolean result = hydra.deactivateEquipment(order."${prefix}Id")
    order."${prefix}Deactivated" = result
    return result
  }

  Boolean unregisterEquipment(Map input = [:]) {
    Map params = [
      prefix : '',
      suffix : ''
    ] + input

    String prefix = "${capitalize(params.prefix)}Equipment${capitalize(params.suffix)}"

    Boolean result = hydra.unregisterEquipment(order."${prefix}Id")
    order."${prefix}Unregistered" = result
    return result
  }

  void fetchEquipmentBind(Map input = [:]) {
    Map params = [
      bindRole               : 'NetConnection',
      sourceIsComponent      : true,
      destinationIsComponent : true,
      equipmentPrefix        : '',
      equipmentSuffix        : '',
      componentPrefix        : '',
      componentSuffix        : '',
      prefix                 : '',
      suffix                 : ''
    ] + input

    String equipmentPrefix     = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}"
    String componentPrefix     = "${equipmentPrefix}${capitalize(params.componentPrefix)}Component${capitalize(params.componentSuffix)}"
    String bindPrefix          = "${capitalize(params.prefix)}Bind${capitalize(params.suffix)}"
    String bindEquipmentPrefix = "${bindPrefix}Equipment"
    String bindComponentPrefix = "${bindEquipmentPrefix}Component"
    String prefix = params.sourceIsComponent ? componentPrefix : equipmentPrefix

    def equipmentId = order."${equipmentPrefix}Id"
    def componentId = order."${componentPrefix}Id"
    Map inp = [
      mainId      : params.sourceIsComponent ? equipmentId : null,
      componentId : params.sourceIsComponent ? componentId : equipmentId,
      bindRole    : "OBJOBJ_BIND_TYPE_${params.bindRole}"
    ]

    Map equipmentBind = hydra.getEquipmentBindBy(inp)
    if (equipmentBind) {
      order."${prefix}${bindPrefix}Id" = equipmentBind.n_obj_object_id
      if (params.destinationIsComponent) {
        order."${prefix}${bindEquipmentPrefix}Id" = equipmentBind.n_bind_main_obj_id
        order."${prefix}${bindComponentPrefix}Id" = equipmentBind.n_bind_object_id
      } else {
        order."${prefix}${bindEquipmentPrefix}Id" = equipmentBind.n_bind_object_id
        order."${prefix}${bindComponentPrefix}Id" = null
      }
    }
  }

  Boolean createEquipmentBind(Map input = [:]) {
    Map params = [
      bindRole               : 'NetConnection',
      sourceIsComponent      : true,
      destinationIsComponent : true,
      equipmentPrefix        : '',
      equipmentSuffix        : '',
      componentPrefix        : '',
      componentSuffix        : '',
      prefix                 : '',
      suffix                 : ''
    ] + input

    String equipmentPrefix     = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}"
    String componentPrefix     = "${equipmentPrefix}${capitalize(params.componentPrefix)}Component${capitalize(params.componentSuffix)}"
    String bindPrefix          = "${capitalize(params.prefix)}Bind${capitalize(params.suffix)}"
    String bindEquipmentPrefix = "${bindPrefix}Equipment"
    String bindComponentPrefix = "${bindEquipmentPrefix}Component"
    String prefix = params.sourceIsComponent ? componentPrefix : equipmentPrefix

    def equipmentId     = order."${equipmentPrefix}Id"
    def componentId     = order."${componentPrefix}Id"
    def bindEquipmentId = order."${prefix}${bindEquipmentPrefix}Id"
    def bindComponentId = order."${prefix}${bindComponentPrefix}Id"

    Map inp = [
      bindRole        : "OBJOBJ_BIND_TYPE_${params.bindRole}",
      mainId          : equipmentId,
      componentId     : params.sourceIsComponent      ? componentId     : null,
      bindMainId      : params.destinationIsComponent ? bindEquipmentId : null,
      bindComponentId : params.destinationIsComponent ? bindComponentId : bindEquipmentId
    ]

    Map equipmentBind = hydra.addEquipmentBind(inp)
    Boolean result = false
    if (equipmentBind) {
      order."${prefix}${bindPrefix}Id" = equipmentBind.num_N_OBJ_OBJECT_ID
      result = true
    }

    order."${prefix}${bindPrefix}Created" = result
    return result
  }

  Boolean deleteEquipmentBind(Map input = [:]) {
    Map params = [
      equipmentPrefix : '',
      equipmentSuffix : '',
      componentPrefix : '',
      componentSuffix : '',
      prefix          : '',
      suffix          : ''
    ] + input

    String equipmentPrefix = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}"
    String componentPrefix = "${equipmentPrefix}${capitalize(params.componentPrefix)}Component${capitalize(params.componentSuffix)}"
    String bindPrefix      = "${capitalize(params.prefix)}Bind${capitalize(params.suffix)}"
    String prefix          = params.sourceIsComponent ? componentPrefix : equipmentPrefix

    def bindId = order."${prefix}${bindPrefix}Id"
    Boolean result = hydra.deleteEquipmentBind(bindId)
    order."${prefix}${bindPrefix}Deleted" = result
    return result
  }

  void fetchEquipmentByAddress(Map input = [:]) {
    Map params = [
      addrType        : '',
      bindAddrType    : 'Serv',
      isMain          : true,
      operationDate   : null,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    String equipmentPrefix = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}"
    String prefix = "${equipmentPrefix}${capitalize(params.prefix)}${isEmpty(params.addrType) ? params.bindAddrType + 'Address' : params.addrType}"

    def addressId      = order."${prefix}Id"
    String addressCode = order."${prefix}"

    if (addressId == null && addressCode == null) {
      addressCode = [is: 'null']
    }
    Map address = hydra.getEntityAddress(
      operationDate   : params.operationDate,
      beginDate       : params.beginDate,
      endDate         : params.endDate,
      entityAddressId : addressId,
      code            : addressCode, //TODO нормальный поиск по адресам
      addrType        : "ADDR_TYPE_${params.addrType ?: 'FactPlace'}",
      bindAddrType    : "BIND_ADDR_TYPE_${params.bindAddrType}",
      isMain          : params.isMain
    )
    order."${equipmentPrefix}Id" = address?.n_object_id
    fetchEquipment(prefix: params.equipmentPrefix, suffix: params.equipmentSuffix)
  }

  void fetchEquipmentByMAC(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      operationDate   : null,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    fetchEquipmentByAddress(params + [addrType : 'MAC'])
  }

  void fetchEquipmentByIP(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      operationDate   : null,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    fetchEquipmentByAddress(params + [addrType : 'IP'])
  }

  void fetchEquipmentByVLAN(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      operationDate   : null,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    fetchEquipmentByAddress(params + [addrType : 'VLAN'])
  }

  void fetchEquipmentBySubnet(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Actual',
      isMain          : true,
      operationDate   : null,
      beginDate       : null,
      endDate         : null,
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    fetchEquipmentByAddress(params + [addrType : 'Subnet'])
  }

  def fetchEquipmentAddParam(Map input = [:]) {
    Map params = [
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      param           : '',
      code            : ''
    ] + input

    String equipmentPrefix = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}"
    String prefix = capitalize(params.prefix)
    String param  = capitalize(params.param)

    def equipmentId = order."${equipmentPrefix}Id" ?: [is: 'null']
    Map addParam = hydra.getEquipmentAddParamBy(
      equipmentId : equipmentId,
      param       : params.code ?: "EQUIP_ADD_PARAM_${param}"
    )
    def (value, valueType) = hydra.getAddParamValue(addParam)
    order."${equipmentPrefix}${prefix}${params.code ?: param}${valueType == 'refId' ? 'Id': ''}" = value
    return value
  }

  Boolean saveEquipmentAddParam(Map input = [:]) {
    Map params = [
      equipmentPrefix : '',
      equipmentSuffix : '',
      param           : '',
      code            : ''
    ] + input

    String equipmentPrefix = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}"
    String prefix   = capitalize(params.prefix)
    String param    = capitalize(params.param)
    def equipmentId = order."${equipmentPrefix}Id"
    def value       = order."${equipmentPrefix}${prefix}${params.code ?: param}" ?: order."${equipmentPrefix}${prefix}${params.code ?: param}Id"

    Map addParam = hydra.addEquipmentAddParam(
      equipmentId,
      param : params.code ?: "EQUIP_ADD_PARAM_${param}",
      value : value
    )
    Boolean result = false
    if (addParam) {
      result = true
    }
    order."${equipmentPrefix}${prefix}${params.code ?: param}Saved" = result
    return result
  }

  Boolean deleteEquipmentAddParam(Map input = [:]) {
    Map params = [
      equipmentPrefix : '',
      equipmentSuffix : '',
      param           : '',
      code            : '',
      force           : false
    ] + input

    String equipmentPrefix = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}"
    String prefix   = capitalize(params.prefix)
    String param    = capitalize(params.param)
    def equipmentId = order."${equipmentPrefix}Id"
    def value       = order."${equipmentPrefix}${prefix}${params.code ?: param}" ?: order."${equipmentPrefix}${prefix}${params.code ?: param}Id"

    Boolean result = true

    if (params.force) {
      result = hydra.deleteEquipmentAddParam(
        equipmentId : equipmentId,
        param       : params.code ?: "EQUIP_ADD_PARAM_${param}"
      )
    } else {
      result = hydra.deleteEquipmentAddParam(
        equipmentId : equipmentId,
        param       : params.code ?: "EQUIP_ADD_PARAM_${param}",
        value       : value // multiple add param support
      )
    }

    order."${equipmentPrefix}${prefix}${params.code ?: param}Deleted" = result
    return result
  }
}
