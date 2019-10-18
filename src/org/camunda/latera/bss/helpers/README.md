# Helper classes (ex. Camunda Helpers repo)
Functions that simplifies BPMN development a bit.
Uses `connectors.hid.Hydra` class to make queries and call procedures in database or `connectors.hoper.Hydra` to call REST API.

## Why do I need this?
Most of tasks of simple processes look the same at high level:
* You already have customer id, but should fetch invididual name to show in form. Or company name. Or first contract. Or its number.
* Equipment is already chosen, but you need to fetch IP address. Or MAC address. Or Telephone. Or installation address. Or fetch it from component. Or provider equipment component by bind. Or provider equipment?...
* Create invidual using data from input fields. Or company. Or contract with number. Or without.
* You are searched for equipment component by it's addresses, but you need for it's parent id. Or fetch equipment first component.
* Price plan is already selected, but you want to show form with read-only fields Price and Service name. Or some additional services?
* and so on

You may think "it will be so nice to have an instrument for simplifying work with these fetches, saves and variable values". But you already have one!

## How it works?
There are three important things here - `Order` class, __variable naming__ and __helper functions__ themselves.

### Order class
It allows to get and set variables like:
```groovy
order.customerId = 123
def customerId = order.customerId
```
instead of old one:
```groovy
execution.setVariable('homsOrderDataCustomerId', 123)
def customerId = execution.getVariable('homsOrderDataCustomerId')
```
Actually all the variables are added to execution with prefix `homsOrderData` because this is how integration between Camunda and HydraOMS works. This class allows you to skip writing this prefix every time and also it converts some types to HydraOMS compatible ones like dates -> ISO strings with dates, etc.
See `utils.Order` class documentation for more information.

### Variable naming
HydraOMS can show and use only fields with primitive types, but not the object fields.
So you cannot set:
```groovy
order.equipment = [id: 123, name: 'Terminal equipment 1', goodId: 12324134, ...]
```
You have to use different variables here:
```groovy
order.equipmentId = 123
order.equipmentName = 'Terminal equipment 1'
order.equipmentGoodId = 12324134
```

But what if you have two types of equipment in your process - provider and customer ones?
Just name your variables like:
```groovy
order.customerEquipmentId = 123
order.providerEquipmentId = 345
```

These `Customer` and `Provider` name parts (in title case) are called __prefixes__ and can be used in almost every helper function or just be empty if you don't need one. They can be any string value in Title style.

If your equipment have components just set variable names like:
```
order.customerEquipmentComponentId = 456
order.providerEquipmentComponentId = 789
```
This `Component` name part (also in title case) is called __suffix__ and also can be passed to functions or not.

Helper functions use variable names like:
```
homsOrderData{prefix}Equipment{suffix}
homsOrderData{prefix}BaseSubject{suffix}
homsOrderData{prefix}Individual{suffix}
homsOrderData{prefix}Company{suffix}
homsOrderData{prefix}Customer
homsOrderData{customerPrefix}Customer{prefix}Contract
homsOrderData{customerPrefix}Customer{prefix}Account
homsOrderData{customerPrefix}Customer{prefix}Group
homsOrderData{equipmentPrefix}Equipment{equipmentSuffix}{prefix}Address{suffix}
homsOrderData{equipmentPrefix}Equipment{equipmentSuffix}{prefix}Region{suffix}
homsOrderData{equipmentPrefix}Equipment{equipmentSuffix}{prefix}City{suffix}
homsOrderData{equipmentPrefix}Equipment{equipmentSuffix}{prefix}Street{suffix}
homsOrderData{equipmentPrefix}Equipment{equipmentSuffix}{prefix}Building{suffix}
homsOrderData{equipmentPrefix}Equipment{equipmentSuffix}{prefix}Flat{suffix}
...
homsOrderData{subjectPrefix}BaseSubject{subjectSuffix}{prefix}Address{suffix}
homsOrderData{subjectPrefix}BaseSubject{subjectSuffix}{prefix}Region{suffix}
...
homsOrderData{equipmentPrefix}Equipment{equipmentSuffix}{prefix}IP{suffix}
homsOrderData{equipmentPrefix}Equipment{equipmentSuffix}{prefix}Subnet{suffix}
homsOrderData{equipmentPrefix}Equipment{equipmentSuffix}{prefix}MAC{suffix}
homsOrderData{equipmentPrefix}Equipment{equipmentSuffix}{prefix}VLAN{suffix}
homsOrderData{equipmentPrefix}Equipment{equipmentSuffix}{prefix}Telephone{suffix}
homsOrderData{subjectPrefix}BaseSubject{subjectSuffix}{prefix}Telephone{suffix}
homsOrderData{subjectPrefix}BaseSubject{subjectSuffix}{prefix}EMail{suffix}
homsOrderData{prefix}PriceLine
homsOrderData{prefix}Service
homsOrderData{equipmentPrefix}Equipment{equipmentSuffix}{servicePrefix}Service{prefix}Subscription
...
```
* EquipmentPrefix is usually something like `Old`, `New`, `Customer`...
* EquipmentSuffix is usually something like `Component`, `NewComponent`...
* Address (any type) prefixes are usually like `Serv`, `Fixed`, `Notice`...

## Helper functions
### Arguments
Functions should be called like:
```
import org.camunda.latera.bss.helpers.Hydra as HydraHelper
def hydraHelper = new HydraHelper(execution)
hydraHelper.functionName(key: value, ...)
```
Arguments are optional and can be passed in any order. See certain function documentation for details.

These functions use only execution variables and input arguments. Return value in mst cases is *void* (fetch) or *Boolean* (create, delete, ...).

### How to use?
Imagine that you already selected or searched for the equipment and stored it in `homsOrderDataCustomerEquipmentId` variable.
For fetching its data like `goodId` or `name` you just need to call function `fetchEquipment`:
```groovy
// set somethere before
order.customerEquipmentId = 123

// and then fetch equipment data by id
hydraHelper.fetchEquipment(prefix: 'Customer')

// with saving results back to variables
assert order.customerEquipmentCode     == '1'
assert order.customerEquipmentName     == 'Terminal equipment-1'
assert order.customerEquipmentSerial   == 'W76ER7FH48397HD8'
assert order.customerEquipmentGoodName == 'Terminal equipment'
assert order.customerEquipmentGoodId   == 345
```
If some of variables which are not directly related to entity you're fetching are already set, like `homsOrderDataCustomerEquipmentGoodId` (is a equipment good data, but nut equipment data itself), they are not replaced with new values. That's because such variables are used in `create*` functions and if you made a mistake due to fetching equipment data by `id: null`, they shouldn't be nullified.

For creating equipment you just need to call function `createEquipment`:
```groovy
// set somethere before
order.customerId = 234
order.newEquipmentGoodId = 345

// and then create equipment you need
hydraHelper.createEquipment(prefix: 'New')

// with saving results back to variables
assert order.newEquipmentCreated == true // if OK
assert order.newEquipmentId == 456

assert order.newEquipmentCreated == false // if error
```

For deleting equipment you just need to call function `deleteEquipment`:
```groovy
// set somethere before
order.oldEquipmentId = 345

// and then delete equipment using its id
hydraHelper.deleteEquipment(prefix: 'Old')

// with saving results back to variables
assert order.oldEquipmentDeleted == true // if OK
assert order.oldEquipmentDeleted == false // if error
```

So simple!

## Notes
Not all of cases can be coreved by helpers - for some custom things like "fetch equipment by name with some specific prefix' it's better to use SQL queries via `connectors.HID` class.