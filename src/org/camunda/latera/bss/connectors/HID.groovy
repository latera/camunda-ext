package org.camunda.latera.bss.connectors

import groovy.net.xmlrpc.XMLRPCServerProxy
import static org.camunda.latera.bss.utils.Numeric.isIntegerStrict
import static org.camunda.latera.bss.utils.Numeric.isNumber
import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import static org.camunda.latera.bss.utils.Numeric.toFloatSafe
import static org.camunda.latera.bss.utils.DateTimeUtil.isDate
import static org.camunda.latera.bss.utils.Oracle.encodeDate
import static org.camunda.latera.bss.utils.Oracle.encodeNull
import static org.camunda.latera.bss.utils.StringUtil.isString
import static org.camunda.latera.bss.utils.StringUtil.varcharToUnicode
import static org.camunda.latera.bss.utils.ListUtil.isList
import org.camunda.latera.bss.connectors.hid.Table
import org.camunda.bpm.engine.delegate.DelegateExecution

class HID implements Table {
  String url
  String user
  private String password
  private static String DEFAULT_URL  = 'http://hid:10080/xml-rpc/db'
  private static String DEFAULT_USER = 'hydra'
  XMLRPCServerProxy proxy

  HID(DelegateExecution execution) {
    def ENV       = System.getenv()
    this.url      = execution.getVariable('hidUrl')      ?: ENV['HID_URL']  ?: DEFAULT_URL
    this.user     = execution.getVariable('hidUser')     ?: ENV['HID_USER'] ?: DEFAULT_USER
    this.password = execution.getVariable('hidPassword') ?: ENV['HID_PASSWORD']

    this.proxy = new XMLRPCServerProxy(this.url)
    this.proxy.setBasicAuth(this.user, this.password)
  }

  /**
    Query database and return rows
    @param query SELECT query
    @param asMap If true, return columns with names, otherwise return columns list
    @param limit Max rows count
    @param page Page number to return data from
  */
  List queryDatabase(CharSequence query, Boolean asMap = false, Integer limit = 0, Integer page = 1) {
    List result = []
    if (limit != 0 && limit != null) {
      query = """SELECT * FROM (
${query}
)
WHERE ROWNUM <= ${limit}"""
    }
    LinkedHashMap answer = this.proxy.invokeMethod('SELECT', [query.toString(), page])
    List rows = answer.SelectResult
    if (rows) {
      rows.each{ List row ->
        // There is row number, just remove it
        row.removeAt(0)

        // Convert codepage from Oracle internal to Unicode
        List convertedRow = convertRow(row)

        if (asMap) {
          // Use "'VC_VALUE', VC_VALUE" format because SELECT procedure don't return column names
          LinkedHashMap mappedRow = (convertedRow as Object[]).toSpreadMap()
          result.add(mappedRow)
        } else {
          result.add(convertedRow)
        }
      }
    }

    return result
  }

  private List convertRow(List row) {
    List result = []
    row.each{ def value ->
      if (isList(value)) {
        result.add(convertRow(value))
      } else {
        result.add(convertCodepage(value))
      }
    }
    return result
  }

  private def convertCodepage(def value) {
    if (isString(value)) {
      return varcharToUnicode(value)
    } else {
      return value
    }
  }

  /**
    Query database and return rows as columns list
    @param query SELECT query
    @param limit Max rows count
    @param page Page number to return data from
  */
  List<List> queryDatabaseList(CharSequence query, Integer limit = 0, Integer page = 1) {
    return queryDatabase(query, false, limit, page)
  }

  /**
    Query database and return rows as columns with names
    @param query SELECT query
    @param limit Max rows count
    @param page Page number to return data from
  */
  List<Map> queryDatabaseMap(CharSequence query, Integer limit = 0, Integer page = 1) {
    return queryDatabase(query, true, limit, page)
  }

  /**
    Query database and return first row
    @param query SELECT query
    @param asMap If true, return columns with names, otherwise return columns list. Default: false
  */
  def queryFirst(CharSequence query, Boolean asMap = false) {
    List result = queryDatabase(query, asMap, 1)

    if (result) {
      return result.getAt(0)
    } else {
      return null
    }
  }

  /**
    Query database and return first row as columns list
    @param query SELECT query
  */
  List queryFirstList(CharSequence query) {
    return queryFirst(query, false)
  }

  /**
    Query database and return first row as columns with names
    @param query SELECT query
  */
  Map queryFirstMap(CharSequence query) {
    return queryFirst(query, true)
  }

  /**
    Call procedure
    @param execName Procedure name
    @param params Arguments
  */
  Map execute(CharSequence execName, Map params) {
    LinkedHashMap encodedParams = [:]
    params.each{ CharSequence key, def value ->
      if (isDate(value)) {
        value = encodeDate(value)
      }
      if (isString(value) || key.startsWith('vch_') || key.startsWith('VC_')) {
        value = value.toString() // Convert GStringImpl to String
      }
      if (key.startsWith('num_') || key.startsWith('N_')) {
        if (isIntegerStrict(value)) { // Convert Ids to numbers
          value = toIntSafe(value)
        } else if (isNumber(value)) { // Convert floats to numbers to remove localized delimiters
          value = toFloatSafe(value)
        }
      }
      encodedParams[key] = encodeNull(value)
    }
    return this.proxy.invokeMethod(execName, [encodedParams])
  }

  /**
    Call procedure

    Overload with named arguments
    @see #execute(CharSequence,Map)
  */
  Map execute(Map params = [:], CharSequence execName) {
    return execute(execName, params)
  }
}
