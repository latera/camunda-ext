package org.camunda.latera.bss.logging

import org.camunda.bpm.engine.delegate.DelegateExecution
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
  String dateFormat

  SimpleLogger(DelegateExecution execution) {
    this.processInstanceID = execution.getProcessInstanceId()
    this.homsOrderCode = execution.getVariable('homsOrderCode')
    this.dateFormat = execution.getVariable('loggingDateFormat')?:"yyyy-MM-dd HH:mm:ss"
  }

  void log(Object message, String level = "info") {
    String timestamp = new Date().format(this.dateFormat)
    String logPrefix

    if (homsOrderCode) {
      logPrefix = "${timestamp} ${processInstanceID} [${homsOrderCode}] ${level.toUpperCase().padRight(5, ' ')} - ".toString()
    } else {
      logPrefix = "${timestamp} ${processInstanceID} ${level.toUpperCase().padRight(5, ' ')} - ".toString()
    }

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
}