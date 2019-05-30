package org.camunda.latera.bss.connectors.hoper

import groovy.net.xmlrpc.*
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.latera.bss.logging.SimpleLogger
import org.camunda.latera.bss.connectors.Hoper
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
  private static Integer DEFAULT_FIRM = 100
  Hoper hoper
  def firmId
  def resellerId
  SimpleLogger logger

  Hydra(DelegateExecution execution) {
    this.logger    = new SimpleLogger(execution)
    this.hoper     = new Hoper(execution)

    this.firmId     = execution.getVariable('hydraFirmId') ?: (execution.getVariable('homsOrderDataFirmId') ?: getDefaultFirmId())
    this.resellerId = execution.getVariable('hydraResellerId') ?: execution.getVariable('homsOrderDataResellerId')
  }

  LinkedHashMap mergeParams(
    LinkedHashMap initial,
    LinkedHashMap input
  ) {
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