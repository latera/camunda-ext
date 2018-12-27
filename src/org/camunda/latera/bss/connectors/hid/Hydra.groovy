package org.camunda.latera.bss.connectors.hid

import groovy.net.xmlrpc.*
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.latera.bss.logging.SimpleLogger
import org.camunda.latera.bss.connectors.HID
import org.camunda.latera.bss.connectors.hid.hydra.Ref
import org.camunda.latera.bss.connectors.hid.hydra.Good
import org.camunda.latera.bss.connectors.hid.hydra.Document
import org.camunda.latera.bss.connectors.hid.hydra.PriceOrder
import org.camunda.latera.bss.connectors.hid.hydra.PriceLine
import org.camunda.latera.bss.connectors.hid.hydra.Subject
import org.camunda.latera.bss.connectors.hid.hydra.Company
import org.camunda.latera.bss.connectors.hid.hydra.Person
import org.camunda.latera.bss.connectors.hid.hydra.Customer
import org.camunda.latera.bss.connectors.hid.hydra.Account
import org.camunda.latera.bss.connectors.hid.hydra.Equipment
import org.camunda.latera.bss.connectors.hid.hydra.Region
import org.camunda.latera.bss.connectors.hid.hydra.Address

class Hydra implements Ref, Good, Document, PriceOrder, PriceLine, Subject, Company, Person, Customer, Account, Equipment, Region, Address {
  HID hid
  def firmId
  DelegateExecution execution
  SimpleLogger logger

  Hydra(DelegateExecution execution) {
    this.execution = execution
    this.logger = new SimpleLogger(this.execution)
    this.hid = new HID(execution)

    def user     = execution.getVariable('hydraUser') ?: 'hydra'
    def password = execution.getVariable('hydraPassword')
    this.firmId  = execution.getVariable('hydraFirmId') ?: 100

    this.hid.execute('MAIN.INIT', [
      vch_VC_IP       : '127.0.0.1',
      vch_VC_USER     : user,
      vch_VC_PASS     : password,
      vch_VC_APP_CODE : 'NETSERV_HID',
      vch_VC_CLN_APPID: 'HydraOMS'
    ])

    this.hid.execute('MAIN.SET_ACTIVE_FIRM', [
      num_N_FIRM_ID: firmId
    ])
  }

  LinkedHashMap mergeParams(
    LinkedHashMap initial,
    LinkedHashMap input
  ) {
    LinkedHashMap params = initial + input

    //If it is set opf instead of opfId, get proper reference ids from Hydra
    LinkedHashMap result = [:]
    params.each{ name, value ->
      def group = (name =~ /^(.*)Id$/)
      if (group.size() > 0) {
        String noIdName = group[0][1]
        if (params.containsKey(noIdName)) {
          result[noIdName] = this.getRefIdByCode(value)
        } else {
          result[name] = value
        }
      } else {
        result[name] = value
      }
    }
    return result
  }

  //Other methods are imported from traits
}