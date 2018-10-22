package org.camunda.latera.bss.executionListeners

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.latera.bss.logging.Logging
import org.camunda.latera.bss.http.HTTPRestProcessor
import org.slf4j.Logger

class AutoSaveOrderData implements ExecutionListener {

  void notify(DelegateExecution execution) {

    def logger = Logging.getLogger(execution)
    if (isSavePossible(execution)) {
      def orderData = getOrderData(execution)

      if (orderData != execution.getVariable('homsOrdDataBuffer')) {
        Logging.log('/ Saving order data...', "info", logger)
        saveOrderData(orderData, execution, logger)
        execution.setVariable('homsOrdDataBuffer', orderData)
        Logging.log('\\ Order data saved', "info", logger)
      } else {
        Logging.log('Order data has not changed, save not needed', "info", logger)
      }
    }
  }

  static private isSavePossible(DelegateExecution execution) {
    execution && execution.getVariable("homsOrderCode") && execution.getVariable("homsOrdDataBuffer")
  }

  def private getOrderData(DelegateExecution execution) {
    def orderData = [:]

    for (e in execution.getVariables()) {
      if (!e.key.startsWith('homsOrderData')) continue

      def dataKey = e.key.replaceFirst(/^homsOrderData/, "")
      dataKey = (dataKey.getAt(0).toLowerCase() + dataKey.substring(1)).toString()
      orderData[dataKey] = e.value
    }

    orderData
  }

  static private saveOrderData(orderData, execution, logger) {
    def homsUrl = execution.getVariable("homsUrl")
    def homsUser = execution.getVariable("homsUser")
    def homsPassword = execution.getVariable("homsPassword")
    def homsOrderCode = execution.getVariable('homsOrderCode')

    def homsRequestObj = [
        order: [
            data: orderData
        ]
    ]

    def httpProcessor = new HTTPRestProcessor(baseUrl: "$homsUrl/api/")
    httpProcessor.httpClient.auth.basic(homsUser, homsPassword)
    httpProcessor.sendRequest('put', path: "orders/$homsOrderCode", body: homsRequestObj, logger: logger)
  }
}
