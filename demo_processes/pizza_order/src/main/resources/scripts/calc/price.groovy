import org.camunda.latera.bss.utils.Order

//Order variables
def order = new Order(execution)

Double price = 0

switch (order.pizzaType) {
  case 'Margherita':
    price += 2.00
    break
  case 'Cheese':
    price += 2.50
    break
  case 'Salami':
    price += 3.00
    break
}

if (order.ingredientCheese) {
  price += 0.15
}
if (order.ingredientSalami) {
  price += 0.25
}
if (order.ingredientPepper) {
  price += 0.10
}
if (order.ingredientMushrooms) {
  price += 0.20
}
if (order.ingredientVegetables) {
  price += 0.20
}
if (order.ingredientOlives) {
  price += 0.25
}

price = price.round(2)
order.pizzaPrice = price
