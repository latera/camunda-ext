package org.camunda.latera.bss.utils

import spock.lang.*
import org.camunda.latera.bss.utils.Numeric

class NumericSpec extends Specification {
  def "#toIntSafe without default value"() {
    expect:
    Numeric.toIntSafe(input) == result

    where:
    input |result
    0     |0
    1     |1
    1.1   |1
    '0'   |0
    '1'   |1
    '01'  |1
    '1.1' |1
    '1,1' |1
    '01.1'|1
    '01,1'|1
    'a'   |null
    null  |null
    false |null
    []    |null
    [:]   |null
  }

  def "#toIntSafe with default value"() {
    expect:
    Numeric.toIntSafe(input, defaultValue) == result

    where:
    input |defaultValue||result
    0     |-1          ||0
    1     |0           ||1
    1.1   |0           ||1
    '0'   |-1          ||0
    '1'   |0           ||1
    '01'  |0           ||1
    '1.1' |0           ||1
    '01.1'|0           ||1
    '1,1' |0           ||1
    '01,1'|0           ||1
    'a'   |0           ||0
    null  |0           ||0
    false |0           ||0
    []    |0           ||0
    [:]   |0           ||0
  }

  def "#toIntStrict with default value"() {
    expect:
    Numeric.toIntStrict(input, defaultValue) == result

    where:
    input |defaultValue||result
    0     |-1          ||0
    1     |0           ||1
    1.1   |0           ||0
    '01'  |0           ||1
    '1'   |0           ||1
    '1.1' |0           ||0
    '01.1'|0           ||0
    '1,1' |0           ||0
    '01,1'|0           ||0
    'a'   |0           ||0
    null  |0           ||0
    false |0           ||0
    []    |0           ||0
    [:]   |0           ||0
  }

  def "#toFloatSafe without default value"() {
    expect:
    Numeric.toFloatSafe(input) == result

    where:
    input |result
    0     |0
    1     |1
    1.1   |1.1
    '1'   |1
    '01'  |1
    '1.1' |1.1
    '01.1'|1.1
    '1,1' |1.1
    '01,1'|1.1
    'a'   |null
    null  |null
    false |null
    []    |null
    [:]   |null
  }

  def "#toFloatSafe with default value"() {
    expect:
    Numeric.toFloatSafe(input, defaultValue) == result

    where:
    input |defaultValue||result
    0     |-1          ||0
    1     |0           ||1
    1.1   |0           ||1.1
    '1'   |0           ||1
    '01'  |0           ||1
    '1.1' |0           ||1.1
    '01.1'|0           ||1.1
    '1,1' |0           ||1.1
    '01,1'|0           ||1.1
    'a'   |0           ||0
    null  |0           ||0
    false |0           ||0
    []    |0           ||0
    [:]   |0           ||0
  }

  def "#isInteger"() {
    expect:
    Numeric.isInteger(input) == result

    where:
    input |result
    0     |true
    1     |true
    1.1   |false
    '1'   |true
    '01'  |true
    '1.1' |false
    '01.1'|false
    '1,1' |false
    '01,1'|false
    'a'   |false
    null  |false
    false |false
    []    |false
    [:]   |false
  }

  def "#isIntegerStrict"() {
    expect:
    Numeric.isIntegerStrict(input) == result

    where:
    input |result
    0     |true
    1     |true
    1.0   |false
    1.1   |false
    '1'   |true
    '01'  |true
    '1.1' |false
    '01.1'|false
    '1,1' |false
    '01,1'|false
    'a'   |false
    null  |false
    false |false
    []    |false
    [:]   |false
  }

  def "#isFloat"() {
    expect:
    Numeric.isFloat(input) == result

    where:
    input |result
    0     |true
    1     |true
    1.1   |true
    '1'   |true
    '01'  |true
    '1.1' |true
    '01.1'|true
    '1,1' |true
    '01,1'|true
    'a'   |false
    null  |false
    false |false
    []    |false
    [:]   |false
  }

  def "#isNumber"() {
    expect:
    Numeric.isNumber(input) == result

    where:
    input |result
    0     |true
    1     |true
    1.1   |true
    '1'   |true
    '01'  |true
    '1.1' |true
    '01.1'|true
    '1,1' |true
    '01,1'|true
    'a'   |false
    null  |false
    false |false
    []    |false
    [:]   |false
  }

  def "#round"() {
    expect:
    Numeric.round(input, digits) == result

    where:
    input  |digits||result
    1      |0     ||1
    1      |1     ||1
    1      |2     ||1
    1.1    |0     ||1
    1.1    |1     ||1.1
    1.1    |2     ||1.1
    1.11   |0     ||1
    1.11   |1     ||1.1
    1.11   |2     ||1.11
    1.11   |3     ||1.11
    1.111  |0     ||1
    1.111  |1     ||1.1
    1.111  |2     ||1.11
    1.111  |3     ||1.111
    1.111  |4     ||1.111
    '1'    |2     ||1
    '1.1'  |2     ||1.1
    '1.11' |2     ||1.11
    '1.111'|2     ||1.11
    '1,1'  |2     ||1.1
    '1,11' |2     ||1.11
    '1,111'|2     ||1.11
    'a'    |2     ||0
    null   |2     ||0
    false  |2     ||0
    []     |2     ||0
    [:]    |2     ||0
  }

  def "#max"() {
    expect:
    Numeric.max(first, second) == result

    where:
    first    |second ||result
    1        |-1     ||1
    1        |0      ||1
    1        |1      ||1
    1        |2      ||2
    1.1      |-1     ||1.1
    1.1      |0      ||1.1
    1.1      |1      ||1.1
    1.1      |1.2    ||1.2
    1.1      |2      ||2
    '1'      |0      ||1
    '1'      |1      ||1
    '1'      |2      ||2
    '1'      |'0'    ||1
    '1'      |'1'    ||1
    '1'      |'2'    ||2
    '1.1'    |0      ||1.1
    '1.1'    |1      ||1.1
    '1.1'    |1.2    ||1.2
    '1.1'    |2      ||2
    '1.1'    |'0'    ||1.1
    '1.1'    |'1'    ||1.1
    '1.1'    |'1.2'  ||1.2
    '1.1'    |'2'    ||2
    '1,1'    |0      ||1.1
    '1,1'    |1      ||1.1
    '1,1'    |1.2    ||1.2
    '1,1'    |2      ||2
    '1,1'    |'0'    ||1.1
    '1,1'    |'1'    ||1.1
    '1,1'    |'1.2'  ||1.2
    '1,1'    |'1,2'  ||1.2
    '1,1'    |'2'    ||2
    'a'      |2      ||2
    'a'      |-1     ||0
    'a'      |0      ||0
    'a'      |'b'    ||0
    null     |2      ||2
    null     |-1     ||0
    null     |0      ||0
    null     |null   ||0
    false    |2      ||2
    false    |-1     ||0
    false    |0      ||0
    false    |true   ||0
    false    |false  ||0
    []       |2      ||2
    []       |-1     ||0
    []       |0      ||0
    []       |[]     ||0
    []       |[1]    ||0
    [:]      |2      ||2
    [:]      |-1     ||0
    [:]      |0      ||0
    [:]      |[:]    ||0
    [:]      |[a:1]  ||0
  }

  def "#min"() {
    expect:
    Numeric.min(first, second) == result

    where:
    first    |second ||result
    1        |-1     ||-1
    1        |0      ||0
    1        |1      ||1
    1        |2      ||1
    1.1      |-1     ||-1
    1.1      |0      ||0
    1.1      |1      ||1
    1.1      |1.2    ||1.1
    1.1      |2      ||1.1
    '1'      |0      ||0
    '1'      |1      ||1
    '1'      |2      ||1
    '1'      |'0'    ||0
    '1'      |'1'    ||1
    '1'      |'2'    ||1
    '1.1'    |0      ||0
    '1.1'    |1      ||1
    '1.1'    |1.2    ||1.1
    '1.1'    |2      ||1.1
    '1.1'    |'0'    ||0
    '1.1'    |'1'    ||1
    '1.1'    |'1.2'  ||1.1
    '1.1'    |'2'    ||1.1
    '1,1'    |0      ||0
    '1,1'    |1      ||1
    '1,1'    |1.2    ||1.1
    '1,1'    |2      ||1.1
    '1,1'    |'0'    ||0
    '1,1'    |'1'    ||1
    '1,1'    |'1.2'  ||1.1
    '1,1'    |'1,2'  ||1.1
    '1,1'    |'2'    ||1.1
    'a'      |2      ||0
    'a'      |-1     ||-1
    'a'      |0      ||0
    'a'      |'b'    ||0
    null     |2      ||0
    null     |-1     ||-1
    null     |0      ||0
    null     |null   ||0
    false    |2      ||0
    false    |-1     ||-1
    false    |0      ||0
    false    |true   ||0
    false    |false  ||0
    []       |2      ||0
    []       |-1     ||-1
    []       |0      ||0
    []       |[]     ||0
    []       |[1]    ||0
    [:]      |2      ||0
    [:]      |-1     ||-1
    [:]      |0      ||0
    [:]      |[:]    ||0
    [:]      |[a:1]  ||0
  }
}