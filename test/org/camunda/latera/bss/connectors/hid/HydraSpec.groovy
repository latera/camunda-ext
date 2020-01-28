package org.camunda.latera.bss.connectors.hid

import org.camunda.latera.bss.connectors.HID
import org.camunda.latera.bss.internal.Version
import spock.lang.*

class HydraSpec extends Specification {
  @Shared def hid

  def setup() {
    hid = Mock(HID)
  }

  def "#mainInit"() {
    given:
      Hydra hydra = new Hydra(hid)
      String user = 'HyDrA'
      String password = '123'
      String ip = '255.255.255.255'
      String app = 'NETSERV_ISP_OFFICE'
      String appId = 'test'
    when: 'Call without arguments'
      hydra.mainInit()
    then: 'It calls procedure at least once'
      (1.._) * hid.execute('MAIN.INIT', [
        'vch_VC_IP'        : '127.0.0.1',
        'vch_VC_USER'      : 'hydra',
        'vch_VC_PASS'      : null,
        'vch_VC_APP_CODE'  :'NETSERV_HID',
        'vch_VC_CLN_APPID' :'HydraOMS'
      ])
    when: 'Set custom username'
      hydra.mainInit(user: user)
    then: 'It is being passed to procedure'
      1 * hid.execute('MAIN.INIT', { it.vch_VC_USER == user })
    when: 'Set custom password'
      hydra.mainInit(password: password)
    then: 'It is being passed to procedure'
      1 * hid.execute('MAIN.INIT', { it.vch_VC_PASS == password })
    when: 'Set custom IP'
      hydra.mainInit(ip: ip)
    then: 'It is being passed to procedure'
      1 * hid.execute('MAIN.INIT', { it.vch_VC_IP == ip })
    when: 'Set custom application'
      hydra.mainInit(app: app)
      hydra.mainInit(appCode: app)
    then:
      2 * hid.execute('MAIN.INIT', { it.vch_VC_APP_CODE == app })
    when: 'Set custom app description'
      hydra.mainInit(appId: appId)
    then: 'It is being passed to procedure'
      1 * hid.execute('MAIN.INIT', { it.vch_VC_CLN_APPID == appId })
  }

  def "#setFirm"() {
    given:
      Hydra hydra = new Hydra(hid)
    when: 'firmId is passed'
      hydra.setFirm(200)
    then: 'procedure will be called with this value'
      1 * hid.execute('MAIN.SET_ACTIVE_FIRM', [
        num_N_FIRM_ID: 200
      ])
    when: 'firmId is not passed'
      hydra.setFirm()
    then: 'Fallback to default firmId'
      1 * hid.execute('MAIN.SET_ACTIVE_FIRM', [
        num_N_FIRM_ID: 100
      ])
  }

  def "#mergeParams"() {
    given:
      Hydra hydra = Spy(Hydra, constructorArgs: [hid]) {
        getRefIdByCode('DOC_TYPE_Contract') >> 123
      }
    expect:
      hydra.mergeParams(initial, input) == result
    where:
      initial         |input                                                          |result
      [docTypeId: 234]|[docTypeId: null]                                              |[docTypeId: null]
      [docTypeId: 234]|[docTypeId: 345]                                               |[docTypeId: 345]
      [docTypeId: 234]|[                 docType: null]                               |[docTypeId: 234,  docType: null]
      [docTypeId: 234]|[docTypeId: null, docType: null]                               |[docTypeId: null, docType: null]
      [docTypeId: 234]|[docTypeId: 345,  docType: null]                               |[docTypeId: 345,  docType: null]
      [docTypeId: 234]|[                 docType: 'DOC_TYPE_Contract']                |[docTypeId: 123]
      [docTypeId: 234]|[docTypeId: null, docType: 'DOC_TYPE_Contract']                |[docTypeId: 123]
      [docTypeId: 234]|[docTypeId: 345,  docType: 'DOC_TYPE_Contract']                |[docTypeId: 123]
      [docTypeId: 234]|[                 docType: ['=':        'DOC_TYPE_Contract']]  |[docTypeId: ['=':        123]]
      [docTypeId: 234]|[docTypeId: null, docType: ['!=':       'DOC_TYPE_Contract']]  |[docTypeId: ['!=':       123]]
      [docTypeId: 234]|[                 docType: ['=':         123]]                 |[docTypeId: ['=':        123]]
      [docTypeId: 234]|[docTypeId: null, docType: ['!=':        123]]                 |[docTypeId: ['!=':       123]]
      [docTypeId: 234]|[                 docType: ['=':        '123']]                |[docTypeId: ['=':       '123']]
      [docTypeId: 234]|[docTypeId: null, docType: ['!=':       '123']]                |[docTypeId: ['!=':      '123']]
      [docTypeId: 234]|[                 docType: ['<':         123]]                 |[docTypeId: ['<':        123]]
      [docTypeId: 234]|[                 docType: ['<=':        123]]                 |[docTypeId: ['<=':       123]]
      [docTypeId: 234]|[                 docType: ['<':        '123']]                |[docTypeId: ['<':       '123']]
      [docTypeId: 234]|[                 docType: ['<=':       '123']]                |[docTypeId: ['<=':      '123']]
      [docTypeId: 234]|[docTypeId: null, docType: ['>':         123]]                 |[docTypeId: ['>':        123]]
      [docTypeId: 234]|[docTypeId: null, docType: ['>=':        123]]                 |[docTypeId: ['>=':       123]]
      [docTypeId: 234]|[docTypeId: null, docType: ['>':        '123']]                |[docTypeId: ['>':       '123']]
      [docTypeId: 234]|[docTypeId: null, docType: ['>=':       '123']]                |[docTypeId: ['>=':      '123']]
      [docTypeId: 234]|[                 docType: [is:          null]]                |[docTypeId: [is:         null]]
      [docTypeId: 234]|[docTypeId: null, docType: ['is not':    null]]                |[docTypeId: ['is not':   null]]
      [docTypeId: 234]|[                 docType: [is:         'null']]               |[docTypeId: [is:        'null']]
      [docTypeId: 234]|[docTypeId: null, docType: ['is not':   'null']]               |[docTypeId: ['is not':  'null']]
      [docTypeId: 234]|[                 docType: [in:         ['DOC_TYPE_Contract']]]|[docTypeId: [in:        [123]]]
      [docTypeId: 234]|[docTypeId: null, docType: [in:         ['DOC_TYPE_Contract']]]|[docTypeId: [in:        [123]]]
      [docTypeId: 234]|[docTypeId: 345,  docType: [in:         ['DOC_TYPE_Contract']]]|[docTypeId: [in:        [123]]]
      [docTypeId: 234]|[                 docType: [in:         [123]]]                |[docTypeId: [in:        [123]]]
      [docTypeId: 234]|[docTypeId: null, docType: [in:         [123]]]                |[docTypeId: [in:        [123]]]
      [docTypeId: 234]|[docTypeId: 345,  docType: [in:         [123]]]                |[docTypeId: [in:        [123]]]
      [docTypeId: 234]|[                 docType: [in:        ['123']]]               |[docTypeId: [in:       ['123']]]
      [docTypeId: 234]|[docTypeId: null, docType: [in:        ['123']]]               |[docTypeId: [in:       ['123']]]
      [docTypeId: 234]|[docTypeId: 345,  docType: [in:        ['123']]]               |[docTypeId: [in:       ['123']]]
      [docTypeId: 234]|[                 docType: ['not in':   ['DOC_TYPE_Contract']]]|[docTypeId: ['not in':  [123]]]
      [docTypeId: 234]|[docTypeId: null, docType: ['not in':   ['DOC_TYPE_Contract']]]|[docTypeId: ['not in':  [123]]]
      [docTypeId: 234]|[docTypeId: 345,  docType: ['not in':   ['DOC_TYPE_Contract']]]|[docTypeId: ['not in':  [123]]]
      [docTypeId: 234]|[                 docType: ['not in':   [123]]]                |[docTypeId: ['not in':  [123]]]
      [docTypeId: 234]|[docTypeId: null, docType: ['not in':   [123]]]                |[docTypeId: ['not in':  [123]]]
      [docTypeId: 234]|[docTypeId: 345,  docType: ['not in':   [123]]]                |[docTypeId: ['not in':  [123]]]
      [docTypeId: 234]|[                 docType: ['not in':  ['123']]]               |[docTypeId: ['not in': ['123']]]
      [docTypeId: 234]|[docTypeId: null, docType: ['not in':  ['123']]]               |[docTypeId: ['not in': ['123']]]
      [docTypeId: 234]|[docTypeId: 345,  docType: ['not in':  ['123']]]               |[docTypeId: ['not in': ['123']]]
      [docTypeId: 234]|[                 docType: [like:       'DOC_TYPE_Contract']]  |[docTypeId: [like:       'DOC_TYPE_Contract']]
      [docTypeId: 234]|[docTypeId: null, docType: [like:       'DOC_TYPE_Contract%']] |[docTypeId: [like:       'DOC_TYPE_Contract%']]
      [docTypeId: 234]|[docTypeId: 345,  docType: [like:       'DOC_TYPE_Contract%']] |[docTypeId: [like:       'DOC_TYPE_Contract%']]
      [docTypeId: 234]|[                 docType: ['not like': 'DOC_TYPE_Contract%']] |[docTypeId: ['not like': 'DOC_TYPE_Contract%']]
      [docTypeId: 234]|[docTypeId: null, docType: ['not like': 'DOC_TYPE_Contract%']] |[docTypeId: ['not like': 'DOC_TYPE_Contract%']]
      [docTypeId: 234]|[docTypeId: 345,  docType: ['not like': 'DOC_TYPE_Contract%']] |[docTypeId: ['not like': 'DOC_TYPE_Contract%']]
      [docTypeId: 234]|[                 docType: ['in     (123)']]                   |[docTypeId: ['in     (123)']]
      [docTypeId: 234]|[                 docType: ['not in (123)']]                   |[docTypeId: ['not in (123)']]
  }

  def "#getLangId with passed locale"() {
    given:
      Hydra hydra = Spy(Hydra, constructorArgs: [hid]) {
        getRefIdByCode('LANG_Russian') >> 123
        getRefIdByCode('LANG_English') >> 234
        getRefIdByCode('LANG_Es')      >> 345
      }
    expect:
      hydra.getLangId(locale) == result
    where:
      locale    |result
      'russian' |123
      'ru'      |123
      'english' |234
      'en'      |234
      'es'      |345
      'unknown' |null
  }

  def "#getLangId without default argument"() {
    given:
      Hydra hydra = Spy(Hydra, constructorArgs: [hid]) {
        getRefIdByCode('LANG_Russian') >> 123
      }
    expect:
      hydra.getLangId() == 123
  }
}