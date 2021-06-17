package org.camunda.latera.bss.connectors

import org.camunda.latera.bss.http.HTTPRestProcessor
import org.camunda.latera.bss.logging.SimpleLogger
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.latera.bss.utils.Order
import org.camunda.latera.bss.utils.JSON
import org.camunda.latera.bss.utils.Base64Converter
import static org.camunda.latera.bss.utils.DateTimeUtil.local

class HOMS {
  String url
  String user
  private String token
  HTTPRestProcessor http
  DelegateExecution execution
  String homsOrderCode
  String homsOrderId
  SimpleLogger logger

  HOMS(DelegateExecution execution) {
    this.execution  = execution
    this.logger     = new SimpleLogger(this.execution)
    def ENV         = System.getenv()

    this.url        = execution.getVariable('homsUrl')  ?: ENV['HOMS_URL'] ?: 'http://homs:3000/api'
    this.user       = execution.getVariable('homsUser') ?: ENV['HOMS_USER']
    this.token      = execution.getVariable('hbwToken') ?: execution.getVariable('homsToken') ?: execution.getVariable('homsPassword') ?: ENV['HBW_TOKEN'] ?: ENV['HOMS_TOKEN'] ?: ENV['HOMS_PASSWORD']
    Boolean supress = execution.getVariable('homsOrderSupress') ?: false

    this.http = new HTTPRestProcessor(
      baseUrl   : this.url,
      user      : this.user,
      password  : this.token,
      supressRequestBodyLog  : supress,
      supressResponseBodyLog : supress,
      execution : this.execution
    )
    this.homsOrderCode = execution.getVariable('homsOrderCode')
    this.homsOrderId   = execution.getVariable('homsOrderId')
  }

  Map createOrder(CharSequence type, Map data = [:]) {
    LinkedHashMap body = [
      order: [
        order_type_code : type,
        data            : data
      ]
    ]
    logger.info('/ Creating new order ...')
    LinkedHashMap result = http.sendRequest(
      'post',
      path: 'orders',
      supressRequestBodyLog  : false,
      supressResponseBodyLog : false,
      body: body
    )
    LinkedHashMap order = result.order
    homsOrderCode = order.code
    homsOrderId   = order.id
    execution.setVariable('homsOrderCode', homsOrderCode)
    execution.setVariable('homsOrderId',   homsOrderId)
    logger.info("\\ Order created")
    return order
  }

  Map createOrder(Map data, CharSequence type) {
    return createOrder(type, data)
  }

  void startOrder() {
    def initiatorEmail = execution.getVariable('initiatorEmail')
    LinkedHashMap body = [
      order: [
        state      : 'in_progress',
        done_at    : null,
        bp_id      : execution.getProcessInstanceId(),
        bp_state   : 'in_progress',
        user_email : initiatorEmail,
      ]
    ]
    logger.info('/ Starting order ...')
    this.http.sendRequest(
      'put',
      path: "orders/${homsOrderCode}",
      body: body
    )
    logger.info('\\ Order started')
  }

  void saveOrderData() {
    LinkedHashMap body = [
      order: [
        data: Order.getDataRaw(execution)
      ]
    ]
    logger.info('/ Saving order data...')
    this.http.sendRequest(
      'put',
      path: "orders/${homsOrderCode}",
      body: body
    )
    logger.info('\\ Order data saved')
  }

  void getOrderData() {
    logger.info('/ Receiving order data...')
    def result = this.http.sendRequest(
      'get',
      path: "orders/${homsOrderCode}"
    )
    homsOrderId = result.order.id
    execution.setVariable('homsOrderId', homsOrderId)

    LinkedHashMap orderData = [:]
    result.order.data.each { k,v -> //Magic, do not touch
      if (k != 'homsOrderDataUploadedFile') {
        orderData[k] = v
      }
    }
    Order.saveData(orderData, execution)
    execution.setVariable('homsOrdDataBuffer', orderData)
    logger.info('\\ Order data received')
  }

  void finishOrder() {
    LinkedHashMap body = [
      order: [
        state:    'done',
        bp_state: 'done',
        done_at:  local()
      ]
    ]
    logger.info("/ Finishing order ...")
    this.http.sendRequest(
      'put',
      path: "orders/${homsOrderCode}",
      body: body
    )
    logger.info('\\ Order finished')
  }

  List attachFiles(List files, String prefix = '', Boolean save = true) {
    files.eachWithIndex { item, i ->
      LinkedHashMap file = [name: item.name]
      file.content = Base64Converter.to(item.content)
      files[i] = file
    }
    LinkedHashMap body = [
      files: JSON.to(files)
    ]
    logger.info("Attaching files to order ${homsOrderId}")
    List newFiles =  this.http.sendRequest(
      'post',
      path: '/widget/file_upload',
      body: body,
      supressRequestBodyLog:  true
    )
    if (save) {
      List existingFiles = Order.getFiles(prefix, execution)
      List newList = existingFiles + newFiles
      Order.setFiles(prefix, newList, execution)
    }
    return newFiles
  }

  List attachFile(Map file, String prefix = '', Boolean save = true) {
    return attachFiles([file], prefix, save)
  }

  void sendTaskEvent(String taskId, String eventName, String assignee, List<String> users, Long version) {
    LinkedHashMap body = [
      event_name: eventName,
      assignee:   assignee,
      users:      users,
      version:    version
    ]

    logger.info("/ Sending event ${eventName} with version ${version} for task ${taskId} ...")

    this.http.sendRequest(
      'put',
      path: "/widget/events/tasks/${taskId}",
      body: body
    )

    logger.info("\\ Event ${eventName} with version ${version} for task ${taskId} has been sent")
  }
}
