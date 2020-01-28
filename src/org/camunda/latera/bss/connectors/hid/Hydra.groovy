package org.camunda.latera.bss.connectors.hid

import static org.camunda.latera.bss.utils.StringUtil.capitalize
import static org.camunda.latera.bss.utils.MapUtil.isMap
import static org.camunda.latera.bss.utils.ListUtil.isList
import static org.camunda.latera.bss.utils.StringUtil.isString
import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.latera.bss.logging.SimpleLogger
import org.camunda.latera.bss.connectors.HID
import org.camunda.latera.bss.connectors.hid.hydra.Ref
import org.camunda.latera.bss.connectors.hid.hydra.Message
import org.camunda.latera.bss.connectors.hid.hydra.DataType
import org.camunda.latera.bss.connectors.hid.hydra.AddParam
import org.camunda.latera.bss.connectors.hid.hydra.Good
import org.camunda.latera.bss.connectors.hid.hydra.Document
import org.camunda.latera.bss.connectors.hid.hydra.Contract
import org.camunda.latera.bss.connectors.hid.hydra.PriceOrder
import org.camunda.latera.bss.connectors.hid.hydra.ChargeLog
import org.camunda.latera.bss.connectors.hid.hydra.Bill
import org.camunda.latera.bss.connectors.hid.hydra.Subject
import org.camunda.latera.bss.connectors.hid.hydra.Company
import org.camunda.latera.bss.connectors.hid.hydra.Person
import org.camunda.latera.bss.connectors.hid.hydra.Reseller
import org.camunda.latera.bss.connectors.hid.hydra.Group
import org.camunda.latera.bss.connectors.hid.hydra.Customer
import org.camunda.latera.bss.connectors.hid.hydra.Account
import org.camunda.latera.bss.connectors.hid.hydra.Subscription
import org.camunda.latera.bss.connectors.hid.hydra.Equipment
import org.camunda.latera.bss.connectors.hid.hydra.Region
import org.camunda.latera.bss.connectors.hid.hydra.Address
import org.camunda.latera.bss.connectors.hid.hydra.Param
import org.camunda.latera.bss.connectors.hid.hydra.Search
import org.camunda.latera.bss.connectors.hid.hydra.Tag

class Hydra implements Ref, Message, DataType, AddParam, Good, Document, Contract, PriceOrder, ChargeLog, Bill, Subject, Company, Person, Reseller, Group, Customer, Account, Subscription, Equipment, Region, Address, Param, Search, Tag {
  private static String  DEFAULT_USER   = 'hydra'
  private static Integer DEFAULT_FIRM   = 100
  private static String  DEFAULT_LOCALE = 'ru'
  HID hid
  String user
  private String password
  Number firmId
  Number resellerId
  SimpleLogger logger
  String locale
  Map regionHierarchyOverride

  Hydra(DelegateExecution execution) {
    this(
      new HID(execution),
      logger          : new SimpleLogger(execution),
      user            : execution.getVariable('hydraUser'),
      password        : execution.getVariable('hydraPassword'),
      firmId          : execution.getVariable('hydraFirmId')     ?: execution.getVariable('homsOrderDataFirmId'),
      resellerId      : execution.getVariable('hydraResellerId') ?: execution.getVariable('homsOrderDataResellerId'),
      locale          : execution.getVariable('locale'),
      regionHierarchy : execution.getVariable('regionHierarchy')
    )
  }

  Hydra(Map params = [:], HID hid) {
    this(params + [hid: hid])
  }

  Hydra(Map params = [:]) {
    def ENV         = System.getenv()
    this.hid        = params.hid      ?: new HID()
    this.logger     = params.logger   ?: new SimpleLogger()
    this.user       = params.user     ?: ENV['HYDRA_USER'] ?: DEFAULT_USER
    this.password   = params.password ?: ENV['HYDRA_PASSWORD']
    this.locale     = params.locale   ?: DEFAULT_LOCALE
    this.firmId     = toIntSafe(params.firmId) ?: DEFAULT_FIRM
    this.resellerId = toIntSafe(params.resellerId)
    this.regionHierarchyOverride = params.regionHierarchy

    mainInit()
    setFirm()
  }

  private Map mergeParams(Map initial, Map input) {
    LinkedHashMap params = initial + input

    //If it is set addressType instead of addressTypeId, substitute constants here
    LinkedHashMap result = [:]
    List keysToExclude = []
    params.each{ CharSequence name, def value ->
      // addressTypeId
      def group = (name =~ /^(.*)Id$/)
      if (group.size() > 0) {
        String noIdName = group[0][1] // addressType
        if (params.containsKey(noIdName) && params[noIdName] != null) { // addressTypeId: ..., addressType: 'ADDR_TYPE_IP', excluding addressType: null
          if (isMap(params[noIdName])) { //addrType: [in: ...]
            Map where = params[noIdName]
            ['in', 'not in', '=', '!='].each { CharSequence operator ->
              // addressTypeId: ..., addressType: [in: ['ADDR_TYPE_IP', 'ADDR_TYPE_IP6'], 'not in': ['ADDR_TYPE_Subnet']]
              if (where.containsKey(operator)) {
                if (isList(where[operator])) {
                  List newWhere = []
                  where[operator].each { def val ->
                    def newVal = null
                    if (isString(val)) { //addressType: [in: ['ADDR_TYPE_IP'], ...]
                      newVal = getRefIdByCode(val) // -> addressTypeId: [in: [123, 456], 'not in': [789]]
                    }
                    if (newVal == null) { //addressType: [in: [123], ...]
                      newVal = val // -> addressTypeId: [in: [123], ...] without any changes
                    }
                    newWhere << newVal
                  }
                  where[operator] = newWhere
                } else if (isString(where[operator])) { //addressType: ['!=': 'ADDR_TYPE_IP']
                  def newVal = getRefIdByCode(where[operator])
                  if (newVal != null) {
                    where[operator] = newVal // -> addressTypeId: ['!=': 123]
                  }
                }
              }
            } // overwise addressType: [like: 'A%'] -> addressTypeId: [like: 'A%']
            result[name] = where
          } else if (isString(params[noIdName])) {
            result[name] = getRefIdByCode(params[noIdName]) // addressType: 'ADDR_TYPE_IP' -> addressTypeId: 123
          } else {
            result[name] = params[noIdName] // addressTypeId: ..., addressType: [in: [123, 456]] -> addressTypeId: [in: [123, 456]]
          }
          keysToExclude.add(name) // original addressTypeId
          keysToExclude.add(noIdName) // addressType
        }
      }
    }

    //And then remove non-id key if id was set above
    params.each{ CharSequence name, def value ->
      if (!keysToExclude.contains(name)) {
        result[name] = value
      }
    }
    return result
  }

  Number getLangId(CharSequence langCode = getLocale()) {
    if (langCode == 'en') {
      langCode = 'english'
    } else if (langCode == 'ru') {
      langCode = 'russian'
    }
    return getRefIdByCode("LANG_${capitalize(langCode)}")
  }

  void mainInit(Map input = [:]) {
    LinkedHashMap params = [
      ip       : '127.0.0.1',
      user     : this.user,
      password : this.password,
      app      : 'NETSERV_HID',
      appId    : 'HydraOMS'
    ] + input

    hid.execute('MAIN.INIT', [
      vch_VC_IP        : params.ip,
      vch_VC_USER      : params.user,
      vch_VC_PASS      : params.password,
      vch_VC_APP_CODE  : params.appCode ?: params.app,
      vch_VC_CLN_APPID : params.appId
    ])
  }

  void setFirm(def firmId = getFirmId()) {
    hid.execute('MAIN.SET_ACTIVE_FIRM', [
      num_N_FIRM_ID: firmId
    ])
  }
  //Other methods are imported from traits
}
