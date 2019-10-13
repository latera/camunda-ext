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
    [false] |[null]
    [[]]    |[null]
    [[:]]   |[null]
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
    [false] |[]
    [1]     |[1]
    ['a']   |['a']
  }
}