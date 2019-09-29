package org.camunda.latera.bss.connectors.otrs

private trait Entity {
  Map getEntity(CharSequence type, def id) {
    LinkedHashMap result = null
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

  Map createEntity(CharSequence type, Map params) {
    LinkedHashMap result = null
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

  Map updateEntity(CharSequence type, def id, Map params) {
    LinkedHashMap result = null
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

  Boolean deleteEntity(CharSequence type, def id) {
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