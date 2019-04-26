package org.camunda.latera.bss.connectors

import org.camunda.latera.bss.http.HTTPRestProcessor
import org.camunda.latera.bss.logging.SimpleLogger
import org.camunda.bpm.engine.delegate.DelegateExecution

class Hoper {
  HTTPRestProcessor processor
  LinkedHashMap headers
  DelegateExecution execution
  SimpleLogger logger
  String url
  String version
  LinkedHashMap auth

  Hoper(DelegateExecution execution) {
    this.execution = execution
    this.logger    = new SimpleLogger(execution)

    this.url      = execution.getVariable("hoperUrl")
    this.version  = execution.getVariable("hoperVersion") ?: '2'
    this.auth     = [
      login:    execution.getVariable("hydraUser"),
      password: execution.getVariable("hydraPassword")
    ]
    this.processor = new HTTPRestProcessor(baseUrl   : this.url,
                                           execution : execution)
  }

  def sendRequest(LinkedHashMap input, String method = 'get') {
    def token   = this.processor.sendRequest(path: "/rest/v${this.version}/login", body: [session: this.auth], supressRequestBodyLog: true, supressResponseBodyLog: true, 'get')?.session?.token
    def headers = [
      'Authorization': "Token token=\"${token}\""
    ]
    if (!input.headers) {
      input.headers = [:]
    }
    input.headers += headers
    input.path = "/rest/v${this.version}/${input.path}".toString()
    return this.processor.sendRequest(input, method)
  }
}