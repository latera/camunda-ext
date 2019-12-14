package org.camunda.latera.bss.connectors.hoper.hydra

import static org.camunda.latera.bss.utils.StringUtil.capitalize

trait Entity {
  Map getEntity(Map type, def id, Map query = [:]) {
    LinkedHashMap result = null
    try {
      result = hoper.sendRequest(
        'get',
        path : preparePath(type, id),
        query : query
      )?."${type.one}"
    } catch (Exception e) {
      logger.error(e)
    }
    return result
  }

  List getEntities(Map type, Map query = [:]) {
    List result = null
    try {
      result = hoper.sendRequest(
        'get',
        path  : preparePath(type),
        query : query
      )?."${type.plural}"
    } catch (Exception e) {
      logger.error(e)
    }
    return result
  }

  Map createEntity(Map type, Map params) {
    LinkedHashMap result = null
    try {
      Map paramsWoBase64 = suppressBase64(params)
      Boolean supress = params != paramsWoBase64
      logger.info("Creating ${type.one} with params ${paramsWoBase64}")
      result = hoper.sendRequest(
        'post',
        path : preparePath(type),
        body : ["${type.one}": params],
        supressRequestBodyLog  : supress,
        supressResponseBodyLog : supress
      )?."${type.one}"
      logger.info("   ${capitalize(type.one)} was created successfully!")
    } catch (Exception e) {
      logger.error("   Error while creating ${type.one}")
      logger.error(e)
    }
    return result
  }

  Map updateEntity(Map type, def id, Map params) {
    LinkedHashMap result = null
    try {
      Map paramsWoBase64 = suppressBase64(params)
      Boolean supress = params != paramsWoBase64
      logger.info("Updating ${type.one} id ${id} with params ${paramsWoBase64}")
      result = hoper.sendRequest(
        'put',
        path : preparePath(type, id),
        body : ["${type.one}": params],
        supressRequestBodyLog  : supress,
        supressResponseBodyLog : supress
      )?."${type.one}"
      logger.info("   ${capitalize(type.one)} was updated successfully!")
    } catch (Exception e) {
      logger.error("   Error while updating ${type.one}")
      logger.error(e)
    }
    return result
  }

  Boolean deleteEntity(Map type, def id) {
    try {
      logger.info("Deleting ${type.one} id ${id}")
      hoper.sendRequest(
        'delete',
        path : preparePath(type, id)
      )
      logger.info("   ${capitalize(type.one)} was deleted successfully!")
      return true
    } catch (Exception e) {
      logger.error("   Error while deleting ${type.one}")
      logger.error(e)
      return false
    }
  }
}