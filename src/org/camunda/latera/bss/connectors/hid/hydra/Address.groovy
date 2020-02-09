package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.StringUtil.notEmpty
import static org.camunda.latera.bss.utils.StringUtil.isEmpty
import static org.camunda.latera.bss.utils.StringUtil.forceNotEmpty
import static org.camunda.latera.bss.utils.StringUtil.joinNonEmpty
import static org.camunda.latera.bss.utils.ListUtil.isList
import static org.camunda.latera.bss.utils.Oracle.encodeDateStr
import static org.camunda.latera.bss.utils.Oracle.decodeBool
import static org.camunda.latera.bss.utils.Oracle.encodeBool
import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import static org.camunda.latera.bss.utils.DateTimeUtil.local
import static org.camunda.latera.bss.utils.MapUtil.keysList
import static org.camunda.latera.bss.utils.Constants.ADDR_TYPE_FactPlace
import static org.camunda.latera.bss.utils.Constants.BIND_ADDR_TYPE_Actual
import static org.camunda.latera.bss.utils.Constants.ADDR_STATE_On
import java.time.temporal.Temporal

trait Address {
  private static String MAIN_ADDRESSES_TABLE      = 'SI_V_ADDRESSES'
  private static String SUBJECT_ADDRESSES_TABLE   = 'SI_V_SUBJ_ADDRESSES'
  private static String OBJECT_ADDRESSES_TABLE    = 'SI_V_OBJ_ADDRESSES'
  private static String OBJECT_ADDRESSES_MV       = 'SI_MV_OBJ_ADDRESSES'
  private static String SUBJECT_ADDRESSES_MV      = 'SI_MV_SUBJ_ADDRESSES'

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

  /**
   * Get addresses table name
   */
  String getMainAddressesTable() {
    return MAIN_ADDRESSES_TABLE
  }

  /**
   * Get subject addresses table name
   */
  String getSubjectAddressesTable() {
    return SUBJECT_ADDRESSES_TABLE
  }

  /**
   * Get object addresses table name
   */
  String getObjectAddressesTable() {
    return OBJECT_ADDRESSES_TABLE
  }

  /**
   * Get subject addresses material view name
   */
  String getSubjectAddressesMV() {
    return SUBJECT_ADDRESSES_MV
  }

  /**
   * Get object addresses material view name
   */
  String getObjectAddressesMV() {
    return OBJECT_ADDRESSES_MV
  }

  /**
   * Get place address fields with their short names
   * @param buildingType {@link CharSequence String}. Custom building type code which will be used instead of 'REGION_TYPE_Building'. Optional
   * @return Map[field, fieldShortName], e.g. {@code [building: 'зд.', home: ..., entrance: '', ...]}
   */
  Map getAddressFields(CharSequence buildingType = null) {
    Map result = getBuildingFields(buildingType)
    ADDRESS_FIELDS.each{ key, value ->
      result[key] = getMessageNameByCode(value)
    }
    return result
  }

  /**
   * Get place address fields
   * @param buildingType {@link CharSequence String}. Custom building type code which will be used instead of 'REGION_TYPE_Building'. Optional
   * @return List[String], e.g. {@code ['building', 'home', 'entrance', ...]}
   */
  List getAddressFieldNames(CharSequence buildingType = null) {
    return keysList(getAddressFields(buildingType))
  }

  /**
   * Get default address type
   */
  String getDefaultAddressType() {
    return getRefCode(getDefaultAddressTypeId())
  }

  /**
   * Get default address type id
   */
  Number getDefaultAddressTypeId() {
    return ADDR_TYPE_FactPlace
  }

  /**
   * Get default address bind type
   */
  String getDefaultAddressBindType() {
    return getRefCode(getDefaultAddressBindTypeId())
  }

  /**
   * Get default address bind type id
   */
  Number getDefaultAddressBindTypeId() {
    return BIND_ADDR_TYPE_Actual
  }

  /**
   * Get default address state
   */
  String getDefaultAddressState() {
    return getRefCode(getDefaultAddressStateId())
  }

  /**
   * Get default address state id
   */
  Number getDefaultAddressStateId() {
    return ADDR_STATE_On
  }

  /**
   * Search for object addresses by different fields value
   * @param objAddressId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param objectId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param addressId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param addrTypeId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: see {@link #getDefaultAddressTypeId()}
   * @param addrType        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parAddressId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param regionId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param rawAddress      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param flat            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param floor           {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param entrance        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param rem             {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bindAddrTypeId  {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: see {@link #getDefaultAddressBindTypeId()}
   * @param bindAddrType    {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parObjAddressId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param stateId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param state           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param operationDate   {@link java.time.Temporal Any date type}. Optional, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Default: current date time, but only if beginDate and endDate are not set
   * @param beginDate       {@link java.time.Temporal Any date type}. Optional, {@link LinkedHashMap Map} with WHERE clause or SELECT query
   * @param endDate         {@link java.time.Temporal Any date type}. Optional, {@link LinkedHashMap Map} with WHERE clause or SELECT query
   * @param limit           {@link Integer}. Optional, default: 0 (unlimited)
   * @param order           {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: C_FL_MAIN DESC
   * @return List[Map] of object address table rows
   */
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
      where.vc_entrance_no = params.entrance
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

  /**
   * Search for one object address by different fields value
   * @param objAddressId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param objectId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param addressId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param addrTypeId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: see {@link #getDefaultAddressTypeId()}
   * @param addrType        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parAddressId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param regionId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param rawAddress      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param flat            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param floor           {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param entrance        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param rem             {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bindAddrTypeId  {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: see {@link #getDefaultAddressBindTypeId()}
   * @param bindAddrType    {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parObjAddressId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param stateId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param state           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param operationDate   {@link java.time.Temporal Any date type}. Optional, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Default: current date time, but only if beginDate and endDate are not set
   * @param beginDate       {@link java.time.Temporal Any date type}. Optional, {@link LinkedHashMap Map} with WHERE clause or SELECT query
   * @param endDate         {@link java.time.Temporal Any date type}. Optional, {@link LinkedHashMap Map} with WHERE clause or SELECT query
   * @param order           {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: C_FL_MAIN DESC
   * @return Map with object address table row
   */
  Map getObjAddressBy(Map input) {
    return getObjAddressesBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Get object address by id
   * @param objAddressId {@link java.math.BigInteger BigInteger}
   * @return Map with object address table row
   */
  Map getObjAddress(def objAddressId) {
    LinkedHashMap where = [
      n_obj_address_id: objAddressId
    ]
    return hid.getTableFirst(getObjectAddressesTable(), where: where)
  }

  /**
   * Search for subject addresses by different fields value
   * @param subjAddressId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subjectId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param addressId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param addrTypeId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: see {@link #getDefaultAddressTypeId()}
   * @param addrType        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parAddressId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param regionId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param rawAddress      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param flat            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param floor           {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param entrance        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param rem             {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bindAddrTypeId  {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: see {@link #getDefaultAddressBindTypeId()}
   * @param bindAddrType    {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param stateId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param state           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit           {@link Integer}. Optional, default: 0 (unlimited)
   * @param order           {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: C_FL_MAIN DESC
   * @return List[Map] of subject address table rows
   */
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
      where.n_subj_address_id = params.subjAddressId
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
      where.vc_entrance_no = params.entrance
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

  /**
   * Search for one subject address by different fields value
   * @param subjAddressId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subjectId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param addressId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param addrTypeId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: see {@link #getDefaultAddressTypeId()}
   * @param addrType        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parAddressId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param regionId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param rawAddress      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param flat            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param floor           {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param entrance        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param rem             {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bindAddrTypeId  {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: see {@link #getDefaultAddressBindTypeId()}
   * @param bindAddrType    {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param stateId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param state           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param order           {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: C_FL_MAIN DESC
   * @return Map with subject address table row
   */
  Map getSubjAddressBy(Map input) {
    return getSubjAddressesBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Get subject address by id
   * @param subjAddressId {@link java.math.BigInteger BigInteger}
   * @return Map with subject address table row
   */
  Map getSubjAddress(def subjAddressId) {
    LinkedHashMap where = [
      n_subj_address_id: subjAddressId
    ]
    return hid.getTableFirst(getSubjectAddressesTable(), where: where)
  }

  /**
   * Search for object or subject addresses by different fields value
   * @param entityAddressId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param entityTypeId       {@link CharSequence String}. Used to determine, is that a subject or object address. Optional
   * @param entityId           {@link java.math.BigInteger BigInteger}. Used to determine, is that a subject or object address. Optional
   * @param addressId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param addrTypeId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: see {@link #getDefaultAddressTypeId()}
   * @param addrType           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parAddressId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code               {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param regionId           {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param rawAddress         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param flat               {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param floor              {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param entrance           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param rem                {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bindAddrTypeId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: see {@link #getDefaultAddressBindTypeId()}
   * @param bindAddrType       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parEntityAddressId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param stateId            {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param state              {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param operationDate      {@link java.time.Temporal Any date type}. Optional, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Default: current date time, but only if beginDate and endDate are not set
   * @param beginDate          {@link java.time.Temporal Any date type}. Optional, {@link LinkedHashMap Map} with WHERE clause or SELECT query
   * @param endDate            {@link java.time.Temporal Any date type}. Optional, {@link LinkedHashMap Map} with WHERE clause or SELECT query
   * @param limit              {@link Integer}. Optional, default: 0 (unlimited)
   * @param order              {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: C_FL_MAIN DESC
   * @return List[Map] of object or subject address table rows
   */
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

  /**
   * Search for one object or subject addresses by different fields value
   * @param entityAddressId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param entityTypeId       {@link CharSequence String}. Optiona, used to determine, is that a subject or object address
   * @param entityId           {@link java.math.BigInteger BigInteger}. Optional, used to determine, is that a subject or object address
   * @param addressId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param addrTypeId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: see {@link #getDefaultAddressTypeId()}
   * @param addrType           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parAddressId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code               {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param regionId           {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param rawAddress         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param flat               {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param floor              {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param entrance           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param rem                {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param bindAddrTypeId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: see {@link #getDefaultAddressBindTypeId()}
   * @param bindAddrType       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parEntityAddressId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, only for object addresses
   * @param stateId            {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param state              {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param operationDate      {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: current date time, but only if beginDate and endDate are not set
   * @param beginDate          {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Only for object addresses. Optional
   * @param endDate            {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Only for object addresses. Optional
   * @param order              {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: C_FL_MAIN DESC
   * @return Map with object or subject address table row
   */
  Map getEntityAddressBy(Map input) {
    return getEntityAddressesBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Get object or subject address by id
   * @param entityOrEntityTypeId {@link java.math.BigInteger BigInteger}. Used to determine, is that a subject or object address
   * @param entityAddressId {@link java.math.BigInteger BigInteger}
   * @return Map with object or subject address table row
   */
  Map getEntityAddress(def entityOrEntityTypeId, def entityAddressId) {
    Boolean isSubj = isSubject(entityOrEntityTypeId)

    if (isSubj) {
      return getSubjAddress(entityAddressId)
    } else {
      return getObjAddress(entityAddressId)
    }
  }

  /**
   * Search for addresses by different fields value
   * @param addressId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param addrTypeId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: see {@link #getDefaultAddressTypeId()}
   * @param addrType     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parAddressId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param regionId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param rawAddress   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param flat         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param floor        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param entrance     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param providerId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: current firm Id
   * @param rem          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit        {@link Integer}. Optional, default: 0 (unlimited)
   * @param order        {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: N_ADDRESS_ID DESC
   * @return List[Map] of address table rows
   */
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
      limit        : 0,
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
      where.vc_entrance_no = params.entrance
    }
    if (params.providerId) {
      where."DECODE(n_provider_id, NULL, ${params.providerId}, n_provider_id)" = params.providerId
    }
    if (params.rem) {
      where.vc_rem = params.rem
    }
    return hid.getTableData(getMainAddressesTable(), where: where, order: params.order, limit: params.limit)
  }

  /**
   * Search for one address by different fields value
   * @param addressId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param addrTypeId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: see {@link #getDefaultAddressTypeId()}
   * @param addrType     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parAddressId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param regionId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param rawAddress   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param flat         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param floor        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param entrance     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param providerId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: current firm Id
   * @param rem          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit        {@link Integer}. Optional, default: 0 (unlimited)
   * @param order        {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: N_ADDRESS_ID DESC
   * @return Map with address table row
   */
  Map getAddressBy(Map input) {
    return getAddressesBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Get address by id
   * @param addressId {@link java.math.BigInteger BigInteger}
   * @return Map with address table row
   */
  Map getAddress(def addressId) {
    LinkedHashMap where = [
      n_address_id: addressId
    ]
    return hid.getTableFirst(getMainAddressesTable(), where: where)
  }

  /**
   * Check if address is empty
   * @param input {@link LinkedHashMap Map} with address fields
   * @return True if address fields are null or empty, false otherwise
   * @see #getAddressFields()
   */
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

  /**
   * Check if address is not empty
   * @see #isAddressEmpty(Map)
   */
  Boolean notAddressEmpty(Map input) {
    return !isAddressEmpty(input)
  }

  /**
   * Check if address and region is empty
   * @param input {@link LinkedHashMap Map} with region and address fields
   * @return True if region and address fields are null or empty, false otherwise
   * @see #isAddressEmpty(Map)
   * @see Region#isRegionEmpty(Map)
   */
  Boolean isRegionAddressEmpty(Map input) {
    return isAddressEmpty(input) && isRegionEmpty(input)
  }

  /**
   * Check if address and region is not empty
   * @see Region#isRegionAddressEmpty(Map)
   */
  Boolean notRegionAddressEmpty(Map input) {
    return !isRegionAddressEmpty(input)
  }

  /**
   * Create or update subject address
   * @param subjAddressId  {@link java.math.BigInteger BigInteger}. Optional
   * @param addressId      {@link java.math.BigInteger BigInteger}. Optional
   * @param subjectId      {@link java.math.BigInteger BigInteger}. Optional
   * @param bindAddrTypeId {@link java.math.BigInteger BigInteger}. Optional
   * @param bindAddrType   {@link CharSequence String}. Optional
   * @param addrTypeId     {@link java.math.BigInteger BigInteger}. Optional
   * @param addrType       {@link CharSequence String}. Optional
   * @param code           {@link CharSequence String}. Optional
   * @param regionId       {@link java.math.BigInteger BigInteger}. Optional
   * @param rawAddress     {@link CharSequence String}. Optional
   * @param flat           {@link CharSequence String}. Optional
   * @param floor          {@link java.math.BigInteger BigInteger}. Optional
   * @param entrance       {@link CharSequence String}. Optional
   * @param rem            {@link CharSequence String}. Optional
   * @param stateId        {@link java.math.BigInteger BigInteger}. Optional
   * @param state          {@link CharSequence String}. Optional
   * @param isMain         {@link Boolean}. Optional
   * @return Map with created subject address (in Oracle API procedure notation)
   */
  private Map putSubjAddress(Map input) {
    LinkedHashMap defaultParams = [
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
    ]
    try {
      LinkedHashMap existingAddress = [:]
      if (notEmpty(input.subjAddressId)) {
        LinkedHashMap subjAddress = getSubjAddress(input.subjAddressId)
        existingAddress = [
          subjAddressId  : subjAddress.n_subj_address_id,
          addressId      : subjAddress.n_address_id,
          subjectId      : subjAddress.n_subject_id,
          bindAddrTypeId : subjAddress.n_subj_addr_type_id,
          addrTypeId     : subjAddress.n_addr_type_id,
          code           : subjAddress.vc_code,
          regionId       : subjAddress.n_region_id,
          rawAddress     : subjAddress.vc_address,
          flat           : subjAddress.vc_flat,
          floor          : subjAddress.n_floor,
          entrance       : subjAddress.n_entrance_no ?: subjAddress.vc_entrance_no,
          rem            : subjAddress.vc_rem,
          stateId        : subjAddress.n_addr_state_id,
          isMain         : decodeBool(subjAddress.c_fl_main)
        ]
      }
      LinkedHashMap params = mergeParams(defaultParams, existingAddress + input)

      logger.info("${params.subjAddressId ? 'Updating' : 'Creating'} address with params ${params}")
      LinkedHashMap args = [
        num_N_SUBJ_ADDRESS_ID   : params.subjAddressId,
        num_N_ADDRESS_ID        : params.addressId,
        num_N_SUBJECT_ID        : params.subjectId,
        num_N_SUBJ_ADDR_TYPE_ID : params.bindAddrTypeId,
        num_N_ADDR_TYPE_ID      : params.addrTypeId,
        vch_VC_CODE             : params.code,
        vch_VC_ADDRESS          : params.rawAddress,
        num_N_REGION_ID         : params.regionId,
        vch_VC_FLAT             : params.flat,
        num_N_FLOOR_NO          : params.floor,
        ch_C_FL_MAIN            : encodeBool(params.isMain),
        num_N_ADDR_STATE_ID     : params.stateId,
        vch_VC_REM              : params.rem,
        vch_VC_ENTRANCE_NO      : params.entrance
      ]

      LinkedHashMap result = hid.execute('SI_ADDRESSES_PKG.SI_SUBJ_ADDRESSES_PUT_EX', args)
      logger.info("   Subject ${params.subjectId} address ${result.num_N_SUBJ_ADDRESS_ID} was ${params.subjAddressId ? 'updated' : 'created'} successfully!")
      return result
    } catch (Exception e){
      logger.error("   Error while ${input.subjAddressId ? 'updating' : 'creating'} a subject address!")
      logger.error_oracle(e)
      return null
    }
  }

  /**
   * Create or update object address
   * @param objAddressId    {@link java.math.BigInteger BigInteger}. Optional
   * @param addressId       {@link java.math.BigInteger BigInteger}. Optional
   * @param objectId        {@link java.math.BigInteger BigInteger}. Optional
   * @param bindAddrTypeId  {@link java.math.BigInteger BigInteger}. Optional
   * @param bindAddrType    {@link CharSequence String}. Optional
   * @param parObjAddressId {@link java.math.BigInteger BigInteger}. Optional
   * @param addrTypeId      {@link java.math.BigInteger BigInteger}. Optional
   * @param addrType        {@link CharSequence String}. Optional
   * @param code            {@link CharSequence String}. Optional
   * @param regionId        {@link java.math.BigInteger BigInteger}. Optional
   * @param rawAddress      {@link CharSequence String}. Optional
   * @param flat            {@link CharSequence String}. Optional
   * @param floor           {@link java.math.BigInteger BigInteger}. Optional
   * @param entrance        {@link CharSequence String}. Optional
   * @param rem             {@link CharSequence String}. Optional
   * @param stateId         {@link java.math.BigInteger BigInteger}. Optional
   * @param state           {@link CharSequence String}. Optional
   * @param isMain          {@link Boolean}. Optional
   * @param beginDate       {@link java.time.Temporal Any date type}. Optional
   * @param endDate         {@link java.time.Temporal Any date type}. Optional
   * @return Map with created object address (in Oracle API procedure notation)
   */
  private Map putObjAddress(Map input) {
    LinkedHashMap defaultParams = [
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
    ]
    try {
      LinkedHashMap existingAddress = [:]
      if (notEmpty(input.objAddressId)) {
        LinkedHashMap objAddress = getObjAddress(input.objAddressId)
        existingAddress = [
          objAddressId    : objAddress.n_obj_address_id,
          addressId       : objAddress.n_address_id,
          objectId        : objAddress.n_object_id,
          bindAddrTypeId  : objAddress.n_obj_addr_type_id,
          parObjAddressId : objAddress.n_par_obj_addr_id,
          addrTypeId      : objAddress.n_addr_type_id,
          code            : objAddress.vc_code,
          regionId        : objAddress.n_region_id,
          rawAddress      : objAddress.vc_address,
          flat            : objAddress.vc_flat,
          floor           : objAddress.n_floor,
          entrance        : objAddress.n_entrance_no ?: objAddressId.vc_entrance_no,
          rem             : objAddress.vc_rem,
          stateId         : objAddress.n_addr_state_id,
          isMain          : decodeBool(objAddress.c_fl_main),
          beginDate       : objAddress.d_begin,
          endDate         : objAddress.d_end
        ]
      }
      LinkedHashMap params = mergeParams(defaultParams, existingAddress + input)

      logger.info("${params.objAddressId ? 'Updating' : 'Creating'} address with params ${params}")
      LinkedHashMap args = [
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
        num_N_FLOOR_NO         : params.floor,
        ch_C_FL_MAIN           : encodeBool(params.isMain),
        dt_D_BEGIN             : params.beginDate ?: local(),
        dt_D_END               : params.endDate,
        num_N_ADDR_STATE_ID    : params.stateId,
        vch_VC_REM             : params.rem,
        vch_VC_ENTRANCE_NO     : params.entrance
      ]

      LinkedHashMap result = hid.execute('SI_ADDRESSES_PKG.SI_OBJ_ADDRESSES_PUT_EX', args)
      logger.info("   Object ${params.objectId} address ${result.num_N_OBJ_ADDRESS_ID} was ${params.objAddressId ? 'updated' : 'created'} successfully!")
      return result
    } catch (Exception e){
      logger.error("   Error while ${input.objAddressId ? 'updating' : 'creating'} an object address!")
      logger.error_oracle(e)
      return null
    }
  }

  /**
   * Create or update subject or object address
   * @param entityAddressId    {@link java.math.BigInteger BigInteger}. Optional
   * @param entityId           {@link java.math.BigInteger BigInteger}. Optional
   * @param entityTypeId       {@link java.math.BigInteger BigInteger}. Used to determine, is that a subject or object address. Optional
   * @param entityType         {@link CharSequence String}. Used to determine, is that a subject or object address. Optional
   * @param bindAddrTypeId     {@link java.math.BigInteger BigInteger}. Optional
   * @param bindAddrType       {@link CharSequence String}. Optional
   * @param parEntityAddressId {@link java.math.BigInteger BigInteger}. Only for object addresses. Optional
   * @param addrTypeId         {@link java.math.BigInteger BigInteger}. Optional
   * @param addrType           {@link CharSequence String}. Optional
   * @param code               {@link CharSequence String}. Optional
   * @param regionId           {@link java.math.BigInteger BigInteger}. Optional
   * @param rawAddress         {@link CharSequence String}. Optional
   * @param flat               {@link CharSequence String}. Optional
   * @param floor              {@link java.math.BigInteger BigInteger}. Optional
   * @param entrance           {@link CharSequence String}. Optional
   * @param rem                {@link CharSequence String}. Optional
   * @param stateId            {@link java.math.BigInteger BigInteger}. Optional
   * @param state              {@link CharSequence String}. Optional
   * @param isMain             {@link Boolean}. Optional
   * @param beginDate          {@link java.time.Temporal Any date type}. Only for object addresses. Optional
   * @param endDate            {@link java.time.Temporal Any date type}. Only for object addresses. Optional
   * @return Map with created subject or object address (in Oracle API procedure notation)
   */
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

    Boolean isSubj = false

    if (notEmpty(params.entityAddressId)) {
      if (notEmpty(params.entityTypeId) || notEmpty(params.entityId)) {
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
          throw new Exception ("No address found!")
        }
      }
    }

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

  /**
   * Create subject address
   * @param subjectId      {@link java.math.BigInteger BigInteger}
   * @param addressId      {@link java.math.BigInteger BigInteger}. Optional
   * @param bindAddrTypeId {@link java.math.BigInteger BigInteger}. Optional
   * @param bindAddrType   {@link CharSequence String}. Optional
   * @param addrTypeId     {@link java.math.BigInteger BigInteger}. Optional
   * @param addrType       {@link CharSequence String}. Optional
   * @param code           {@link CharSequence String}. Optional
   * @param regionId       {@link java.math.BigInteger BigInteger}. Optional
   * @param rawAddress     {@link CharSequence String}. Optional
   * @param flat           {@link CharSequence String}. Optional
   * @param floor          {@link java.math.BigInteger BigInteger}. Optional
   * @param entrance       {@link CharSequence String}. Optional
   * @param rem            {@link CharSequence String}. Optional
   * @param stateId        {@link java.math.BigInteger BigInteger}. Optional
   * @param state          {@link CharSequence String}. Optional
   * @param isMain         {@link Boolean}. Optional
   * @return Map with created subject address (in Oracle API procedure notation)
   */
  Map createSubjAddress(Map input = [:], def subjectId) {
    input.remove('subjAddressId')
    return putSubjAddress(input + [subjectId: subjectId])
  }

  /**
   * Create person address
   * @see #createSubjAddress(Map, def)
   */
  Map createPersonAddress(Map input = [:], def personId) {
    return createSubjAddress(input, personId)
  }

  /**
   * Create company address
   * @see #createSubjAddress(Map, def)
   */
  Map createCompanyAddress(Map input = [:], def companyId) {
    return createSubjAddress(input, companyId)
  }

  /**
   * Create object address
   * @param objectId        {@link java.math.BigInteger BigInteger}
   * @param addressId       {@link java.math.BigInteger BigInteger}. Optional
   * @param bindAddrTypeId  {@link java.math.BigInteger BigInteger}. Optional
   * @param bindAddrType    {@link CharSequence String}. Optional
   * @param parObjAddressId {@link java.math.BigInteger BigInteger}. Optional
   * @param addrTypeId      {@link java.math.BigInteger BigInteger}. Optional
   * @param addrType        {@link CharSequence String}. Optional
   * @param code            {@link CharSequence String}. Optional
   * @param regionId        {@link java.math.BigInteger BigInteger}. Optional
   * @param rawAddress      {@link CharSequence String}. Optional
   * @param flat            {@link CharSequence String}. Optional
   * @param floor           {@link java.math.BigInteger BigInteger}. Optional
   * @param entrance        {@link CharSequence String}. Optional
   * @param rem             {@link CharSequence String}. Optional
   * @param stateId         {@link java.math.BigInteger BigInteger}. Optional
   * @param state           {@link CharSequence String}. Optional
   * @param isMain          {@link Boolean}. Optional
   * @param beginDate       {@link java.time.Temporal Any date type}. Optional
   * @param endDate         {@link java.time.Temporal Any date type}. Optional
   * @return Map with created object address (in Oracle API procedure notation)
   */
  Map createObjAddress(Map input = [:], def objectId) {
    input.remove('objAddressId')
    return putObjAddress(input + [objectId: objectId])
  }

  /**
   * Create or update subject or object address
   * @param entityId           {@link java.math.BigInteger BigInteger}
   * @param entityTypeId       {@link java.math.BigInteger BigInteger}. Used to determine, is that a subject or object address. Optional
   * @param entityType         {@link CharSequence String}. Used to determine, is that a subject or object address. Optional
   * @param bindAddrTypeId     {@link java.math.BigInteger BigInteger}. Optional
   * @param bindAddrType       {@link CharSequence String}. Optional
   * @param parEntityAddressId {@link java.math.BigInteger BigInteger}. Only for object addresses. Optional
   * @param addrTypeId         {@link java.math.BigInteger BigInteger}. Optional
   * @param addrType           {@link CharSequence String}. Optional
   * @param code               {@link CharSequence String}. Optional
   * @param regionId           {@link java.math.BigInteger BigInteger}. Optional
   * @param rawAddress         {@link CharSequence String}. Optional
   * @param flat               {@link CharSequence String}. Optional
   * @param floor              {@link java.math.BigInteger BigInteger}. Optional
   * @param entrance           {@link CharSequence String}. Optional
   * @param rem                {@link CharSequence String}. Optional
   * @param stateId            {@link java.math.BigInteger BigInteger}. Optional
   * @param state              {@link CharSequence String}. Optional
   * @param isMain             {@link Boolean}. Optional
   * @param beginDate          {@link java.time.Temporal Any date type}. Only for object addresses. Optional
   * @param endDate            {@link java.time.Temporal Any date type}. Only for object addresses. Optional
   * @return Map with created subject or object  address (in Oracle API procedure notation)
   */
  Map createEntityAddress(Map input = [:], def entityId) {
    input.remove('entityAddressId')
    return putEntityAddress(input + [entityId: entityId])
  }

  /**
   * Create or update subject or object address
   * @see #createEntityAddress(Map, def)
   */
  Map createEntityAddress(Map input = [:], def entityId, def entityTypeId) {
    input.remove('entityAddressId')
    return putEntityAddress(input + [entityId: entityId, entityTypeId: entityTypeId])
  }

  /**
   * Update subject address
   * @param subjAddressId  {@link java.math.BigInteger BigInteger}
   * @param addressId      {@link java.math.BigInteger BigInteger}. Optional
   * @param bindAddrTypeId {@link java.math.BigInteger BigInteger}. Optional
   * @param bindAddrType   {@link CharSequence String}. Optional
   * @param addrTypeId     {@link java.math.BigInteger BigInteger}. Optional
   * @param addrType       {@link CharSequence String}. Optional
   * @param code           {@link CharSequence String}. Optional
   * @param regionId       {@link java.math.BigInteger BigInteger}. Optional
   * @param rawAddress     {@link CharSequence String}. Optional
   * @param flat           {@link CharSequence String}. Optional
   * @param floor          {@link java.math.BigInteger BigInteger}. Optional
   * @param entrance       {@link CharSequence String}. Optional
   * @param rem            {@link CharSequence String}. Optional
   * @param stateId        {@link java.math.BigInteger BigInteger}. Optional
   * @param state          {@link CharSequence String}. Optional
   * @param isMain         {@link Boolean}. Optional
   * @return Map with updated subject address (in Oracle API procedure notation)
   */
  Map updateSubjAddress(Map input = [:], def subjAddressId) {
    return putSubjAddress(input + [subjAddressId: subjAddressId])
  }

  /**
   * Update person address
   * @see #updateSubjAddress(Map, def)
   */
  Map updatePersonAddress(Map input = [:], def subjAddressId) {
    return updateSubjAddress(input, subjAddressId)
  }

  /**
   * Update company address
   * @see #updateSubjAddress(Map, def)
   */
  Map updateCompanyAddress(Map input = [:], def subjAddressId) {
    return updateSubjAddress(input, subjAddressId)
  }

  /**
   * Update object address
   * @param objAddressId    {@link java.math.BigInteger BigInteger}
   * @param addressId       {@link java.math.BigInteger BigInteger}. Optional
   * @param bindAddrTypeId  {@link java.math.BigInteger BigInteger}. Optional
   * @param bindAddrType    {@link CharSequence String}. Optional
   * @param parObjAddressId {@link java.math.BigInteger BigInteger}. Optional
   * @param addrTypeId      {@link java.math.BigInteger BigInteger}. Optional
   * @param addrType        {@link CharSequence String}. Optional
   * @param code            {@link CharSequence String}. Optional
   * @param regionId        {@link java.math.BigInteger BigInteger}. Optional
   * @param rawAddress      {@link CharSequence String}. Optional
   * @param flat            {@link CharSequence String}. Optional
   * @param floor           {@link java.math.BigInteger BigInteger}. Optional
   * @param entrance        {@link CharSequence String}. Optional
   * @param rem             {@link CharSequence String}. Optional
   * @param stateId         {@link java.math.BigInteger BigInteger}. Optional
   * @param state           {@link CharSequence String}. Optional
   * @param isMain          {@link Boolean}. Optional
   * @param beginDate       {@link java.time.Temporal Any date type}. Optional
   * @param endDate         {@link java.time.Temporal Any date type}. Optional
   * @return Map with updated object address (in Oracle API procedure notation)
   */
  Map updateObjAddress(Map input = [:], def objAddressId) {
    return putObjAddress(input + [objAddressId: objAddressId])
  }

  /**
   * Update subject or object address
   * @param entityAddressId    {@link java.math.BigInteger BigInteger}
   * @param addressId          {@link java.math.BigInteger BigInteger}. Optional
   * @param bindAddrTypeId     {@link java.math.BigInteger BigInteger}. Optional
   * @param bindAddrType       {@link CharSequence String}. Optional
   * @param parEntityAddressId {@link java.math.BigInteger BigInteger}. Optional
   * @param addrTypeId         {@link java.math.BigInteger BigInteger}. Optional
   * @param addrType           {@link CharSequence String}. Optional
   * @param code               {@link CharSequence String}. Optional
   * @param regionId           {@link java.math.BigInteger BigInteger}. Optional
   * @param rawAddress         {@link CharSequence String}. Optional
   * @param flat               {@link CharSequence String}. Optional
   * @param floor              {@link java.math.BigInteger BigInteger}. Optional
   * @param entrance           {@link CharSequence String}. Optional
   * @param rem                {@link CharSequence String}. Optional
   * @param stateId            {@link java.math.BigInteger BigInteger}. Optional
   * @param state              {@link CharSequence String}. Optional
   * @param isMain             {@link Boolean}. Optional
   * @param beginDate          {@link java.time.Temporal Any date type}. Optional
   * @param endDate            {@link java.time.Temporal Any date type}. Optional
   * @return Map with updated subject or object address (in Oracle API procedure notation)
   */
  Map updateEntityAddress(Map input = [:], def entityAddressId) {
    return putEntityAddress(input + [entityAddressId: entityAddressId])
  }

  /**
   * Get place address fields with short name position indicator and value
   * @param input {@link LinkedHashMap Map} with address fields
   * @return List[List[fieldType, isAfter, value]], e.g. {@code [['building', 'N', 'зд.'], ['home', 'N', 'д.'], ..]}
   */
  List getAddressItems(Map input) {
    List addressItems = []
    getAddressFields().each{ type, value ->
      addressItems.add([value, 'N', input[type] ?: ''])
    }
    return addressItems
  }


  /**
   * Calculate place full address from its parts
   * @param input {@link LinkedHashMap Map} with region and address fields
   * @return String with full address, e.g. {@code 'Россия, г. Москва, ул. Пример, зд. 1, д. 2, кв. 5'}
   */
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

  /**
   * Delete subject address
   * @param subjAddressId {@link java.math.BigInteger BigInteger}
   * @return True if subject address was successfully deleted, false otherwise
   */
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

  /**
   * Delete object address
   * @param objAddressId {@link java.math.BigInteger BigInteger}
   * @return True if object address was successfully deleted, false otherwise
   */
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

  /**
   * Delete subject or object address
   * @param entityAddressId {@link java.math.BigInteger BigInteger}
   * @return True if subject or object address was successfully deleted, false if not or address not found
   */
  Boolean deleteEntityAddress(def entityAddressId) {
    return deleteEntityAddress([entityAddressId: entityAddressId])
  }

  /**
   * Delete subject or object address
   * @param input {@link LinkedHashMap Map} with address to search for
   * @return True if subject or object address was successfully deleted, false if not or address not found
   */
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

    if (notEmpty(params.entityAddressId)) {
      if (notEmpty(params.entityTypeId) || notEmpty(params.entityId)) {
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

  /**
   * Delete person address
   * @param personId      {@link java.math.BigInteger BigInteger}
   * @param subjAddressId {@link java.math.BigInteger BigInteger}
   * @see #deleteSubjAddress(def)
   * @deprecated
   */
  Boolean deletePersonAddress(def personId, def subjAddressId) {
    return deleteSubjAddress(subjAddressId)
  }

  /**
   * Delete person address
   * @param subjAddressId {@link java.math.BigInteger BigInteger}
   * @see #deleteSubjAddress(def)
   */
  Boolean deletePersonAddress(def subjAddressId) {
    return deleteSubjAddress(subjAddressId)
  }

  /**
   * Delete company address
   * @param companyId     {@link java.math.BigInteger BigInteger}
   * @param subjAddressId {@link java.math.BigInteger BigInteger}
   * @see #deleteSubjAddress(def)
   * @deprecated
   */
  Boolean deleteCompanyAddress(def companyId, def subjAddressId) {
    return deleteSubjAddress(subjAddressId)
  }

  /**
   * Delete company address
   * @param subjAddressId {@link java.math.BigInteger BigInteger}
   * @see #deleteSubjAddress(def)
   */
  Boolean deleteCompanyAddress(def subjAddressId) {
    return deleteSubjAddress(subjAddressId)
  }

  /**
   * Close object address
   * @param objAddressId {@link java.math.BigInteger BigInteger}
   * @param endDate      {@link java.time.Temporal Any date type}. Default: current date time. Optional
   * @return True if object address was successfully closed, false otherwise
   */
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

  /**
   * Close object address
   * @param input {@link LinkedHashMap Map} with address to close
   * @see #getObjAddress(def)
   * @see #closeObjAddress(def, Temporal)
   */
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

  /**
   * Close entity address
   * @see #closeObjAddress(Map)
   */
  Boolean closeEntityAddress(Map input) {
    def objectId     = input.entityId
    def objAddressId = input.entityAddressId
    input.remove('entityId')
    input.remove('entityAddressId')
    return closeObjAddress(input + [objectId: objectId, objAddressId: objAddressId])
  }

  /**
   * Get free IPv4 addresses
   * @param objectId         {@link java.math.BigInteger BigInteger}. Object id to use resource pool restrictions. Optional
   * @param subnetAddressId  {@link java.math.BigInteger BigInteger}. Subnet address id to use for search for free IP address. Optional
   * @param subnetAddressIds List[{@link java.math.BigInteger BigInteger}]. Subnet address ids to use for search for free IP address. Optional
   * @param subnetAddress    {@link CharSequence String}. Subnet address code to use for search for free IP address. Optional
   * @param subnetAddresses  List[{@link CharSequence String}]. Subnet addresses codes to use for search for free IP address. Optional
   * @param vlanId           {@link java.math.BigInteger BigInteger}. Vlan id to use for restricting IP subnets list. Optional
   * @param vlan             {@link CharSequence String}. Vlan code to use for restricting IP subnets list. Optional
   * @param isPublic         {@link Boolean}. True to get only public IPv4 addresses, false only for private ones, null to disable filtration. Optional
   * @param firmId           {@link java.math.BigInteger BigInteger}. Provider id to get addresses from. Optional, default: current firm Id
   * @param limit            {@link Integer}. Limit for result count. Optional, default: 10
   * @return List[Map] with IPv4 address data, e.g. {@code [[vc_ip: '10.10.10.10', n_subnet_id: 1234142301, vc_subnet: '10.10.0.0/24']]}
   */
  List getFreeIPAddresses(Map input) {
    LinkedHashMap defaultParams = [
      groupId           : null,
      objectId          : null,
      subnetAddressId   : null,
      vlanId            : null,
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

    if (params.subnetAddressId && isEmpty(params.subnetAddressIds)) {
      params.subnetAddressIds = [params.subnetAddressId]
      params.remove('subnetAddressId')
    }
    if (isEmpty(params.subnetAddressIds)) {
      params.subnetAddressIds = []
    }
    if (params.vlanId) {
      List subnetIdsByVLAN = getSubnetAddressesByVLAN(addressId: params.vlanId).collect{ Map subnet -> toIntSafe(subnet.n_subnet_id) }
      if (params.subnetAddressIds) {
        params.subnetAddressIds = params.subnetAddressIds.findAll { def subnetAddrId -> toIntSafe(subnetAddrId) in subnetIdsByVLAN }
      } else {
        params.subnetAddressIds = subnetIdsByVLAN
      }
    }
    List addresses = []

    String filterReal = '1=1'
    if (params.isPublic != null) {
      filterReal = "UTILS_ADDRESSES_PKG_S.IS_REAL_IP(A.N_VALUE) = '${params.isPublic ? 'Y' : 'N'}'"
    }

    if (isEmpty(params.subnetAddressIds)) {
      params.subnetAddressIds = [null]
    }

    params.subnetAddressIds.each { def subnetAddressId ->
      try {
        if (isEmpty(addresses)) {
          if (notEmpty(params.objectId)) {
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

  /**
   * Get free IPv4 address
   * @param objectId         {@link java.math.BigInteger BigInteger}. Object id to use resource pool restrictions. Optional
   * @param subnetAddressId  {@link java.math.BigInteger BigInteger}. Subnet address id to use for search for free IP address
   * @param subnetAddressIds List[{@link java.math.BigInteger BigInteger}]. Subnet address ids to use for search for free IP address. Optional
   * @param subnetAddress    {@link CharSequence String}. Subnet address code to use for search for free IP address. Optional
   * @param subnetAddresses  List[{@link CharSequence String}]. Subnet addresses codes to use for search for free IP address. Optional
   * @param vlanId           {@link java.math.BigInteger BigInteger}. Vlan id to use for restricting IP subnets list. Optional
   * @param vlan             {@link CharSequence String}. Vlan code to use for restricting IP subnets list. Optional
   * @param isPublic         {@link Boolean}. True to get only public IPv4 addresses, false only for private ones, null to disable filtration. Optional
   * @param firmId           {@link java.math.BigInteger BigInteger}. Provider id to get addresses from. Optional, default: current firm Id
   * @return Map with IPv4 address data, e.g. {@code [vc_ip: '10.10.10.10', n_subnet_id: 1234142301, vc_subnet: '10.10.0.0/24']}
   */
  Map getFreeIPAddress(Map input) {
    List result = getFreeIPAddresses(input + [limit: 1])
    if (result) {
      return result.getAt(0)
    } else {
      return null
    }
  }

  /**
   * Get free IPv4 address code
   * @return String with IPv4 address code, e.g. {@code '10.10.10.10'}
   * @see #getFreeIPAddress(Map)
   */
  String getFreeIP(Map input) {
    return getFreeIPAddress(input)?.vc_ip
  }

  /**
   * Get free IPv6 addresses
   * @param objectId         {@link java.math.BigInteger BigInteger}. Object id to use resource pool restrictions. Optional
   * @param subnetAddressId  {@link java.math.BigInteger BigInteger}. Subnet address id to use for search for free IP address
   * @param subnetAddressIds List[{@link java.math.BigInteger BigInteger}]. Subnet address ids to use for search for free IP address. Optional
   * @param subnetAddress    {@link CharSequence String}. Subnet address code to use for search for free IP address. Optional
   * @param subnetAddresses  List[{@link CharSequence String}]. Subnet addresses codes to use for search for free IP addressltration. Optional
   * @param firmId           {@link java.math.BigInteger BigInteger}. Provider id to get addresses from. Optional, default: current firm Id
   * @param limit            Limit for result count. Optional, default: 10
   * @return List[Map] with IPv6 address data, e.g. {@code [[vc_ip: '2400:ca00:2000:a000::', n_subnet_id: 1234142301, vc_subnet: '2400:ca00:2000:a000::/52 ']]}
   */
  List getFreeIPv6Addresses(Map input) {
    LinkedHashMap defaultParams = [
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
      input.subnetAddressIds = getAddressesBy(code: [in: input.subnetAddresses], addrType: 'ADDR_TYPE_Subnet6', order: [n_value: 'asc', vc_value: 'asc']).collect {Map address -> toIntSafe(address.n_address_id) }
      input.remove('subnetAddresses')
    }
    LinkedHashMap params = mergeParams(defaultParams, input)
    if (params.subnetAddressId && isEmpty(params.subnetAddressIds)) {
      params.subnetAddressIds = [params.subnetAddressId]
      params.remove('subnetAddressId')
    }
    if (isEmpty(params.subnetAddressIds)) {
      params.subnetAddressIds = []
    }
    if (isEmpty(params.subnetAddressIds)) {
      params.subnetAddressIds = [null]
    }

    List addresses = []

    params.subnetAddressIds.each { def subnetAddressId ->
      try {
        if (!addresses) {
          if (notEmpty(params.objectId)) {
            if (notEmpty(subnetAddressId)) {
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
      } catch (Exception e) {}
    }
    return addresses
  }

  /**
   * Get free IPv6 address
   * @param objectId         {@link java.math.BigInteger BigInteger}. Object id to use resource pool restrictions. Optional
   * @param subnetAddressId  {@link java.math.BigInteger BigInteger}. Subnet address id to use for search for free IP address
   * @param subnetAddressIds List[{@link java.math.BigInteger BigInteger}]. Subnet address ids to use for search for free IP address. Optional
   * @param subnetAddress    {@link CharSequence String}. Subnet address code to use for search for free IP address. Optional
   * @param subnetAddresses  List[{@link CharSequence String}]. Subnet addresses codes to use for search for free IP addressltration. Optional
   * @param firmId           {@link java.math.BigInteger BigInteger}. Provider id to get addresses from. Optional, default: current firm Id
   * @return Map with IPv6 address data, e.g. {@code [[vc_ip: '2400:ca00:2000:a000::', n_subnet_id: 1234142301, vc_subnet: '2400:ca00:2000:a000::/52 ']]}
   */
  Map getFreeIPv6Address(Map input) {
    List result = getFreeIPv6Addresses(input + [limit: 1])
    if (result) {
      return result.getAt(0)
    } else {
      return null
    }
  }

  /**
   * Get free IPv6 address code
   * @return SIPv6 address code, e.g. {@code '2400:ca00:2000:a000::'}
   * @see #getFreeIPv6Address(Map)
   */
  String getFreeIPv6(Map input) {
    return getFreeIPv6Address(input)?.vc_ip
  }

  /**
   * Get free IPv6 subnet
   * @return IPv6 subnet code, e.g. {@code '2400:ca00:2000:a000::/60'}
   * @see #getFreeIPv6(Map)
   * @see #getSubnetv6Mask()
   */
  String getFreeIPv6Subnet(Map input) {
    String subnet = null
    try {
      subnet = "${getFreeIPv6(input)}/${getSubnetv6Mask()}"
    } catch (Exception e){
      logger.error_oracle(e)
    }
    return subnet
  }

  /**
   * Get free telephone addresses
   * @param objectId      {@link java.math.BigInteger BigInteger}. Object id to use resource pool restrictions. Optional
   * @param telCodeId     {@link java.math.BigInteger BigInteger}. Telephone code address id to use for search for free phone numbers. Optional
   * @param telCodeIds    List[{@link java.math.BigInteger BigInteger}]. Telephone code ids to use for search for free phone numbers. Optional
   * @param telCode       {@link CharSequence String}. Telephone code to use for search for free phone numbers. Optional
   * @param telCodes      List[{@link CharSequence String}]. Telephone codes to use for search for free phone numbers. Optional
   * @param firmId        {@link java.math.BigInteger BigInteger}. Provider id to get addresses from. Optional, default: current firm Id
   * @param limit         Limit for result count. Optional, default: 10
   * @return List[Map] with telephone address data, e.g. {@code [[vc_phone_number: '79123456789', n_telcode_id: 1234142301, vc_tel_code: '79123']]}
   */
  List getFreeTelephoneNumbers(Map input) {
    LinkedHashMap defaultParams = [
      groupId       : null,
      objectId      : null,
      telCodeId     : null,
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

  /**
   * Get free telephone address
   * @param objectId      {@link java.math.BigInteger BigInteger}. Object id to use resource pool restrictions. Optional
   * @param telCodeId     {@link java.math.BigInteger BigInteger}. Telephone code address id to use for search for free phone numbers. Optional
   * @param telCodeIds    List[{@link java.math.BigInteger BigInteger}]. Telephone code ids to use for search for free phone numbers. Optional. Optional
   * @param telCode       {@link CharSequence String}. Telephone code to use for search for free phone numbers. Optional
   * @param telCodes      List[{@link CharSequence String}]. Telephone codes to use for search for free phone numbers. Optional
   * @param firmId        {@link java.math.BigInteger BigInteger}. Provider id to get addresses from. Optional, default: current firm Id
   * @return Map with telephone address data, e.g. {@code [vc_phone_number: '79123456789', n_telcode_id: 1234142301, vc_tel_code: '79123']}
   */
  Map getFreeTelephoneNumber(Map input) {
    List result = getFreeTelephoneNumbers(input + [limit: 1])
    if (result) {
      return result.getAt(0)
    } else {
      return null
    }
  }

  /**
   * Get free telephone number
   * @return Telephone number, e.g. {@code '79123456789'}
   * @see #getFreeTelephoneNumber(Map)
   */
  String getFreePhoneNumber(Map input) {
    return getFreeTelephoneNumber(input)?.vc_phone_number
  }

  /**
   * Get free IPv4 subnet addresses
   * !!! Unlike IPv4, IPv6 or telephones, subnets should already exist in database to return here !!!
   * @param rootId        {@link java.math.BigInteger BigInteger}. Subnet address id to use for selecting child subnets. Optional
   * @param mask          {@link java.math.BigInteger BigInteger}. Mask of subnet to return, e.g. 30. Optional
   * @param vlanId        {@link java.math.BigInteger BigInteger}. Vlan id to use for restricting IP subnets list. Optional
   * @param vlan          {@link CharSequence String}. Vlan code to use for restricting IP subnets list. Optional
   * @param isPublic      {@link Boolean}. True to get only public subnets, false only for private ones, null to disable filtration. Optional
   * @param firmId        {@link java.math.BigInteger BigInteger}. Provider id to get addresses from. Optional, default: current firm Id
   * @param limit         Limit for result count. Optional, default: 10
   * @return List[Map] with IPv4 subnet data, e.g. {@code [[n_subnet_id: 1234142301, vc_subnet: '10.10.0.0/24', n_par_addr_id: 1234142201]]}
   */
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
    String notAssigned = """
      NOT EXISTS ( -- Не привязаны к оборудованию
      SELECT 1
      FROM
          SI_V_OBJ_ADDRESSES OA
      WHERE
          OA.N_ADDR_STATE_ID = SYS_CONTEXT('CONST', 'ADDR_STATE_On')
      AND OA.N_ADDRESS_ID    = FA.N_SUBNET_ID
      AND ${date} BETWEEN OA.D_BEGIN AND NVL(OA.D_END, ${date})
    )"""

    String notAssignedChild = params.mask == '30' ? '1=1' : """NOT EXISTS ( -- И нет дочерних привязок к оборудованию
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

    // WARN Чтобы получение подсети работало, подсети нужного размера должны быть нарезаны
    try {
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
    } catch (Exception e){
      logger.error_oracle(e)
    }
    return addresses
  }

  /**
   * Get free IPv4 subnet address
   * !!! Unlike IPv4, IPv6 or telephones, subnets should already exist in database to return here !!!
   * @param rootId           {@link java.math.BigInteger BigInteger}. Subnet address id to use for selecting child subnets. Optional
   * @param mask             {@link java.math.BigInteger BigInteger}. Mask of subnet to return, e.g. 30. Optional
   * @param vlanId           {@link java.math.BigInteger BigInteger}. Vlan id to use for restricting IP subnets list. Optional
   * @param vlan             {@link CharSequence String}. Vlan code to use for restricting IP subnets list. Optional
   * @param isPublic         {@link Boolean}. True to get only public subnets, false only for private ones, null to disable filtration. Optional
   * @param firmId           {@link java.math.BigInteger BigInteger}. Provider id to get addresses from. Optional, default: current firm Id
   * @return Map with IPv4 subnet data, e.g. {@code [n_subnet_id: 1234142301, vc_subnet: '10.10.0.0/24', n_par_addr_id: 1234142201]}
   */
  Map getFreeSubnetAddress(Map input) {
    List result = getFreeSubnetAddresses(input + [limit: 1])
    if (result) {
      return result.getAt(0)
    } else {
      return null
    }
  }

  /**
   * Get free IPv4 subnet code
   * @return IPv4 subnet code, e.g. {@code '10.10.0.0/24'}
   * @see #getFreeSubnetAddress(Map)
   */
  String getFreeSubnet(Map input) {
    return getFreeSubnetAddress(input)?.vc_subnet
  }

  /**
   * Get subnet id by IP code
   */
  Number getSubnetIdByIP(CharSequence ip) {
    def subnetId = null
    try {
      subnetId = toIntSafe(getAddressBy(code: ip, addrType: [in: ['ADDR_TYPE_IP', 'ADDR_TYPE_IP6']])?.n_par_addr_id)
    } catch (Exception e){
      logger.error_oracle(e)
    }
    return toIntSafe(subnetId)
  }

  /**
   * Get subnet code by IP code
   */
  String getSubnetByIP(CharSequence ip) {
    def subnetId = getSubnetIdByIP(ip)
    String subnet = null
    if (subnetId) {
      subnet = getAddress(addressId: subnetId, addrType: 'ADDR_TYPE_SUBNET')?.vc_code
    }
    return subnet
  }

  /**
   * Get subnet mask by subnet address id
   */
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

  /**
   * Get subnet mask by subnet code
   */
  String getSubnetMask(CharSequence subnet) {
    String mask = null
    def subnetId = getAddress(code: subnet, addrType: 'ADDR_TYPE_SUBNET')
    if (subnetId) {
      mask = getSubnetMaskById(subnetId)
    }
    return mask
  }

  /**
   * Get subnet mask by IP address id
   */
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

  /**
   * Get subnet mask by IP code
   */
  String getIPMask(CharSequence ip) {
    String mask = null
    def subnetId = getSubnetIdByIP(ip)
    if (subnetId) {
      mask = getSubnetMaskById(subnetId)
    }
    return mask
  }

  /**
   * Get gateway code by subnet address id
   */
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

  /**
   * Get gateway code by subnet code
   */
  String getSubnetGateway(CharSequence subnet) {
    String gateway = null
    def subnetId = getAddress(code: subnet, addrType: 'ADDR_TYPE_SUBNET')
    if (subnetId) {
      gateway = getSubnetGatewayById(subnetId)
    }
    return gateway
  }

  /**
   * Get gateway code by IP address id
   */
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

  /**
   * Get gateway code by IP code
   */
  String getIPGateway(CharSequence ip) {
    String gateway = null
    def subnetId = getSubnetIdByIP(ip)
    if (subnetId) {
      gateway = getSubnetGatewayById(subnetId)
    }
    return gateway
  }

  /**
   * Get IPv6 subnet mask
   * @see #getIPv6Mask()
   */
  String getSubnetv6Mask() {
    return getIPv6Mask()
  }

  /**
   * Get IPv6 address mask
   * @return String with IPv6 address mask from database, e.g. '60'
   */
  String getIPv6Mask() {
    return getParamValueBy(param: 'PAR_IPv6SubnetLength', subjectId: getFirmId())?.n_value
  }

  /**
   * Get all parent IPv4 subnets for some subnet
   * @param addressId     {@link java.math.BigInteger BigInteger}. Subnet address id. Optional
   * @param address       {@link CharSequence String}. Subnet code. Optional
   * @param firmId        {@link java.math.BigInteger BigInteger}. Provider id to get addresses from. Optional, default: current firm Id
   * @param limit         Limit for result count. Optional, default: 10
   * @return List[Map] with IPv4 subnet data, e.g. {@code [[n_address_id: 1234142301, code: '10.10.0.0/24', n_value: 168430080, n_par_addr_id: 1234142201, level: 0]]}
   */
  List getParentSubnetAddresses(Map input) {
    LinkedHashMap defaultParams = [
      addressId : null,
      firmId    : getFirmId(),
      limit     : 0
    ]
    if ((input.containsKey('address') && notEmpty(input.address)) || (input.containsKey('code') && notEmpty(input.code))) {
      input.addressId = getAddressBy(code: input.address ?: input.code, type: 'ADDR_TYPE_Subnet')?.n_address_id
      input.remove('address')
    }
    LinkedHashMap params = mergeParams(defaultParams, input)
    List addresses = []

    try {
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
    } catch (Exception e){
      logger.error_oracle(e)
    }
    return addresses
  }

  /**
   * Get VLAN address for subnet
   * @param addressId     {@link java.math.BigInteger BigInteger}. Subnet address id. Optional
   * @param address       {@link CharSequence String}. Subnet code. Optional
   * @param firmId        {@link java.math.BigInteger BigInteger}. Provider id to get addresses from. Optional, default: current firm Id
   * @return Map with VLAN data, e.g. {@code [n_vlan_id: 1234142301, vc_vlan: '1234']}
   */
  Map getVLANAddressBySubnet(Map input) {
    LinkedHashMap defaultParams = [
      addressId : null,
      firmId    : getFirmId()
    ]
    if ((input.containsKey('address') && notEmpty(input.address)) || (input.containsKey('code') && notEmpty(input.code))) {
      input.addressId = getAddressBy(code: input.address ?: input.code, type: 'ADDR_TYPE_Subnet')?.n_address_id
      input.remove('address')
    }
    LinkedHashMap params = mergeParams(defaultParams, input)
    LinkedHashMap address = null

    try {
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
    } catch (Exception e){
      logger.error_oracle(e)
    }
    return address
  }

  /**
   * Get VLAN code for subnet
   * @return VLAN code, e.g. '1234'
   * @see getVLANAddressBySubnet
   */
  String getVLANBySubnet(Map input) {
    return getVLANAddressBySubnet(input)?.vc_vlan
  }

  /**
   * Get subnet addresses by VLAN
   * @param addressId     {@link java.math.BigInteger BigInteger}. VLAN address id. Optional
   * @param address       {@link CharSequence String}. VLAN code. Optional
   * @param firmId        {@link java.math.BigInteger BigInteger}. Provider id to get addresses from. Optional, default: current firm Id
   * @param limit         Limit for result count. Optional, default: 10
   * @return List[Map] of subnet address data, e.g. {@code [[n_subnet_id: 1234142301, vc_subnet: '10.10.0.0/24', n_par_addr_id: 1234142201]]}
   */
  List getSubnetAddressesByVLAN(Map input) {
    LinkedHashMap defaultParams = [
      addressId     : null,
      firmId        : getFirmId(),
      limit         : 0
    ]
    if ((input.containsKey('address') && notEmpty(input.address)) || (input.containsKey('code') && notEmpty(input.code))) {
      input.addressId = getAddressBy(code: input.address ?: input.code, addrType: 'ADDR_TYPE_VLAN')?.n_address_id
      input.remove('address')
      input.remove('code')
    }
    LinkedHashMap params = mergeParams(defaultParams, input)
    List addresses = []

    try {
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
    } catch (Exception e){
      logger.error_oracle(e)
    }
    return addresses
  }

  /**
   * Get subnet address by VLAN
   * @param addressId     {@link java.math.BigInteger BigInteger}. VLAN address id. Optional
   * @param address       {@link CharSequence String}. VLAN code. Optional
   * @param firmId        {@link java.math.BigInteger BigInteger}. Provider id to get addresses from. Optional, Optional, default: current firm Id
   * @return Map with subnet address data, e.g. {@code [n_subnet_id: 1234142301, vc_subnet: '10.10.0.0/24', n_par_addr_id: 1234142201]}
   */
  Map getSubnetAddressByVLAN(Map input) {
    List result = getSubnetAddressesByVLAN(input + [limit: 1])
    if (result) {
      return result.getAt(0)
    } else {
      return null
    }
  }

  /**
   * Get subnet code by VLAN
   * @return Subnet code, e.g. '10.10.0.0/24'
   * @see #getSubnetAddressByVLAN(Map)
   */
  String getSubnetByVLAN(Map input) {
    return getSubnetAddressByVLAN(input)?.vc_subnet
  }

  /**Refresh object addresses quick search material view
   * @see Search#refreshMaterialView(CharSequence,CharSequence)
   */
  Boolean refreshObjAddresses(CharSequence method = 'C') {
    return refreshMaterialView(getSubjectAddressesMV(), method)
  }

  /**Refresh subject addresses quick search material view
   * @see Search#refreshMaterialView(CharSequence,CharSequence)
   */
  Boolean refreshSubjAddresses(CharSequence method = 'C') {
    return refreshMaterialView(getObjectAddressesMV(), method)
  }

  /**Refresh object and subject addresses quick search material views
   * @see Search#refreshMaterialView(CharSequence,CharSequence)
   */
  Boolean refreshEntityAddresses(CharSequence method = 'C') {
    return refreshObjAddresses(method) && refreshSubjAddresses(method)
  }
}
