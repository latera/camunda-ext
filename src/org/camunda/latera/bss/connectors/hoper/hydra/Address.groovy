package org.camunda.latera.bss.connectors.hoper.hydra

trait Address {
  private static LinkedHashMap ADDRESS_ENTITY_TYPE = [
    one    : 'address',
    plural : 'addresses'
  ]
  private static Integer DEFAULT_ADDRESS_TYPE_ID      = 1006 // 'ADDR_TYPE_FactPlace'
  private static Integer DEFAULT_ADDRESS_BIND_TYPE_ID = 6016 // 'BIND_ADDR_TYPE_Serv'
  private static Integer DEFAULT_ADDRESS_STATE_ID     = 1029 // 'ADDR_STATE_On'

  def getAddressEntityType(def parentType, def parentId) {
    def parent = "${parentType.parent}/${parentType.plural}/${parentId}"
    return ADDRESS_ENTITY_TYPE + [parent: parent]
  }

  def getPersonAddressEntityType(def subjectId) {
    return getAddressEntityType(getPersonEntityType(), subjectId)
  }

  def getCompanyAddressEntityType(def subjectId) {
    return getAddressEntityType(getCompanyEntityType(), subjectId)
  }

  def getDefaultAddressTypeId() {
    return DEFAULT_ADDRESS_TYPE_ID
  }

  def getDefaultAddressBindTypeId() {
    return DEFAULT_ADDRESS_BIND_TYPE_ID
  }

  def getDefaultAddressStateId() {
    return DEFAULT_ADDRESS_STATE_ID
  }

  LinkedHashMap getAddressDefaultParams() {
    return [
      addrTypeId      : getDefaultAddressTypeId(),
      parAddressId    : null,
      code            : null,
      regionId        : null,
      rawAddress      : null,
      flat            : null,
      floor           : null,
      entrance        : null,
      intercomCode    : null,
      rem             : null,
      bindAddrTypeId  : getDefaultAddressBindTypeId(),
      stateId         : getDefaultAddressStateId(),
      isMain          : null,
      beginDate       : null,
      endDate         : null,
      firmId          : getFirmId()
    ]
  }

  LinkedHashMap getAddressParamsMap(LinkedHashMap params) {
    return [
      n_addr_type_id      : params.addrTypeId,
      n_par_addr_id       : params.parAddressId,
      vc_code             : params.code,
      n_region_id         : params.regionId,
      vc_address          : params.rawAddress,
      vc_flat             : params.flat,
      n_floor_no          : params.floor,
      vc_entrance_no      : params.entrance,
      vc_intercom_code    : params.intercomCode,
      vc_rem              : params.rem,
      n_subj_addr_type_id : params.bindAddrTypeId,
      n_addr_state_id     : params.stateId,
      c_fl_main           : params.isMain,
      d_begin             : params.beginDate,
      d_end               : params.endDate,
      n_firm_id           : params.firmId
    ]
  }

  LinkedHashMap getAddressParams(LinkedHashMap input) {
    def params = getAddressDefaultParams() + input
    def data   = getAddressParamsMap(params)
    return nvlParams(data)
  }

  Boolean getAddresses(def entityType = 'person', def entityId) {
    if (entityType == getPersonEntityType().one) {
      return getPersonAddresses(entityId)
    } else if (entityType == getCompanyEntityType().one) {
      return getCompanyAddresses(entityId)
    }
  }

  List getPersonAddresses(def personId, LinkedHashMap input = [:]) {
    def params = getPaginationDefaultParams() + input
    return getEntities(getPersonAddressEntityType(personId), params)
  }

  List getCompanyAddresses(def companyId, LinkedHashMap input = [:]) {
    def params = getPaginationDefaultParams() + input
    return getEntities(getCompanyAddressEntityType(companyId), params)
  }

  Boolean getAddress(def entityType = 'person', def entityId, def entityAddressId) {
    if (entityType == getPersonEntityType().one) {
      return getPersonAddress(entityId, entityAddressId)
    } else if (entityType == getCompanyEntityType().one) {
      return getCompanyAddress(entityId, entityAddressId)
    }
  }

  LinkedHashMap getPersonAddress(def personId, def subjAddressId) {
    return getEntity(getPersonAddressEntityType(personId), subjAddressId)
  }

  LinkedHashMap getCompanyAddress(def companyId, def subjAddressId) {
    return getEntity(getCompanyAddressEntityType(companyId), subjAddressId)
  }

  Boolean createAddress(def entityType = 'person', def entityId, LinkedHashMap input) {
    if (entityType == getPersonEntityType().one) {
      return createPersonAddress(entityId, input)
    } else if (entityType == getCompanyEntityType().one) {
      return createCompanyAddress(entityId, input)
    }
  }

  LinkedHashMap createPersonAddress(def personId, LinkedHashMap input) {
    LinkedHashMap params = getAddressParams(input)
    return createEntity(getPersonAddressEntityType(personId), params)
  }

  LinkedHashMap createCompanyAddress(def companyId, LinkedHashMap input) {
    LinkedHashMap params = getAddressParams(input)
    return createEntity(getCompanyAddressEntityType(companyId), params)
  }

  Boolean updateAddress(def entityType = 'person', def entityId, def entityAddressId, LinkedHashMap input) {
    if (entityType == getPersonEntityType().one) {
      return updatePersonAddress(entityId, entityAddressId, input)
    } else if (entityType == getCompanyEntityType().one) {
      return updateCompanyAddress(entityId, entityAddressId, input)
    }
  }

  LinkedHashMap updatePersonAddress(def personId, def subjAddressId, LinkedHashMap input) {
    LinkedHashMap params = getAddressParams(input)
    return updateEntity(getPersonAddressEntityType(personId), subjAddressId, params)
  }

  LinkedHashMap updateCompanyAddress(def companyId, def subjAddressId, LinkedHashMap input) {
    LinkedHashMap params = getAddressParams(input)
    return updateEntity(getCompanyAddressEntityType(companyId), subjAddressId, params)
  }

  Boolean putAddress(def entityType = 'person', def entityId, LinkedHashMap input) {
    if (entityType == getPersonEntityType().one) {
      return putPersonAddress(entityId, input)
    } else if (entityType == getCompanyEntityType().one) {
      return putCompanyAddress(entityId, input)
    }
  }

  LinkedHashMap putPersonAddress(def personId, LinkedHashMap input) {
    def subjAddressId = input.subjAddressId
    input.remove('subjAddressId')

    if (subjAddressId) {
      return updatePersonAddress(personId, subjAddressId, input)
    } else {
      return createPersonAddress(personId, input)
    }
  }

  LinkedHashMap putCompanyAddress(def companyId, LinkedHashMap input) {
    def subjAddressId = input.subjAddressId
    input.remove('subjAddressId')

    if (subjAddressId) {
      return updateCompanyAddress(companyId, subjAddressId, input)
    } else {
      return createCompanyAddress(companyId, input)
    }
  }

  Boolean deleteAddress(def entityType = 'person', def entityId, def entityAddressId) {
    if (entityType == getPersonEntityType().one) {
      return deletePersonAddress(entityId, entityAddressId)
    } else if (entityType == getCompanyEntityType().one) {
      return deleteCompanyAddress(entityId, entityAddressId)
    }
  }

  Boolean deletePersonAddress(def personId, def subjAddressId) {
    return deleteEntity(getPersonAddressEntityType(personId), subjAddressId)
  }

  Boolean deleteCompanyAddress(def companyId, def subjAddressId) {
    return deleteEntity(getCompanyAddressEntityType(companyId), subjAddressId)
  }
}