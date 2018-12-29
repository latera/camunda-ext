package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.utils.Oracle
import org.camunda.latera.bss.utils.DateTimeUtil

trait Subscription {
  private static String SUBSCRIPTIONS_TABLE = 'SI_V_SUBSCRIPTIONS'

  def getSubscriptionsTable() {
    return SUBSCRIPTIONS_TABLE
  }

  LinkedHashMap getSubscription(def subscriptionId) {
    LinkedHashMap where = [
      n_subscription_id: subscriptionId
    ]
    return hid.getTableFirst(getSubscriptionsTable(), where: where)
  }

  List getSubscriptionsBy(
    LinkedHashMap input
  ) {
    LinkedHashMap params = mergeParams([
      customerId        : null,
      accountId         : null,
      docId             : null,
      goodId            : null,
      equipmentId       : null,
      parSubscriptionId : null,
      isClosed          : null,
      operationDate     : null
    ], input)
    LinkedHashMap where = [:]
    
    if (params.customerId != null) {
      where.n_customer_id = params.customerId
    }
    if (params.accountId != null) {
      where.n_account_id = params.accountId
    }
    if (params.docId != null) {
      where.n_doc_id = params.docId
    }
    if (params.goodId != null) {
      where.n_service_id = params.goodId
    }
    if (params.equipmentId != null) {
      where.n_object_id = params.equipmentId
    }
    if (params.parSubscriptionId) {
      where.n_par_subscription_id = params.parSubscriptionId
    }
    if (params.isClosed) {
      where.c_fl_closed = Oracle.encodeBool(params.isClosed)
    }
    if (params.operationDate) {
      where[operationDate] = [BETWEEN: "D_BEGIN AND NVL(D_END, ${params.operationDate})"]
    }
    return hid.getTableData(getSubscriptionsTable(), where: where)
  }

  LinkedHashMap getSubscriptionBy(
    LinkedHashMap input
  ) {
    return getSubscriptionsBy(input)?.getAt(0)
  }

  LinkedHashMap putSubscription(LinkedHashMap input) {
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
      evaluateDiscounts   : true,
      useMaterializedView : true
    ], input)
    def unitId = getGoodUnitId(goodId)
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
        b_EvaluateDiscounts        : Oracle.encodeFlag(params.evaluateDiscounts),
        b_UseMaterializedView      : Oracle.encodeFlag(params.useMaterializedView)
      ])
      logger.info("   Subscription ${subscription.num_N_SUBJ_GOOD_ID} was put successfully!")
      return subscription
    } catch (Exception e){
      logger.error("Error while putting subscription!")
      logger.error(e)
      return null
    }
  }

  void closeSubscription(
    def     subscriptionId,
    def     endDate = DateTimeUtil.now(),
    Boolean setInvoiceDate = false) {
    try {
      logger.info("Closing subscription ${subscriptionId} with date ${endDate}")
      hid.execute('SI_USERS_PKG.SI_USER_GOODS_CLOSE', [
        num_N_SUBJ_GOOD_ID : subscriptionId,
        dt_D_END           : endDate,
        b_InvoiceEnd       : Oracle.encodeFlag(setInvoiceDate),
      ])
      logger.info("   Subscription closed successfully!")
    } catch (Exception e){
      logger.error("Error while closing subscription!")
      logger.error(e)
    }
  }

  void closeSubscription(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      subscriptionId : null,
      endDate        : DateTimeUtil.now(),
      setInvoiceDate : false
    ], input)
    closeSubscription(params.subscriptionId, params.endDate, params.setInvoiceDate)
  }

  void deleteSubscription(def subscriptionId) {
    try {
      logger.info("Deleting subscription ${subscriptionId}")
      hid.execute('SI_USERS_PKG.SI_USER_GOODS_DEL', [
        num_N_SUBJ_GOOD_ID: subscriptionId
      ])
      logger.info("   Subscription deleted successfully!")
    } catch (Exception e){
      logger.error("Error while deleting subscription!")
      logger.error(e)
    }
  }
}