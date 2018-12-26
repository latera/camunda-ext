package org.camunda.latera.bss.executionListeners

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.latera.bss.logging.SimpleLogger
import org.camunda.latera.bss.http.HTTPRestProcessor

class SaveOrderData implements ExecutionListener {

  static void saveOrderData(DelegateExecution execution) {
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

    def httpProcessor = new HTTPRestProcessor(baseUrl: "$homsUrl/api/", execution: execution)
    httpProcessor.httpClient.auth.basic(homsUser, homsPassword)
    httpProcessor.sendRequest('put', path: "orders/$homsOrderCode", body: homsRequestObj)
  }

  void notify(DelegateExecution execution) {

    SimpleLogger logger = new SimpleLogger(execution)

    logger.log('/ Saving order data...', 'info')
    saveOrderData(execution)
    logger.log('\\ Order data saved', 'info')
  }
}
