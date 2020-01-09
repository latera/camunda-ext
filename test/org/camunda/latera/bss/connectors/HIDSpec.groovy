package org.camunda.latera.bss.connectors

import groovy.net.xmlrpc.XMLRPCServerProxy
import org.camunda.latera.bss.utils.DateTimeUtil
import org.camunda.latera.bss.utils.Oracle
import java.time.LocalDateTime
import java.time.ZonedDateTime
import spock.lang.*

class HIDSpec extends Specification {
  @Shared def proxy

  static String emptyListQuery = """
  SELECT 145253, 'text1' FROM DUAL WHERE ROWNUM = 0
  """
  static String emptyMapQuery = """
    SELECT 'n_value', 145253, 'vc_value', 'text1' FROM DUAL WHERE ROWNUM = 0
  """
  static Map emptyResponse = [
    PagesNumber   : '1',
    RecordsNumber : '0',
    SelectResult  : []
  ]

  def setupSpec() {
    String fakeUrl = 'http://localhost'
    proxy = Mock(XMLRPCServerProxy, constructorArgs: [fakeUrl])
  }

  def "#queryDatabaseList"() {
    given:
      HID hid = GroovySpy(HID, constructorArgs:[proxy])

      String listListQuery = """
      SELECT 145253, 'text1' FROM DUAL
      UNION ALL
      SELECT 564535, 'text2' FROM DUAL
      """
      Map listListResponse = [
        PagesNumber   : '1',
        RecordsNumber : '2',
        SelectResult  : [
          ['1', 145253, 'text1'],
          ['2', 564535, 'text2']
        ]
      ]
      List listListResult = [
        [145253, 'text1'],
        [564535, 'text2']
      ]
    when: 'execute query'
      List res = hid.queryDatabaseList(listListQuery)
    then: 'it calls HID`s SELECT internal function'
      1 * hid.call('SELECT', _) >> listListResponse
    and: 'return data as List[List] of column values'
      res == listListResult
    when: 'resultset is empty'
      res = hid.queryDatabaseList(emptyListQuery)
    then: 'it calls HID`s SELECT internal function'
      1 * hid.call('SELECT', _) >> emptyResponse
    and: 'return empty List'
      res == []
  }

  def "#queryDatabaseMap"() {
    given:
      HID hid = GroovySpy(HID, constructorArgs:[proxy])

      String listMapQuery = """
      SELECT 'n_value', 145253, 'vc_value', 'text1' FROM DUAL
      UNION ALL
      SELECT 'n_value', 564535, 'vc_value', 'text2' FROM DUAL
      """
      Map listMapResponse = [
        PagesNumber   : '1',
        RecordsNumber : '2',
        SelectResult  : [
          ['1', 'n_value', 145253, 'vc_value', 'text1'],
          ['2', 'n_value', 564535, 'vc_value', 'text2']
        ]
      ]
      List listMapResult = [
        [n_value: 145253, vc_value: 'text1'],
        [n_value: 564535, vc_value: 'text2']
      ]
    when: 'execute query'
      List res = hid.queryDatabaseMap(listMapQuery)
    then: 'it calls HID`s SELECT internal function'
      1 * hid.call('SELECT', _) >> listMapResponse
    and: 'return data as List[Map] of query result'
      res == listMapResult
    when: 'resultset is empty'
      res = hid.queryDatabaseMap(emptyMapQuery)
    then: 'it calls HID`s SELECT internal function'
      1 * hid.call('SELECT', _) >> emptyResponse
    and: 'return empty List'
      res == []
  }

  def "#queryFirstList"() {
    given:
      HID hid = GroovySpy(HID, constructorArgs:[proxy])

      String listQuery = """
      SELECT 145253, 'text1' FROM DUAL
      """
      Map listResponse = [
        PagesNumber   : '1',
        RecordsNumber : '1',
        SelectResult  : [
          ['1', 145253, 'text1']
        ]
      ]
      List listResult = [145253, 'text1']
    when: 'execute query'
      List res = hid.queryFirstList(listQuery)
    then: 'it calls HID`s SELECT internal function'
      1 * hid.call('SELECT', _) >> listResponse
    and: 'return data as List with column values'
      res == listResult
    when: 'resultset is empty'
      res = hid.queryFirstList(emptyListQuery)
    then: 'it calls HID`s SELECT internal function'
      1 * hid.call('SELECT', _) >> emptyResponse
    and: 'return empty List'
      res == null
  }

  def "#queryFirstMap"() {
    given:
      HID hid = GroovySpy(HID, constructorArgs:[proxy])

      String mapQuery = """
      SELECT 'n_value', 145253, 'vc_value', 'text1' FROM DUAL
      UNION ALL
      SELECT 'n_value', 564535, 'vc_value', 'text2' FROM DUAL
      """
      Map mapResponse = [
        PagesNumber   : '1',
        RecordsNumber : '1',
        SelectResult  : [
          ['1', 'n_value', 145253, 'vc_value', 'text1']
        ]
      ]
      Map mapResult = [n_value: 145253, vc_value: 'text1']
    when: 'execute query'
      Map res = hid.queryFirstMap(mapQuery)
    then: 'it calls HID`s SELECT internal function'
      1 * hid.call('SELECT', _) >> mapResponse
    and: 'return data as Map with query result'
      res == mapResult
    when: 'resultset is empty'
      res = hid.queryFirstMap(emptyMapQuery)
    then: 'it calls HID`s SELECT internal function'
      1 * hid.call('SELECT', _) >> emptyResponse
    and: 'return empty List'
      res == null
  }

  def "#execute"() {
    given:
      HID hid = GroovySpy(HID, constructorArgs:[proxy])
      String procedure = 'SD_DOCUMENTS_PKG.SD_DOCUMENTS_PUT'
      Date          current  = new Date()
      LocalDateTime now      = DateTimeUtil.parseIsoNoTZ('2020-01-01T00:00:00')
      ZonedDateTime local    = DateTimeUtil.parseIso('2020-01-01T00:00:00+03:00')
      Map args = [
        num_N_DOC_ID      : null,
        num_N_OWNER_ID    : 'NULL',
        vch_VC_DOC_NAME   : '',
        num_N_BASE_DOC_ID : 123,
        num_N_PAR_DOC_ID  : '345',
        num_N_PRICE       : '567.90',
        vch_VC_CODE       : 'Some code',
        dt_D_BEGIN        : current,
        dt_D_END          : now,
        dt_D_OPER         : local
      ]
      Map input = [
        num_N_DOC_ID      : 'NULL',
        num_N_OWNER_ID    : 'NULL',
        vch_VC_DOC_NAME   : 'NULL',
        num_N_BASE_DOC_ID : 123,
        num_N_PAR_DOC_ID  : 345,
        num_N_PRICE       : 567.90,
        vch_VC_CODE       : 'Some code',
        dt_D_BEGIN        : current,
        dt_D_END          : Oracle.encodeDate(now),
        dt_D_OPER         : Oracle.encodeDate(local)
      ]
      Map result = [
        num_N_DOC_ID      : null,
        num_N_OWNER_ID    : null,
        vch_VC_DOC_NAME   : null,
        num_N_BASE_DOC_ID : 123,
        num_N_PAR_DOC_ID  : 345,
        num_N_PRICE       : 567.90,
        vch_VC_CODE       : 'Some code',
        dt_D_BEGIN        : current,
        dt_D_END          : Oracle.encodeDate(now),
        dt_D_OPER         : Oracle.encodeDate(local)
      ]
    when: 'execute query'
      Map res = hid.execute(procedure, args)
    then: 'it calls HID`s procedure with passed args'
      1 * hid.call(procedure, [input]) >> result
    and: 'return data as Map with input and output values'
      res == result
    when: 'execute query using named args overload'
      res = hid.execute(procedure, *: args)
    then: 'it calls HID`s procedure with passed args'
      1 * hid.call(procedure, [input]) >> result
    and: 'return data as Map with input and output values'
      res == result
  }
}