package org.camunda.latera.bss.connectors.hoper.hydra

trait Address {
  private static LinkedHashMap ADDRESS_ENTITY_TYPE = [
    one    : 'address',
    plural : 'addresses'
  ]
  private static Integer DEFAULT_ADDRESS_TYPE_ID      = 1006 // 'ADDR_TYPE_FactPlace'
  private static Integer DEFAULT_ADDRESS_BIND_TYPE_ID = 6016 // 'BIND_ADDR_TYPE_Serv'
  private static Integer DEFAULT_ADDRESS_STATE_ID     = 1029 // 'ADDR_STATE_On'

  Map getAddressEntityType(Map parentType, def id = null) {
    return ADDRESS_ENTITY_TYPE + withParent(parentType) + withId(id)
  }

  Map getPersonAddressEntityType(def personId, def id = null) {
    return getAddressEntityType(getPersonEntityType(personId), id)
  }

  Map getCompanyAddressEntityType(def companyId, def id = null) {
    return getAddressEntityType(getCompanyEntityType(companyId), id)
  }

  Integer getDefaultAddressTypeId() {
    return DEFAULT_ADDRESS_TYPE_ID
  }

  Integer getDefaultAddressBindTypeId() {
    return DEFAULT_ADDRESS_BIND_TYPE_ID
  }

  Integer getDefaultAddressStateId() {
    return DEFAULT_ADDRESS_STATE_ID
  }

  private Map getAddressDefaultParams() {
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

  private Map getAddressParamsMap(Map params) {
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

  private Map getAddressParams(Map input) {
    LinkedHashMap params = getAddressDefaultParams() + input
    LinkedHashMap data   = getAddressParamsMap(params)
    return prepareParams(data)
  }

  Boolean getAddresses(def entityType = 'person', def entityId) {
    if (entityType == getPersonEntityType().one) {
      return getPersonAddresses(entityId)
    } else if (entityType == getCompanyEntityType().one) {
      return getCompanyAddresses(entityId)
    }
  }

  List getPersonAddresses(Map input = [:], def personId) {
    LinkedHashMap params = getPaginationDefaultParams() + input
    return getEntities(getPersonAddressEntityType(personId), params)
  }

  List getCompanyAddresses(Map input = [:], def companyId) {
    LinkedHashMap params = getPaginationDefaultParams() + input
    return getEntities(getCompanyAddressEntityType(companyId), params)
  }

  Boolean getAddress(String entityType = 'person', def entityId, def entityAddressId) {
    if (entityType == getPersonEntityType().one) {
      return getPersonAddress(entityId, entityAddressId)
    } else if (entityType == getCompanyEntityType().one) {
      return getCompanyAddress(entityId, entityAddressId)
    }
  }

  Map getPersonAddress(def personId, def subjAddressId) {
    return getEntity(getPersonAddressEntityType(personId), subjAddressId)
  }

  Map getCompanyAddress(def companyId, def subjAddressId) {
    return getEntity(getCompanyAddressEntityType(companyId), subjAddressId)
  }

  Boolean createAddress(Map input = [:], String entityType = 'person', def entityId) {
    if (entityType == getPersonEntityType().one) {
      return createPersonAddress(entityId, input)
    } else if (entityType == getCompanyEntityType().one) {
      return createCompanyAddress(entityId, input)
    }
  }

  Map createPersonAddress(Map input = [:], def personId, ) {
    LinkedHashMap params = getAddressParams(input)
    return createEntity(getPersonAddressEntityType(personId), params)
  }

  Map createCompanyAddress(Map input = [:], def companyId) {
    LinkedHashMap params = getAddressParams(input)
    return createEntity(getCompanyAddressEntityType(companyId), params)
  }

  Boolean updateAddress(Map input = [:], String entityType = 'person', def entityId, def entityAddressId) {
    if (entityType == getPersonEntityType().one) {
      return updatePersonAddress(entityId, entityAddressId, input)
    } else if (entityType == getCompanyEntityType().one) {
      return updateCompanyAddress(entityId, entityAddressId, input)
    }
  }

  Map updatePersonAddress(Map input = [:], def personId, def subjAddressId) {
    LinkedHashMap params = getAddressParams(input)
    return updateEntity(getPersonAddressEntityType(personId), subjAddressId, params)
  }

  Map updateCompanyAddress(Map input = [:], def companyId, def subjAddressId) {
    LinkedHashMap params = getAddressParams(input)
    return updateEntity(getCompanyAddressEntityType(companyId), subjAddressId, params)
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