package org.camunda.latera.bss.connectors.hid

trait Table {
  static LinkedHashMap tableColumnsCache = [:]
  static List defaultFields = null
  static LinkedHashMap defaultWhere = [:]
  static List defaultOrder = null

  List getTableColumns(String tableName, String tableOwner = 'AIS_NET') {
    String tableFullName = "${tableOwner}.${tableName}"
    if (this.tableColumnsCache.containsKey(tableFullName)) {
      return this.tableColumnsCache[tableFullName]
    } else {
      List columnsList = null

      List result = this.queryDatabase("""
        SELECT COLUMN_NAME
        FROM   ALL_TAB_COLUMNS
        WHERE  TABLE_NAME = '${tableName}'
        AND    OWNER      = '${tableOwner}'
      """, false)

      columnsList = result*.getAt(0) //get only first column values

      if (columnsList) {
        this.tableColumnsCache[tableFullName] = columnsList
      }
      return columnsList
    }
  }

  List getTableData(
    String tableName,
    fields = this.defaultFields,
    LinkedHashMap where = this.defaultWhere,
    List order = this.defaultOrder
  ) {
    String query = "SELECT"
    
    if (fields == '*' || fields == null) {
      fields = this.getTableColumns(tableName)
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
    return this.queryDatabase(query)
  }

  List getTableData(
    LinkedHashMap options,
    String tableName
  ) {
    LinkedHashMap params = [
      fields: this.defaultFields,
      where:  this.defaultWhere,
      order:  this.defaultOrder
    ] + options
    return this.getTableData(tableName, params.fields, params.where, params.order)
  }

  List getTableData(
    LinkedHashMap input
  ) {
    LinkedHashMap params = [
      tableName: '',
      fields:    this.defaultFields,
      where:     this.defaultWhere,
      order:     this.defaultOrder
    ] + input
    return this.getTableData(params.tableName, params.fields, params.where, params.order)
  }

  Object getTableFirst(
    String tableName,
    fields = this.defaultFields,
    LinkedHashMap where = this.defaultWhere,
    List order = this.defaultOrder
  ) {
    if (fields instanceof String && fields != '*') {
      return this.getTableData(tableName, [fields], where)?.getAt(0)?."${fields}"
    }
    return this.getTableData(tableName, fields, where)?.getAt(0)
  }

  Object getTableFirst(
    LinkedHashMap options,
    String tableName
  ) {
    LinkedHashMap params = [
      fields: this.defaultFields,
      where:  this.defaultWhere,
      order:  this.defaultOrder
    ] + options
    return this.getTableFirst(tableName, params.fields, params.where, params.order)
  }

  Object getTableFirst(
    LinkedHashMap input
  ) {
    LinkedHashMap params = [
      tableName: '',
      fields:    this.defaultFields,
      where:     this.defaultWhere,
      order:     this.defaultOrder
    ] + input
    return this.getTableFirst(params.tableName, params.fields, params.where, params.order)
  }
}