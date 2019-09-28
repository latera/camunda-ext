package org.camunda.latera.bss.connectors.hoper.hydra

import static org.camunda.latera.bss.utils.DateTimeUtil.*
import java.time.temporal.Temporal

trait Subscription {
  private static LinkedHashMap AVAILABLE_SERVICES_ENTITY_TYPE = [
    one    : 'available_service',
    plural : 'available_services'
  ]

  private static LinkedHashMap SUBSCRIPTION_ENTITY_TYPE = [
    one    : 'subscription',
    plural : 'subscriptions'
  ]

  private static LinkedHashMap CHILD_SUBSCRIPTION_ENTITY_TYPE = [
    one    : 'additional_service',
    plural : 'additional_services'
  ]

  private Map getAvailableServicesEntityType(def customerId, def id = null) {
    return AVAILABLE_SERVICES_ENTITY_TYPE + withParent(getCustomerEntityType(customerId)) + withId(id)
  }

  private Map getSubscriptionEntityType(def customerId, def id = null) {
    return SUBSCRIPTION_ENTITY_TYPE + withParent(getCustomerEntityType(customerId)) + withId(id)
  }

  private Map getChildSubscriptionEntityType(def customerId, def subscriptionId, def id = null) {
    return CHILD_SUBSCRIPTION_ENTITY_TYPE + withParent(getSubscriptionEntityType(customerId, subscriptionId)) + withId(id)
  }

  private Map getAvailableServicesDefaultParams() {
    return [
      accountId          : null,
      docId              : null,
      equipmentId        : null,
      parSubscriptionId  : null,
      operationDate      : null
    ]
  }

  private Map getAvailableServicesParamsMap(Map params) {
    return [
      n_account_id          : params.accountId,
      n_contract_id         : params.docId,
      n_equipment_id        : params.equipmentId,
      n_par_subscription_id : params.parSubscriptionId,
      d_oper                : params.operationDate,
      per_page              : params.perPage,
      page                  : params.page
    ]
  }

  private Map getSubscriptionDefaultParams() {
    return [
      accountId          : null,
      docId              : null,
      goodId             : null,
      equipmentId        : null,
      quant              : null,
      beginDate          : null,
      endDate            : null
    ]
  }

  private Map getSubscriptionParamsMap(Map params) {
    return [
      n_account_id     : params.accountId,
      n_contract_id    : params.docId,
      n_service_id     : params.goodId,
      n_object_id      : params.equipmentId,
      n_quant          : params.quant,
      d_begin          : params.beginDate,
      d_end            : params.endDate,
      close_charge_log : params.closeChargeLog
    ]
  }

  private Map getChildSubscriptionDefaultParams() {
    return [
      accountId          : null,
      goodId             : null,
      quant              : null,
      parSubscriptionId  : null,
      beginDate          : null,
      endDate            : null
    ]
  }

  private Map getChildSubscriptionParamsMap(Map params) {
    return [
      n_account_id          : params.accountId,
      n_service_id          : params.goodId,
      n_quant               : params.quant,
      n_par_subscription_id : params.parSubscriptionId,
      d_begin               : params.beginDate,
      d_end                 : params.endDate,
      immediate             : params.immediate
    ]
  }

  private Map getAvailableServicesParams(Map input) {
    LinkedHashMap params = getAvailableServicesDefaultParams() + input
    LinkedHashMap data   = getAvailableServicesParamsMap(params)
    return prepareParams(data)
  }

  private Map getSubscriptionParams(Map input) {
    LinkedHashMap params = getSubscriptionDefaultParams() + input
    LinkedHashMap data   = getSubscriptionParamsMap(params)
    return prepareParams(data)
  }

  private Map getChildSubscriptionParams(Map input) {
    LinkedHashMap params = getChildSubscriptionDefaultParams() + input
    LinkedHashMap data   = getChildSubscriptionParamsMap(params)
    return prepareParams(data)
  }

  List getAvailableServices(def customerId, Map input = [:]) {
    LinkedHashMap params = getPaginationDefaultParams() + getAvailableServicesParams(input)
    return getEntities(getAvailableServicesEntityType(customerId), params)
  }

  List getAvailableServices(Map input, def customerId) {
    return getAvailableServices(customerId, input)
  }

  Map getAvailableService(def customerId, Map input) {
    def serviceId = input.serviceId ?: input.goodId
    List availableServices = getAvailableServices(customerId, input)
    for (service in availableServices) {
      if (service.n_good_id.toString() == serviceId.toString()) {
          return service
      }
    }
  }

  Map getAvailableService(Map input, def customerId) {
    return getAvailableService(customerId, input)
  }

  Map getAvailableServiceByName(def customerId, Map input) {
    def serviceName = input.serviceName ?: input.goodName
    List availableServices = getAvailableServices(customerId, input)
    for (service in availableServices) {
      if (service.vc_name == serviceName) {
        return service
      }
    }
    return null
  }

  Map getAvailableServiceByName(Map input, def customerId) {
    return getAvailableServiceByName(customerId, input)
  }

  Map getAvailableServiceByCode(def customerId, Map input) {
    def serviceCode = input.serviceCode ?: input.goodCode
    List availableServices = getAvailableServices(customer, input)
    for (service in availableServices) {
      if (service.vc_code == serviceName) {
        return service
      }
    }
    return null
  }

  Map getAvailableServiceByCode(Map input, def customerId) {
    return getAvailableServiceByCode(customerId, input)
  }

  List getSubscriptions(def customerId, Map input = [:]) {
    LinkedHashMap params = getPaginationDefaultParams() + input
    return getEntities(getSubscriptionEntityType(customerId), params)
  }

  Map getSubscriptions(Map input, def customerId) {
    return getSubscriptions(customerId, input)
  }

  Map getSubscription(def customerId, def subscriptionId) {
    return getEntity(getSubscriptionEntityType(customerId), subscriptionId)
  }

  Map createSubscription(def customerId, Map input = [:]) {
    LinkedHashMap params = getSubscriptionParams(input)
    return createEntity(getSubscriptionEntityType(customerId), params)
  }

  Map createSubscription(Map input) {
    def customerId = input.customerId
    input.remove('customerId')
    return createSubscription(customerId, input)
  }

  Map createSubscription(Map input, def customerId) {
    return createSubscription(customerId, input)
  }

  Map updateSubscription(def customerId, def subscriptionId, Map input) {
    LinkedHashMap params = getSubscriptionParams(input)
    return updateEntity(getSubscriptionEntityType(customerId), subscriptionId, params)
  }

  Map updateSubscription(Map input) {
    def customerId = input.customerId
    input.remove('customerId')
    def subscriptionId = input.subscriptionId
    input.remove('subscriptionId')
    return updateSubscription(customerId, subscriptionId, input)
  }

  Map updateSubscription(Map input, def customerId, def subscriptionId) {
    return updateSubscription(customerId, subscriptionId, input)
  }

  Map putSubscription(Map input) {
    def customerId = input.customerId
    input.remove('customerId')
    return putSubscription(customerId, input)
  }

  Map putSubscription(def customerId, Map input) {
    def parSubscriptionId = input.parSubscriptionId
    input.remove('parSubscriptionId')
    def subscriptionId = input.subscriptionId
    input.remove('subscriptionId')

    if (parSubscriptionId) {
      if (subscriptionId) {
        return updateSubscription(customerId, subscriptionId, input)
      } else {
        return createSubscription(customerId, input)
      }
    } else {
      if (subscriptionId) {
        return updateChildSubscription(customerId, parSubscriptionId, subscriptionId, input)
      } else {
        return createChildSubscription(customerId, parSubscriptionId, input)
      }
    }
  }

  Map putSubscription(Map input, def customerId) {
    return putSubscription(customerId, input)
  }

  Map closeSubscription(def customerId, def subscriptionId, Temporal endDate, Boolean closeChargeLog = false) {
    return updateSubscription(customerId, subscriptionId, [endDate: endDate, closeChargeLog: closeChargeLog])
  }

  Map closeSubscription(Map input) {
    LinkedHashMap params = [
      customerId     : null,
      subscriptionId : null,
      endDate        : local(),
      immediate      : true
    ] + input
    return closeSubscription(params.customerId, params.subscriptionId, params.endDate, params.immediate)
  }

  List getChildSubscriptions(def customerId, def subscriptionId, Map input = [:]) {
    LinkedHashMap params = getPaginationDefaultParams() + input
    return getEntities(getChildSubscriptionEntityType(customerId, subscriptionId), params)
  }

  List getChildSubscriptions(Map input, def customerId, def subscriptionId) {
    return getChildSubscriptions(customerId, subscriptionId, input)
  }

  Map getChildSubscription(def customerId, def subscriptionId, def childSubscriptionId) {
    return getEntity(getChildSubscriptionEntityType(customerId, subscriptionId), childSubscriptionId)
  }

  Map createChildSubscription(def customerId, def subscriptionId, Map input = [:]) {
    LinkedHashMap params = getChildSubscriptionParams(input + [parSubscriptionId: subscriptionId])
    return createEntity(getChildSubscriptionEntityType(customerId, subscriptionId), params)
  }

  Map createChildSubscription(Map input) {
    def customerId = input.customerId
    input.remove('customerId')
    def subscriptionId = input.subscriptionId
    input.remove('subscriptionId')
    return createChildSubscription(customerId, subscriptionId, input)
  }

  Map createChildSubscription(Map input, def customerId, def subscriptionId) {
    return createChildSubscription(customerId, subscriptionId, input)
  }

  Map updateChildSubscription(def customerId, def subscriptionId, def childSubscriptionId, Map input = [:]) {
    LinkedHashMap params = getChildSubscriptionParams(input)
    return updateEntity(getChildSubscriptionEntityType(customerId, subscriptionId), childSubscriptionId, params)
  }

  Map updateChildSubscription(Map input) {
    def customerId = input.customerId
    input.remove('customerId')
    def subscriptionId = input.subscriptionId
    input.remove('subscriptionId')
    def childSubscriptionId = input.childSubscriptionId
    input.remove('childSubscriptionId')
    return updateChildSubscription(customerId, subscriptionId, childSubscriptionId, input)
  }

  Map updateChildSubscription(Map input, def customerId, def subscriptionId, def childSubscriptionId) {
    return updateChildSubscription(customerId, subscriptionId, childSubscriptionId, input)
  }

  Map putChildSubscription(Map input) {
    def customerId = input.customerId
    input.remove('customerId')
    return putChildSubscription(customerId, input)
  }

  Map putChildSubscription(def customerId, Map input) {
    def parSubscriptionId = input.parSubscriptionId
    input.remove('parSubscriptionId')
    return putChildSubscription(customerId, parSubscriptionId, input)
  }

  Map putChildSubscription(Map input, def customerId) {
    return putChildSubscription(customerId, input)
  }

  Map putChildSubscription(def customerId, def parSubscriptionId, Map input) {
    def subscriptionId = input.subscriptionId
    input.remove('subscriptionId')

    if (subscriptionId) {
      return updateChildSubscription(customerId, parSubscriptionId, subscriptionId, input)
    } else {
      return createChildSubscription(customerId, parSubscriptionId, input)
    }
  }

  Map putChildSubscription(Map input, def customerId, def parSubscriptionId) {
    return putChildSubscription(customerId, parSubscriptionId, input)
  }

  Map closeChildSubscription(def customerId, def subscriptionId, def childSubscriptionId, Temporal endDate = local(), Boolean immediate = false) {
    return updateChildSubscription(customerId, subscriptionId, childSubscriptionId, [endDate: endDate, immediate: immediate])
  }

  Map closeChildSubscription(Map input) {
    LinkedHashMap params = [
      customerId          : null,
      subscriptionId      : null,
      childSubscriptionId : null,
      endDate             : local(),
      immediate           : true
    ] + input
    return closeChildSubscription(params.customerId, params.subscriptionId, params.childSubscriptionId, params.endDate, params.immediate)
  }
}