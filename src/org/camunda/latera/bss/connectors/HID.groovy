package org.camunda.latera.bss.connectors

import groovy.net.xmlrpc.*
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.latera.bss.logging.SimpleLogger

class HID {
  XMLRPCServerProxy proxy
  private String logger

  HID(DelegateExecution execution) {
    String hidUrl = execution.getVariable('hidUrl')
    String hidUser = execution.getVariable('hidUser')
    String hidPassword = execution.getVariable('hidPassword')
    logger = new SimpleLogger(execution)
    proxy = new XMLRPCServerProxy(hidUrl)
    proxy.setBasicAuth(hidUser, hidPassword)
  }

  Object queryDatabase(String query) {
    return this.proxy.SELECT(query).SelectResult
  }

  Object execute(String procedure, LinkedHashMap params) {
    return this.proxy."${procedure}"(params)
  }

  String getRefId(String code) {
    return this.queryDatabase("""
        SELECT 
        SI_REF_PKG_S.GET_ID_BY_CODE('${code}') 
        FROM DUAL""")?.getAt(0)?.getAt(1)
  }

  String getSubjValueTypeId(String code) {
    return this.queryDatabase("""
      SELECT N_SUBJ_VALUE_TYPE_ID 
        FROM SI_V_SUBJ_VALUES_TYPE 
       WHERE VC_CODE = '${code}'
    """)?.getAt(0)?.getAt(1)
  }

  String getGoodValueTypeId(String code) {
    return this.queryDatabase("""
      SELECT 
      SR_GOODS_PKG_S.GET_GOOD_VALUE_TYPE_ID('${code}')
      FROM DUAL
    """)?.getAt(0)?.getAt(1)
  }

  String getObjValueTypeId(String code) {
    return this.getGoodValueTypeId(code)
  }

  String getDocValueTypeId(String code) {
    return this.queryDatabase("""
      SELECT N_DOC_VALUE_TYPE_ID
        FROM SS_V_WFLOW_DOC_VALUES_TYPE
       WHERE VC_CODE = '${code}' 
    """)?.getAt(0)?.getAt(1)
  }
}
