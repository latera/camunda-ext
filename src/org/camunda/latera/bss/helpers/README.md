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
There are four important things here:
* `Order` class
* __variable naming__
* __prefixes__ and __suffixes__
* __helper functions__ themselves

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

Actually all the variables are added to execution with prefix `homsOrderData` because this is how integration between Camunda and HydraOMS works.

This class allows you to skip writing this prefix every time and also it converts some types to HydraOMS compatible ones like `dates -> ISO strings with dates`, etc.

See `utils.Order` class documentation for more information.

### Variable naming
HydraOMS can show and use only fields with generic types, but not the object-like fields.

So you cannot set:
```groovy
order.equipment = [
  id: 123,
  name: 'Terminal equipment 1',
  goodId: 12324134, ...
]
```

You have to use different variables here:
```groovy
order.equipmentId = 123
order.equipmentName = 'Terminal equipment 1'
order.equipmentGoodId = 12324134
```

If variables were objects, the whole structure of them would look like:
```groovy
*Equipment {
  Id: String | BigInteger
  Name: String
  Code: String
  Serial: String
  GoodName: String
  GoodId: String | BigInteger

  Created: Boolean // read-only
  Deleted: Boolean // read-only
  Deactivated: Boolean // read-only
  Unregistered: Boolean // read-only

  *ABC: String | Integer | Float | Boolean //for add param EQUIP_ADD_PARAM_ABC with types varchar, char, flag, number, ref (code value)
  *ABCId: String | BigInteger //for add param EQUIP_ADD_PARAM_ABC with type ref (id value)
  *ABCSaved: Boolean // read-only
  *ABCDeleted: Boolean // read-only

  *IP: String
  *IPId: String | BigInteger //objAddressId of created address, to use close and delete functions
  *IPMask: String | Integer //mask of parent subnet (not /32 as for all IP addresses)
  *IPGateway: String //gateway of parent subnet
  *IPCreated: Boolean // read-only
  *IPClosed: Boolean // read-only
  *IPDeleted: Boolean // read-only

  *Subnet: String
  *SubnetId: String | BigInteger //objAddressId of created address, to use close and delete functions
  *SubnetMask: String | Integer //mask of subnet
  *SubnetGateway: String //gateway of subnet
  *SubnetCreated: Boolean // read-only
  *SubnetClosed: Boolean // read-only
  *SubnetDeleted: Boolean // read-only

  *Subnet6...

  *MAC: String
  *MACId: String | BigInteger //objAddressId of created address, to use close and delete functions
  *MACCreated: Boolean // read-only
  *MACClosed: Boolean // read-only
  *MACDeleted: Boolean // read-only

  *VLAN...
  *Telephone...

  // % = IP|MAC|VLAN|Subnet|Subnet6
  // E.g. IPParentVLAN, MACParentIP
  *%Parent {
    %: String //code of parent objAddress to create bind
    %Id: String | BigInteger //parent objAddressId to create bind
  }

  // % = Serv|Actual|Resource|etc, see BIND_ADDR_TYPE_% constant names
  *%RegionId: String | BigInteger //regionId of some region
  *%RegionLevel: String //read-only level name of some region, like 'state', 'district', 'city', etc
  *%RegionLevelNum: String | Integer //read-only level number of some region, 0 for 'state', etc.
  //building fields
  *%Home: String
  *%Construct: String
  *%Corpus: String
  *%Ownership: String
  *%Building: String
  //regions and their types
  *%Street: String
  *%StreetType: String  //ref code like REGION_TYPE_Avenue
  *%City: String
  *%CityType: String //ref code like REGION_TYPE_City
  *%District: String
  *%DistrictType: String //ref code like REGION_TYPE_District
  ...
  *%State: String
  *%StateType: String //ref code like REGION_TYPE_State
  //address fields
  *%RawAddress: String
  *%Address: String //read-only, calculated address
  *%AddressId: String | BigInteger //objAddressId of created address, to use close and delete functions
  *%AddressCreated: Boolean // read-only
  *%AddressClosed: Boolean // read-only
  *%AddressDeleted: Boolean // read-only

  *Component {
    //same fields as for Equipment type
  }

  *Bind {
    Id
    Created: Boolean // read-only
    Deleted: Boolean // read-only

    Equipment... {
      Component...
    }
  }
}

*Customer {
  Id: String | BigInteger
  Code: String
  FirmId: String | BigInteger
  StateId: String | BigInteger

  Created: Boolean // read-only
  Disabled: Boolean // read-only

  //Main group
  GroupId: String | BigInteger
  GroupName: String //read-only

  //Additional groups
  *GroupId: String | BigInteger
  *GroupName: String //read-only
  *GroupBindCreated: Boolean // read-only
  *GroupBindDeleted: Boolean // read-only

  //NetService = PayPal|ATOL|etc
  //E.g. PayPalLogin, ATOLAccessAdded
  *(NetService)Id: String | BigInteger //id of network service subscription for (NetService) service
  *(NetService)Login: String
  *(NetService)Password: String //generated automatically
  *(NetService)AccessAdded: Boolean // read-only

  //Application = SelfCare|etc
  //E.g. SelfCareLogin, SelfCarePassword
  *(Application)Id: String | BigInteger //id of network service subscription for (Application) application
  *(Application)Login: String
  *(Application)Password: String //generated automatically
  *(Application)AccessAdded: Boolean // read-only

  *ABC: String | Integer | Float | Boolean //for add param SUBJ_VAL_ABC with types varchar, char, flag, number, ref (code value)
  *ABCId: String | BigInteger //for add param SUBJ_VAL_ABC with type ref (id value)
  *ABCSaved: Boolean // read-only
  *ABCDeleted: Boolean // read-only

  *Account {
    Id: String | BigInteger
    Number: String
    Currency: String //currency ref code
    CurrencyId: String | BigInteger //currency ref id
    Created: Boolean // read-only

    BalanceSum: BigDecimal
    FreeSum: BigDecimal
  }

  *Files: List[Map] //JSON with list of files metadata: {url, name, origin_name, eal_name, upload_time, end_point, bucket}
  *FilesAttached: Boolean // read-only
}

*BaseSubject {
  Id: String | BigInteger
  Code: String //read-only, calculated
  Name: String //read-only, calculated
  INN: String | BigInteger
  KPP: String | BigInteger
  OPFId: String | BigInteger

  Type: String //base subject ref code, read-only
  IsCompany: Boolean //true if Type == SUBJ_TYPE_Company, read-only

  *ABC: String | Integer | Float | Boolean //for add param SUBJ_VAL_ABC with types varchar, char, flag, number, ref (code value)
  *ABCId: String | BigInteger //for add param SUBJ_VAL_ABC with type ref (id value)
  *ABCSaved: Boolean // read-only
  *ABCDeleted: Boolean // read-only

  *Telephone: String
  *TelephoneId: String | BigInteger //subjAddressId of created address, to use delete function
  *TelephoneCreated: Boolean // read-only
  *TelephoneDeleted: Boolean // read-only

  *EMail: String
  *EMailId: String | BigInteger //subjAddressId of created address, to use delete function
  *EMailCreated: Boolean // read-only
  *EMailDeleted: Boolean // read-only

  // % = Jur|Post|Fixed|Actual|Notice|etc, see BIND_ADDR_TYPE_% constant names
  *%RegionId: String | BigInteger //regionId of some region
  *%RegionLevel: String //read-only level name of some region, like 'state', 'district', 'city', etc
  *%RegionLevelNum: String | Integer //read-only level number of some region, 0 for 'state', etc.
  //building fields
  *%Home: String
  *%Construct: String
  *%Corpus: String
  *%Ownership: String
  *%Building: String
  //regions and their types
  *%Street: String
  *%StreetType: String  //ref code like REGION_TYPE_Avenue
  *%City: String
  *%CityType: String //ref code like REGION_TYPE_City
  *%District: String
  *%DistrictType: String //ref code like REGION_TYPE_District
  ...
  *%State: String
  *%StateType: String //ref code like REGION_TYPE_State
  //address fields
  *%RawAddress: String
  *%Address: String //read-only, calculated address
  *%AddressId: String | BigInteger //objAddressId of created address, to use close and delete functions
  *%AddressCreated: Boolean // read-only
  *%AddressClosed: Boolean // read-only
  *%AddressDeleted: Boolean // read-only
}

*Individual {
  FirstName: String
  SecondName: String
  LastName: String
  Gender: String //ref code
  BirthDate: DatetimeISO
  BirthPlace: String

  IdentType: String | BigInteger //ref id for identifier types
  IdentSerial: String | BigInteger
  IdentNumber: String | BigInteger
  IdentIssuedAuthor: String
  IdentIssuedDate: DatetimeISO
  IdentIssuedDepartment: String
}

*Company {
  Name: String
  Code: String
  LastName: String
  OGRN: String
}

Reseller {
  Code: String
  Name: String

  *Customer {
    //same fields as for Customer type
  }

  *ABC: String | Integer | Float | Boolean //for add param SUBJ_VAL_ABC with types varchar, char, flag, number, ref (code value)
  *ABCId: String | BigInteger //for add param SUBJ_VAL_ABC with type ref (id value)
  *ABCSaved: Boolean // read-only
  *ABCDeleted: Boolean // read-only
}

*BaseContract {
  Id: String| BigInteger
}

*Contract {
  Id: String| BigInteger
  Number: String | BigInteger
  Name: String | BigInteger //read only

  Created: Boolean // read-only

  DissolveDate: DatetimeISO //read-only
  Dissolved: Boolean // read-only

  *ABC: String | Integer | Float | Boolean //for add param DOC_VAL_ABC with types varchar, char, flag, number, ref (code value)
  *ABCId: String | BigInteger //for add param DOC_VAL_ABC with type ref (id value)
  *ABCSaved: Boolean // read-only
  *ABCDeleted: Boolean // read-only
}

*ContractApp {
  //same fields as for Contract type
}

*AddAgreement {
  //same fields as for Contract type
}

*PriceLineId //prefix here is same as for Service type
*Service {
  Id: String | BigInteger
  Name: String //read-only
  Price: BigDecimal //read-only
  PriceWoTax: BigDecimal //read-only

  *ABC: String | Integer | Float | Boolean //for add param GOOD_VAL_ABC with types varchar, char, flag, number, ref (code value)
  *ABCId: String | BigInteger //for add param GOOD_VAL_ABC with type ref (id value)
  *ABCSaved: Boolean // read-only
  *ABCDeleted: Boolean // read-only
}

*(''|Par|Prev)Subscription {
  Id: String | BigInteger
  BeginDate: DatetimeISO // read-only
  EndDate: DatetimeISO // read-only

  IsClosed: Boolean //read-only

  Created: Boolean // read-only
  Deleted: Boolean // read-only

  CloseDate: DatetimeISO //read-only
  Closed: Boolean // read-only
}
```
But actually variables can be only generic type, so we make this structure flatten.

So id of equipment:
```groovy
Equipment {
  Id
}
```
became
`homsOrderDataEquipmentId`.

Subnet code of component of equipment:
```groovy
Equipment {
  Component {
    Subnet
  }
}
```
became
`homsOrderDataEquipmentComponentSubnet`.

If some equipment component is connected to another equipment component, and we need IP gateway of this second component:
```groovy
Equipment {
  Component {
    Bind {
      Equipment {
        Component {
          IPGateway
        }
      }
    }
  }
}
```
variable name will be:
`homsOrderDataEquipmentComponentBindEquipmentComponentIPGateway`.

Usually it's easier to read variable names from the end to the beginning.

### Prefixes and suffixes
Imagine what you have two types of equipment in your process - provider and customer ones. How to differ them in your code?

You can simply name your variables like:
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

Suffixes are usually something like `Old`, `New`, `Customer`...

Prefixes is usually something like `Component`, `NewComponent`...

Symbol `*` was used for both of them in scheme above (and `%` for addrTypes and bindAddrTypes).



So if you need to name id of customer equipment:
```groovy
CustomerEquipment {
  Id
}
```
it will be
`homsOrderDataCustomerEquipmentId`.

Code of old subnet assigned to component of customer equipment:
```groovy
CustomerEquipment {
  Component {
    OldSubnet
  }
}
```
became
`homsOrderDataCustomerEquipmentComponentOldSubnet`.

If customer equipment component is connected to provider equipment (BRAS) component, and we need IP gateway of this component:
```groovy
CustomerEquipment {
  Component {
    BRASBind {
      Equipment {
        Component {
          IPGateway
        }
      }
    }
  }
}
```
variable name will be:
`homsOrderDataCustomerEquipmentComponentBRASBindEquipmentComponentIPGateway`.

## Helper functions
### Arguments
Functions should be called like:
```groovy
import org.camunda.latera.bss.helpers.Hydra as HydraHelper
def hydraHelper = new HydraHelper(execution)
hydraHelper.functionName(key: value, ...)
```
Arguments are optional and can be passed in any order. See certain function documentation for details.

These functions use only execution variables and input arguments. Return value in mst cases is *void* (fetch) or *Boolean* (create, delete, ...).

### How to use?
Imagine that you already selected or searched for the equipment and stored it in `homsOrderDataCustomerEquipmentId` variable.
For fetching its data like `GoodId` or `Name` you just need to call function `fetchEquipment`:
```groovy
// set somethere before it of equipment
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
If some of variables (which are not directly related to entity you're fetching) are already set, like `homsOrderDataCustomerEquipmentGoodId` (is a equipment good data, but nut equipment data itself), they are not replaced with new values. That's because such variables are used in `create*` functions and if you made a mistake due to fetching equipment data by `id: null`, they shouldn't be nullified.

For creating equipment you just need to call function `createEquipment`:
```groovy
// set somethere before customer id and new equipment id
order.customerId = 234
order.newEquipmentGoodId = 345

// and then create new equipment you need
hydraHelper.createEquipment(prefix: 'New')

// with saving results back to variables
assert order.newEquipmentCreated == true // if OK
assert order.newEquipmentId      == 456

assert order.newEquipmentCreated == false // if error
```

Or if some non-child relation has its own prefix, like customer is `Reseller`:
```groovy
// set somethere before reseller customer id and new equipment id
order.resellerCustomerId = 234
order.newEquipmentGoodId = 345

// and then create new equipment you need
hydraHelper.createEquipment(prefix: 'New', customerPrefix: 'Reseller')

// with saving results back to variables
assert order.newEquipmentCreated == true // if OK
assert order.newEquipmentId == 456

assert order.newEquipmentCreated == false // if error
```

Or hard mode with many relations:
```groovy
import static org.camunda.latera.bss.utils.DateTimeUtil.local
//set price line id
hydraHelper.fetchService(prefix: 'Tariff')

// service data is saved back to  variables
assert order.tariffServiceId         = 567
assert order.tariffServiceName       = 'Awesome tariff'
assert order.tariffServicePrice      = 10.0
assert order.tariffServicePriceWoTax = 9.0

// set somethere before which customer
order.resellerCustomerId        = 234
order.resellerCustomerAccountId = 345
order.someEquipmentId           = 456

// and then create new subscription you need
hydraHelper.createServiceSubscription(
  prefix          : 'New',
  customerPrefix  : 'Reseller',
  equipmentPrefix : 'Some',
  servicePrefix   : 'Tariff',
  beginDate       : local(),
//endDate         : null,
  payDay          : 1
)

// or for one-off subscription
/*
hydraHelper.createOneOffServiceSubscription(
  prefix          : 'New',
  customerPrefix  : 'Reseller',
  equipmentPrefix : 'Some',
  servicePrefix   : 'Tariff',
  beginDate       : local() // = endDate
)
*/

// with saving results back to variables
assert order.newSubscriptionCreated == true // if OK
assert order.newSubscriptionId      == 678

assert order.newSubscriptionCreated == false // if error
```

For deleting equipment you just need to call function `deleteEquipment`:
```groovy
// set somethere before equipment id
order.oldEquipmentId = 345

// and then delete equipment using its id
hydraHelper.deleteEquipment(prefix: 'Old')

// with saving results back to variables
assert order.oldEquipmentDeleted == true // if OK
assert order.oldEquipmentDeleted == false // if error
```

For closing subscription you just need to call function `closeServiceSubscription`:
```groovy
// set somethere before equipment id
order.oldSubscriptionId = 345

// and then delete equipment using its id
hydraHelper.closeServiceSubscription(prefix: 'Old')

// with saving results back to variables
assert order.oldSubscriptionClosed == true // if OK
assert order.oldSubscriptionCloseDate == LocalDateTime('2019-12-01T00:00:00')

assert order.oldSubscriptionClosed == false // if error
```

So simple!

## Notes
Not all of cases can be coreved by helpers - for some custom things like "fetch equipment by name with some specific prefix' it's better to use SQL queries via `connectors.HID` class.