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
  private static String DEFAULT_URL  = 'http://hid:10080/xml-rpc/db'
  private static String DEFAULT_USER = 'hydra'
  XMLRPCServerProxy proxy

  HID(Map params = [:]) {
    if (params.proxy != null) {
      this.proxy = params.proxy
    } else {
      def ENV = System.getenv()

      String url      = params.url      ?: ENV['HID_URL']  ?: DEFAULT_URL
      String user     = params.user     ?: ENV['HID_USER'] ?: DEFAULT_USER
      String password = params.password ?: ENV['HID_PASSWORD']

      this.proxy = new XMLRPCServerProxy(url)
      proxy.setBasicAuth(user, password)
    }
  }

  HID(XMLRPCServerProxy proxy) {
    this(proxy: proxy)
  }

  HID(DelegateExecution execution) {
    this(
      url      : execution.getVariable('hidUrl'),
      user     : execution.getVariable('hidUser'),
      password : execution.getVariable('hidPassword')
    )
  }

  HID(CharSequence url, CharSequence user, CharSequence password) {
    this(
      url      : url,
      user     : user,
      password : password
    )
  }

  Map call(CharSequence name, def params) {
    return proxy.invokeMethod(name, params)
  }

  List queryDatabase(CharSequence query, Boolean asMap = false, Integer limit = 0, Integer page = 1) {
    List result = []
    if (limit != 0 && limit != null) {
      query = """SELECT * FROM (
${query}
)
WHERE ROWNUM <= ${limit}"""
    }
    List rows = call('SELECT', [query.toString(), page]).SelectResult
    if (rows) {
      rows.each{ List row ->
        // There is row number, just remove it
        row.removeAt(0)

        // Convert codepage from
        List convertedRow = []
        row.each{ def value ->
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
    List result = queryDatabase(query, asMap, 1)

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
    return call(execName, [encodedParams])
  }

  Map execute(Map params = [:], CharSequence execName) {
    return execute(execName, params)
  }
}
