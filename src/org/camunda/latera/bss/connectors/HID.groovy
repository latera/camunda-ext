package org.camunda.latera.bss.connectors

import groovy.net.xmlrpc.*
import org.camunda.latera.bss.utils.StringUtil
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

  Object queryDatabase(String query, Boolean asMap = true) {
    List result = []
    List rows = this.proxy.invokeMethod('SELECT', [query]).SelectResult
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

  Object queryFirst(String query, Boolean asMap = true) {
    return this.queryDatabase(query, asMap)?.getAt(0)
  }

  Object execute(String execName, LinkedHashMap params) {
    LinkedHashMap encodedParams = [:]
    params.each{ key, value ->
      encodedParams[key] = Oracle.encodeNull(value)
    }
    return this.proxy.invokeMethod(execName, [encodedParams])
  }
}