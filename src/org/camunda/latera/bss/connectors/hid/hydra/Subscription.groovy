package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Oracle.*
import static org.camunda.latera.bss.utils.DateTimeUtil.local
import java.time.temporal.Temporal

trait Subscription {
  private static String SUBSCRIPTIONS_TABLE = 'SI_V_SUBSCRIPTIONS'

  String getSubscriptionsTable() {
    return SUBSCRIPTIONS_TABLE
  }

  Map getSubscription(def subscriptionId) {
    LinkedHashMap where = [
      n_subscription_id: subscriptionId
    ]
    return hid.getTableFirst(getSubscriptionsTable(), where: where)
  }

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
      endDate            : null
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
      where[oracleDate] = [BETWEEN: "D_BEGIN AND NVL(D_END, ${oracleDate})"]
    }
    LinkedHashMap order = [d_begin: 'asc']
    return hid.getTableData(getSubscriptionsTable(), where: where, order: order)
  }

  Map getSubscriptionBy(Map input) {
    return getSubscriptionsBy(input)?.getAt(0)
  }

  List getChildSubscriptions(def customerId, def subscriptionId, Map input = [:]) {
    return getSubscriptionsBy(
      input + [
        customerId        : customerId,
        parSubscriptionId : subscriptionId
      ]
    )
  }

  Map getChildSubscription(def customerId, def subscriptionId, def childSubscriptionId) {
    return getSubscription(childSubscriptionId)
  }

  Map putSubscription(Map input) {
    LinkedHashMap params = mergeParams([
      subscriptionId      : null,
      customerId          : null,
      accountId           : null,
      docId               : null,
      goodId              : null,
      equipmentId         : null,
      parSubscriptionId   : null,
      prevSubscriptionId  : null,
      quant               : null,
      payDay              : null,
      beginDate           : null,
      endDate             : null,
      invoiceEndDate      : null,
      evaluateDiscounts   : true
    ], input)
    def unitId = getGoodUnitId(params.goodId)
    if (unitId == getPieceUnitId() && params.quant == null) {
      params.quant = 1
    } else if (unitId == getUnknownUnitId()) {
      params.quant = null
    }
    try {
      logger.info("Putting subscription with params ${params}")
      LinkedHashMap subscription = hid.execute('SI_USERS_PKG.SI_USER_GOODS_PUT', [
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
        dt_D_INVOICE_END           : params.invoiceEndDate,
        num_N_PREV_SUBSCRIPTION_ID : params.prevSubscriptionId,
        b_EvaluateDiscounts        : encodeFlag(params.evaluateDiscounts)
      ])
      logger.info("   Subscription ${subscription.num_N_SUBJ_GOOD_ID} was put successfully!")
      return subscription
    } catch (Exception e){
      logger.error("   Error while putting subscription!")
      logger.error_oracle(e)
      return null
    }
  }

  Map putSubscription(def customerId, Map input) {
    return putSubscription(input + [customerId: customerId])
  }

  Map putSubscription(Map input, def customerId) {
    return putSubscription(customerId, input)
  }

  Map createSubscription(Map input) {
    input.remove('subscriptionId')
    return putSubscription(input)
  }

  Map createSubscription(def customerId, Map input) {
    return createSubscription(input + [customerId: customerId])
  }

  Map createSubscription(Map input, def customerId) {
    return createSubscription(customerId, input)
  }

  Map updateSubscription(Map input) {
    return putSubscription(input)
  }

  Map updateSubscription(def customerId, def subscriptionId, Map input) {
    return updateSubscription(input + [subscriptionId: subscriptionId, customerId: customerId])
  }

  Map updateSubscription(Map input, def customerId, def subscriptionId) {
    return updateSubscription(customerId, subscriptionId, input)
  }

  Map putChildSubscription(Map input) {
    return putSubscription(input)
  }

  Map putChildSubscription(def customerId, Map input) {
    putChildSubscription(input + [customerId: customerId])
  }

  Map putChildSubscription(Map input, def customerId) {
    return putChildSubscription(customerId, input)
  }

  Map putChildSubscription(def customerId, def parSubscriptionId, Map input) {
    putChildSubscription(input + [parSubscriptionId: parSubscriptionId, customerId: customerId])
  }

  Map putChildSubscription(Map input, def customerId, def parSubscriptionId) {
    return putChildSubscription(customerId, parSubscriptionId, input)
  }

  Map createChildSubscription(Map input) {
    def customerId = input.customerId
    input.remove('customerId')
    def parSubscriptionId = input.subscriptionId ?: input.parSubscriptionId
    input.remove('subscriptionId')
    input.remove('parSubscriptionId')
    return putChildSubscription(customerId, parSubscriptionId, input)
  }

  Map createChildSubscription(def customerId, def subscriptionId, Map input = [:]) {
    return createChildSubscription(input + [parSubscriptionId: subscriptionId, customerId: customerId])
  }

  Map createChildSubscription(Map input, def customerId, def subscriptionId) {
    return createChildSubscription(customerId, subscriptionId, input)
  }

  Map updateChildSubscription(Map input) {
    def customerId = input.customerId
    input.remove('customerId')
    def parSubscriptionId = input.parSubscriptionId
    input.remove('parSubscriptionId')
    def subscriptionId = input.subscriptionId ?: input.childSubscriptionId
    input.remove('subscriptionId')
    input.remove('childSubscriptionId')
    return putChildSubscription(customerId, parSubscriptionId, input + [subscriptionId: subscriptionId])
  }

  Map updateChildSubscription(def customerId, def subscriptionId, def childSubscriptionId, Map input = [:]) {
    return updateChildSubscription(input + [customerId: customerId, parSubscriptionId: subscriptionId, subscriptionId: childSubscriptionId])
  }

  Map updateChildSubscription(Map input, def customerId, def subscriptionId, def childSubscriptionId) {
    return updateChildSubscription(customerId, subscriptionId, childSubscriptionId, input)
  }

  Map putOneOffSubscription(Map input) {
    LinkedHashMap params = mergeParams([
      accountId           : null,
      docId               : null,
      goodId              : null,
      equipmentId         : null,
      parSubscriptionId   : null,
      quant               : 1,
      beginDate           : null,
      charge              : true
    ], input)
    def unitId = getGoodUnitId(params.goodId)
    if (unitId == getPieceUnitId() && params.quant == null) {
      params.quant = 1
    } else if (unitId == getUnknownUnitId()) {
      params.quant = null
    }
    try {
      logger.info("Putting one-off subscription with params ${params}")
      LinkedHashMap subscription = hid.execute('US_SPECIAL_PKG.ADD_ONE_OFF_SUBSCRIPTION', [
        num_N_SUBSCRIPTION_ID      : null,
        num_N_ACCOUNT_ID           : params.accountId,
        num_N_CONTRACT_ID          : params.docId,
        num_N_SERVICE_ID           : params.goodId,
        num_N_OBJECT_ID            : params.equipmentId,
        num_N_PAR_SUBSCRIPTION_ID  : params.parSubscriptionId,
        num_N_QUANT                : params.quant,
        dt_D_BEGIN                 : params.beginDate,
        b_Charge                   : encodeFlag(params.charge)
      ])
      logger.info("   One-off Subscription ${subscription.num_N_SUBSCRIPTION_ID} was put successfully!")
      return subscription
    } catch (Exception e){
      logger.error("   Error while putting one-off subscription!")
      logger.error_oracle(e)
      return null
    }
  }

  Boolean closeSubscription(
    def      subscriptionId,
    Temporal endDate = local(),
    Boolean  closeChargeLog = false
  ) {
    try {
      logger.info("Closing subscription ${subscriptionId} with date ${endDate}")
      hid.execute('SI_USERS_PKG.SI_USER_GOODS_CLOSE', [
        num_N_SUBJ_GOOD_ID : subscriptionId,
        dt_D_END           : endDate,
        b_InvoiceEnd       : encodeFlag(setInvoiceDate),
      ])
      logger.info("   Subscription closed successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while closing subscription!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean closeSubscription(Map input) {
    LinkedHashMap params = mergeParams([
      subscriptionId : null,
      endDate        : local(),
      closeChargeLog : false
    ], input)
    return closeSubscription(params.subscriptionId, params.endDate, params.closeChargeLog ?: params.setInvoiceDate)
  }

  Map closeSubscription(def customerId, def subscriptionId, Temporal endDate, Boolean closeChargeLog = false) {
    return closeSubscription(subscriptionId, endDate, closeChargeLog)
  }

  Boolean closeSubscriptionForce(
    def subscriptionId,
    Temporal endDate = local()
  ) {
    Boolean result = closeSubscription(subscriptionId, endDate, true)

    def invoiceId = getInvoiceIdBySubscription(subscriptionId: subscriptionId, operationDate: endDate)
    if (invoiceId) {
      result = closeInvoice(docId: invoiceId, endDate: endDate)
    }
    return result
  }

  Boolean closeSubscriptionForce(Map input) {
    LinkedHashMap params = mergeParams([
      subscriptionId : null,
      endDate        : local()
    ], input)
    return closeSubscriptionForce(params.subscriptionId, params.endDate)
  }

  Map closeChildSubscription(def customerId, def subscriptionId, def childSubscriptionId, Temporal endDate = local(), Boolean immediate = false) {
    return closeSubscriptionForce(childSubscriptionId, endDate, immediate)
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

  Boolean deleteSubscriptionForce(def subscriptionId) {
    Boolean result = true
    List invoices = getInvoicesBySubscription(subscriptionId)
    invoices.each { it ->
      Boolean cancelResult = cancelInvoice(it.n_doc_id)
      result = result && cancelResult
    }
    Boolean deleteResult = deleteSubscription(subscriptionId)
    result = result && deleteResult
    return result
  }
}