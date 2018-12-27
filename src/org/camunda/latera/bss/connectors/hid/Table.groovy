package org.camunda.latera.bss.connectors.hid

trait Table {
  static LinkedHashMap TABLE_COLUMNS_CACHE = [:]
  static LinkedHashMap DEFAULT_WHERE       = [:]
  static List          DEFAULT_FIELDS      = null
  static List          DEFAULT_ORDER       = null

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
      """, false)

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
    List order = DEFAULT_ORDER
  ) {
    String query = "SELECT"
    
    if (fields == '*' || fields == null) {
      fields = getTableColumns(tableName)
    }
    
    fields.each{ field ->
      query += """
      '${field.toLowerCase()}', ${field}""" + (field == fields.last() ? '' : ',')
    }
    query += """
    FROM ${tableName}"""

    if (where?.size() > 0) {
      query += """
    WHERE 1 = 1"""

      where.each{ field, value ->
        if (value instanceof LinkedHashMap) {
          value.each { condition, content ->
            query += """
    AND ${field} ${condition} ${content}"""
          }
        } else if (value instanceof List) {
          value.each { condition ->
            query += """
    AND ${field} ${condition}"""
          }
        } else if (value instanceof String) {
          query += """
    AND ${field} = '${value}'"""
        } else {
          query += """
    AND ${field} = ${value}"""
        }
      }
    }

    if (order?.size() > 0) {
      query += """
    ORDER BY"""
      order.each{column ->
        query += """
        ${column}""" + (column == order.last() ? '' : ',')
      }
    }
    return queryDatabase(query)
  }

  List getTableData(
    LinkedHashMap options,
    String tableName
  ) {
    LinkedHashMap params = [
      fields: DEFAULT_FIELDS,
      where:  DEFAULT_WHERE,
      order:  DEFAULT_ORDER
    ] + options
    return getTableData(tableName, params.fields, params.where, params.order)
  }

  List getTableData(
    LinkedHashMap input
  ) {
    LinkedHashMap params = [
      tableName: '',
      fields:    DEFAULT_FIELDS,
      where:     DEFAULT_WHERE,
      order:     DEFAULT_ORDER
    ] + input
    return getTableData(params.tableName, params.fields, params.where, params.order)
  }

  Object getTableFirst(
    String tableName,
    fields = DEFAULT_FIELDS,
    LinkedHashMap where = DEFAULT_WHERE,
    List order = DEFAULT_ORDER
  ) {
    if (fields instanceof String && fields != '*') {
      return getTableData(tableName, [fields], where)?.getAt(0)?."${fields}"
    }
    return getTableData(tableName, fields, where)?.getAt(0)
  }

  Object getTableFirst(
    LinkedHashMap options,
    String tableName
  ) {
    LinkedHashMap params = [
      fields: DEFAULT_FIELDS,
      where:  DEFAULT_WHERE,
      order:  DEFAULT_ORDER
    ] + options
    return getTableFirst(tableName, params.fields, params.where, params.order)
  }

  Object getTableFirst(
    LinkedHashMap input
  ) {
    LinkedHashMap params = [
      tableName: '',
      fields:    DEFAULT_FIELDS,
      where:     DEFAULT_WHERE,
      order:     DEFAULT_ORDER
    ] + input
    return getTableFirst(params.tableName, params.fields, params.where, params.order)
  }
}