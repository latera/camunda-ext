package org.camunda.latera.bss.executionListeners

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.latera.bss.logging.Logging
import org.camunda.latera.bss.http.HTTPRestProcessor
import org.slf4j.Logger

class GetOrderData implements ExecutionListener {

  static void getOrderData(DelegateExecution execution, Logger logger) {
    def homsUrl = execution.getVariable("homsUrl")
    def homsUser = execution.getVariable("homsUser")
    def homsPassword = execution.getVariable("homsPassword")
    def homsOrderCode = execution.getVariable('homsOrderCode')

    def httpProcessor = new HTTPRestProcessor(baseUrl: "$homsUrl/api/")
    httpProcessor.httpClient.auth.basic(homsUser, homsPassword)

    def homsResp = httpProcessor.sendRequest('get', path: "orders/$homsOrderCode", logger: logger)
    def orderObj = homsResp["order"]
    def orderData = orderObj["data"].collectEntries{[it.key, it.value]}

    execution.setVariable("homsOrderId", orderObj["id"])
    for (e in orderObj["data"]) {
      execution.setVariable("homsOrderData" + e.key.capitalize(), orderObj["data"][e.key])
    }
    execution.setVariable("homsOrdDataBuffer", orderData)
  }

  void notify(DelegateExecution execution) {

    def logger = Logging.getLogger(execution)

    Logging.log('/ Receiving order data...', "info", logger)
    getOrderData(execution, logger)
    Logging.log('\\ Order data received', "info", logger)
  }
}
