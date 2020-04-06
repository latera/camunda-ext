package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.Oracle.decodeBool
import static org.camunda.latera.bss.utils.Oracle.encodeDateStr
import static org.camunda.latera.bss.utils.StringUtil.capitalize
import static org.camunda.latera.bss.utils.StringUtil.isEmpty
import static org.camunda.latera.bss.utils.StringUtil.notEmpty
import static org.camunda.latera.bss.utils.DateTimeUtil.local
import static org.camunda.latera.bss.utils.Numeric.toFloatSafe

/**
 * Service and subscription helper methods collection
 */
trait Service {
  /**
   * Get service (good) data by price line id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*PriceLineId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*ServiceId}         {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*ServiceName}       {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*ServicePrice}      {@link Double}</li>
   *   <li>{@code homsOrderData*ServicePriceWoTax} {@link Double}</li>
   * </ul>
   * @param prefix {@link CharSequence String}. Service and price line prefix. Optional. Default: empty string
   */
  void fetchService(Map input = [:]) {
    Map params = [
      prefix : ''
    ] + input

    String servicePrefix   = "${capitalize(params.prefix)}Service"
    String priceLinePrefix = "${capitalize(params.prefix)}PriceLine"

    def priceLineId = order."${priceLinePrefix}Id"
    if (isEmpty(priceLineId)) {
      return
    }

    Map priceLine   = hydra.getPriceLine(priceLineId)

    order."${servicePrefix}Id"         = priceLine?.n_good_id
    order."${servicePrefix}Name"       = priceLine?.vc_good_name
    order."${servicePrefix}Price"      = toFloatSafe(priceLine?.n_price?.replace(',', '.'), 0.0)
    order."${servicePrefix}PriceWoTax" = toFloatSafe(priceLine?.n_price_wo_tax?.replace(',', '.'), 0.0)
  }

  /**
   * Get service additional parameter value and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*ServiceId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Service*%Param%Id} {@link java.math.BigInteger BigInteger}. if additional parameter value is ref id</li>
   *   <li>{@code homsOrderData*Service*%Param%}   Any type, if additional parameter is not a ref</li>
   * </ul>
   * @param param         {@link CharSequence String}. Additional parameter short code (=variable part name) ('Param' for 'GOOD_VAL_Param')
   * @param code          {@link CharSequence String}. Additional parameter full code (if it does not start from 'GOOD_VAL_')
   * @param servicePrefix {@link CharSequence String}. Service prefix. Optional. Default: empty string
   * @param prefix        {@link CharSequence String}. Additional parameter prefix. Optional. Default: empty string
   * @return Additional parameter value
   */
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

    def serviceId = order."${servicePrefix}Id"
    if (isEmpty(serviceId)) {
      return
    }

    Map addParam = hydra.getGoodAddParamBy(
      goodId : serviceId,
      param  : params.code ?: "GOOD_VAL_${param}"
    )
    def (value, valueType) = hydra.getAddParamValue(addParam)
    order."${servicePrefix}${prefix}${params.code ?: param}${valueType == 'refId' ? 'Id': ''}" = value
    return value
  }

  /**
   * Get subscription data by id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Service*SubscriptionId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Service*ParSubscriptionId}  {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment*Service*PrevSubscriptionId} {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*CustomerId}                           {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*AccountId}                            {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*ContractId}                           {@link java.math.BigInteger BigInteger}. Filled up only if doc type is contract</li>
   *   <li>{@code homsOrderData*ContractAppId}                        {@link java.math.BigInteger BigInteger}. Filled up only if doc type is contract app</li>
   *   <li>{@code homsOrderData*AddAgreementId}                       {@link java.math.BigInteger BigInteger}. Filled up only if doc type is add agreement</li>
   *   <li>{@code homsOrderData*EquipmentId}                          {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*ServiceId}                            {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*ServiceName}                          {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*ServicePrice}                         {@link Double}</li>
   *   <li>{@code homsOrderData*ServicePriceWoTax}                    {@link Double}</li>
   * </ul>
   * @param prefix                 {@link CharSequence String}. Subscription prefix. Optional. Default: empty string
   * @param customerPrefix         {@link CharSequence String}. Customer prefix. Optional. Default: empty string
   * @param accountPrefix          {@link CharSequence String}. Account prefix. Optional. Default: empty string
   * @param contractPrefix         {@link CharSequence String}. Contract, contract app or add agreement prefix. Optional. Default: empty string
   * @param servicePrefix          {@link CharSequence String}. Service and price line prefix. Optional. Default: empty string
   * @param equipmentPrefix        {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param parSubscriptionPrefix  {@link CharSequence String}. Parent subscription prefix. Optional. Default: empty string
   * @param prevSubscriptionPrefix {@link CharSequence String}. Previous subscription prefix. Optional. Default: empty string
   */
  void fetchSubscription(Map input = [:]) {
    Map params = [
      parSubscriptionPrefix  : '',
      prevSubscriptionPrefix : '',
      accountPrefix          : '',
      customerPrefix         : '',
      contractPrefix         : '',
      servicePrefix          : '',
      equipmentPrefix        : '',
      prefix                 : ''
    ] + input

    String customerPrefix         = "${capitalize(params.customerPrefix)}Customer"
    String accountPrefix          = "${capitalize(params.accountPrefix)}Account"
    String contractPrefix         = capitalize(params.contractPrefix)
    String servicePrefix          = "${capitalize(params.servicePrefix)}Service"
    String priceLinePrefix        = "${capitalize(params.servicePrefix)}PriceLine"
    String equipmentPrefix        = "${capitalize(params.equipmentPrefix)}Equipment"
    String parSubscriptionPrefix  = "${equipmentPrefix}${servicePrefix}${capitalize(params.parSubscriptionPrefix)}ParSubscription"
    String prevSubscriptionPrefix = "${equipmentPrefix}${servicePrefix}${capitalize(params.prevSubscriptionPrefix)}PrevSubscription"
    String subscriptionPrefix     = "${equipmentPrefix}${servicePrefix}${capitalize(params.prefix)}Subscription"

    def subscriptionId = order."${subscriptionPrefix}Id"
    if (isEmpty(subscriptionId)) {
      return
    }

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
    if (isEmpty(order."${servicePrefix}Id")) {
      order."${servicePrefix}Id" = subscription?.n_service_id
    }
    if (isEmpty(order."${equipmentPrefix}Id")) {
      order."${equipmentPrefix}Id" = subscription?.n_object_id
    }

    if (notEmpty(subscription?.n_doc_id)) {
      if (hydra.isContract(subscription.n_doc_id) && isEmpty(order."${contractPrefix}ContractId")) {
        order."${contractPrefix}ContractId" = subscription.n_doc_id
      } else if (hydra.isContractApp(subscription.n_doc_id) && isEmpty(order."${contractPrefix}ContractAppId")) {
        order."${contractPrefix}ContractAppId" = subscription.n_doc_id
      } else if (hydra.isAddAgreement(subscription.n_doc_id) && isEmpty(order."${contractPrefix}AddAgreementId")) {
        order."${contractPrefix}AddAgreementId" = subscription.n_doc_id
      }
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

  /**
   * Get subscription data by its customer id, account id, contract id, service id, equipment id and dates, and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*CustomerId}         {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*AccountId}          {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*ContractId}         {@link java.math.BigInteger BigInteger}. Optional, used only if contractType == 'contract'</li>
   *   <li>{@code homsOrderData*ContractAppId}      {@link java.math.BigInteger BigInteger}. Optional, used only if contractType == 'contractApp'</li>
   *   <li>{@code homsOrderData*AddAgreementId}     {@link java.math.BigInteger BigInteger}. Optional, used only if contractType == 'addAgreement'</li>
   *   <li>{@code homsOrderData*EquipmentId}        {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*ServiceId}          {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Service*SubscriptionId}     {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment*Service*ParSubscriptionId}  {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment*Service*PrevSubscriptionId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * @param prefix                 {@link CharSequence String}. Subscription prefix. Optional. Default: empty string
   * @param customerPrefix         {@link CharSequence String}. Customer prefix. Optional. Default: empty string
   * @param accountPrefix          {@link CharSequence String}. Account prefix. Optional. Default: empty string
   * @param contractPrefix         {@link CharSequence String}. Contract, contract app or add agreement prefix. Optional. Default: empty string
   * @param contractType           {@link CharSequence String} 'contract', 'contractApp' or 'addAgreement'. Optional. Default: 'contract'
   * @param servicePrefix          {@link CharSequence String}. Service and price line prefix. Optional. Default: empty string
   * @param equipmentPrefix        {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param parSubscriptionPrefix  {@link CharSequence String}. Parent subscription prefix. Optional. Default: empty string
   * @param prevSubscriptionPrefix {@link CharSequence String}. Previous subscription prefix. Optional. Default: empty string
   * @param beginDate              {@link java.time.Temporal Any date type}. Subscription begin date. Optional. Default: null
   * @param endDate                {@link java.time.Temporal Any date type}. Subscription end date. Optional. Default: null
   * @param operationDate          {@link java.time.Temporal Any date type}. Date which should overlap subscription begin and end dates. Optional. Default: current datetime
   * @param isClosed               {@link Boolean}. If false search only non-closed subscriptions, if true search only closed subscriptions, if null - disable filter. Optional. Default: false
   * @param onlyParent             {@link Boolean}. If true search only parent subscriptions, if false disable filter. Optional. Default: true
   */
  void fetchServiceSubscription(Map input = [:]) {
    Map params = [
      parSubscriptionPrefix  : '',
      prevSubscriptionPrefix : '',
      customerPrefix         : '',
      accountPrefix          : '',
      contractPrefix         : '',
      contractType           : 'contract',
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
    String contractPrefix         = capitalize(params.contractPrefix)
    String contractType           = capitalize(params.contractType)
    String servicePrefix          = "${capitalize(params.servicePrefix)}Service"
    String equipmentPrefix        = "${capitalize(params.equipmentPrefix)}Equipment"
    String parSubscriptionPrefix  = "${equipmentPrefix}${servicePrefix}${capitalize(params.parSubscriptionPrefix)}ParSubscription"
    String prevSubscriptionPrefix = "${equipmentPrefix}${servicePrefix}${capitalize(params.prevSubscriptionPrefix)}PrevSubscription"
    String subscriptionPrefix     = "${equipmentPrefix}${servicePrefix}${capitalize(params.prefix)}Subscription"

    def customerId         = order."${customerPrefix}Id"
    def accountId          = order."${accountPrefix}Id"
    def contractId         = order."${contractPrefix}${contractType}Id"
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

  /**
   * Create subscription and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*CustomerId}                           {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*AccountId}                            {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*ContractId}                           {@link java.math.BigInteger BigInteger}. Optional, used only if contractType == 'contract'</li>
   *   <li>{@code homsOrderData*ContractAppId}                        {@link java.math.BigInteger BigInteger}. Optional, used only if contractType == 'contractApp'</li>
   *   <li>{@code homsOrderData*AddAgreementId}                       {@link java.math.BigInteger BigInteger}. Optional, used only if contractType == 'addAgreement'</li>
   *   <li>{@code homsOrderData*EquipmentId}                          {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*ServiceId}                            {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment*Service*ParSubscriptionId}  {@link java.math.BigInteger BigInteger}. Optional</li>
   *   <li>{@code homsOrderData*Equipment*Service*PrevSubscriptionId} {@link java.math.BigInteger BigInteger}. Optional</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Service*SubscriptionId}      {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment*Service*SubscriptionCreated} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param prefix                 {@link CharSequence String}. Subscription prefix. Optional. Default: empty string
   * @param customerPrefix         {@link CharSequence String}. Customer prefix. Optional. Default: empty string
   * @param accountPrefix          {@link CharSequence String}. Account prefix. Optional. Default: empty string
   * @param contractPrefix         {@link CharSequence String}. Contract, contract app or add agreement prefix. Optional. Default: empty string
   * @param contractType           {@link CharSequence String} 'contract', 'contractApp' or 'addAgreement'. Optional. Default: 'contract'
   * @param servicePrefix          {@link CharSequence String}. Service and price line prefix. Optional. Default: empty string
   * @param equipmentPrefix        {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param parSubscriptionPrefix  {@link CharSequence String}. Parent subscription prefix. Optional. Default: empty string
   * @param prevSubscriptionPrefix {@link CharSequence String}. Previous subscription prefix. Optional. Default: empty string
   * @param beginDate              {@link java.time.Temporal Any date type}. Subscription begin date. Optional. Default: current datetime
   * @param endDate                {@link java.time.Temporal Any date type}. Subscription end date. Optional. Default: null
   * @param payDay                 {@link Integer}. Payment date. Optional. Default: null
   * @return True if subsription was created successfully, false otherwise
   */
  Boolean createServiceSubscription(Map input = [:]) {
    Map params = [
      parSubscriptionPrefix  : '',
      prevSubscriptionPrefix : '',
      customerPrefix         : '',
      accountPrefix          : '',
      contractPrefix         : '',
      contractType           : 'contract',
      servicePrefix          : '',
      equipmentPrefix        : '',
      prefix                 : '',
      beginDate              : local(),
      endDate                : null,
      payDay                 : null
    ] + input

    String customerPrefix         = "${capitalize(params.customerPrefix)}Customer"
    String accountPrefix          = "${capitalize(params.accountPrefix)}Account"
    String contractPrefix         = capitalize(params.contractPrefix)
    String contractType           = capitalize(params.contractType)
    String servicePrefix          = "${capitalize(params.servicePrefix)}Service"
    String equipmentPrefix        = "${capitalize(params.equipmentPrefix)}Equipment"
    String parSubscriptionPrefix  = "${equipmentPrefix}${servicePrefix}${capitalize(params.parSubscriptionPrefix)}ParSubscription"
    String prevSubscriptionPrefix = "${equipmentPrefix}${servicePrefix}${capitalize(params.prevSubscriptionPrefix)}PrevSubscription"
    String subscriptionPrefix     = "${equipmentPrefix}${servicePrefix}${capitalize(params.prefix)}Subscription"

    def customerId         = order."${customerPrefix}Id"
    def accountId          = order."${accountPrefix}Id"
    def contractId         = order."${contractPrefix}${contractType}Id"
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

  /**
   * Create one-off subscription and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*CustomerId}                                {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*AccountId}                                 {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*ContractId}                                {@link java.math.BigInteger BigInteger}. Optional, used only if contractType == 'contract'</li>
   *   <li>{@code homsOrderData*ContractAppId}                             {@link java.math.BigInteger BigInteger}. Optional, used only if contractType == 'contractApp'</li>
   *   <li>{@code homsOrderData*AddAgreementId}                            {@link java.math.BigInteger BigInteger}. Optional, used only if contractType == 'addAgreement'</li>
   *   <li>{@code homsOrderData*EquipmentId}                               {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*OneOffServiceId}                           {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment*OneOffService*ParSubscriptionId} {@link java.math.BigInteger BigInteger}. Optional</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*OneOffService*SubscriptionId}      {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment*OneOffService*SubscriptionCreated} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param prefix                {@link CharSequence String}. Subscription prefix. Optional. Default: empty string
   * @param customerPrefix        {@link CharSequence String}. Customer prefix. Optional. Default: empty string
   * @param accountPrefix         {@link CharSequence String}. Account prefix. Optional. Default: empty string
   * @param contractPrefix        {@link CharSequence String}. Contract, contract app or add agreement prefix. Optional. Default: empty string
   * @param contractType          {@link CharSequence String} 'contract', 'contractApp' or 'addAgreement'. Optional. Default: 'contract'
   * @param servicePrefix         {@link CharSequence String}. Service and price line prefix. Optional. Default: empty string
   * @param equipmentPrefix       {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param parSubscriptionPrefix {@link CharSequence String}. Parent subscription prefix. Optional. Default: empty string
   * @param beginDate             {@link java.time.Temporal Any date type}. Subscription begin date. Optional. Default: current datetime
   * @return True if one-off subsription was created successfully, false otherwise
   */
  Boolean createOneOffServiceSubscription(Map input = [:]) {
    Map params = [
      accountPrefix         : '',
      contractPrefix        : '',
      contractType          : 'contract',
      servicePrefix         : '',
      equipmentPrefix       : '',
      parSubscriptionPrefix : '',
      prefix                : '',
      beginDate             : local()
    ] + input

    String customerPrefix        = "${capitalize(params.customerPrefix)}Customer"
    String accountPrefix         = "${capitalize(params.accountPrefix)}Account"
    String contractPrefix        = capitalize(params.contractPrefix)
    String contractType          = capitalize(params.contractType)
    String servicePrefix         = "${capitalize(params.servicePrefix)}OneOffService"
    String equipmentPrefix       = "${capitalize(params.equipmentPrefix)}Equipment"
    String parSubscriptionPrefix = "${equipmentPrefix}${servicePrefix}${capitalize(params.parSubscriptionPrefix)}ParSubscription"
    String subscriptionPrefix    = "${equipmentPrefix}${servicePrefix}${capitalize(params.prefix)}Subscription"

    def customerId        = order."${customerPrefix}Id"
    def accountId         = order."${accountPrefix}Id"
    def contractId        = order."${contractPrefix}${contractType}Id"
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

  /**
   * Create adjustment and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*AccountId}           {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*ContractId}          {@link java.math.BigInteger BigInteger}. Optional, used only if contractType == 'contract'</li>
   *   <li>{@code homsOrderData*ContractAppId}       {@link java.math.BigInteger BigInteger}. Optional, used only if contractType == 'contractApp'</li>
   *   <li>{@code homsOrderData*AddAgreementId}      {@link java.math.BigInteger BigInteger}. Optional, used only if contractType == 'addAgreement'</li>
   *   <li>{@code homsOrderData*EquipmentId}         {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*AdjustmentServiceId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*AdjustmentCreated} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param prefix                 {@link CharSequence String}. Adjustment prefix. Optional. Default: empty string
   * @param customerPrefix         {@link CharSequence String}. Customer prefix. Optional. Default: empty string
   * @param accountPrefix          {@link CharSequence String}. Account prefix. Optional. Default: empty string
   * @param contractPrefix         {@link CharSequence String}. Contract, contract app or add agreement prefix. Optional. Default: empty string
   * @param contractType           {@link CharSequence String} 'contract', 'contractApp' or 'addAgreement'. Optional. Default: 'contract'
   * @param servicePrefix          {@link CharSequence String}. Service and price line prefix. Optional. Default: empty string
   * @param equipmentPrefix        {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param sum                    {@link Double}. Adjustment sum. Optional if 'sumWoTax' is passed
   * @param sumWoTax               {@link Double}. Adjustment sum without taxes. Optional if 'sumWoTax' is passed
   * @param operationDate          {@link java.time.Temporal Any date type}. Adjustment datetime. Optional. Default: current datetime
   * @return True if adjustment was created successfully, false otherwise
   */
  Boolean createAdjustment(Map input = [:]) {
    Map params = [
      accountPrefix   : '',
      contractPrefix  : '',
      contractType    : 'contract',
      servicePrefix   : '',
      equipmentPrefix : '',
      prefix          : '',
      sum             : null,
      sumWoTax        : null,
      operationDate   : local()
    ] + input

    String accountPrefix           = "${capitalize(params.accountPrefix)}Account"
    String contractPrefix          = capitalize(params.contractPrefix)
    String contractType            = capitalize(params.contractType)
    String servicePrefix           = capitalize(params.servicePrefix)
    String adjustmentServicePrefix = "${servicePrefix}AdjustmentService"
    String equipmentPrefix         = "${capitalize(params.equipmentPrefix)}Equipment"
    String adjustmentPrefix        = "${equipmentPrefix}${servicePrefix}${capitalize(params.prefix)}Adjustment"

    def accountId    = order."${accountPrefix}Id"
    def contractId   = order."${contractPrefix}${contractType}Id"
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

  /**
   * Close subscription and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Service*SubscriptionId}  {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Service*SubscriptionCloseDate} {@link java.time.LocalDateTime LocalDateTime}</li>
   *   <li>{@code homsOrderData*Equipment*Service*SubscriptionClosed}    {@link Boolean}. Same as return value</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Subscription prefix. Optional. Default: empty string
   * @param servicePrefix   {@link CharSequence String}. Service and price line prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param endDate         {@link java.time.Temporal Any date type}. Subscription end date. Optional. Default: current datetime
   * @return True if subsription was closed successfully, false otherwise
   */
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

  /**
   * Delete subscription and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Service*SubscriptionId}  {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Service*SubscriptionDeleted} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Subscription prefix. Optional. Default: empty string
   * @param servicePrefix   {@link CharSequence String}. Service and price line prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param endDate         {@link java.time.Temporal Any date type}. Subscription end date. Optional. Default: current datetime
   * @return True if subsription was deleted successfully, false otherwise
   */
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
