package org.camunda.latera.bss.connectors.odoo.types

trait Customer {
  private static String CUSTOMER_ENTITY_TYPE = 'res.partner'

  def getCustomerEntityType() {
    return CUSTOMER_ENTITY_TYPE
  }

  LinkedHashMap getCustomerDefaultParams() {
    return [
      name            : null,
      email           : null,
      isCompany       : null,
      companyName     : null,
      organizationId  : null,
      userId          : null,
      teamId          : null,
      phoneNumber     : null,
      countryId       : null,
      stateId         : null,
      city            : null,
      street          : null,
      street2         : null,
      zip             : null,
      hydraCustomerId : null,
      comment         : null
    ]
  }

  LinkedHashMap getCustomerParamsMap(LinkedHashMap params) {
    return [
      name          : params.name,
      email         : params.email,
      is_company    : params.isCompany,
      company_name  : params.companyName,
      company_id    : params.organizationId,
      user_id       : params.userId,
      team_id       : params.teamId,
      phone         : params.phoneNumber,
      country_id    : params.countryId,
      state_id      : params.stateId,
      city          : params.city,
      street        : params.street,
      street2       : params.street2,
      zip           : params.zip,
      hydra_account : params.hydraCustomerId,
      comment       : params.comment,
      customer      : true
    ]
  }

  LinkedHashMap getCustomerParams(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    def params = getCustomerDefaultParams() + input
    def where  = getCustomerParamsMap(params)
    return convertParams(nvlParams(where) + convertKeys(additionalParams))
  }

  LinkedHashMap getCustomer(def id) {
    return getEntity(getCustomerEntityType(), id)
  }

  List getCustomersBy(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    LinkedHashMap params = getCustomerParams(input, additionalParams)
    return getEntitiesBy(getCustomerEntityType(), params)
  }

  LinkedHashMap getCustomerBy(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    return getCustomersBy(input, additionalParams)?.getAt(0)
  }

  LinkedHashMap createCustomer(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    LinkedHashMap params = getCustomerParams(input, additionalParams)
    return createEntity(getCustomerEntityType(), params)
  }

  LinkedHashMap updateCustomer(def id, LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    LinkedHashMap params = getCustomerParams(input, additionalParams)
    return updateEntity(getCustomerEntityType(), id, params)
  }

  LinkedHashMap updateCustomer(LinkedHashMap input, def id, LinkedHashMap additionalParams = [:]) {
    return updateCustomer(id, input, additionalParams)
  }

  Boolean deleteCustomer(def id) {
    return deleteEntity(getCustomerEntityType(), id)
  }
}