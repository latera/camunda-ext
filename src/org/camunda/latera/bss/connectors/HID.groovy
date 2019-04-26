package org.camunda.latera.bss.connectors

import groovy.net.xmlrpc.*
import java.time.LocalDateTime
import org.codehaus.groovy.runtime.GStringImpl
import org.camunda.latera.bss.utils.StringUtil
import org.camunda.latera.bss.utils.DateTimeUtil
import org.camunda.latera.bss.utils.Oracle
import org.camunda.latera.bss.connectors.hid.Table
import org.camunda.bpm.engine.delegate.DelegateExecution

class HID implements Table {
  XMLRPCServerProxy proxy

  HID(DelegateExecution execution) {
    def url      = execution.getVariable('hidUrl')  ?: 'http://hid:10080'
    def user     = execution.getVariable('hidUser') ?: 'hydra'
    def password = execution.getVariable('hidPassword')

    this.proxy = new XMLRPCServerProxy(url)
    this.proxy.setBasicAuth(user, password)
  }

  Object queryDatabase(String query, Boolean asMap = false, Boolean noLimit = false) {
    List result = []
    def pageNumber = noLimit ? 0 : 1
    LinkedHashMap answer = this.proxy.invokeMethod('SELECT', [query, pageNumber])
    List rows = answer.SelectResult
    if (rows) {
      rows.each{ row ->
        // There is row number, just remove it
        row.removeAt(0)

        // Convert codepage from
        List convertedRow = []
        row.each{ value ->
          if (StringUtil.isString(value)) {
            convertedRow.add(StringUtil.varcharToUnicode(value))
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

  Object queryFirst(String query, Boolean asMap = false, Boolean noLimit = false) {
    def result = this.queryDatabase(query, asMap)

    if (result) {
      return result.getAt(0)
    } else {
      return null
    }
  }

  Object execute(String execName, LinkedHashMap params) {
    LinkedHashMap encodedParams = [:]
    params.each{ key, value ->
      if (DateTimeUtil.isDate(value)) {
        value = Oracle.encodeDate(value)
      }
      if (value instanceof GStringImpl) {
        value = value.toString()
      }
      encodedParams[key] = Oracle.encodeNull(value)
    }
    return this.proxy.invokeMethod(execName, [encodedParams])
  }
}
