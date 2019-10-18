package org.camunda.latera.bss.helpers

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.latera.bss.connectors.hid.Hydra as HIDHydra
import org.camunda.latera.bss.connectors.hoper.Hydra as HoperHydra
import org.camunda.latera.bss.utils.Order
import org.camunda.latera.bss.helpers.hydra.Account
import org.camunda.latera.bss.helpers.hydra.Address
import org.camunda.latera.bss.helpers.hydra.BaseSubject
import org.camunda.latera.bss.helpers.hydra.Company
import org.camunda.latera.bss.helpers.hydra.Contract
import org.camunda.latera.bss.helpers.hydra.Customer
import org.camunda.latera.bss.helpers.hydra.Equipment
import org.camunda.latera.bss.helpers.hydra.File
import org.camunda.latera.bss.helpers.hydra.Group
import org.camunda.latera.bss.helpers.hydra.Individual
import org.camunda.latera.bss.helpers.hydra.Region
import org.camunda.latera.bss.helpers.hydra.Reseller
import org.camunda.latera.bss.helpers.hydra.Service
import org.camunda.latera.bss.helpers.hydra.Spartial

class Hydra implements Account, Address, BaseSubject, Company, Contract, Customer, Equipment, File, Group, Individual, Region, Reseller, Service, Spartial {
  DelegateExecution execution
  Order order
  HIDHydra hydra
  HoperHydra hoper

  Hydra(Map params = [:], DelegateExecution execution) {
    this.execution  = execution
    this.order      = new Order(execution)

    if (params.hydra != null) {
      this.hydra = params.hydra
    } else {
      this.hydra = new HIDHydra(execution)
    }

    if (params.hoper) {
      if (params.hoper != true) {
        this.hoper = params.hoper
      } else {
        this.hoper = new HoperHydra(execution)
      }
    }
  }
  //Other methods are imported from traits
}