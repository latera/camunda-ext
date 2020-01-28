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
}