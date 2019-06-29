package org.camunda.latera.bss.executionListeners

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.latera.bss.logging.SimpleLogger

class EventLogging implements ExecutionListener {
  void notify(DelegateExecution execution) {
    String eventString = "Occurred ${execution.getEventName()} ${execution.getCurrentActivityId()} ${execution.getCurrentActivityName()?:''}".toString()
    new SimpleLogger(execution).info(eventString)
  }
}
