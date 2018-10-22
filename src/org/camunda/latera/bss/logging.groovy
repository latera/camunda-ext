package org.camunda.latera.bss.logging

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
