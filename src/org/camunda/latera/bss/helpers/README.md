# Helper classes (ex. Camunda Helpers repo)
Functions that simplifies BPMN development a bit.
Uses connectors.hid.Hydra to make queries and call procedures in database.

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
There are two important things here - __variable naming__ and __helper functions__ themselves.

### Variable naming
HydraOMS can show only fields with primitive types, but not the object fields.
So you cannot set:
```groovy
execution.setVariable('homsOrderDataEquipment', [id: 123, name: 'Terminal equipment 1', goodId: 12324134, ...])
```
You have to use different variables here:
```groovy
execution.setVariable('homsOrderDataEquipmentId',     123)
execution.setVariable('homsOrderDataEquipmentName',  'Terminal equipment 1')
execution.setVariable('homsOrderDataEquipmentGoodId', 12324134)
```

But what if you have two types of equipment in your process - provider and customer ones?
Just name your variables like:
```groovy
execution.setVariable('homsOrderDataCustomerEquipmentId', 123)
execution.setVariable('homsOrderDataProviderEquipmentId', 345)
```

These `Customer` and `Provider` name parts are called __prefixes__ and can be used in almost every helper function or just be empty if you don't need one. They can be any string value in Title style.

If your equipment have components just set variable names like:
```groovy
execution.setVariable('homsOrderDataCustomerEquipmentComponentId', 456)
execution.setVariable('homsOrderDataProviderEquipmentComponentId', 789)
```
This `Component` name part is called __suffix__ and also can be passed to functions or not.

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
EquipmentPrefix is usually something like `Old`, `New`, `Customer`...
EquipmentSuffix is usually something like `Component`, `NewComponent`...
Address (any type) prefixes are usually like `Serv`, `Fixed`, `Notice`...

## Helper functions
### Arguments
Functions should be called like:
```
import static org.camunda.latera.bss.helpers.Hydra.*
functionName(execution, hydra: hydra, key: value, ...)
```
First argument (positional) is always `execution` because function should get and save execution variables.
Second argument (optional) is an instance of `Hydra` class because functions in most cases should fetch or save something in Hydra database. If ommited, temp class instance will be created, but due to session initialization on every instanse creation it's better to create instance just once and then pass to all helper functions in script.
Other arguments (optional) are named arguments and can be passed in any order. See certain function documentation for details.

These functions use only execution variables and input arguments. *There is no return value.*

### How to use?
Imagine that you already selected or searched for the equipment and stored it in `homsOrderDataCustomerEquipmentId` variable.
For fetching its data like `goodId` or `name` you just need to call function `fetchEquipment`:
```groovy
import static org.camunda.latera.bss.helpers.Hydra.fetchEquipment
// set somethere before
execution.setVariable('homsOrderDataCustomerEquipmentId', 123)

// and then fetch equipment data by id
fetchEquipment(execution, prefix: 'Customer')
```
It will use value of first variable, fetch data from Hydra and save to `homsOrderDataCustomerEquipment*` fields.

For creating equipment you just need to call function `createEquipment`:
```groovy
import static org.camunda.latera.bss.helpers.Hydra.createEquipment
// set somethere before
execution.setVariable('homsOrderDataCustomerId', 234)
execution.setVariable('homsOrderDataNewEquipmentGoodId', 345)

// and then create equipment you need
createEquipment(execution, prefix: 'New')
```
It will pass value of first two variables to equipment creation function, then save equipment id to `homsOrderDataNewEquipmentId` fields and also set variable `homsOrderDataNewEquipmentCreated` to __true__ if everything was OK.

For deleting equipment you just need to call function `deleteEquipment`:
```groovy
import static org.camunda.latera.bss.helpers.Hydra.deleteEquipment
// set somethere before
execution.setVariable('homsOrderDataOldEquipmentId', 456)

// and then delete equipment using its id
deleteEquipment(execution, prefix: 'Old')
```
It will pass value of first variable to equipment deletion function and then set variable `homsOrderDataOldEquipmentDeleted` to __true__ if everything was OK.

So simple!

## Notes
Not all of cases can be coreved by helpers - for some custom things like "fetch equipment by name with some specific prefix' it's better to use SQL queries via connetors.HID class.