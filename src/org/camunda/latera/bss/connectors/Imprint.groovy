package org.camunda.latera.bss.connectors

import org.camunda.latera.bss.http.HTTPRestProcessor
import org.camunda.latera.bss.logging.SimpleLogger
import org.camunda.latera.bss.utils.DateTimeUtil
import org.camunda.bpm.engine.delegate.DelegateExecution

class Imprint {
  HTTPRestProcessor processor
  LinkedHashMap headers
  DelegateExecution execution
  SimpleLogger logger
  String locale

  Imprint(DelegateExecution execution) {
    this.execution = execution
    this.logger    = new SimpleLogger(execution)

    this.locale = execution.getVariable("locale") ?: 'en'
    def url     = execution.getVariable("imprintUrl")
    def version = execution.getVariable("imprintVersion")
    def token   = execution.getVariable("imprintToken")
    def headers = [
      'X_IMPRINT_API_VERSION' : version,
      'X_IMPRINT_API_TOKEN'   : token
    ]
    this.processor = new HTTPRestProcessor(baseUrl   : url,
                                           headers   : headers,
                                           execution : execution)
  }

  def print(String template, LinkedHashMap data) {
    this.logger.info("Printing begin...")
    def body = [
      template : template,
      data     : [
                  now       : DateTimeUtil.format(DateTimeUtil.local()),
                  today     : DateTimeUtil.format(DateTimeUtil.local(), DateTimeUtil.SIMPLE_DATE_FORMAT),
                  todayFull : DateTimeUtil.format(DateTimeUtil.local(), DateTimeUtil.FULL_DATE_FORMAT, this.locale)
                ] + data
    ]
    def result = this.processor.sendRequest(path        : '/api/print',
                                            body        : body,
                                            contentType : 'application/json',
                                            'post')
    def file = result
    return file
  }
}