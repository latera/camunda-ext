package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.connectors.HID
import org.camunda.latera.bss.connectors.hid.Hydra
import org.camunda.latera.bss.utils.DateTimeUtil
import org.camunda.latera.bss.utils.Constants
import spock.lang.*

class AddParamSpec extends Specification {
  @Shared Integer STRING_ADD_PARAM  = 1234
  @Shared Integer BOOLEAN_ADD_PARAM = 2345
  @Shared Integer INTEGER_ADD_PARAM = 3456
  @Shared Integer FLOAT_ADD_PARAM   = 4567
  @Shared Integer DATE_ADD_PARAM    = 5678
  @Shared Integer REF_ADD_PARAM     = 6789
  @Shared def hid
  @Shared def now

  def setup() {
    hid = Mock(HID)
    now = DateTimeUtil.parseDateTimeAny('01.01.2020 00:00:00')
  }

  def "#getAddParamDataType(Map)"() {
    given:
      Hydra hydra = new Hydra(hid)
    expect:
      hydra.getAddParamDataType(n_data_type_id: dataTypeId) == dataType
    where:
      dataTypeId                 |dataType
      Constants.DATA_TYPE_Varchar|'string'
      Constants.DATA_TYPE_Flag   |'bool'
      Constants.DATA_TYPE_Integer|'number'
      Constants.DATA_TYPE_Number |'number'
      Constants.DATA_TYPE_Date   |'date'
      Constants.DATA_TYPE_Ref    |'refId'
  }

  def "#getAddParamDataType(Map,def)"() {
    given:
      Hydra hydra = new Hydra(hid)
    expect:
      hydra.getAddParamDataType(value, n_data_type_id: dataTypeId) == result
    where:
      dataTypeId                 |value                                ||result
      Constants.DATA_TYPE_Varchar|'123'                                ||['string',                                 '123']
      Constants.DATA_TYPE_Varchar|null                                 ||['string',                                  null]
      Constants.DATA_TYPE_Flag   |true                                 ||['bool',                                    true]
      Constants.DATA_TYPE_Flag   |false                                ||['bool',                                   false]
      Constants.DATA_TYPE_Flag   |null                                 ||['bool',                                    null]
      Constants.DATA_TYPE_Integer|123                                  ||['number',                                   123]
      Constants.DATA_TYPE_Integer|null                                 ||['number',                                  null]
      Constants.DATA_TYPE_Number |123.00                               ||['number',                                123.00]
      Constants.DATA_TYPE_Number |null                                 ||['number',                                  null]
      Constants.DATA_TYPE_Date   |now                                  ||['date',                                     now]
      Constants.DATA_TYPE_Date   |null                                 ||['date',                                    null]
      Constants.DATA_TYPE_Ref    |Constants.DOC_TYPE_SubscriberContract||['refId',  Constants.DOC_TYPE_SubscriberContract]
      Constants.DATA_TYPE_Ref    |'DOC_TYPE_SubscriberContract'        ||['ref',            'DOC_TYPE_SubscriberContract']
      Constants.DATA_TYPE_Ref    |null                                 ||['ref',                                     null]
  }

  def "#getAddParamValue"() {
    given:
      Hydra hydra = Spy(Hydra, constructorArgs: [hid]) {
        getDocumentAddParamType(STRING_ADD_PARAM)  >> [n_data_type_id: Constants.DATA_TYPE_Varchar]
        getDocumentAddParamType(BOOLEAN_ADD_PARAM) >> [n_data_type_id: Constants.DATA_TYPE_Flag]
        getDocumentAddParamType(INTEGER_ADD_PARAM) >> [n_data_type_id: Constants.DATA_TYPE_Integer]
        getDocumentAddParamType(FLOAT_ADD_PARAM)   >> [n_data_type_id: Constants.DATA_TYPE_Number]
        getDocumentAddParamType(DATE_ADD_PARAM)    >> [n_data_type_id: Constants.DATA_TYPE_Date]
        getDocumentAddParamType(REF_ADD_PARAM)     >> [n_data_type_id: Constants.DATA_TYPE_Ref]
      }
    expect:
      hydra.getAddParamValue(value, withType, visualRefValue) == result
      hydra.getAddParamValue(value, withType: withType, visualRefValue: visualRefValue) == result
    where:
      value                                                                                |withType|visualRefValue||result
      [n_doc_value_type_id: STRING_ADD_PARAM, vc_value: '123'                             ]|false   |false         ||'123'
      [n_doc_value_type_id: STRING_ADD_PARAM, vc_value: null                              ]|false   |false         ||null
      [n_doc_value_type_id: STRING_ADD_PARAM, vc_value: '123'                             ]|true    |false         ||['123', 'string']
      [n_doc_value_type_id: STRING_ADD_PARAM, vc_value: null                              ]|true    |false         ||[null,  'string']

      [n_doc_value_type_id: BOOLEAN_ADD_PARAM, c_fl_value: 'Y'                            ]|false   |false         ||true
      [n_doc_value_type_id: BOOLEAN_ADD_PARAM, c_fl_value: 'N'                            ]|false   |false         ||false
      [n_doc_value_type_id: BOOLEAN_ADD_PARAM, c_fl_value: null                           ]|false   |false         ||false
      [n_doc_value_type_id: BOOLEAN_ADD_PARAM, c_fl_value: 'Y'                            ]|true    |false         ||[true,  'bool']
      [n_doc_value_type_id: BOOLEAN_ADD_PARAM, c_fl_value: 'N'                            ]|true    |false         ||[false, 'bool']
      [n_doc_value_type_id: BOOLEAN_ADD_PARAM, c_fl_value: null                           ]|true    |false         ||[false, 'bool']

      [n_doc_value_type_id: INTEGER_ADD_PARAM, n_value: '123'                             ]|false   |false         ||123
      [n_doc_value_type_id: INTEGER_ADD_PARAM, n_value: null,                             ]|false   |false         ||null
      [n_doc_value_type_id: INTEGER_ADD_PARAM, n_value: '123'                             ]|true    |false         ||[123,  'number']
      [n_doc_value_type_id: INTEGER_ADD_PARAM, n_value: null                              ]|true    |false         ||[null, 'number']

      [n_doc_value_type_id: FLOAT_ADD_PARAM, n_value: '123.00'                            ]|false   |false         ||123.00
      [n_doc_value_type_id: FLOAT_ADD_PARAM, n_value: '123,00'                            ]|false   |false         ||123.00
      [n_doc_value_type_id: FLOAT_ADD_PARAM, n_value: null                                ]|false   |false         ||null
      [n_doc_value_type_id: FLOAT_ADD_PARAM, n_value: '123.00'                            ]|true    |false         ||[123.00, 'number']
      [n_doc_value_type_id: FLOAT_ADD_PARAM, n_value: '123,00'                            ]|true    |false         ||[123.00, 'number']
      [n_doc_value_type_id: FLOAT_ADD_PARAM, n_value: null                                ]|true    |false         ||[null,   'number']

      [n_doc_value_type_id: DATE_ADD_PARAM, d_value: now                                  ]|false   |false         ||now
      [n_doc_value_type_id: DATE_ADD_PARAM, d_value: null                                 ]|false   |false         ||null
      [n_doc_value_type_id: DATE_ADD_PARAM, d_value: now                                  ]|true    |false         ||[now,  'date']
      [n_doc_value_type_id: DATE_ADD_PARAM, d_value: null                                 ]|true    |false         ||[null, 'date']

      [n_doc_value_type_id: REF_ADD_PARAM, n_ref_id: Constants.DOC_TYPE_SubscriberContract]|false   |false         ||Constants.DOC_TYPE_SubscriberContract
      [n_doc_value_type_id: REF_ADD_PARAM, n_ref_id: null                                 ]|false   |false         ||null
      [n_doc_value_type_id: REF_ADD_PARAM, n_ref_id: Constants.DOC_TYPE_SubscriberContract]|true    |false         ||[Constants.DOC_TYPE_SubscriberContract, 'refId']
      [n_doc_value_type_id: REF_ADD_PARAM, n_ref_id: null                                 ]|true    |false         ||[null, 'refId']
      [n_doc_value_type_id: REF_ADD_PARAM, vc_visual_value: 'DOC_TYPE_SubscriberContract' ]|false   |true          ||'DOC_TYPE_SubscriberContract'
      [n_doc_value_type_id: REF_ADD_PARAM, vc_visual_value: null                          ]|false   |true          ||null
      [n_doc_value_type_id: REF_ADD_PARAM, vc_visual_value: 'DOC_TYPE_SubscriberContract' ]|true    |true          ||['DOC_TYPE_SubscriberContract', 'refId']
      [n_doc_value_type_id: REF_ADD_PARAM, vc_visual_value: null                          ]|true    |true          ||[null,   'refId']
  }

}