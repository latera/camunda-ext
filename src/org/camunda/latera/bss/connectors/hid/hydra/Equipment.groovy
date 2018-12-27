package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.utils.Oracle
trait Equipment {
  static String EQUIPMENT_TABLE = 'SI_V_OBJECTS'

  LinkedHashMap getEquipment(equipmentId) {
    LinkedHashMap where = [
      n_object_id: equipmentId
    ]
    return hid.getTableFirst(EQUIPMENT_TABLE, where: where)
  }

  def getEquipmentValueTypeIdByCode(String code) {
    return getGoodValueTypeIdByCode(code)
  }

  LinkedHashMap putEquipment(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
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
      logger.info("Putting new eqipment ${params.code} with type ${params.typeId} and owner ${params.ownerId}")
      LinkedHashMap equipment = hid.execute('SI_USERS_PKG.CREATE_NET_DEVICE', [
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
      logger.info("   Equipment ${equipmentId.num_N_OBJECT_ID} was put successfully!")
      return equipment
    } catch (Exception e){
      logger.error("Error while putting new equipment!")
      logger.error(e)
      return null
    }
  }

  void deleteEquipment(Long equipmentId) {
    try {
      logger.info("Deleting eqipment ${equipmentId}")
      hid.execute('SI_DEVICES_PKG.SI_DEVICES_DEL', [
        num_N_OBJECT_ID: equipmentId
      ])
      logger.info("   Equipment deleted successfully!")
    } catch (Exception e){
      logger.error("Error while deleting equipment!")
      logger.error(e)
    }
    return null
  }

  void putEquipmentAddParam(LinkedHashMap input) {
    LinkedHashMap defaultParams = [
      equipmentId :  null,
      paramId     :  null,
      date        :  null,
      string      :  null,
      bool        :  null,
      refId       :  null
    ]

    if (input.containsKey('param')) {
      input.paramId = getEquipmentValueTypeIdByCode(input.param)
      input.remove('param')
    }
    LinkedHashMap params = mergeParams(defaultParams, input)
    try {
      def paramValue = params.date ?: params.string ?: params.number ?: params.bool ?: params.refId
      logger.info("Putting additional param ${params.paramId} value ${paramValue} to equipment ${params.equipmentId}")

      hid.execute('SI_OBJECTS_PKG.PUT_OBJ_VALUE', [
        num_N_OBJECT_ID          : params.equipmentId,
        num_N_OBJ_VALUE_TYPE_ID  : params.paramId,
        dt_D_VALUE               : params.date,
        vch_VC_VALUE             : params.string,
        num_N_VALUE              : params.number,
        ch_C_FL_VALUE            : Oracle.encodeBool(params.bool),
        num_N_REF_ID             : params.refId
      ])
      logger.info("   Additional param value was put successfully!")
    } catch (Exception e){
      logger.error("Error while putting additional param!")
      logger.error(e)
    }
  }
}