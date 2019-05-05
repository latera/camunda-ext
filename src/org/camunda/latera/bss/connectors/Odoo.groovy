package org.camunda.latera.bss.connectors

import groovyx.net.http.HttpException
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.latera.bss.http.HTTPRestProcessor
import org.camunda.latera.bss.logging.SimpleLogger

import org.camunda.latera.bss.connectors.odoo.Main
import org.camunda.latera.bss.connectors.odoo.types.Lead
import org.camunda.latera.bss.connectors.odoo.types.Customer
import org.camunda.latera.bss.connectors.odoo.types.Country

class Odoo implements Main, Lead, Customer, Country {
  String url
  private String user
  private String password
  private String token
  String db
  HTTPRestProcessor http
  DelegateExecution execution
  SimpleLogger logger

  Odoo(DelegateExecution execution) {
    this.execution = execution
    this.logger    = new SimpleLogger(execution)

    this.url      = execution.getVariable('crmUrl')
    this.user     = execution.getVariable('crmUser')
    this.password = execution.getVariable('crmPassword')
    this.token    = execution.getVariable('crmToken')
    this.db       = execution.getVariable('crmDatabase') ?: 'odoo'
    this.http     = new HTTPRestProcessor(baseUrl     : url,
                                          contentType : 'application/x-www-form-urlencoded',
                                          execution   : execution)
  }

  private String authToken() {
    if (token) {
      return token.toString()
    }

    def query = [
      login    : user,
      password : password,
      db       : db
    ]

    return http.sendRequest(
        'get',
        path  : '/api/auth/token',
        query : query)?.access_token?.toString()
  }

  private LinkedHashMap authHeader() {
    return ["Access-Token": this.authToken()]
  }

  def sendRequest(LinkedHashMap input, String method = 'get') {
    if (!input.headers) {
      input.headers = [:]
    }
    input.headers += this.authHeader()
    if (input.path) {
      input.path = "/api/${input.path}"
    }
    return http.sendRequest(input, method)
  }
}
