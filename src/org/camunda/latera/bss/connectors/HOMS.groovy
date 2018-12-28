package org.camunda.latera.bss.connectors

import org.camunda.latera.bss.http.HTTPRestProcessor
import org.camunda.latera.bss.logging.SimpleLogger
import org.camunda.bpm.engine.delegate.DelegateExecution

class HOMS {
  HTTPRestProcessor processor
  DelegateExecution execution
  LinkedHashMap headers
  SimpleLogger logger

  HOMS(DelegateExecution execution) {
    this.headers   = [:]
    this.execution = execution
    this.logger    = new SimpleLogger(this.execution)

    def url       = execution.getVariable("homsUrl")
    def user      = execution.getVariable("homsUser")
    def password  = execution.getVariable("homsPassword")
    def token     = execution.getVariable("hbwToken")
    if (token) {
      this.headers = [
        'Token': token
      ]
    }
    this.processor = new HTTPRestProcessor(baseUrl:   url,
                                           user:      user,
                                           password:  password,
                                           execution: execution)
  }

  def createOrder(String type, LinkedHashMap data) {
    LinkedHashMap body = [
      order: [
        order_type_code: type,
        data: data
      ]
    ]
    this.logger.info("Creating new order")
    def result = this.processor.sendRequest(path:   '/api/orders',
                                            body:    body,
                                            headers: this.headers,
                                            'post')
    LinkedHashMap order = result.order
    this.execution.setVariable("homsOrderCode", order.code)
    this.execution.setVariable("homsOrderId",   order.id)
    return order
  }

  def attachFiles(List files) {
    Long orderId = this.execution.getVariable("homsOrderId")
    LinkedHashMap body = [
      order_id:    orderId,
      attachments: files
    ]
    this.logger.info("Attaching files to order ${orderId}")
    return this.processor.sendRequest(path:   '/api/attachments',
                                      body:    body, 
                                      headers: this.headers,
                                      'post')
  }
}