package org.camunda.latera.bss.utils

import spock.lang.*
import org.camunda.latera.bss.utils.JSON
import org.camunda.latera.bss.utils.DateTimeUtil
import org.camunda.latera.bss.utils.CSV

class JSONSpec extends Specification {
  @Shared def todayStr = '2000-10-13T01:02:03'
  @Shared def today    = DateTimeUtil.parseDateTimeAny(todayStr)
  @Shared def nowStr   = '2000-10-13T01:02:03.123'
  @Shared def now      = DateTimeUtil.parseDateTimeAny(nowStr)
  @Shared def localStr = '2000-10-13T01:02:03.123+03:00'
  @Shared def local    = DateTimeUtil.parseDateTimeAny(localStr)
  @Shared def csv      = new CSV(a:1, b:2)

  def "#escape"() {
    expect:
    JSON.escape(input) == result

    where:
    input    |result
    today    |todayStr
    now      |nowStr
    local    |localStr
    csv      |[[a:'1', b:'2']]
    0        |0
    'a'      |'a'
    false    |false
    null     |null
    []       |[]
    [0]      |[0]
    ['a']    |['a']
    [false]  |[false]
    [today]  |[todayStr]
    [now]    |[nowStr]
    [local]  |[localStr]
    [csv]    |[[[a:'1', b:'2']]]
    [:]      |[:]
    [a:1]    |[a:1]
    [a:'a']  |[a:'a']
    [a:false]|[a:false]
    [a:null] |[a:null]
    [a:today]|[a:todayStr]
    [a:now]  |[a:nowStr]
    [a:local]|[a:localStr]
    [a:csv]  |[a:[[a:'1', b:'2']]]
  }

  def "#to"() {
    expect:
    JSON.to(input) == result

    where:
    input    |result
    today    |"\"${todayStr}\""
    now      |"\"${nowStr}\""
    local    |"\"${localStr}\""
    csv      |'[{"a":"1","b":"2"}]'
    0        |'0'
    'a'      |'"a"'
    false    |'false'
    null     |'null'
    []       |'[]'
    [0]      |'[0]'
    ['a']    |'["a"]'
    [false]  |'[false]'
    [today]  |"[\"${todayStr}\"]"
    [now]    |"[\"${nowStr}\"]"
    [local]  |"[\"${localStr}\"]"
    [csv]    |'[[{"a":"1","b":"2"}]]'
    [:]      |'{}'
    [a:1]    |'{"a":1}'
    [a:'a']  |'{"a":"a"}'
    [a:false]|'{"a":false}'
    [a:null] |'{"a":null}'
    [a:today]|"{\"a\":\"${todayStr}\"}"
    [a:now]  |"{\"a\":\"${nowStr}\"}"
    [a:local]|"{\"a\":\"${localStr}\"}"
    [a:csv]  |'{"a":[{"a":"1","b":"2"}]}'
  }

  def "#from"() {
    expect:
    JSON.from(input) == result

    where:
    input                      |result
    "\"${todayStr}\""          |todayStr // doesn't automatically convert back to date types
    "\"${nowStr}\""            |nowStr   // doesn't automatically convert back to date types
    "\"${localStr}\""          |localStr // doesn't automatically convert back to date types
    '[{"a":"1","b":"2"}]'      |[[a:'1', b:'2']] // doesn't automatically convert back to CSV types
    '0'                        |0
    '"a"'                      |'a'
    'false'                    |false
    'null'                     |null
    '[]'                       |[]
    '[0]'                      |[0]
    '["a"]'                    |['a']
    '[false]'                  |[false]
    "[\"${todayStr}\"]"        |[todayStr]
    "[\"${nowStr}\"]"          |[nowStr]
    "[\"${localStr}\"]"        |[localStr]
    '[[{"a":"1","b":"2"}]]'    |[[[a:'1', b:'2']]]
    '{}'                       |[:]
    '{"a":1}'                  |[a:1]
    '{"a":"a"}'                |[a:'a']
    '{"a":false}'              |[a:false]
    '{"a":null}'               |[a:null]
    "{\"a\":\"${todayStr}\"}"  |[a:todayStr]
    "{\"a\":\"${nowStr}\"}"    |[a:nowStr]
    "{\"a\":\"${localStr}\"}"  |[a:localStr]
    '{"a":[{"a":"1","b":"2"}]}'|[a:[[a:'1', b:'2']]]
  }
}