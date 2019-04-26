package org.camunda.latera.bss.logging

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.latera.bss.utils.DateTimeUtil
import org.camunda.latera.bss.utils.StringUtil
import java.time.format.DateTimeFormatter
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Logging {

  static Logger getLogger(entity) {
    Logger logger = null
    try {
      String processId = "${entity.getProcessDefinitionId()} (${entity.getProcessInstanceId()})"
      logger = LoggerFactory.getLogger(processId)
    }
    finally {
      logger
    }
  }

  static void log(def msg, String level = "info", Logger logger = null) {
    if (logger) {
      logger."${level}"(msg)
    } else {
      println("[${level}] ${msg}")
    }
  }
}

class SimpleLogger {
  String processInstanceID
  String homsOrderCode
  DateTimeFormatter dateFormat

  SimpleLogger(DelegateExecution execution) {
    this.processInstanceID = execution.getProcessInstanceId()
    this.homsOrderCode = execution.getVariable('homsOrderCode') ?: 'ORD-NONE'
    this.dateFormat = execution.getVariable('loggingDateFormat') ? DateTimeFormatter.ofPattern(execution.getVariable('loggingDateFormat')) : DateTimeUtil.DATE_TIME_FORMAT
  }

  void log(Object message, String level = "info") {
    String timestamp = DateTimeUtil.now().format(this.dateFormat)
    String logPrefix

    logPrefix = "${timestamp} ${processInstanceID} [${homsOrderCode}] ${level.toUpperCase().padRight(5, ' ')} - ".toString()

    message.toString().split('\n').each { it ->
      println(logPrefix + it)
    }
  }

  void debug(Object message) {
    log(message, 'debug')
  }

  void info(Object message) {
    log(message, 'info')
  }

  void warn(Object message) {
    log(message, 'warn')
  }

  void error(Object message) {
    log(message, 'error')
  }

  void log_oracle(Object message, String level = "info") {
    message = StringUtil.varcharToUnicode(message.toString())
    log(message, level)
  }

  void debug_oracle(Object message) {
    log_oracle(message, 'debug')
  }

  void info_oracle(Object message) {
    log_oracle(message, 'info')
  }

  void warn_oracle(Object message) {
    log_oracle(message, 'warn')
  }

  void error_oracle(Object message) {
    log_oracle(message, 'error')
  }
}