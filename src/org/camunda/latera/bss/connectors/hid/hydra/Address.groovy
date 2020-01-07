package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.StringUtil.notEmpty
import static org.camunda.latera.bss.utils.StringUtil.forceNotEmpty
import static org.camunda.latera.bss.utils.StringUtil.joinNonEmpty
import static org.camunda.latera.bss.utils.ListUtil.isList
import static org.camunda.latera.bss.utils.Oracle.encodeDateStr
import static org.camunda.latera.bss.utils.Oracle.decodeBool
import static org.camunda.latera.bss.utils.Oracle.encodeBool
import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import static org.camunda.latera.bss.utils.DateTimeUtil.local
import static org.camunda.latera.bss.utils.MapUtil.keysList
import java.time.temporal.Temporal

trait Address {
  private static String MAIN_ADDRESSES_TABLE      = 'SI_V_ADDRESSES'
  private static String SUBJECT_ADDRESSES_TABLE   = 'SI_V_SUBJ_ADDRESSES'
  private static String OBJECT_ADDRESSES_TABLE    = 'SI_V_OBJ_ADDRESSES'
  private static String OBJECT_ADDRESSES_MV       = 'SI_MV_OBJ_ADDRESSES'
  private static String SUBJECT_ADDRESSES_MV      = 'SI_MV_SUBJ_ADDRESSES'
  private static String DEFAULT_ADDRESS_TYPE      = 'ADDR_TYPE_FactPlace'
  private static String DEFAULT_ADDRESS_BIND_TYPE = 'BIND_ADDR_TYPE_Actual'
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
      parObjAddressId : null,
      stateId         : getDefaultAddressStateId(),
      isMain          : null,
      operationDate   : null,
      beginDate       : null,
      endDate         : null,
      limit           : 0,
      order           : [c_fl_main: 'desc']
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
    if (params.parObjAddressId) {
      where.n_par_obj_addr_id = params.parObjAddressId
    }
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
    return hid.getTableData(getObjectAddressesTable(), where: where, order: params.order, limit: params.limit)
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
      limit           : 0,
      order           : [c_fl_main: 'desc']
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
    return hid.getTableData(getSubjectAddressesTable(), where: where, order: params.order, limit: params.limit)
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
      entityAddressId    : null,
      entityTypeId       : null,
      entityId           : null,
      addressId          : null,
      addrTypeId         : getDefaultAddressTypeId(),
      parAddressId       : null,
      code               : null,
      regionId           : null,
      rawAddress         : null,
      flat               : null,
      floor              : null,
      entrance           : null,
      rem                : null,
      bindAddrTypeId     : getDefaultAddressBindTypeId(),
      parEntityAddressId : null,
      stateId            : getDefaultAddressStateId(),
      isMain             : null,
      operationDate      : null,
      beginDate          : null,
      endDate            : null
    ], input)
    Boolean isSubj = isSubject(params.entityTypeId ?: params.entityId)

    if (isSubj) {
      params.subjAddressId = params.entityAddressId
      params.subjectId     = params.entityId
      return getSubjAddressesBy(params)
    } else {
      params.objAddressId    = params.entityAddressId
      params.objectId        = params.entityId
      params.parObjAddressId = params.parEntityAddressId
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
      rem          : null,
      order        : [n_address_id: 'desc']
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
    return hid.getTableData(getMainAddressesTable(), where: where, order: params.order, limit: params.limit)
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

  private Map putSubjAddress(Map input) {
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

  private Map putObjAddress(Map input) {
    LinkedHashMap params = mergeParams([
      objAddressId    : null,
      addressId       : null,
      objectId        : null,
      bindAddrTypeId  : null,
      parObjAddressId : null,
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
    try {
      logger.info("Putting address with params ${params}")
      LinkedHashMap address = null
      if (this.version >= '5.1.2') {
        address = hid.execute('SI_ADDRESSES_PKG.SI_OBJ_ADDRESSES_PUT_EX', [
          num_N_OBJ_ADDRESS_ID   : params.objAddressId,
          num_N_ADDRESS_ID       : params.addressId,
          num_N_OBJECT_ID        : params.objectId,
          num_N_OBJ_ADDR_TYPE_ID : params.bindAddrTypeId,
          num_N_PAR_OBJ_ADDR_ID  : params.parObjAddressId,
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
          num_N_PAR_OBJ_ADDR_ID  : params.parObjAddressId,
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

  private Map putEntityAddress(Map input) {
    LinkedHashMap params = mergeParams([
      entityAddressId    : null,
      entityId           : null,
      entityTypeId       : null,
      addressId          : null,
      bindAddrTypeId     : null,
      parEntityAddressId : null,
      addrTypeId         : null,
      code               : null,
      regionId           : null,
      rawAddress         : null,
      flat               : null,
      floor              : null,
      entrance           : null,
      rem                : null,
      stateId            : getDefaultAddressStateId(),
      isMain             : null,
      beginDate          : local(),
      endDate            : null
    ], input)

    Boolean isSubj = isSubject(params.entityTypeId ?: params.entityId)

    if (isSubj) {
      params.subjAddressId   = params.entityAddressId
      params.subjectId       = params.entityId
      LinkedHashMap address  = putSubjAddress(params)
      if (address) {
        address.num_N_ENTITY_ADDRESS_ID = address.num_N_SUBJ_ADDRESS_ID
        address.num_N_ENTITY_ID         = address.num_N_SUBJECT_ID
      }
      return address
    } else {
      params.objAddressId    = params.entityAddressId
      params.objectId        = params.entityId
      params.parObjAddressId = params.parEntityAddressId
      LinkedHashMap address  = putObjAddress(params)
      if (address) {
        address.num_N_ENTITY_ADDRESS_ID = address.num_N_OBJ_ADDRESS_ID
        address.num_N_ENTITY_ID         = address.num_N_OBJECT_ID
      }
      return address
    }
  }

  Map createSubjAddress(Map input = [:], def subjectId) {
    input.remove('subjAddressId')
    return putSubjAddress(input + [subjectId: subjectId])
  }

  Map createPersonAddress(Map input = [:], def personId) {
    return createSubjAddress(input, personId)
  }

  Map createCompanyAddress(Map input = [:], def companyId) {
    return createSubjAddress(input, companyId)
  }

  Map createObjAddress(Map input = [:], def objectId) {
    input.remove('objAddressId')
    return putObjAddress(input + [objectId: objectId])
  }

  Map createEntityAddress(Map input = [:], def entityId) {
    input.remove('entityAddressId')
    return putEntityAddress(input + [entityId: entityId])
  }

  Map updateSubjAddress(Map input = [:], def subjAddressId) {
    return putSubjAddress(input + [subjAddressId: subjAddressId])
  }

  Map updatePersonAddress(Map input = [:], def subjAddressId) {
    return updateSubjAddress(input, subjAddressId)
  }

  Map updateCompanyAddress(Map input = [:], def subjAddressId) {
    return updateSubjAddress(input, subjAddressId)
  }

  Map updateObjAddress(Map input = [:], def objAddressId) {
    return putObjAddress(input + [objAddressId: objAddressId])
  }

  Map updateEntityAddress(Map input = [:], def entityAddressId) {
    return putEntityAddress(input + [entityAddressId: entityAddressId])
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
      objAddressId    : null,
      addressId       : null,
      objectId        : null,
      bindAddrTypeId  : null,
      parObjAddressId : null,
      addrTypeId      : null,
      stateId         : getDefaultAddressStateId(),
      isMain          : null,
      endDate         : local()
    ], input)

    if (!params.objAddressId) {
      LinkedHashMap address = getObjAddress(
        addressId       : params.addressId,
        objectId        : params.objectId,
        addrTypeId      : params.addrTypeId,
        bindAddrTypeId  : params.bindAddrTypeId,
        parObjAddressId : params.parObjAddressId,
        operationDate   : params.endDate,
        stateId         : params.stateId,
        isMain          : params.isMain
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
      vlanId            : null,
      operationDate     : local(),
      firmId            : getFirmId(),
      isPublic          : null,
      limit             : 10
    ]
    if (input.containsKey('subnetAddress') && notEmpty(input.subnetAddress)) {
      input.subnetAddressId = getAddressBy(code: input.subnetAddress, addrType: 'ADDR_TYPE_Subnet')?.n_address_id
      input.remove('subnetAddress')
    }
    if (input.containsKey('subnetAddresses') && notEmpty(input.subnetAddresses) && isList(input.subnetAddresses)) {
      input.subnetAddressIds = getAddressesBy(code: [in: input.subnetAddresses], addrType: 'ADDR_TYPE_Subnet', order: [n_value: 'asc']).collect { Map address -> toIntSafe(address.n_address_id) }
      input.remove('subnetAddress')
    }
    if (input.containsKey('vlan') && notEmpty(input.vlan)) {
      input.vlanId = getAddressBy(code: input.vlan, addrType: 'ADDR_TYPE_VLAN')?.n_address_id
      input.remove('vlan')
    }
    LinkedHashMap params = mergeParams(defaultParams, input)

    if (params.subnetAddressId && !params.subnetAddressIds) {
      params.subnetAddressIds = [params.subnetAddressId]
      params.remove('subnetAddressId')
    }
    if (!params.subnetAddressIds) {
      params.subnetAddressIds = []
    }
    if (params.vlanId) {
      List subnetIdsByVLAN = getSubnetAddressesByVLAN(vlanId: params.vlanId).collect{ Map subnet -> toIntSafe(subnet.n_subnet_id) }
      if (params.subnetAddressIds) {
        params.subnetAddressIds = params.subnetAddressIds.findAll { Map subnetAddrId -> toIntSafe(subnetAddrId) in subnetIdsByVLAN }
      } else {
        params.subnetAddressIds = subnetIdsByVLAN
      }
    }
    List addresses = []
    String date = encodeDateStr(params.operationDate)

    String filterReal = '1=1'
    if (params.isPublic != null) {
      filterReal = "UTILS_ADDRESSES_PKG_S.IS_REAL_IP(A.N_VALUE) = '${params.isPublic ? 'Y' : 'N'}'"
    }

    (params.subnetAddressIds ?: [null]).each { def subnetAddrId ->
      try {
        if (!addresses) {
          if (params.objectId) {
            addresses = hid.queryDatabase("""
              WITH FREE_IP AS (
                SELECT DISTINCT
                    A.VC_CODE       VC_IP,
                    A.N_VALUE       N_VALUE,
                    PA.N_ADDRESS_ID N_SUBNET_ID,
                    PA.VC_CODE      VC_SUBNET
                FROM
                    TABLE(SI_ADDRESSES_PKG_S.GET_FREE_ADDRESS_FOR_OBJECT(
                      num_N_OBJECT_ID        => ${params.objectId},
                      num_N_ADDR_TYPE_ID     => SYS_CONTEXT('CONST', 'ADDR_TYPE_IP'),
                      num_N_RANGE_ADDRESS_ID => ${subnetAddressId ?: 'NULL'}
                    )) A,
                    SI_V_ADDRESSES AA,
                    SI_V_ADDRESSES PA
                WHERE
                    AA.N_ADDRESS_ID  (+)= A.N_ADDRESS_ID
                AND PA.N_ADDRESS_ID  (+)= AA.N_PAR_ADDR_ID
                AND PA.N_PROVIDER_ID (+)= ${params.firmId}
                ORDER BY
                    N_VALUE
              )
              SELECT
                'vc_ip',       A.VC_IP,
                'n_subnet_id', A.N_SUBNET_ID,
                'vc_subnet',   A.VC_SUBNET
              FROM FREE_IP A
              WHERE ${filterReal}
            """, true, params.limit)
          } else if (params.groupId && this.version <= '5.1.2') {
          addresses = hid.queryDatabase("""
            WITH FREE_IP AS (
              SELECT
                  SI_ADDRESSES_PKG_S.GET_FREE_IP_ADDRESS(
                    num_N_SUBNET_ADDR_ID => A.N_ADDRESS_ID,
                    num_N_PROVIDER_ID    => ${params.firmId}) N_VALUE,
                  A.N_ADDRESS_ID N_SUBNET_ID,
                  A.VC_CODE      VC_SUBNET
              FROM
                  SI_V_ADDRESSES   A,
                  RG_PAR_ADDRESSES RG
              WHERE
                  ${date} BETWEEN RG.D_BEGIN AND NVL(RG.D_END, ${date})
              AND A.N_ADDRESS_ID         = RG.N_ADDRESS_ID
              AND RG.N_PAR_ADDR_ID       = ${params.groupId}
              AND RG.N_ADDR_BIND_TYPE_ID = SYS_CONTEXT('CONST', 'ADDR_ADDR_TYPE_Group')
              AND RG.N_PROVIDER_ID       = ${params.firmId}
            ),
            SORTED_IP AS (
              SELECT DISTINCT
                  *,
                  SI_ADDRESSES_PKG_S.NUMBER_TO_IP_ADDRESS(A.N_VALUE) VC_IP
              FROM
                  FREE_IP A
              ORDER BY
                  N_VALUE
            )
            SELECT
              'vc_ip',       A.VC_IP,
              'n_subnet_id', A.N_SUBNET_ID,
              'vc_subnet',   A.VC_SUBNET
            FROM SORTED_IP A
            WHERE ${filterReal}
          """, true, params.limit)
        } else {
          addresses = hid.queryDatabase("""
            WITH FREE_IP AS (
              SELECT
                  SI_ADDRESSES_PKG_S.GET_FREE_IP_ADDRESS(
                    num_N_SUBNET_ADDR_ID => A.N_ADDRESS_ID,
                    num_N_PROVIDER_ID    => ${params.firmId}) N_VALUE,
                  A.N_ADDRESS_ID N_SUBNET_ID,
                  A.VC_CODE      VC_SUBNET
              FROM
                  SI_V_ADDRESSES A
              WHERE
                  A.N_ADDRESS_ID = ${subnetAddressId}
            ),
            SORTED_IP AS (
              SELECT DISTINCT
                  *,
                  SI_ADDRESSES_PKG_S.NUMBER_TO_IP_ADDRESS(N_VALUE) VC_IP
              FROM
                  FREE_IP
              ORDER BY
                  N_VALUE
            )
            SELECT
              'vc_ip',       A.VC_IP,
              'n_subnet_id', A.N_SUBNET_ID,
              'vc_subnet',   A.VC_SUBNET
            FROM SORTED_IP A
            WHERE ${filterReal}
          """, true)
          }
        }
      } catch (Exception e) {}
    }
    return addresses
  }

  Map getFreeIPAddress(Map input) {
    List result = getFreeIPAddresses(input + [limit: 1])
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
      firmId          : getFirmId(),
      limit           : 10
    ]
    if (input.containsKey('subnetAddress') && notEmpty(input.subnetAddress)) {
      input.subnetAddressId = getAddressBy(code: input.subnetAddress, addrType: 'ADDR_TYPE_Subnet6')?.n_address_id
      input.remove('subnetAddress')
    }
    if (input.containsKey('subnetAddresses') && notEmpty(input.subnetAddresses) && isList(input.subnetAddresses)) {
      input.subnetAddressIds = getAddressesBy(code: [in: input.subnetAddresses], addrType: 'ADDR_TYPE_Subnet6', order: [n_value: 'asc', vc_value: 'asc']).collect {Map address -> address?.n_address_id}
      input.remove('subnetAddresses')
    }
    LinkedHashMap params = mergeParams(defaultParams, input)
    if (params.subnetAddressId && !params.subnetAddressIds) {
      params.subnetAddressIds = [params.subnetAddressId]
      params.remove('subnetAddressId')
    }
    if (!params.subnetAddressIds) {
      params.subnetAddressIds = []
    }

    List addresses = []

    (params.subnetAddressIds ?: [null]).each { def subnetAddressId ->
      try {
        if (!addresses) {
          if (this.version >= '5.1.2') {
            if (params.objectId) {
              if (subnetAddressId) {
                addresses = hid.queryDatabase("""
                  SELECT
                      'vc_ip',       SI_IP_ADDRESSES_PKG_S.VARCHAR_TO_IP6(A.VC_VALUE),
                      'n_subnet_id', PA.N_ADDRESS_ID,
                      'vc_subnet',   PA.VC_CODE VC_SUBNET
                  FROM
                      TABLE(SI_ADDRESSES_PKG_S.GET_FREE_ADDRESS_FOR_OBJECT(
                        num_N_OBJECT_ID        => ${params.objectId},
                        num_N_ADDR_TYPE_ID     => SYS_CONTEXT('CONST', 'ADDR_TYPE_IP6'),
                        num_N_RANGE_ADDRESS_ID => ${subnetAddressId}
                      )) A,
                      SI_V_ADDRESSES PA
                  WHERE
                    PA.N_ADDRESS_ID  = ${subnetAddressId}
                AND PA.N_PROVIDER_ID = ${params.firmId}
                """, true, params.limit)
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
                """, true, params.limit)
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
                    A.N_ADDRESS_ID = ${subnetAddressId}
              """, true, params.limit)
            }
          }
        }
      } catch (Exception e) {}
    }
    return addresses
  }

  Map getFreeIPv6Address(Map input) {
    List result = getFreeIPv6Addresses(input + [limit: 1])
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
      subnet = "${getFreeIPv6(input)}/${getSubnetv6Mask()}"
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
      firmId        : getFirmId(),
      limit         : 10
    ]
    if (input.containsKey('telCode') && notEmpty(input.telCode)) {
      input.telCodeId = getAddressBy(code: input.telCode, addrType: 'ADDR_TYPE_TelCode')?.n_address_id
      input.remove('telCode')
    }
    if (input.containsKey('telCodes') && notEmpty(input.telCodes) && isList(input.telCodes)) {
      input.telCodeIds = getAddressesBy(code: [in: input.telCodes], addrType: 'ADDR_TYPE_TelCode', order: [n_value: 'asc']).collect {Map address -> address?.n_address_id}
      input.remove('telCode')
    }
    LinkedHashMap params = mergeParams(defaultParams, input)
    if (params.telCodeId && !params.telCodeIds) {
      params.telCodeIds = [params.telCodeId]
      params.remove('telCodeId')
    }
    if (!params.telCodeIds) {
      params.telCodeIds = []
    }

    List addresses = []
    String date = encodeDateStr(params.operationDate)

    (params.telCodeIds ?: [null]).each { def telCodeId ->
      try {
        if (!addresses) {
          if (params.objectId) {
            if (telCodeId) {
              addresses = hid.queryDatabase("""
                SELECT
                    'vc_phone_number', A.VC_CODE,
                    'n_telcode_id',    A.N_ADDRESS_ID,
                    'vc_tel_code',     A.VC_CODE VC_SUBNET
                FROM
                    TABLE(SI_ADDRESSES_PKG_S.GET_FREE_ADDRESS_FOR_OBJECT(
                      num_N_OBJECT_ID        => ${params.objectId},
                      num_N_ADDR_TYPE_ID     => SYS_CONTEXT('CONST', 'ADDR_TYPE_Telephone'),
                      num_N_RANGE_ADDRESS_ID => ${telCodeId}
                    )) A,
                    SI_V_ADDRESSES PA
                WHERE
                    PA.N_ADDRESS_ID  = ${telCodeId}
                AND PA.N_PROVIDER_ID = ${params.firmId}
              """, true, params.limit)
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
              """, true, params.limit)
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
            """, true, params.limit)
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
                  A.N_ADDRESS_ID = ${telCodeId}
            """, true, params.limit)
          }
        }
      } catch (Exception e) {}
    }
    return addresses
  }

  Map getFreeTelephoneNumber(Map input) {
    List result = getFreeTelephoneNumbers(input + [limit: 1])
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
      vlanId        : null,
      operationDate : local(),
      firmId        : getFirmId(),
      isPublic      : null,
      limit         : 10
    ]
    if (input.containsKey('root') && notEmpty(input.root)) {
      input.rootId = getAddressBy(code: input.root, addrType: 'ADDR_TYPE_Subnet')?.n_address_id
      input.remove('root')
    }
    if (input.containsKey('vlan') && notEmpty(input.vlan)) {
      input.vlanId = getAddressBy(code: input.vlan, addrType: 'ADDR_TYPE_VLAN')?.n_address_id
      input.remove('vlan')
    }
    if (input.vlanId && !input.groupId && !input.rootId) {
      input.rootId = getSubnetAddressByVLAN(vlanId: input.vlanId)?.n_subnet_id
    }
    LinkedHashMap params = mergeParams(defaultParams, input)

    String filterReal = '1=1'
    if (params.isPublic != null) {
      filterReal = "UTILS_ADDRESSES_PKG_S.IS_REAL_SUBNET(S.N_VALUE, S.N_MASK) = ${params.isPublic ? 1 : 0}"
    }

    List addresses = []
    String date = encodeDateStr(params.operationDate)
    String filter = params.mask ? "(SI_ADDRESSES_PKG_S.GET_BITN_BY_MASK(A.N_MASK) = '${params.mask}' OR A.N_MASK = '${params.mask}')" : '1=1'
    String notAssigned = ''
    if (this.version >= '5.1.2') {
      notAssigned = """NOT EXISTS ( -- Не привязаны к оборудованию
        SELECT 1
        FROM
            SI_V_OBJ_ADDRESSES OA
        WHERE
            OA.N_ADDR_STATE_ID = SYS_CONTEXT('CONST', 'ADDR_STATE_On')
        AND OA.N_ADDRESS_ID    = FA.N_SUBNET_ID
        AND ${date} BETWEEN OA.D_BEGIN AND NVL(OA.D_END, ${date})
      )"""
    } else {
      notAssigned = """NOT EXISTS ( -- Не привязаны к оборудованию
        SELECT 1
        FROM
            SI_V_OBJ_ADDRESSES OA
        WHERE
            OA.N_ADDR_STATE_ID = SYS_CONTEXT('CONST', 'ADDR_STATE_On')
        AND OA.N_ADDRESS_ID    = FA.N_ADDRESS_ID
        AND ${date} BETWEEN OA.D_BEGIN AND NVL(OA.D_END, ${date})
      )"""
    }

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
            A.N_PAR_ADDR_ID    = FA.N_SUBNET_ID
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
          SELECT
              *
          FROM
              SI_V_PROV_SUBNETS A
          WHERE
              A.N_PROVIDER_ID = ${params.firmId}
          CONNECT BY PRIOR
              A.N_SUBNET_ID  = A.N_PAR_SUBNET_ID
          START WITH
              A.N_PAR_SUBNET_ID = ${params.rootId}
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
          ORDER BY
              N_VALUE
        ),
        SELECT
            'n_subnet_id',   S.N_ADDRESS_ID,
            'vc_subnet',     S.VC_CODE,
            'n_par_addr_id', S.N_PAR_ADDR_ID
        FROM  FREE_SUBNETS S
        WHERE ${filterReal}
        """, true, params.limit)
      } else {
        addresses = hid.queryDatabase("""
        WITH AVAILABLE_SUBNETS AS (
          SELECT
              RA.*
          FROM
              RG_PAR_ADDRESSES   RA
          WHERE
              RA.N_PROVIDER_ID = ${params.firmId}
          CONNECT BY PRIOR
              RA.N_ADDRESS_ID  = RA.N_PAR_ADDR_ID
          AND RA.N_ADDR_BIND_TYPE_ID = SYS_CONTEXT('CONST', 'ADDR_ADDR_TYPE_Parent')
          START WITH
              RA.N_PAR_ADDR_ID = ${params.groupId ?: params.rootId}
        ),
        FILTERED_SUBNETS AS (
          SELECT
              AA.*,
              A.VC_CODE,
              A.N_VALUE,
              A.N_MASK
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
          ORDER BY
              N_VALUE
        )
        SELECT
            'n_subnet_id',   S.N_ADDRESS_ID,
            'vc_subnet',     S.VC_CODE,
            'n_par_addr_id', S.N_PAR_ADDR_ID
        FROM  FREE_SUBNETS S
        WHERE ${filterReal}
        """, true, params.limit)
      }
    } catch (Exception e){
      logger.error_oracle(e)
    }
    return addresses
  }

  Map getFreeSubnetAddress(Map input) {
    List result = getFreeSubnetAddresses(input + [limit: 1])
    if (result) {
      return result.getAt(0)
    } else {
      return null
    }
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
    String subnet = null
    if (subnetId) {
      subnet = getAddress(addressId: subnetId, addrType: 'ADDR_TYPE_SUBNET')?.vc_code
    }
    return subnet
  }

  String getSubnetMaskById(def subnetId) {
    String mask = null
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
    String mask = null
    def subnetId = getAddress(code: subnet, addrType: 'ADDR_TYPE_SUBNET')
    if (subnetId) {
      mask = getSubnetMaskById(subnetId)
    }
    return mask
  }

  String getIPMaskById(def addressId) {
    String mask = null
    try {
      String ip = getAddress(addressId)?.vc_code
      mask = getIPMask(ip)
    } catch (Exception e){
      logger.error_oracle(e)
    }
    return mask
  }

  String getIPMask(CharSequence ip) {
    String mask = null
    def subnetId = getSubnetIdByIP(ip)
    if (subnetId) {
      mask = getSubnetMaskById(subnetId)
    }
    return mask
  }

  String getSubnetGatewayById(def subnetId) {
    String gateway = null
    try {
      def gatewayId = getAddress(subnetId)?.n_bind_addr_id
      if (gatewayId) {
        gateway = getAddress(gatewayId)?.vc_code
      }
    } catch (Exception e){
      logger.error_oracle(e)
    }
    return gateway
  }

  String getSubnetGateway(CharSequence subnet) {
    String gateway = null
    def subnetId = getAddress(code: subnet, addrType: 'ADDR_TYPE_SUBNET')
    if (subnetId) {
      gateway = getSubnetGatewayById(subnetId)
    }
    return gateway
  }

  String getIPGatewayById(def addressId) {
    String gateway = null
    try {
      String ip = getAddress(addressId)?.vc_code
      gateway = getIPGateway(ip)
    } catch (Exception e){
      logger.error_oracle(e)
    }
    return gateway
  }

  String getIPGateway(CharSequence ip) {
    String gateway = null
    def subnetId = getSubnetIdByIP(ip)
    if (subnetId) {
      gateway = getSubnetGatewayById(subnetId)
    }
    return gateway
  }

  String getSubnetv6Mask() {
    return getIPv6Mask()
  }

  String getIPv6Mask() {
    return getParamValueBy(param: 'PAR_IPv6SubnetLength', subjectId: getFirmId())?.n_value
  }

  List getParentSubnetAddresses(Map input) {
    LinkedHashMap defaultParams = [
      addressId     : null,
      mask          : null,
      operationDate : local(),
      firmId        : getFirmId(),
      limit         : 0
    ]
    if ((input.containsKey('address') && notEmpty(input.address)) || (input.containsKey('code') && notEmpty(input.code))) {
      input.addressId = getAddressBy(code: input.address ?: input.code, type: 'ADDR_TYPE_Subnet')?.n_address_id
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
              A.N_PROVIDER_ID   = ${params.firmId}
          START WITH
              A.N_SUBNET_ID     = ${params.addressId ?: params.subnetId}"
          CONNECT BY PRIOR
              A.N_PAR_SUBNET_ID = A.N_SUBNET_ID
          ORDER BY LEVEL ASC
        """, true, params.level ?: params.limit)
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
              RA.N_ADDRESS_ID = A.N_ADDRESS_ID
          AND ${date} BETWEEN RA.D_BEGIN AND NVL(RA.D_END, ${date})
          AND RA.N_ADDR_BIND_TYPE_ID = SYS_CONTEXT('CONST', 'ADDR_ADDR_TYPE_Parent')
          AND RA.N_PROVIDER_ID = ${params.firmId}
          START WITH
              A.N_ADDRESS_ID = ${params.addressId ?: params.subnetId}"
          CONNECT BY PRIOR
              RA.N_PAR_ADDR_ID = RA.N_ADDRESS_ID
          ORDER BY LEVEL ASC
        """, true, params.level ?: params.limit)
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
      input.addressId = getAddressBy(code: input.address ?: input.code, type: 'ADDR_TYPE_Subnet')?.n_address_id
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
                A.N_PROVIDER_ID   = ${params.firmId}
            START WITH
                A.N_SUBNET_ID     = ${params.addressId ?: params.subnetId}"
            CONNECT BY PRIOR
                A.N_PAR_SUBNET_ID = A.N_SUBNET_ID
            ORDER BY LEVEL ASC
          )

          SELECT
              'n_vlan_id',        A.N_VLAN_ID,
              'vc_vlan',          A.VC_VLAN_CODE
          FROM
              SUBNETS             S,
              SI_V_VLAN_ADDRESSES A
          WHERE
              S.N_SUBNET_ID = A.N_ADDRESS_ID
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
                A.N_ADDRESS_ID = ${params.addressId ?: params.subnetId}"
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

  List getSubnetAddressesByVLAN(Map input) {
    LinkedHashMap defaultParams = [
      addressId     : null,
      mask          : null,
      operationDate : local(),
      firmId        : getFirmId(),
      limit         : 0
    ]
    if ((input.containsKey('address') && notEmpty(input.address)) || (input.containsKey('code') && notEmpty(input.code))) {
      input.addressId = getAddressBy(code: input.address ?: input.code, type: 'ADDR_TYPE_VLAN')?.n_address_id
      input.remove('address')
    }
    LinkedHashMap params = mergeParams(defaultParams, input)
    List addresses = []
    String date = encodeDateStr(params.operationDate)

    try {
      if (this.version >= '5.1.2') {
        addresses = hid.queryDatabase("""
          SELECT
              'n_subnet_id',   A.N_SUBNET_ID,
              'vc_subnet',     A.VC_CODE,
              'n_par_addr_id', A.N_PAR_SUBNET_ID
          FROM
              SI_V_PROV_SUBNETS4  A,
              SI_V_VLAN_ADDRESSES V
          WHERE
              V.N_VLAN_ID     = ${params.addressId ?: params.vlanId}
          AND A.N_SUBNET_ID   = V.N_ADDRESS_ID
          AND A.N_PROVIDER_ID = ${params.firmId}
          ORDER BY A.N_VALUE ASC
        """, true, params.limit)
      } else {
        addresses = hid.queryDatabase("""
          SELECT
              'n_subnet_id',   A.N_ADDRESS_ID,
              'vc_subnet',     A.VC_CODE,
              'n_par_addr_id', A.N_PAR_ADDR_ID
          FROM
              SI_V_ADDRESSES   A,
              SI_V_ADDRESSES   V,
              RG_PAR_ADDRESSES RV
          WHERE
              RV.N_ADDRESS_ID        = A.N_ADDRESS_ID
          AND ${date} BETWEEN RV.D_BEGIN AND NVL(RV.D_END, ${date})
          AND RV.N_ADDR_BIND_TYPE_ID = SYS_CONTEXT('CONST', 'ADDR_ADDR_TYPE_SubnetViaVLAN')
          AND RV.N_PROVIDER_ID       = ${params.firmId}
          AND RV.N_PAR_ADDR_ID       = V.N_ADDRESS_ID
          AND A.N_ADDRESS_ID         = ${params.addressId ?: params.vlanId}
          AND V.N_ADDR_TYPE_ID       = SYS_CONTEXT('CONST', 'ADDR_TYPE_VLAN')
          ORDER BY A.N_VALUE ASC
        """, true, params.limit)
      }
    } catch (Exception e){
      logger.error_oracle(e)
    }
    return addresses
  }


  Map getSubnetAddressByVLAN(Map input) {
    List result = getSubnetAddressesByVLAN(input + [limit: 1])
    if (result) {
      return result.getAt(0)
    } else {
      return null
    }
  }

  String getSubnetByVLAN(Map input) {
    return getSubnetAddressByVLAN(input)?.vc_subnet
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