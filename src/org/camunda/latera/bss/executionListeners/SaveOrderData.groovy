package org.camunda.latera.bss.executionListeners

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.latera.bss.logging.Logging
import org.camunda.latera.bss.http.HTTPRestProcessor
import org.slf4j.Logger

class SaveOrderData implements ExecutionListener {

  static void saveOrderData(DelegateExecution execution, Logger logger) {
    def homsUrl = execution.getVariable("homsUrl")
    def homsUser = execution.getVariable("homsUser")
    def homsPassword = execution.getVariable("homsPassword")
    def homsOrderCode = execution.getVariable('homsOrderCode')

    def homsRequestObj = [
        order: [
            data: [:]
        ]
    ]

    for (e in execution.getVariables()) {
      if (!e.key.startsWith('homsOrderData')) continue

      def dataKey = e.key.replaceFirst(/^homsOrderData/, "")
      dataKey = (dataKey.getAt(0).toLowerCase() + dataKey.substring(1)).toString()
      homsRequestObj.order.data[dataKey] = e.value
    }

    def httpProcessor = new HTTPRestProcessor(baseUrl: "$homsUrl/api/")
    httpProcessor.httpClient.auth.basic(homsUser, homsPassword)
    httpProcessor.sendRequest('put', path: "orders/$homsOrderCode", body: homsRequestObj, logger: logger)
  }

  void notify(DelegateExecution execution) {

    def logger = Logging.getLogger(execution)

    Logging.log('/ Saving order data...', logger: logger)
    saveOrderData(execution, logger)
    Logging.log('\\ Order data saved', logger: logger)
  }
}
