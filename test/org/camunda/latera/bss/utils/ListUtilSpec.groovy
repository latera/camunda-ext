package org.camunda.latera.bss.utils

import spock.lang.*
import org.camunda.latera.bss.utils.ListUtil

class ListUtilSpec extends Specification {
  def "#isList"() {
    expect:
    ListUtil.isList(input) == result

    where:
    input         |result
    [] as Object[]|true
    [] as byte[]  |true
    []            |true
    null          |false
    [null]        |true
    1             |false
    [1]           |true
    'a'           |false
    ['a']         |true
    false         |false
    [false]       |true
    [:]           |false
    [[:]]         |true
  }

  def "#isByteArray"() {
    expect:
    ListUtil.isByteArray(input) == result

    where:
    input         |result
    [] as byte[]  |true
    [] as Object[]|false
    []            |false
    null          |false
    [null]        |false
    1             |false
    [1]           |false
    'a'           |false
    ['a']         |false
    false         |false
    [false]       |false
    [:]           |false
    [[:]]         |false
  }

  def "#upperCase"() {
    expect:
    ListUtil.upperCase(input) == result

    where:
    input  |result
    ['a']  |['A']
    ['A']  |['A']
    [1]    |[1]
    [false]|[false]
    []     |[]
  }

  def "#lowerCase"() {
    expect:
    ListUtil.lowerCase(input) == result

    where:
    input  |result
    ['a']  |['a']
    ['A']  |['a']
    [1]    |[1]
    [false]|[false]
    []     |[]
  }

  def "#parse"() {
    expect:
    ListUtil.parse(input) == result

    where:
    input    |result
    []       |[]
    null     |[]
    '[]'     |[]
    1        |[1]
    '[1]'    |[1]
    'a'      |['a']
    '["a"]'  |['a']
    'null'   |['null']
    '[null]' |[null]
    false    |[false]
    'false'  |['false']
    '[false]'|[false]
    [:]      |[[:]]
    '{}'     |['{}']
    '[{}]'   |[[:]]
  }

  def "#nvl"() {
    expect:
    ListUtil.nvl(input) == result

    where:
    input   |result
    []      |[]
    [null]  |[]
    ['null']|[null]
    ['NULL']|[null]
    [false] |[false]
    [true]  |[true]
    [[]]    |[null]
    [[:]]   |[null]
    [0]     |[0]
    [1]     |[1]
    ['a']   |['a']
  }

  def "#forceNvl"() {
    expect:
    ListUtil.forceNvl(input) == result

    where:
    input   |result
    []      |[]
    [null]  |[]
    ['null']|[]
    ['NULL']|[]
    [[:]]   |[]
    [[]]    |[]
    [false] |[false]
    [0]     |[0]
    [1]     |[1]
    ['a']   |['a']
  }

  def "#firstNotNull"() {
    expect:
    ListUtil.firstNotNull(input, defaultValue) == result

    where:
    input   |defaultValue|result
    []      |null        |null
    [null]  |null        |null
    ['null']|null        |'null'
    ['NULL']|null        |'NULL'
    [false] |null        |false
    [true]  |null        |true
    [[]]    |null        |[]
    [[:]]   |null        |[:]
    [0]     |null        |0
    [1]     |null        |1
    ['a']   |null        |'a'
    []      |false       |false
    [null]  |false       |false
    ['null']|false       |'null'
    ['NULL']|false       |'NULL'
    [false] |false       |false
    [true]  |false       |true
    [[]]    |false       |[]
    [[:]]   |false       |[:]
    [0]     |false       |0
    [1]     |false       |1
    ['a']   |false       |'a'
    []      |true        |true
    [null]  |true        |true
    ['null']|true        |'null'
    ['NULL']|true        |'NULL'
    [false] |true        |false
    [true]  |false       |true
    [[]]    |true        |[]
    [[:]]   |true        |[:]
    [0]     |true        |0
    [1]     |true        |1
    ['a']   |true        |'a'
  }

  def "#firstNvl"() {
    expect:
    ListUtil.firstNvl(input, defaultValue) == result

    where:
    input   |defaultValue|result
    []      |null        |null
    [null]  |null        |null
    ['null']|null        |null
    ['NULL']|null        |null
    [[:]]   |null        |null
    [[]]    |null        |null
    [false] |null        |false
    [true]  |null        |true
    [0]     |null        |0
    [1]     |null        |1
    ['a']   |null        |'a'
    []      |false       |false
    [null]  |false       |false
    ['null']|false       |false
    ['NULL']|false       |false
    [[:]]   |false       |false
    [[]]    |false       |false
    [false] |false       |false
    [true]  |false       |true
    [0]     |false       |0
    [1]     |false       |1
    ['a']   |false       |'a'
    []      |true        |true
    [null]  |true        |true
    ['null']|true        |true
    ['NULL']|true        |true
    [[:]]   |true        |true
    [[]]    |true        |true
    [false] |true        |false
    [true]  |true        |true
    [0]     |true        |0
    [1]     |true        |1
    ['a']   |true        |'a'
  }
}