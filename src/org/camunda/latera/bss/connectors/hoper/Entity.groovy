package org.camunda.latera.bss.connectors.hoper.hydra

import org.camunda.latera.bss.utils.StringUtil

trait Entity {
  LinkedHashMap getEntityDefaultParams() {
    return [:]
  }

  LinkedHashMap getEntityParamsMap(LinkedHashMap params) {
    return [:]
  }

  LinkedHashMap getEntityParams(LinkedHashMap input) {
    def params = getEntityDefaultParams() + input
    def where  = getEntityParamsMap(params)
    return getEntityParamsMap(params)
  }

  LinkedHashMap getEntity(def type, def id) {
    def result = null
    try {
      result = hoper.sendRequest(
        'get',
        path : "${type.parent}/${type.plural}/${id}"
      )?."${type.one}"
    } catch (Exception e) {
      logger.error(e)
    }
    return result
  }

  LinkedHashMap createEntity(def type, LinkedHashMap params) {
    def result = null
    try {
      logger.info("Creating ${type.one} with params ${params}")
      result = hoper.sendRequest(
        'post',
        path : "${type.parent}/${type.plural}",
        body : ["${type.one}": params]
      )?."${type.one}"
      logger.info("   ${StringUtil.capitalize(type.one)} was created successfully!")
    } catch (Exception e) {
      logger.error("   Error while creating ${type.one}")
      logger.error(e)
    }
    return result
  }

  LinkedHashMap updateEntity(def type, def id, LinkedHashMap params) {
    def result = null
    try {
      logger.info("Updating ${type.one} id ${id} with params ${params}")
      result = hoper.sendRequest(
        'put',
        path : "${type.parent}/${type.plural}/${id}",
        body : ["${type.one}": params]
      )?."${type.one}"
      logger.info("   ${StringUtil.capitalize(type.one)} was updated successfully!")
    } catch (Exception e) {
      logger.error("   Error while updating ${type.one}")
      logger.error(e)
    }
    return result
  }

  Boolean deleteEntity(def type, def id) {
    try {
      logger.info("Deleting ${type.one} id ${id}")
      hoper.sendRequest(
        'delete',
        path : "${type.parent}/${type.plural}/${id}"
      )
      logger.info("   ${StringUtil.capitalize(type.one)} was deleted successfully!")
      return true
    } catch (Exception e) {
      logger.error("   Error while deleting ${type.one}")
      logger.error(e)
      return false
    }
  }
}