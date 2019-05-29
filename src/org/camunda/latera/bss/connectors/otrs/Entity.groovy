package org.camunda.latera.bss.connectors.otrs

trait Entity {
  LinkedHashMap getEntity(def type, def id) {
    def result = null
    try {
      result = sendRequest(
        'get',
        path : "${type}Get/${id}"
      )
    } catch (Exception e) {
      logger.error(e)
    }
    return result
  }

  LinkedHashMap createEntity(def type, LinkedHashMap params) {
    def result = null
    try {
      logger.info("Creating ${type} with params ${params}")
      result = sendRequest(
        'post',
        path : "${type}Create/",
        body : params
      )
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
        path : "${type}Update/${id}",
        body : params
      )
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
        path: "${type}Delete/${id}"
      )
      return true
    } catch (Exception e) {
      logger.error("   Error while deleting ${type}")
      logger.error(e)
      return false
    }
  }
}