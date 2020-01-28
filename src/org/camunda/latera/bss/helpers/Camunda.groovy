package org.camunda.latera.bss.helpers

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.latera.bss.helpers.camunda.Task
import org.camunda.latera.bss.logging.SimpleLogger

class Camunda implements Task {
  DelegateExecution execution
  SimpleLogger logger

  Camunda(DelegateExecution execution) {
    this.execution = execution
    this.logger    = new SimpleLogger(execution)
  }
  //Other methods are imported from traits
}