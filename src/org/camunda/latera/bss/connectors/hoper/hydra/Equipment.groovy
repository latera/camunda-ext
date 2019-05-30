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
  private static Integer EQUIPMENT_STATE_REGISTER_OFF_ID = 3040 //'OBJ_STATE_RegisterOff'

  def getEquipmentEntityType(def id = null) {
    return EQUIPMENT_ENTITY_TYPE + withParent(getObjectEntityType()) + withId(id)
  }

  def getEquipmentEntryEntityType(def equipmentId, def id = null) {
    return EQUIPMENT_ENTRY_ENTITY_TYPE + withParent(getEquipmentEntityType(equipmentId)) + withId(id)
  }

  def getCustomerEquipmentEntityType(def customerId, def id = null) {
    return CUSTOMER_EQUIPMENT_ENTITY_TYPE + withParent(getCustomerEntityType(customerId)) + withId(id)
  }

  def getEquipmentStateActualId() {
    return EQUIPMENT_STATE_ACTUAL_ID
  }

  def getEquipmentStateNotActiveId() {
    return EQUIPMENT_STATE_NOT_ACTIVE_ID
  }

  def getEquipmentStateRegisterOffId() {
    return EQUIPMENT_STATE_REGISTER_OFF_ID
  }

  LinkedHashMap getEquipmentDefaultParams() {
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

  LinkedHashMap getEquipmentEntryDefaultParams() {
    return getEquipmentDefaultParams() + [
      mainObjectId    : null,
      firmId          : getFirmId(),
      stateId         : getEquipmentStateActualId()
    ]
  }

  LinkedHashMap getEquipmentParamsMap(LinkedHashMap params, LinkedHashMap additionalParams = [:]) {
    def result =  [
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

  LinkedHashMap getEquipmentEntryParamsMap(LinkedHashMap params, LinkedHashMap additionalParams = [:]) {
    def result =  [
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

  LinkedHashMap getEquipmentParams(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    def params = getEquipmentDefaultParams() + input
    def data   = getEquipmentParamsMap(params, additionalParams)
    return nvlParams(data)
  }

  LinkedHashMap getEquipmentEntryParams(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    def params = getEquipmentEntryDefaultParams() + input
    def data   = getEquipmentEntryParamsMap(params, additionalParams)
    return nvlParams(data)
  }

  List getCustomerObjects(def customerId, LinkedHashMap input = [:]) {
    def params = getPaginationDefaultParams() + input
    return getEntities(getCustomerEquipmentEntityType(customerId), params)
  }

  LinkedHashMap getCustomerEquipments(def customerId) {
    return getCustomerObjects(customerId)
  }

  LinkedHashMap getCustomerEquipment(def customerId, def equipmentId) {
    return getEntity(getCustomerEquipmentEntityType(customerId), equipmentId)
  }

  List getEquipmentEntries(def equipmentId, LinkedHashMap input = [:]) {
    def params = getPaginationDefaultParams() + input
    return getEntities(getEquipmentEntryEntityType(equipmentId), params)
  }

  LinkedHashMap getEquipmentEntry(def equipmentId, def entryId) {
    return getEntity(getEquipmentEntryEntityType(equipmentId), entryId)
  }

  LinkedHashMap createCustomerEquipment(def customerId, LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    LinkedHashMap params = getEquipmentParams(input, additionalParams)
    return createEntity(getCustomerEquipmentEntityType(customerId), params)
  }

  LinkedHashMap createEquipmentEntry(def equipmentId, LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    LinkedHashMap params = getEquipmentEntryParams(input + [mainObjectId: equipmentId], additionalParams)
    return createEntity(getEquipmentEntryEntityType(equipmentId), params)
  }

  LinkedHashMap updateCustomerEquipment(def customerId, def equipmentId, LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    LinkedHashMap params = getEquipmentParams(input, additionalParams)
    return updateEntity(getCustomerEquipmentEntityType(customerId), equipmentId, params)
  }

  LinkedHashMap updateEquipmentEntry(def equipmentId, def entryId, LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    LinkedHashMap params = getEquipmentEntryParams(input, additionalParams)
    return updateEntity(getEquipmentEntryEntityType(equipmentId), entryId, params)
  }

  LinkedHashMap putCustomerEquipment(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    def customerId = input.customerId
    input.remove('customerId')
    return putCustomerEquipment(customerId, input, additionalParams)
  }

  LinkedHashMap putCustomerEquipment(def customerId, LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    def equipmentId = input.equipmentId
    input.remove('equipmentId')

    if (equipmentId) {
      return updateCustomerEquipment(customerId, equipmentId, input, additionalParams)
    } else {
      return createCustomerEquipment(customerId, input, additionalParams)
    }
  }

  LinkedHashMap putEquipmentEntry(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    def equipmentId = input.equipmentId
    input.remove('equipmentId')

    return putEquipmentEntry(equipmentId, input, additionalParams)
  }

  LinkedHashMap putEquipmentEntry(def equipmentId, LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    def entryId = input.entryId ?: input.objectId
    input.remove('objectId')

    if (entryId) {
      return updateEquipmentEntry(equipmentId, entryId, input, additionalParams)
    } else {
      return createEquipmentEntry(equipmentId, input, additionalParams)
    }
  }

  Boolean deleteCustomerEquipment(def customerId, def equipmentId) {
    return deleteEntity(getCustomerEquipmentEntityType(customerId), equipmentId)
  }

  Boolean deleteEquipmentEntry(def equipmentId, def entryId) {
    return deleteEntity(getEquipmentEntryEntityType(equipmentId), entryId)
  }
}