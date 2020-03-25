package org.camunda.latera.bss.connectors

import org.camunda.latera.bss.http.HTTPRestProcessor
import org.camunda.latera.bss.logging.SimpleLogger
import org.camunda.latera.bss.utils.Base64Converter
import org.camunda.bpm.engine.delegate.DelegateExecution

class Hoper {
  String url
  String user
  private String password
  Integer version
  HTTPRestProcessor http
  SimpleLogger logger

  Hoper(DelegateExecution execution) {
    this.logger = new SimpleLogger(execution)
    def ENV     = System.getenv()

    this.url      =  execution.getVariable("hoperUrl")      ?: ENV['HOPER_URL']     ?: 'http://hoper:3000'
    this.version  = (execution.getVariable("hoperVersion")  ?: ENV['HOPER_VERSION'] ?: 2)?.toInteger()
    this.user     =  execution.getVariable("hydraUser")     ?: ENV['HYDRA_USER']
    this.password =  execution.getVariable("hydraPassword") ?: ENV['HYDRA_PASSWORD']

    this.http = new HTTPRestProcessor(
      baseUrl   : "${this.url}/rest/v${this.version}",
      execution : execution
    )
  }

  private String authToken() {
    LinkedHashMap auth = [
      session: [
        login    : this.user,
        password : this.password
      ]
    ]
    return http.sendRequest(
      'post',
      path : 'login',
      body : auth,
      supressRequestBodyLog  : true,
      supressResponseBodyLog : true
    )?.session?.token
  }

  private String authBasic() {
    String auth = "${this.user}:${this.password}"
    return Base64Converter.to(auth)
  }

  private Map authHeader() {
    if (this.version == 1) {
      return ['Authorization': "Basic ${this.authBasic()}"]
    }
    if (this.version == 2) {
      return ['Authorization': "Token token=\"${this.authToken()}\""]
    }
    return []
  }

  def sendRequest(Map input, CharSequence method = 'get') {
    if (!input.headers) {
      input.headers = [:]
    }
    input.headers += this.authHeader()
    return http.sendRequest(input, method)
  }
}