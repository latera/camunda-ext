# Code style
## Naming
* Use `camelCase` for properties and methods
* Use `CAPITALIZED_NAMES` for contrants
* Use `Id` instead of `ID`
* For method which returns entity by id use `getEntity(def id)` name instead of `getEntityById()`.
* For method which returns entities list use `getEntities()` name.
* For method which searches for entities by some condition use `getEntitiesBy(Map params)` name.
* For method which searches for one entity by some condition use `getEntityBy(Map params)` name.
* Use `createEntity(Map params)` (or `createEntity(Map params = [:], def parentId)`), `updateEntity(Map newParams = [:], def id)` and `deleteEntity(def id)` for moving through entity lifecycle.
* Only mandatory args should be passed directly to function or method. Optional should be passed as named args using groovy's feature to push them all into first _Map_ argument.
* If its possible use constant names instead of IDs or put comments. `func(paramId: 123)` call is very ugly, prefer `func(param: 'SOME_CONST')`.
* If you fetch constant value by name via API, allow to pass both its name (`param`) and value (`paramId`) to methods.

## Types
* Don't use _String_ or _Integer_ identifiers because different systems return different types of fields. Allow both with _def_ type.
* Use interfaces in function arguments - e.g. create function with _CharSequence_ argument to accept both _String_ and _GStringImpl_ input values. _def_ here is too wide.
* Use exact types in function return values if possible to leverage redundant _def_ usage.
* Return _Map_ in case of returning exactly one value.
* Return _List_ in case of multiple return values.
* Log errors and return `null` (for _Map_) or `[]` (for _List_) instead of throwing exception.

## API
* Due to totally different fields of objects used by API using own classes for entities (e.g. Customer, Person, Company, etc) is overkill. Use Map instead.
* Most of classes are just kind of wrappers for some API (XML-RPC, REST, etc). They should be called in procedural style:
```groovy
API api = new API(execution, optional: value)
Map result = api.doSomething(data: some, optional: args)
Map anotherResult = api.doOther(result, some: update)
```
instead of returning custom objects with hidden calls:
```groovy
User user = api.createUser(...)
user.save()
```
* **Do not convert return value from third-party systems. You can cut part of data to simplify later usage (reduce List to one element in case of always one item return), but don't convert field names and values. It takes too much time to implement this conversion. Also some systems have support of additional values storaging in entity so you can't describe all possible return values.**
* If one-result method just calls ahother method which return multiple results, pass `limit` if possible - instead of sending large answers and parsing them on client side just don't ask to receive them from server at all.
* **Use cache when it possible**.
* Don't commit wrappers for ad-hoc solutions like custom web-servers and API. Create classes inside business process code, not here.
* Prefer queries generators instead of hard coding some queries.

## Other
* Use util classes for value coersion and conversion.
