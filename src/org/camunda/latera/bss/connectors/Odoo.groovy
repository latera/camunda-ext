package org.camunda.latera.bss.connectors

import groovyx.net.http.HttpException
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.latera.bss.http.HTTPRestProcessor
import org.camunda.latera.bss.logging.SimpleLogger

import org.camunda.latera.bss.connectors.odoo.Main
import org.camunda.latera.bss.connectors.odoo.Entity
import org.camunda.latera.bss.connectors.odoo.types.Lead
import org.camunda.latera.bss.connectors.odoo.types.Customer
import org.camunda.latera.bss.connectors.odoo.types.Country

class Odoo implements Main, Entity, Lead, Customer, Country {
  String url
  String user
  private String password
  private String token
  String db
  HTTPRestProcessor http
  SimpleLogger logger

  Odoo(DelegateExecution execution) {
    this.logger   = new SimpleLogger(execution)
    def ENV       = System.getenv()

    this.url      = ENV['ODOO_URL']      ?: execution.getVariable('odooUrl')
    this.user     = ENV['ODOO_USER']     ?: execution.getVariable('odooUser')
    this.password = ENV['ODOO_PASSWORD'] ?: execution.getVariable('odooPassword')
    this.token    = ENV['ODOO_TOKEN']    ?: execution.getVariable('odooToken')
    this.db       = ENV['ODOO_DB']       ?: execution.getVariable('odooDatabase') ?: 'odoo'
    this.http     = new HTTPRestProcessor(
      baseUrl     : url,
      contentType : 'application/x-www-form-urlencoded',
      execution   : execution
    )
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
        query : query,
        supressRequestBodyLog  : true,
        supressResponseBodyLog : true
    )?.access_token?.toString()
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
