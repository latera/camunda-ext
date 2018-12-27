package org.camunda.latera.bss.connectors.hid.hydra

trait Address {
  //Address staff
  static String DEFAULT_ADDRESS  = 'Российская Федерация'
  static LinkedHashMap ADDRESS_ITEMS = [
    home      : 'д.',
    corpus    : 'корп.',
    construct : 'стр.',
    entrance  : 'подъезд',
    floor     : 'этаж',
    flat      : 'кв.'
  ]

  List getEntityAddresses (entityTypeId, entityId, addrTypeId = this.getRefIdByCode('ADDR_TYPE_FactPlace'), bindAddrTypeId = this.getRefIdByCode('BIND_ADDR_TYPE_Serv'), addrStateId = this.getRefIdByCode('ADDR_STATE_On'), isMain = null) {
    Boolean isSubj = this.isSubject(entityTypeId)
    String addressQuery = """
    SELECT
        'n_region_id',       A.N_REGION_ID,
        'n_address_id',      A.N_ADDRESS_ID,
    """
    if (isSubj) {
      addressQuery += 
    """
        'n_subj_address_id', A.N_SUBJ_ADDRESS_ID,
    """
    } else {
    """
        'n_obj_address_id',  A.N_OBJ_ADDRESS_ID,
    """
    }
    addressQuery += """
        'n_entrance_no',     A.N_ENTRANCE_NO,
        'n_floor_no',        A.N_FLOOR_NO,
        'vc_flat',           A.VC_FLAT,
        'vc_visual_code',    A.VC_VISUAL_CODE,
        'vc_code',           A.VC_CODE,
        'c_fl_main',         A.C_FL_MAIN
    """
    
    if (isSubj) {
      addressQuery += """
      FROM
            SI_V_SUBJ_ADDRESSES A
      WHERE
            A.n_subject_Id = ${entityId}
      """
      if (bindAddrTypeId) {
        addressQuery += """
        AND   A.n_subj_addr_type_id = ${bindAddrTypeId}
        """
      }
    } else {
      addressQuery += """
      FROM
            SI_V_OBJ_ADDRESSES A
      WHERE
            A.n_object_Id = ${entityId}
      """
      if (bindAddrTypeId) {
        addressQuery += """
        AND   A.n_obj_addr_type_id = ${bindAddrTypeId}
        """
      }
    }

    if (addrTypeId) {
      addressQuery += """
      AND   A.n_addr_type_id = ${addrTypeId}
      """
    }

    if (addrStateId) {
      addressQuery += """
      AND   A.n_addr_state_id = ${addrStateId}
      """
    }

    if (isMain != null) {
      addressQuery += """
      AND   A.c_fl_main = ${this.encodeNull(isMain)}
      """
    }

    addressQuery += """
    ORDER BY c_fl_main DESC
    """
    
    return this.hid.queryDatabase(addressQuery)
  }

  List getEntityAddresses (
    LinkedHashMap input
  ) {
    LinkedHashMap args = this.mergeParams([
      entityTypeId   : null,
      entityId       : null,
      addrTypeId     : this.getRefIdByCode('ADDR_TYPE_FactPlace'),
      bindAddrTypeId : this.getRefIdByCode('BIND_ADDR_TYPE_Serv'),
      addrStateId    : this.getRefIdByCode('ADDR_STATE_On'),
      isMain         : false
    ], input)
    return this.getEntityAddresses(*args)
  }

  LinkedHashMap getEntityAddress (
    LinkedHashMap input
  ) {
    return this.getEntityAddresses(input)?.getAt(0)
  }

  LinkedHashMap getAddress (
    def addressId
  ) {
    LinkedHashMap where = [
      n_address_id: addressId
    ]
    return this.hid.getTableFirst('SI_V_ADDRESSES', where: where)
  }

  LinkedHashMap putSubjAddress (
    LinkedHashMap input
  ) {
    LinkedHashMap params = this.mergeParams([
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
      stateId        :  this.getRefIdByCode('ADDR_STATE_On'),
      isMain         :  false
    ], input)
    try {
      this.logger.log("Putting address with code ${params.code} to subject with id ${params.subjectId}")
      LinkedHashMap address = this.hid.execute('SI_ADDRESSES_PKG.SI_SUBJ_ADDRESSES_PUT_EX', [
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
      this.logger.log("   Address ${addressId.num_N_ADDRESS_ID} added to subject!")
      return address
    } catch (Exception e){
      this.logger.log("Error while adding a subject address!")
      this.logger.log(e)
      return null
    }
  }

  LinkedHashMap putObjAddress (
    LinkedHashMap input
  ) {
    LinkedHashMap params = this.mergeParams([
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
      stateId        :  this.getRefIdByCode('ADDR_STATE_On'),
      isMain         :  false
    ], input)
    try {
      this.logger.log("Putting address with code ${params.code} to object with id ${params.objectId}")
      LinkedHashMap address = this.hid.execute('SI_ADDRESSES_PKG.SI_OBJ_ADDRESSES_PUT_EX', [
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
      this.logger.log("   Address ${address.num_N_ADDRESS_ID} added to object!")
      return address
    } catch (Exception e){
      this.logger.log("Error while adding an object address!")
      this.logger.log(e)
      return null
    }
  }

  String calcAddress (
    LinkedHashMap input
  ) {
    String address = this.DEFAULT_ADDRESS

    String regionQuery = this.REGION_TYPES.eachWithIndex{ type, i -> """
      SELECT R.VC_VALUE, NVL(R.VC_VALUE_2,'N'), '${input[this.REGION_NAMES[i]] ?: ''}'
      FROM   SI_V_REF     R
      WHERE  R.VC_CODE = '${type}'
    """
    }.join("""
      UNION ALL
    """)

    List addressItemsValues = this.ADDRESS_ITEMS.each{ type, value -> 
      [0, type, 'N', input[value] ?: ""]
    }

    List addressResult = this.hid.queryDatabase(regionQuery, false)

    if(addressResult){
      (addressResult + addressItemsValues).each{ it ->
        String  name  = it[2]
        Boolean after = this.decodeBool(it[1])
        String  part  = it[0]
        if (name != null && name != '' && name != ' ' && name != 'null'){
          String item = (!after ? part + ' ' : '') + name + (after ? ' ' + part : '')
          address += ', ' + item
        }
      }
    }
    return address
  }
}