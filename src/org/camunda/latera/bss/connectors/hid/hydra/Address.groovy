package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.utils.Oracle

trait Address {
  private static String MAIN_ADDRESSES_TABLE    = 'SI_V_ADDRESSES'
  private static String SUBJECT_ADDRESSES_TABLE = 'SI_V_SUBJ_ADDRESSES'
  private static String OBJECT_ADDRESSES_TABLE  = 'SI_V_OBJ_ADDRESSES'

  private static String DEFAULT_ADDRESS_TYPE      = 'ADDR_TYPE_FactPlace'
  private static String DEFAULT_ADDRESS_BIND_TYPE = 'BIND_ADDR_TYPE_Serv'
  private static String DEFAULT_ADDRESS_STATE     = 'ADDR_STATE_On'
  private static LinkedHashMap ADDRESS_ITEMS = [
    home      : 'д.',
    corpus    : 'корп.',
    construct : 'стр.',
    entrance  : 'подъезд',
    floor     : 'этаж',
    flat      : 'кв.'
  ]

  List getEntityAddresses(
    def entityTypeId,
    def entityId,
    def addrTypeId     = getRefIdByCode(DEFAULT_ADDRESS_TYPE),
    def bindAddrTypeId = getRefIdByCode(DEFAULT_ADDRESS_BIND_TYPE),
    def addrStateId    = getRefIdByCode(DEFAULT_ADDRESS_STATE),
    Boolean isMain     = null
  ) {
    Boolean isSubj = isSubject(entityTypeId)
    String entityPrefix = isSubj ? 'subj' : 'obj'
    String tableName    = isSubj ? SUBJECT_ADDRESSES_TABLE : OBJECT_ADDRESSES_TABLE

    LinkedHashMap where = [
      "n_${entityPrefix}ect_id": entityId
    ]

    if (bindAddrTypeId) {
      where."n_${entityPrefix}_addr_type_id" = bindAddrTypeId
    }

    if (addrTypeId) {
      where.n_addr_type_id = addrTypeId
    }

    if (addrStateId) {
      where.n_addr_state_id = addrStateId
    }

    if (isMain != null) {
      where.c_fl_main = Oracle.encodeNull(isMain)
    }

    return hid.getTableData(tableName, where: where, order: ['C_FL_MAIN DESC'])
  }

  List getEntityAddresses (
    LinkedHashMap input
  ) {
    LinkedHashMap params = mergeParams([
      entityTypeId   : null,
      entityId       : null,
      addrTypeId     : getRefIdByCode(DEFAULT_ADDRESS_TYPE),
      bindAddrTypeId : getRefIdByCode(DEFAULT_ADDRESS_BIND_TYPE),
      addrStateId    : getRefIdByCode(DEFAULT_ADDRESS_STATE),
      isMain         : null
    ], input)
    return getEntityAddresses(params.entityTypeId,
                                   params.entityId,
                                   params.addrTypeId,
                                   params.bindAddrTypeId,
                                   params.addrStateId,
                                   params.isMain)
  }

  LinkedHashMap getEntityAddress (
    LinkedHashMap input
  ) {
    return getEntityAddresses(input)?.getAt(0)
  }

  LinkedHashMap getAddress(
    def addressId
  ) {
    LinkedHashMap where = [
      n_address_id: addressId
    ]
    return hid.getTableFirst(MAIN_ADDRESSES_TABLE, where: where)
  }

  LinkedHashMap putSubjAddress(
    LinkedHashMap input
  ) {
    LinkedHashMap params = mergeParams([
      subjAddressId  :  null,
      addressId      :  null,
      subjectId      :  null,
      bindAddrTypeId :  null,
      addrTypeId     :  null,
      code           :  null,
      regionId       :  null,
      rawAddress     :  null,
      flat           :  null,
      entrance       :  null,
      rem            :  null,
      stateId        :  getRefIdByCode(DEFAULT_ADDRESS_STATE),
      isMain         :  false
    ], input)
    try {
      logger.info("Putting address with code ${params.code} to subject with id ${params.subjectId}")
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
          ch_C_FL_MAIN            : params.isMain,
          num_N_ADDR_STATE_ID     : params.stateId,
          vch_VC_REM              : params.rem,
          b_UpdateRegister        : 0
      ])
      logger.info("   Address ${addressId.num_N_ADDRESS_ID} added to subject!")
      return address
    } catch (Exception e){
      logger.error("Error while adding a subject address!")
      logger.error(e)
      return null
    }
  }

  LinkedHashMap putObjAddress(
    LinkedHashMap input
  ) {
    LinkedHashMap params = mergeParams([
      objAddressId   :  null,
      addressId      :  null,
      objectId       :  null,
      bindAddrTypeId :  null,
      addrTypeId     :  null,
      code           :  null,
      regionId       :  null,
      rawAddress     :  null,
      flat           :  null,
      entrance       :  null,
      rem            :  null,
      stateId        :  getRefIdByCode(DEFAULT_ADDRESS_STATE),
      isMain         :  false
    ], input)
    try {
      logger.info("Putting address with code ${params.code} to object with id ${params.objectId}")
      LinkedHashMap address = hid.execute('SI_ADDRESSES_PKG.SI_OBJ_ADDRESSES_PUT_EX', [
          num_N_OBJ_ADDRESS_ID    : params.objAddressId,
          num_N_ADDRESS_ID        : params.addressId,
          num_N_OBJ_ADDR_TYPE_ID  : params.bindAddrTypeId,
          num_N_ADDR_TYPE_ID      : params.addrTypeId,
          vch_VC_CODE             : params.code,
          vch_VC_ADDRESS          : params.rawAddress,
          num_N_REGION_ID         : params.regionId,
          vch_VC_FLAT             : params.flat,
          num_N_ENTRANCE_NO       : params.entrance,
          num_N_FLOOR_NO          : params.floor,
          ch_C_FL_MAIN            : params.isMain,
          num_N_ADDR_STATE_ID     : params.stateId,
          vch_VC_REM              : params.rem,
          b_UpdateRegister        : 0
      ])
      logger.info("   Address ${address.num_N_ADDRESS_ID} added to object!")
      return address
    } catch (Exception e){
      logger.error("Error while adding an object address!")
      logger.error(e)
      return null
    }
  }

  String calcAddress(
    LinkedHashMap input
  ) {
    String address = ''

    String regionQuery = ""
    REGION_TYPES.eachWithIndex{ type, i -> 
      regionQuery += """
      SELECT VC_VALUE, NVL(VC_VALUE_2,'N'), '${input[REGION_NAMES[i]] ?: ''}'
      FROM   ${REFS_TABLE}
      WHERE  VC_CODE = '${input[type] ?: ""}'""" + (type == REGION_TYPES.last() ? '' : """
      UNION ALL""")
    }

    List addressItemsValues = []
    ADDRESS_ITEMS.each{ type, value -> 
      addressItemsValues.add([value, 'N', input[type] ?: ""])
    }
    List addressResult = hid.queryDatabase(regionQuery, false)

    if(addressResult){
      def result = addressResult + addressItemsValues
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
}