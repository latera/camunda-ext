package org.camunda.latera.bss.connectors.hid

import org.camunda.latera.bss.connectors.HID
import org.camunda.latera.bss.internal.Version
import org.camunda.latera.bss.utils.Constants
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
      String password = 'DOC_TYPE_Unknow'
      String ip = '255.255.255.255'
      String app = 'NETSERV_ISP_OFFICE'
      String appId = 'test'
    when: 'Call without arguments'
      hydra.mainInit()
    then: 'It calls procedure at least once'
      (1.._) * hid.execute('MAIN.INIT', [
        vch_VC_IP        : '127.0.0.1',
        vch_VC_USER      : 'hydra',
        vch_VC_PASS      : null,
        vch_VC_APP_CODE  :'NETSERV_HID',
        vch_VC_CLN_APPID :'HydraOMS'
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
      Integer someFirm = 200
      Hydra hydra = new Hydra(hid)
    when: 'firmId is passed'
      hydra.setFirm(someFirm)
    then: 'procedure will be called with this value'
      1 * hid.execute('MAIN.SET_ACTIVE_FIRM', [
        num_N_FIRM_ID: someFirm
      ])
    when: 'firmId is not passed'
      hydra.setFirm()
    then: 'Fallback to default firmId'
      1 * hid.execute('MAIN.SET_ACTIVE_FIRM', [
        num_N_FIRM_ID: Constants.DEFAULT_FIRM
      ])
  }

  def "#mergeParams"() {
    given:
      Hydra hydra = new Hydra(hid)
    expect:
      hydra.mergeParams(initial, input) == result
    where:
      initial         |input                                                                                                                                   |result
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: null]                                                                                           |[docTypeId: null]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: Constants.DOC_TYPE_ContractAPP]                                                                 |[docTypeId: Constants.DOC_TYPE_ContractAPP]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[                                            docType: null]                                                 |[docTypeId: Constants.DOC_TYPE_BaseContract,  docType: null]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: null,                            docType: null]                                                 |[docTypeId: null, docType: null]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: Constants.DOC_TYPE_ContractAPP,  docType: null]                                                 |[docTypeId: Constants.DOC_TYPE_ContractAPP,  docType: null]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[                                            docType:              'DOC_TYPE_SubscriberContract']           |[docTypeId: Constants.DOC_TYPE_SubscriberContract]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: null,                            docType:              'DOC_TYPE_SubscriberContract']           |[docTypeId: Constants.DOC_TYPE_SubscriberContract]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: Constants.DOC_TYPE_ContractAPP,  docType:              'DOC_TYPE_SubscriberContract']           |[docTypeId: Constants.DOC_TYPE_SubscriberContract]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[                                            docType: ['=':        'DOC_TYPE_SubscriberContract']]          |[docTypeId: ['=':        Constants.DOC_TYPE_SubscriberContract]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: null,                            docType: ['!=':       'DOC_TYPE_SubscriberContract']]          |[docTypeId: ['!=':       Constants.DOC_TYPE_SubscriberContract]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[                                            docType: ['=':         Constants.DOC_TYPE_SubscriberContract]] |[docTypeId: ['=':        Constants.DOC_TYPE_SubscriberContract]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: null,                            docType: ['!=':        Constants.DOC_TYPE_SubscriberContract]] |[docTypeId: ['!=':       Constants.DOC_TYPE_SubscriberContract]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[                                            docType: ['=':        'DOC_TYPE_Unknow']]                      |[docTypeId: ['=':       'DOC_TYPE_Unknow']]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: null,                            docType: ['!=':       'DOC_TYPE_Unknow']]                      |[docTypeId: ['!=':      'DOC_TYPE_Unknow']]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[                                            docType: ['<':         Constants.DOC_TYPE_SubscriberContract]] |[docTypeId: ['<':        Constants.DOC_TYPE_SubscriberContract]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[                                            docType: ['<=':        Constants.DOC_TYPE_SubscriberContract]] |[docTypeId: ['<=':       Constants.DOC_TYPE_SubscriberContract]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[                                            docType: ['<':        'DOC_TYPE_Unknow']]                      |[docTypeId: ['<':       'DOC_TYPE_Unknow']]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[                                            docType: ['<=':       'DOC_TYPE_Unknow']]                      |[docTypeId: ['<=':      'DOC_TYPE_Unknow']]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: null,                            docType: ['>':         Constants.DOC_TYPE_SubscriberContract]] |[docTypeId: ['>':        Constants.DOC_TYPE_SubscriberContract]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: null,                            docType: ['>=':        Constants.DOC_TYPE_SubscriberContract]] |[docTypeId: ['>=':       Constants.DOC_TYPE_SubscriberContract]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: null,                            docType: ['>':        'DOC_TYPE_Unknow']]                      |[docTypeId: ['>':       'DOC_TYPE_Unknow']]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: null,                            docType: ['>=':       'DOC_TYPE_Unknow']]                      |[docTypeId: ['>=':      'DOC_TYPE_Unknow']]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[                                            docType: [is:          null]]                                  |[docTypeId: [is:         null]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: null,                            docType: ['is not':    null]]                                  |[docTypeId: ['is not':   null]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[                                            docType: [is:         'null']]                                 |[docTypeId: [is:        'null']]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: null,                            docType: ['is not':   'null']]                                 |[docTypeId: ['is not':  'null']]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[                                            docType: [in:         ['DOC_TYPE_SubscriberContract']]]        |[docTypeId: [in:        [Constants.DOC_TYPE_SubscriberContract]]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: null,                            docType: [in:         ['DOC_TYPE_SubscriberContract']]]        |[docTypeId: [in:        [Constants.DOC_TYPE_SubscriberContract]]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: Constants.DOC_TYPE_ContractAPP,  docType: [in:         ['DOC_TYPE_SubscriberContract']]]        |[docTypeId: [in:        [Constants.DOC_TYPE_SubscriberContract]]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[                                            docType: [in:         [Constants.DOC_TYPE_SubscriberContract]]]|[docTypeId: [in:        [Constants.DOC_TYPE_SubscriberContract]]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: null,                            docType: [in:         [Constants.DOC_TYPE_SubscriberContract]]]|[docTypeId: [in:        [Constants.DOC_TYPE_SubscriberContract]]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: Constants.DOC_TYPE_ContractAPP,  docType: [in:         [Constants.DOC_TYPE_SubscriberContract]]]|[docTypeId: [in:        [Constants.DOC_TYPE_SubscriberContract]]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[                                            docType: [in:         ['DOC_TYPE_Unknow']]]                    |[docTypeId: [in:       ['DOC_TYPE_Unknow']]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: null,                            docType: [in:         ['DOC_TYPE_Unknow']]]                    |[docTypeId: [in:       ['DOC_TYPE_Unknow']]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: Constants.DOC_TYPE_ContractAPP,  docType: [in:         ['DOC_TYPE_Unknow']]]                    |[docTypeId: [in:       ['DOC_TYPE_Unknow']]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[                                            docType: ['not in':   ['DOC_TYPE_SubscriberContract']]]        |[docTypeId: ['not in':  [Constants.DOC_TYPE_SubscriberContract]]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: null,                            docType: ['not in':   ['DOC_TYPE_SubscriberContract']]]        |[docTypeId: ['not in':  [Constants.DOC_TYPE_SubscriberContract]]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: Constants.DOC_TYPE_ContractAPP,  docType: ['not in':   ['DOC_TYPE_SubscriberContract']]]        |[docTypeId: ['not in':  [Constants.DOC_TYPE_SubscriberContract]]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[                                            docType: ['not in':   [Constants.DOC_TYPE_SubscriberContract]]]|[docTypeId: ['not in':  [Constants.DOC_TYPE_SubscriberContract]]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: null,                            docType: ['not in':   [Constants.DOC_TYPE_SubscriberContract]]]|[docTypeId: ['not in':  [Constants.DOC_TYPE_SubscriberContract]]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: Constants.DOC_TYPE_ContractAPP,  docType: ['not in':   [Constants.DOC_TYPE_SubscriberContract]]]|[docTypeId: ['not in':  [Constants.DOC_TYPE_SubscriberContract]]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[                                            docType: ['not in':   ['DOC_TYPE_Unknow']]]                    |[docTypeId: ['not in': ['DOC_TYPE_Unknow']]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: null,                            docType: ['not in':   ['DOC_TYPE_Unknow']]]                    |[docTypeId: ['not in': ['DOC_TYPE_Unknow']]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: Constants.DOC_TYPE_ContractAPP,  docType: ['not in':   ['DOC_TYPE_Unknow']]]                    |[docTypeId: ['not in': ['DOC_TYPE_Unknow']]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[                                            docType: [like:       'DOC_TYPE_SubscriberContract']]          |[docTypeId: [like:       'DOC_TYPE_SubscriberContract']]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: null,                            docType: [like:       'DOC_TYPE_SubscriberContract%']]         |[docTypeId: [like:       'DOC_TYPE_SubscriberContract%']]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: Constants.DOC_TYPE_ContractAPP,  docType: [like:       'DOC_TYPE_SubscriberContract%']]         |[docTypeId: [like:       'DOC_TYPE_SubscriberContract%']]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[                                            docType: ['not like': 'DOC_TYPE_SubscriberContract%']]         |[docTypeId: ['not like': 'DOC_TYPE_SubscriberContract%']]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: null,                            docType: ['not like': 'DOC_TYPE_SubscriberContract%']]         |[docTypeId: ['not like': 'DOC_TYPE_SubscriberContract%']]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[docTypeId: Constants.DOC_TYPE_ContractAPP,  docType: ['not like': 'DOC_TYPE_SubscriberContract%']]         |[docTypeId: ['not like': 'DOC_TYPE_SubscriberContract%']]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[                                            docType: ["in     (${Constants.DOC_TYPE_SubscriberContract})"]]|[docTypeId: ["in     (${Constants.DOC_TYPE_SubscriberContract})"]]
      [docTypeId: Constants.DOC_TYPE_BaseContract]|[                                            docType: ["not in (${Constants.DOC_TYPE_SubscriberContract})"]]|[docTypeId: ["not in (${Constants.DOC_TYPE_SubscriberContract})"]]
  }

  def "#getLangId"() {
    given:
      Hydra hydra = new Hydra(hid)
    expect:
      hydra.getLangId()     == Constants.LANG_Ru
      hydra.getLangId('ru') == Constants.LANG_Ru
      hydra.getLangId('en') == Constants.LANG_En
  }
}