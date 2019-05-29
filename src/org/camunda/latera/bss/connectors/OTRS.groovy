package org.camunda.latera.bss.connectors

import groovyx.net.http.HttpException
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.latera.bss.http.HTTPRestProcessor
import org.camunda.latera.bss.logging.SimpleLogger

import org.camunda.latera.bss.connectors.otrs.Main
import org.camunda.latera.bss.connectors.otrs.Entity
import org.camunda.latera.bss.connectors.otrs.types.Ticket

class OTRS implements Main, Entity, Ticket {
  String url
  String user
  private String password
  HTTPRestProcessor http
  SimpleLogger logger

  OTRS(DelegateExecution execution) {
    this.logger   = new SimpleLogger(execution)
    def ENV       = System.getenv()

    this.url      = ENV['OTRS_URL']      ?: execution.getVariable('otrsUrl')
    this.user     = ENV['OTRS_USER']     ?: execution.getVariable('otrsUser')
    this.password = ENV['OTRS_PASSWORD'] ?: execution.getVariable('otrsPassword')
    this.http     = new HTTPRestProcessor(
      baseUrl   : url,
      execution : execution,
    )
  }

  def sendRequest(LinkedHashMap input, String method = 'get') {
    if (input.path) {
      input.path = "nph-genericinterface.pl/Webservice/HOMS/${input.path}?UserLogin=${user}&Password=${password}"
    }
    return http.sendRequest(input, method)
  }
}
