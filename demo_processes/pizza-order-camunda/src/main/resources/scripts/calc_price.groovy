def Double price = 0

switch (execution.getVariable("homsOrderDataPizzaType")) {
  case "Margherita":
    price += 2.00
    break
  case "Cheese":
    price += 2.50
    break
  case "Salami":
    price += 3.00
    break
}

if (execution.getVariable("homsOrderDataIngredientCheese")) {
  price += 0.15
}
if (execution.getVariable("homsOrderDataIngredientSalami")) {
  price += 0.25
}
if (execution.getVariable("homsOrderDataIngredientPepper")) {
  price += 0.10
}
if (execution.getVariable("homsOrderDataIngredientMushrooms")) {
  price += 0.20
}
if (execution.getVariable("homsOrderDataIngredientVegetables")) {
  price += 0.20
}
if (execution.getVariable("homsOrderDataIngredientOlives")) {
  price += 0.25
}

price = price.round(2)

execution.setVariable("homsOrderDataPizzaPrice", price)
