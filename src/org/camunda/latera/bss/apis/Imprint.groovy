package org.camunda.latera.bss.apis

import org.camunda.latera.bss.http.HTTPRestProcessor
import org.camunda.latera.bss.logging.SimpleLogger
import org.camunda.latera.bss.utils.IO
import org.camunda.bpm.engine.delegate.DelegateExecution

class Imprint {
  HTTPRestProcessor processor
  LinkedHashMap headers
  DelegateExecution execution
  SimpleLogger logger

  Imprint(DelegateExecution execution) {
    this.execution = execution
    this.logger    = new SimpleLogger(execution)

    def url     = execution.getVariable("imprintUrl")
    def version = execution.getVariable("imprintVersion")
    def token   = execution.getVariable("imprintToken")
    this.headers = [
      'X_IMPRINT_API_VERSION': version,
      'X_IMPRINT_API_TOKEN':   token
    ]
    this.processor = new HTTPRestProcessor(baseUrl: url, user: user, password: password, execution: execution)
  }

  def print(LinkedHashMap json) {
    this.logger.info("Printing begin...")
    def result = this.processor.sendRequest(path: '/api/print', body: json, headers: this.headers, requestContentType: 'application/json', 'post')
    def file   = IO.getBytes(result)
    return file
  }
}