package org.camunda.latera.bss.executionListeners

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.latera.bss.connectors.HOMS

class StartOrder implements ExecutionListener {
  void notify(DelegateExecution execution) {
    def homs = new HOMS(execution)
    homs.startOrder()
  }
}
