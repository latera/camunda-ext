package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Oracle.encodeBool
import static org.camunda.latera.bss.utils.Oracle.encodeFlag
import static org.camunda.latera.bss.utils.Oracle.encodeDateStr
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

  Map getSubscriptionBy(Map input) {
    return getSubscriptionsBy(input + [limit: 1])?.getAt(0)
  }

  List getChildSubscriptions(Map input = [:], def parSubscriptionId) {
    return getSubscriptionsBy(input + [parSubscriptionId : parSubscriptionId])
  }

  Map getChildSubscription(def childSubscriptionId) {
    return getSubscription(childSubscriptionId)
  }

  private Map putSubscription(Map input) {
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

  Map createSubscription(Map input = [:], def customerId) {
    return putSubscription(input + [customerId: customerId])
  }

  Map updateSubscription(Map input = [:], def subscriptionId) {
    return putSubscription(input + [subscriptionId: subscriptionId])
  }

  Map createChildSubscription(Map input = [:], def parSubscriptionId) {
    return putSubscription(input + [parSubscriptionId: parSubscriptionId])
  }

  Map updateChildSubscription(Map input = [:], def childSubscriptionId) {
    return updateSubscription(input, childSubscriptionId)
  }

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
        b_InvoiceEnd       : encodeFlag(params.closeChargeLog ?: params.immediate),
      ])
      logger.info("   Subscription closed successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while closing subscription!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean closeChildSubscription(Map input = [:], def childSubscriptionId) {
    return closeSubscription(input, childSubscriptionId)
  }

  Boolean closeSubscriptionForce(def subscriptionId, Temporal endDate = local()) {
    Boolean result = closeSubscription(subscriptionId, endDate: endDate, closeChargeLog: true)

    def invoiceId = getInvoiceIdBySubscription(subscriptionId: subscriptionId, operationDate: endDate)
    if (invoiceId) {
      result = closeInvoice(docId: invoiceId, endDate: endDate)
    }
    return result
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
    List invoices = getInvoicesBySubscription(subscriptionId: subscriptionId)
    invoices.each { Map invoice ->
      Boolean cancelResult = cancelInvoice(invoice.n_doc_id)
      result = result && cancelResult
    }
    Boolean deleteResult = deleteSubscription(subscriptionId)
    result = result && deleteResult
    return result
  }
}