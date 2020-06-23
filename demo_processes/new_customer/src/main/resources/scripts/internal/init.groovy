import org.camunda.latera.bss.utils.StringUtil
import org.camunda.latera.bss.connectors.HOMS

def orderCode = execution.getVariable('homsOrderCode')

if (StringUtil.isEmpty(orderCode)) {
  def homs = new HOMS(execution)
  homs.createOrder('new_customer')
}

execution.setBusinessKey(execution.getVariable('homsOrderCode'))
