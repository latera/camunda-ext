# Code style
## Naming
* Use camelCase names for properties and methods
* Use CAPITALIZED_NAMES for contrants
* Use 'Id' instead of 'ID'
* For method which returns entity by id use getEntity(def id) name instead of getEntityById().
* For method which returns entities list use getEntities() name.
* For method which searches for entities by some condition use getEntitiesBy(Map params) name.
* For method which searches for one entity by some condition use getEntityBy(Map params) name.
* Use createEntity(Map params), updateEntity(Map newParams, def id) and deleteEntity(def id) for moving through entity lifecycle.
* If its possible use constant names instead of IDs or put comments. `func(unknownId: 123)` call is very ugly.
* If you fetch constant value by name via API, allow to pass both `param` and `paramId` to methods.

## Types
* Don't use String or Integer IDs because different systems return different types of fields. Allow both with 'def' type.
* Use interfaces in function arguments - e.g. create function with CharSequence argument to accept both String and GStringImpl input values. def is too wide.
* Use classes in function return values- leverage redundant 'def' if possible.
* Return Map or null in case of one return value.
* Return List in case of multiple return values.
* Log errors and return null or [] instead of throwing exception.

## API
* Due to totally different fields of objects used by API using own classes for entities (e.g. Customer, Person, Company, etc) is overkill. Use Map instead.
* Most of classes are just kind of wrappers for some API (XML-RPC, REST, etc). They should be called in function style:
```groovy
API api = new API(execution, optional: value)
Map result = api.doSomething(data: some, optional: args)
Map anotherResult = api.doOther(result, some: update)
```
instead of returning custom objects with hidden calls:
```
User user = api.createUser(...)
user.save()
```
* **Do not convert return value from third-party systems. You can cut part of data to simplify later usage (reduce List to one element in case of always one item return), but don't convert field names and values. It takes too much time to implement this conversion. Also some systems have support of additional values storaging in entity so you can't describe all possible return values.**
* If one-result method just calls ahother method which return multiple results, pass `limit` if possible - instead of sending large answers and parsing them on client side just don't ask to receive them from server at all.
* Use cache when it possible.
* Don't commit wrappers for ad-hoc solutions like self-written servers and API.
* Prefer queries generators instead of hard code some queries in code.

## Other
* Use util classes for value coersion and conversion.