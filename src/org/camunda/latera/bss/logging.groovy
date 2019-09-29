package org.camunda.latera.bss.logging

import org.camunda.bpm.engine.delegate.DelegateExecution
import static org.camunda.latera.bss.utils.DateTimeUtil.DATE_TIME_FORMAT
import static org.camunda.latera.bss.utils.DateTimeUtil.format
import static org.camunda.latera.bss.utils.DateTimeUtil.local
import static org.camunda.latera.bss.utils.StringUtil.varcharToUnicode
import java.time.format.DateTimeFormatter
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Logging {

  static Logger getLogger(def entity) {
    Logger logger = null
    try {
      String processId = "${entity.getProcessDefinitionId()} (${entity.getProcessInstanceId()})".toString()
      logger = LoggerFactory.getLogger(processId)
    }
    finally {
      logger
    }
  }

  static void log(def msg, CharSequence level = "info", Logger logger = null) {
    if (logger) {
      logger."${level}"(msg)
    } else {
      println("[${level}] ${msg}")
    }
  }
}

class SimpleLogger {
  String processInstanceId
  String homsOrderCode
  DateTimeFormatter dateFormat

  SimpleLogger(DelegateExecution execution) {
    this.processInstanceId = execution.getProcessInstanceId()
    this.homsOrderCode = execution.getVariable('homsOrderCode')  ?: 'ORD-NONE'
    this.dateFormat = execution.getVariable('loggingDateFormat') ? DateTimeFormatter.ofPattern(execution.getVariable('loggingDateFormat')) : DATE_TIME_FORMAT
  }

  void log(Object message, CharSequence level = "info") {
    String timestamp = format(local(), this.dateFormat)
    String logPrefix = "${timestamp} ${processInstanceId} [${homsOrderCode}] ${level.toUpperCase().padRight(5, ' ')} - ".toString()

    message.toString().split('\n').each { def it ->
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

  void log_oracle(Object message, CharSequence level = 'info') {
    message = varcharToUnicode(message.toString())
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