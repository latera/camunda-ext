package org.camunda.latera.bss.connectors

import org.camunda.latera.bss.http.HTTPRestProcessor
import org.camunda.latera.bss.logging.SimpleLogger
import org.camunda.latera.bss.utils.Base64Converter
import org.camunda.bpm.engine.delegate.DelegateExecution

class Hoper {
  String url
  private String user
  private String password
  String version
  HTTPRestProcessor http
  DelegateExecution execution
  SimpleLogger logger

  Hoper(DelegateExecution execution) {
    this.execution = execution
    this.logger    = new SimpleLogger(execution)

    this.url      = execution.getVariable("hoperUrl")
    this.version  = execution.getVariable("hoperVersion") ?: '2'
    this.user     = execution.getVariable("hydraUser")
    this.password = execution.getVariable("hydraPassword")
    this.http     = new HTTPRestProcessor(baseUrl   : this.url,
                                          execution : execution)
  }

  private def authToken() {
    def auth = [
      session: [
        login    : user,
        password : password
      ]
    ]
    return http.sendRequest('get',
                            path : "/rest/v${this.version}/login",
                            body : auth,
                            supressRequestBodyLog  : true,
                            supressResponseBodyLog : true)?.session?.token
  }

  private def authBasic() {
    def auth = "${user}:${password}"
    return Base64Converter.to(auth)
  }

  private def authHeader() {
    if (version.toString() == '1') {
      return ['Authorization': "Basic ${authBasic}"]
    }
    if (version.toString() == '2') {
      return ['Authorization': "Token token=\"${authToken}\""]
    }
    return []
  }

  def sendRequest(LinkedHashMap input, String method = 'get') {
    if (!input.headers) {
      input.headers = [:]
    }
    input.headers += authHeader
    input.path = "/rest/v${version}/${input.path}".toString()
    return http.sendRequest(input, method)
  }
}