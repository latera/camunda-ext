package org.camunda.latera.bss.executionListeners

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.latera.bss.logging.Logging
import org.camunda.latera.bss.http.HTTPRestProcessor
import org.slf4j.Logger

class StartOrder implements ExecutionListener {

  static void startOrder(DelegateExecution execution, Logger logger) {
    def homsUrl = execution.getVariable("homsUrl")
    def homsUser = execution.getVariable("homsUser")
    def homsPassword = execution.getVariable("homsPassword")
    def homsOrderCode = execution.getVariable('homsOrderCode')
    def initiatorEmail = execution.getVariable("initiatorEmail")

    def homsRequestObj = [
        order: [
            state:      "in_progress",
            done_at:    null,
            bp_id:      execution.getProcessInstanceId(),
            bp_state:   "in_progress",
            user_email: initiatorEmail,
        ]
    ]

    def httpProcessor = new HTTPRestProcessor(baseUrl: "$homsUrl/api/")
    httpProcessor.httpClient.auth.basic(homsUser, homsPassword)
    httpProcessor.sendRequest('put', path: "orders/$homsOrderCode", body: homsRequestObj, logger: logger)
  }

  void notify(DelegateExecution execution) {

    def logger = Logging.getLogger(execution)

    Logging.log('/ Starting order...', "info", logger)
    startOrder(execution, logger)
    Logging.log('\\ Order started', "info", logger)
  }
}
