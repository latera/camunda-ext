package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.utils.Oracle
trait Equipment {
  private static String OBJECTS_TABLE                = 'SI_V_OBJECTS'
  private static String EQUIPMENT_COMPONENTS_TABLE   = 'SI_V_OBJECTS_SPEC'
  private static String EQUIPMENT_BINDS_TABLE        = 'SI_V_OBJ_OBJECTS_BINDS'
  private static String EQUIPMENT_ADD_PARAMS_TABLE   = 'SI_V_OBJ_VALUES'
  private static String EQUIPMENT_STATE_ACTUAL       = 'OBJ_STATE_Active'
  private static String EQUIPMENT_STATE_NOT_ACTIVE   = 'OBJ_STATE_NotActive'
  private static String EQUIPMENT_STATE_REGISTER_OFF = 'OBJ_STATE_RegisterOff'

  def getObjectsTable() {
    return OBJECTS_TABLE
  }

  def getEquipmentTable() {
    return getObjectsTable()
  }

  def getEquipmentComponentsTable() {
    return EQUIPMENT_COMPONENTS_TABLE
  }

  def getEquipmentBindsTable() {
    return EQUIPMENT_BINDS_TABLE
  }

  def getEquipmentAddParamsTable() {
    return EQUIPMENT_ADD_PARAMS_TABLE
  }

  def getEquipmentStateActual() {
    return EQUIPMENT_STATE_ACTUAL
  }

  def getEquipmentStateActualId() {
    return getRefIdByCode(getEquipmentStateActual())
  }

  def getEquipmentStateNotActive() {
    return EQUIPMENT_STATE_NOT_ACTIVE
  }

  def getEquipmentStateNotActiveId() {
    return getRefIdByCode(getEquipmentStateNotActive())
  }

  def getEquipmentStateRegisterOff() {
    return EQUIPMENT_STATE_REGISTER_OFF
  }

  def getEquipmentStateRegisterOffId() {
    return getRefIdByCode(getEquipmentStateRegisterOff())
  }

  LinkedHashMap getObject(def objectId) {
    LinkedHashMap where = [
      n_object_id: objectId
    ]
    return hid.getTableFirst(getObjectsTable(), where: where)
  }

  LinkedHashMap getEquipment(def equipmentId) {
    return getObject(equipmentId)
  }

  List getObjectsBy(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      equipmentId : null,
      typeId      : null,
      ownerId     : null,
      stateId     : getEquipmentStateActualId(),
      code        : null,
      name        : null,
      extCode     : null,
      serialNo    : null,
      invNo       : null
    ], input)
    LinkedHashMap where = [:]
    if (params.equipmentId) {
      where.n_object_id = params.equipmentId
    }
    if (params.typeId || params.goodId) {
      where.n_good_id = params.typeId ?: params.goodId
    }
    if (params.ownerId) {
      where.n_owner_id = params.ownerId
    }
    if (params.stateId) {
      where.n_obj_state_id = params.stateId
    }
    if (params.code) {
      where.vc_code = params.code
    }
    if (params.name) {
      where.vc_name = params.name
    }
    if (params.name) {
      where.vc_name = params.name
    }
    if (params.extCode) {
      where.vc_code_add = params.extCode
    }
    if (params.serialNo) {
      where.vc_serial = params.serialNo
    }
    if (params.invNo) {
      where.vc_inv_no = params.invNo
    }
    return hid.getTableData(getObjectsTable(), where: where)
  }

  LinkedHashMap getObjectBy(LinkedHashMap input) {
    return getObjectsBy(input)?.getAt(0) 
  }

  List getEquipmentsBy(LinkedHashMap input) { //Ew...
    return getObjectsBy(input)
  }

  LinkedHashMap getEquipmentBy(LinkedHashMap input) {
    return getObjectBy(input)
  }

  List getEquipmentComponentsBy(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      componentId     : null,
      equipmentId     : null,
      typeId          : null,
      componentTypeId : null,
      ownerId         : null,
      stateId         : getEquipmentStateActualId(),
      code            : null,
      name            : null
    ], input)
    LinkedHashMap where = [:]
    if (params.componentId) {
      where.n_object_id = params.componentId
    }
    if (params.equipmentId) {
      where.n_main_object_id = params.equipmentId
    }
    if (params.typeId || params.goodId) {
      where.n_good_id = params.typeId ?: params.goodId
    }
    if (params.componentTypeId) {
      where.n_good_spec_id = params.componentTypeId
    }
    if (params.ownerId) {
      where.n_owner_id = params.ownerId
    }
    if (params.stateId) {
      where.n_obj_state_id = params.stateId
    }
    if (params.code) {
      where.vc_code = params.code
    }
    if (params.name) {
      where.vc_name = params.name
    }
    return hid.getTableData(getEquipmentComponentsTable(), where: where)
  }

  LinkedHashMap getEquipmentComponentBy(LinkedHashMap input) {
    return getEquipmentComponentsBy(input)?.getAt(0)
  }

  LinkedHashMap getEquipmentComponent(def componentId) {
    return getEquipmentComponentBy(componentId: componentId)
  }

  List getEquipmentBindsBy(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      bindId          : null,
      mainId          : null,
      componentId     : null,
      bindRoleId      : null,
      bindMainId      : null,
      bindComponentId : null
    ], input)
    LinkedHashMap where = [:]
    if (params.bindId) {
      where.n_obj_object_id = params.bindId
    }
    if (params.mainId) {
      where.n_main_object_id = params.mainId
    }
    if (params.componentId) {
      where.n_object_id = params.componentId
    }
    if (params.bindRoleId) {
      where.n_obj_role_id = params.bindRoleId
    }
    if (params.bindMainId) {
      where.n_bind_main_obj_id = params.bindMainId
    }
    if (params.bindComponentId) {
      where.n_bind_object_id = params.bindComponentId
    }
    return hid.getTableData(getEquipmentBindsTable(), where: where)
  }

  LinkedHashMap getEquipmentBindBy(LinkedHashMap input) {
    return getEquipmentBindsBy(input)?.getAt(0)
  }

  LinkedHashMap putEquipment(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      equipmentId : null,
      typeId      : null,
      ownerId     : null,
      code        : null,
      name        : null,
      addressId   : null,
      ip          : null,
      createIp    : false,
      mac         : null,
      bindMainId  : null,
      bindRoleId  : null
    ], input)
    try {
      logger.info("Putting equipment with params ${params}")
      LinkedHashMap equipment = hid.execute('SI_USERS_PKG.CREATE_NET_DEVICE', [
        num_N_OBJECT_ID        : params.equipmentId,
        num_N_GOOD_ID          : params.typeId ?: params.goodId,
        num_N_USER_ID          : params.ownerId,
        vch_VC_CODE            : params.code,
        vch_VC_NAME            : params.name,
        num_N_ADDRESS_ID       : params.addressId,
        vch_VC_IP              : params.ip,
        b_CREATE_IP            : Oracle.encodeFlag(params.createIp),
        vch_VC_MAC             : params.mac,
        num_N_BIND_MAIN_OBJ_ID : params.bindMainId,
        num_N_OBJ_ROLE_ID      : params.bindRoleId
      ])
      logger.info("   Equipment id ${equipment.num_N_OBJECT_ID} was put successfully!")
      return equipment
    } catch (Exception e){
      logger.error("   Error while putting new equipment!")
      logger.error_oracle(e)
      return null
    }
  }

  LinkedHashMap createEquipment(LinkedHashMap input) {
    input.remove('equipmentId')
    return putEquipment(input)
  }

  LinkedHashMap updateEquipment(def equipmentId, LinkedHashMap input) {
    return putEquipment(input + [equipmentId: equipmentId])
  }

  LinkedHashMap putEquipmentComponent(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      equipmentId : null,
      typeId      : null,
      ownerId     : null,
      stateId     : getEquipmentStateActualId(),
      code        : null,
      name        : null
    ], input)
    try {
      if (params.typeId == null) {
        def equipment = getEquipment(params.equipmentId)
        def componentGood = getGoodBy(baseGoodId: equipment?.n_good_id)
        params.typeId = componentGood?.n_good_id
      }
      logger.info("Putting equipment component with params ${params}")
      LinkedHashMap component = hid.execute('US_SPECIAL_PKG.ADD_ONE_OBJECT_SPEC', [
        num_N_SPEC_OBJECT_ID : null,
        num_N_OBJECT_ID      : params.equipmentId,
        num_N_GOOD_SPEC_ID   : params.typeId ?: params.goodId,
        num_N_OWNER_ID       : params.ownerId,
        num_N_OBJ_STATE_ID   : params.stateId,
        vch_VC_CODE          : params.code,
        vch_VC_NAME          : params.name
      ])
      logger.info("   Component id ${component.num_N_SPEC_OBJECT_ID} was put successfully!")
      return component
    } catch (Exception e){
      logger.error("   Error while putting new component!")
      logger.error_oracle(e)
      return null
    }
  }

  LinkedHashMap createEquipmentComponent(LinkedHashMap input) {
    input.remove('equipmentComponentId')
    return putEquipmentComponent(input)
  }

  LinkedHashMap createEquipmentComponent(def equipmentId, LinkedHashMap input) {
    input.remove('equipmentComponentId')
    return putEquipment(input + [equipmentId: equipmentId])
  }

  Boolean deleteEquipment(def equipmentId) {
    try {
      logger.info("Deleting eqipment ${equipmentId}")
      hid.execute('SI_DEVICES_PKG.SI_DEVICES_DEL', [
        num_N_OBJECT_ID: equipmentId
      ])
      logger.info("   Equipment deleted successfully!")
      return true
    } catch (Exception e){
      logger.error("  Error while deleting equipment!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean deleteComponent(def componentId) {
    return deleteEquipment(componentId)
  }

  def getEquipmentAddParamType(def paramId) {
    return getGoodAddParamType(paramId)
  }

  def getEquipmentAddParamTypesBy(LinkedHashMap input) {
    return getGoodAddParamTypesBy(input)
  }

  def getEquipmentAddParamTypeBy(LinkedHashMap input) {
    return getGoodAddParamTypeBy(input)
  }

  def getEquipmentAddParamTypeByCode(String code) {
    return getEquipmentAddParamTypeBy(code: code)
  }

  def getObjectAddParamType(def paramId) {
    return getEquipmentAddParamType(paramId)
  }

  def getObjectAddParamTypesBy(LinkedHashMap input) {
    return getEquipmentAddParamTypesBy(input)
  }

  def getObjectAddParamTypeBy(LinkedHashMap input) {
    return getEquipmentAddParamTypeBy(input)
  }

  def getObjectAddParamTypeByCode(String code) {
    return getEquipmentAddParamTypeByCode(code)
  }

  LinkedHashMap prepareEquipmentAddParam(LinkedHashMap input) {
    def param = null
    if (input.containsKey('param')) {
      param = getEquipmentAddParamTypeBy(input.param.toString())
      input.paramId = param?.n_good_value_type_id
      input.remove('param')
    } else if (input.containsKey('paramId')) {
      param = getEquipmentAddParamType(input.paramId)
    }
    input.isMultiple = Oracle.decodeBool(param.c_fl_multi)

    if (input.containsKey('value')) {
      def valueType = getAddParamDataType(param)
      input."${valueType}" = input.value
      input.remove('value')
    }
    return input
  }

  List getEquipmentAddParamsBy(LinkedHashMap input) {
    def params = mergeParams([
      objValueId  : null,
      equipmentId : null,
      paramId     : null,
      date        : null,
      string      : null,
      number      : null,
      bool        : null,
      refId       : null
    ], prepareEquipmentAddParam(input))
    LinkedHashMap where = [:]

    if (params.objValueId) {
      where.n_obj_value_id = params.objValueId
    }
    if (params.equipmentId) {
      where.n_object_id = params.equipmentId
    }
    if (params.paramId) {
      where.n_good_value_type_id = params.paramId
    }
    if (params.date) {
      where.d_value = params.date
    }
    if (params.string) {
      where.vc_value = params.string
    }
    if (params.number) {
      where.n_value = params.number
    }
    if (params.bool != null) {
      where.c_fl_value = Oracle.encodeBool(params.bool)
    }
    if (params.refId) {
      where.n_ref_id = params.refId
    }
    return hid.getTableData(getEquipmentAddParamsTable(), where: where)
  }

  LinkedHashMap getEquipmentAddParamBy(LinkedHashMap input) {
    return getEquipmentAddParamsBy(input)?.getAt(0)
  }

  LinkedHashMap putEquipmentAddParam(LinkedHashMap input) {
    def params = mergeParams([
      objValueId  : null,
      equipmentId : null,
      paramId     : null,
      date        : null,
      string      : null,
      number      : null,
      bool        : null,
      refId       : null
    ], prepareEquipmentAddParam(input))
    try {
      if (!params.objValueId && !params.isMultiple) {
        params.objValueId = getEquipmentAddParamBy(
          objectId : input.objectId,
          paramId  : input.paramId
        )?.n_obj_value_id
      }

      logger.info("${params.objValueId ? 'Putting' : 'Creating'} object additional value with params ${params}")

      def result = hid.execute('SI_OBJECTS_PKG.SI_OBJ_VALUES_PUT', [
        num_N_OBJ_VALUE_ID       : params.objValueId,
        num_N_OBJECT_ID          : params.equipmentId,
        num_N_GOOD_VALUE_TYPE_ID : params.paramId,
        dt_D_VALUE               : params.date,
        vch_VC_VALUE             : params.string,
        num_N_VALUE              : params.number,
        ch_C_FL_VALUE            : Oracle.encodeBool(params.bool),
        num_N_REF_ID             : params.refId
      ])
      logger.info("   Object additional value was ${params.objValueId ? 'put' : 'created'} successfully!")
      return result
    } catch (Exception e){
      logger.error("  Error while putting object additional value!")
      logger.error_oracle(e)
      return null
    }
  }

  LinkedHashMap putEquipmentComponentAddParam(LinkedHashMap input) {
    def componentId = input.componentId ?: input.equipmentId
    input.remove('componentId')
    return putEquipmentAddParam(input + [equipmentId: componentId])
  }

  LinkedHashMap putObjectAddParam(LinkedHashMap input) {
    return putEquipmentAddParam(input)
  }

  LinkedHashMap addEquipmentAddParam(LinkedHashMap input) {
    return putEquipmentAddParam(input)
  }

  LinkedHashMap addEquipmentAddParam(def equipmentId, LinkedHashMap input) {
    return addEquipmentAddParam(input + [equipmentId: equipmentId])
  }

  LinkedHashMap addEquipmentAddParam(LinkedHashMap input, def equipmentId) {
    return addEquipmentAddParam(equipmentId, input)
  }

  LinkedHashMap addEquipmentComponentAddParam(LinkedHashMap input) {
    return putEquipmentComponentAddParam(input)
  }

  LinkedHashMap addEquipmentComponentAddParam(def componentId, LinkedHashMap input) {
    return addEquipmentComponentAddParam(input + [componentId: componentId])
  }

  LinkedHashMap addEquipmentComponentAddParam(LinkedHashMap input, def componentId) {
    return addEquipmentComponentAddParam(componentId, input)
  }

  LinkedHashMap addObjectAddParam(LinkedHashMap input) {
    def objectId = input.objectId ?: input.equipmentId
    input.remove('objectId')
    return putEquipmentAddParam(input + [equipmentId: objectId])
  }

  LinkedHashMap addObjectAddParam(def objectId, LinkedHashMap input) {
    return addObjectAddParam(input + [objectId: objectId])
  }

  Boolean deleteEquipmentAddParam(def objValueId) {
    try {
      logger.info("Deleting object additional alue id ${objValueId}")
      hid.execute('SI_OBJECTS_PKG.SI_OBJ_VALUES_DEL', [
        num_N_OBJ_VALUE_ID : objValueId
      ])
      logger.info("   Object additional value was deleted successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while deleting object additional value!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean deleteEquipmentAddParam(LinkedHashMap input) {
    def objValueId = getEquipmentAddParamBy(input)?.n_obj_value_id
    return deleteEquipmentAddParam(objValueId)
  }

  Boolean deleteEquipmentComponentAddParam(def objValueId) {
    return deleteEquipmentAddParam(objValueId)
  }

  Boolean deleteEquipmentComponentAddParam(LinkedHashMap input) {
    def objValueId = getEquipmentComponentAddParamBy(input)?.n_obj_value_id
    return deleteEquipmentComponentAddParam(objValueId)
  }

  Boolean deleteObjectAddParam(def objValueId) {
    return deleteEquipmentAddParam(objValueId)
  }

  Boolean deleteObjectAddParam(LinkedHashMap input) {
    return deleteEquipmentAddParam(objValueId)
  }

  LinkedHashMap putEquipmentBind(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      bindId          : null,
      mainId          : null,
      componentId     : null,
      bindRoleId      : null,
      bindMainId      : null,
      bindComponentId : null
    ], input)
    try {
      logger.info("Putting eqipment bind role ${params.bindRoleId} between main id ${params.mainId} with component id ${params.componentId} and main id ${params.bindMainId} with component id ${params.bindComponentId}")
      LinkedHashMap bind = hid.execute('SI_OBJECTS_PKG.SI_OBJ_OBJECTS_BINDS_PUT', [
        num_N_OBJ_OBJECT_ID    : params.bindId,
        num_N_OBJ_ROLE_ID      : params.bindRoleId,
        num_N_MAIN_OBJECT_ID   : params.mainId,
        num_N_OBJECT_ID        : params.componentId,
        num_N_BIND_MAIN_OBJ_ID : params.bindMainId,
        num_N_BIND_OBJECT_ID   : params.bindComponentId
      ])
      logger.info("   Equipment bind id ${bind.num_N_OBJ_OBJECT_ID} was put successfully!")
      return bind
    } catch (Exception e){
      logger.error("  Error while putting equipment bind!")
      logger.error_oracle(e)
      return null
    }
  }

  LinkedHashMap addEquipmentBind(LinkedHashMap input) {
    return putEquipmentBind(input)
  }

  Boolean deleteEquipmentBind(def bindId) {
    try {
      logger.info("Deleting eqipment bind id ${bindId}")
      hid.execute('SI_OBJECTS_PKG.SI_OBJ_OBJECTS_DEL', [
        num_N_OBJ_OBJECT_ID : bindId
      ])
      logger.info("   Equipment bind deleted successfully!")
      return true
    } catch (Exception e){
      logger.error("  Error while deleting equipment bind!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean changeEquipmentState(def equipmentId, def stateId) {
    try {
      logger.info("Changing equipment ${equipmentId} state to ${stateId}")
      hid.execute('SI_OBJECTS_PKG.CHANGE_STATE', [
        num_N_OBJECT_ID    : equipmentId,
        num_N_OBJ_STATE_ID : stateId
      ])
      logger.info("   Equipment state was changed successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while changing equipment state!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean actualizeEquipment(def equipmentId) {
    return changeEquipmentState(equipmentId, getEquipmentStateActualId())
  }

  Boolean deactivateEquipment(def equipmentId) {
    return changeEquipmentState(equipmentId, getEquipmentStateNotActiveId())
  }

  Boolean unregisterEquipment(def equipmentId) {
    return changeEquipmentState(equipmentId, getEquipmentStateRegisterOffId())
  }
}