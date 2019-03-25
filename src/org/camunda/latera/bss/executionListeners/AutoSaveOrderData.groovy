package org.camunda.latera.bss.executionListeners

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.latera.bss.logging.SimpleLogger
import org.camunda.latera.bss.connectors.HOMS
import org.camunda.latera.bss.utils.Order

class AutoSaveOrderData implements ExecutionListener {
  void notify(DelegateExecution execution) {
    SimpleLogger logger = new SimpleLogger(execution)

    if (isSavePossible(execution)) {
      def orderData = Order.getData(execution)

      if (orderData != execution.getVariable('homsOrdDataBuffer')) {
        def homs = new HOMS(execution)
        homs.saveOrderData()
      } else {
        logger.log('Order data has not changed, save not needed', "info")
      }
    }
  }

  static private isSavePossible(DelegateExecution execution) {
    execution && execution.getVariable("homsOrderCode") && execution.getVariable("homsOrdDataBuffer")
  }
}
