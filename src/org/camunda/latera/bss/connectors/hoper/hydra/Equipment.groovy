package org.camunda.latera.bss.connectors.hoper.hydra

trait Equipment {
  private static LinkedHashMap EQUIPMENT_ENTITY_TYPE = [
    one    : 'net_device',
    plural : 'net_devices'
  ]
  private static LinkedHashMap CUSTOMER_EQUIPMENT_ENTITY_TYPE = [
    one    : 'equipment',
    plural : 'equipment'
  ]
  private static LinkedHashMap EQUIPMENT_ENTRY_ENTITY_TYPE = [
    one    : 'entry',
    plural : 'entries'
  ]
  private static Integer EQUIPMENT_STATE_ACTUAL_ID       = 1040 // 'OBJ_STATE_Active'
  private static Integer EQUIPMENT_STATE_NOT_ACTIVE_ID   = 2040 // 'OBJ_STATE_NotActive'
  private static Integer EQUIPMENT_STATE_REGISTER_OFF_ID = 3040 // 'OBJ_STATE_RegisterOff'

  Map getEquipmentEntityType(def id = null) {
    return EQUIPMENT_ENTITY_TYPE + withParent(getObjectEntityType()) + withId(id)
  }

  Map getEquipmentEntryEntityType(def equipmentId, def id = null) {
    return EQUIPMENT_ENTRY_ENTITY_TYPE + withParent(getEquipmentEntityType(equipmentId)) + withId(id)
  }

  Map getCustomerEquipmentEntityType(def customerId, def id = null) {
    return CUSTOMER_EQUIPMENT_ENTITY_TYPE + withParent(getCustomerEntityType(customerId)) + withId(id)
  }

  Integer getEquipmentStateActualId() {
    return EQUIPMENT_STATE_ACTUAL_ID
  }

  Integer getEquipmentStateNotActiveId() {
    return EQUIPMENT_STATE_NOT_ACTIVE_ID
  }

  Integer getEquipmentStateRegisterOffId() {
    return EQUIPMENT_STATE_REGISTER_OFF_ID
  }

  private Map getEquipmentDefaultParams() {
    return [
      code            : null,
      name            : null,
      goodId          : null,
      extCode         : null,
      invNo           : null,
      serialNo        : null,
      rem             : null,
      firmId          : getFirmId(),
      stateId         : getEquipmentStateActualId()
    ]
  }

  private Map getEquipmentEntryDefaultParams() {
    return getEquipmentDefaultParams() + [
      mainObjectId    : null,
      firmId          : getFirmId(),
      stateId         : getEquipmentStateActualId()
    ]
  }

  private Map getEquipmentParamsMap(Map params, Map additionalParams = [:]) {
    LinkedHashMap result =  [
      vc_code             : params.code,
      vc_name             : params.name,
      n_good_id           : params.goodId,
      vc_code_add         : params.extCode,
      vc_inventory_number : params.invNo,
      vc_serial_number    : params.serialNo,
      vc_rem              : params.rem,
      n_obj_state_id      : params.stateId,
      n_firm_id           : params.firmId
    ]
    if (additionalParams) {
      result.additional_values = params.additionalParams
    }
    return result
  }

  private Map getEquipmentEntryParamsMap(Map params, Map additionalParams = [:]) {
    LinkedHashMap result =  [
      vc_code             : params.code,
      vc_name             : params.name,
      n_catalog_item_id   : params.goodId,
      n_main_object_id    : params.mainObjectId,
      vc_code_add         : params.extCode,
      vc_inventory_number : params.invNo,
      vc_serial_number    : params.serialNo,
      vc_rem              : params.rem,
      n_obj_state_id      : params.stateId,
      n_firm_id           : params.firmId
    ]
    if (additionalParams) {
      result.additional_values = params.additionalParams
    }
    return result
  }

  private Map getEquipmentParams(Map input, Map additionalParams = [:]) {
    LinkedHashMap params = getEquipmentDefaultParams() + input
    LinkedHashMap data   = getEquipmentParamsMap(params, additionalParams)
    return prepareParams(data)
  }

  private Map getEquipmentEntryParams(Map input, Map additionalParams = [:]) {
    LinkedHashMap params = getEquipmentEntryDefaultParams() + input
    LinkedHashMap data   = getEquipmentEntryParamsMap(params, additionalParams)
    return prepareParams(data)
  }

  List getCustomerObjects(Map input = [:], def customerId) {
    LinkedHashMap params = getPaginationDefaultParams() + input
    return getEntities(getCustomerEquipmentEntityType(customerId), params)
  }

  Map getCustomerEquipment(def customerId, def equipmentId) {
    return getEntity(getCustomerEquipmentEntityType(customerId), equipmentId)
  }

  Map getCustomerObject(def customerId, def equipmentId) {
    return getCustomerEquipment(customerId, equipmentId)
  }

  List getEquipmentEntries(Map input = [:], def equipmentId) {
    LinkedHashMap params = getPaginationDefaultParams() + input
    return getEntities(getEquipmentEntryEntityType(equipmentId), params)
  }

  Map getEquipmentEntry(def equipmentId, def entryId) {
    return getEntity(getEquipmentEntryEntityType(equipmentId), entryId)
  }

  Map createCustomerEquipment(Map input = [:], def customerId, Map additionalParams = [:]) {
    LinkedHashMap params = getEquipmentParams(input, additionalParams)
    return createEntity(getCustomerEquipmentEntityType(customerId), params)
  }

  Map createEquipmentEntry(Map input = [:], def equipmentId, Map additionalParams = [:]) {
    LinkedHashMap params = getEquipmentEntryParams(input + [mainObjectId: equipmentId], additionalParams)
    return createEntity(getEquipmentEntryEntityType(equipmentId), params)
  }

  Map updateCustomerEquipment(Map input = [:], def customerId, def equipmentId, Map additionalParams = [:]) {
    LinkedHashMap params = getEquipmentParams(input, additionalParams)
    return updateEntity(getCustomerEquipmentEntityType(customerId), equipmentId, params)
  }

  Map updateEquipmentEntry(Map input = [:], def equipmentId, def entryId, Map additionalParams = [:]) {
    LinkedHashMap params = getEquipmentEntryParams(input, additionalParams)
    return updateEntity(getEquipmentEntryEntityType(equipmentId), entryId, params)
  }

  Boolean deleteCustomerEquipment(def customerId, def equipmentId) {
    return deleteEntity(getCustomerEquipmentEntityType(customerId), equipmentId)
  }

  Boolean deleteEquipmentEntry(def equipmentId, def entryId) {
    return deleteEntity(getEquipmentEntryEntityType(equipmentId), entryId)
  }
}