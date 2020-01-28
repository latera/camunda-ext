package org.camunda.latera.bss.connectors

import groovy.net.xmlrpc.XMLRPCServerProxy
import static org.camunda.latera.bss.utils.StringUtil.isString
import static org.camunda.latera.bss.utils.Numeric.isIntegerStrict
import static org.camunda.latera.bss.utils.Numeric.isNumber
import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import static org.camunda.latera.bss.utils.Numeric.toFloatSafe
import static org.camunda.latera.bss.utils.StringUtil.varcharToUnicode
import static org.camunda.latera.bss.utils.DateTimeUtil.isDate
import static org.camunda.latera.bss.utils.Oracle.encodeDate
import static org.camunda.latera.bss.utils.Oracle.encodeNull
import org.camunda.latera.bss.connectors.hid.Table
import org.camunda.bpm.engine.delegate.DelegateExecution

class HID implements Table {
  String url
  String user
  private String password
  XMLRPCServerProxy proxy

  HID(DelegateExecution execution) {
    def ENV       = System.getenv()
    this.url      = execution.getVariable('hidUrl')      ?: ENV['HID_URL']  ?: 'http://hid:10080/xml-rpc/db'
    this.user     = execution.getVariable('hidUser')     ?: ENV['HID_USER'] ?: 'hydra'
    this.password = execution.getVariable('hidPassword') ?: ENV['HID_PASSWORD']

    this.proxy = new XMLRPCServerProxy(this.url)
    this.proxy.setBasicAuth(this.user, this.password)
  }

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
      rows.each{ row ->
        // There is row number, just remove it
        row.removeAt(0)

        // Convert codepage from
        List convertedRow = []
        row.each{ value ->
          if (isString(value)) {
            convertedRow.add(varcharToUnicode(value))
          } else {
            convertedRow.add(value)
          }
        }

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

  List queryDatabaseList(CharSequence query, Integer limit = 0, Integer page = 1) {
    return queryDatabase(query, false, limit, page)
  }

  List queryDatabaseMap(CharSequence query, Integer limit = 0, Integer page = 1) {
    return queryDatabase(query, true, limit, page)
  }

  def queryFirst(CharSequence query, Boolean asMap = false) {
    List result = this.queryDatabase(query, asMap, 1)

    if (result) {
      return result.getAt(0)
    } else {
      return null
    }
  }

  List queryFirstList(CharSequence query) {
    return queryFirst(query, false)
  }

  Map queryFirstMap(CharSequence query) {
    return queryFirst(query, true)
  }

  def execute(CharSequence execName, Map params) {
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

  def execute(Map params = [:], CharSequence execName) {
    return execute(execName, params)
  }
}
