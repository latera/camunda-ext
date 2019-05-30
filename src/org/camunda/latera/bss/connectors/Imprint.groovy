package org.camunda.latera.bss.connectors

import org.camunda.latera.bss.http.HTTPRestProcessor
import org.camunda.latera.bss.logging.SimpleLogger
import org.camunda.latera.bss.utils.DateTimeUtil
import org.camunda.latera.bss.utils.Order
import org.camunda.bpm.engine.delegate.DelegateExecution

class Imprint {
  String url
  String version
  private String token
  HTTPRestProcessor http
  LinkedHashMap headers
  SimpleLogger logger
  String locale
  DelegateExecution execution

  Imprint(DelegateExecution execution) {
    this.execution  = execution
    this.logger  = new SimpleLogger(execution)
    def ENV      = System.getenv()

    this.locale  = execution.getVariable("locale") ?: 'en'
    this.url     =  ENV['IMPRINT_URL']     ?: execution.getVariable("imprintUrl")
    this.version = (ENV['IMPRINT_VERSION'] ?: execution.getVariable("imprintVersion"))?.toInteger()
    this.token   =  ENV['IMPRINT_TOKEN']   ?: execution.getVariable("imprintToken")
    def headers = [
      'X_IMPRINT_API_VERSION' : this.version,
      'X_IMPRINT_API_TOKEN'   : this.token
    ]
    this.http = new HTTPRestProcessor(
      baseUrl   : url,
      headers   : headers,
      execution : execution,
      supressResponseBodyLog : true
    )
  }

  def print(String template, LinkedHashMap data = [:]) {
    this.logger.info("Printing begin...")
    if (!data) {
      data = Order.getData(execution)
    }
    def body = [
      template : template,
      data     : [
                  now       : DateTimeUtil.format(DateTimeUtil.local()),
                  today     : DateTimeUtil.format(DateTimeUtil.local(), DateTimeUtil.SIMPLE_DATE_FORMAT),
                  todayFull : DateTimeUtil.format(DateTimeUtil.local(), DateTimeUtil.FULL_DATE_FORMAT, this.locale)
                ] + data
    ]
    def file = this.http.sendRequest(
      'post',
      path        : '/api/print',
      body        : body,
      contentType : 'application/json'
    )
    return file
  }
}