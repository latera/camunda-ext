package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.StringUtil.capitalize
import static org.camunda.latera.bss.utils.StringUtil.isEmpty

/**
  * Equipment helper methods collection
  */
trait Equipment {
  /**
   * Get equipment data by id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Name}     {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*Code}     {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*Serial}   {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*GoodId}   {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment*GoodName} {@link CharSequence String}</li>
   * </ul>
   * @param prefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param suffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   */
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

  /**
   * Get customer's first equipment data by customer id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*CustomerId}       {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment*GoodId} {@link java.math.BigInteger BigInteger}. Optional</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id}       {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment*Name}     {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*Code}     {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*Serial}   {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*GoodName} {@link CharSequence String}</li>
   * </ul>
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param customerPrefix  {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   */
  void fetchCustomerFirstEquipment(Map input = [:]) {
    Map params = [
      equipmentPrefix : '',
      equipmentSuffix : '',
      customerPrefix  : ''
    ] + input

    String customerPrefix  = "${capitalize(params.customerPrefix)}Customer"
    String equipmentPrefix = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}"

    def customerId = order."${customerPrefix}Id"
    if (isEmpty(customerId)) {
      return
    }

    def goodId     = order."${equipmentPrefix}GoodId"
    Map equipment  = hydra.getEquipmentBy(
      ownerId: customerId,
      typeId: goodId
    )

    order."${equipmentPrefix}Id" = equipment?.n_object_id
    fetchEquipment(prefix: params.equipmentPrefix)
  }

  /**
   * Get equipment data by component id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Component*Id} {@link java.math.BigInteger BigInteger}. Optional</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id}       {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment*Name}     {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*Code}     {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*Serial}   {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*GoodName} {@link CharSequence String}</li>
   * </ul>
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param componentPrefix {@link CharSequence String}. Component prefix. Optional. Default: empty string
   * @param componentSuffix {@link CharSequence String}. Component suffix. Optional. Default: empty string
   */
  void fetchEquipmentByComponent(Map input = [:]) {
    Map params = [
      equipmentPrefix : '',
      equipmentSuffix : '',
      componentPrefix : '',
      componentSuffix : ''
    ] + input

    String equipmentPrefix = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}"
    String componentPrefix = "${equipmentPrefix}${capitalize(params.componentPrefix)}Component${capitalize(params.componentSuffix)}"

    def componentId = order."${componentPrefix}Id"
    if (isEmpty(componentId)) {
      return
    }

    Map component = hydra.getEquipmentComponentBy(componentId: componentId)

    order."${equipmentPrefix}Id" = component?.n_main_object_id
    fetchEquipment(prefix: params.equipmentPrefix)
  }

  /**
   * Get first equipment component data by equipment id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id} {@link java.math.BigInteger BigInteger}. Optional</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Component*Id}       {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**Component*Name}     {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment**Component*Code}     {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment**Component*Serial}   {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment**Component*GoodName} {@link CharSequence String}</li>
   * </ul>
   * @param typeId          {@link java.math.BigInteger BigInteger}. Component type id. Optional. Default: null
   * @param goodSpecId      {@link java.math.BigInteger BigInteger}. Component good specification id. Optional. Default: null
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param componentPrefix {@link CharSequence String}. Component prefix. Optional. Default: empty string
   * @param componentSuffix {@link CharSequence String}. Component suffix. Optional. Default: empty string
   */
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

    def equipmentId = order."${equipmentPrefix}Id"
    if (isEmpty(equipmentId)) {
      return
    }

    Map component = hydra.getEquipmentComponentBy(equipmentId: equipmentId, typeId: params.typeId, goodSpecId: params.goodSpecId)
    order."${equipmentPrefix}${componentPrefix}Id" = component?.n_object_id
    fetchEquipment(prefix: params.equipmentPrefix, suffix: "${capitalize(params.equipmentSuffix)}${componentPrefix}")
  }

  /**
   * Create equipment and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*CustomerId}       {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment*GoodId} {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment*Name}   {@link CharSequence String}. Optional</li>
   *   <li>{@code homsOrderData*Equipment*Code}   {@link CharSequence String}. Optional</li>
   *   <li>{@code homsOrderData*Equipment*Serial} {@link CharSequence String}. Optional</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id}      {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment*Created} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param prefix         {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param suffix         {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param customerPrefix {@link CharSequence String}. Customer prefix. Optional. Default: empty string
   * @return True if equipment was created successfully, false otherwise
   */
  Boolean createEquipment(Map input = [:]) {
    Map params = [
      customerPrefix : '',
      prefix         : '',
      suffix         : ''
    ] + input

    String customerPrefix = "${capitalize(params.customerPrefix)}Customer"
    String prefix = "${capitalize(params.prefix)}Equipment${capitalize(params.suffix)}"

    Map equipment = hydra.createEquipment(
      ownerId : order."${customerPrefix}Id",
      typeId  : order."${prefix}GoodId",
      name    : order."${prefix}Name",
      code    : order."${prefix}Code",
      serial  : order."${prefix}Serial"
    )
    Boolean result = false
    if (equipment) {
      order."${prefix}Id" = equipment.num_N_OBJECT_ID
      result = true
    }
    order."${prefix}Created" = result
    return result
  }

  /**
   * Create equipment component and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id}                {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**Component*GoodId} {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**Component*Name}   {@link java.math.BigInteger BigInteger}. Optional</li>
   *   <li>{@code homsOrderData*Equipment**Component*Code}   {@link java.math.BigInteger BigInteger}. Optional</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Component*Id}      {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**Component*Created} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Component prefix. Optional. Default: empty string
   * @param suffix          {@link CharSequence String}. Component suffix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @return True if equipment component was created successfully, false otherwise
   */
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
      typeId  : order."${prefix}GoodId",
      name    : order."${prefix}Name",
      code    : order."${prefix}Code"
    )
    Boolean result = false
    if (component) {
      order."${prefix}Id" = component.num_N_SPEC_OBJECT_ID
      result = true
    }
    order."${prefix}Created" = result
    return result
  }

  /**
   * Delete equipment component and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Deleted} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param prefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param suffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @return True if equipment was deleted successfully, false otherwise
   */
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

  /**
   * Deactivate equipment component and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Deactivated} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param prefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param suffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @return True if equipment was deativated successfully, false otherwise
   */
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

  /**
   * Unregister equipment component and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Unregistered} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param prefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param suffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @return True if equipment was unregistered successfully, false otherwise
   */
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

  /**
   * Get equipment-equipment bind and fill up execution variables
   * <p>
   * Input execution variables:
   * <p>
   * {@code if sourceIsComponent == true}:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Component*Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * {@code if sourceIsComponent == false}:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <p>
   * {@code if sourceIsComponent == true}:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Component**BindId}                   {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**Component**BindEquipmentId}          {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**Component**BindEquipmentComponentId} {@link java.math.BigInteger BigInteger}. Set only {@code if destinationIsComponent = true}</li>
   * </ul>
   * {@code if sourceIsComponent == false}:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**BindId}                   {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**BindEquipmentId}          {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**BindEquipmentComponentId} {@link java.math.BigInteger BigInteger}. Set only {@code if destinationIsComponent = true}</li>
   * </ul>
   * @param bindRole               {@link CharSequence String}. Bind role code without 'OBJOBJ_BIND_TYPE_'. Optional. Default: 'NetConnection'
   * @param sourceIsComponent      {@link Boolean}. True if Component variable should be taken into consideration, false otherwise
   * @param destinationIsComponent {@link Boolean}. False if bind destination is component and its id should be saved, false otherwise
   * @param equipmentPrefix        {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix        {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param componentPrefix        {@link CharSequence String}. Component prefix. Optional. Default: empty string
   * @param componentSuffix        {@link CharSequence String}. Component suffix. Optional. Default: empty string
   * @param prefix                 {@link CharSequence String}. Bind prefix. Optional. Default: empty string
   */
  void fetchEquipmentBind(Map input = [:]) {
    Map params = [
      bindRole               : 'NetConnection',
      sourceIsComponent      : true,
      destinationIsComponent : true,
      equipmentPrefix        : '',
      equipmentSuffix        : '',
      componentPrefix        : '',
      componentSuffix        : '',
      prefix                 : ''
    ] + input

    String equipmentPrefix     = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}"
    String componentPrefix     = "${equipmentPrefix}${capitalize(params.componentPrefix)}Component${capitalize(params.componentSuffix)}"
    String bindPrefix          = "${capitalize(params.prefix)}Bind"
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

  /**
   * Create equipment-equipment bind value and fill up execution variables
   * <p>
   * Input execution variables:
   * <p>
   * {@code if sourceIsComponent == true}:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Component**Id}                       {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**Component**BindEquipmentId}          {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**Component**BindEquipmentComponentId} {@link java.math.BigInteger BigInteger}. Optional, used only {@code if destinationIsComponent = true}</li>
   * </ul>
   * {@code if sourceIsComponent == false}:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id}                        {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**BindEquipmentId}          {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**BindEquipmentComponentId} {@link java.math.BigInteger BigInteger}. Optional, used only {@code if destinationIsComponent = true}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <p>
   * {@code if sourceIsComponent == true}:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Component**BindId} {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**Component**BindCreated} {@link Boolean}. Same as return value</li>
   * </ul>
   * {@code if sourceIsComponent == false}:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**BindId} {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**BindCreated} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param bindRole               {@link CharSequence String}. Bind role code without 'OBJOBJ_BIND_TYPE_'. Optional. Default: 'NetConnection'
   * @param sourceIsComponent      {@link Boolean}. True if Component variable should be taken into consideration, false otherwise
   * @param destinationIsComponent {@link Boolean}. False if bind destination is component and its id should be saved, false otherwise
   * @param equipmentPrefix        {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix        {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param componentPrefix        {@link CharSequence String}. Component prefix. Optional. Default: empty string
   * @param componentSuffix        {@link CharSequence String}. Component suffix. Optional. Default: empty string
   * @param prefix                 {@link CharSequence String}. Bind prefix. Optional. Default: empty string
   * @return True if equipment-equipment bind was created successfully, false otherwise
   */
  Boolean createEquipmentBind(Map input = [:]) {
    Map params = [
      bindRole               : 'NetConnection',
      sourceIsComponent      : true,
      destinationIsComponent : true,
      equipmentPrefix        : '',
      equipmentSuffix        : '',
      componentPrefix        : '',
      componentSuffix        : '',
      prefix                 : ''
    ] + input

    String equipmentPrefix     = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}"
    String componentPrefix     = "${equipmentPrefix}${capitalize(params.componentPrefix)}Component${capitalize(params.componentSuffix)}"
    String bindPrefix          = "${capitalize(params.prefix)}Bind"
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

  /**
   * Delete equipment-equipment bind value and fill up execution variables
   * <p>
   * Input execution variables:
   * <p>
   * {@code if sourceIsComponent == true}:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Component**BindId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * {@code if sourceIsComponent == false}:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <p>
   * {@code if sourceIsComponent == true}:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Component**BindDeleted} {@link Boolean}. Same as return value</li>
   * </ul>
   * {@code if sourceIsComponent == false}:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**BindDeleted} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param sourceIsComponent {@link Boolean}. True if Component variable should be taken into consideration, false otherwise
   * @param equipmentPrefix   {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix   {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param componentPrefix   {@link CharSequence String}. Component prefix. Optional. Default: empty string
   * @param componentSuffix   {@link CharSequence String}. Component suffix. Optional. Default: empty string
   * @param prefix            {@link CharSequence String}. Bind prefix. Optional. Default: empty string
   * @return True if equipment-equipment bind was deleted successfully, false otherwise
   */
  Boolean deleteEquipmentBind(Map input = [:]) {
    Map params = [
      equipmentPrefix : '',
      equipmentSuffix : '',
      componentPrefix : '',
      componentSuffix : '',
      prefix          : ''
    ] + input

    String equipmentPrefix = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}"
    String componentPrefix = "${equipmentPrefix}${capitalize(params.componentPrefix)}Component${capitalize(params.componentSuffix)}"
    String bindPrefix      = "${capitalize(params.prefix)}Bind"
    String prefix          = params.sourceIsComponent ? componentPrefix : equipmentPrefix

    def bindId = order."${prefix}${bindPrefix}Id"
    Boolean result = hydra.deleteEquipmentBind(bindId)
    order."${prefix}${bindPrefix}Deleted" = result
    return result
  }

  /**
   * Get equipment data by its address (street, IP, MAC, VLAN, Subnet, Subnet6, Telephone) and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Address}   {@link CharSequence String}. Address code. Optional if 'AddressId' is set</li>
   *   <li>{@code homsOrderData*Equipment**AddressId} {@link CharSequence String}. Object address id. Optional if 'Address' is set</li>
   * </ul>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id}       {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**Name}     {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment**Code}     {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment**Serial}   {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment**GoodId}   {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**GoodName} {@link CharSequence String}</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. IP address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Serv'
   * @param addrType        {@link CharSequence String}. Address type without 'ADDR_TYPE_' part. Optional. Default: '' (street address)
   * @param beginDate       {@link java.time.Temporal Any date type}. Object address begin date. Optional. Default: null
   * @param endDate         {@link java.time.Temporal Any date type}. Object address end date. Optional. Default: null
   * @param operationDate   {@link java.time.Temporal Any date type}. Date which should overlap between object address begin and end dates. Optional. Default: current datetime
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses subscriptions, if null - disable filter. Optional. Default: true
   */
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

    if (isEmpty(addressId) && isEmpty(addressCode)) {
      return
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

  /**
   * Get equipment data by its MAC address and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**MAC}   {@link CharSequence String}. MAC address code. Optional if 'MACId' is set</li>
   *   <li>{@code homsOrderData*Equipment**MACId} {@link CharSequence String}. MAC object address id. Optional if 'MAC' is set</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id}       {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**Name}     {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment**Code}     {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment**Serial}   {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment**GoodId}   {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**GoodName} {@link CharSequence String}</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. MAC address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param beginDate       {@link java.time.Temporal Any date type}. Object address begin date. Optional. Default: null
   * @param endDate         {@link java.time.Temporal Any date type}. Object address end date. Optional. Default: null
   * @param operationDate   {@link java.time.Temporal Any date type}. Date which should overlap between object address begin and end dates. Optional. Default: current datetime
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses subscriptions, if null - disable filter. Optional. Default: true
   */
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

  /**
   * Get equipment data by its IPv4 address and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**IP}   {@link CharSequence String}. IP address code. Optional if 'IPId' is set</li>
   *   <li>{@code homsOrderData*Equipment**IPId} {@link CharSequence String}. IP object address id. Optional if 'IP' is set</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id}       {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**Name}     {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment**Code}     {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment**Serial}   {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment**GoodId}   {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**GoodName} {@link CharSequence String}</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. IP address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param beginDate       {@link java.time.Temporal Any date type}. Object address begin date. Optional. Default: null
   * @param endDate         {@link java.time.Temporal Any date type}. Object address end date. Optional. Default: null
   * @param operationDate   {@link java.time.Temporal Any date type}. Date which should overlap between object address begin and end dates. Optional. Default: current datetime
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses subscriptions, if null - disable filter. Optional. Default: true
   */
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

  /**
   * Get equipment data by its VLAN address and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**VLAN}   {@link CharSequence String}. VLAN address code. Optional if 'VLANId' is set</li>
   *   <li>{@code homsOrderData*Equipment**VLANId} {@link CharSequence String}. VLAN object address id. Optional if 'VLAN' is set</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id}       {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**Name}     {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment**Code}     {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment**Serial}   {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment**GoodId}   {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**GoodName} {@link CharSequence String}</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. VLAN address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param beginDate       {@link java.time.Temporal Any date type}. Object address begin date. Optional. Default: null
   * @param endDate         {@link java.time.Temporal Any date type}. Object address end date. Optional. Default: null
   * @param operationDate   {@link java.time.Temporal Any date type}. Date which should overlap between object address begin and end dates. Optional. Default: current datetime
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses subscriptions, if null - disable filter. Optional. Default: true
   */
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

  /**
   * Get equipment data by its IPv4 subnet address and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Subnet}   {@link CharSequence String}. IPv4 subnet address code. Optional if 'SubnetId' is set</li>
   *   <li>{@code homsOrderData*Equipment**SubnetId} {@link CharSequence String}. IPv4 subnet object address id. Optional if 'Subnet' is set</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id}       {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**Name}     {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment**Code}     {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment**Serial}   {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment**GoodId}   {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**GoodName} {@link CharSequence String}</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. IPv4 subnet address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param beginDate       {@link java.time.Temporal Any date type}. Object address begin date. Optional. Default: null
   * @param endDate         {@link java.time.Temporal Any date type}. Object address end date. Optional. Default: null
   * @param operationDate   {@link java.time.Temporal Any date type}. Date which should overlap between object address begin and end dates. Optional. Default: current datetime
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses subscriptions, if null - disable filter. Optional. Default: true
   */
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

  /**
   * Get equipment data by its IPv6 subnet address and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Subnet6}   {@link CharSequence String}. IPv6 subnet address code. Optional if 'Subnet6Id' is set</li>
   *   <li>{@code homsOrderData*Equipment**Subnet6Id} {@link CharSequence String}. IPv6 subnet object address id. Optional if 'Subnet6' is set</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id}       {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**Name}     {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment**Code}     {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment**Serial}   {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment**GoodId}   {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**GoodName} {@link CharSequence String}</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. IPv6 subnet address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param beginDate       {@link java.time.Temporal Any date type}. Object address begin date. Optional. Default: null
   * @param endDate         {@link java.time.Temporal Any date type}. Object address end date. Optional. Default: null
   * @param operationDate   {@link java.time.Temporal Any date type}. Date which should overlap between object address begin and end dates. Optional. Default: current datetime
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses subscriptions, if null - disable filter. Optional. Default: true
   */
  void fetchEquipmentBySubnet6(Map input = [:]) {
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

    fetchEquipmentByAddress(params + [addrType : 'Subnet6'])
  }

  /**
   * Get equipment data by its phone number and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Telephone}   {@link CharSequence String}. Phone number. Optional if 'TelephoneId' is set</li>
   *   <li>{@code homsOrderData*Equipment**TelephoneId} {@link CharSequence String}. Phone number object address id. Optional if 'Telephone' is set</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**Id}       {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**Name}     {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment**Code}     {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment**Serial}   {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment**GoodId}   {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**GoodName} {@link CharSequence String}</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Telephone address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Object-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param beginDate       {@link java.time.Temporal Any date type}. Object address begin date. Optional. Default: null
   * @param endDate         {@link java.time.Temporal Any date type}. Object address end date. Optional. Default: null
   * @param operationDate   {@link java.time.Temporal Any date type}. Date which should overlap between object address begin and end dates. Optional. Default: current datetime
   * @param isMain          {@link Boolean}. If true search only main object addresses, if false search only non-main object addresses subscriptions, if null - disable filter. Optional. Default: true
   */
  void fetchEquipmentByPhone(Map input = [:]) {
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

    fetchEquipmentByAddress(params + [addrType : 'Telephone'])
  }

  /**
   * Get equipment data by its phone number and fill up execution variables
   *
   * Alias for {@link #fetchEquipmentByPhone(Map)}
   * @see #fetchEquipmentByPhone(Map)
   */
  void fetchEquipmentByTelephone(Map input = [:]) {
    fetchEquipmentByPhone(input)
  }

  /**
   * Get equipment additional parameter value and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**%Param%Id} {@link java.math.BigInteger BigInteger}. if additional parameter value is ref id</li>
   *   <li>{@code homsOrderData*Equipment**%Param%}   Any type, if additional parameter is not a ref</li>
   * </ul>
   * @param param           {@link CharSequence String}. Additional parameter short code (=variable part name) ('Param' for 'SUBJ_VAL_Param')
   * @param code            {@link CharSequence String}. Additional parameter full code (if it does not start from 'SUBJ_VAL_')
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param prefix          {@link CharSequence String}. Additional parameter prefix. Optional. Default: empty string
   * @return Additional parameter value
   */
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

    def equipmentId = order."${equipmentPrefix}Id"
    if (isEmpty(equipmentId)) {
      return
    }

    Map addParam = hydra.getEquipmentAddParamBy(
      equipmentId : equipmentId,
      param       : params.code ?: "EQUIP_ADD_PARAM_${param}"
    )
    def (value, valueType) = hydra.getAddParamValue(addParam)
    order."${equipmentPrefix}${prefix}${params.code ?: param}${valueType == 'refId' ? 'Id': ''}" = value
    return value
  }

  /**
   * Save equipment additional parameter value and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id} {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**%Param%Id} {@link java.math.BigInteger BigInteger}. if additional parameter value is ref id</li>
   *   <li>{@code homsOrderData*Equipment**%Param%}   Any type, if additional parameter value is not a ref or ref code is used instead</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**%Param%Saved} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param param           {@link CharSequence String}. Additional parameter short code (=variable part name) ('Param' for 'SUBJ_VAL_Param')
   * @param code            {@link CharSequence String}. Additional parameter full code (if it does not start from 'SUBJ_VAL_')
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param prefix          {@link CharSequence String}. Additional parameter prefix. Optional. Default: empty string
   * @return True if additional parameter value was saved successfully, false otherwise
   */
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

  /**
   * Delete base subject additional parameter value and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id}         {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**%Param%Id} {@link java.math.BigInteger BigInteger}. if additional parameter value is ref id</li>
   *   <li>{@code homsOrderData*Equipment**%Param%}   Any type, if additional parameter value is not a ref or ref code is used instead</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**%Param%Deleted} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param param           {@link CharSequence String}.  Additional parameter short code (=variable part name) ('Param' for 'SUBJ_VAL_Param')
   * @param code            {@link CharSequence String}.  Additional parameter full code (if it does not start from 'SUBJ_VAL_')
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param prefix          {@link CharSequence String}.  Additional parameter prefix. Optional. Default: empty string
   * @param force           {@link Boolean}. For multiple additional parameters. If you need to remove only a value which is equal to one stored in the input execution variable, pass false. Otherwise method will remove additional param value without check
   * @return True if additional parameter value was deleted successfully, false otherwise
   */
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
