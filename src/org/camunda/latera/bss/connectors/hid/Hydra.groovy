package org.camunda.latera.bss.connectors.hid

import static org.camunda.latera.bss.utils.StringUtil.capitalize
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
import org.camunda.latera.bss.connectors.hid.hydra.Search
import org.camunda.latera.bss.internal.Version

class Hydra implements Ref, Message, DataType, AddParam, Good, Document, Contract, PriceOrder, Invoice, Bill, Subject, Company, Person, Reseller, Group, Customer, Account, Subscription, Equipment, Region, Address, Search {
  private static Integer DEFAULT_FIRM = 100
  HID hid
  String user
  private String password
  def firmId
  def resellerId
  SimpleLogger logger
  String locale
  Version version
  Map regionHierarchyOverride

  Hydra(DelegateExecution execution) {
    this.logger     = new SimpleLogger(execution)
    this.hid        = new HID(execution)
    def ENV         = System.getenv()

    this.locale     = execution.getVariable('locale')
    this.user       = ENV['HYDRA_USER']     ?: execution.getVariable('hydraUser') ?: 'hydra'
    this.password   = ENV['HYDRA_PASSWORD'] ?: execution.getVariable('hydraPassword')
    this.firmId     = toIntSafe(execution.getVariable('hydraFirmId')     ?: (execution.getVariable('homsOrderDataFirmId') ?: getDefaultFirmId()))
    this.resellerId = toIntSafe(execution.getVariable('hydraResellerId') ?: execution.getVariable('homsOrderDataResellerId'))
    this.regionHierarchyOverride = execution.getVariable('regionHierarchy')

    mainInit(
      user     : this.user,
      password : this.password
    )
    setFirm()
    this.version = getVersion()
  }

  private Map mergeParams(Map initial, Map input) {
    LinkedHashMap params = initial + input

    //If it is set opf instead of opfId, get proper reference ids from Hydra
    LinkedHashMap result = [:]
    List keysToExclude = []
    params.each{ name, value ->
      def group = (name =~ /^(.*)Id$/)
      if (group.size() > 0) {
        String noIdName = group[0][1]
        if (params.containsKey(noIdName) && params[noIdName] != null) {
          result[name] = getRefIdByCode(params[noIdName])
          keysToExclude.add(name)
          keysToExclude.add(noIdName)
        }
      }
    }
    //And then remove non-id key if id was set above
    params.each{ name, value ->
      if (!keysToExclude.contains(name)) {
        result[name] = value
      }
    }
    return result
  }

  Number getDefaultFirmId() {
    return DEFAULT_FIRM
  }

  Number getFirmId() {
    return firmId
  }

  Number getResellerId() {
    return resellerId
  }

  String getLocale() {
    return locale ?: 'ru'
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

  void mainInit(Map input) {
    LinkedHashMap params = [
      ip       : '127.0.0.1',
      user     : null,
      password : null,
      appCode  : 'NETSERV_HID',
      appId    : 'HydraOMS'
    ] + input

    hid.execute('MAIN.INIT', [
      vch_VC_IP       : params.ip,
      vch_VC_USER     : params.user,
      vch_VC_PASS     : params.password,
      vch_VC_APP_CODE : params.appCode,
      vch_VC_CLN_APPID: params.appId
    ])

  }

  void setFirm(def firmId = getFirmId()) {
    hid.execute('MAIN.SET_ACTIVE_FIRM', [
      num_N_FIRM_ID: firmId
    ])
  }

  Version getVersion() {
    try {
      LinkedHashMap result = hid.execute('MAIN.GET_DB_VERSION', [
        num_HydraVersion    : null,
        num_MajorVersion    : null,
        num_MinorVersion    : null,
        num_Modification    : null,
        vch_Revision        : null,
        dt_InstallationDate : null
      ])
      return new Version(result.num_HydraVersion, result.num_MajorVersion, result.num_MinorVersion, result.num_Modification)
    } catch (Exception e){
      logger.error_oracle(e)
      return null
    }
  }
  //Other methods are imported from traits
}