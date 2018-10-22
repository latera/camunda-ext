package org.camunda.latera.bss.executionListeners

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.latera.bss.logging.Logging
import org.slf4j.Logger

class EventLogging implements ExecutionListener {

  void notify(DelegateExecution execution) {

    Logger logger = Logging.getLogger(execution)
    String eventString = "Occurred ${execution.getEventName()} ${execution.getCurrentActivityId()} ${execution.getCurrentActivityName()?:''}"
    Logging.log(eventString, 'info', logger)
  }
}
