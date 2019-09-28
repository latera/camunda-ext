package org.camunda.latera.bss.connectors.odoo

private trait Entity {
  private Map getEntity(CharSequence type, def id) {
    LinkedHashMap result = null
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

  private List getEntitiesBy(CharSequence type, Map params) {
    List result = []
    LinkedHashMap query = searchQuery(params)
    try {
      result = sendRequest(
        'get',
        path  : "${type}/",
        query : query
      )?.data
    } catch (Exception e) {
      logger.error(e)
    }
    return result
  }

  private Map getEntityBy(CharSequence type, Map params) {
    return getEntitiesBy(type, params)?.getAt(0)
  }

  private Map createEntity(CharSequence type, Map params) {
    LinkedHashMap result = null
    try {
      logger.info("Creating ${type} with params ${params}")
      result = sendRequest(
        'post',
        path : "${type}",
        body : params
      )?.data
    } catch (Exception e) {
      logger.error("   Error while creating ${type}")
      logger.error(e)
    }
    return result
  }

  private Map updateEntity(CharSequence type, def id, Map params) {
    LinkedHashMap result = null
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

  private Boolean deleteEntity(CharSequence type, def id) {
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