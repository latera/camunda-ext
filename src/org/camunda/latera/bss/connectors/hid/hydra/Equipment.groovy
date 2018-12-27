package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.utils.Oracle
trait Equipment {
  LinkedHashMap getEquipment(equipmentId) {
    LinkedHashMap where = [
      n_object_id: equipmentId
    ]
    return this.hid.getTableFirst('SI_V_OBJECTS', where: where)
  }

  def getEquipmentValueTypeIdByCode(String code) {
    return this.getGoodValueTypeIdByCode(code)
  }

  LinkedHashMap putEquipment(LinkedHashMap input) {
    LinkedHashMap params = this.mergeParams([
      id            :  null,
      typeId        :  null,
      ownerId       :  null,
      code          :  null,
      name          :  null,
      addressId     :  null,
      ip            :  null,
      mac           :  null,
      bindMainId    :  null,
      bindRoleId    :  null
    ], input)
    try {
      this.logger.log("Putting new eqipment ${params.code} with type ${params.typeId} and owner ${params.ownerId}")
      LinkedHashMap equipment = this.hid.execute('SI_USERS_PKG.CREATE_NET_DEVICE',[
        num_N_OBJECT_ID        : id,
        num_N_GOOD_ID          : typeId,
        num_N_USER_ID          : ownerId,
        vch_VC_CODE            : code,
        vch_VC_NAME            : name,
        num_N_ADDRESS_ID       : addressId,
        vch_VC_IP              : ip,
        vch_VC_MAC             : mac,
        num_N_BIND_MAIN_OBJ_ID : bindMainId,
        num_N_OBJ_ROLE_ID      : bindRoleId
      ])
      this.logger.log("   Equipment ${equipmentId.num_N_OBJECT_ID} was put successfully!")
      return equipment
    } catch (Exception e){
      this.logger.log("Error while putting new equipment!")
      this.logger.log(e)
      return null
    }
  }

  void deleteEquipment(Long equipmentId) {
    try {
      this.logger.log("Deleting eqipment ${equipmentId}")
      this.hid.execute('SI_DEVICES_PKG.SI_DEVICES_DEL', [
        num_N_OBJECT_ID: equipmentId
      ])
      this.logger.log("   Equipment deleted successfully!")
    } catch (Exception e){
      this.logger.log("Error while deleting equipment!")
      this.logger.log(e)
    }
    return null
  }

  void putEquipmentAddParam(LinkedHashMap input) {
    LinkedHashMap params = this.mergeParams([
      equipmentId :  null,
      paramId     :  null,
      date        :  null,
      string      :  null,
      bool        :  null,
      refId       :  null
    ], input)
    try {
      this.logger.log("Putting additional param ${param} value ${paramValue} to equipment ${equipmentId}")
      if (params.containsKey(param)) {
        params.paramId = this.getEquipmentValueTypeIdByCode(param)
      }

      this.hid.execute('SI_OBJECTS_PKG.PUT_OBJ_VALUE', [
        num_N_OBJECT_ID          : params.equipmentId,
        num_N_OBJ_VALUE_TYPE_ID  : params.paramId,
        dt_D_VALUE               : params.date,
        vch_VC_VALUE             : params.string,
        num_N_VALUE              : params.number,
        ch_C_FL_VALUE            : Oracle.encodeBool(params.bool),
        num_N_REF_ID             : params.refId
      ])
      this.logger.log("   Additional param value was put successfully!")
    } catch (Exception e){
      this.logger.log("Error while putting additional param!")
      this.logger.log(e)
    }
  }
}