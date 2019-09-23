package org.camunda.latera.bss.connectors.hid

import static org.camunda.latera.bss.utils.DateTimeUtil.*
import static org.camunda.latera.bss.utils.StringUtil.*
import static org.camunda.latera.bss.utils.ListUtil.*
import static org.camunda.latera.bss.utils.MapUtil.*
import static org.camunda.latera.bss.utils.Oracle.*
import org.camunda.latera.bss.internal.TableColumnCache

trait Table {
  private static LinkedHashMap DEFAULT_WHERE       = [:]
  private static LinkedHashMap DEFAULT_ORDER       = [:]
  private static List          DEFAULT_FIELDS      = null
  private static Integer       DEFAULT_LIMIT       = 0
  private static String        DEFAULT_TABLE_ALIAS = 'T'
  private static Boolean       DEFAULT_AS_MAP      = true

  List getTableColumns(CharSequence tableName, CharSequence tableOwner = 'AIS_NET') {
    String tableFullName = "${tableOwner}.${tableName}"
    List columnsList = TableColumnCache.instance.get(tableFullName)
    if (columnsList) {
      return columnsList
    }

    List result = queryDatabase("""
      SELECT COLUMN_NAME
      FROM   ALL_TAB_COLUMNS
      WHERE  TABLE_NAME = '${tableName}'
      AND    OWNER      = '${tableOwner}'
    """, false, 0, 0)

    columnsList = lowerCase(result*.getAt(0)) // get only first column values
    return TableColumnCache.instance.putAndGet(tableFullName, columnsList)
  }

  String prepareTableQuery(
    CharSequence tableName,
    fields = DEFAULT_FIELDS,
    Map where = DEFAULT_WHERE,
    order = DEFAULT_ORDER,
    CharSequence tableAlias = DEFAULT_TABLE_ALIAS,
    Boolean asMap = DEFAULT_AS_MAP
  ) {
    String query = "SELECT"
    List columns = getTableColumns(tableName)

    if (isList(fields)) { // Allow to pass fields: ['*', [n_recipient_id: '...']]
      List newFields = []
      fields.each { field ->
        if (field == '*') {
          newFields << columns
        } else {
          newFields << field
        }
      }
      fields = newFields
    } else if (isMap(fields)) { // Allow to pass fields: ['*': null, n_recipient_id: '...']
      List newFields = []
      fields.each { name, value ->
        if (name == '*' || value == '*') {
          newFields << columns
        } else {
          newFields << ["${name}": value]
        }
      }
      fields = newFields
    } else if (fields == '*' || fields == null) {
      fields = columns
    } else if (isString(fields)) {
      fields = [fields]
    }

    fields.each{ field ->
      if (isMap(field)) {
        field.each { name, content ->
          String prefix = asMap ? "'${name}'," : ''
          if (isList(content)) {
            query += """
            ${prefix} (${content.join(' ')}) ${name},""" // fields: [n_recipient_id: ['select 1 from dual']]
          } else if (isString(content) && content =~ /(?i)select/) {
            query += """
            ${prefix} (${content}) ${name},""" // fields: [n_recipient_id: 'select 1 from dual']
          } else {
            query += """
            ${prefix} ${content} ${name},""" // fields: [n_recipient_id: 1]
          }
        }
      } else {
        String prefix = asMap ? "'${field.toLowerCase()}'," : ''
        query += """
        ${prefix} ${tableAlias}.${field},"""
      }
    }
    query = query.replaceAll(/,*$/, '') + """
    FROM ${tableName} ${tableAlias}"""

    if (where?.size() > 0) {
      query += """
    WHERE 1 = 1"""

      where.each{ field, value ->
        if (field ==~ /^[_]+(.*)$/) {
          field = field.replaceFirst(/^[_]+(.*)$/, '$1') //beginDate: ['>': now()], _beginDate: ['<': dayEnd()], __beginDate: [...]
        }
        if (field.toString().toLowerCase() in columns) {
          field = "${tableAlias}.${field}"
        }
        if (isMap(value)) {
          value.each { condition, content ->
            query += """
      AND ${field} ${condition} ${isList(content) ? '(' + content.join(',') + ')' : content}""" // n_subject_id: [is: null], n_main_object_id: [is: 'not null'], n_good_id: [in: [1,2,3]], n_doc_id: ['not in': [1,2,3]], vc_code: [like: '%subst%'], vc_name: ['not like': '%subst%']
          }
        } else if (isList(value)) {
          value.each { condition ->
            if (isString(condition) && condition =~ /(?i)select/) {
              query += """
      AND ${field} (${condition})""" // exists: ['select 1 from dual']
            } else {
              query += """
      AND ${field} ${condition}""" // sysdate: ['between d_begin and nvl(d_end, sysdate)']
            }
          }
        } else if (isString(value)) {
          query += """
    AND ${field} = '${value}'""" // vc_code: 'code'
        } else if (isDate(value)) {
          query += """
    AND ${field} = ${encodeDateStr(value)}""" // d_begin: LocalDateTime('2001-01-01T00:00:00+00')
        } else {
          query += """
    AND ${field} = ${value}""" // n_object_id: 123
        }
      }
    }

    if (order?.size() > 0) {
      query += """
    ORDER BY"""

      if (isMap(order)) {
        order.each { field, direction ->
        if (field.toString().toLowerCase() in columns) {
          field = "${tableAlias}.${field}"
        }
        query += """
          ${field} ${direction},""" // [c_fl_main: 'asc', d_begin: 'desc']
        }
      } else if (isList(order)) {
        order.each { field ->
        query += """
          ${field},""" // ['c_fl_main asc', 'd_begin desc']
        }
      }
    }
    query = query.replaceAll(/,*$/, '')
    return query
  }

  String prepareTableQuery(Map options, CharSequence tableName) {
    LinkedHashMap params = [
      fields     : DEFAULT_FIELDS,
      where      : DEFAULT_WHERE,
      order      : DEFAULT_ORDER,
      tableAlias : DEFAULT_TABLE_ALIAS,
      asMap      : DEFAULT_AS_MAP
    ] + options
    return prepareTableQuery(tableName, params.fields, params.where, params.order, params.tableAlias, params.asMap)
  }

  List getTableData(
    CharSequence tableName,
    fields = DEFAULT_FIELDS,
    Map where = DEFAULT_WHERE,
    order = DEFAULT_ORDER,
    Integer limit = DEFAULT_LIMIT,
    CharSequence tableAlias = DEFAULT_TABLE_ALIAS,
    Boolean asMap = DEFAULT_AS_MAP
  ) {
    String query = prepareTableQuery(tableName, fields, where, order, tableAlias, asMap)
    return queryDatabase(query, asMap, limit)
  }

  List getTableData(Map options, CharSequence tableName) {
    LinkedHashMap params = [
      fields     : DEFAULT_FIELDS,
      where      : DEFAULT_WHERE,
      order      : DEFAULT_ORDER,
      limit      : DEFAULT_LIMIT,
      tableAlias : DEFAULT_TABLE_ALIAS,
      asMap      : DEFAULT_AS_MAP
    ] + options
    return getTableData(tableName, params.fields, params.where, params.order, params.limit, params.tableAlias, params.asMap)
  }

  List getTableData(Map input) {
    LinkedHashMap params = [
      tableName  : '',
      fields     : DEFAULT_FIELDS,
      where      : DEFAULT_WHERE,
      order      : DEFAULT_ORDER,
      limit      : DEFAULT_LIMIT,
      tableAlias : DEFAULT_TABLE_ALIAS,
      asMap      : DEFAULT_AS_MAP
    ] + input
    return getTableData(params.tableName, params.fields, params.where, params.order, params.limit, params.tableAlias, params.asMap)
  }

  def getTableFirst(
    String tableName,
    fields = DEFAULT_FIELDS,
    Map where = DEFAULT_WHERE,
    order = DEFAULT_ORDER,
    CharSequence tableAlias = DEFAULT_TABLE_ALIAS,
    Boolean asMap = DEFAULT_AS_MAP
  ) {
    if (asMap && isString(fields) && fields != '*') {
      return getTableData(tableName, fields, where, order, 1, tableAlias, asMap)?.getAt(0)?."${fields}"
    }
    return getTableData(tableName, fields, where, order, 1, tableAlias, asMap)?.getAt(0)
  }

  def getTableFirst(Map options, CharSequence tableName) {
    LinkedHashMap params = [
      fields     : DEFAULT_FIELDS,
      where      : DEFAULT_WHERE,
      order      : DEFAULT_ORDER,
      tableAlias : DEFAULT_TABLE_ALIAS,
      asMap      : DEFAULT_AS_MAP
    ] + options
    return getTableFirst(tableName, params.fields, params.where, params.order, params.tableAlias, params.asMap)
  }

  def getTableFirst(Map input) {
    LinkedHashMap params = [
      tableName  : '',
      fields     : DEFAULT_FIELDS,
      where      : DEFAULT_WHERE,
      order      : DEFAULT_ORDER,
      tableAlias : DEFAULT_TABLE_ALIAS,
      asMap      : DEFAULT_AS_MAP
    ] + input
    return getTableFirst(params.tableName, params.fields, params.where, params.order, params.tableAlias, params.asMap)
  }
}