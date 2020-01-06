package org.camunda.latera.bss.connectors.otrs

trait Entity {
  Map getEntity(Map params = [:], CharSequence type, def id) {
    LinkedHashMap result = null
    try {
      result = sendRequest(
        'get',
        path : "${type}Get/${id}",
        query: params.query
      )
    } catch (Exception e) {
      logger.error(e)
    }
    return result
  }

  Map createEntity(Map params = [:], CharSequence type) {
    LinkedHashMap result = null
    try {
      Map paramsWoBase64 = suppressBase64(params)
      Boolean supress = params != paramsWoBase64
      logger.info("Creating ${type} with params ${paramsWoBase64}")
      result = sendRequest(
        'post',
        path : "${type}Create/",
        body : params,
        supressRequestBodyLog  : supress,
        supressResponseBodyLog : supress
      )
    } catch (Exception e) {
      logger.error("   Error while creating ${type}")
      logger.error(e)
    }
    return result
  }

  Map updateEntity(Map params = [:], CharSequence type, def id) {
    LinkedHashMap result = null
    try {
      Map paramsWoBase64 = suppressBase64(params)
      Boolean supress = params != paramsWoBase64
      logger.info("Updating ${type} id ${id} with params ${paramsWoBase64}")
      result = sendRequest(
        'put',
        path : "${type}Update/${id}",
        body : params,
        supressRequestBodyLog  : supress,
        supressResponseBodyLog : supress
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