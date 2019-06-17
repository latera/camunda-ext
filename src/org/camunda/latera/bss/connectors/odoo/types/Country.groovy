package org.camunda.latera.bss.connectors.odoo.types

trait Country {
  private static String COUNTRY_ENTITY_TYPE       = 'res.country'
  private static String COUNTRY_STATE_ENTITY_TYPE = 'res.country_state'

  def getCountryEntityType() {
    return COUNTRY_ENTITY_TYPE
  }

  LinkedHashMap getCountryDefaultParams() {
    return [
      code          : null,
      name          : null,
      currencyId    : null,
      phoneCode     : null,
      addressFormat : null,
      vatLabel      : null
    ]
  }

  LinkedHashMap getCountryParamsMap(LinkedHashMap params) {
    return [
      code           : params.code,
      name           : params.name,
      currency_id    : params.currencyId,
      phone_code     : params.phoneCode,
      address_format : params.addressFormat,
      vat_label      : params.vatLabel
    ]
  }

  LinkedHashMap getCountryParams(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    def params = getCountryDefaultParams() + input
    return prepareParams(this.&getCountryParamsMap, params, additionalParams)
  }

  LinkedHashMap getCountry(def id) {
    return getEntity(getCountryEntityType(), id)
  }

  List getCountriesBy(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    LinkedHashMap params = getCountryParams(input, additionalParams)
    return getEntitiesBy(getCountryEntityType(), params)
  }

  LinkedHashMap getCountryBy(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    return getCountriesBy(input, additionalParams)?.getAt(0)
  }

  LinkedHashMap createCountry(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    LinkedHashMap params = getCountryParams(input, additionalParams)
    return createEntity(getCountryEntityType(), params)
  }

  LinkedHashMap updateCountry(def id, LinkedHashMap data, LinkedHashMap additionalParams = [:]) {
    LinkedHashMap params = getCountryParams(input, additionalParams)
    return updateEntity(getCountryEntityType(), id, params)
  }

  LinkedHashMap updateCountry(LinkedHashMap input, def id, LinkedHashMap additionalParams = [:]) {
    return updateCountry(id, input, additionalParams)
  }

  Boolean deleteCountry(def id) {
    return deleteEntity(getCountryEntityType(), id)
  }

  def getCountryStateEntityType() {
    return COUNTRY_STATE_ENTITY_TYPE
  }

  LinkedHashMap getCountryStateDefaultParams() {
    return [
      code      : null,
      name      : null,
      countryId : null
    ]
  }

  LinkedHashMap getCountryStateParamsMap(LinkedHashMap params) {
    return [
      code       : input.code,
      name       : input.name,
      country_id : input.countryId
    ]
  }

  LinkedHashMap getCountryStateParams(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    def params = getCountryStateDefaultParams() + input
    return prepareParams(this.&getCountryStateParamsMap, params, additionalParams)
  }

  LinkedHashMap getCountryState(def id) {
    return getEntity(getCountryStateEntityType(), id)
  }

  List getCountryStatesBy(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    LinkedHashMap params = getCountryStateParams(input, additionalParams)
    return getEntitiesBy(getCountryStateEntityType(), params)
  }

  LinkedHashMap getCountryStateBy(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    return getCountryStatesBy(input, additionalParams)?.getAt(0)
  }

  LinkedHashMap createCountryState(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    LinkedHashMap params = getCountryStateParams(input, additionalParams)
    return createEntity(getCountryStateEntityType(), params)
  }

  LinkedHashMap updateCountryState(def id, LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    LinkedHashMap params = getCountryStateParams(input, additionalParams)
    return updateEntity(getCountryStateEntityType(), id, params)
  }

  LinkedHashMap updateCountryState(LinkedHashMap input, def id, LinkedHashMap additionalParams = [:]) {
    return updateCountryState(id, input, additionalParams)
  }

  Boolean deleteCountryState(def id) {
    return deleteEntity(getCountryStateEntityType(), id)
  }
}