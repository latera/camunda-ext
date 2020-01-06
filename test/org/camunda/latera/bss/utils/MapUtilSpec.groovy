package org.camunda.latera.bss.utils

import spock.lang.*
import org.camunda.latera.bss.utils.MapUtil

class MapUtilSpec extends Specification {
  def "#isMap"() {
    expect:
    MapUtil.isMap(input) == result

    where:
    input|result
    [:]  |true
    []   |false
    null |false
    1    |false
    'a'  |false
    false|false
  }

  def "#parse"() {
    expect:
    MapUtil.parse(input) == result

    where:
    input    |result
    [a:1]    |[a:1]
    '{"a":1}'|[a:1]
    [:]      |[:]
    '{}'     |[:]
    null     |[:]
    []       |[:]
    1        |[:]
    'a'      |[:]
    false    |[:]
  }

  def "#keysList"() {
    expect:
    MapUtil.keysList(input) == result

    where:
    input|result
    [a:1]|['a']
    [1:1]|['1']
    [:]  |[]
    null |[]
  }

  def "#keysCount"() {
    expect:
    MapUtil.keysCount(input) == result

    where:
    input    |result
    [a:1,b:2]|2
    [1:1]    |1
    [:]      |0
    null     |0
  }

  def "#merge"() {
    expect:
    MapUtil.merge(first, second) == result

    where:
    first       |second      |result
    [:]         |[a:3,b:2]   |[a:3,b:2]
    [b:2]       |[a:3]       |[a:3,b:2]
    [a:1,b:2]   |[a:3]       |[a:3,b:2]
    [a:1,b:2]   |["${'a'}":3]|[a:3,b:2]
    [a:3,b:2]   |[:]         |[a:3,b:2]
    [a:1,b:null]|[a:3]       |[a:3,b:null]
    [a:1,b:null]|[a:3,b:2]   |[a:3,b:2]
    [a:1,b:2]   |[a:3,b:null]|[a:3,b:null]
  }

  def "#mergeNotNull"() {
    expect:
    MapUtil.mergeNotNull(first, second) == result

    where:
    first       |second      |result
    [:]         |[a:3,b:2]   |[a:3,b:2]
    [b:2]       |[a:3]       |[a:3,b:2]
    [a:1,b:2]   |[a:3]       |[a:3,b:2]
    [a:1,b:2]   |["${'a'}":3]|[a:3,b:2]
    [a:3,b:2]   |[:]         |[a:3,b:2]
    [a:1,b:null]|[a:3]       |[a:3,b:null]
    [a:1,b:null]|[a:3,b:2]   |[a:3,b:2]
    [a:1,b:2]   |[a:3,b:null]|[a:3,b:2]
  }

  def "#camelizeKeys with Map input - without firstUpper"() {
    expect:
    MapUtil.camelizeKeys(input, false) == result

    where:
    input          |result
    [some:1]       |[some:1]
    ["some_text":1]|[someText:1]
    ['':1]         |['':1]
    [:]            |[:]
  }

  def "#camelizeKeys with Map input - with firstUpper"() {
    expect:
    MapUtil.camelizeKeys(input, true) == result

    where:
    input          |result
    [some:1]       |[Some:1]
    ["some_text":1]|[SomeText:1]
    ['':1]         |['':1]
    [:]            |[:]
  }

  def "#camelizeKeys with List input - without firstUpper"() {
    expect:
    MapUtil.camelizeKeys(input, false) == result

    where:
    input            |result
    [[some:1]]       |[[some:1]]
    [["some_text":1]]|[[someText:1]]
    [['':1]]         |[['':1]]
    [[:]]            |[[:]]
    [1]              |[1]
    ['a']            |['a']
    [false]          |[false]
  }

  def "#camelizeKeys with List input - with firstUpper"() {
    expect:
    MapUtil.camelizeKeys(input, true) == result

    where:
    input            |result
    [[some:1]]       |[[Some:1]]
    [["some_text":1]]|[[SomeText:1]]
    [['':1]]         |[['':1]]
    [[:]]            |[[:]]
    [1]              |[1]
    ['a']            |['a']
    [false]          |[false]
  }

  def "#deepCamelizeKeys with Map input - without firstUpper"() {
    expect:
    MapUtil.deepCamelizeKeys(input, false) == result

    where:
    input                         |result
    [some:[value:1]]              |[some:[value:1]]
    ["some_text":["some_value":1]]|[someText:[someValue:1]]
    ["some_text":['':1]]          |[someText:['':1]]
    ["some_text":["some_value"]]  |[someText:["some_value"]]
    ["some_text":[1]]             |[someText:[1]]
    ["some_text":[false]]         |[someText:[false]]
    ['':1]                        |['':1]
    [:]                           |[:]
  }

  def "#deepCamelizeKeys with Map input - with firstUpper"() {
    expect:
    MapUtil.deepCamelizeKeys(input, true) == result

    where:
    input                         |result
    [some:[value:1]]              |[Some:[Value:1]]
    ["some_text":["some_value":1]]|[SomeText:[SomeValue:1]]
    ["some_text":['':1]]          |[SomeText:['':1]]
    ["some_text":["some_value"]]  |[SomeText:["some_value"]]
    ["some_text":[1]]             |[SomeText:[1]]
    ["some_text":[false]]         |[SomeText:[false]]
    ['':1]                        |['':1]
    [:]                           |[:]
  }

  def "#deepCamelizeKeys with List input - without firstUpper"() {
    expect:
    MapUtil.deepCamelizeKeys(input, false) == result

    where:
    input                           |result
    [[some:[value:1]]]              |[[some:[value:1]]]
    [["some_text":["some_value":1]]]|[[someText:[someValue:1]]]
    [["some_text":['':1]]]          |[[someText:['':1]]]
    [["some_text":["some_value"]]]  |[[someText:["some_value"]]]
    [["some_text":[1]]]             |[[someText:[1]]]
    [["some_text":[false]]]         |[[someText:[false]]]
    [['':1]]                        |[['':1]]
    [[:]]                           |[[:]]
    [1]                             |[1]
    ['a']                           |['a']
    [false]                         |[false]
  }

  def "#deepCamelizeKeys with List input - with firstUpper"() {
    expect:
    MapUtil.deepCamelizeKeys(input, true) == result

    where:
    input                           |result
    [[some:[value:1]]]              |[[Some:[Value:1]]]
    [["some_text":["some_value":1]]]|[[SomeText:[SomeValue:1]]]
    [["some_text":['':1]]]          |[[SomeText:['':1]]]
    [["some_text":["some_value"]]]  |[[SomeText:["some_value"]]]
    [["some_text":[1]]]             |[[SomeText:[1]]]
    [["some_text":[false]]]         |[[SomeText:[false]]]
    [['':1]]                        |[['':1]]
    [[:]]                           |[[:]]
    [1]                             |[1]
    ['a']                           |['a']
    [false]                         |[false]
  }

  def "#snakeCaseKeys with Map input"() {
    expect:
    MapUtil.snakeCaseKeys(input) == result

    where:
    input       |result
    [some:1]    |[some:1]
    [someText:1]|["some_text":1]
    ['':1]      |['':1]
    [:]         |[:]
  }

  def "#snakeCaseKeys with List input"() {
    expect:
    MapUtil.snakeCaseKeys(input) == result

    where:
    input         |result
    [[some:1]]    |[[some:1]]
    [[someText:1]]|[["some_text":1]]
    [['':1]]      |[['':1]]
    [[:]]         |[[:]]
    [1]           |[1]
    ['a']         |['a']
    [false]       |[false]
  }

  def "#deepSnakeCaseKeys with Map input"() {
    expect:
    MapUtil.deepSnakeCaseKeys(input) == result

    where:
    input                   |result
    [some:[value:1]]        |[some:[value:1]]
    [someText:[someValue:1]]|["some_text":["some_value":1]]
    [someText:['':1]]       |["some_text":['':1]]
    [someText:["someValue"]]|["some_text":["someValue"]]
    [someText:[1]]          |["some_text":[1]]
    [someText:[false]]      |["some_text":[false]]
    ['':1]                  |['':1]
    [:]                     |[:]
  }

  def "#deepSnakeCaseKeys with List input"() {
    expect:
    MapUtil.deepSnakeCaseKeys(input) == result

    where:
    input                     |result
    [[some:[value:1]]]        |[[some:[value:1]]]
    [[someText:[someValue:1]]]|[["some_text":["some_value":1]]]
    [[someText:['':1]]]       |[["some_text":['':1]]]
    [[someText:["someValue"]]]|[["some_text":["someValue"]]]
    [[someText:[1]]]          |[["some_text":[1]]]
    [[someText:[false]]]      |[["some_text":[false]]]
    [['':1]]                  |[['':1]]
    [[:]]                     |[[:]]
    [[1]]                     |[[1]]
    [['a']]                   |[['a']]
    [[false]]                 |[[false]]
  }

  def "#nvl"() {
    expect:
    MapUtil.nvl(input) == result

    where:
    input     |result
    [:]       |[:]
    [a:null]  |[:]
    [a:'null']|[a:null]
    [a:'NULL']|[a:null]
    [a:false] |[a:false]
    [a:true]  |[a:true]
    [a:[]]    |[a:null]
    [a:[:]]   |[a:null]
    [a:0]     |[a:0]
    [a:1]     |[a:1]
    [a:'a']   |[a:'a']
  }

  def "#forceNvl"() {
    expect:
    MapUtil.forceNvl(input) == result

    where:
    input     |result
    [:]       |[:]
    [a:null]  |[:]
    [a:'null']|[:]
    [a:'NULL']|[:]
    [a:false] |[a:false]
    [a:true]  |[a:true]
    [a:[]]    |[:]
    [a:[:]]   |[:]
    [a:0]     |[a:0]
    [a:1]     |[a:1]
    [a:'a']   |[a:'a']
  }
}