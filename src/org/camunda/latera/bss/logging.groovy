package org.camunda.latera.bss.logging

import org.camunda.bpm.engine.delegate.DelegateExecution
import static org.camunda.latera.bss.utils.DateTimeUtil.*
import static org.camunda.latera.bss.utils.StringUtil.varcharToUnicode
import static org.camunda.latera.bss.utils.Constants.*
import java.time.format.DateTimeFormatter

class SimpleLogger {
  String processInstanceId = null
  String homsOrderCode = 'ORD-NONE'
  DateTimeFormatter dateFormat = DATE_TIME_FORMAT
  Integer currentLevel

  SimpleLogger(DelegateExecution execution) {
    def ENV = System.getenv()

    this.processInstanceId =  execution.getProcessInstanceId()
    this.homsOrderCode     =  execution.getVariable('homsOrderCode') ?: 'ORD-NONE'
    this.dateFormat        = (execution.getVariable('logDateFormat') || execution.getVariable('loggingDateFormat')) ? DateTimeFormatter.ofPattern(execution.getVariable('logDateFormat') ?: execution.getVariable('loggingDateFormat')) : DATE_TIME_FORMAT
    this.currentLevel      =  levelToInt(execution.getVariable('logLevel') ?: execution.getVariable('loggingLevel') ?: ENV['BPM_LOG_LEVEL'] ?: 'info')
  }

  SimpleLogger(CharSequence level = null) {
    def ENV = System.getenv()

    this.currentLevel = levelToInt(level ?: ENV['BPM_LOG_LEVEL'] ?: 'info')
  }

  static Integer levelToInt(CharSequence level) {
    String lvl = level.toString().toLowerCase()
    switch (lvl) {
      case 'debug':
        return DEBUG
      case 'info':
        return INFO
      case ['warn', 'warning']:
        return WARNING
      case ['err', 'error']:
        return ERROR
      case ['crit', 'critical']:
        return CRITICAL
      default:
        return DEFAULT
    }
  }

  void log(Object message, CharSequence level = 'info') {
    if (levelToInt(level) >= this.currentLevel) {
      String timestamp = format(local(), this.dateFormat)
      String logPrefix = "${timestamp} ${processInstanceId} [${homsOrderCode}] ${level.toUpperCase().padRight(5, ' ')} - ".toString()

      message.toString().split('\n').each { def it ->
        println(logPrefix + it)
      }
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
