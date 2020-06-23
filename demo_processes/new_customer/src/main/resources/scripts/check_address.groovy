import org.camunda.latera.bss.utils.Order

def order = new Order(execution)

if (order.customerEntrance.toString().isInteger()) {
  order.isAddressIsAvailable = true
} else {
  order.isAddressIsAvailable = false
}
