package org.camunda.latera.bss.executionListeners

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.latera.bss.logging.SimpleLogger
import org.camunda.latera.bss.http.HTTPRestProcessor

class FinishOrder implements ExecutionListener {

  static void finishOrder(DelegateExecution execution) {
    def homsUrl = execution.getVariable("homsUrl")
    def homsUser = execution.getVariable("homsUser")
    def homsPassword = execution.getVariable("homsPassword")
    def homsOrderCode = execution.getVariable('homsOrderCode')

    def homsRequestObj = [
        order: [
            state:      "done",
            done_at:    String.format("%tFT%<tRZ", Calendar.getInstance(TimeZone.getTimeZone("Z"))),
            bp_state:   "done"
        ]
    ]

    def httpProcessor = new HTTPRestProcessor(baseUrl: "$homsUrl/api/", execution: execution)
    httpProcessor.httpClient.auth.basic(homsUser, homsPassword)
    httpProcessor.sendRequest('put', path: "orders/$homsOrderCode", body: homsRequestObj)
  }

  void notify(DelegateExecution execution) {

    SimpleLogger logger = new SimpleLogger(execution)

    logger.log('/ Finishing order...', "info")
    finishOrder(execution)
    logger.log('\\ Order finished', "info")
  }
}
