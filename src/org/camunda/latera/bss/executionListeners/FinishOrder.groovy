package org.camunda.latera.bss.executionListeners

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.latera.bss.connectors.HOMS

class FinishOrder implements ExecutionListener {
  void notify(DelegateExecution execution) {
    new HOMS(execution).finishOrder()
  }
}
