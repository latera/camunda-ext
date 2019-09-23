package org.camunda.latera.bss.connectors.hid

import groovy.net.xmlrpc.*
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

class Hydra implements Ref, Message, DataType, AddParam, Good, Document, Contract, PriceOrder, Invoice, Bill, Subject, Company, Person, Reseller, Group, Customer, Account, Subscription, Equipment, Region, Address, Search {
  private static Integer DEFAULT_FIRM = 100
  HID hid
  String user
  private String password
  def firmId
  def resellerId
  SimpleLogger logger
  String locale
  Map regionHierarchyOverride

  Hydra(DelegateExecution execution) {
    this.logger     = new SimpleLogger(execution)
    this.hid        = new HID(execution)
    def ENV         = System.getenv()

    this.locale     = execution.getVariable('locale')
    this.user       = ENV['HYDRA_USER']     ?: execution.getVariable('hydraUser') ?: 'hydra'
    this.password   = ENV['HYDRA_PASSWORD'] ?: execution.getVariable('hydraPassword')
    this.firmId     = toIntSafe(execution.getVariable('hydraFirmId') ?: (execution.getVariable('homsOrderDataFirmId') ?: getDefaultFirmId()))
    this.resellerId = toIntSafe(execution.getVariable('hydraResellerId') ?: execution.getVariable('homsOrderDataResellerId'))

    this.hid.execute('MAIN.INIT', [
      vch_VC_IP       : '127.0.0.1',
      vch_VC_USER     : this.user,
      vch_VC_PASS     : this.password,
      vch_VC_APP_CODE : 'NETSERV_HID',
      vch_VC_CLN_APPID: 'HydraOMS'
    ])

    this.hid.execute('MAIN.SET_ACTIVE_FIRM', [
      num_N_FIRM_ID: getFirmId()
    ])
  }

  Map mergeParams(Map initial, Map input) {
    LinkedHashMap params = initial + input

    //If it is set opf instead of opfId, get proper reference ids from Hydra
    LinkedHashMap result = [:]
    List keysToExclude = []
    params.each{ name, value ->
      def group = (name =~ /^(.*)Id$/)
      if (group.size() > 0) {
        String noIdName = group[0][1]
        if (params.containsKey(noIdName)) {
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
    return getRefIdByCode("LANG_${capitalize(getLocale())}")
  }
  //Other methods are imported from traits
}