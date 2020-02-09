package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Oracle.encodeFlag
import static org.camunda.latera.bss.utils.Oracle.encodeBool
import static org.camunda.latera.bss.utils.Oracle.decodeBool
import static org.camunda.latera.bss.utils.StringUtil.notEmpty
import static org.camunda.latera.bss.utils.StringUtil.isEmpty
import static org.camunda.latera.bss.utils.Constants.ENTITY_TYPE_Object
import static org.camunda.latera.bss.utils.Constants.OBJ_STATE_Active
import static org.camunda.latera.bss.utils.Constants.OBJ_STATE_NotActive
import static org.camunda.latera.bss.utils.Constants.OBJ_STATE_RegisterOff

trait Equipment {
  private static String OBJECTS_TABLE                = 'SI_V_OBJECTS'
  private static String EQUIPMENT_COMPONENTS_TABLE   = 'SI_V_OBJECTS_SPEC'
  private static String EQUIPMENT_BINDS_TABLE        = 'SI_V_OBJ_OBJECTS_BINDS'
  private static String EQUIPMENT_ADD_PARAMS_TABLE   = 'SI_V_OBJ_VALUES'
  private static String OBJECTS_MV                   = 'SI_V_OBJECTS'
  private static String EQUIPMENT_ADD_PARAMS_MV      = 'SI_V_OBJ_VALUES'

  /**
   * Get objects table name
   */
  String getObjectsTable() {
    return OBJECTS_TABLE
  }

  /**
   * Get equipment table name
   */
  String getEquipmentTable() {
    return getObjectsTable()
  }

  /**
   * Get equipment components table name
   */
  String getEquipmentComponentsTable() {
    return EQUIPMENT_COMPONENTS_TABLE
  }

  /**
   * Get equipment binds table name
   */
  String getEquipmentBindsTable() {
    return EQUIPMENT_BINDS_TABLE
  }

  /**
   * Get equipment add param values table name
   */
  String getEquipmentAddParamsTable() {
    return EQUIPMENT_ADD_PARAMS_TABLE
  }

  /**
   * Get object quick search material view name
   */
  String getObjectsMV() {
    return OBJECTS_MV
  }

  /**
   * Get equipment quick search material view name
   */
  String getEquipmentMV() {
    return getObjectsMV()
  }

  /**
   * Get equipment add params quick search material view name
   */
  String getEquipmentAddParamsMV() {
    return EQUIPMENT_ADD_PARAMS_MV
  }

  /**
   * Get equipment entity type ref code
   */
  String getEquipmentEntityType() {
    return getRefCode(getEquipmentEntityTypeId())
  }

  /**
   * Get equipment entity type ref id
   */
  Number getEquipmentEntityTypeId() {
    return ENTITY_TYPE_Object
  }

  /**
   * Get equipment actual state ref code
   */
  String getEquipmentStateActual() {
    return getRefCode(getEquipmentStateActualId())
  }

  /**
   * Get equipment actual state ref id
   */
  Number getEquipmentStateActualId() {
    return OBJ_STATE_Active
  }

  /**
   * Get equipment not active state ref code
   */
  String getEquipmentStateNotActive() {
    return getRefCode(getEquipmentStateNotActiveId())
  }

  /**
   * Get equipment not active state ref id
   */
  Number getEquipmentStateNotActiveId() {
    return OBJ_STATE_NotActive
  }

  /**
   * Get equipment unregistered state ref code
   */
  String getEquipmentStateRegisterOff() {
    return getRefCode(getEquipmentStateRegisterOffId())
  }

  /**
   * Get equipment unregistered state ref id
   */
  Number getEquipmentStateRegisterOffId() {
    return OBJ_STATE_RegisterOff
  }

  /**
   * Get object by id
   * @param objectId {@link java.math.BigInteger BigInteger}
   * @return Map with object table row or null
   */
  Map getObject(def objectId) {
    LinkedHashMap where = [
      n_object_id: objectId
    ]
    return hid.getTableFirst(getObjectsTable(), where: where)
  }

  /**
   * Get equipment by id
   * @param equipmentId {@link java.math.BigInteger BigInteger}
   * @return Map with equipment table row or null
   */
  Map getEquipment(def equipmentId) {
    return getObject(equipmentId)
  }

  /**
   * Search for objects by different fields value
   * @param objectId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param equipmentId Alias fo 'objectId'
   * @param typeId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodId      Alias fo 'typeId'
   * @param ownerId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param extCode     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param serialNo    {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param invNo       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param stateId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: actual
   * @param state       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit       {@link Integer}. Optional, default: 0 (unlimited)
   * @param order       {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: D_BEGIN ASC, VC_DOC_NO DESC
   * @return List[Map] of object table rows
   */
  List getObjectsBy(Map input) {
    LinkedHashMap params = mergeParams([
      objectId    : null,
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
    if (params.objectId || params.equipmentId) {
      where.n_object_id = params.objectId ?: params.equipmentId
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

  /**
   * Search for object by different fields value
   * @param objectId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param equipmentId Alias fo 'objectId'
   * @param typeId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodId      Alias fo 'typeId'
   * @param ownerId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param extCode     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param serialNo    {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param invNo       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param stateId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: actual
   * @param state       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param order       {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: D_BEGIN ASC, VC_DOC_NO DESC
   * @return Map with object table row
   */
  Map getObjectBy(Map input) {
    return getObjectsBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Check if entity or entity type is object
   * @param entityOrEntityType {@link java.math.BigInteger BigInteger} or {@link CharSequence String}. Entity id, entity type ref id or entity type ref code
   * @return True if given value is object, false otherwise
   */
  Boolean isObject(def entityOrEntityType) {
    if (entityOrEntityType == null) {
      return false
  }

    Number entityIdOrEntityTypeId = toIntSafe(entityOrEntityType)
    if (entityIdOrEntityTypeId != null) {
      return getObject(entityIdOrEntityTypeId) != null || entityIdOrEntityTypeId == getObjectEntityTypeId() || toIntSafe(getGood(entityIdOrEntityTypeId)?.n_good_kind_id) in [getObjectGoodKindId(), getNetServiceKindId()]
    } else {
      return entityOrEntityType == getObjectEntityType() || entityOrEntityType in [getObjectGoodKind(), getNetServiceKind()]
    }
  }

  /**
   * Search for equipment by different fields value
   * @see #getObjectBy(Map)
   */
  Map getEquipmentBy(Map input) {
    return getObjectBy(input)
  }

  /**
   * Get customer objects
   * @param customerId  d{@link java.math.BigInteger BigInteger}
   * @param objectId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param equipmentId Alias fo 'objectId'
   * @param typeId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodId      Alias fo 'typeId'
   * @param code        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param extCode     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param serialNo    {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param invNo       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param stateId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: actual
   * @param state       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit       {@link Integer}. Optional, default: 0 (unlimited)
   * @param order       {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: D_BEGIN ASC, VC_DOC_NO DESC
   * @return List[Map] of object table rows
   */
  List getCustomerObjects(Map input = [:], def customerId) {
    return getObjectsBy(input + [ownerId: customerId])
  }

  /**
   * Get customer object
   * @param customerId  {@link java.math.BigInteger BigInteger}
   * @param objectId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param equipmentId Alias fo 'objectId'
   * @param typeId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodId      Alias fo 'typeId'
   * @param code        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param extCode     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param serialNo    {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param invNo       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param stateId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: actual
   * @param state       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param order       {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: D_BEGIN ASC, VC_DOC_NO DESC
   * @return Map with  object table row
   */
  Map getCustomerObject(Map input = [:], def customerId) {
    return getObjectBy(input + [ownerId: customerId])
  }

  /**
   * Create or update equipment
   * @param equipmentId {@link java.math.BigInteger BigInteger}. Optional
   * @param objectId    Alias for 'equipmentId'
   * @param typeId      {@link java.math.BigInteger BigInteger}. Optional
   * @param goodId      Alias for 'typeId'
   * @param ownerId     {@link java.math.BigInteger BigInteger}. Optional
   * @param code        {@link CharSequence String}. Optional
   * @param name        {@link CharSequence String}. Optional
   * @param extCode     {@link CharSequence String}. Optional
   * @param serialNo    {@link CharSequence String}. Optional
   * @param invNo       {@link CharSequence String}. Optional
   * @param addressId   {@link java.math.BigInteger BigInteger}. Optional
   * @param createIp    {@link Boolean}. Optional, default: false
   * @param mac         {@link CharSequence String}. Optional
   * @param bindMainId  {@link java.math.BigInteger BigInteger}. Optional
   * @param bindRoleId  {@link java.math.BigInteger BigInteger}. Optional
   * @param bindRole    {@link CharSequence String}. Optional
   * @return Map with created or updated equipment (in Oracle API procedure notation)
   */
  private Map putEquipment(Map input) {
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
      LinkedHashMap existingEquipment = [:]
      if (notEmpty(input.equipmentId)) {
        LinkedHashMap equipment = getEquipment(input.equipmentId)
        existingEquipment = [
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
      LinkedHashMap params = mergeParams(defaultParams, existingEquipment + input)

      if (isEmpty(params.equipmentId) && notEmpty(params.ownerId)) {
        logger.info("Creating equipment with params ${params}")
        LinkedHashMap result = hid.execute('SI_USERS_PKG.CREATE_NET_DEVICE', [
          num_N_OBJECT_ID        : params.equipmentId ?: params.objectId,
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
      } else {
        logger.info("Updating equipment with params ${params}")
        LinkedHashMap result = hid.execute('SI_OBJECTS_PKG.SI_OBJECTS_PUT', [
          num_N_OBJECT_ID        : params.equipmentId,
          num_N_GOOD_ID          : params.typeId ?: params.goodId,
          vch_VC_NAME            : params.name,
          vch_VC_CODE            : params.code,
          num_N_FIRM_ID          : params.firmId,
          vch_VC_CODE_ADD        : params.extCode,
          vch_VC_REM             : params.rem,
          vch_VC_SERIAL          : params.serialNo,
          vch_VC_INV_NO          : params.invNo,
          num_N_OWNER_ID         : params.ownerId,
          num_N_MAIN_OBJECT_ID   : params.bindMainId
        ])
        logger.info("   Equipment id ${result.num_N_OBJECT_ID} was updated successfully!")
        return result
      }
    } catch (Exception e){
      logger.error("   Error while putting new equipment!")
      logger.error_oracle(e)
      return null
    }
  }

  /**
   * Create equipment
   * @param typeId      {@link java.math.BigInteger BigInteger}
   * @param goodId      Alias for 'typeId'
   * @param ownerId     {@link java.math.BigInteger BigInteger}. Optional
   * @param code        {@link CharSequence String}. Optional
   * @param name        {@link CharSequence String}. Optional
   * @param extCode     {@link CharSequence String}. Optional
   * @param serialNo    {@link CharSequence String}. Optional
   * @param invNo       {@link CharSequence String}. Optional
   * @param addressId   {@link java.math.BigInteger BigInteger}. Optional
   * @param createIp    {@link Boolean}. Optional, default: false
   * @param mac         {@link CharSequence String}. Optional
   * @param bindMainId  {@link java.math.BigInteger BigInteger}. Optional
   * @param bindRoleId  {@link java.math.BigInteger BigInteger}. Optional
   * @param bindRole    {@link CharSequence String}. Optional
   * @return Map with created equipment (in Oracle API procedure notation)
   */
  Map createEquipment(Map input) {
    input.remove('equipmentId')
    return putEquipment(input)
  }

  /**
   * Create customer equipment
   * @param customerId  {@link java.math.BigInteger BigInteger}
   * @param typeId      {@link java.math.BigInteger BigInteger}
   * @param goodId      Alias for 'typeId'
   * @param code        {@link CharSequence String}. Optional
   * @param name        {@link CharSequence String}. Optional
   * @param extCode     {@link CharSequence String}. Optional
   * @param serialNo    {@link CharSequence String}. Optional
   * @param invNo       {@link CharSequence String}. Optional
   * @param addressId   {@link java.math.BigInteger BigInteger}. Optional
   * @param createIp    {@link Boolean}. Optional, default: false
   * @param mac         {@link CharSequence String}. Optional
   * @param bindMainId  {@link java.math.BigInteger BigInteger}. Optional
   * @param bindRoleId  {@link java.math.BigInteger BigInteger}. Optional
   * @param bindRole    {@link CharSequence String}. Optional
   * @return Map with created equipment (in Oracle API procedure notation)
   */
  Map createCustomerEquipment(Map input = [:], def customerId) {
    return createEquipment(input + [ownerId: customerId])
  }

  /**
   * Create customer equipment
   * @param equipmentId {@link java.math.BigInteger BigInteger}
   * @param code        {@link CharSequence String}. Optional
   * @param name        {@link CharSequence String}. Optional
   * @param extCode     {@link CharSequence String}. Optional
   * @param serialNo    {@link CharSequence String}. Optional
   * @param invNo       {@link CharSequence String}. Optional
   * @return Map with updated equipment (in Oracle API procedure notation)
   */
  Map updateEquipment(Map input = [:], def equipmentId) {
    return putEquipment(input + [equipmentId: equipmentId])
  }

  /**
   * Search for equipment components by different fields value
   * @param componentId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param entryId         Alias fo 'componentId'
   * @param equipmentId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param objectId        Alias fo 'equipmentId'
   * @param typeId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodId          Alias fo 'typeId'
   * @param componentTypeId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodSpecId      Alias fo 'typeId'
   * @param ownerId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param stateId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: actual
   * @param state           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit           {@link Integer}. Optional, default: 0 (unlimited)
   * @param order           {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: D_BEGIN ASC, VC_DOC_NO DESC
   * @return List[Map] of equipment component table rows
   */
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
    if (params.componentId || params.entryId) {
      where.n_object_id = params.componentId ?: parans.entryId
    }
    if (params.equipmentId || params.objectId) {
      where.n_main_object_id = params.equipmentId ?: params.objectId
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

  /**
   * Search for equipment component by different fields value
   * @param componentId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param entryId         Alias fo 'componentId'
   * @param equipmentId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param objectId        Alias fo 'equipmentId'
   * @param typeId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodId          Alias fo 'typeId'
   * @param componentTypeId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodSpecId      Alias fo 'typeId'
   * @param ownerId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param stateId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: actual
   * @param state           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param order           {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: D_BEGIN ASC, VC_DOC_NO DESC
   * @return Map with equipment component table row
   */
  Map getEquipmentComponentBy(Map input) {
    return getEquipmentComponentsBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Search for equipment components
   * @param equipmentId     {@link java.math.BigInteger BigInteger}
   * @param componentId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param typeId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodId          Alias fo 'typeId'
   * @param componentTypeId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodSpecId      Alias fo 'typeId'
   * @param ownerId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param stateId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: actual
   * @param state           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit           {@link Integer}. Optional, default: 0 (unlimited)
   * @param order           {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: D_BEGIN ASC, VC_DOC_NO DESC
   * @return List[Map] of equipment component table rows
   */
  List getEquipmentEntries(Map input = [:], def equipmentId) {
    return getEquipmentComponentsBy(input + [equipmentId: equipmentId])
  }

  /**
   * Search for equipment component
   * @param equipmentId     {@link java.math.BigInteger BigInteger}
   * @param componentId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param typeId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodId          Alias fo 'typeId'
   * @param componentTypeId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodSpecId      Alias fo 'typeId'
   * @param ownerId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param stateId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: actual
   * @param state           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param order           {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: D_BEGIN ASC, VC_DOC_NO DESC
   * @return Map with equipment component table row
   */
  Map getEquipmentComponent(def componentId) {
    return getEquipmentComponentBy(componentId: componentId)
  }

  /**
   * Create or update equipment component
   * @param componentId {@link java.math.BigInteger BigInteger}. Optional
   * @param entryId     Alias for 'componentId'
   * @param equipmentId {@link java.math.BigInteger BigInteger}. Optional
   * @param objectId    Alias for 'equipmentId'
   * @param typeId      {@link java.math.BigInteger BigInteger}. Optional
   * @param goodId      Alias for 'typeId'
   * @param ownerId     {@link java.math.BigInteger BigInteger}. Optional
   * @param code        {@link CharSequence String}. Optional
   * @param name        {@link CharSequence String}. Optional
   * @param extCode     {@link CharSequence String}. Optional
   * @param serialNo    {@link CharSequence String}. Optional
   * @param invNo       {@link CharSequence String}. Optional
   * @return Map with created or updated equipment component (in Oracle API procedure notation)
   */
  private Map putEquipmentComponent(Map input) {
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
      LinkedHashMap component = hid.execute('US_SPECIAL_PKG.ADD_ONE_OBJECT_SPEC', [
        num_N_SPEC_OBJECT_ID : null,
        num_N_OBJECT_ID      : params.equipmentId,
        num_N_GOOD_SPEC_ID   : params.typeId ?: params.goodId,
        num_N_OWNER_ID       : params.ownerId,
        num_N_OBJ_STATE_ID   : params.stateId,
        vch_VC_CODE          : params.code,
        vch_VC_NAME          : params.name
      ])
      logger.info("   Component id ${component.num_N_SPEC_OBJECT_ID} was created successfully!")

      if (notEmpty(params.extCode) || notEmpty(params.serialNo) || notEmpty(params.invNo)) {
        params.componentId = component.num_N_SPEC_OBJECT_ID
        return updateEquipmentComponent(params)
      }
      return component
    } catch (Exception e){
      logger.error("   Error while creating new component!")
      logger.error_oracle(e)
      return null
    }
  }

  /**
   * Create equipment component
   * @param equipmentId {@link java.math.BigInteger BigInteger}
   * @param typeId      {@link java.math.BigInteger BigInteger}. Optional
   * @param goodId      Alias for 'typeId'
   * @param ownerId     {@link java.math.BigInteger BigInteger}. Optional
   * @param code        {@link CharSequence String}. Optional
   * @param name        {@link CharSequence String}. Optional
   * @param extCode     {@link CharSequence String}. Optional
   * @param serialNo    {@link CharSequence String}. Optional
   * @param invNo       {@link CharSequence String}. Optional
   * @return Map with created equipment component (in Oracle API procedure notation)
   */
  Map createEquipmentComponent(Map input = [:], def equipmentId) {
    input.remove('entryId')
    input.remove('componentId')
    input.remove('equipmentComponentId')
    return putEquipmentComponent(input + [equipmentId: equipmentId])
  }

  /**
   * Update equipment component
   * @param componentId {@link java.math.BigInteger BigInteger}
   * @param ownerId     {@link java.math.BigInteger BigInteger}. Optional
   * @param code        {@link CharSequence String}. Optional
   * @param name        {@link CharSequence String}. Optional
   * @param extCode     {@link CharSequence String}. Optional
   * @param serialNo    {@link CharSequence String}. Optional
   * @param invNo       {@link CharSequence String}. Optional
   * @return Map with updated equipment component (in Oracle API procedure notation)
   */
  Map updateComponent(Map input = [:], def componentId) {
    return putEquipmentComponent(input + [componentId: componentId])
  }

  /**
   * Delete equipment
   * @param equipmentId {@link java.math.BigInteger BigInteger}
   * @return True if equipment was deleted successfully, false otherwise
   */
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

  /**
   * Delete customer equipment
   * @param customerId {@link java.math.BigInteger BigInteger}
   * @param equipmentId {@link java.math.BigInteger BigInteger}
   * @return True if equipment was deleted successfully, false otherwise
   * @deprecated use {@link #deleteEquipment(def)} instead
   */
  Boolean deleteCustomerEquipment(def customerId, def equipmentId) {
    return deleteEquipment(equipmentId)
  }

  /**
   * Delete equipment component
   * @see #deleteEquipment(def)
   * @deprecated use {@link #deleteEquipmentComponent(def)} instead
   */
  Boolean deleteComponent(def componentId) {
    return deleteEquipment(componentId)
  }

  /**
   * Delete equipment component
   * @see #deleteEquipment(def)
   */
  Boolean deleteEquipmentComponent(def componentId) {
    return deleteComponent(componentId)
  }

  /**
   * Get equipment add param type by id
   * @param goodValueTypeId {@link java.math.BigInteger BigInteger}
   * @return Map with object add param table row or null
   */
  Map getEquipmentAddParamType(def goodValueTypeId) {
    return getGoodAddParamType(goodValueTypeId)
  }

  /**
   * Search for equipment add param types by different fields value
   * @see #getGoodAddParamTypesBy(Map)
   */
  List getEquipmentAddParamTypesBy(Map input) {
    return getGoodAddParamTypesBy(input)
  }

  /**
   * Search for equipment add param type by different fields value
   * @see #getGoodAddParamTypeBy(Map)
   */
  Map getEquipmentAddParamTypeBy(Map input) {
    return getGoodAddParamTypeBy(input)
  }

  /**
   * Search for object add param type by code
   * @param code {@link CharSequence String}
   * @return Map with object add param type table row
   */
  Map getEquipmentAddParamTypeByCode(CharSequence code) {
    return getEquipmentAddParamTypeBy(code: code)
  }

  /**
   * Get object add param type by id
   * @see #getEquipmentAddParamType(def)
   */
  Map getObjectAddParamType(def objValueTypeId) {
    return getEquipmentAddParamType(objValueTypeId)
  }

  /**
   * Search for object add param types by different fields value
   * @see #getEquipmentAddParamTypesBy(Map)
   */
  List getObjectAddParamTypesBy(Map input) {
    return getEquipmentAddParamTypesBy(input)
  }

  /**
   * Search for object add param type by different fields value
   * @see #getEquipmentAddParamTypeBy(Map)
   */
  Map getObjectAddParamTypeBy(Map input) {
    return getEquipmentAddParamTypeBy(input)
  }

  /**
   * Search for object add param type by code
   * @see #getEquipmentAddParamTypeByCode(Map)
   */
  Map getObjectAddParamTypeByCode(CharSequence code) {
    return getEquipmentAddParamTypeByCode(code)
  }

  /**
   * Prepare equipment add param value to save
   * @param paramId {@link java.math.BigInteger BigInteger}. Optional if 'param' is passed
   * @param param   {@link CharSequence String}. Optional is 'paramId' is passed
   * @param value   Any type. Optional
   * @return Map with add param value
   * <pre>
   * {@code
   * [
   *   paramId : _, # good add param type id
   *   bool    : _, # if add param is boolean type
   *   number  : _, # if add param is number type
   *   string  : _, # if add param is string type
   *   date    : _, # if add param is date type
   *   refId   : _, # if add param is refId type and value can be converted to BigInteger (ref id)
   *   ref     : _  # if add param is refId type and value cannot be converted to BigInteger (ref code)
   * ]
   * }
   * </pre>
   */
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

  /**
   * Search for equipment add param values by different fields value
   * @param objValueId  {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param equipmentId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param paramId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional is 'param' is passed
   * @param param       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional is 'paramId' is passed
   * @param date        {@link java.time.Temporal Any date type}. Optional
   * @param number      {@link Double}, {@link Integer}, {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param string      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bool        {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ref         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param value       Any type which is automatically converted to 'date', 'string', 'name', 'bool' or 'refId', see {@link #prepareGoodAddParam(Map)}. Optional
   * @param limit       {@link Integer}. Optional, default: 0 (unlimited)
   * @param order       {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: [:]
   * @return List[Map] of equipment add param value table rows
   */
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

  /**
   * Search for equipment add param value by different fields value
   * @param objValueId  {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param equipmentId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param paramId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional is 'param' is passed
   * @param param       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional is 'paramId' is passed
   * @param date        {@link java.time.Temporal Any date type}. Optional
   * @param number      {@link Double}, {@link Integer}, {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param string      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bool        {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param refId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ref         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param value       Any type which is automatically converted to 'date', 'string', 'name', 'bool' or 'refId', see {@link #prepareGoodAddParam(Map)}. Optional
   * @param limit       {@link Integer}. Optional, default: 0 (unlimited)
   * @param order       {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: [:]
   * @return Map with equipment add param value table row
   */
  Map getEquipmentAddParamBy(Map input) {
    return getEquipmentAddParamsBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Create or update equipment add param value
   * @param objValueId  {@link java.math.BigInteger BigInteger}. Optional
   * @param equipmentId {@link java.math.BigInteger BigInteger}. Optional
   * @param paramId     {@link java.math.BigInteger BigInteger}. Optional
   * @param param       {@link CharSequence String}. Optional
   * @param date        {@link java.time.Temporal Any date type}. Optional
   * @param number      {@link Double}, {@link Integer}, {@link java.math.BigInteger BigInteger}. Optional
   * @param string      {@link CharSequence String}. Optional
   * @param bool        {@link Boolean}. Optional
   * @param refId       {@link java.math.BigInteger BigInteger}. Optional
   * @param ref         {@link CharSequence String}. Optional
   * @param value       Any type which is automatically converted to 'date', 'string', 'name', 'bool' or 'refId', see {@link #prepareDocumentAddParam(Map)}. Optional
   * @return Map with created or updated equipment add param value (in Oracle API procedure notation)
   */
  private Map putEquipmentAddParam(Map input) {
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
          equipmentId : input.equipmentId,
          paramId     : input.paramId
        )?.n_obj_value_id
      }

      logger.info("${params.objValueId ? 'Updating' : 'Creating'} object additional value with params ${params}")

      LinkedHashMap addParam = hid.execute('SI_OBJECTS_PKG.SI_OBJ_VALUES_PUT', [
        num_N_OBJ_VALUE_ID       : params.objValueId,
        num_N_OBJECT_ID          : params.equipmentId,
        num_N_GOOD_VALUE_TYPE_ID : params.paramId,
        dt_D_VALUE               : params.date,
        vch_VC_VALUE             : params.string,
        num_N_VALUE              : params.number,
        ch_C_FL_VALUE            : encodeBool(params.bool),
        num_N_REF_ID             : params.refId
      ])
      logger.info("   Object additional value ${addParam.num_N_OBJ_VALUE_ID} was ${params.objValueId ? 'updated' : 'created'} successfully!")
      return addParam
    } catch (Exception e){
      logger.error("  Error while ${input.objValueId ? 'updating' : 'creating'} object additional value!")
      logger.error_oracle(e)
      return null
    }
  }

  /**
   * Create or update object add param value
   * @param objectId {@link java.math.BigInteger BigInteger}
   * @param paramId  {@link java.math.BigInteger BigInteger}. Optional
   * @param param    {@link CharSequence String}. Optional
   * @param date     {@link java.time.Temporal Any date type}. Optional
   * @param number   {@link Double}, {@link Integer}, {@link java.math.BigInteger BigInteger}. Optional
   * @param string   {@link CharSequence String}. Optional
   * @param bool     {@link Boolean}. Optional
   * @param refId    {@link java.math.BigInteger BigInteger}. Optional
   * @param ref      {@link CharSequence String}. Optional
   * @param value    Any type which is automatically converted to 'date', 'string', 'name', 'bool' or 'refId', see {@link #prepareDocumentAddParam(Map)}. Optional
   * @return Map with created object add param value (in Oracle API procedure notation)
   */
  Map addObjectAddParam(Map input = [:], def objectId) {
    return putEquipmentAddParam(input + [equipmentId: objectId])
  }

  /**
   * Create or update equipment add param value
   * @param equipmentId {@link java.math.BigInteger BigInteger}
   * @param paramId     {@link java.math.BigInteger BigInteger}. Optional
   * @param param       {@link CharSequence String}. Optional
   * @param date        {@link java.time.Temporal Any date type}. Optional
   * @param number      {@link Double}, {@link Integer}, {@link java.math.BigInteger BigInteger}. Optional
   * @param string      {@link CharSequence String}. Optional
   * @param bool        {@link Boolean}. Optional
   * @param refId       {@link java.math.BigInteger BigInteger}. Optional
   * @param ref         {@link CharSequence String}. Optional
   * @param value       Any type which is automatically converted to 'date', 'string', 'name', 'bool' or 'refId', see {@link #prepareDocumentAddParam(Map)}. Optional
   * @return Map with created equipment add param value (in Oracle API procedure notation)
   */
  Map addEquipmentAddParam(Map input = [:], def equipmentId) {
    return addObjectAddParam(input, equipmentId)
  }

  /**
   * Create or update equipment component add param value
   * @param componentId {@link java.math.BigInteger BigInteger}
   * @param paramId     {@link java.math.BigInteger BigInteger}. Optional
   * @param param       {@link CharSequence String}. Optional
   * @param date        {@link java.time.Temporal Any date type}. Optional
   * @param number      {@link Double}, {@link Integer}, {@link java.math.BigInteger BigInteger}. Optional
   * @param string      {@link CharSequence String}. Optional
   * @param bool        {@link Boolean}. Optional
   * @param refId       {@link java.math.BigInteger BigInteger}. Optional
   * @param ref         {@link CharSequence String}. Optional
   * @param value       Any type which is automatically converted to 'date', 'string', 'name', 'bool' or 'refId', see {@link #prepareDocumentAddParam(Map)}. Optional
   * @return Map with created equipment component add param value (in Oracle API procedure notation)
   */
  Map addEquipmentComponentAddParam(Map input, def componentId) {
    return addObjectAddParam(input, componentId)
  }

  /**
   * Delete equipment add param value
   * @param objValueId {@link java.math.BigInteger BigInteger}
   * @return True if equipment add param value was deleted successfully, false otherwise
   */
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

  /**
   * Delete equipment add param value
   *
   * Overload for searching and deleting add param value
   * @see #getEquipmentAddParamBy(Map)
   * @see #deleteEquipmentAddParam(def)
   */
  Boolean deleteEquipmentAddParam(Map input) {
    def objValueId = getEquipmentAddParamBy(input)?.n_obj_value_id
    return deleteEquipmentAddParam(objValueId)
  }

  /**
   * Delete equipment compnent add param value
   * @see #deleteEquipmentAddParam(def)
   */
  Boolean deleteEquipmentComponentAddParam(def objValueId) {
    return deleteEquipmentAddParam(objValueId)
  }

  /**
   * Delete equipment component add param value
   *
   * Overload for searching and deleting add param value
   * @see #deleteEquipmentAddParam(Map)
   */
  Boolean deleteEquipmentComponentAddParam(Map input) {
    input.equipmentId = input.componentId
    input.remove('componentId')
    def objValueId = getEquipmentAddParamBy(input)?.n_obj_value_id
    return deleteEquipmentComponentAddParam(objValueId)
  }

  /**
   * Delete equipment compnent add param value
   * @see #deleteEquipmentAddParam(def)
   */
  Boolean deleteObjectAddParam(def objValueId) {
    return deleteEquipmentAddParam(objValueId)
  }

  /**
   * Delete object add param value
   *
   * Overload for searching and deleting add param value
   * @see #getEquipmentAddParamBy(Map)
   * @see #deleteEquipmentAddParam(def)
   */
  Boolean deleteObjectAddParam(Map input) {
    input.equipmentId = input.objectId
    input.remove('objectId')
    return deleteEquipmentAddParam(input)
  }

  /**
   * Search for equipment-equipment binds by different fields value
   * @param bindId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param mainId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param componentId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bindMainId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bindComponentId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bindRoleId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bindRole        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit           {@link Integer}. Optional, default: 0 (unlimited)
   * @param order           {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: [:]
   * @return List[Map] of equipment-equipment bind table rows
   */
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

  /**
   * Search for equipment-equipment bind by different fields value
   * @param bindId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param mainId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param componentId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bindMainId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bindComponentId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bindRoleId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bindRole        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit           {@link Integer}. Optional, default: 0 (unlimited)
   * @param order           {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: [:]
   * @return Map with equipment-equipment bind table row
   */
  Map getEquipmentBindBy(Map input) {
    return getEquipmentBindsBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Create or update equipment-equipment bind
   * @param bindId          {@link java.math.BigInteger BigInteger}. Optional
   * @param mainId          {@link java.math.BigInteger BigInteger}. Optional
   * @param componentId     {@link java.math.BigInteger BigInteger}. Optional
   * @param bindMainId      {@link java.math.BigInteger BigInteger}. Optional
   * @param bindComponentId {@link java.math.BigInteger BigInteger}. Optional
   * @param bindRoleId      {@link java.math.BigInteger BigInteger}. Optional
   * @param bindRole        {@link CharSequence String}. Optional
   * @return Map with created or updated equipment-equipment bind (in Oracle API procedure notation)
   */
  private Map putEquipmentBind(Map input) {
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

  /**
   * Create pment-equipment bind
   * @param mainId          {@link java.math.BigInteger BigInteger}
   * @param componentId     {@link java.math.BigInteger BigInteger}. Optional
   * @param bindMainId      {@link java.math.BigInteger BigInteger}
   * @param bindComponentId {@link java.math.BigInteger BigInteger}. Optional
   * @param bindRoleId      {@link java.math.BigInteger BigInteger}. Optional if 'bindRole' is passed
   * @param bindRole        {@link CharSequence String}. Optional if 'bindRoleId' is passed
   * @return Map with created equipment-equipment bind (in Oracle API procedure notation)
   */
  Map addEquipmentBind(Map input) {
    return putEquipmentBind(input)
  }

  /**
   * Delete equipment-equipment bind
   * @param bindId {@link java.math.BigInteger BigInteger}. Optional
   * @return True if bind was deleted successfully, false otherwise
   */
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

  /**
   * Change equipment state
   * @param equipmentId {@link java.math.BigInteger BigInteger}
   * @param stateId {@link java.math.BigInteger BigInteger}
   * @return True if equipment state was changed successfully, false otherwise
   */
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

  /**
   * Change equipment state to Actual
   * @param equipmentId {@link java.math.BigInteger BigInteger}
   * @return True if equipment state was changed successfully, false otherwise
   */
  Boolean actualizeEquipment(def equipmentId) {
    return changeEquipmentState(equipmentId, getEquipmentStateActualId())
  }

  /**
   * Change equipment state to Deactivated
   * @param equipmentId {@link java.math.BigInteger BigInteger}
   * @return True if equipment state was changed successfully, false otherwise
   */
  Boolean deactivateEquipment(def equipmentId) {
    return changeEquipmentState(equipmentId, getEquipmentStateNotActiveId())
  }

  /**
   * Change equipment state to Unredistered
   * @param equipmentId {@link java.math.BigInteger BigInteger}
   * @return True if equipment state was changed successfully, false otherwise
   */
  Boolean unregisterEquipment(def equipmentId) {
    return changeEquipmentState(equipmentId, getEquipmentStateRegisterOffId())
  }

  /**
   * Refresh objects quick search material view
   * @see #refreshMaterialView(CharSequence,CharSequence)
   * @return True if quick search was updated successfully, false otherwise
   */
  Boolean refreshObjects(CharSequence method = 'C') {
    return refreshMaterialView(getObjectsMV(), method)
  }

  /**
   * Refresh equipment quick search material view
   * @see #refreshObjects(CharSequence)
   */
  Boolean refreshEquipment(CharSequence method = 'C') {
    return refreshObjects(method)
  }

  /**
   * Refresh equipment app params quick search material view
   * @see #refreshMaterialView(CharSequence,CharSequence)
   * @return True if quick search was updated successfully, false otherwise
   */
  Boolean refreshEquipmentAddParams(CharSequence method = 'C') {
    return refreshMaterialView(getEquipmentAddParamsMV(), method)
  }
}