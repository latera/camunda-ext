package org.camunda.latera.bss.connectors.odoo

import org.camunda.latera.bss.utils.DateTimeUtil
import org.camunda.latera.bss.utils.StringUtil

trait Main {
  private static LinkedHashMap DEFAULT_WHERE  = [:]
  private static LinkedHashMap DEFAULT_ORDER  = [:]
  private static List          DEFAULT_FIELDS = []
  private static Integer       DEFAULT_LIMIT  = 0
  private static Integer       DEFAULT_OFFSET = 0

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
      sendRequest(
        'delete',
        path : "${type}/${id}"
      )
      return true
    } catch (Exception e) {
      logger.error(e)
      return false
    }
  }

  LinkedHashMap searchQuery(
    List fields = DEFAULT_FIELDS,
    LinkedHashMap where = DEFAULT_WHERE,
    def order = DEFAULT_ORDER,
    Integer limit = DEFAULT_LIMIT,
    Integer offset = DEFAULT_OFFSET
  ) {
    def query   = []
    def orderBy = []

    if (where?.size() > 0) {
      where.each{ field, value ->
        if (value instanceof LinkedHashMap) {
          value.each { condition, content ->
            query += """('${field}','${condition}','${content}')'"""
          }
        } else {
          def condition = '='
          def content  = value

          if (field ==~ /^(.*)!$/) {
            // Not equal
            condition = '!='
            field = field.replaceFirst(/^(.*)!$/, '$1')
          }

          if (DateTimeUtil.isDate(value)) {
            content = DateTimeUtil.format(value, DateTimeUtil.ISO_FORMAT)
          }
          query += """('${field}','${condition}','${content}')'"""
        }
      }
    }

    if (order?.size() > 0) {
      if (order instanceof LinkedHashMap) {
        order.each { column, direction ->
          orderBy += "${column} ${direction}"
        }
      } else if (order instanceof List) {
        order.each { column ->
          orderBy += column
        }
      }
    }

    return [
      fields : "[${fields.join(',')}]",
      domain : "[${query.join(',')}]",
      order  : "[${orderBy.join(',')}]",
      limit  : limit,
      offset : offset
    ]
  }

  LinkedHashMap searchQuery(LinkedHashMap input) {
    def params = prepareQuery(input)
    return searchQuery(params.fields, params.where, params.order, params.limit, params.offset)
  }

  LinkedHashMap prepareQuery(LinkedHashMap input) {
    def fields = DEFAULT_FIELDS
    def where  = DEFAULT_WHERE
    def order  = DEFAULT_ORDER
    def limit  = DEFAULT_LIMIT
    def offset = DEFAULT_OFFSET

    if (input.fields) {
      fields = input.fields
      input.remove('fields')
    }
    if (input.order) {
      order = input.order
      input.remove('order')
    }
    if (input.limit) {
      limit = input.limit
      input.remove('limit')
    }
    if (input.offset) {
      offset = input.offset
      input.remove('offset')
    }
    where = input

    return [
      fields : fields,
      where  : where,
      order  : order,
      limit  : limit,
      offset : offset
    ]
  }

  LinkedHashMap nvlParams(LinkedHashMap input) {
    def params = [:]
    input.each { key, value ->
      if (value != null) {
        params[key] = value
      }
    }

    return params
  }

  LinkedHashMap convertKeys(LinkedHashMap input) {
    return StringUtil.snakeCaseKeys(input)
  }
}