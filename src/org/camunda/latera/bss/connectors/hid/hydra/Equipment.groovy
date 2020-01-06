package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Oracle.encodeFlag
import static org.camunda.latera.bss.utils.Oracle.encodeBool
import static org.camunda.latera.bss.utils.Oracle.decodeBool
trait Equipment {
  private static String OBJECTS_TABLE                = 'SI_V_OBJECTS'
  private static String EQUIPMENT_COMPONENTS_TABLE   = 'SI_V_OBJECTS_SPEC'
  private static String EQUIPMENT_BINDS_TABLE        = 'SI_V_OBJ_OBJECTS_BINDS'
  private static String EQUIPMENT_ADD_PARAMS_TABLE   = 'SI_V_OBJ_VALUES'
  private static String OBJECTS_MV                   = 'SI_V_OBJECTS'
  private static String EQUIPMENT_ADD_PARAMS_MV      = 'SI_V_OBJ_VALUES'
  private static String ENTITY_TYPE_OBJECT           = 'ENTITY_TYPE_Object'
  private static String EQUIPMENT_STATE_ACTUAL       = 'OBJ_STATE_Active'
  private static String EQUIPMENT_STATE_NOT_ACTIVE   = 'OBJ_STATE_NotActive'
  private static String EQUIPMENT_STATE_REGISTER_OFF = 'OBJ_STATE_RegisterOff'

  String getObjectsTable() {
    return OBJECTS_TABLE
  }

  String getEquipmentTable() {
    return getObjectsTable()
  }

  String getEquipmentComponentsTable() {
    return EQUIPMENT_COMPONENTS_TABLE
  }

  String getEquipmentBindsTable() {
    return EQUIPMENT_BINDS_TABLE
  }

  String getEquipmentAddParamsTable() {
    return EQUIPMENT_ADD_PARAMS_TABLE
  }

  String getObjectsMV() {
    return OBJECTS_MV
  }

  String getEquipmentMV() {
    return getObjectsMV()
  }

  String getEquipmentEntityType() {
    return ENTITY_TYPE_OBJECT
  }

  Number getEquipmentEntityTypeId() {
    return getRefIdByCode(getEquipmentEntityType())
  }

  String getEquipmentAddParamsMV() {
    return EQUIPMENT_ADD_PARAMS_MV
  }

  String getEquipmentStateActual() {
    return EQUIPMENT_STATE_ACTUAL
  }

  Number getEquipmentStateActualId() {
    return getRefIdByCode(getEquipmentStateActual())
  }

  String getEquipmentStateNotActive() {
    return EQUIPMENT_STATE_NOT_ACTIVE
  }

  Number getEquipmentStateNotActiveId() {
    return getRefIdByCode(getEquipmentStateNotActive())
  }

  String getEquipmentStateRegisterOff() {
    return EQUIPMENT_STATE_REGISTER_OFF
  }

  Number getEquipmentStateRegisterOffId() {
    return getRefIdByCode(getEquipmentStateRegisterOff())
  }

  Map getObject(def objectId) {
    LinkedHashMap where = [
      n_object_id: objectId
    ]
    return hid.getTableFirst(getObjectsTable(), where: where)
  }

  Map getEquipment(def equipmentId) {
    return getObject(equipmentId)
  }

  List getObjectsBy(Map input) {
    LinkedHashMap params = mergeParams([
      equipmentId : null,
      typeId      : null,
      ownerId     : null,
      stateId     : getEquipmentStateActualId(),
      code        : null,
      name        : null,
      extCode     : null,
      serialNo    : null,
      invNo       : null,
      limit       : 0
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
    return hid.getTableData(getObjectsTable(), where: where, order: params.order, limit: params.limit)
  }

  Map getObjectBy(Map input) {
    return getObjectsBy(input + [limit: 1])?.getAt(0)
  }

  Map getObjectByCode(CharSequence code) {
    return getObjectBy(code: code)
  }

  Map getObjectByName(CharSequence name) {
    return getObjectBy(name: name)
  }

  Number getObjectIdByCode(CharSequence code) {
    return toIntSafe(getObjectByCode(code)?.n_object_id)
  }

  Number getObjectIdByName(CharSequence name) {
    return toIntSafe(getObjectByName(name)?.n_object_id)
  }

  Boolean isObject(CharSequence entityOrEntityType) {
    return getObjectByCode(entityOrEntityType) != null || getObjectByName(entityOrEntityType) != null || entityOrEntityType == getObjectEntityType() || entityOrEntityType in [getObjectGoodKind(), getNetServiceKind()]
  }

  Boolean isObject(def entityIdOrEntityTypeId) {
    return getObject(entityIdOrEntityTypeId) != null || entityIdOrEntityTypeId == getObjectEntityTypeId() || toIntSafe(getGood(entityIdOrEntityTypeId)?.n_good_kind_id) in [getObjectGoodKindId(), getNetServiceKindId()]
  }

  List getCustomerObjects(def customerId, Map input = [:]) {
    return getObjectBy(input + [ownerId: customerId])
  }

  Map getCustomerObjects(Map input, def customerId) {
    return getCustomerObjects(customerId, input)
  }

  List getEquipmentsBy(Map input) { //Ew...
    return getObjectsBy(input)
  }

  Map getCustomerEquipments(def customerId, Map input = [:]) {
    return getCustomerObjects(customerId, input)
  }

  Map getCustomerEquipments(Map input, def customerId) {
    return getCustomerEquipments(customerId, input)
  }

  Map getEquipmentBy(Map input) {
    return getObjectBy(input)
  }

  Map getCustomerEquipment(def customerId, def equipmentId) {
    return getEquipmentBy(ownerId: customerId, equipmentId: equipmentId)
  }

  Map putEquipment(Map input) {
    LinkedHashMap defaultParams = [
      equipmentId : null,
      typeId      : null,
      ownerId     : null,
      code        : null,
      name        : null,
      extCode     : null,
      serialNo    : null,
      invNo       : null,
      addressId   : null,
      ip          : null,
      createIp    : false,
      mac         : null,
      bindMainId  : null,
      bindRoleId  : null
    ]
    try {
      if (input.equipmentId) {
        LinkedHashMap equipment = getEquipment(input.equipmentId)
        defaultParams += [
          equipmentId : equipment.n_object_id,
          typeId      : equipment.n_good_id,
          ownerId     : equipment.n_owner_id,
          stateId     : equipment.n_obj_state_id,
          code        : equipment.vc_code,
          name        : equipment.vc_name,
          extCode     : equipment.vc_code_add,
          serialNo    : equipment.vc_serial,
          invNo       : equipment.vc_inv_no,
          rem         : equipment.vc_rem,
          bindMainId  : equipment.n_main_object_id
        ]
      }
      LinkedHashMap params = mergeParams(defaultParams, input)

      if (input.equipmentId) {
        logger.info("Updating equipment with params ${params}")
        LinkedHashMap result = hid.execute('SI_OBJECTS_PKG.SI_OBJECTS_PUT', [
          num_N_OBJECT_ID        : equipment.equipmentId,
          num_N_GOOD_ID          : equipment.typeId ?: params.goodId,
          vch_VC_NAME            : equipment.name,
          vch_VC_CODE            : equipment.code,
          num_N_FIRM_ID          : equipment.firmId,
          vch_VC_CODE_ADD        : equipment.extCode,
          vch_VC_REM             : equipment.rem,
          vch_VC_SERIAL          : equipment.serialNo,
          vch_VC_INV_NO          : equipment.invNo,
          num_N_OWNER_ID         : equipment.ownerId,
          num_N_MAIN_OBJECT_ID   : equipment.bindMainId
        ])
        logger.info("   Equipment id ${equipment.num_N_OBJECT_ID} was updated successfully!")
        return result
      } else {
        logger.info("Creating equipment with params ${params}")
        LinkedHashMap result = hid.execute('SI_USERS_PKG.CREATE_NET_DEVICE', [
          num_N_OBJECT_ID        : params.equipmentId,
          num_N_GOOD_ID          : params.typeId ?: params.goodId,
          num_N_USER_ID          : params.ownerId,
          vch_VC_CODE            : params.code,
          vch_VC_NAME            : params.name,
          num_N_ADDRESS_ID       : params.addressId,
          vch_VC_IP              : params.ip,
          b_CREATE_IP            : encodeFlag(params.createIp),
          vch_VC_MAC             : params.mac,
          num_N_BIND_MAIN_OBJ_ID : params.bindMainId,
          num_N_OBJ_ROLE_ID      : params.bindRoleId
        ])
        logger.info("   Equipment id ${result.num_N_OBJECT_ID} was created successfully!")

        if (params.extCode || params.serialNo || params.invNo) {
          params.equipmentId = result.num_N_OBJECT_ID
          return updateEquipment(params)
        }
        return result
      }
    } catch (Exception e){
      logger.error("   Error while putting new equipment!")
      logger.error_oracle(e)
      return null
    }
  }

  Map createEquipment(Map input) {
    input.remove('equipmentId')
    return putEquipment(input)
  }

  Map createCustomerEquipment(def customerId, Map input) {
    return createEquipment(input + [ownerId: customerId])
  }

  Map createCustomerEquipment(Map input, def customerId) {
    return createCustomerEquipment(customerId, input)
  }

  Map updateEquipment(Map input) {
    return putEquipment(input)
  }

  Map updateEquipment(def equipmentId, Map input) {
    return updateEquipment(input + [equipmentId: equipmentId])
  }

  Map updateEquipment(Map input, def equipmentId) {
    return updateEquipment(equipmentId, input)
  }

  Map putCustomerEquipment(Map input) {
    def customerId = input.customerId ?: input.ownerId
    input.remove('customerId')
    input.remove('ownerId')
    return putEquipment(input + [ownerId: customerId])
  }

  Map putCustomerEquipment(def customerId, Map input) {
    return putCustomerEquipment(input + [customerId: customerId])
  }

  Map putCustomerEquipment(Map input, def customerId) {
    return putCustomerEquipment(customerId, input)
  }

  Map updateCustomerEquipment(def customerId, def equipmentId, Map input) {
    return putCustomerEquipment(customerId, input + [equipmentId: equipmentId])
  }

  Map updateCustomerEquipment(Map input, def customerId, def equipmentId) {
    return updateCustomerEquipment(customerId, equipmentId, input)
  }

  List getEquipmentComponentsBy(Map input) {
    LinkedHashMap params = mergeParams([
      componentId     : null,
      equipmentId     : null,
      typeId          : null,
      componentTypeId : null,
      ownerId         : null,
      stateId         : getEquipmentStateActualId(),
      code            : null,
      name            : null,
      limit           : 0
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
    if (params.componentTypeId || params.goodSpecId) {
      where.n_good_spec_id = params.componentTypeId || params.goodSpecId
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
    return hid.getTableData(getEquipmentComponentsTable(), where: where, order: params.order, limit: params.limit)
  }

  Map getEquipmentComponentBy(Map input) {
    return getEquipmentComponentsBy(input + [limit: 1])?.getAt(0)
  }

  List getEquipmentEntries(def equipmentId, Map input = [:]) {
    return getEquipmentComponentsBy(input + [equipmentId: equipmentId])
  }

  List getEquipmentEntries(Map input, def equipmentId) {
    return getEquipmentEntries(equipmentId, input)
  }

  Map getEquipmentComponent(def componentId) {
    return getEquipmentComponentBy(componentId: componentId)
  }

  Map getEquipmentEntry(def equipmentId, def entryId) {
    return getEquipmentComponentBy(equipmentId: equipmentId, componentId: entryId)
  }

  Map putEquipmentComponent(Map input) {
    LinkedHashMap defaultParams = [
      equipmentId : null,
      componentId : null,
      typeId      : null,
      ownerId     : null,
      stateId     : getEquipmentStateActualId(),
      code        : null,
      name        : null,
      extCode     : null,
      serialNo    : null,
      invNo       : null
    ]
    try {
      LinkedHashMap params = mergeParams(defaultParams, input)
      if (params.typeId == null) {
        LinkedHashMap equipment = getEquipment(params.equipmentId)
        LinkedHashMap componentGood = getGoodBy(baseGoodId: equipment?.n_good_id)
        params.typeId = componentGood?.n_good_id
      }

      if (params.entryId || params.componentId || params.equipmentComponentId) {
        params.bindMainId  = params.equipmentId
        params.equipmentId = params.entryId ?: params.componentId ?: params.equipmentComponentId
        input.remove('entryId')
        input.remove('equipmentComponentId')
        return updateEquipment(params)
      }

      logger.info("Creating equipment component with params ${params}")
      LinkedHashMap result = hid.execute('US_SPECIAL_PKG.ADD_ONE_OBJECT_SPEC', [
        num_N_SPEC_OBJECT_ID : null,
        num_N_OBJECT_ID      : params.equipmentId,
        num_N_GOOD_SPEC_ID   : params.typeId ?: params.goodId,
        num_N_OWNER_ID       : params.ownerId,
        num_N_OBJ_STATE_ID   : params.stateId,
        vch_VC_CODE          : params.code,
        vch_VC_NAME          : params.name
      ])
      logger.info("   Component id ${result.num_N_SPEC_OBJECT_ID} was created successfully!")

      if (params.extCode || params.serialNo || params.invNo) {
        params.componentId = result.num_N_SPEC_OBJECT_ID
        return updateEquipmentComponent(params)
      }
      return result
    } catch (Exception e){
      logger.error("   Error while creating new component!")
      logger.error_oracle(e)
      return null
    }
  }

  Map createEquipmentComponent(Map input) {
    input.remove('entryId')
    input.remove('componentId')
    input.remove('equipmentComponentId')
    return putEquipmentComponent(input)
  }

  Map createEquipmentComponent(def equipmentId, Map input) {
    return createEquipmentComponent(input + [equipmentId: equipmentId])
  }

  Map createEquipmentComponent(Map input, def equipmentId) {
    return createEquipmentComponent(equipmentId, input)
  }

  Map updateEquipmentComponent(Map input) {
    return putEquipmentComponent(input)
  }

  Map updateEquipmentComponent(def equipmentId, def componentId, Map input) {
    return updateEquipmentComponent(input + [equipmentId: equipmentId, componentId: componentId])
  }

  Map updateEquipmentComponent(Map input, def equipmentId, def componentId) {
    return updateEquipmentComponent(equipmentId, componentId, input)
  }

  Map putEquipmentEntry(Map input) {
    return putEquipmentComponent(input)
  }

  Map putEquipmentEntry(def equipmentId, Map input) {
    return putEquipmentEntry(input + [equipmentId: equipmentId])
  }

  Map createEquipmentEntry(Map input) {
    input.remove('entryId')
    input.remove('componentId')
    input.remove('equipmentComponentId')
    return putEquipmentEntry(input)
  }

  Map createEquipmentEntry(def equipmentId, Map input) {
    return createEquipmentEntry(input + [equipmentId: equipmentId])
  }

  Map createEquipmentEntry(Map input, def equipmentId) {
    return createEquipmentComponent(input, equipmentId)
  }

  Map updateEquipmentEntry(Map input) {
    return putEquipmentEntry(input)
  }

  Map updateEquipmentEntry(def equipmentId, def entryId, Map input) {
    return updateEquipmentEntry(input + [equipmentId: equipmentId, componentId: entryId])
  }

  Map updateEquipmentEntry(Map input, def equipmentId, def entryId) {
    return updateEquipmentEntry(input, equipmentId, entryId)
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

  Boolean deleteCustomerEquipment(def customerId, def equipmentId) {
    return deleteEquipment(equipmentId)
  }

  Boolean deleteComponent(def componentId) {
    return deleteEquipment(componentId)
  }

  Boolean deleteEquipmentComponent(def componentId) {
    return deleteComponent(componentId)
  }

  Boolean deleteEquipmentEntry(def equipmentId, def entryId) {
    return deleteComponent(entryId)
  }

  Map getEquipmentAddParamType(def paramId) {
    return getGoodAddParamType(paramId)
  }

  List getEquipmentAddParamTypesBy(Map input) {
    return getGoodAddParamTypesBy(input)
  }

  Map getEquipmentAddParamTypeBy(Map input) {
    return getGoodAddParamTypeBy(input)
  }

  Map getEquipmentAddParamTypeByCode(CharSequence code) {
    return getEquipmentAddParamTypeBy(code: code)
  }

  Map getObjectAddParamType(def paramId) {
    return getEquipmentAddParamType(paramId)
  }

  List getObjectAddParamTypesBy(Map input) {
    return getEquipmentAddParamTypesBy(input)
  }

  Map getObjectAddParamTypeBy(Map input) {
    return getEquipmentAddParamTypeBy(input)
  }

  Map getObjectAddParamTypeByCode(CharSequence code) {
    return getEquipmentAddParamTypeByCode(code)
  }

  Map prepareEquipmentAddParam(Map input) {
    LinkedHashMap param = null
    if (input.containsKey('param')) {
      param = getEquipmentAddParamTypeByCode(input.param.toString())
      input.paramId = param?.n_good_value_type_id
      input.remove('param')
    } else if (input.containsKey('paramId')) {
      param = getEquipmentAddParamType(input.paramId)
    }
    input.isMultiple = decodeBool(param.c_fl_multi)

    if (input.containsKey('value')) {
      def (valueType, val) = getAddParamDataType(param, input.value)
      input."${valueType}" = val
      input.remove('value')
    }
    return input
  }

  List getEquipmentAddParamsBy(Map input) {
    LinkedHashMap params = mergeParams([
      objValueId  : null,
      equipmentId : null,
      paramId     : null,
      date        : null,
      string      : null,
      number      : null,
      bool        : null,
      refId       : null,
      limit       : 0
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
      where.c_fl_value = encodeBool(params.bool)
    }
    if (params.refId) {
      where.n_ref_id = params.refId
    }
    return hid.getTableData(getEquipmentAddParamsTable(), where: where, order: params.order, limit: params.limit)
  }

  Map getEquipmentAddParamBy(Map input) {
    return getEquipmentAddParamsBy(input + [limit: 1])?.getAt(0)
  }

  Map putEquipmentAddParam(Map input) {
    LinkedHashMap params = mergeParams([
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
        ch_C_FL_VALUE            : encodeBool(params.bool),
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

  Map putEquipmentComponentAddParam(Map input) {
    def componentId = input.componentId ?: input.equipmentId
    input.remove('componentId')
    return putEquipmentAddParam(input + [equipmentId: componentId])
  }

  Map putObjectAddParam(Map input) {
    return putEquipmentAddParam(input)
  }

  Map addEquipmentAddParam(Map input) {
    return putEquipmentAddParam(input)
  }

  Map addEquipmentAddParam(def equipmentId, Map input) {
    return addEquipmentAddParam(input + [equipmentId: equipmentId])
  }

  Map addEquipmentAddParam(Map input, def equipmentId) {
    return addEquipmentAddParam(equipmentId, input)
  }

  Map addEquipmentComponentAddParam(Map input) {
    return putEquipmentComponentAddParam(input)
  }

  Map addEquipmentComponentAddParam(def componentId, Map input) {
    return addEquipmentComponentAddParam(input + [componentId: componentId])
  }

  Map addEquipmentComponentAddParam(Map input, def componentId) {
    return addEquipmentComponentAddParam(componentId, input)
  }

  Map addObjectAddParam(Map input) {
    def objectId = input.objectId ?: input.equipmentId
    input.remove('objectId')
    return putEquipmentAddParam(input + [equipmentId: objectId])
  }

  Map addObjectAddParam(def objectId, Map input) {
    return addObjectAddParam(input + [objectId: objectId])
  }

  Map addObjectAddParam(Map input, def objectId) {
    return addObjectAddParam(objectId, input)
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

  Boolean deleteEquipmentAddParam(Map input) {
    def objValueId = getEquipmentAddParamBy(input)?.n_obj_value_id
    return deleteEquipmentAddParam(objValueId)
  }

  Boolean deleteEquipmentComponentAddParam(def objValueId) {
    return deleteEquipmentAddParam(objValueId)
  }

  Boolean deleteEquipmentComponentAddParam(Map input) {
    def objValueId = getEquipmentComponentAddParamBy(input)?.n_obj_value_id
    return deleteEquipmentComponentAddParam(objValueId)
  }

  Boolean deleteObjectAddParam(def objValueId) {
    return deleteEquipmentAddParam(objValueId)
  }

  Boolean deleteObjectAddParam(Map input) {
    return deleteEquipmentAddParam(input)
  }

  List getEquipmentBindsBy(Map input) {
    LinkedHashMap params = mergeParams([
      bindId          : null,
      mainId          : null,
      componentId     : null,
      bindRoleId      : null,
      bindMainId      : null,
      bindComponentId : null,
      limit           : 0
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
    return hid.getTableData(getEquipmentBindsTable(), where: where, order: params.order, limit: params.limit)
  }

  Map getEquipmentBindBy(Map input) {
    return getEquipmentBindsBy(input + [limit: 1])?.getAt(0)
  }

  Map putEquipmentBind(Map input) {
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

  Map addEquipmentBind(Map input) {
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

  Boolean refreshObjects(CharSequence method = 'C') {
    return refreshMaterialView(getObjectsMV(), method)
  }

  Boolean refreshEquipment(CharSequence method = 'C') {
    return refreshObjects(method)
  }

  Boolean refreshEquipmentAddParams(CharSequence method = 'C') {
    return refreshMaterialView(getEquipmentAddParamsMV(), method)
  }
}