package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.connectors.HID
import org.camunda.latera.bss.connectors.hid.Hydra
import org.camunda.latera.bss.utils.DateTimeUtil
import spock.lang.*

class AddParamSpec extends Specification {
  @Shared def hid
  @Shared def now

  def setup() {
    hid = Mock(HID)
    now = DateTimeUtil.parseDateTimeAny('01.01.2020 00:00:00')
  }

  def "#getAddParamDataType(Map)"() {
    given:
      Hydra hydra = Spy(Hydra, constructorArgs: [hid]) {
        getStringTypeId()  >>  123
        getBooleanTypeId() >>  234
        getIntegerTypeId() >>  345
        getFloatTypeId()   >>  456
        getDateTypeId()    >>  567
        getRefTypeId()     >>  678
      }
    expect:
      hydra.getAddParamDataType(n_data_type_id: dataTypeId) == dataType
    where:
      dataTypeId|dataType
      123       |'string'
      234       |'bool'
      345       |'number'
      456       |'number'
      567       |'date'
      678       |'refId'
  }

  def "#getAddParamDataType(Map,def)"() {
    given:
      Hydra hydra = Spy(Hydra, constructorArgs: [hid]) {
        getStringTypeId()  >>  123
        getBooleanTypeId() >>  234
        getIntegerTypeId() >>  345
        getFloatTypeId()   >>  456
        getDateTypeId()    >>  567
        getRefTypeId()     >>  678
      }
    expect:
      hydra.getAddParamDataType(value, n_data_type_id: dataTypeId) == result
    where:
      dataTypeId|value ||result
      123       |'123' ||['string',  '123']
      123       |null  ||['string',   null]
      234       |true  ||['bool',     true]
      234       |false ||['bool',    false]
      234       |null  ||['bool',     null]
      345       |123   ||['number',    123]
      345       |null  ||['number',   null]
      456       |123.00||['number', 123.00]
      456       |null  ||['number',   null]
      567       |now   ||['date',      now]
      567       |null  ||['date',     null]
      678       |123101||['refId',  123101]
      678       |'code'||['ref',    'code']
      678       |null  ||['ref',      null]
  }

  def "#getAddParamValue"() {
    given:
      Hydra hydra = Spy(Hydra, constructorArgs: [hid]) {
        getStringTypeId()  >>  123
        getBooleanTypeId() >>  234
        getIntegerTypeId() >>  345
        getFloatTypeId()   >>  456
        getDateTypeId()    >>  567
        getRefTypeId()     >>  678

        getDocumentAddParamType(1234)  >> [n_data_type_id: 123]
        getDocumentAddParamType(2345)  >> [n_data_type_id: 234]
        getDocumentAddParamType(3456)  >> [n_data_type_id: 345]
        getDocumentAddParamType(4567)  >> [n_data_type_id: 456]
        getDocumentAddParamType(5678)  >> [n_data_type_id: 567]
        getDocumentAddParamType(6789)  >> [n_data_type_id: 678]
      }
    expect:
      hydra.getAddParamValue(value, withType, visualRefValue) == result
      hydra.getAddParamValue(value, withType: withType, visualRefValue: visualRefValue) == result
    where:
      value                                                |withType|visualRefValue||result
      [n_doc_value_type_id: 1234, vc_value: '123'         ]|false   |false         ||'123'
      [n_doc_value_type_id: 1234, vc_value: null          ]|false   |false         ||null
      [n_doc_value_type_id: 1234, vc_value: '123'         ]|true    |false         ||['123', 'string']
      [n_doc_value_type_id: 1234, vc_value: null          ]|true    |false         ||[null,  'string']

      [n_doc_value_type_id: 2345, c_fl_value: 'Y'         ]|false   |false         ||true
      [n_doc_value_type_id: 2345, c_fl_value: 'N'         ]|false   |false         ||false
      [n_doc_value_type_id: 2345, c_fl_value: null        ]|false   |false         ||false
      [n_doc_value_type_id: 2345, c_fl_value: 'Y'         ]|true    |false         ||[true,  'bool']
      [n_doc_value_type_id: 2345, c_fl_value: 'N'         ]|true    |false         ||[false, 'bool']
      [n_doc_value_type_id: 2345, c_fl_value: null        ]|true    |false         ||[false, 'bool']

      [n_doc_value_type_id: 3456, n_value: '123'          ]|false   |false         ||123
      [n_doc_value_type_id: 3456, n_value: null,          ]|false   |false         ||null
      [n_doc_value_type_id: 3456, n_value: '123'          ]|true    |false         ||[123,  'number']
      [n_doc_value_type_id: 3456, n_value: null           ]|true    |false         ||[null, 'number']

      [n_doc_value_type_id: 4567, n_value: '123.00'       ]|false   |false         ||123.00
      [n_doc_value_type_id: 4567, n_value: '123,00'       ]|false   |false         ||123.00
      [n_doc_value_type_id: 4567, n_value: null           ]|false   |false         ||null
      [n_doc_value_type_id: 4567, n_value: '123.00'       ]|true    |false         ||[123.00, 'number']
      [n_doc_value_type_id: 4567, n_value: '123,00'       ]|true    |false         ||[123.00, 'number']
      [n_doc_value_type_id: 4567, n_value: null           ]|true    |false         ||[null,   'number']

      [n_doc_value_type_id: 5678, d_value: now            ]|false   |false         ||now
      [n_doc_value_type_id: 5678, d_value: null           ]|false   |false         ||null
      [n_doc_value_type_id: 5678, d_value: now            ]|true    |false         ||[now,  'date']
      [n_doc_value_type_id: 5678, d_value: null           ]|true    |false         ||[null, 'date']

      [n_doc_value_type_id: 6789, n_ref_id: 1234          ]|false   |false         ||1234
      [n_doc_value_type_id: 6789, n_ref_id: null          ]|false   |false         ||null
      [n_doc_value_type_id: 6789, n_ref_id: 1234          ]|true    |false         ||[1234, 'refId']
      [n_doc_value_type_id: 6789, n_ref_id: null          ]|true    |false         ||[null, 'refId']
      [n_doc_value_type_id: 6789, vc_visual_value: 'some' ]|false   |true          ||'some'
      [n_doc_value_type_id: 6789, vc_visual_value: null   ]|false   |true          ||null
      [n_doc_value_type_id: 6789, vc_visual_value: 'some' ]|true    |true          ||['some', 'refId']
      [n_doc_value_type_id: 6789, vc_visual_value: null   ]|true    |true          ||[null,   'refId']
  }

}