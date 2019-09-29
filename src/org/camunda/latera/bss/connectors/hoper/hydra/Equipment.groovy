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

  private static Map getEquipmentEntityType(def id = null) {
    return EQUIPMENT_ENTITY_TYPE + withParent(getObjectEntityType()) + withId(id)
  }

  private static Map getEquipmentEntryEntityType(def equipmentId, def id = null) {
    return EQUIPMENT_ENTRY_ENTITY_TYPE + withParent(getEquipmentEntityType(equipmentId)) + withId(id)
  }

  private static Map getCustomerEquipmentEntityType(def customerId, def id = null) {
    return CUSTOMER_EQUIPMENT_ENTITY_TYPE + withParent(getCustomerEntityType(customerId)) + withId(id)
  }

  static Number getEquipmentStateActualId() {
    return EQUIPMENT_STATE_ACTUAL_ID
  }

  static Number getEquipmentStateNotActiveId() {
    return EQUIPMENT_STATE_NOT_ACTIVE_ID
  }

  static Number getEquipmentStateRegisterOffId() {
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

  private static Map getEquipmentParamsMap(Map params, Map additionalParams = [:]) {
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

  private static Map getEquipmentEntryParamsMap(Map params, Map additionalParams = [:]) {
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

  List getCustomerObjects(def customerId, Map input = [:]) {
    LinkedHashMap params = getPaginationDefaultParams() + input
    return getEntities(getCustomerEquipmentEntityType(customerId), params)
  }

  List getCustomerObjects(Map input, def customerId) {
    return getCustomerObjects(customerId, input)
  }

  Map getCustomerEquipments(def customerId, Map input = [:]) {
    return getCustomerObjects(customerId, input)
  }

  List getCustomerEquipments(Map input, def customerId) {
    return getCustomerEquipments(customerId, input)
  }

  Map getCustomerEquipment(def customerId, def equipmentId) {
    return getEntity(getCustomerEquipmentEntityType(customerId), equipmentId)
  }

  List getEquipmentEntries(def equipmentId, Map input = [:]) {
    LinkedHashMap params = getPaginationDefaultParams() + input
    return getEntities(getEquipmentEntryEntityType(equipmentId), params)
  }

  List getEquipmentEntries(Map input, def equipmentId) {
    return getEquipmentEntries(equipmentId, input)
  }

  Map getEquipmentEntry(def equipmentId, def entryId) {
    return getEntity(getEquipmentEntryEntityType(equipmentId), entryId)
  }

  Map createCustomerEquipment(def customerId, Map input, Map additionalParams = [:]) {
    LinkedHashMap params = getEquipmentParams(input, additionalParams)
    return createEntity(getCustomerEquipmentEntityType(customerId), params)
  }

  Map createCustomerEquipment(Map input, Map additionalParams = [:]) {
    def customerId = input.customerId
    input.remove('customerId')
    return createCustomerEquipment(customerId, input, additionalParams)
  }

  Map createCustomerEquipment(Map input, def customerId, Map additionalParams = [:]) {
    return createCustomerEquipment(customerId, input, additionalParams)
  }

  Map createEquipmentEntry(def equipmentId, Map input, Map additionalParams = [:]) {
    LinkedHashMap params = getEquipmentEntryParams(input + [mainObjectId: equipmentId], additionalParams)
    return createEntity(getEquipmentEntryEntityType(equipmentId), params)
  }

  Map createEquipmentEntry(Map input, Map additionalParams = [:]) {
    def equipmentId = input.equipmentId
    input.remove('equipmentId')
    return createCustomerEquipment(equipmentId, input, additionalParams)
  }

  Map createEquipmentEntry(Map input, def equipmentId, Map additionalParams = [:]) {
    return createEquipmentEntry(equipmentId, input, additionalParams)
  }

  Map updateCustomerEquipment(def customerId, def equipmentId, Map input, Map additionalParams = [:]) {
    LinkedHashMap params = getEquipmentParams(input, additionalParams)
    return updateEntity(getCustomerEquipmentEntityType(customerId), equipmentId, params)
  }

  Map updateCustomerEquipment(Map input, Map additionalParams = [:]) {
    def customerId = input.customerId
    input.remove('customerId')
    def equipmentId = input.equipmentId
    input.remove('equipmentId')
    return updateCustomerEquipment(customerId, equipmentId, input, additionalParams)
  }

  Map updateCustomerEquipment(Map input, def customerId, def equipmentId, Map additionalParams = [:]) {
    return updateCustomerEquipment(customerId, equipmentId, input, additionalParams)
  }

  Map updateEquipmentEntry(def equipmentId, def entryId, Map input, Map additionalParams = [:]) {
    LinkedHashMap params = getEquipmentEntryParams(input, additionalParams)
    return updateEntity(getEquipmentEntryEntityType(equipmentId), entryId, params)
  }

  Map updateEquipmentEntry(Map input, Map additionalParams = [:]) {
    def equipmentId = input.equipmentId
    input.remove('equipmentId')
    def entryId = input.entryId
    input.remove('entryId')
    return updateEquipmentEntry(equipmentId, entryId, input, additionalParams)
  }

  Map updateEquipmentEntry(Map input, def equipmentId, def entryId, Map additionalParams = [:]) {
    return updateEquipmentEntry(equipmentId, entryId, input, additionalParams)
  }

  Map putCustomerEquipment(def customerId, Map input, Map additionalParams = [:]) {
    def equipmentId = input.equipmentId
    input.remove('equipmentId')

    if (equipmentId) {
      return updateCustomerEquipment(customerId, equipmentId, input, additionalParams)
    } else {
      return createCustomerEquipment(customerId, input, additionalParams)
    }
  }

  Map putCustomerEquipment(Map input, Map additionalParams = [:]) {
    def customerId = input.customerId
    input.remove('customerId')
    return putCustomerEquipment(customerId, input, additionalParams)
  }

  Map putEquipmentEntry(def equipmentId, Map input, Map additionalParams = [:]) {
    def entryId = input.entryId ?: input.objectId
    input.remove('objectId')

    if (entryId) {
      return updateEquipmentEntry(equipmentId, entryId, input, additionalParams)
    } else {
      return createEquipmentEntry(equipmentId, input, additionalParams)
    }
  }

  Map putEquipmentEntry(Map input, Map additionalParams = [:]) {
    def equipmentId = input.equipmentId
    input.remove('equipmentId')

    return putEquipmentEntry(equipmentId, input, additionalParams)
  }

  Boolean deleteCustomerEquipment(def customerId, def equipmentId) {
    return deleteEntity(getCustomerEquipmentEntityType(customerId), equipmentId)
  }

  Boolean deleteEquipmentEntry(def equipmentId, def entryId) {
    return deleteEntity(getEquipmentEntryEntityType(equipmentId), entryId)
  }
}