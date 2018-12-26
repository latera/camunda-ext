package org.camunda.latera.bss.executionListeners

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.latera.bss.logging.SimpleLogger
import org.camunda.latera.bss.http.HTTPRestProcessor

class StartOrder implements ExecutionListener {

  static void startOrder(DelegateExecution execution) {
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

    def httpProcessor = new HTTPRestProcessor(baseUrl: "$homsUrl/api/", execution: execution)
    httpProcessor.httpClient.auth.basic(homsUser, homsPassword)
    httpProcessor.sendRequest('put', path: "orders/$homsOrderCode", body: homsRequestObj)
  }

  void notify(DelegateExecution execution) {

    SimpleLogger logger = new SimpleLogger(execution)

    logger.log('/ Starting order...', "info")
    startOrder(execution)
    logger.log('\\ Order started', "info")
  }
}
