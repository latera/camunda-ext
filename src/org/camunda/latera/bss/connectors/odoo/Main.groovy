package org.camunda.latera.bss.connectors.odoo

import org.camunda.latera.bss.utils.DateTimeUtil
import org.camunda.latera.bss.utils.StringUtil

trait Main {
  private static LinkedHashMap DEFAULT_WHERE  = [:]
  private static LinkedHashMap DEFAULT_ORDER  = [:]
  private static List          DEFAULT_FIELDS = []
  private static Integer       DEFAULT_LIMIT  = 0
  private static Integer       DEFAULT_OFFSET = 0

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
            query += """('${field}','${condition}',${escapeSearchValue(content)})"""
          }
        } else {
          def condition = '='
          def content  = value

          if (field ==~ /^(.*)!$/) {
            // Not equal
            condition = '!='
            field = field.replaceFirst(/^(.*)!$/, '$1')
          }

          query += """('${field}','${condition}',${escapeSearchValue(content)})"""
        }
      }
    }

    if (order?.size() > 0) {
      if (order instanceof LinkedHashMap) {
        order.each { column, direction ->
          orderBy += "'${column} ${direction}'"
        }
      } else if (order instanceof List) {
        order.each { column ->
          orderBy += "'${column}'"
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
        if (value == 'null' || value == 'NULL') {
          params[key] = null
        } else {
          params[key] = value
        }
      }
    }

    return params
  }

  LinkedHashMap convertKeys(LinkedHashMap input) {
    return StringUtil.snakeCaseKeys(input)
  }

  def escapeSearchValue(def value) {
    if (value instanceof Boolean) {
      return StringUtil.capitalize("${value}")
    }
    if (StringUtil.isString(value)) {
      return "'${value}'"
    }
    return value
  }

  def convertValue(def value) {
    if (value == null && value == 'null') {
      return false //D`oh
    }
    if (DateTimeUtil.isDate(value)) {
      return "'${DateTimeUtil.iso(value)}'"
    }
    return value
  }

  LinkedHashMap convertParams(LinkedHashMap input) {
    LinkedHashMap result = [:]
    input.each { key, value ->
      result[key] = convertValue(value)
    }
  }
}