package org.camunda.latera.bss.connectors.odoo

import static org.camunda.latera.bss.utils.DateTimeUtil.isDate
import static org.camunda.latera.bss.utils.DateTimeUtil.iso
import static org.camunda.latera.bss.utils.StringUtil.isString
import static org.camunda.latera.bss.utils.StringUtil.join
import static org.camunda.latera.bss.utils.MapUtil.isMap
import static org.camunda.latera.bss.utils.MapUtil.nvl
import static org.camunda.latera.bss.utils.ListUtil.isList

trait Main {
  private static LinkedHashMap DEFAULT_WHERE  = [:]
  private static LinkedHashMap DEFAULT_ORDER  = [:]
  private static List          DEFAULT_FIELDS = []
  private static Integer       DEFAULT_LIMIT  = 0
  private static Integer       DEFAULT_OFFSET = 0

  Map searchQuery(
    List fields = DEFAULT_FIELDS,
    Map where = DEFAULT_WHERE,
    def order = DEFAULT_ORDER,
    Integer limit = DEFAULT_LIMIT,
    Integer offset = DEFAULT_OFFSET
  ) {
    List query   = []
    List orderBy = []

    if (where?.size() > 0) {
      where.each{ CharSequence field, def value ->
        if (isMap(value)) {
          value.each { CharSequence condition, def content ->
            query += """('${field}','${condition}',${escapeSearchValue(content)})"""
          }
        } else {
          String condition = '='

          if (field ==~ /^(.*)!$/) {
            // Not equal
            condition = '!='
            field = field.replaceFirst(/^(.*)!$/, '$1')
          }

          query += """('${field}','${condition}',${escapeSearchValue(value)})"""
        }
      }
    }

    if (order?.size() > 0) {
      if (isMap(order)) {
        convertKeys(order).each { CharSequence column, def direction ->
          orderBy += "'${column} ${direction}'"
        }
      } else if (isList(order)) {
        order.each { CharSequence column ->
          orderBy += "'${column}'"
        }
      } else {
        orderBy += "'${order}'"
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

  Map searchQuery(Map input) {
    LinkedHashMap params = prepareQuery(input)
    return searchQuery(params.fields, params.where, params.order, params.limit, params.offset)
  }

  private Map prepareQuery(Map input) {
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

  private Map prepareParams(Closure paramsParser, Map input, Map additionalParams) {
    return convertParams(nvl(paramsParser(input) + negativeParser(paramsParser, input)) + convertKeys(additionalParams))
  }

  private Map negativeParser(Closure paramsParser, Map negativeInput) {
    LinkedHashMap originalInput = [:]
    LinkedHashMap input         = [:]
    LinkedHashMap negativeWhere = [:]

    // 'stageId!': 3 -> 'stageId': 3
    if (negativeInput?.size() > 0) {
      negativeInput.each{ CharSequence field, def value ->
        if (field ==~ /^(.*)!$/) {
          String originalField = field.replaceFirst(/^(.*)!$/, '$1')
          originalInput[originalField] = value
        }
      }
    }

    // 'stageId': 3 -> 'stage_id': 3
    input = paramsParser(originalInput)

    // 'stage_id': 3 -> 'stage_id!': 3
    input.each{ CharSequence field, def value ->
      negativeWhere["${field}!".toString()] = value
    }
    return negativeWhere
  }

  private Map convertKeys(Map input) {
    return snakeCaseKeys(input)
  }

  private def escapeSearchValue(def value) {
    if (value instanceof Boolean) {
      return capitalize("${value}")
    }
    if (isString(value)) {
      return "'${value}'"
    }
    if (isList(value)) {
      List newList = []
      value.each { def it ->
        newList += escapeSearchValue(it)
      }
      return "[${join(newList, ',')}]"
    }
    return value
  }

  private def convertValue(def value) {
    if (value == null && value == 'null') {
      return false //D`oh
    }
    if (isDate(value)) {
      return "'${iso(value)}'"
    }
    return value
  }

  private Map convertParams(Map input) {
    LinkedHashMap result = [:]
    input.each { CharSequence key, def value ->
      result[key] = convertValue(value)
    }
    return result
  }
}