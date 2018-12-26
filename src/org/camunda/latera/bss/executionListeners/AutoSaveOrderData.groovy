package org.camunda.latera.bss.executionListeners

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.latera.bss.logging.SimpleLogger
import org.camunda.latera.bss.http.HTTPRestProcessor

class AutoSaveOrderData implements ExecutionListener {

  void notify(DelegateExecution execution) {

    SimpleLogger logger = new SimpleLogger(execution)
    if (isSavePossible(execution)) {
      def orderData = getOrderData(execution)

      if (orderData != execution.getVariable('homsOrdDataBuffer')) {
        logger.log('/ Saving order data...', "info")
        saveOrderData(orderData, execution)
        execution.setVariable('homsOrdDataBuffer', orderData)
        logger.log('\\ Order data saved', "info")
      } else {
        logger.log('Order data has not changed, save not needed', "info")
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

  static private saveOrderData(orderData, execution) {
    def homsUrl = execution.getVariable("homsUrl")
    def homsUser = execution.getVariable("homsUser")
    def homsPassword = execution.getVariable("homsPassword")
    def homsOrderCode = execution.getVariable('homsOrderCode')

    def homsRequestObj = [
        order: [
            data: orderData
        ]
    ]

    def httpProcessor = new HTTPRestProcessor(baseUrl: "$homsUrl/api/", execution: execution)
    httpProcessor.httpClient.auth.basic(homsUser, homsPassword)
    httpProcessor.sendRequest('put', path: "orders/$homsOrderCode", body: homsRequestObj)
  }
}
