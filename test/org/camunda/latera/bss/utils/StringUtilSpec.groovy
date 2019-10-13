package org.camunda.latera.bss.utils

import spock.lang.*
import java.nio.charset.Charset
import org.camunda.latera.bss.utils.StringUtil

class StringUtilSpec extends Specification {
  def "#isString"() {
    expect:
    StringUtil.isString(input) == result

    where:
    input        |result
    'some string'|true
    "${true}"    |true
    null         |false
    0            |false
    false        |false
    []           |false
    [:]          |false
  }

  def "#isEmpty"() {
    expect:
    StringUtil.isEmpty(input) == result

    where:
    input        |result
    'some string'|false
    ''           |true
    ' '          |true
    '\t'         |true
    '\n'         |true
    null         |true
    'null'       |false
    'NULL'       |false
    0            |true
    false        |true
    []           |true
    [:]          |true
  }

  def "#notEmpty"() {
    expect:
    StringUtil.notEmpty(input) == result

    where:
    input        |result
    'some string'|true
    ''           |false
    ' '          |false
    '\t'         |false
    '\n'         |false
    null         |false
    'null'       |true
    'NULL'       |true
    0            |false
    false        |false
    []           |false
    [:]          |false
  }

  def "#forceIsEmpty"() {
    expect:
    StringUtil.forceIsEmpty(input) == result

    where:
    input        |result
    'some string'|false
    ''           |true
    ' '          |true
    '\t'         |true
    '\n'         |true
    null         |true
    'null'       |true
    'NULL'       |true
    0            |true
    false        |true
    []           |true
    [:]          |true
  }

  def "#forceNotEmpty"() {
    expect:
    StringUtil.forceNotEmpty(input) == result

    where:
    input        |result
    'some string'|true
    ''           |false
    ' '          |false
    '\t'         |false
    '\n'         |false
    null         |false
    'null'       |false
    'NULL'       |false
    0            |false
    false        |false
    []           |false
    [:]          |false
  }

  def "#trim"() {
    expect:
    StringUtil.trim(input) == result

    where:
    input            |result
    '\t   some'      |'some'
    'some   \t'      |'some'
    '\t   some   \t' |'some'
    '\n  some  \n'   |'some'
    '\nsome\n'       |'some'
    '\n\t some\n\t ' |'some'
    's o m e'        |'s o m e'
    's o\nm e'       |'s o\nm e'
    null             |null
    0                |'0'
    false            |'false'
    []               |'[]'
    [:]              |'[:]'
  }

  def "#nvl"() {
    expect:
    StringUtil.nvl(input) == result

    where:
    input |result
    'some'|'some'
    ''    |''
    null  |null
    'null'|null
    'NULL'|null
    0     |'0'
    false |'false'
    []    |'[]'
    [:]   |'[:]'
  }

  def "#forceNvl"() {
    expect:
    StringUtil.forceNvl(input) == result

    where:
    input |result
    'some'|'some'
    ''    |null
    null  |null
    'null'|null
    'NULL'|null
    0     |'0'
    false |'false'
    []    |'[]'
    [:]   |'[:]'
  }

  def "#camelize without firstUpper"() {
    expect:
    StringUtil.camelize(input, false) == result

    where:
    input      |result
    'some'     |'some'
    'some_text'|'someText'
    ''         |''
  }

  def "#camelize with firstUpper"() {
    expect:
    StringUtil.camelize(input, true) == result

    where:
    input      |result
    'some'     |'Some'
    'some_text'|'SomeText'
    ''         |''
  }

  def "#camelize with named arguments - without firstUpper"() {
    expect:
    StringUtil.camelize(input, firstUpper: false) == result

    where:
    input      |result
    'some'     |'some'
    'some_text'|'someText'
    ''         |''
  }

  def "#camelize with named arguments - with firstUpper"() {
    expect:
    StringUtil.camelize(input, firstUpper: true) == result

    where:
    input      |result
    'some'     |'Some'
    'some_text'|'SomeText'
    ''         |''
  }

  def "#snakeCase"() {
    expect:
    StringUtil.snakeCase(input) == result

    where:
    input      |result
    'some'     |'some'
    'someText' |'some_text'
    'SomeText' |'some_text'
    ''         |''
  }

  def "#capitalize"() {
    expect:
    StringUtil.capitalize(input) == result

    where:
    input       |result
    'some'      |'Some'
    'Some'      |'Some'
    'some text' |'Some text'
    'Some text' |'Some text'
    'someText'  |'SomeText'
    'SomeText'  |'SomeText'
    's'         |'S'
    'S'         |'S'
    ''          |''
  }

  def "#decapitalize"() {
    expect:
    StringUtil.decapitalize(input) == result

    where:
    input       |result
    'Some'      |'some'
    'some'      |'some'
    'Some Text' |'some Text'
    'some Text' |'some Text'
    'SomeText'  |'someText'
    'someText'  |'someText'
    'S'         |'s'
    's'         |'s'
    ''          |''
  }

  def "#join"() {
    expect:
    StringUtil.join(input, delimiter) == result

    where:
    input                        |delimiter|result
    [1,2]                        |','      |'1,2'
    [1,2]                        |' '      |'1 2'
    [1,2]                        |';'      |'1;2'
    [1,2,null,'','null',0,false] |','      |'1,2,null,,null,0,false'
    [1,2,null,'','null',0,false] |' '      |'1 2 null  null 0 false'
    [1,2,null,'','null',0,false] |';'      |'1;2;null;;null;0;false'
    []                           |','      |''
    []                           |';'      |''
    []                           |' '      |''
  }

  def "#joinNonEmpty"() {
    expect:
    StringUtil.joinNonEmpty(input, delimiter) == result

    where:
    input                        |delimiter|result
    [1,2]                        |','      |'1,2'
    [1,2]                        |' '      |'1 2'
    [1,2]                        |';'      |'1;2'
    [1,2,null,'','null',0,false] |','      |'1,2'
    [1,2,null,'','null',0,false] |' '      |'1 2'
    [1,2,null,'','null',0,false] |';'      |'1;2'
    []                           |','      |''
    []                           |';'      |''
    []                           |' '      |''
  }

  def "#random"() {
    expect:
    StringUtil.random(inputLen).size() == resultLen
    StringUtil.random(inputLen) != StringUtil.random(inputLen)

    where:
    inputLen|resultLen
    6       |6
    7       |7
    8       |8
  }

  def "#unicodeToVarchar"() {
    expect:
    StringUtil.unicodeToVarchar(input) == result as byte[]

    where:
    input |result
    'test'|[116, 101, 115, 116]
    'Test'|[84,  101, 115, 116]
    '0'   |[48]
    ' '   |[32]
    ''    |null
    null  |null
  }

  def "#varcharToUnicode"() {
    expect:
    if (input != null && input != '') {
      input = new String(input as byte[], Charset.forName("ISO-8859-1"))
    }
    StringUtil.varcharToUnicode(input) == result

    where:
    input                |result
    [116, 101, 115, 116] |'test'
    [84,  101, 115, 116] |'Test'
    [48]                 |'0'
    [32]                 |' '
    ''                   |null
    []                   |null
    null                 |null
  }

  def "#unicodeToCP1251"() {
    expect:
    StringUtil.unicodeToCP1251(input) == result as byte[]

    where:
    input |result
    'test'|[116, 101, 115, 116]
    'Test'|[84,  101, 115, 116]
    '0'   |[48]
    ' '   |[32]
    ''    |null
    null  |null
  }

  def "#cp1251ToUnicode"() {
    expect:
    if (input != null && input != '') {
      input = new String(input as byte[], Charset.forName("CP1251"))
    }
    StringUtil.varcharToUnicode(input) == result

    where:
    input               |result
    [116, 101, 115, 116]|'test'
    [84,  101, 115, 116]|'Test'
    [48]                |'0'
    [32]                |' '
    ''                   |null
    []                  |null
    null                |null
  }
}