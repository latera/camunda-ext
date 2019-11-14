package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.StringUtil.notEmpty
import static org.camunda.latera.bss.utils.StringUtil.forceNotEmpty
import static org.camunda.latera.bss.utils.StringUtil.joinNonEmpty
import static org.camunda.latera.bss.utils.Oracle.encodeDateStr
import static org.camunda.latera.bss.utils.Oracle.decodeBool
import static org.camunda.latera.bss.utils.Oracle.encodeBool
import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import static org.camunda.latera.bss.utils.DateTimeUtil.local
import static org.camunda.latera.bss.utils.MapUtil.keysList
import java.time.temporal.Temporal
import org.camunda.latera.bss.internal.Version

trait Address {
  private static String MAIN_ADDRESSES_TABLE      = 'SI_V_ADDRESSES'
  private static String SUBJECT_ADDRESSES_TABLE   = 'SI_V_SUBJ_ADDRESSES'
  private static String OBJECT_ADDRESSES_TABLE    = 'SI_V_OBJ_ADDRESSES'
  private static String OBJECT_ADDRESSES_MV       = 'SI_MV_OBJ_ADDRESSES'
  private static String SUBJECT_ADDRESSES_MV      = 'SI_MV_SUBJ_ADDRESSES'
  private static String DEFAULT_ADDRESS_TYPE      = 'ADDR_TYPE_FactPlace'
  private static String DEFAULT_ADDRESS_BIND_TYPE = 'BIND_ADDR_TYPE_Serv'
  private static String DEFAULT_ADDRESS_STATE     = 'ADDR_STATE_On'

  /*
  Structure
    field: messageCode
  messageCode is being translated to current locale ('locale' execution variable)
  */
  private static LinkedHashMap ADDRESS_FIELDS = [
    entrance  : 'Addr_EntranceString',
    floor     : 'Addr_FloorString',
    flat      : 'Addr_FlatString'
  ]

  String getMainAddressesTable() {
    return MAIN_ADDRESSES_TABLE
  }

  String getSubjectAddressesTable() {
    return SUBJECT_ADDRESSES_TABLE
  }

  String getObjectAddressesTable() {
    return OBJECT_ADDRESSES_TABLE
  }

  String getSubjectAddressesMV() {
    return SUBJECT_ADDRESSES_MV
  }

  String getObjectAddressesMV() {
    return OBJECT_ADDRESSES_MV
  }

  // Get [building: 'зд.', home: ..., entrance: '', ...]
  Map getAddressFields(CharSequence buildingType = null) {
    Map result = getBuildingFields(buildingType)
    ADDRESS_FIELDS.each{ key, value ->
      result[key] = getMessageNameByCode(value)
    }
    return result
  }

  List getAddressFieldNames(CharSequence buildingType = null) {
    return keysList(getAddressFields(buildingType))
  }

  String getDefaultAddressType() {
    return DEFAULT_ADDRESS_TYPE
  }

  Number getDefaultAddressTypeId() {
    return getRefIdByCode(getDefaultAddressType())
  }

  String getDefaultAddressBindType() {
    return DEFAULT_ADDRESS_BIND_TYPE
  }

  Number getDefaultAddressBindTypeId() {
    return getRefIdByCode(getDefaultAddressBindType())
  }

  String getDefaultAddressState() {
    return DEFAULT_ADDRESS_STATE
  }

  Number getDefaultAddressStateId() {
    return getRefIdByCode(getDefaultAddressState())
  }

  List getObjAddressesBy(Map input) {
    LinkedHashMap params = mergeParams([
      objAddressId    : null,
      objectId        : null,
      addressId       : null,
      addrTypeId      : getDefaultAddressTypeId(),
      parAddressId    : null,
      code            : null,
      regionId        : null,
      rawAddress      : null,
      flat            : null,
      floor           : null,
      entrance        : null,
      rem             : null,
      bindAddrTypeId  : getDefaultAddressBindTypeId(),
      stateId         : getDefaultAddressStateId(),
      isMain          : null,
      operationDate   : null,
      beginDate       : null,
      endDate         : null,
      limit           : 0
    ], input)

    LinkedHashMap where = [:]
    if (params.objAddressId) {
      where.n_obj_address_id = params.objAddressId
    }
    if (params.objectId) {
      where.n_object_id = params.objectId
    }
    if (params.bindAddrTypeId) {
      where.n_obj_addr_type_id = params.bindAddrTypeId
    }
    if (params.addressId) {
      where.n_address_id = params.addressId
    }
    if (params.addrTypeId) {
      where.n_addr_type_id = params.addrTypeId
    }
    if (params.parAddressId) {
      where.n_par_addr_id = params.parAddressId
    }
    if (params.code) {
      where.vc_code = params.code
    }
    if (params.regionId) {
      where.n_region_id = params.regionId
    }
    if (params.rawAddress) {
      where.vc_address = params.rawAddress
    }
    if (params.flat) {
      where.vc_flat = params.flat
    }
    if (params.floor) {
      where.n_floor = params.floor
    }
    if (params.entrance) {
      if (this.version >= '5.1.2') {
        where.vc_entrance_no = params.entrance
      } else {
        where.n_entrance_no = params.entrance
      }
    }
    if (params.rem) {
      where.vc_rem = params.rem
    }
    if (params.stateId) {
      where.n_addr_state_id = params.stateId
    }
    if (params.isMain != null) {
      where.c_fl_main = encodeBool(params.isMain)
    }
    // Only for objects addresses
    if (params.beginDate) {
      where.d_begin = params.beginDate
    }
    if (params.endDate) {
      where.d_end = params.endDate
    }
    if (!params.operationDate && !params.endDate && !params.beginDate) {
      params.operationDate = local()
    }
    if (params.operationDate) {
      String oracleDate = encodeDateStr(params.operationDate)
      where[oracleDate] = [between: "d_begin and nvl(d_end, ${oracleDate})"]
    }
    LinkedHashMap order = [c_fl_main: 'desc']
    return hid.getTableData(getObjectAddressesTable(), where: where, order: order, limit: params.limit)
  }

  Map getObjAddressBy(Map input) {
    return getObjAddressesBy(input + [limit: 1])?.getAt(0)
  }

  Map getObjAddress(def objAddressId) {
    LinkedHashMap where = [
      n_obj_address_id: objAddressId
    ]
    return hid.getTableFirst(getObjectAddressesTable(), where: where)
  }

  List getSubjAddressesBy(Map input) {
    LinkedHashMap params = mergeParams([
      subjAddressId   : null,
      subjectId       : null,
      addressId       : null,
      addrTypeId      : getDefaultAddressTypeId(),
      parAddressId    : null,
      code            : null,
      regionId        : null,
      rawAddress      : null,
      flat            : null,
      floor           : null,
      entrance        : null,
      rem             : null,
      bindAddrTypeId  : getDefaultAddressBindTypeId(),
      stateId         : getDefaultAddressStateId(),
      isMain          : null,
      limit           : 0
    ], input)

    LinkedHashMap where = [:]
    if (params.subjAddressId) {
      where.n_sub_address_id = params.subjAddressId
    }
    if (params.subjectId) {
      where.n_subject_id = params.subjectId
    }
    if (params.bindAddrTypeId) {
      where.n_subj_addr_type_id = params.bindAddrTypeId
    }
    if (params.addressId) {
      where.n_address_id = params.addressId
    }
    if (params.addrTypeId) {
      where.n_addr_type_id = params.addrTypeId
    }
    if (params.parAddressId) {
      where.n_par_addr_id = params.parAddressId
    }
    if (params.code) {
      where.vc_code = params.code
    }
    if (params.regionId) {
      where.n_region_id = params.regionId
    }
    if (params.rawAddress) {
      where.vc_address = params.rawAddress
    }
    if (params.flat) {
      where.vc_flat = params.flat
    }
    if (params.floor) {
      where.n_floor = params.floor
    }
    if (params.entrance) {
      if (this.version >= '5.1.2') {
        where.vc_entrance_no = params.entrance
      } else {
        where.n_entrance_no = params.entrance
      }
    }
    if (params.rem) {
      where.vc_rem = params.rem
    }
    if (params.stateId) {
      where.n_addr_state_id = params.stateId
    }
    if (params.isMain != null) {
      where.c_fl_main = encodeBool(params.isMain)
    }
    LinkedHashMap order = [c_fl_main: 'desc']
    return hid.getTableData(getSubjectAddressesTable(), where: where, order: order, limit: params.limit)
  }

  Map getSubjAddressBy(Map input) {
    return getSubjAddressesBy(input + [limit: 1])?.getAt(0)
  }

  Map getSubjAddress(def subjAddressId) {
    LinkedHashMap where = [
      n_subj_address_id: subjAddressId
    ]
    return hid.getTableFirst(getSubjectAddressesTable(), where: where)
  }

  List getEntityAddressesBy(Map input) {
    LinkedHashMap params = mergeParams([
      entityAddressId : null,
      entityTypeId    : null,
      entityId        : null,
      addressId       : null,
      addrTypeId      : getDefaultAddressTypeId(),
      parAddressId    : null,
      code            : null,
      regionId        : null,
      rawAddress      : null,
      flat            : null,
      floor           : null,
      entrance        : null,
      rem             : null,
      bindAddrTypeId  : getDefaultAddressBindTypeId(),
      stateId         : getDefaultAddressStateId(),
      isMain          : null,
      operationDate   : null,
      beginDate       : null,
      endDate         : null
    ], input)
    Boolean isSubj = isSubject(params.entityTypeId ?: params.entityId)

    if (isSubj) {
      params.subjAddressId = params.entityAddressId
      params.subjectId     = params.entityId
      return getSubjAddressesBy(params)
    } else {
      params.objAddressId  = params.entityAddressId
      params.objectId      = params.entityId
      return getObjAddressesBy(params)
    }
  }

  Map getEntityAddressBy(Map input) {
    return getEntityAddressesBy(input + [limit: 1])?.getAt(0)
  }

  Map getEntityAddress(def entityOrEntityTypeId, def entityAddressId) {
    Boolean isSubj = isSubject(entityOrEntityTypeId)

    if (isSubj) {
      return getSubjAddress(entityAddressId)
    } else {
      return getObjAddress(entityAddressId)
    }
  }

  List getAddressesBy(Map input) {
    LinkedHashMap params = mergeParams([
      addressId    : null,
      addrTypeId   : getDefaultAddressTypeId(),
      parAddressId : null,
      code         : null,
      regionId     : null,
      rawAddress   : null,
      flat         : null,
      floor        : null,
      entrance     : null,
      providerId   : getFirmId(),
      rem          : null
    ], input)

    LinkedHashMap where = [:]
    if (params.addressId) {
      where.n_address_id = params.addressId
      if (addrTypeId == getDefaultAddressTypeId()) {
        params.addrTypeId = null
      }
    }
    if (params.addrTypeId) {
      where.n_addr_type_id = params.addrTypeId
    }
    if (params.parAddressId) {
      where.n_par_addr_id = params.parAddressId
    }
    if (params.code) {
      where.vc_code = params.code
    }
    if (params.regionId) {
      where.n_region_id = params.regionId
    }
    if (params.rawAddress) {
      where.vc_address = params.rawAddress
    }
    if (params.flat) {
      where.vc_flat = params.flat
    }
    if (params.floor) {
      where.n_floor = params.floor
    }
    if (params.entrance) {
      if (this.version >= '5.1.2') {
        where.vc_entrance_no = params.entrance
      } else {
        where.n_entrance_no = params.entrance
      }
    }
    if (params.providerId) {
      where."DECODE(n_provider_id, NULL, ${params.providerId}, n_provider_id)" = params.providerId
    }
    if (params.rem) {
      where.vc_rem = params.rem
    }
    LinkedHashMap order = [n_address_id: 'desc']
    return hid.getTableData(getMainAddressesTable(), where: where, order: order, limit: params.limit)
  }

  Map getAddressBy(Map input) {
    return getAddressesBy(input + [limit: 1])?.getAt(0)
  }

  Map getAddress(def addressId) {
    LinkedHashMap where = [
      n_address_id: addressId
    ]
    return hid.getTableFirst(getMainAddressesTable(), where: where)
  }

  Boolean isAddressEmpty(Map input) {
    Boolean result = true
    List addressFields = getAddressFieldNames()

    (['regionId', 'code', 'rawAddress'] + addressFields).each { name ->
      if (forceNotEmpty(input[name])) {
        result = false
      }
    }
    return result
  }

  Boolean notAddressEmpty(Map input) {
    return !isAddressEmpty(input)
  }

  Boolean isRegionAddressEmpty(Map input) {
    return isAddressEmpty(input) && isRegionEmpty(input)
  }

  Boolean notRegionAddressEmpty(Map input) {
    return !isRegionAddressEmpty(input)
  }

  Map putSubjAddress(Map input) {
    LinkedHashMap params = mergeParams([
      subjAddressId  : null,
      addressId      : null,
      subjectId      : null,
      bindAddrTypeId : null,
      addrTypeId     : null,
      code           : null,
      regionId       : null,
      rawAddress     : null,
      flat           : null,
      floor          : null,
      entrance       : null,
      rem            : null,
      stateId        : getDefaultAddressStateId(),
      isMain         : null
    ], input)
    try {
      logger.info("Putting address with params ${params}")
      LinkedHashMap address = null
      if (this.version >= '5.1.2') {
        address = hid.execute('SI_ADDRESSES_PKG.SI_SUBJ_ADDRESSES_PUT_EX', [
          num_N_SUBJ_ADDRESS_ID   : params.subjAddressId,
          num_N_ADDRESS_ID        : params.addressId,
          num_N_SUBJECT_ID        : params.subjectId,
          num_N_SUBJ_ADDR_TYPE_ID : params.bindAddrTypeId,
          num_N_ADDR_TYPE_ID      : params.addrTypeId,
          vch_VC_CODE             : params.code,
          vch_VC_ADDRESS          : params.rawAddress,
          num_N_REGION_ID         : params.regionId,
          vch_VC_FLAT             : params.flat,
          vch_VC_ENTRANCE_NO      : params.entrance,
          num_N_FLOOR_NO          : params.floor,
          ch_C_FL_MAIN            : encodeBool(params.isMain),
          num_N_ADDR_STATE_ID     : params.stateId,
          vch_VC_REM              : params.rem
        ])
      } else {
        address = hid.execute('SI_ADDRESSES_PKG.SI_SUBJ_ADDRESSES_PUT_EX', [
          num_N_SUBJ_ADDRESS_ID   : params.subjAddressId,
          num_N_ADDRESS_ID        : params.addressId,
          num_N_SUBJECT_ID        : params.subjectId,
          num_N_SUBJ_ADDR_TYPE_ID : params.bindAddrTypeId,
          num_N_ADDR_TYPE_ID      : params.addrTypeId,
          vch_VC_CODE             : params.code,
          vch_VC_ADDRESS          : params.rawAddress,
          num_N_REGION_ID         : params.regionId,
          vch_VC_FLAT             : params.flat,
          num_N_ENTRANCE_NO       : params.entrance,
          num_N_FLOOR_NO          : params.floor,
          ch_C_FL_MAIN            : encodeBool(params.isMain),
          num_N_ADDR_STATE_ID     : params.stateId,
          vch_VC_REM              : params.rem
        ])
      }
      logger.info("   Address ${address.num_N_ADDRESS_ID} added to subject!")
      return address
    } catch (Exception e){
      logger.error("   Error while adding a subject address!")
      logger.error_oracle(e)
      return null
    }
  }

  Map putObjAddress(Map input) {
    LinkedHashMap params = mergeParams([
      objAddressId   : null,
      addressId      : null,
      objectId       : null,
      bindAddrTypeId : null,
      addrTypeId     : null,
      code           : null,
      regionId       : null,
      rawAddress     : null,
      flat           : null,
      floor          : null,
      entrance       : null,
      rem            : null,
      stateId        : getDefaultAddressStateId(),
      isMain         : null,
      beginDate      : local(),
      endDate        : null
    ], input)
    try {
      logger.info("Putting address with params ${params}")
      LinkedHashMap address = null
      if (this.version >= '5.1.2') {
        address = hid.execute('SI_ADDRESSES_PKG.SI_OBJ_ADDRESSES_PUT_EX', [
          num_N_OBJ_ADDRESS_ID   : params.objAddressId,
          num_N_ADDRESS_ID       : params.addressId,
          num_N_OBJECT_ID        : params.objectId,
          num_N_OBJ_ADDR_TYPE_ID : params.bindAddrTypeId,
          num_N_ADDR_TYPE_ID     : params.addrTypeId,
          vch_VC_CODE            : params.code,
          vch_VC_ADDRESS         : params.rawAddress,
          num_N_REGION_ID        : params.regionId,
          vch_VC_FLAT            : params.flat,
          vch_VC_ENTRANCE_NO     : params.entrance,
          num_N_FLOOR_NO         : params.floor,
          ch_C_FL_MAIN           : encodeBool(params.isMain),
          dt_D_BEGIN             : params.beginDate ?: local(),
          dt_D_END               : params.endDate,
          num_N_ADDR_STATE_ID    : params.stateId,
          vch_VC_REM             : params.rem
        ])
      } else {
        address = hid.execute('SI_ADDRESSES_PKG.SI_OBJ_ADDRESSES_PUT_EX', [
          num_N_OBJ_ADDRESS_ID   : params.objAddressId,
          num_N_ADDRESS_ID       : params.addressId,
          num_N_OBJECT_ID        : params.objectId,
          num_N_OBJ_ADDR_TYPE_ID : params.bindAddrTypeId,
          num_N_ADDR_TYPE_ID     : params.addrTypeId,
          vch_VC_CODE            : params.code,
          vch_VC_ADDRESS         : params.rawAddress,
          num_N_REGION_ID        : params.regionId,
          vch_VC_FLAT            : params.flat,
          num_N_ENTRANCE_NO      : params.entrance,
          num_N_FLOOR_NO         : params.floor,
          ch_C_FL_MAIN           : encodeBool(params.isMain),
          dt_D_BEGIN             : params.beginDate ?: local(),
          dt_D_END               : params.endDate,
          num_N_ADDR_STATE_ID    : params.stateId,
          vch_VC_REM             : params.rem
        ])
      }
      logger.info("   Address ${address.num_N_ADDRESS_ID} added to object!")
      return address
    } catch (Exception e){
      logger.error("   Error while adding an object address!")
      logger.error_oracle(e)
      return null
    }
  }

  Map putEntityAddress(Map input) {
    LinkedHashMap params = mergeParams([
      entityAddressId : null,
      entityId        : null,
      entityTypeId    : null,
      addressId       : null,
      bindAddrTypeId  : null,
      addrTypeId      : null,
      code            : null,
      regionId        : null,
      rawAddress      : null,
      flat            : null,
      floor           : null,
      entrance        : null,
      rem             : null,
      stateId         : getDefaultAddressStateId(),
      isMain          : null,
      beginDate       : local(),
      endDate         : null
    ], input)

    Boolean isSubj = isSubject(params.entityTypeId ?: params.entityId)

    if (isSubj) {
      params.subjAddressId  = params.entityAddressId
      params.subjectId      = params.entityId
      LinkedHashMap address = putSubjAddress(params)
      if (address) {
        address.num_N_ENTITY_ADDRESS_ID = address.num_N_SUBJ_ADDRESS_ID
        address.num_N_ENTITY_ID         = address.num_N_SUBJECT_ID
      }
      return address
    } else {
      params.objAddressId  = params.entityAddressId
      params.objectId      = params.entityId
      LinkedHashMap address = putObjAddress(params)
      if (address) {
        address.num_N_ENTITY_ADDRESS_ID = address.num_N_OBJ_ADDRESS_ID
        address.num_N_ENTITY_ID         = address.num_N_OBJECT_ID
      }
      return address
    }
  }

  Map putPersonAddress(def personId, Map input) {
    return putSubjAddress(input + [subjectId: personId])
  }

  Map putPersonAddress(Map input, def personId) {
    return putPersonAddress(personId, input)
  }

  Map putCompanyAddress(def companyId, Map input) {
    return putSubjAddress(input + [subjectId: companyId])
  }

  Map putCompanyAddress(Map input, def companyId) {
    return putCompanyAddress(companyId, input)
  }

  Map createSubjAddress(Map input) {
    input.remove('subjAddressId')
    return putSubjAddress(input)
  }

  Map createObjAddress(Map input) {
    input.remove('objAddressId')
    return putObjAddress(input)
  }

  Map createEntityAddress(Map input) {
    input.remove('entityAddressId')
    return putEntityAddress(input)
  }

  Map createSubjAddress(def subjectId, Map input) {
    return putSubjAddress(input + [subjectId: subjectId])
  }

  Map createSubjAddress(Map input, def subjectId) {
    return createSubjAddress(subjectId, input)
  }

  Map createPersonAddress(def personId, Map input) {
    return createSubjAddress(personId, input)
  }

  Map createPersonAddress(Map input, def personId) {
    return createPersonAddress(personId, input)
  }

  Map createCompanyAddress(def companyId, Map input) {
    return createSubjAddress(companyId, input)
  }

  Map createCompanyAddress(Map input, def companyId) {
    return createCompanyAddress(companyId, input)
  }

  Map createObjAddress(def objectId, Map input) {
    return putObjAddress(input + [objectId: objectId])
  }

  Map createObjAddress(Map input, def objectId) {
    return createObjAddress(objectId, input)
  }

  Map createEntityAddress(def entityId, Map input) {
    return putEntityAddress(input + [entityId: entityId])
  }

  Map createEntityAddress(Map input, def entityId) {
    return createEntityAddress(entityId, input)
  }

  Map updateSubjAddress(Map input) {
    return putSubjAddress(input)
  }

  Map updateObjAddress(Map input) {
    return putObjAddress(input)
  }

  Map updateEntityAddress(Map input) {
    return putEntityAddress(input)
  }

  Map updateSubjAddress(def subjAddressId, Map input) {
    return putSubjAddress(input + [subjAddressId: subjAddressId])
  }

  Map updateSubjAddress(Map input, def subjAddressId) {
    return updateSubjAddress(subjAddressId, input)
  }

  Map updatePersonAddress(def personId, def subjAddressId, Map input) {
    return updateSubjAddress(subjAddressId, input)
  }

  Map updatePersonAddress(Map input, def personId, def subjAddressId) {
    return updatePersonAddress(personId, subjAddressId, input)
  }

  Map updateCompanyAddress(def companyId, def subjAddressId, Map input) {
    return updateSubjAddress(subjAddressId, input)
  }

  Map updateCompanyAddress(Map input, def companyId, def subjAddressId) {
    return updateCompanyAddress(companyId, subjAddressId, input)
  }

  Map updateObjAddress(def objAddressId, Map input) {
    return putObjAddress(input + [objAddressId: objAddressId])
  }

  Map updateObjAddress(Map input, def objAddressId) {
    return updateObjAddress(objAddressId, input)
  }

  Map updateEntityAddress(def entityAddressId, Map input) {
    return putEntityAddress(input + [entityAddressId: entityAddressId])
  }

  Map updateEntityAddress(Map input, def entityAddressId) {
    return updateEntityAddress(entityAddressId, input)
  }

  List getAddressItems(Map input) {
    List addressItems = []
    getAddressFields().each{ type, value ->
      addressItems.add([value, 'N', input[type] ?: ""])
    }
    return addressItems
  }

  String calcAddress(Map input) {
    List address = []

    List regionItems = getRegionItems(input)
    if (regionItems){
      List result = regionItems + getAddressItems(input)
      result.eachWithIndex{ it, i ->
        String  part  = it[0]
        Boolean after = decodeBool(it[1])
        String  name  = it[2]
        if (forceNotEmpty(name)) {
          String item = (after ? '' : (part ?  part + ' ' : '')) + name + (after ? (part ?  ' ' + part : '') : '')
          address << item
        }
      }
    }

    return joinNonEmpty(address, ', ')
  }

  Boolean deleteSubjAddress(def subjAddressId) {
    try {
      logger.info("Deleting subject address id ${subjAddressId}")
      hid.execute('SI_ADDRESSES_PKG.SI_SUBJ_ADDRESSES_DEL', [
        num_N_SUBJ_ADDRESS_ID : subjAddressId
      ])
      logger.info("   Subject address was deleted successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while deleting a subject address!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean deleteObjAddress(def objAddressId) {
    try {
      logger.info("Deleting object address id ${objAddressId}")
      hid.execute('SI_ADDRESSES_PKG.SI_OBJ_ADDRESSES_DEL', [
        num_N_OBJ_ADDRESS_ID : objAddressId
      ])
      logger.info("   Object address was deleted successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while deleting an object address!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean deleteEntityAddress(Map input) {
    LinkedHashMap params = mergeParams([
      entityAddressId : null,
      entityTypeId    : null,
      addressId       : null,
      entityId        : null,
      bindAddrTypeId  : null,
      addrTypeId      : null,
      stateId         : getDefaultAddressStateId(),
      isMain          : null
    ], input)

    Boolean isSubj = false

    if (params.entityAddressId) {
      if (params.entityTypeId || params.entityId) {
        isSubj = isSubject(params.entityTypeId ?: params.entityId)
      } else {
        LinkedHashMap objAddress = getEntityAddress(
          entityAddressId : params.entityAddressId,
          entityId        : params.entityId,
          addressId       : params.addressId,
          addrTypeId      : params.addrTypeId,
          bindAddrTypeId  : params.bindAddrTypeId,
          stateId         : params.stateId,
          isMain          : params.isMain
        )
        LinkedHashMap subjAddress = getEntityAddress(
          entityType      : 'SUBJ_TYPE_Company',
          entityAddressId : params.entityAddressId,
          entityId        : params.entityId,
          addressId       : params.addressId,
          addrTypeId      : params.addrTypeId,
          bindAddrTypeId  : params.bindAddrTypeId,
          stateId         : params.stateId,
          isMain          : params.isMain
        )
        if (objAddress) {
          isSubj = false
        } else if (subjAddress) {
          isSubj = true
        } else {
          logger.info("No address found!")
          return true
        }
      }
    } else {
      LinkedHashMap address = getEntityAddress(
        addressId      : params.addressId,
        entityTypeId   : params.entityTypeId,
        entityId       : params.entityId,
        addrTypeId     : params.addrTypeId,
        bindAddrTypeId : params.bindAddrTypeId,
        stateId        : params.stateId,
        isMain         : params.isMain
      )
      if (!address) {
        logger.info("No address found!")
        return true
      }
      if (address.n_obj_address_id) {
        isSubj = false
        params.entityAddressId = address?.n_obj_address_id
      } else {
        isSubj = true
        params.entityAddressId = address?.n_subj_address_id
      }
    }

    if (isSubj) {
      return deleteSubjAddress(params.entityAddressId)
    } else {
      return deleteObjAddress(params.entityAddressId)
    }
  }

  Boolean deletePersonAddress(def personId, def subjAddressId) {
    return deleteSubjAddress(subjAddressId)
  }

  Boolean deleteCompanyAddress(def companyId, def subjAddressId) {
    return deleteSubjAddress(subjAddressId)
  }

  Boolean closeObjAddress(
    def objAddressId,
    Temporal endDate = local()
  ) {
    try {
      logger.info("Closing object address id ${objAddressId} with end date ${endDate}")
      hid.execute('SI_ADDRESSES_PKG.SI_OBJ_ADDRESSES_CLOSE', [
        num_N_OBJ_ADDRESS_ID : objAddressId,
        dt_D_END             : endDate
      ])
      logger.info("   Object address was closed successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while closing an object address!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean closeObjAddress(Map input) {
    LinkedHashMap params = mergeParams([
      objAddressId   : null,
      addressId      : null,
      objectId       : null,
      bindAddrTypeId : null,
      addrTypeId     : null,
      stateId        : getDefaultAddressStateId(),
      isMain         : null,
      endDate        : local()
    ], input)

    if (!params.objAddressId) {
      LinkedHashMap address = getObjAddress(
        addressId      : params.addressId,
        objectId       : params.objectId,
        addrTypeId     : params.addrTypeId,
        bindAddrTypeId : params.bindAddrTypeId,
        operationDate  : params.endDate,
        stateId        : params.stateId,
        isMain         : params.isMain
      )
      if (!address) {
        logger.error('No address found!')
        return false
      }
      params.objAddressId = address.n_obj_address_id
    }

    return closeObjAddress(params.objAddressId, params.endDate)
  }

  Boolean closeEntityAddress(Map input) {
    def objectId     = input.entityId
    def objAddressId = input.entityAddressId
    input.remove('entityId')
    input.remove('entityAddressId')
    return closeObjAddress(input + [objectId: objectId, objAddressId: objAddressId])
  }

  List getFreeIPAddresses(Map input) {
    LinkedHashMap defaultParams = [
      groupId           : null,
      objectId          : null,
      subnetAddressId   : null,
      operationDate     : local(),
      firmId            : getFirmId()
    ]
    if (input.containsKey('subnetAddress') && notEmpty(input.subnetAddress)) {
      input.subnetAddressId = getAddressBy(code: input.subnetAddress, addrType: 'ADDR_TYPE_Subnet')?.n_address_id
      input.remove('subnetAddress')
    }
    LinkedHashMap params = mergeParams(defaultParams, input)
    List addresses = []
    String date = encodeDateStr(params.operationDate)

    try {
      if (params.objectId) {
        addresses = hid.queryDatabase("""
          SELECT
              'vc_ip',       A.VC_CODE,
              'n_subnet_id', A.N_ADDRESS_ID,
              'vc_subnet',   PA.VC_CODE VC_SUBNET
          FROM
              TABLE(SI_ADDRESSES_PKG_S.GET_FREE_ADDRESS_FOR_OBJECT(
                num_N_OBJECT_ID        => ${params.objectId},
                num_N_ADDR_TYPE_ID     => SYS_CONTEXT('CONST', 'ADDR_TYPE_IP'),
                num_N_RANGE_ADDRESS_ID => ${params.subnetAddressId ?: 'NULL'}
              )) A,
              SI_V_ADDRESSES AA,
              SI_V_ADDRESSES PA
          WHERE
              AA.N_ADDRESS_ID  (+)= A.N_ADDRESS_ID
          AND PA.N_ADDRESS_ID  (+)= AA.N_PAR_ADDR_ID
          AND PA.N_PROVIDER_ID (+)= ${params.firmId}
        """, true)
      } else if (params.groupId && this.version <= '5.1.2') {
        addresses = hid.queryDatabase("""
          SELECT
              'vc_ip', SI_ADDRESSES_PKG_S.NUMBER_TO_IP_ADDRESS(SI_ADDRESSES_PKG_S.GET_FREE_IP_ADDRESS(
                num_N_SUBNET_ADDR_ID => A.N_ADDRESS_ID,
                num_N_PROVIDER_ID    => ${params.firmId})),
              'n_subnet_id', A.N_ADDRESS_ID,
              'vc_subnet',   A.VC_CODE
          FROM
              SI_V_ADDRESSES   A,
              RG_PAR_ADDRESSES RG
          WHERE
              ${date} BETWEEN RG.D_BEGIN AND NVL(RG.D_END, ${date})
          AND A.N_ADDRESS_ID         = RG.N_ADDRESS_ID
          AND RG.N_PAR_ADDR_ID       = ${params.groupId}
          AND RG.N_ADDR_BIND_TYPE_ID = SYS_CONTEXT('CONST', 'ADDR_ADDR_TYPE_Group')
          AND RG.N_PROVIDER_ID       = ${params.firmId}
        """, true)
      } else {
        addresses = hid.queryDatabase("""
          SELECT
              'vc_ip', SI_ADDRESSES_PKG_S.NUMBER_TO_IP_ADDRESS(SI_ADDRESSES_PKG_S.GET_FREE_IP_ADDRESS(
                num_N_SUBNET_ADDR_ID => A.N_ADDRESS_ID,
                num_N_PROVIDER_ID    => ${params.firmId})),
              'n_subnet_id', A.N_ADDRESS_ID,
              'vc_subnet',   A.VC_CODE
          FROM
              SI_V_ADDRESSES A
          WHERE
              A.N_ADDRESS_ID = ${params.subnetAddressId}
        """, true)
      }
    } catch (Exception e){
      logger.error_oracle(e)
    }
    return addresses
  }

  Map getFreeIPAddress(Map input) {
    List result = getFreeIPAddresses(input)
    if (result) {
      return result.getAt(0)
    } else {
      return null
    }
  }

  String getFreeIP(Map input) {
    return getFreeIPAddress(input)?.vc_ip
  }

  List getFreeIPv6Addresses(Map input) {
    LinkedHashMap defaultParams = [
      groupId         : null,
      objectId        : null,
      subnetAddressId : null,
      operationDate   : local(),
      firmId          : getFirmId()
    ]
    if (input.containsKey('subnetAddress') && notEmpty(input.subnetAddress)) {
      input.subnetAddressId = getAddressBy(code: input.subnetAddress, addrType: 'ADDR_TYPE_Subnet6')?.n_address_id
      input.remove('subnetAddress')
    }
    LinkedHashMap params = mergeParams(defaultParams, input)

    List addresses = []
    String date = encodeDateStr(params.operationDate)

    try {
      if (this.version >= '5.1.2') {
        if (params.objectId) {
          if (params.subnetAddressId) {
            addresses = hid.queryDatabase("""
              SELECT
                  'vc_ip',       SI_IP_ADDRESSES_PKG_S.VARCHAR_TO_IP6(A.VC_VALUE),
                  'n_subnet_id', PA.N_ADDRESS_ID,
                  'vc_subnet',   PA.VC_CODE VC_SUBNET
              FROM
                  TABLE(SI_ADDRESSES_PKG_S.GET_FREE_ADDRESS_FOR_OBJECT(
                    num_N_OBJECT_ID        => ${params.objectId},
                    num_N_ADDR_TYPE_ID     => SYS_CONTEXT('CONST', 'ADDR_TYPE_IP6'),
                    num_N_RANGE_ADDRESS_ID => ${params.subnetAddressId}
                  )) A,
                  SI_V_ADDRESSES PA
              WHERE
                PA.N_ADDRESS_ID  = ${params.subnetAddressId}
            AND PA.N_PROVIDER_ID = ${params.firmId}
            """, true)
          } else {
            addresses = hid.queryDatabase("""
              SELECT
                  'vc_ip',       SI_IP_ADDRESSES_PKG_S.VARCHAR_TO_IP6(A.VC_VALUE),
                  'n_subnet_id', PA.N_ADDRESS_ID,
                  'vc_subnet',   PA.VC_CODE VC_SUBNET
              FROM
                  TABLE(SI_ADDRESSES_PKG_S.GET_FREE_ADDRESS_FOR_OBJECT(
                    num_N_OBJECT_ID        => ${params.objectId},
                    num_N_ADDR_TYPE_ID     => SYS_CONTEXT('CONST', 'ADDR_TYPE_IP6'),
                    num_N_RANGE_ADDRESS_ID => NULL
                  )) A,
                  SI_V_ADDRESSES AA,
                  SI_V_ADDRESSES PA
              WHERE
                AA.N_ADDRESS_ID  (+)= A.N_ADDRESS_ID
            AND PA.N_ADDRESS_ID  (+)= AA.N_PAR_ADDR_ID
            AND PA.N_PROVIDER_ID (+)= ${params.firmId}
            """, true)
          }
        } else {
          addresses = hid.queryDatabase("""
            SELECT
                'vc_ip', SI_IP_ADDRESSES_PKG_S.VARCHAR_TO_IP6(SI_ADDRESS_RESTRICTIONS_PKG_S.GET_FREE_IP6_SLOW(
                  num_N_RANGE_ADDRESS_ID => A.N_ADDRESS_ID,
                  num_N_PROVIDER_ID      => ${params.firmId})),
                'n_subnet_id', A.N_ADDRESS_ID,
                'vc_subnet',   A.VC_CODE
            FROM
                SI_V_ADDRESSES A
            WHERE
                A.N_ADDRESS_ID = ${params.subnetAddressId}
          """, true)
        }
      }
    } catch (Exception e){
      logger.error_oracle(e)
    }
    return addresses
  }

  Map getFreeIPv6Address(Map input) {
    List result = getFreeIPv6Addresses(input)
    if (result) {
      return result.getAt(0)
    } else {
      return null
    }
  }

  String getFreeIPv6(Map input) {
    return getFreeIPv6Address(input)?.vc_ip
  }

  String getFreeIPv6Subnet(Map input) {
    String subnet = null
    try {
      subnet = "${getFreeIPv6(input)}/${getIPv6Mask()}"
    } catch (Exception e){
      logger.error_oracle(e)
    }
    return subnet
  }

  List getFreeTelephoneNumbers(Map input) {
    LinkedHashMap defaultParams = [
      groupId       : null,
      objectId      : null,
      telCodeId     : null,
      operationDate : local(),
      firmId        : getFirmId()
    ]
    if (input.containsKey('telCode') && notEmpty(input.telCode)) {
      input.telCodeId = getAddressBy(code: input.telCode, addrType: 'ADDR_TYPE_TelCode')?.n_address_id
      input.remove('telCode')
    }
    LinkedHashMap params = mergeParams(defaultParams, input)

    List addresses = []
    String date = encodeDateStr(params.operationDate)

    try {
      if (params.objectId) {
        if (params.telCodeId) {
          addresses = hid.queryDatabase("""
            SELECT
                'vc_phone_number', A.VC_CODE,
                'n_telcode_id',    A.N_ADDRESS_ID,
                'vc_tel_code',     A.VC_CODE VC_SUBNET
            FROM
                TABLE(SI_ADDRESSES_PKG_S.GET_FREE_ADDRESS_FOR_OBJECT(
                  num_N_OBJECT_ID        => ${params.objectId},
                  num_N_ADDR_TYPE_ID     => SYS_CONTEXT('CONST', 'ADDR_TYPE_Telephone'),
                  num_N_RANGE_ADDRESS_ID => ${params.telCodeId}
                )) A,
                SI_V_ADDRESSES PA
            WHERE
                PA.N_ADDRESS_ID  = ${params.telCodeId}
            AND PA.N_PROVIDER_ID = ${params.firmId}
          """, true)
        } else {
          addresses = hid.queryDatabase("""
            SELECT
                'vc_phone_number', A.VC_CODE,
                'n_telcode_id',    A.N_ADDRESS_ID,
                'vc_tel_code',     A.VC_CODE VC_SUBNET
            FROM
                TABLE(SI_ADDRESSES_PKG_S.GET_FREE_ADDRESS_FOR_OBJECT(
                  num_N_OBJECT_ID        => ${params.objectId},
                  num_N_ADDR_TYPE_ID     => SYS_CONTEXT('CONST', 'ADDR_TYPE_Telephone'),
                  num_N_RANGE_ADDRESS_ID => NULL
                )) A,
                SI_V_ADDRESSES AA,
                SI_V_ADDRESSES PA
            WHERE
                AA.N_ADDRESS_ID  (+)= A.N_ADDRESS_ID
            AND PA.N_ADDRESS_ID  (+)= AA.N_PAR_ADDR_ID
            AND PA.N_PROVIDER_ID (+)= ${params.firmId}
          """, true)
        }
      } else if (params.groupId && this.version <= '5.1.2') {
        addresses = hid.queryDatabase("""
          SELECT
              'vc_phone_number', SI_ADDRESSES_PKG_S.GET_FREE_PHONE_NUMBER(
                num_N_TEL_CODE_ID => A.N_ADDRESS_ID,
                num_N_PROVIDER_ID => ${params.firmId}),
              'n_telcode_id', A.N_ADDRESS_ID,
              'vc_tel_code',  A.VC_CODE
          FROM
              SI_V_ADDRESSES A,
              RG_PAR_ADDRESSES RG
          WHERE
              ${date} BETWEEN RG.D_BEGIN AND NVL(RG.D_END, ${date})
          AND A.N_ADDRESS_ID         = RG.N_ADDRESS_ID
          AND RG.N_PAR_ADDR_ID       = ${params.groupId}
          AND RG.N_ADDR_BIND_TYPE_ID = SYS_CONTEXT('CONST', 'ADDR_ADDR_TYPE_Group')
          AND RG.N_PROVIDER_ID       = ${params.firmId}
        """, true)
      } else {
        addresses = hid.queryDatabase("""
          SELECT
              'vc_phone_number', SI_ADDRESSES_PKG_S.GET_FREE_PHONE_NUMBER(
                num_N_TEL_CODE_ID => A.N_ADDRESS_ID,
                num_N_PROVIDER_ID => ${params.firmId}),
              'n_telcode_id', A.N_ADDRESS_ID,
              'vc_tel_code',  A.VC_CODE
          FROM
              SI_V_ADDRESSES A
          WHERE
              A.N_ADDRESS_ID = ${params.telCodeId}
        """, true)
      }
    } catch (Exception e){
      logger.error_oracle(e)
    }
    return addresses
  }

  Map getFreeTelephoneNumber(Map input) {
    List result = getFreeTelephoneNumbers(input)
    if (result) {
      return result.getAt(0)
    } else {
      return null
    }
  }

  String getFreePhoneNumber(Map input) {
    return getFreeTelephoneNumber(input)?.vc_phone_number
  }

  List getFreeSubnetAddresses(Map input) {
    LinkedHashMap defaultParams = [
      groupId       : null,
      rootId        : null,
      mask          : null,
      operationDate : local(),
      firmId        : getFirmId()
    ]
    if (input.containsKey('root') && notEmpty(input.root)) {
      input.rootId = getAddressBy(code: input.root, addrType: 'ADDR_TYPE_Subnet')?.n_address_id
      input.remove('root')
    }
    LinkedHashMap params = mergeParams(defaultParams, input)

    List addresses = []
    String date = encodeDateStr(params.operationDate)
    String filter = params.mask ? "SI_ADDRESSES_PKG_S.GET_BITN_BY_MASK(A.N_MASK) = '${params.mask}'" : '1=1'
    String notAssigned = """NOT EXISTS ( -- Не привязаны к оборудованию
      SELECT 1
      FROM
          SI_V_OBJ_ADDRESSES OA
      WHERE
          OA.N_ADDR_STATE_ID = SYS_CONTEXT('CONST', 'ADDR_STATE_On')
      AND OA.N_ADDRESS_ID    = FA.N_ADDRESS_ID
      AND ${date} BETWEEN OA.D_BEGIN AND NVL(OA.D_END, ${date})
    )"""

    String notAssignedChild = ''
      if (this.version >= '5.1.2') {
      notAssignedChild = params.mask == '30' ? '1=1' : """NOT EXISTS ( -- И нет дочерних привязок к оборудованию
        SELECT 1
        FROM
            SI_V_OBJ_ADDRESSES OA,
            SI_V_ADDRESSES     A
        WHERE
            OA.N_ADDR_STATE_ID = SYS_CONTEXT('CONST', 'ADDR_STATE_On')
        AND A.N_ADDRESS_ID     = OA.N_ADDRESS_ID
        AND A.N_ADDR_TYPE_ID   = FA.N_ADDR_TYPE_ID
        AND ${date} BETWEEN OA.D_BEGIN AND NVL(OA.D_END, ${date})
        AND A.N_PROVIDER_ID    = ${params.firmId}
        START WITH
            A.N_PAR_ADDR_ID    = FA.N_ADDRESS_ID
        CONNECT BY PRIOR
            A.N_ADDRESS_ID     = A.N_PAR_ADDR_ID
      )"""
    } else {
      notAssignedChild = params.mask == '30' ? '1=1' : """NOT EXISTS ( -- И нет дочерних привязок к оборудованию
        SELECT 1
        FROM
            SI_V_OBJ_ADDRESSES OA,
            RG_PAR_ADDRESSES   RP
        WHERE
            OA.N_ADDR_STATE_ID     = SYS_CONTEXT('CONST', 'ADDR_STATE_On')
        AND OA.N_ADDR_TYPE_ID      = FA.N_ADDR_TYPE_ID
        AND RP.N_ADDRESS_ID        = OA.N_ADDRESS_ID
        AND ${date} BETWEEN OA.D_BEGIN AND NVL(OA.D_END, ${date})
        AND ${date} BETWEEN RP.D_BEGIN AND NVL(RP.D_END, ${date})
        AND RP.N_ADDR_BIND_TYPE_ID = SYS_CONTEXT('CONST', 'ADDR_ADDR_TYPE_Parent')
        AND RP.N_PROVIDER_ID       = ${params.firmId}
        START WITH
            RP.N_PAR_ADDR_ID       = FA.N_ADDRESS_ID
        CONNECT BY PRIOR
            RP.N_ADDRESS_ID        = RP.N_PAR_ADDR_ID
      )"""
    }

    // WARN Чтобы получение подсети работало, подсети нужного размера должны быть нарезаны
    try {
      if (this.version >= '5.1.2') {
        addresses = hid.queryDatabase("""
        WITH AVAILABLE_SUBNETS AS (
          SELECT *
          FROM   SI_V_PROV_SUBNETS A
          WHERE  A.N_PROVIDER_ID = ${params.firmId}
          CONNECT BY PRIOR
                A.N_ADDRESS_ID  = A.N_PAR_ADDR_ID
          START WITH
                A.N_PAR_ADDR_ID = ${params.rootId}
        ),
        FILTERED_SUBNETS AS (
          SELECT
              *
          FROM
              AVAILABLE_SUBNETS A
          WHERE
              ${filter}
        ),
        FREE_SUBNETS AS (
          SELECT DISTINCT
              *
          FROM
              FILTERED_SUBNETS FA
          WHERE
              ${notAssigned}
          AND ${notAssignedChild}
        ),
        SORTED_SUBNETS AS (
          SELECT *
          FROM FREE_SUBNETS
          ORDER BY N_VALUE
        )
        SELECT
            'n_subnet_id',   N_ADDRESS_ID,
            'vc_subnet',     VC_CODE,
            'n_par_addr_id', N_PAR_ADDR_ID
        FROM  SORTED_SUBNETS
        WHERE ROWNUM < 10
        """, true)
      } else {
        addresses = hid.queryDatabase("""
        WITH AVAILABLE_SUBNETS AS (
          SELECT RA.*
          FROM   RG_PAR_ADDRESSES   RA
          WHERE  RA.N_PROVIDER_ID = ${params.firmId}
          CONNECT BY PRIOR
                RA.N_ADDRESS_ID  = RA.N_PAR_ADDR_ID
          AND   RA.N_ADDR_BIND_TYPE_ID = SYS_CONTEXT('CONST', 'ADDR_ADDR_TYPE_Parent')
          START WITH
                RA.N_PAR_ADDR_ID = ${params.groupId ?: params.rootId}
        ),
        FILTERED_SUBNETS AS (
          SELECT
              AA.*,
              A.VC_CODE,
              A.N_VALUE
          FROM
              AVAILABLE_SUBNETS AA,
              SI_V_ADDRESSES    A
          WHERE
              A.N_ADDRESS_ID = AA.N_ADDRESS_ID
          AND ${filter}
          AND A.N_ADDR_TYPE_ID = SYS_CONTEXT('CONST', 'ADDR_TYPE_Subnet')
        ),
        FREE_SUBNETS AS (
          SELECT DISTINCT
              *
          FROM
              FILTERED_SUBNETS FA
          WHERE
              ${notAssigned}
          AND ${notAssignedChild}
        ),
        SORTED_SUBNETS AS (
          SELECT *
          FROM FREE_SUBNETS
          ORDER BY N_VALUE
        )
        SELECT
            'n_subnet_id',   N_ADDRESS_ID,
            'vc_subnet',     VC_CODE,
            'n_par_addr_id', N_PAR_ADDR_ID
        FROM  SORTED_SUBNETS
        WHERE ROWNUM < 10
        """, true)
      }
    } catch (Exception e){
      logger.error_oracle(e)
    }
    return addresses
  }

  Map getFreeSubnetAddress(Map input) {
    return getFreeSubnetAddresses(input)?.getAt(0)
  }

  String getFreeSubnet(Map input) {
    return getFreeSubnetAddress(input)?.vc_subnet
  }

  Number getSubnetIdByIP(CharSequence ip) {
    def subnetId = null
    try {
      if (this.version >= '5.1.2') {
        subnetId = toIntSafe(getAddressBy(code: ip, addrType: [in: ['ADDR_TYPE_IP', 'ADDR_TYPE_IP6']])?.n_par_addr_id)
      } else {
        subnetId = toIntSafe(hid.queryFirst("""
          SELECT SI_ADDRESSES_PKG_S.GET_SUBNET_BY_IP_ADDRESS('$ip')
          FROM   DUAL
        """)?.getAt(0))
      }
    } catch (Exception e){
      logger.error_oracle(e)
    }
    return toIntSafe(subnetId)
  }

  String getSubnetByIP(CharSequence ip) {
    def subnetId = getSubnetIdByIP(ip)
    LinkedHashMap subnet = null
    if (subnetId) {
      subnet = getAddress(addressId: subnetId, addrType: 'ADDR_TYPE_SUBNET')?.vc_code
    }
    return subnet
  }

  String getSubnetMaskById(def subnetId) {
    String mask = ''
    try {
      mask = hid.queryFirst("""
        SELECT SI_ADDRESSES_PKG_S.GET_BITN_BY_MASK(SI_ADDRESSES_PKG_S.GET_N_MASK_BY_SUBNET(${subnetId}))
        FROM   DUAL
      """)?.getAt(0)
    } catch (Exception e){
      logger.error_oracle(e)
    }
    return mask
  }

  String getSubnetMask(CharSequence subnet) {
    String mask = ''
    def subnetId = getAddress(code: subnet, addrType: 'ADDR_TYPE_SUBNET')
    if (subnetId) {
      mask = getSubnetMaskById(subnetId)
    }
    return mask
  }

  String getSuubnetv6Mask() {
    return getIPv6Mask()
  }

  String getIPv6Mask() {
    return getParamValue(param: 'PAR_IPv6SubnetLength', subjectId: getFirmId())
  }

  List getParentSubnetAddresses(Map input) {
    LinkedHashMap defaultParams = [
      addressId     : null,
      mask          : null,
      operationDate : local(),
      firmId        : getFirmId()
    ]
    if ((input.containsKey('address') && notEmpty(input.address)) || (input.containsKey('code') && notEmpty(input.code))) {
      input.addressId = getAddressBy(code: input.address ?: input.code, type: 'ADDR_TYPE_Subnet')
      input.remove('address')
    }
    LinkedHashMap params = mergeParams(defaultParams, input)
    List addresses = []
    String date = encodeDateStr(params.operationDate)

    try {
      if (this.version >= '5.1.2') {
        addresses = hid.queryDatabase("""
          SELECT DISTINCT
              'n_address_id',  A.N_ADDRESS_ID,
              'code',          A.VC_CODE,
              'n_value',       A.N_VALUE,
              'n_par_addr_id', A.N_PAR_ADDR_ID,
              'level',         LEVEL
          FROM
              SI_V_PROV_SUBNETS A
          WHERE
              A.N_PROVIDER_ID = ${params.firmId}
          START WITH
              A.N_ADDRESS_ID = ${params.addressId}"
          CONNECT BY PRIOR
              A.N_PAR_ADDR_ID  = A.N_ADDRESS_ID
          ORDER BY LEVEL ASC
        """, true)
      } else {
        addresses = hid.queryDatabase("""
          SELECT DISTINCT
              'n_address_id',  A.N_ADDRESS_ID,
              'code',          A.VC_CODE,
              'n_value',       A.N_VALUE,
              'n_par_addr_id', RA.N_PAR_ADDR_ID,
              'level',         LEVEL
          FROM
              SI_V_ADDRESSES   A,
              RG_PAR_ADDRESSES RA
          WHERE
              RA.N_ADDRESS_ID        = A.N_ADDRESS_ID
          AND ${date} BETWEEN RA.D_BEGIN AND NVL(RA.D_END, ${date})
          AND RA.N_ADDR_BIND_TYPE_ID = SYS_CONTEXT('CONST', 'ADDR_ADDR_TYPE_Parent')
          AND RA.N_PROVIDER_ID       = ${params.firmId}
          START WITH
              A.N_ADDRESS_ID = ${params.addressId}"
          CONNECT BY PRIOR
              RA.N_PAR_ADDR_ID       = RA.N_ADDRESS_ID
          ORDER BY LEVEL ASC
        """, true)
      }
    } catch (Exception e){
      logger.error_oracle(e)
    }
    return addresses
  }

  Map getVLANAddressBySubnet(Map input) {
    LinkedHashMap defaultParams = [
      addressId     : null,
      mask          : null,
      operationDate : local(),
      firmId        : getFirmId()
    ]
    if ((input.containsKey('address') && notEmpty(input.address)) || (input.containsKey('code') && notEmpty(input.code))) {
      input.addressId = getAddressBy(code: input.address ?: input.code, type: 'ADDR_TYPE_Subnet')
      input.remove('address')
    }
    LinkedHashMap params = mergeParams(defaultParams, input)
    LinkedHashMap address = null
    String date = encodeDateStr(params.operationDate)

    try {
      if (this.version >= '5.1.2') {
        address = hid.queryFirst("""
          WITH SUBNETS AS (
            SELECT DISTINCT
                A.N_ADDRESS_ID,
                A.VC_CODE,
                A.N_VALUE,
                A.N_PAR_ADDR_ID,
                LEVEL
            FROM
                SI_V_PROV_SUBNETS4 A
            WHERE
                A.N_PROVIDER_ID = ${params.firmId}
            START WITH
                A.N_ADDRESS_ID = ${params.addressId}"
            CONNECT BY PRIOR
                A.N_PAR_ADDR_ID  = A.N_ADDRESS_ID
            ORDER BY LEVEL ASC
          )

          SELECT
              'n_vlan_id',        A.N_VLAN_ID,
              'vc_vlan',          A.VC_VLAN_CODE
          FROM
              SUBNETS             S,
              SI_V_VLAN_ADDRESSES A
          WHERE
              S.N_ADDRESS_ID = A.N_ADDRESS_ID
        """, true)
      } else {
        address = hid.queryFirst("""
          WITH SUBNETS AS (
            SELECT DISTINCT
                A.N_ADDRESS_ID,
                A.VC_CODE,
                A.N_VALUE,
                RA.N_PAR_ADDR_ID,
                LEVEL
            FROM
                SI_V_ADDRESSES   A,
                RG_PAR_ADDRESSES RA
            WHERE
                RA.N_ADDRESS_ID        = A.N_ADDRESS_ID
            AND A.N_ADDR_TYPE_ID       = SYS_CONTEXT('CONST', 'ADDR_TYPE_Subnet')
            AND ${date} BETWEEN RA.D_BEGIN AND NVL(RA.D_END, ${date})
            AND RA.N_ADDR_BIND_TYPE_ID = SYS_CONTEXT('CONST', 'ADDR_ADDR_TYPE_Parent')
            AND RA.N_PROVIDER_ID       = ${params.firmId}
            START WITH
                A.N_ADDRESS_ID = ${params.addressId}"
            CONNECT BY PRIOR
                RA.N_PAR_ADDR_ID       = RA.N_ADDRESS_ID
            ORDER BY LEVEL ASC
          )

          SELECT
              'n_vlan_id',     A.N_ADDRESS_ID,
              'vc_vlan',       A.VC_CODE
          FROM
              SUBNETS          S,
              SI_V_ADDRESSES   A,
              RG_PAR_ADDRESSES RG
          WHERE
              RG.N_ADDRESS_ID        = S.N_ADDRESS_ID
          AND ${date} BETWEEN RG.D_BEGIN AND NVL(RG.D_END, ${date})
          AND RG.N_ADDR_BIND_TYPE_ID = SYS_CONTEXT('CONST', 'ADDR_ADDR_TYPE_SubnetViaVLAN')
          AND RG.N_PROVIDER_ID       = ${params.firmId}
          AND RG.N_PAR_ADDR_ID       = A.N_ADDRESS_ID
          AND A.N_ADDR_TYPE_ID       = SYS_CONTEXT('CONST', 'ADDR_TYPE_VLAN')
        """, true)
      }
    } catch (Exception e){
      logger.error_oracle(e)
    }
    return address
  }

  String getVLANBySubnet(Map input) {
    return getVLANAddressBySubnet(input)?.vc_vlan
  }

  Boolean refreshObjAddresses(CharSequence method = 'C') {
    return refreshMaterialView(getSubjectAddressesMV(), method)
  }

  Boolean refreshSubjAddresses(CharSequence method = 'C') {
    return refreshMaterialView(getObjectAddressesMV(), method)
  }

  Boolean refreshEntityAddresses(CharSequence method = 'C') {
    return refreshObjAddresses(method) && refreshSubjAddresses(method)
  }
}