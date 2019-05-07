package org.camunda.latera.bss.connectors.odoo

trait Entity {
  LinkedHashMap getEntityDefaultParams() {
    return [:]
  }

  LinkedHashMap getEntityParamsMap(LinkedHashMap params) {
    return [:]
  }

  LinkedHashMap getEntityParams(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    def params = getEntityDefaultParams() + input
    def where  = getEntityParamsMap(params)
    return prepareQuery(where + convertKeys(additionalParams))
  }

  LinkedHashMap getEntity(def type, def id) {
    def result = null
    try {
      result = sendRequest(
        'get',
        path : "${type}/${id}"
      )?.data
    } catch (Exception e) {
      logger.error(e)
    }
    return result
  }

  List getEntitiesBy(def type, LinkedHashMap input) {
    def result = []
    def query = searchQuery(input)
    try {
      result = sendRequest(
        'get',
        path : "${type}/",
        body : query
      )?.data
    } catch (Exception e) {
      logger.error(e)
    }
    return result
  }

  LinkedHashMap getEntityBy(def type, LinkedHashMap input) {
    return getEntitiesBy(type, input)?.getAt(0)
  }

  LinkedHashMap createEntity(def type, LinkedHashMap params) {
    def result = null
    try {
      logger.info("Creating ${type} with params ${params}")
      result = sendRequest(
        'post',
        path : "${type}/",
        body : params
      )?.data
    } catch (Exception e) {
      logger.error("   Error while creating ${type}")
      logger.error(e)
    }
    return result
  }

  LinkedHashMap updateEntity(def type, def id, LinkedHashMap params) {
    def result = null
    try {
      logger.info("Updating ${type} id ${id} with params ${params}")
      result = sendRequest(
        'put',
        path : "${type}/${id}",
        body : params
      )?.data
    } catch (Exception e) {
      logger.error("   Error while updating ${type}")
      logger.error(e)
    }
    return result
  }

  Boolean deleteEntity(def type, def id) {
    try {
      logger.info("Deleting ${type} id ${id}")
      sendRequest(
        'delete',
        path : "${type}/${id}"
      )
      return true
    } catch (Exception e) {
      logger.error("   Error while deleting ${type}")
      logger.error(e)
      return false
    }
  }
}