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
import org.camunda.latera.bss.connectors.hid.hydra.Invoice
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
import org.camunda.latera.bss.internal.Version

class Hydra implements Ref, Message, DataType, AddParam, Good, Document, Contract, PriceOrder, Invoice, Bill, Subject, Company, Person, Reseller, Group, Customer, Account, Subscription, Equipment, Region, Address, Param, Search {
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
  private Version version
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
    setVersion(params.version)
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
            ['in', 'not in'].each { CharSequence operator ->
              // addressTypeId: ..., addressType: [in: ['ADDR_TYPE_IP', 'ADDR_TYPE_IP6'], 'not in': ['ADDR_TYPE_Subnet']]
              if (where.containsKey(operator) && isList(where[operator])) {
                List newWhere = []
                where[operator].each { CharSequence code ->
                  newWhere << getRefIdByCode(code)  // -> addressTypeId: [in: [123, 456], 'not in': [789]]
                }
                where[operator] = newWhere
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

  Number getLangId() {
    String langCode = getLocale()
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

  void setVersion(Version version = null) {
    if (version == null) {
      this.version = null
      getVersion()
    } else {
      this.version = version
    }
  }

  Version getVersion() {
    if (this.version != null) {
      return this.version
    }
    try {
      LinkedHashMap result = hid.execute('MAIN.GET_DB_VERSION', [
        num_HydraVersion    : null,
        num_MajorVersion    : null,
        num_MinorVersion    : null,
        num_Modification    : null,
        vch_Revision        : null,
        dt_InstallationDate : null
      ])
      this.version = new Version(result.num_HydraVersion, result.num_MajorVersion, result.num_MinorVersion, result.num_Modification)
      return this.version

    } catch (Exception e){
      logger.error_oracle(e)
      return null
    }
  }
  //Other methods are imported from traits
}