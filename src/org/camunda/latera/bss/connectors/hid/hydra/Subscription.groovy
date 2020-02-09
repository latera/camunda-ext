package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Oracle.encodeBool
import static org.camunda.latera.bss.utils.Oracle.encodeFlag
import static org.camunda.latera.bss.utils.Oracle.encodeDateStr
import static org.camunda.latera.bss.utils.DateTimeUtil.local
import static org.camunda.latera.bss.utils.StringUtil.notEmpty
import static org.camunda.latera.bss.utils.StringUtil.isEmpty
import java.time.temporal.Temporal

trait Subscription {
  private static String SUBSCRIPTIONS_TABLE = 'SI_V_SUBSCRIPTIONS'

  /**
   * Get subscriptions table name
   */
  String getSubscriptionsTable() {
    return SUBSCRIPTIONS_TABLE
  }

  /**
   * Get subscription by id
   * @param subscriptionId {@link java.math.BigInteger BigInteger}
   * @return Map with subscription table row or null
   */
  Map getSubscription(def subscriptionId) {
    LinkedHashMap where = [
      n_subscription_id: subscriptionId
    ]
    return hid.getTableFirst(getSubscriptionsTable(), where: where)
  }

  /**
   * Search for subscriptions by different fields value
   * @param subscriptionId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param customerId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param accountId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docId              {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodId             {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param equipmentId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parSubscriptionId  {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param prevSubscriptionId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isClosed           {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param operationDate      {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: current date time, but only if beginDate and endDate are not set
   * @param beginDate          {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param endDate            {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit              {@link Integer}. Optional, default: 0 (unlimited)
   * @param order              {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: D_BEGIN ASC
   * @return List[Map] of subscription table rows
   */
  List getSubscriptionsBy(Map input) {
    LinkedHashMap params = mergeParams([
      subscriptionId     : null,
      customerId         : null,
      accountId          : null,
      docId              : null,
      goodId             : null,
      equipmentId        : null,
      parSubscriptionId  : null,
      prevSubscriptionId : null,
      isClosed           : null,
      operationDate      : null,
      beginDate          : null,
      endDate            : null,
      limit              : 0,
      order              : [d_begin: 'asc']
    ], input)
    LinkedHashMap where = [:]

    if (params.subscriptionId) {
      where.n_subscription_id = params.subscriptionId
    }
    if (params.customerId) {
      where.n_customer_id = params.customerId
    }
    if (params.accountId) {
      where.n_account_id = params.accountId
    }
    if (params.docId) {
      where.n_doc_id = params.docId
    }
    if (params.goodId) {
      where.n_service_id = params.goodId
    }
    if (params.equipmentId) {
      where.n_object_id = params.equipmentId
    }
    if (params.parSubscriptionId) {
      where.n_par_subscription_id = params.parSubscriptionId
    }
    if (params.prevSubscriptionId) {
      where.n_prev_subscription_id = params.prevSubscriptionId
    }
    if (params.isClosed != null) {
      where.c_fl_closed = encodeBool(params.isClosed)
    }
    if (params.beginDate) {
      where.d_begin = params.beginDate
    }
    if (params.endDate) {
      where.d_end = params.endDate
    }
    if (!params.operationDate && !params.endDate && !params.beginDate) {
      params.operationDate = local()
    }
    if (params.operationDate) {
      String oracleDate = encodeDateStr(params.operationDate)
      where[oracleDate] = [between: "d_begin and nvl(d_end, ${oracleDate})"]
    }
    return hid.getTableData(getSubscriptionsTable(), where: where, order: params.order, limit: params.limit)
  }

  /**
   * Search for subscription by different fields value
   * @param subscriptionId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param customerId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param accountId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docId              {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodId             {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param equipmentId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parSubscriptionId  {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param prevSubscriptionId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isClosed           {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param operationDate      {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: current date time, but only if beginDate and endDate are not set
   * @param beginDate          {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param endDate            {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param order              {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: D_BEGIN ASC
   * @return Map wth subscription table row
   */
  Map getSubscriptionBy(Map input) {
    return getSubscriptionsBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Search for subscriptions for parent subscription id
   * @param parSubscriptionId  {@link java.math.BigInteger BigInteger}
   * @param subscriptionId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param customerId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param accountId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param docId              {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodId             {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param equipmentId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param prevSubscriptionId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isClosed           {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param operationDate      {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: current date time, but only if beginDate and endDate are not set
   * @param beginDate          {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param endDate            {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit              {@link Integer}. Optional, default: 0 (unlimited)
   * @param order              {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: D_BEGIN ASC
   * @return List[Map] of subscription table rows
   */
  List getChildSubscriptions(Map input = [:], def parSubscriptionId) {
    return getSubscriptionsBy(input + [parSubscriptionId : parSubscriptionId])
  }

  /**
   * Get child subscription by id
   * @param childSubscriptionId {@link java.math.BigInteger BigInteger}
   * @deprecated use {@link #getSubscription(def)} instead
   */
  Map getChildSubscription(def childSubscriptionId) {
    return getSubscription(childSubscriptionId)
  }

  /**
   * Create or update subscription
   * @param subscriptionId     {@link java.math.BigInteger BigInteger}. Optional
   * @param customerId         {@link java.math.BigInteger BigInteger}. Optional
   * @param accountId          {@link java.math.BigInteger BigInteger}. Optional
   * @param accountId          {@link java.math.BigInteger BigInteger}. Optional
   * @param docId              {@link java.math.BigInteger BigInteger}. Optional
   * @param goodId             {@link java.math.BigInteger BigInteger}. Optional
   * @param equipmentId        {@link java.math.BigInteger BigInteger}. Optional
   * @param parSubscriptionId  {@link java.math.BigInteger BigInteger}. Optional
   * @param prevSubscriptionId {@link java.math.BigInteger BigInteger}. Optional
   * @param quant              {@link Integer}. Optional
   * @param payDay             {@link Integer}. Optional, possible values: 1..28
   * @param beginDate          {@link java.time.Temporal Any date type}. Optional
   * @param endDate            {@link java.time.Temporal Any date type}. Optional
   * @param chargeLogEndDate   {@link java.time.Temporal Any date type}. Optional
   * @param evaluateDiscounts  {@link Boolean}. Optional, default: true
   * @return Map with created or updated subscription (in Oracle API procedure notation)
   */
  private Map putSubscription(Map input) {
    LinkedHashMap defaultParams = [
      subscriptionId     : null,
      customerId         : null,
      accountId          : null,
      docId              : null,
      goodId             : null,
      equipmentId        : null,
      parSubscriptionId  : null,
      prevSubscriptionId : null,
      quant              : null,
      payDay             : null,
      beginDate          : null,
      endDate            : null,
      chargeLogEndDate   : null,
      evaluateDiscounts  : true
    ]

    LinkedHashMap existingSubscription = [:]
    if (notEmpty(input.subscriptionId)) {
      LinkedHashMap subscription = getSubscription(input.subscriptionId)
      existingSubscription += [
        subscriptionId     : subscription.n_subscription_id,
        customerId         : subscription.n_customer_id,
        accountId          : subscription.n_account_id,
        docId              : subscription.n_doc_id,
        goodId             : subscription.n_service_id,
        equipmentId        : subscription.n_object_id,
        parSubscriptionId  : subscription.n_par_subscription_id,
        prevSubscriptionId : subscription.n_prev_subscription_id,
        quant              : subscription.n_quant,
        payDay             : subscription.n_pay_day,
        beginDate          : subscription.d_begin,
        endDate            : subscription.d_end,
        chargeLogEndDate   : subscription.d_charge_log_end
      ]
    }

    LinkedHashMap params = mergeParams(defaultParams, existingSubscription + input)

    def unitId = getGoodUnitId(params.goodId)

    if (unitId == getPieceUnitId() && params.quant == null) {
      params.quant = 1
    } else if (unitId == getUnknownUnitId()) {
      params.quant = null
    }

    try {
      logger.info("Putting subscription with params ${params}")
      LinkedHashMap result = hid.execute('SI_USERS_PKG.SI_USER_GOODS_PUT', [
        num_N_SUBJ_GOOD_ID         : params.subscriptionId,
        num_N_GOOD_ID              : params.goodId,
        num_N_SUBJECT_ID           : params.customerId,
        num_N_ACCOUNT_ID           : params.accountId,
        num_N_OBJECT_ID            : params.equipmentId,
        num_N_DOC_ID               : params.docId,
        num_N_PAR_SUBJ_GOOD_ID     : params.parSubscriptionId,
        num_N_PAY_DAY              : params.payDay,
        num_N_QUANT                : params.quant,
        num_N_UNIT_ID              : params.unitId,
        dt_D_BEGIN                 : params.beginDate,
        dt_D_END                   : params.endDate,
        dt_D_CHARGE_LOG_END        : params.chargeLogEndDate,
        num_N_PREV_SUBSCRIPTION_ID : params.prevSubscriptionId,
        b_EvaluateDiscounts        : encodeFlag(params.evaluateDiscounts)
      ])
      logger.info("   Subscription ${result.num_N_SUBJ_GOOD_ID} was put successfully!")
      return result
    } catch (Exception e){
      logger.error("   Error while putting subscription!")
      logger.error_oracle(e)
      return null
    }
  }

  /**
   * Create subscription
   * @param customerId         {@link java.math.BigInteger BigInteger}
   * @param accountId          {@link java.math.BigInteger BigInteger}
   * @param docId              {@link java.math.BigInteger BigInteger}
   * @param goodId             {@link java.math.BigInteger BigInteger}
   * @param equipmentId        {@link java.math.BigInteger BigInteger}. Optional
   * @param parSubscriptionId  {@link java.math.BigInteger BigInteger}. Optional
   * @param prevSubscriptionId {@link java.math.BigInteger BigInteger}. Optional
   * @param quant              {@link Integer}. Optional
   * @param payDay             {@link Integer}. Optional, possible values: 1..28
   * @param beginDate          {@link java.time.Temporal Any date type}. Optional
   * @param endDate            {@link java.time.Temporal Any date type}. Optional
   * @param chargeLogEndDate   {@link java.time.Temporal Any date type}. Optional
   * @param evaluateDiscounts  {@link Boolean}. Optional, default: true
   * @return Map with created subscription (in Oracle API procedure notation)
   */
  Map createSubscription(Map input = [:], def customerId) {
    return putSubscription(input + [customerId: customerId])
  }

  /**
   * Update subscription
   * @param subscriptionId     {@link java.math.BigInteger BigInteger}
   * @param quant              {@link Integer}. Optional
   * @param payDay             {@link Integer}. Optional, possible values: 1..28
   * @param endDate            {@link java.time.Temporal Any date type}. Optional
   * @param chargeLogEndDate   {@link java.time.Temporal Any date type}. Optional
   * @return Map with updated subscription (in Oracle API procedure notation)
   */
  Map updateSubscription(Map input = [:], def subscriptionId) {
    return putSubscription(input + [subscriptionId: subscriptionId])
  }

  /**
   * Create child subscription
   * @param parSubscriptionId  {@link java.math.BigInteger BigInteger}
   * @param customerId         {@link java.math.BigInteger BigInteger}
   * @param accountId          {@link java.math.BigInteger BigInteger}
   * @param docId              {@link java.math.BigInteger BigInteger}
   * @param goodId             {@link java.math.BigInteger BigInteger}
   * @param equipmentId        {@link java.math.BigInteger BigInteger}. Optional
   * @param parSubscriptionId  {@link java.math.BigInteger BigInteger}. Optional
   * @param prevSubscriptionId {@link java.math.BigInteger BigInteger}. Optional
   * @param quant              {@link Integer}. Optional
   * @param payDay             {@link Integer}. Optional, possible values: 1..28
   * @param beginDate          {@link java.time.Temporal Any date type}. Optional
   * @param endDate            {@link java.time.Temporal Any date type}. Optional
   * @param chargeLogEndDate   {@link java.time.Temporal Any date type}. Optional
   * @param evaluateDiscounts  {@link Boolean}. Optional, default: true
   * @return Map with created child subscription (in Oracle API procedure notation)
   */
  Map createChildSubscription(Map input = [:], def parSubscriptionId) {
    return putSubscription(input + [parSubscriptionId: parSubscriptionId])
  }

  /**
   * Update child subscription
   * @see #updateSubscription(Map,def)
   */
  Map updateChildSubscription(Map input = [:], def childSubscriptionId) {
    return updateSubscription(input, childSubscriptionId)
  }

  /**
   * Close subscription
   * @param subscriptionId  {@link java.math.BigInteger BigInteger}
   * @param endDate         {@link java.time.Temporal Any date type}. Optional, default: current datetime
   * @param closeChargeLog  {@link Boolean}. Optional, default: false
   * @return True if subscription was closed successfully, false otherwise
   */
  Boolean closeSubscription(Map input = [:], def subscriptionId) {
    LinkedHashMap params = [
      endDate        : local(),
      closeChargeLog : false
    ] + input
    try {
      logger.info("Closing subscription ${subscriptionId} with date ${endDate}")
      hid.execute('SI_USERS_PKG.SI_USER_GOODS_CLOSE', [
        num_N_SUBJ_GOOD_ID : subscriptionId,
        dt_D_END           : params.endDate,
        b_ChargeLogEnd     : encodeFlag(params.closeChargeLog ?: params.immediate),
      ])
      logger.info("   Subscription closed successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while closing subscription!")
      logger.error_oracle(e)
      return false
    }
  }

  /**
   * Close child subscription
   * @see #closeSubscription(Map,def)
   */
  Boolean closeChildSubscription(Map input = [:], def childSubscriptionId) {
    return closeSubscription(input, childSubscriptionId)
  }

  /**
   * Close subscription and charge logs
   * @param subscriptionId  {@link java.math.BigInteger BigInteger}
   * @param endDate         {@link java.time.Temporal Any date type}. Optional, default: current datetime
   * @return True if subscription and charge logs were closed successfully, false otherwise
   */
  Boolean closeSubscriptionForce(def subscriptionId, Temporal endDate = local()) {
    Boolean result = closeSubscription(subscriptionId, endDate: endDate, closeChargeLog: true)

    def chargeLogId = getChargeLogIdBySubscription(subscriptionId: subscriptionId, operationDate: endDate)
    if (chargeLogId) {
      result = closeChargeLog(docId: chargeLogId, endDate: endDate)
    }
    return result
  }

  /**
   * Delete subscription
   * @param subscriptionId {@link java.math.BigInteger BigInteger}
   * @return True if subscription was deleted successfully, false otherwise
   */
  Boolean deleteSubscription(def subscriptionId) {
    try {
      logger.info("Deleting subscription ${subscriptionId}")
      hid.execute('SI_USERS_PKG.SI_USER_GOODS_DEL', [
        num_N_SUBJ_GOOD_ID: subscriptionId
      ])
      logger.info("   Subscription deleted successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while deleting subscription!")
      logger.error_oracle(e)
      return false
    }
  }

  /**
   * Delete subscription and all charge logs
   * @param subscriptionId {@link java.math.BigInteger BigInteger}
   * @return True if subscription and charge logs were deleted successfully, false otherwise
   */
  Boolean deleteSubscriptionForce(def subscriptionId) {
    Boolean result = true
    List chargeLogs = getChargeLogsBySubscription(subscriptionId: subscriptionId)

    chargeLogs.each { Map chargeLog ->
      Boolean cancelResult = cancelChargeLog(chargeLog.n_doc_id)
      result = result && cancelResult
    }

    Boolean deleteResult = deleteSubscription(subscriptionId)

    result = result && deleteResult
    return result
  }
}
