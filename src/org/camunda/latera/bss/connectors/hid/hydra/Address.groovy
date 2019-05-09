package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.utils.Oracle
import org.camunda.latera.bss.utils.DateTimeUtil

trait Address {
  private static String MAIN_ADDRESSES_TABLE    = 'SI_V_ADDRESSES'
  private static String SUBJECT_ADDRESSES_TABLE = 'SI_V_SUBJ_ADDRESSES'
  private static String OBJECT_ADDRESSES_TABLE  = 'SI_V_OBJ_ADDRESSES'

  private static String DEFAULT_ADDRESS_TYPE      = 'ADDR_TYPE_FactPlace'
  private static String DEFAULT_ADDRESS_BIND_TYPE = 'BIND_ADDR_TYPE_Serv'
  private static String DEFAULT_ADDRESS_STATE     = 'ADDR_STATE_On'
  private static LinkedHashMap ADDRESS_ITEMS = [
    building  : 'зд.',
    home      : 'д.',
    corpus    : 'корп.',
    construct : 'стр.',
    entrance  : 'подъезд',
    floor     : 'этаж',
    flat      : 'кв.'
  ]
  private static List ADDRESS_ITEMS_NAMES = ADDRESS_ITEMS.keySet() as List

  def getMainAddressesTable() {
    return MAIN_ADDRESSES_TABLE
  }

  def getSubjectAddressesTable() {
    return SUBJECT_ADDRESSES_TABLE
  }

  def getObjectAddressesTable() {
    return OBJECT_ADDRESSES_TABLE
  }

  def getDefaultAddressType() {
    return DEFAULT_ADDRESS_TYPE
  }

  def getAddressItems() {
    return ADDRESS_ITEMS
  }

  def getAddressItemsNames() {
    return ADDRESS_ITEMS_NAMES
  }

  def getDefaultAddressTypeId() {
    return getRefIdByCode(getDefaultAddressType())
  }

  def getDefaultAddressBindType() {
    return DEFAULT_ADDRESS_BIND_TYPE
  }

  def getDefaultAddressBindTypeId() {
    return getRefIdByCode(getDefaultAddressBindType())
  }

  def getDefaultAddressState() {
    return DEFAULT_ADDRESS_STATE
  }

  def getDefaultAddressStateId() {
    return getRefIdByCode(getDefaultAddressState())
  }

  List getObjAddresses(LinkedHashMap input) {
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
      endDate         : null
    ], input)

    LinkedHashMap where = [:]
    if (params.objAddressId) {
      where.objAddressId = params.objAddressId
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
      where.n_entrance_no = params.entrance
    }
    if (params.rem) {
      where.vc_rem = params.rem
    }
    if (params.stateId) {
      where.n_addr_state_id = params.stateId
    }
    if (params.isMain != null) {
      where.c_fl_main = Oracle.encodeBool(params.isMain)
    }
    // Only for objects addresses
    if (params.beginDate) {
      where.d_begin = params.beginDate
    }
    if (params.endDate) {
      where.d_end = params.endDate
    }
    if (!params.operationDate && !params.endDate && !params.beginDate) {
      params.operationDate = DateTimeUtil.now()
    }
    if (params.operationDate) {
      String oracleDate = Oracle.encodeDateStr(params.operationDate)
      where[oracleDate] = [BETWEEN: "D_BEGIN AND NVL(D_END, ${oracleDate})"]
    }
    return hid.getTableData(getObjectAddressesTable(), where: where, order: ['C_FL_MAIN DESC'])
  }

  LinkedHashMap getSubjAddress(LinkedHashMap input) {
    return getSubjAddresses(input)?.getAt(0)
  }

  List getSubjAddresses(LinkedHashMap input) {
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
      isMain          : null
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
      where.n_entrance_no = params.entrance
    }
    if (params.rem) {
      where.vc_rem = params.rem
    }
    if (params.stateId) {
      where.n_addr_state_id = params.stateId
    }
    if (params.isMain != null) {
      where.c_fl_main = Oracle.encodeBool(params.isMain)
    }
    return hid.getTableData(getSubjectAddressesTable(), where: where, order: ['C_FL_MAIN DESC'])
  }

  LinkedHashMap getObjAddress(LinkedHashMap input) {
    return getObjAddresses(input)?.getAt(0)
  }

  List getEntityAddresses(LinkedHashMap input) {
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
      return getSubjAddresses(params)
    } else {
      params.objAddressId  = params.entityAddressId
      params.objectId      = params.entityId
      return getObjAddresses(params)
    }
  }

  LinkedHashMap getEntityAddress(LinkedHashMap input) {
    return getEntityAddresses(input)?.getAt(0)
  }

  List getAddresses(LinkedHashMap input) {
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
      where.n_entrance_no = params.entrance
    }
    if (params.providerId) {
      where."DECODE(n_provider_id, NULL, ${params.providerId}, n_provider_id)" = params.providerId
    }
    if (params.rem) {
      where.vc_rem = params.rem
    }
    return hid.getTableData(getMainAddressesTable(), where: where, order: ['N_ADDRESS_ID ASC'])
  }

  LinkedHashMap getAddress(LinkedHashMap input) {
    return getAddresses(input)?.getAt(0)
  }

  LinkedHashMap getAddress(def addressId) {
    return getAddress(addressId: addressId)
  }

  LinkedHashMap putSubjAddress(LinkedHashMap input) {
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
      LinkedHashMap address = hid.execute('SI_ADDRESSES_PKG.SI_SUBJ_ADDRESSES_PUT_EX', [
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
          ch_C_FL_MAIN            : Oracle.encodeBool(params.isMain),
          num_N_ADDR_STATE_ID     : params.stateId,
          vch_VC_REM              : params.rem
      ])
      logger.info("   Address ${address.num_N_ADDRESS_ID} added to subject!")
      return address
    } catch (Exception e){
      logger.error("   Error while adding a subject address!")
      logger.error_oracle(e)
      return null
    }
  }

  LinkedHashMap putObjAddress(LinkedHashMap input) {
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
      beginDate      : DateTimeUtil.now(),
      endDate        : null
    ], input)
    try {
      logger.info("Putting address with params ${params}")
      LinkedHashMap address = hid.execute('SI_ADDRESSES_PKG.SI_OBJ_ADDRESSES_PUT_EX', [
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
          ch_C_FL_MAIN           : Oracle.encodeBool(params.isMain),
          dt_D_BEGIN             : params.beginDate ?: DateTimeUtil.now(),
          dt_D_END               : params.endDate,
          num_N_ADDR_STATE_ID    : params.stateId,
          vch_VC_REM             : params.rem
      ])
      logger.info("   Address ${address.num_N_ADDRESS_ID} added to object!")
      return address
    } catch (Exception e){
      logger.error("   Error while adding an object address!")
      logger.error_oracle(e)
      return null
    }
  }

  LinkedHashMap putEntityAddress(LinkedHashMap input) {
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
      beginDate       : DateTimeUtil.now(),
      endDate         : null
    ], input)

    Boolean isSubj = isSubject(params.entityTypeId ?: params.entityId)

    if (isSubj) {
      params.subjAddressId = params.entityAddressId
      params.subjectId     = params.entityId
      def address = putSubjAddress(params)
      if (address) {
        address.num_N_ENTITY_ADDRESS_ID = address.num_N_SUBJ_ADDRESS_ID
        address.num_N_ENTITY_ID         = address.num_N_SUBJECT_ID
      }
      return address
    } else {
      params.objAddressId  = params.entityAddressId
      params.objectId      = params.entityId
      def address = putObjAddress(params)
      if (address) {
        address.num_N_ENTITY_ADDRESS_ID = address.num_N_OBJ_ADDRESS_ID
        address.num_N_ENTITY_ID         = address.num_N_OBJECT_ID
      }
      return address
    }
  }

  LinkedHashMap createSubjAddress(LinkedHashMap input) {
    input.remove('subjAddressId')
    return putSubjAddress(input)
  }

  LinkedHashMap createObjAddress(LinkedHashMap input) {
    input.remove('objAddressId')
    return putObjAddress(input)
  }

  LinkedHashMap createEntityAddress(LinkedHashMap input) {
    input.remove('entityAddressId')
    return putEntityAddress(input)
  }

  LinkedHashMap createSubjAddress(def subjectId, LinkedHashMap input) {
    return putSubjAddress(input + [subjectId: subjectId])
  }

  LinkedHashMap createObjAddress(def objectId, LinkedHashMap input) {
    return putObjAddress(input + [objectId: objectId])
  }

  LinkedHashMap createEntityAddress(def entityId, LinkedHashMap input) {
    return putEntityAddress(input + [entityId: entityId])
  }

  LinkedHashMap updateSubjAddress(def subjAddressId, LinkedHashMap input) {
    return putSubjAddress(input + [subjAddressId: subjAddressId])
  }

  LinkedHashMap updateObjAddress(def objAddressId, LinkedHashMap input) {
    return putObjAddress(input + [objAddressId: objAddressId])
  }

  LinkedHashMap updateEntityAddress(def entityAddressId, LinkedHashMap input) {
    return putEntityAddress(input + [entityAddressId: entityAddressId])
  }

  List getAddressItemsValues(LinkedHashMap input) {
    List addressItemsValues = []
    getAddressItems().each{ type, value ->
      addressItemsValues.add([value, 'N', input[type] ?: ""])
    }
    return addressItemsValues
  }

  String calcAddress(LinkedHashMap input) {
    String address = ''

    List regionItemsValues = getRegionItemsValues(input)

    if(regionItemsValues){
      def result = regionItemsValues + getAddressItemsValues(input)
      result.eachWithIndex{ it, i ->
        String  part  = it[0]
        Boolean after = Oracle.decodeBool(it[1])
        String  name  = it[2]
        if (name != null && name != '' && name != ' ' && name != 'null'){
          String item = (!after ? (part ?  part + ' ' : '') : '') + name + (after ? (part ?  ' ' + part : '') : '')
          address += (i > 0 ? ', ' : '') + item
        }
      }
    }
    return address
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

  Boolean deleteEntityAddress(LinkedHashMap input) {
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
        def objAddress = getEntityAddresses(
          entityAddressId : params.entityAddressId,
          entityId        : params.entityId,
          addressId       : params.addressId,
          addrTypeId      : params.addrTypeId,
          bindAddrTypeId  : params.bindAddrTypeId,
          stateId         : params.stateId,
          isMain          : params.isMain
        )
        def subjAddress = getEntityAddresses(
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

  Boolean closeObjAddress(
    def objAddressId,
    def endDate = DateTimeUtil.now()
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

  Boolean closeObjAddress(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      entityAddressId : null,
      addressId       : null,
      entityId        : null,
      bindAddrTypeId  : null,
      addrTypeId      : null,
      stateId         : getDefaultAddressStateId(),
      isMain          : null,
      endDate         : DateTimeUtil.now()
    ], input)

    if (!params.entityAddressId) {
      LinkedHashMap address = getEntityAddress(
        addressId      : params.addressId,
        entityId       : params.entityId,
        addrTypeId     : params.addrTypeId,
        bindAddrTypeId : params.bindAddrTypeId,
        operationDate  : params.endDate,
        stateId        : params.stateId,
        isMain         : params.isMain
      )
      if (!address) {
        logger.error("No address found!")
        return false
      }
      params.entityAddressId = address.n_obj_address_id
    }

    return closeObjAddress(params.entityAddressId, params.endDate)
  }

  Boolean closeEntityAddress(LinkedHashMap input) {
    return closeObjAddress(input)
  }

  List getFreeIPAddresses(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      groupId         : null,
      subnetAddressId : null,
      operationDate   : DateTimeUtil.now(),
      firmId          : getFirmId()
    ], input)

    def addresses = []
    def date = Oracle.encodeDateStr(params.operationDate)

    try {
      if (groupId) {
        addresses = hid.queryDatabase("""
          SELECT
              'vc_ip', SI_ADDRESSES_PKG_S.NUMBER_TO_IP_ADDRESS(SI_ADDRESSES_PKG_S.GET_FREE_IP_ADDRESS(
                num_N_SUBNET_ADDR_ID => A.N_ADDRESS_ID,
                num_N_PROVIDER_ID    => ${params.firmId})),
              'n_subnet_id', A.N_ADDRESS_ID,
              'vc_subnet',   A.VC_CODE
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
        addresses = hid.queryFirst("""
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

  LinkedHashMap getFreeIPAddress(LinkedHashMap input) {
    def result = getFreeIPAddresses(input)
    if (result) {
      return result.getAt(0)
    } else {
      return null
    }
  }

  String getFreeIP(LinkedHashMap input) {
    return getFreeIPAddress(input)?.vc_ip
  }

  List getFreeTelephoneNumbers(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      groupId       : null,
      telCodeId     : null,
      operationDate : DateTimeUtil.now(),
      firmId        : getFirmId()
    ], input)

    def addresses = []
    def date = Oracle.encodeDateStr(params.operationDate)

    try {
      if (params.groupId) {
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
        addresses = hid.queryFirst("""
          SELECT
              'vc_phone_number', SI_ADDRESSES_PKG_S.GET_FREE_PHONE_NUMBER(
                num_N_TEL_CODE_ID => A.N_ADDRESS_ID,
                num_N_PROVIDER_ID => ${params.firmId}),
              'n_telcode_id', A.N_ADDRESS_ID,
              'vc_tel_code',  A.VC_CODE
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

  LinkedHashMap getFreeTelephoneNumber(LinkedHashMap input) {
    def result = getFreeTelephoneNumbers(input)
    if (result) {
      return result.getAt(0)
    } else {
      return null
    }
  }

  String getFreePhoneNumber(LinkedHashMap input) {
    return getFreeTelephoneNumber(input)?.vc_phone_number
  }

  List getFreeSubnetAddresses(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      groupId       : null,
      rootId        : null,
      mask          : null,
      operationDate : DateTimeUtil.now(),
      firmId        : getFirmId()
    ], input)

    def addresses = []
    def date = Oracle.encodeDateStr(params.operationDate)
    def filter = params.mask ? "SI_ADDRESSES_PKG_S.GET_BITN_BY_MASK(A.N_MASK) = '${params.mask}'" : '1=1'
    def notAssigned = """NOT EXISTS ( -- Не привязаны к оборудованию
      SELECT 1
      FROM
          SI_V_OBJ_ADDRESSES OA
      WHERE
          OA.N_ADDR_STATE_ID     = SYS_CONTEXT('CONST', 'ADDR_STATE_On')
      AND OA.N_ADDRESS_ID        = FA.N_ADDRESS_ID
      AND ${date} BETWEEN OA.D_BEGIN AND NVL(OA.D_END, ${date})
    )"""
    def notAssignedChild = params.mask != '30' ? """NOT EXISTS ( -- И нет дочерних привязок к оборудованию
      SELECT 1
      FROM
          SI_V_OBJ_ADDRESSES OA,
          RG_PAR_ADDRESSES   RP
      WHERE
          OA.N_ADDR_TYPE_ID      = SYS_CONTEXT('CONST', 'ADDR_TYPE_Subnet')
      AND OA.N_ADDR_STATE_ID     = SYS_CONTEXT('CONST', 'ADDR_STATE_On')
      AND RP.N_ADDRESS_ID        = OA.N_ADDRESS_ID
      AND ${date} BETWEEN OA.D_BEGIN AND NVL(OA.D_END, ${date})
      AND ${date} BETWEEN RP.D_BEGIN AND NVL(RP.D_END, ${date})
      AND RP.N_ADDR_BIND_TYPE_ID = SYS_CONTEXT('CONST', 'ADDR_ADDR_TYPE_Parent')
      AND RP.N_PROVIDER_ID       = ${params.firmId}
      START WITH
          RP.N_PAR_ADDR_ID       = FA.N_ADDRESS_ID
      CONNECT BY PRIOR
          RP.N_ADDRESS_ID        = RP.N_PAR_ADDR_ID
    )""" : '1=1'

    // WARN Чтобы получение подсети работало, подсети нужного размера должны быть нарезаны
    try {
      addresses = hid.queryDatabase("""
      WITH AVAILABLE_SUBNETS AS (
        SELECT RA.*
        FROM   RG_PAR_ADDRESSES   RA
        WHERE  RA.N_PROVIDER_ID = ${params.firmId}
        CONNECT BY PRIOR
              RA.N_ADDRESS_ID  = RA.N_PAR_ADDR_ID
        AND   RA.N_ADDR_BIND_TYPE_ID = SYS_CONTEXT('CONST', 'ADDR_ADDR_TYPE_Parent')
        START WITH
              RA.N_PAR_ADDR_ID = ${params.groupId ? params.groupId : params.rootId}
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
            A.N_ADDRESS_ID        = AA.N_ADDRESS_ID
        AND ${filter}
        AND A.N_ADDR_TYPE_ID      = SYS_CONTEXT('CONST', 'ADDR_TYPE_Subnet')
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
    } catch (Exception e){
      logger.error_oracle(e)
    }
    return addresses
  }

  LinkedHashMap getFreeSubnetAddress(LinkedHashMap input) {
    def result = getFreeSubnetAddresses(input)
    if (result) {
      return result.getAt(0)
    } else {
      return null
    }
  }

  String getFreeSubnet(LinkedHashMap input) {
    return getFreeSubnetAddress(input)?.vc_subnet
  }

  def getSubnetIdByIP(String ip) {
    def subnetId = null
    try {
      subnetId = hid.queryFirst("""
        SELECT SI_ADDRESSES_PKG_S.GET_SUBNET_BY_IP_ADDRESS('$ip')
        FROM   DUAL
      """)?.getAt(0)
    } catch (Exception e){
      logger.error_oracle(e)
    }
    return subnetId
  }

  String getSubnetByIP(String ip) {
    def subnetId = getSubnetIdByIP(ip)
    def subnet = null
    if (subnetId) {
      subnet = getAddress(addressId: subnetId, addrType: 'ADDR_TYPE_SUBNET')?.vc_code
    }
    return subnet
  }

  String getSubnetMaskById(def subnetId) {
    def mask = ''
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

  String getSubnetMask(String subnet) {
    def mask = ''
    def subnetId = getAddress(code: subnet, addrType: 'ADDR_TYPE_SUBNET')
    if (subnetId) {
      mask = getSubnetMaskById(subnetId)
    }
    return mask
  }

  List getParentSubnetAddresses(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      addressId     : null,
      code          : null,
      mask          : null,
      operationDate : DateTimeUtil.now(),
      firmId        : getFirmId()
    ], input)

    def addresses = []
    def date = Oracle.encodeDateStr(params.operationDate)
    def startWith = params.addressId ? "A.N_ADDRESS_ID = ${params.addressId}" : "A.VC_CODE = '${params.code}'"

    try {
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
            ${startWith}
        CONNECT BY PRIOR
            RA.N_PAR_ADDR_ID       = RA.N_ADDRESS_ID
        ORDER BY LEVEL ASC
      """, true)
    } catch (Exception e){
      logger.error_oracle(e)
    }
    return addresses
  }

  LinkedHashMap getVLANAddressBySubnet(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      addressId     : null,
      code          : null,
      operationDate : DateTimeUtil.now(),
      firmId        : getFirmId()
    ], input)

    def address = null
    def date = Oracle.encodeDateStr(params.operationDate)
    def startWith = params.addressId ? "A.N_ADDRESS_ID = ${params.addressId}" : "A.VC_CODE = '${params.code}'"

    try {
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
              ${startWith}
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
    } catch (Exception e){
      logger.error_oracle(e)
    }
    return address
  }

  String getVLANBySubnet(LinkedHashMap input) {
    return getVLANAddressBySubnet(input)?.vc_vlan
  }
}