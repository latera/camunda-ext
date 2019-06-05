package org.camunda.latera.bss.connectors.hid

import org.camunda.latera.bss.utils.DateTimeUtil
import org.camunda.latera.bss.utils.StringUtil
import org.camunda.latera.bss.utils.ListUtil
import org.camunda.latera.bss.utils.MapUtil
import org.camunda.latera.bss.utils.Oracle

trait Table {
  private static LinkedHashMap TABLE_COLUMNS_CACHE = [:]
  private static LinkedHashMap DEFAULT_WHERE       = [:]
  private static LinkedHashMap DEFAULT_ORDER       = [:]
  private static List          DEFAULT_FIELDS      = null

  List getTableColumns(String tableName, String tableOwner = 'AIS_NET') {
    String tableFullName = "${tableOwner}.${tableName}"
    if (TABLE_COLUMNS_CACHE.containsKey(tableFullName)) {
      return TABLE_COLUMNS_CACHE[tableFullName]
    } else {
      List columnsList = null

      List result = queryDatabase("""
        SELECT COLUMN_NAME
        FROM   ALL_TAB_COLUMNS
        WHERE  TABLE_NAME = '${tableName}'
        AND    OWNER      = '${tableOwner}'
      """, false, true)

      columnsList = result*.getAt(0) //get only first column values

      if (columnsList) {
        TABLE_COLUMNS_CACHE[tableFullName] = columnsList
      }
      return columnsList
    }
  }

  List getTableData(
    String tableName,
    fields = DEFAULT_FIELDS,
    LinkedHashMap where = DEFAULT_WHERE,
    order = DEFAULT_ORDER
  ) {
    String query = "SELECT"

    if (fields == '*' || fields == null) {
      fields = getTableColumns(tableName)
    }

    fields.each{ field ->
      query += """
      '${field.toLowerCase()}', T.${field}""" + (field == fields.last() ? '' : ',')
    }
    query += """
    FROM ${tableName} T"""

    if (where?.size() > 0) {
      query += """
    WHERE 1 = 1"""

      where.each{ field, value ->
        if (field ==~ /^[_]+(.*)$/) {
          //Allow to use same fields name several times
          field = field.replaceFirst(/^[_]+(.*)$/, '$1')
        }
        if (MapUtil.isMap(value)) {
          value.each { condition, content ->
            query += """
    AND ${field} ${condition} ${ListUtil.isList(content) ? '(' + content.join(',') + ')' : content}""" // ['not in': [1,2,3]]
          }
        } else if (ListUtil.isList(value)) {
          value.each { condition ->
            query += """
    AND ${field} ${condition}"""
          }
        } else if (StringUtil.isString(value)) {
          query += """
    AND ${field} = '${value}'"""
        } else if (DateTimeUtil.isDate(value)) {
          query += """
    AND ${field} = ${Oracle.encodeDateStr(value)}"""
        } else {
          query += """
    AND ${field} = ${value}"""
        }
      }
    }

    if (order?.size() > 0) {
      query += """
    ORDER BY"""

      if (order instanceof LinkedHashMap) {
        order.each { column, direction ->
        query += """
          ${column} ${direction}""" + (column == order.keySet().last() ? '' : ',')
        }
      } else if (order instanceof List) {
        order.each { column ->
        query += """
          ${column}""" + (column == order.last() ? '' : ',')
        }
      }
    }
    return queryDatabase(query, true)
  }

  List getTableData(LinkedHashMap options, String tableName) {
    LinkedHashMap params = [
      fields : DEFAULT_FIELDS,
      where  : DEFAULT_WHERE,
      order  : DEFAULT_ORDER
    ] + options
    return getTableData(tableName, params.fields, params.where, params.order)
  }

  List getTableData(LinkedHashMap input) {
    LinkedHashMap params = [
      tableName : '',
      fields    : DEFAULT_FIELDS,
      where     : DEFAULT_WHERE,
      order     : DEFAULT_ORDER
    ] + input
    return getTableData(params.tableName, params.fields, params.where, params.order)
  }

  def getTableFirst(
    String tableName,
    fields = DEFAULT_FIELDS,
    LinkedHashMap where = DEFAULT_WHERE,
    order = DEFAULT_ORDER
  ) {
    if (fields instanceof String && fields != '*') {
      return getTableData(tableName, [fields], where)?.getAt(0)?."${fields}"
    }
    return getTableData(tableName, fields, where)?.getAt(0)
  }

  def getTableFirst(LinkedHashMap options, String tableName) {
    LinkedHashMap params = [
      fields : DEFAULT_FIELDS,
      where  : DEFAULT_WHERE,
      order  : DEFAULT_ORDER
    ] + options
    return getTableFirst(tableName, params.fields, params.where, params.order)
  }

  def getTableFirst(LinkedHashMap input) {
    LinkedHashMap params = [
      tableName : '',
      fields    : DEFAULT_FIELDS,
      where     : DEFAULT_WHERE,
      order     : DEFAULT_ORDER
    ] + input
    return getTableFirst(params.tableName, params.fields, params.where, params.order)
  }
}