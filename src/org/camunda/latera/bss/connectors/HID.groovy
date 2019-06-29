package org.camunda.latera.bss.connectors

import groovy.net.xmlrpc.*
import java.time.LocalDateTime
import static org.camunda.latera.bss.utils.StringUtil.*
import static org.camunda.latera.bss.utils.DateTimeUtil.*
import static org.camunda.latera.bss.utils.Oracle.*
import org.camunda.latera.bss.connectors.hid.Table
import org.camunda.bpm.engine.delegate.DelegateExecution

class HID implements Table {
  String url
  String user
  private String password
  XMLRPCServerProxy proxy

  HID(DelegateExecution execution) {
    def ENV       = System.getenv()
    this.url      = ENV['HID_URL']      ?: execution.getVariable('hidUrl')  ?: 'http://hid:10080'
    this.user     = ENV['HID_USER']     ?: execution.getVariable('hidUser') ?: 'hydra'
    this.password = ENV['HID_PASSWORD'] ?: execution.getVariable('hidPassword')

    this.proxy = new XMLRPCServerProxy(this.url)
    this.proxy.setBasicAuth(this.user, this.password)
  }

  List queryDatabase(CharSequence query, Boolean asMap = false, Boolean noLimit = false) {
    List result = []
    Integer pageNumber = noLimit ? 0 : 1
    LinkedHashMap answer = this.proxy.invokeMethod('SELECT', [query.toString(), pageNumber])
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

  List queryDatabaseList(CharSequence query, Boolean noLimit = false) {
    return queryDatabase(query, false, noLimit)
  }

  List queryDatabaseMap(CharSequence query, Boolean noLimit = false) {
    return queryDatabase(query, true, noLimit)
  }

  def queryFirst(CharSequence query, Boolean asMap = false, Boolean noLimit = false) {
    List result = this.queryDatabase(query, asMap)

    if (result) {
      return result.getAt(0)
    } else {
      return null
    }
  }

  List queryFirstList(CharSequence query, Boolean noLimit = false) {
    return queryFirst(query, false, noLimit)
  }

  Map queryFirstMap(CharSequence query, Boolean noLimit = false) {
    return queryFirst(query, true, noLimit)
  }

  def execute(CharSequence execName, Map params) {
    LinkedHashMap encodedParams = [:]
    params.each{ key, value ->
      if (isDate(value)) {
        value = encodeDate(value)
      }
      if (isString(value)) {
        value = value.toString() // Convert GStringImpl to String
      }
      encodedParams[key] = encodeNull(value)
    }
    return this.proxy.invokeMethod(execName, [encodedParams])
  }
}
