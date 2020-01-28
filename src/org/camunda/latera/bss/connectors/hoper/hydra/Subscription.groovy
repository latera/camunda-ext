package org.camunda.latera.bss.connectors.hoper.hydra

import static org.camunda.latera.bss.utils.DateTimeUtil.local
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

  Map getAvailableServicesEntityType(def customerId, def id = null) {
    return AVAILABLE_SERVICES_ENTITY_TYPE + withParent(getCustomerEntityType(customerId)) + withId(id)
  }

  Map getSubscriptionEntityType(def customerId, def id = null) {
    return SUBSCRIPTION_ENTITY_TYPE + withParent(getCustomerEntityType(customerId)) + withId(id)
  }

  Map getChildSubscriptionEntityType(def customerId, def subscriptionId, def id = null) {
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

  List getAvailableServices(Map input = [:], def customerId) {
    LinkedHashMap params = getPaginationDefaultParams() + getAvailableServicesParams(input)
    return getEntities(getAvailableServicesEntityType(customerId), params)
  }

  Map getAvailableService(Map input = [:], def customerId) {
    def serviceId = input.serviceId ?: input.goodId
    List availableServices = getAvailableServices(input, customerId)
    for (Map service in availableServices) {
      if (service.n_good_id.toString() == serviceId.toString()) {
          return service
      }
    }
  }

  Map getAvailableServiceByName(Map input = [:], def customerId) {
    String serviceName = input.serviceName ?: input.goodName ?: input.name
    List availableServices = getAvailableServices(input, customerId)
    for (Map service in availableServices) {
      if (service.vc_name == serviceName) {
        return service
      }
    }
    return null
  }

  Map getAvailableServiceByName(def customerId, CharSequence name) {
    return getAvailableServiceByName(customerId, name: name)
  }

  Map getAvailableServiceByCode(Map input = [:], def customerId) {
    String serviceCode = input.serviceCode ?: input.goodCode
    List availableServices = getAvailableServices(input, customerId)
    for (service in availableServices) {
      if (service.vc_code == serviceCode) {
        return service
      }
    }
    return null
  }

  Map getAvailableServiceByCode(def customerId, CharSequence code) {
    return getAvailableServiceByCode(customerId, name: code)
  }

  List getSubscriptions(Map input = [:], def customerId) {
    LinkedHashMap params = getPaginationDefaultParams() + input
    return getEntities(getSubscriptionEntityType(customerId), params)
  }

  Map getSubscription(def customerId, def subscriptionId) {
    return getEntity(getSubscriptionEntityType(customerId), subscriptionId)
  }

  Map createSubscription(Map input = [:], def customerId) {
    LinkedHashMap params = getSubscriptionParams(input)
    return createEntity(getSubscriptionEntityType(customerId), params)
  }

  Map updateSubscription(Map input = [:], def customerId, def subscriptionId) {
    LinkedHashMap params = getSubscriptionParams(input)
    return updateEntity(getSubscriptionEntityType(customerId), subscriptionId, params)
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

  Map closeSubscription(Map input = [:], def customerId, def subscriptionId) {
    return closeSubscription(input + [customerId: customerId, subscriptionId: subscriptionId])
  }

  List getChildSubscriptions(Map input = [:], def customerId, def subscriptionId) {
    LinkedHashMap params = getPaginationDefaultParams() + input
    return getEntities(getChildSubscriptionEntityType(customerId, subscriptionId), params)
  }

  Map getChildSubscription(def customerId, def subscriptionId, def childSubscriptionId) {
    return getEntity(getChildSubscriptionEntityType(customerId, subscriptionId), childSubscriptionId)
  }

  Map createChildSubscription(Map input = [:], def customerId, def subscriptionId) {
    LinkedHashMap params = getChildSubscriptionParams(input + [parSubscriptionId: subscriptionId])
    return createEntity(getChildSubscriptionEntityType(customerId, subscriptionId), params)
  }

  Map updateChildSubscription(Map input = [:], def customerId, def subscriptionId, def childSubscriptionId) {
    LinkedHashMap params = getChildSubscriptionParams(input)
    return updateEntity(getChildSubscriptionEntityType(customerId, subscriptionId), childSubscriptionId, params)
  }

  Map closeChildSubscription(Map input) {
    LinkedHashMap params = [
      customerId          : null,
      subscriptionId      : null,
      childSubscriptionId : null,
      endDate             : local(),
      immediate           : true
    ] + input
    return updateChildSubscription(params.customerId, params.subscriptionId, endDate: endDate, immediate: immediate)
  }

  Map closeChildSubscription(Map input = [:], def customerId, def subscriptionId, def childSubscriptionId) {
    return closeSubscription(input + [customerId: customerId, subscriptionId: subscriptionId, childSubscriptionId: childSubscriptionId])
  }
}