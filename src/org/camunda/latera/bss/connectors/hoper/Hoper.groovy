package org.camunda.latera.bss.connectors

import org.camunda.latera.bss.http.HTTPRestProcessor
import org.camunda.latera.bss.logging.SimpleLogger
import org.camunda.bpm.engine.delegate.DelegateExecution

class Hoper {
  String url
  String user
  private String password
  HTTPRestProcessor http
  SimpleLogger logger

  Hoper(DelegateExecution execution) {
    this.logger = new SimpleLogger(execution)
    def ENV     = System.getenv()

    this.url      =  execution.getVariable("hoperUrl")      ?: ENV['HOPER_URL']     ?: 'http://hoper:3000'
    this.user     =  execution.getVariable("hydraUser")     ?: ENV['HYDRA_USER']
    this.password =  execution.getVariable("hydraPassword") ?: ENV['HYDRA_PASSWORD']

    this.http = new HTTPRestProcessor(
      baseUrl   : "${this.url}/rest/v2",
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
      'get',
      path : 'login',
      body : auth,
      supressRequestBodyLog  : true,
      supressResponseBodyLog : true
    )?.session?.token
  }

  private Map authHeader() {
    return ['Authorization': "Token token=\"${this.authToken()}\""]
  }

  def sendRequest(Map input, CharSequence method = 'get') {
    if (!input.headers) {
      input.headers = [:]
    }
    input.headers += this.authHeader()
    return http.sendRequest(input, method)
  }
}
