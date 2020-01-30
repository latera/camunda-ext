package org.camunda.latera.bss.connectors.hoper

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.latera.bss.logging.SimpleLogger
import org.camunda.latera.bss.connectors.Hoper
import static org.camunda.latera.bss.utils.MapUtil.merge
import static org.camunda.latera.bss.utils.Constants.DEFAULT_FIRM
import org.camunda.latera.bss.connectors.hoper.hydra.Main
import org.camunda.latera.bss.connectors.hoper.hydra.Entity
import org.camunda.latera.bss.connectors.hoper.hydra.Subject
import org.camunda.latera.bss.connectors.hoper.hydra.Person
import org.camunda.latera.bss.connectors.hoper.hydra.Company
import org.camunda.latera.bss.connectors.hoper.hydra.File
import org.camunda.latera.bss.connectors.hoper.hydra.Object
import org.camunda.latera.bss.connectors.hoper.hydra.Address
import org.camunda.latera.bss.connectors.hoper.hydra.Customer
import org.camunda.latera.bss.connectors.hoper.hydra.Account
import org.camunda.latera.bss.connectors.hoper.hydra.Document
import org.camunda.latera.bss.connectors.hoper.hydra.Contract
import org.camunda.latera.bss.connectors.hoper.hydra.Equipment
import org.camunda.latera.bss.connectors.hoper.hydra.Subscription

class Hydra implements Main, Entity, Subject, Person, Company, File, Object, Address, Customer, Account, Document, Contract, Equipment, Subscription {
  Hoper hoper
  def firmId
  def resellerId
  SimpleLogger logger

  Hydra(DelegateExecution execution) {
    this.logger = new SimpleLogger(execution)
    this.hoper  = new Hoper(execution)

    this.firmId     = execution.getVariable('hydraFirmId') ?: (execution.getVariable('homsOrderDataFirmId') ?: getDefaultFirmId())
    this.resellerId = execution.getVariable('hydraResellerId') ?: execution.getVariable('homsOrderDataResellerId')
  }

  private Map mergeParams(
    Map initial,
    Map input
  ) {
    LinkedHashMap params = merge(initial, input)

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
    params.each{ CharSequence name, def value ->
      if (!keysToExclude.contains(name)) {
        result[name] = value
      }
    }
    return result
  }

  def getDefaultFirmId() {
    return DEFAULT_FIRM
  }

  def getFirmId() {
    return firmId
  }

  def getResellerId() {
    return resellerId
  }

  //Other methods are imported from traits
}