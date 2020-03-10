v1.4.2 [unreleased]
-------------------
### Features
-   [#25](https://github.com/latera/camunda-ext/pull/25) Add Planado APIv2 connector
-   [#38](https://github.com/latera/camunda-ext/pull/38) Add method for job updating into PlanadoV2 class
-   [#33](https://github.com/latera/camunda-ext/pull/33) Allow to pass goodValueId into hid.Hydra#getGoodAddParamsBy
-   [#24](https://github.com/latera/camunda-ext/pull/24) Add methods for files to document attaching
-   [#39](https://github.com/latera/camunda-ext/pull/39) Add custom fields in create and update methods in PlanadoV2 class
-   [#39](https://github.com/latera/camunda-ext/pull/39) Add html and Content-Disposition to file attachment to mail sender
-   [#32](https://github.com/latera/camunda-ext/pull/32) Allow to pass only specific fields into hid.Hydra#updateSubscription method

### Bugfixes
-   [#27](https://github.com/latera/camunda-ext/pull/27) Remove redundant get methods from hid.Hydra class
-   [#28](https://github.com/latera/camunda-ext/pull/28) Fix return types of HID class methods
-   [#30](https://github.com/latera/camunda-ext/pull/30) Fix wrong Self-Care app id passing into method calls
-   [#26](https://github.com/latera/camunda-ext/pull/26) Fix passing appCode into hid.Hydra#mainInit method
-   [#34](https://github.com/latera/camunda-ext/pull/34) Search settlement accounts for base subjects, not personal ones
-   [#41](https://github.com/latera/camunda-ext/pull/41) Use http-builder-ng-okhttp with PATCH requests support
-   [#42](https://github.com/latera/camunda-ext/pull/42) Fix MailSender usage without auth
-   [#29](https://github.com/latera/camunda-ext/pull/29) Fix calling changePassword in net serv access put methods
-   [#47](https://github.com/latera/camunda-ext/pull/47) Fix hid.Hydra#tagify method
-   [#2239604](https://github.com/latera/camunda-ext/commit/2239604) Fix hid.Hydra#mergeParams method
-   [#44](https://github.com/latera/camunda-ext/pull/44) Add subjTypeId to hid.Hydra#prepareSubjAddParam method

v1.4.1 [2020-02-14]
-------------------
### Features
-   [#4](https://github.com/latera/camunda-ext/pull/4) Allow partial updates in hid.Hydra#update* methods
-   [#7](https://github.com/latera/camunda-ext/pull/7) Add raw arg to Order constructor
-   [#9](https://github.com/latera/camunda-ext/pull/9) Notify HOMS about task events
-   [#10](https://github.com/latera/camunda-ext/pull/10) Send candidate users & assignee to HOMS with task event
-   [#3](https://github.com/latera/camunda-ext/pull/3) Add methods to tag entities into hid.Hydra class
-   [#20](https://github.com/latera/camunda-ext/pull/20) Add newMessage method to MailSender class
-   [#3200317](https://github.com/latera/camunda-ext/commit/3200317) Allow to pass constant id with non-Integer type into getConstantCode method

### Refactoring
-   [#8](https://github.com/latera/camunda-ext/pull/8) Prettify runCommand,  Add constants, use constants instead of magic numbers in logger, Update docs

### Bugfixes
-   [#d0b5409](https://github.com/latera/camunda-ext/commit/d0b5409) Fix stack overflow with recursive getProperty call in Order class
-   [#adbcdfc](https://github.com/latera/camunda-ext/commit/adbcdfc) Fix hid.Hydra#createCustomerAccount method
-   [#94b1996](https://github.com/latera/camunda-ext/commit/94b1996) Fix hid.Hydra#createAddAgreement method
-   [#9b4ed0b](https://github.com/latera/camunda-ext/commit/9b4ed0b) Fix hid.Hydra#putObjAddress method
-   [#1a2834b](https://github.com/latera/camunda-ext/commit/1a2834b) Fix getFreeIPAddresses and getFreeIPv6Addresses method
-   [#77ddad0](https://github.com/latera/camunda-ext/commit/77ddad0) Fix helpers.Hydra#createAddress method parent addresses handling
-   [#9737e0e](https://github.com/latera/camunda-ext/commit/9737e0e) Fix helpers.Hydra#addCustomerGroupBind method
-   [#32c7f99](https://github.com/latera/camunda-ext/commit/32c7f99) Don't call customer equipment create procedure for provider equipment
-   [#989ec5c](https://github.com/latera/camunda-ext/commit/989ec5c) Fix n_subj_address_id field name in hid.Hydra
-   [#b14ab35](https://github.com/latera/camunda-ext/commit/b14ab35) Fix add params saving in Hydra helper
-   [#f5042f3](https://github.com/latera/camunda-ext/commit/f5042f3) Fix hid.Hydra#getSubnetAddressesByVLAN method with code input
-   [#85127dc](https://github.com/latera/camunda-ext/commit/85127dc) Fix hid.Hydra#addAdjustment method
-   [#2b22ae7](https://github.com/latera/camunda-ext/commit/2b22ae7) Fix hoper.Hydra#getAddParamValue method
-   [#d58ab24](https://github.com/latera/camunda-ext/commit/d58ab24) Fix hid.Hydra#createEquipment method
-   [#19c0746](https://github.com/latera/camunda-ext/commit/19c0746) Fix helpers.Hydra#fetchSubscription method
-   [#12](https://github.com/latera/camunda-ext/pull/12) Fix helpers.Hydra#parseRegion method
-   [#207b8cc](https://github.com/latera/camunda-ext/commit/207b8cc) Fix exception while getting constant with spaces in code
-   [#7a30801](https://github.com/latera/camunda-ext/commit/7a30801) Fix names of methods getConstantByCode and getConstantCode
-   [#19](https://github.com/latera/camunda-ext/pull/19) Fix MailSender class methods return values
-   [#21](https://github.com/latera/camunda-ext/pull/21) Fix HID#queryDatabase return value codepage for List output
-   [#23](https://github.com/latera/camunda-ext/pull/23) Fix SMTP connection auto close after sending e-mail

v1.4 [2020-01-13]
-------------------
### Breaking changes
-   [#2](https://github.com/latera/camunda-ext/pull/2) Remove deprecated Logging class
-   [#5](https://github.com/latera/camunda-ext/pull/5) Get rid of put methods

v1.3 [2020-01-07]
-------------------
### Breaking changes
-   [#05e0108](https://github.com/latera/camunda-ext/commit/05e0108) Change hydra_account field to `hydra_customer_id` in _Odoo_ class
-   [#d4a4a88](https://github.com/latera/camunda-ext/commit/d4a4a88) Improve _Region_ and _Address_ class of _hid.Hydra_
-   [#860706d](https://github.com/latera/camunda-ext/commit/860706d) Add private modifier to internal only methods
-   [#6a8e117](https://github.com/latera/camunda-ext/commit/6a8e117) Fix _CSV_ generation and parsing issue
-   [#44fa4cb](https://github.com/latera/camunda-ext/commit/44fa4cb) Improve _CSV_ parsing
-   [#189c94b](https://github.com/latera/camunda-ext/commit/189c94b) Improve _CSV_ boolean coercion
-   [#3eb6018](https://github.com/latera/camunda-ext/commit/3eb6018) Change return value of `getAt` method of _CSV_ class
-   [#ac181f6](https://github.com/latera/camunda-ext/commit/ac181f6) Allow to update values with plus method of _CSV_ class
-   [#834ef9d](https://github.com/latera/camunda-ext/commit/834ef9d) Improve `get/setProperty` type coerse of _CSV_ class
-   [#20e2150](https://github.com/latera/camunda-ext/commit/20e2150) Force NVL `homsOrderData*List` variable values Until CONSULT-3350 will be fixed
-   [#6ae938f](https://github.com/latera/camunda-ext/commit/6ae938f) Merge **camunda-helpers** with **camunda-ext**
-   [#fdb5237](https://github.com/latera/camunda-ext/commit/fdb5237) Convert numeric values to _BigInteger_ of _BigDecimal_ in `Order` class
-   [#b47f0f2](https://github.com/latera/camunda-ext/commit/b47f0f2) Use `objectAddressId` instead of `addressId` in helpers.Hydra address methods
-   [#49aca5d](https://github.com/latera/camunda-ext/commit/49aca5d) Set `force: false` as default value for add params deletion behaviour
-   [#2b7e372](https://github.com/latera/camunda-ext/commit/2b7e372) Change default `addrType` from _Serv_ to _Actual_
-   [#28abfa0](https://github.com/latera/camunda-ext/commit/28abfa0) Coalesce `'null'`, `''` variable values to `null` in `Order#getValue` method
-   [#08b1dd1](https://github.com/latera/camunda-ext/commit/08b1dd1) Fix `StringUtil.isEmpty` function for `false` and `0` values

### Features
-   [#ddcbe81](https://github.com/latera/camunda-ext/commit/ddcbe81) Explicitly set Java 8 target
-   [#844fe16](https://github.com/latera/camunda-ext/commit/844fe16) Pass day, month, year and ISO datetime to Imprint Also add proper Formatter to DateTimeUtil
-   [#d0d978a](https://github.com/latera/camunda-ext/commit/d0d978a) Add forceIsEmpty and forseNotEmpty into StringUtil class
-   [#4de3cf9](https://github.com/latera/camunda-ext/commit/4de3cf9) Add mergeNotNull to _MapUtil_ class
-   [#a279edf](https://github.com/latera/camunda-ext/commit/a279edf) Add `upperCase` and `lowerCase` to _ListUtil_ class
-   [#2e0a7e0](https://github.com/latera/camunda-ext/commit/2e0a7e0) Add prefix argument to file methods of _Order_ class
-   [#c5c0a2b](https://github.com/latera/camunda-ext/commit/c5c0a2b) Add Message class to _hid.Hydra_
-   [#4dfc532](https://github.com/latera/camunda-ext/commit/4dfc532) Improve SQL generator Separate SQL generator (`prepareTableQuery`) and executor (`getTableData`)
-   [#9bff882](https://github.com/latera/camunda-ext/commit/9bff882) Get provider, recipient, etc data for document
-   [#07bffbd](https://github.com/latera/camunda-ext/commit/07bffbd) Add `getOpfCode` method to _Ref_ class
-   [#71e1951](https://github.com/latera/camunda-ext/commit/71e1951) Add named args support to `getAvailableServices` method
-   [#0bb7285](https://github.com/latera/camunda-ext/commit/0bb7285) Add `buildingType` to `getRegionTree` method
-   [#9e9319f](https://github.com/latera/camunda-ext/commit/9e9319f) Add merge method to _MapUtil_
-   [#767a637](https://github.com/latera/camunda-ext/commit/767a637) Add `homsOrderCode` to data pass to Imprint
-   [#237cdb5](https://github.com/latera/camunda-ext/commit/237cdb5) Add constructor of _CSV_ class for CharSequence and List input
-   [#838883d](https://github.com/latera/camunda-ext/commit/838883d) Convert _CSV_ class to `List[Map]` in `JSON.to` method
-   [#be8e7bd](https://github.com/latera/camunda-ext/commit/be8e7bd) Improve _CSV_ class documentation
-   [#b4bd707](https://github.com/latera/camunda-ext/commit/b4bd707) Parse first of `List[List]` as header in _CSV_ constructor
-   [#c3c49bc](https://github.com/latera/camunda-ext/commit/c3c49bc) Allow to pass _String_ to `isExistsWhere` and `deleteWhere` methods of _CSV_ class
-   [#92b3f46](https://github.com/latera/camunda-ext/commit/92b3f46) Fix `CSV#parseHeader` method for Map input
-   [#55492ef](https://github.com/latera/camunda-ext/commit/55492ef) Add `internal.Version` class for checking Hydra and other platform versions
-   [#ea302db](https://github.com/latera/camunda-ext/commit/ea302db) Add `#toIntStrict`, `#isIntegerStrict` to _Numeric_ class
-   [#8d37785](https://github.com/latera/camunda-ext/commit/8d37785) Add version fetching to _hid.Hydra_ class
-   [#9ab662b](https://github.com/latera/camunda-ext/commit/9ab662b) Allow to pass maps to non-id args of _Hydra_ class methods
-   [#a9abeeb](https://github.com/latera/camunda-ext/commit/a9abeeb) Add Param trait to _hid.Hydra_ class
-   [#6455711](https://github.com/latera/camunda-ext/commit/6455711) Add support of 5.1.2 resources like assigning free IPv6 subnets
-   [#206c0cf](https://github.com/latera/camunda-ext/commit/206c0cf) Improve add params type handling in _hid.Hydra_ class
-   [#4033c3b](https://github.com/latera/camunda-ext/commit/4033c3b) Add helper functions for entities add params
-   [#a9d7caa](https://github.com/latera/camunda-ext/commit/a9d7caa) Add `parObjAddressId` field into _hid.Hydra_ and `helpers.Hydra` classes
-   [#5bfcd65](https://github.com/latera/camunda-ext/commit/5bfcd65) Add vlanId arg to `hid.Hydra#getFreeIPAddresses` and `#getFreeSubnetAddresses`
-   [#5bfcd65](https://github.com/latera/camunda-ext/commit/5bfcd65) Add `hid.Hydra#getSubnetByVlan` method
-   [#ca7a889](https://github.com/latera/camunda-ext/commit/ca7a889) Limit `hid.Hydra#getFree*Addresses` results
-   [#3e359eb](https://github.com/latera/camunda-ext/commit/3e359eb) Add `get*Mask` and `get*Gateway` methods into hid.Hydra and helpers.Hydra classes
-   [#e9e887e](https://github.com/latera/camunda-ext/commit/e9e887e) Add timer task methods into `helpers.Camunda` class
-   [#c8f543b](https://github.com/latera/camunda-ext/commit/c8f543b) Allow to set logging level for process instance, change logging level with third-party system responses from info to debug
-   [#7a10638](https://github.com/latera/camunda-ext/commit/7a10638) Add `firstNotNull` and `firstNv`l methods to `utils.ListUtil` class
-   [#bb0ca98](https://github.com/latera/camunda-ext/commit/bb0ca98) Improve constructor default args params
-   [#9852bef](https://github.com/latera/camunda-ext/commit/9852bef) Save bind id to `Customer(NetService)Id` variable in `helpers.Hydra`
-   [#724b32d](https://github.com/latera/camunda-ext/commit/724b32d) Add `isPublic` arg to `getFreeIP/Subnet` methods
-   [#c82ed5a](https://github.com/latera/camunda-ext/commit/c82ed5a) Add _SimpleLogger_ constructor without DelegateExecution input
-   [#4614440](https://github.com/latera/camunda-ext/commit/4614440) Add _MailSender_ additional security options
-   [#82e620a](https://github.com/latera/camunda-ext/commit/82e620a) Allow to use another file upload field names
-   [#d3f94c3](https://github.com/latera/camunda-ext/commit/d3f94c3) Hide file content in `hoper.Hydra#createSubjectFile` method
-   [#3e75edb](https://github.com/latera/camunda-ext/commit/3e75edb) Allow multiple subnets assigned to VLAN in `hid.Hydra#getFree*Address` methods
-   [#42dead6](https://github.com/latera/camunda-ext/commit/42dead6) Add order arg to `hid.Hydra#get*By` methods
-   [#1b30836](https://github.com/latera/camunda-ext/commit/1b30836) Fetch ticket dynamic fields in `OTRS#getTicket` method
-   [#83abbaf](https://github.com/latera/camunda-ext/commit/83abbaf) Allow to skip arguments for `mainInit` in _hid.Hydra_ class
-   [#94dea67](https://github.com/latera/camunda-ext/commit/94dea67) Allow to pass both `vlanId` and `subnetAddressId` into `getFreeIPAddress` method
-   [#ddfc292](https://github.com/latera/camunda-ext/commit/ddfc292) Allow to pass `typeId` instead `refTypeId` into `hid.Hydra#getRefsBy`
-   [#bc63653](https://github.com/latera/camunda-ext/commit/bc63653) Allow to pass custom roles while creating conract/app/add agreement
-   [#0faa846](https://github.com/latera/camunda-ext/commit/0faa846) Allow to delete network service subscriptions
-   [#80ef386](https://github.com/latera/camunda-ext/commit/80ef386) Add `getPersonPrivateBy` method into _hid.Hydra_
-   [#12dba8d](https://github.com/latera/camunda-ext/commit/12dba8d) Hide files base64 content in _OTRS_ class methods

### Bugfixes
-   [#14a989a](https://github.com/latera/camunda-ext/commit/14a989a) Fix `queryFirstMap` and `queryFirstList` in hid class
-   [#7792d88](https://github.com/latera/camunda-ext/commit/7792d88) Fix `add*AddParam` return value
-   [#6acfcfc](https://github.com/latera/camunda-ext/commit/6acfcfc) Fix `closeObjAddress` method arg names
-   [#14d8afd](https://github.com/latera/camunda-ext/commit/14d8afd) Fix `mergeParams` method
-   [#7c99353](https://github.com/latera/camunda-ext/commit/7c99353) Fix 'en' locale id too often fetching from DB
-   [#c22478e](https://github.com/latera/camunda-ext/commit/c22478e) Fix 'ru' locale id too often fetching from DB
-   [#803638b](https://github.com/latera/camunda-ext/commit/803638b) Fix subquery usage detect in _HID_ class
-   [#c9b7133](https://github.com/latera/camunda-ext/commit/c9b7133) Fix `getRefsBy` search by `string2` field
-   [#74ff93a](https://github.com/latera/camunda-ext/commit/74ff93a) Fix trait private static classes compile error
-   [#9f09816](https://github.com/latera/camunda-ext/commit/9f09816) Fix `parseLines` items coerse
-   [#6c8c229](https://github.com/latera/camunda-ext/commit/6c8c229) Fix `skipLines` behaviour - skip lines only in constructor
-   [#e9048d5](https://github.com/latera/camunda-ext/commit/e9048d5) Fix `CSV#plus` operator overload with _List_ input
-   [#a143508](https://github.com/latera/camunda-ext/commit/a143508) Fix `withHeader` value pass into _CSV_ constructor
-   [#f58d33e](https://github.com/latera/camunda-ext/commit/f58d33e) Fix `isExists` method of _CSV_ class
-   [#f016367](https://github.com/latera/camunda-ext/commit/f016367) Fix issues detected by SonarQube
-   [#989b3f3](https://github.com/latera/camunda-ext/commit/989b3f3) Fix `hid.Hydra#updateDocument` method
-   [#f7be912](https://github.com/latera/camunda-ext/commit/f7be912) Fix `Minio#getFile` method
-   [#c0603be](https://github.com/latera/camunda-ext/commit/c0603be) Fix `helpers.Hydra#addNetServiceAccess` method
-   [#ceeba22](https://github.com/latera/camunda-ext/commit/ceeba22) Fix `hoper.Hydra#prepareParams` method
-   [#1e192e6](https://github.com/latera/camunda-ext/commit/1e192e6) Fix `helpers.Camunda` method calls
-   [#454d7ab](https://github.com/latera/camunda-ext/commit/454d7ab) Fix creating binds between two main equipment in _Equipment_ helper
-   [#4f7576d](https://github.com/latera/camunda-ext/commit/4f7576d) Truncate birth datetime to day in _hid.Hydra_ class
-   [#6ade771](https://github.com/latera/camunda-ext/commit/6ade771) Fix `hid.Hydra#mergeParams` for `[in:]` input
-   [#75abb19](https://github.com/latera/camunda-ext/commit/75abb19) Fix _hid.Hydra_ datatype names
-   [#3d224a5](https://github.com/latera/camunda-ext/commit/3d224a5) Convert varchars into strings in _HID_ and _hid.Hydra_ classes
-   [#08e91d8](https://github.com/latera/camunda-ext/commit/08e91d8) Fix errors in _hid.Hydra_ and _helpers.Hydra_ address methods
-   [#0f72f6b](https://github.com/latera/camunda-ext/commit/0f72f6b) Fix `parObjAddressId` misspell
-   [#b4a6be5](https://github.com/latera/camunda-ext/commit/b4a6be5) Fix `Mail#attachFile` method
-   [#e464f26](https://github.com/latera/camunda-ext/commit/e464f26) Fix missing prefix in `homsOrderData*FileList` setter
-   [#171420e](https://github.com/latera/camunda-ext/commit/171420e) Fix _Boolean_ coalesce to `'Y'/'N'` in _CSV_ class
-   [#48bf3b1](https://github.com/latera/camunda-ext/commit/48bf3b1) Coerse fields to _String_ in _Planado_ connector
-   [#ca47123](https://github.com/latera/camunda-ext/commit/ca47123) Fix _hoper.Hydra_ class private methods
-   [#21e56db](https://github.com/latera/camunda-ext/commit/21e56db) Fix _odoo_ class private methods
-   [#3a44e04](https://github.com/latera/camunda-ext/commit/3a44e04) Don't pass `firmId` in `hid.Hydra#addAdjustment` method for version older than 5.1.2
-   [#cc94dea](https://github.com/latera/camunda-ext/commit/cc94dea) Fix _hid.Hydra.Document_ trait methods
-   [#1f7acb1](https://github.com/latera/camunda-ext/commit/1f7acb1) Fix updating equipment in h`id.Hydra#putEquipment` method
-   [#bf80dc2](https://github.com/latera/camunda-ext/commit/bf80dc2) Catch errors in `Imprint#print` method
-   [#1fee796](https://github.com/latera/camunda-ext/commit/1fee796) Fix person add params

v1.2 [2019-08-25]
---------------------

### Breaking changes
-   [#98c2610](https://github.com/latera/camunda-ext/commit/98c2610) Use only subjectId for getSubject at hid.Hydra
-   [#f84d1f7](https://github.com/latera/camunda-ext/commit/f84d1f7) Use only docId in getDocument at hid.Hydra
-   [#cdea4af](https://github.com/latera/camunda-ext/commit/cdea4af) Rename get\*Addresses to get\*AddressesBy in hid.Hydra class
-   [#9479085](https://github.com/latera/camunda-ext/commit/9479085) Sync equipment and subject add param methods in hid.Hydra
-   [#3e8c3cb](https://github.com/latera/camunda-ext/commit/3e8c3cb) Use environment variables to store integrations credentials
-   [#bd61a2c](https://github.com/latera/camunda-ext/commit/bd61a2c) Move subject add param value type detect to function at hid.Hydra
-   [#b77eb43](https://github.com/latera/camunda-ext/commit/b77eb43) Remove good tags field from hid.Hydra
-   [#6e5fe86](https://github.com/latera/camunda-ext/commit/6e5fe86) Use HOMS_TOKEN instead of HOMS_PASSWORD in HOMS class
-   [#487cdb7](https://github.com/latera/camunda-ext/commit/487cdb7) Use SIMPLE_DATE_FORMAT for Date class format Instead of SIMPLE_DATE_TIME_FORMAT
-   [#50497cd](https://github.com/latera/camunda-ext/commit/50497cd) Limit SELECT results for getEnityBy methods in hid.Hydra

### Features
-   [#7b65aa1](https://github.com/latera/camunda-ext/commit/7b65aa1) Add snakeCase and Map keys handlers to StringUtil class
-   [#f974d24](https://github.com/latera/camunda-ext/commit/f974d24) Add Odoo connector class
-   [#e5493e9](https://github.com/latera/camunda-ext/commit/e5493e9) Add support of v1 to Hoper class
-   [#1f40b31](https://github.com/latera/camunda-ext/commit/1f40b31) Add Person methods to Hoper class
-   [#f0fab2d](https://github.com/latera/camunda-ext/commit/f0fab2d) Allow to pass null fields value to Hoper and Odoo methods
-   [#def0331](https://github.com/latera/camunda-ext/commit/def0331) Add getEntities method to hoper.Entity class
-   [#d4233f8](https://github.com/latera/camunda-ext/commit/d4233f8) Support pagination in Hoper.Entity class
-   [#70804a5](https://github.com/latera/camunda-ext/commit/70804a5) Add getPersons method to hoper.Hydra class
-   [#2fcc6a0](https://github.com/latera/camunda-ext/commit/2fcc6a0) Add company methods to hoper.Hydra class
-   [#f0bccc0](https://github.com/latera/camunda-ext/commit/f0bccc0) Add addresses methods to hoper.Hydra class
-   [#918be40](https://github.com/latera/camunda-ext/commit/918be40) Add customer methods to hoper.Hydra class
-   [#396ec2c](https://github.com/latera/camunda-ext/commit/396ec2c) Add account methods to hoper.Hydra class
-   [#25506bf](https://github.com/latera/camunda-ext/commit/25506bf) Add contract methods to hoper.Hydra class
-   [#09867b8](https://github.com/latera/camunda-ext/commit/09867b8) Add equipment methods to hoper.Hydra class
-   [#9e96219](https://github.com/latera/camunda-ext/commit/9e96219) Add subscription methods to hoper.Hydra class
-   [#d6f9f9c](https://github.com/latera/camunda-ext/commit/d6f9f9c) Add overdraft methods to hid.Hydra class
-   [#e8f0554](https://github.com/latera/camunda-ext/commit/e8f0554) Add refreshContractTree method to hid.Hydra class
-   [#9dfbfff](https://github.com/latera/camunda-ext/commit/9dfbfff) Add processCustomer method to hid.Hydra
-   [#893c659](https://github.com/latera/camunda-ext/commit/893c659) Add aliases for methods to hid.Hydra
-   [#955047b](https://github.com/latera/camunda-ext/commit/955047b) Add iso method to DateTimeUtils class
-   [#a5ce97b](https://github.com/latera/camunda-ext/commit/a5ce97b) Set current firmId in hoper.Hydra methods
-   [#b16f515](https://github.com/latera/camunda-ext/commit/b16f515) Add some wrapped put methods into hoper.Hydra
-   [#3d6eb4c](https://github.com/latera/camunda-ext/commit/3d6eb4c) Allow to use multiple add params for subject at hid.Hydra
-   [#5bfc89a](https://github.com/latera/camunda-ext/commit/5bfc89a) Add deleteSubjectAddParam into hid.Hydra
-   [#702ad63](https://github.com/latera/camunda-ext/commit/702ad63) Add good additional params methods into hid.Hydra
-   [#115366f](https://github.com/latera/camunda-ext/commit/115366f) Add document add param methods into hid.Hydra
-   [#69293ae](https://github.com/latera/camunda-ext/commit/69293ae) Add get refs methods into hid.Hydra
-   [#79dfc74](https://github.com/latera/camunda-ext/commit/79dfc74) Add getAccountsBy method into hid.Hydra
-   [#ea8f197](https://github.com/latera/camunda-ext/commit/ea8f197) Allow to use named args for account metods in hid.Hydra
-   [#05c0550](https://github.com/latera/camunda-ext/commit/05c0550) Allow to pass GStringImpl to getTable methods in hid.Hydra
-   [#4da8100](https://github.com/latera/camunda-ext/commit/4da8100) Add contract app and add agreement methods to hid.Hydra
-   [#1232857](https://github.com/latera/camunda-ext/commit/1232857) Allow passing GStringImpl to hid.execute
-   [#aeed9d1](https://github.com/latera/camunda-ext/commit/aeed9d1) Allow to pass named args to update methods in Odoo
-   [#05fd797](https://github.com/latera/camunda-ext/commit/05fd797) Add null escaping to Planado class methods
-   [#bbc4212](https://github.com/latera/camunda-ext/commit/bbc4212) Add serv scheme methods to hid.Hydra
-   [#3f1cc56](https://github.com/latera/camunda-ext/commit/3f1cc56) Add OTRS connector For OTRS v6
-   [#6901d2f](https://github.com/latera/camunda-ext/commit/6901d2f) Allow to pass object id to net services methods in hid.Hydra
-   [#de6af59](https://github.com/latera/camunda-ext/commit/de6af59) Add generateRandomString to StringUtil class
-   [#7238f55](https://github.com/latera/camunda-ext/commit/7238f55) Add priorityId to otrs.Ticket class
-   [#379362a](https://github.com/latera/camunda-ext/commit/379362a) Allow to not pass data to print form in Imprint.print Pass order data by default
-   [#81e1070](https://github.com/latera/camunda-ext/commit/81e1070) Add stream cast functions to IO class
-   [#a48612a](https://github.com/latera/camunda-ext/commit/a48612a) Add Minio connector class
-   [#d17b229](https://github.com/latera/camunda-ext/commit/d17b229) Allow to use Groovy native named args in HOMS.createOrder
-   [#628ba77](https://github.com/latera/camunda-ext/commit/628ba77) Add methods for receiving actual charge logs and account periodic sums into hid.Hydra
-   [#80d116b](https://github.com/latera/camunda-ext/commit/80d116b) Add methods for managing subject comments into hid.Hydra
-   [#f9bca96](https://github.com/latera/camunda-ext/commit/f9bca96) Add isInteger, isFloat, isNumber to Numeric class
-   [#719c86a](https://github.com/latera/camunda-ext/commit/719c86a) Add MapUtil and ListUtil classes with useful methods Like parse, nvl, isList, isMap
-   [#29bb45d](https://github.com/latera/camunda-ext/commit/29bb45d) Add CSV util class with useful methods
-   [#7ea912c](https://github.com/latera/camunda-ext/commit/7ea912c) Add useful static and non-static methods to Order class
-   [#a93ddf4](https://github.com/latera/camunda-ext/commit/a93ddf4) Allow to pass list as 'in' or 'not in' values in hid.Hydra
-   [#032016d](https://github.com/latera/camunda-ext/commit/032016d) Add support of LocalDate into DateTimeUtil
-   [#2e5bf3c](https://github.com/latera/camunda-ext/commit/2e5bf3c) Add doc subject methods into hid.Hydra Also allow to fetch documents by member and manager roles
-   [#a5837ea](https://github.com/latera/camunda-ext/commit/a5837ea) Add putDocument method into hid.Hydra
-   [#c2ae664](https://github.com/latera/camunda-ext/commit/c2ae664) Add invoice content get methods into hid.Hydra
-   [#c456ab8](https://github.com/latera/camunda-ext/commit/c456ab8) Add bill documents and content methods into hid.Hydra
-   [#1c7fd3f](https://github.com/latera/camunda-ext/commit/1c7fd3f) Move hoper file methods into separate class
-   [#e1d6042](https://github.com/latera/camunda-ext/commit/e1d6042) Allow to create order in HOMS with no data
-   [#7fed977](https://github.com/latera/camunda-ext/commit/7fed977) Use functions for cache in ref and getTableColumns in hid class
-   [#61034f0](https://github.com/latera/camunda-ext/commit/61034f0) Improve DateTimeUtil methods
-   [#ce8d885](https://github.com/latera/camunda-ext/commit/ce8d885) Return password from addCustomerNetServiceAccess and addCustomerAppAccess in hid.Hydra
-   [#c347496](https://github.com/latera/camunda-ext/commit/c347496) Allow to use native named args for add*AddParam functions in hid.Hydra
-   [#0236303](https://github.com/latera/camunda-ext/commit/0236303) Add functions for managing doc binds into hid.Hydra
-   [#3484eb7](https://github.com/latera/camunda-ext/commit/3484eb7) Transparently convert date types to ISO string and backwards in Order class
-   [#1be3bef](https://github.com/latera/camunda-ext/commit/1be3bef) Improve type checking
-   [#432357d](https://github.com/latera/camunda-ext/commit/432357d) Add joinNonEmpty method to StringUtil
-   [#b8b21d9](https://github.com/latera/camunda-ext/commit/b8b21d9) Add deepCamelizeKeys and deepSnakeCaseKeys methods into MapUtil class
-   [#6d6d1b8](https://github.com/latera/camunda-ext/commit/6d6d1b8) Add Dadata connectors
-   [#6df2424](https://github.com/latera/camunda-ext/commit/6df2424) Add GoogleMaps connector
-   [#b067bca](https://github.com/latera/camunda-ext/commit/b067bca) Move connectors to their dirs
-   [#fac820f](https://github.com/latera/camunda-ext/commit/fac820f) Pass resellerId to Hydra REST API
-   [#fb24b85](https://github.com/latera/camunda-ext/commit/fb24b85) Add method for updating quick search into hid.Hydra

### Bugfixes
-   [#0dbb535](https://github.com/latera/camunda-ext/commit/0dbb535) Fix runCommand quotes usage in Console class
-   [#06518c1](https://github.com/latera/camunda-ext/commit/06518c1) Change Planado methods return values
-   [#79c55e0](https://github.com/latera/camunda-ext/commit/79c55e0) Use format string with TZ only for ZonedDateTime
-   [#35907c0](https://github.com/latera/camunda-ext/commit/35907c0) Fix overdraft methods missing default param
-   [#044b388](https://github.com/latera/camunda-ext/commit/044b388) Fix get refs method in hid.Hydra
-   [#4939dc1](https://github.com/latera/camunda-ext/commit/4939dc1) Fix get document and add params methods in hid.Hydra
-   [#7252d7c](https://github.com/latera/camunda-ext/commit/7252d7c) Use correct workflow for add agreement and contract in hid.Hydra
-   [#ebf6238](https://github.com/latera/camunda-ext/commit/ebf6238) Fix contract app adn add agreement creation in hid.Hydra
-   [#1852a7e](https://github.com/latera/camunda-ext/commit/1852a7e) Fix SMTP port cast to Integer in Mail class
-   [#4b055f5](https://github.com/latera/camunda-ext/commit/4b055f5) Explicit convert byte[] to String in http logging
-   [#e616064](https://github.com/latera/camunda-ext/commit/e616064) Fix Planado createUser/Company return value
-   [#0796d8d](https://github.com/latera/camunda-ext/commit/0796d8d) Fix good add param types in hid.Hydra
-   [#8181071](https://github.com/latera/camunda-ext/commit/8181071) Add workaround to [HttpBuilderNG issue|https://github.com/http-builder-ng/http-builder-ng/issues/210]
-   [#a0f11dd](https://github.com/latera/camunda-ext/commit/a0f11dd) Fix get contract app/add agreement in hid.Hydra
-   [#badecdc](https://github.com/latera/camunda-ext/commit/badecdc) Fix refreshContractsTree method in Hydra v5
-   [#88f0ec5](https://github.com/latera/camunda-ext/commit/88f0ec5) Do not log files content in Imprint.print method
-   [#f51e713](https://github.com/latera/camunda-ext/commit/f51e713) Do not log files content in HOMS.attach_files method
-   [#293efaa](https://github.com/latera/camunda-ext/commit/293efaa) Fix wrong type cast of getSubjectParamType method in hid.Hydra
-   [#8e6f18f](https://github.com/latera/camunda-ext/commit/8e6f18f) Fix fetching free phone numbers by tel code in hid.Hydra
-   [#16c5551](https://github.com/latera/camunda-ext/commit/16c5551) Fix list of lists JSON escaping
-   [#42b0665](https://github.com/latera/camunda-ext/commit/42b0665) Fix STARTTLS issue with GMail SMTP
-   [#5db94c4](https://github.com/latera/camunda-ext/commit/5db94c4) Change contact filling in Planado class

v1.1 [2019-05-05]
---------------------
### Features
-   [#9a78333](https://github.com/latera/camunda-ext/commit/9a78333) Added SimpleLogger
-   [#08e46b2](https://github.com/latera/camunda-ext/commit/08e46b2) Add utils class for Oracle, Order, IO, JSON, String and DateUtil
-   [#2301d54](https://github.com/latera/camunda-ext/commit/2301d54) Add user and password auth to HTTPRestProcessor class
-   [#e2634a4](https://github.com/latera/camunda-ext/commit/e2634a4) Add class for HOMS API
-   [#94cc1cb](https://github.com/latera/camunda-ext/commit/94cc1cb) Add class for Imprint API
-   [#b2030d9](https://github.com/latera/camunda-ext/commit/b2030d9) Add class for HID API
-   [#d51242f](https://github.com/latera/camunda-ext/commit/d51242f) Add class for Hydra (via HID) API
-   [#1940b23](https://github.com/latera/camunda-ext/commit/1940b23) Move build from Ant to Maven
-   [#8f580d2](https://github.com/latera/camunda-ext/commit/8f580d2) Added Mail and Planado connectors
-   [#f904daf](https://github.com/latera/camunda-ext/commit/f904daf) Added options to hide request & response body for RESTProcessor
-   [#97ed434](https://github.com/latera/camunda-ext/commit/97ed434) Added utils: Numeric, Order
-   [#8e37bf1](https://github.com/latera/camunda-ext/commit/8e37bf1) Some logging methods usage fixes

v1.0 [2018-10-22]
---------------------
