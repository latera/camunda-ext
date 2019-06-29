package org.camunda.latera.bss.connectors.hid

import static org.camunda.latera.bss.utils.DateTimeUtil.*
import static org.camunda.latera.bss.utils.StringUtil.*
import static org.camunda.latera.bss.utils.ListUtil.*
import static org.camunda.latera.bss.utils.MapUtil.*
import static org.camunda.latera.bss.utils.Oracle.*

trait Table {
  private static LinkedHashMap TABLE_COLUMNS_CACHE = [:]
  private static LinkedHashMap DEFAULT_WHERE       = [:]
  private static LinkedHashMap DEFAULT_ORDER       = [:]
  private static List          DEFAULT_FIELDS      = null

  private void putTableColumnsCache(CharSequence tableName, List columnsList) {
    if (!TABLE_COLUMNS_CACHE.containsKey(tableName.toString())) {
      TABLE_COLUMNS_CACHE[tableName.toString()] = columnsList
    }
  }

  private List getTableColumnsCached(CharSequence tableName) {
    if (!TABLE_COLUMNS_CACHE.containsKey(tableName.toString())) {
      return TABLE_COLUMNS_CACHE[tableName.toString()]
    }
    return null
  }

  List getTableColumns(CharSequence tableName, CharSequence tableOwner = 'AIS_NET') {
    String tableFullName = "${tableOwner}.${tableName}"
    List columnsList = getTableColumnsCached(tableFullName)
    if (columnsList) {
      return columnsList
    }

    List result = queryDatabase("""
      SELECT COLUMN_NAME
      FROM   ALL_TAB_COLUMNS
      WHERE  TABLE_NAME = '${tableName}'
      AND    OWNER      = '${tableOwner}'
    """, false, true)

    columnsList = result*.getAt(0) //get only first column values
    putTableColumnsCache(tableFullName, columnsList)
    return columnsList
  }

  List getTableData(
    CharSequence tableName,
    fields = DEFAULT_FIELDS,
    Map where = DEFAULT_WHERE,
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
        if (isMap(value)) {
          value.each { condition, content ->
            query += """
    AND ${field} ${condition} ${isList(content) ? '(' + content.join(',') + ')' : content}""" // ['not in': [1,2,3]]
          }
        } else if (isList(value)) {
          value.each { condition ->
            query += """
    AND ${field} ${condition}"""
          }
        } else if (isString(value)) {
          query += """
    AND ${field} = '${value}'"""
        } else if (isDate(value)) {
          query += """
    AND ${field} = ${encodeDateStr(value)}"""
        } else {
          query += """
    AND ${field} = ${value}"""
        }
      }
    }

    if (order?.size() > 0) {
      query += """
    ORDER BY"""

      if (isMap(order)) {
        order.each { column, direction ->
        query += """
          ${column} ${direction}""" + (column == order.keySet().last() ? '' : ',')
        }
      } else if (isList(order)) {
        order.each { column ->
        query += """
          ${column}""" + (column == order.last() ? '' : ',')
        }
      }
    }
    return queryDatabase(query, true)
  }

  List getTableData(Map options, CharSequence tableName) {
    LinkedHashMap params = [
      fields : DEFAULT_FIELDS,
      where  : DEFAULT_WHERE,
      order  : DEFAULT_ORDER
    ] + options
    return getTableData(tableName, params.fields, params.where, params.order)
  }

  List getTableData(Map input) {
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
    Map where = DEFAULT_WHERE,
    order = DEFAULT_ORDER
  ) {
    if (isString(fields) && fields != '*') {
      return getTableData(tableName, [fields], where)?.getAt(0)?."${fields}"
    }
    return getTableData(tableName, fields, where)?.getAt(0)
  }

  def getTableFirst(Map options, CharSequence tableName) {
    LinkedHashMap params = [
      fields : DEFAULT_FIELDS,
      where  : DEFAULT_WHERE,
      order  : DEFAULT_ORDER
    ] + options
    return getTableFirst(tableName, params.fields, params.where, params.order)
  }

  def getTableFirst(Map input) {
    LinkedHashMap params = [
      tableName : '',
      fields    : DEFAULT_FIELDS,
      where     : DEFAULT_WHERE,
      order     : DEFAULT_ORDER
    ] + input
    return getTableFirst(params.tableName, params.fields, params.where, params.order)
  }
}