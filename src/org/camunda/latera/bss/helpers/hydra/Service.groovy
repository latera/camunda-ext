package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.Oracle.decodeBool
import static org.camunda.latera.bss.utils.Oracle.encodeDateStr
import static org.camunda.latera.bss.utils.StringUtil.capitalize
import static org.camunda.latera.bss.utils.StringUtil.isEmpty
import static org.camunda.latera.bss.utils.DateTimeUtil.local
import static org.camunda.latera.bss.utils.Numeric.toFloatSafe

trait Service {
  void fetchService(Map input = [:]) {
    Map params = [
      prefix : ''
    ] + input

    String servicePrefix   = "${capitalize(params.prefix)}Service"
    String priceLinePrefix = "${capitalize(params.prefix)}PriceLine"

    def priceLineId = order."${priceLinePrefix}Id" ?: [is: 'null']
    Map priceLine   = hydra.getPriceLine(priceLineId)

    order."${servicePrefix}Id"         = priceLine?.n_good_id
    order."${servicePrefix}Name"       = priceLine?.vc_good_name
    order."${servicePrefix}Price"      = toFloatSafe(priceLine?.n_price?.replace(',', '.'), 0.0)
    order."${servicePrefix}PriceWoTax" = toFloatSafe(priceLine?.n_price_wo_tax?.replace(',', '.'), 0.0)
  }

  def fetchServiceAddParam(Map input = [:]) {
    Map params = [
      servicePrefix : '',
      prefix        : '',
      param         : '',
      code          : ''
    ] + input

    String servicePrefix = "${capitalize(params.servicePrefix)}Service"
    String prefix = capitalize(params.prefix)
    String param  = capitalize(params.param)

    def serviceId = order."${servicePrefix}Id" ?: [is: 'null']
    Map addParam = hydra.getGoodAddParamBy(
      goodId : serviceId,
      param  : params.code ?: "GOOD_VAL_${param}"
    )
    def (value, valueType) = hydra.getAddParamValue(addParam)
    order."${servicePrefix}${prefix}${params.code ?: param}${valueType == 'refId' ? 'Id': ''}" = value
    return value
  }

  void fetchSubscription(Map input = [:]) {
    Map params = [
      parSubscriptionPrefix  : '',
      prevSubscriptionPrefix : '',
      accountPrefix          : '',
      contractPrefix         : '',
      servicePrefix          : '',
      equipmentPrefix        : '',
      prefix                 : ''
    ] + input

    String customerPrefix         = "${capitalize(params.customerPrefix)}Customer"
    String accountPrefix          = "${capitalize(params.accountPrefix)}Account"
    String contractPrefix         = "${capitalize(params.contractPrefix)}Contract"
    String servicePrefix          = "${capitalize(params.servicePrefix)}Service"
    String priceLinePrefix        = "${capitalize(params.servicePrefix)}PriceLine"
    String equipmentPrefix        = "${capitalize(params.equipmentPrefix)}Equipment"
    String parSubscriptionPrefix  = "${equipmentPrefix}${servicePrefix}${capitalize(params.parSubscriptionPrefix)}ParSubscription"
    String prevSubscriptionPrefix = "${equipmentPrefix}${servicePrefix}${capitalize(params.prevSubscriptionPrefix)}PrevSubscription"
    String subscriptionPrefix     = "${equipmentPrefix}${servicePrefix}${capitalize(params.prefix)}Subscription"

    def subscriptionId = order."${subscriptionPrefix}Id" ?: [is: 'null']
    def subscription = hydra.getSubscription(subscriptionId)

    order."${parSubscriptionPrefix}Id"     = subscription?.n_par_subscription_id
    order."${prevSubscriptionPrefix}Id"    = subscription?.n_prev_subscription_id
    order."${subscriptionPrefix}BeginDate" = subscription?.d_begin ? local(subscription.d_begin) : null
    order."${subscriptionPrefix}EndDate"   = subscription?.d_end   ? local(subscription.d_end)   : null
    order."${subscriptionPrefix}IsClosed"  = decodeBool(subscription?.c_fl_closed)
    if (isEmpty(order."${customerPrefix}Id")) {
      order."${customerPrefix}Id" = subscription?.n_customer_id
    }
    if (isEmpty(order."${accountPrefix}Id")) {
      order."${accountPrefix}Id" = subscription?.n_account_id
    }
    if (isEmpty(order."${contractPrefix}Id")) {
      order."${contractPrefix}Id" = subscription?.n_doc_id
    }
    if (isEmpty(order."${servicePrefix}Id")) {
      order."${servicePrefix}Id" = subscription?.n_service_id
    }
    if (isEmpty(order."${equipmentPrefix}Id")) {
      order."${equipmentPrefix}Id" = subscription?.n_object_id
    }

    def priceLine = hydra.hid.queryFirst("""
      SELECT 'n_price_line_id', N_PRICE_LINE_ID
      FROM
          TABLE(SI_SUBSCRIPTIONS_PKG_S.GET_AVAILABLE_SERVICES_P(
            num_N_CONTRACT_ID => ${subscription?.n_doc_id},
            num_N_ACCOUNT_ID  => ${subscription?.n_account_id},
            num_N_OBJECT_ID   => ${subscription?.n_object_id}
          ))
      WHERE N_SERVICE_ID = ${subscription?.n_service_id}
    """, true)
    order."${priceLinePrefix}Id" = priceLine?.n_price_line_id
    fetchService(prefix: params.servicePrefix)
  }

  void fetchServiceSubscription(Map input = [:]) {
    Map params = [
      parSubscriptionPrefix  : '',
      prevSubscriptionPrefix : '',
      customerPrefix         : '',
      accountPrefix          : '',
      contractPrefix         : '',
      servicePrefix          : '',
      equipmentPrefix        : '',
      prefix                 : '',
      beginDate              : null,
      endDate                : null,
      operationDate          : local(),
      isClosed               : false,
      onlyParent             : true
    ] + input

    if (params.operationDate == null && params.beginDate == null) {
      params.beginDate = ['<': encodeDateStr(local())]
    }

    String customerPrefix         = "${capitalize(params.customerPrefix)}Customer"
    String accountPrefix          = "${capitalize(params.accountPrefix)}Account"
    String contractPrefix         = "${capitalize(params.contractPrefix)}Contract"
    String servicePrefix          = "${capitalize(params.servicePrefix)}Service"
    String equipmentPrefix        = "${capitalize(params.equipmentPrefix)}Equipment"
    String parSubscriptionPrefix  = "${equipmentPrefix}${servicePrefix}${capitalize(params.parSubscriptionPrefix)}ParSubscription"
    String prevSubscriptionPrefix = "${equipmentPrefix}${servicePrefix}${capitalize(params.prevSubscriptionPrefix)}PrevSubscription"
    String subscriptionPrefix     = "${equipmentPrefix}${servicePrefix}${capitalize(params.prefix)}Subscription"

    def customerId         = order."${customerPrefix}Id"
    def accountId          = order."${accountPrefix}Id"
    def contractId         = order."${contractPrefix}Id"
    def serviceId          = order."${servicePrefix}Id"
    def equipmentId        = order."${equipmentPrefix}Id"
    def parSubscriptionId  = order."${parSubscriptionPrefix}Id"
    def prevSubscriptionId = order."${prevSubscriptionPrefix}Id"
    Map subscription = hydra.getSubscriptionBy(
      customerId         : customerId,
      accountId          : accountId,
      docId              : contractId,
      goodId             : serviceId,
      equipmentId        : equipmentId,
      beginDate          : params.beginDate,
      endDate            : params.endDate,
      operationDate      : params.operationDate,
      isClosed           : params.isClosed,
      parSubscriptionId  : parSubscriptionId ?: (params.onlyParent ? [is: 'null'] : null),
      prevSubscriptionId : prevSubscriptionId
    )

    order."${subscriptionPrefix}Id" = subscription?.n_subscription_id

    if (isEmpty(order."${parSubscriptionPrefix}Id")) {
      order."${parSubscriptionPrefix}Id" = subscription?.n_par_subscription_id
    }

    if (isEmpty(order."${prevSubscriptionPrefix}Id")) {
      order."${prevSubscriptionPrefix}Id" = subscription?.n_prev_subscription_id
    }
    fetchSubscription(params)
  }

  Boolean createServiceSubscription(Map input = [:]) {
    Map params = [
      parSubscriptionPrefix  : '',
      prevSubscriptionPrefix : '',
      customerPrefix         : '',
      accountPrefix          : '',
      contractPrefix         : '',
      servicePrefix          : '',
      equipmentPrefix        : '',
      prefix                 : '',
      beginDate              : local(),
      endDate                : null,
      payDay                 : null
    ] + input

    String customerPrefix         = "${capitalize(params.customerPrefix)}Customer"
    String accountPrefix          = "${capitalize(params.accountPrefix)}Account"
    String contractPrefix         = "${capitalize(params.contractPrefix)}Contract"
    String servicePrefix          = "${capitalize(params.servicePrefix)}Service"
    String equipmentPrefix        = "${capitalize(params.equipmentPrefix)}Equipment"
    String parSubscriptionPrefix  = "${equipmentPrefix}${servicePrefix}${capitalize(params.parSubscriptionPrefix)}ParSubscription"
    String prevSubscriptionPrefix = "${equipmentPrefix}${servicePrefix}${capitalize(params.prevSubscriptionPrefix)}PrevSubscription"
    String subscriptionPrefix     = "${equipmentPrefix}${servicePrefix}${capitalize(params.prefix)}Subscription"

    def customerId         = order."${customerPrefix}Id"
    def accountId          = order."${accountPrefix}Id"
    def contractId         = order."${contractPrefix}Id"
    def serviceId          = order."${servicePrefix}Id"
    def equipmentId        = order."${equipmentPrefix}Id"
    def parSubscriptionId  = order."${parSubscriptionPrefix}Id"
    def prevSubscriptionId = order."${prevSubscriptionPrefix}Id"

    Map subscription = hydra.createSubscription(
      customerId,
      accountId          : accountId,
      docId              : contractId,
      goodId             : serviceId,
      equipmentId        : equipmentId,
      beginDate          : params.beginDate,
      endDate            : params.endDate,
      chargeLogEndDate   : params.endDate,
      payDay             : params.payDay,
      parSubscriptionId  : parSubscriptionId,
      prevSubscriptionId : prevSubscriptionId
    )

    Boolean result = false
    if (subscription) {
      order."${subscriptionPrefix}Id" = subscription.num_N_SUBJ_GOOD_ID
      result = true
    }
    order."${subscriptionPrefix}Created" = result
    return result
  }

  Boolean createOneOffServiceSubscription(Map input = [:]) {
    Map params = [
      accountPrefix         : '',
      contractPrefix        : '',
      servicePrefix         : '',
      equipmentPrefix       : '',
      parSubscriptionPrefix : '',
      prefix                : '',
      beginDate             : local()
    ] + input

    String customerPrefix        = "${capitalize(params.customerPrefix)}Customer"
    String accountPrefix         = "${capitalize(params.accountPrefix)}Account"
    String contractPrefix        = "${capitalize(params.contractPrefix)}Contract"
    String servicePrefix         = "${capitalize(params.servicePrefix)}OneOffService"
    String equipmentPrefix       = "${capitalize(params.equipmentPrefix)}Equipment"
    String parSubscriptionPrefix = "${equipmentPrefix}${servicePrefix}${capitalize(params.parSubscriptionPrefix)}ParSubscription"
    String subscriptionPrefix    = "${equipmentPrefix}${servicePrefix}${capitalize(params.prefix)}Subscription"

    def customerId        = order."${customerPrefix}Id"
    def accountId         = order."${accountPrefix}Id"
    def contractId        = order."${contractPrefix}Id"
    def serviceId         = order."${servicePrefix}Id"
    def equipmentId       = order."${equipmentPrefix}Id"
    def parSubscriptionId = order."${parSubscriptionPrefix}Id"
    Map subscription = hydra.createSubscription(
      customerId,
      accountId         : accountId,
      docId             : contractId,
      goodId            : serviceId,
      equipmentId       : equipmentId,
      parSubscriptionId : parSubscriptionId,
      beginDate         : params.beginDate,
      endDate           : params.beginDate
    )

    Boolean result = false
    if (subscription) {
      order."${subscriptionPrefix}Id" = subscription.num_N_SUBSCRIPTION_ID
      result = true
    }
    order."${subscriptionPrefix}Created" = result
    return result
  }

  Boolean createAdjustment(Map input = [:]) {
    Map params = [
      accountPrefix   : '',
      contractPrefix  : '',
      servicePrefix   : '',
      equipmentPrefix : '',
      prefix          : '',
      sum             : null,
      sumWoTax        : null,
      operationDate   : local()
    ] + input

    String accountPrefix           = "${capitalize(params.accountPrefix)}Account"
    String contractPrefix          = "${capitalize(params.contractPrefix)}Contract"
    String servicePrefix           = "${capitalize(params.servicePrefix)}"
    String adjustmentServicePrefix = "${servicePrefix}AdjustmentService"
    String equipmentPrefix         = "${capitalize(params.equipmentPrefix)}Equipment"
    String adjustmentPrefix        = "${equipmentPrefix}${servicePrefix}${capitalize(params.prefix)}Adjustment"

    def accountId    = order."${accountPrefix}Id"
    def contractId   = order."${contractPrefix}Id"
    def serviceId    = order."${adjustmentServicePrefix}Id"
    def equipmentId  = order."${equipmentPrefix}Id"
    Boolean result = hydra.addAdjustment(
      accountId,
      docId         : contractId,
      goodId        : serviceId,
      equipmentId   : equipmentId,
      sum           : params.sum,
      sumWoTax      : params.sumWoTax,
      operationDate : params.operationDate
    )

    order."${adjustmentPrefix}Created" = result
    return result
  }

  Boolean closeServiceSubscription(Map input = [:]) {
    Map params = [
      servicePrefix   : '',
      equipmentPrefix : '',
      prefix          : '',
      endDate         : local()
    ] + input

    String servicePrefix      = "${capitalize(params.servicePrefix)}Service"
    String equipmentPrefix    = "${capitalize(params.equipmentPrefix)}Equipment"
    String prefix             = "${capitalize(params.prefix)}Subscription"
    String subscriptionPrefix = "${equipmentPrefix}${servicePrefix}${prefix}"

    def subscriptionId = order."${subscriptionPrefix}Id"
    Boolean result     = hydra.closeSubscriptionForce(
      subscriptionId,
      endDate: params.endDate
    )

    if (result) {
      order."${subscriptionPrefix}CloseDate" = params.endDate
    }
    order."${subscriptionPrefix}Closed" = result
    return result
  }

  Boolean deleteServiceSubscription(Map input = [:]) {
    Map params = [
      servicePrefix   : '',
      equipmentPrefix : '',
      prefix          : ''
    ] + input

    String servicePrefix      = "${capitalize(params.servicePrefix)}Service"
    String equipmentPrefix    = "${capitalize(params.equipmentPrefix)}Equipment"
    String prefix             = "${capitalize(params.prefix)}Subscription"
    String subscriptionPrefix = "${equipmentPrefix}${servicePrefix}${prefix}"

    def subscriptionId = order."${subscriptionPrefix}Id"
    Boolean result     = hydra.deleteSubscriptionForce(subscriptionId)

    order."${subscriptionPrefix}Deleted" = result
    return result
  }
}
