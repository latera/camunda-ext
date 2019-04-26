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

  List getSubscriptionsBy(LinkedHashMap input) {
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
      where.c_fl_closed = Oracle.encodeBool(params.isClosed)
    }
    if (params.beginDate) {
      where.d_begin = params.beginDate
    }
    if (params.endDate) {
      where.d_end = params.endDate
    }
    if (!params.operationDate && !params.endDate && !params.beginDate) {
      params.operationDate = DateTimeUtil.now()
    }
    if (params.operationDate) {
      String oracleDate = Oracle.encodeDateStr(params.operationDate)
      where[oracleDate] = [BETWEEN: "D_BEGIN AND NVL(D_END, ${oracleDate})"]
    }
    def order = [d_begin: 'asc']
    return hid.getTableData(getSubscriptionsTable(), where: where, order: order)
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
        b_EvaluateDiscounts        : Oracle.encodeFlag(params.evaluateDiscounts)
      ])
      logger.info("   Subscription ${subscription.num_N_SUBJ_GOOD_ID} was put successfully!")
      return subscription
    } catch (Exception e){
      logger.error("   Error while putting subscription!")
      logger.error_oracle(e)
      return null
    }
  }

  LinkedHashMap putOneOffSubscription(LinkedHashMap input) {
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
        b_Charge                   : Oracle.encodeFlag(params.charge)
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
    def     subscriptionId,
    def     endDate = DateTimeUtil.now(),
    Boolean setInvoiceDate = false
  ) {
    try {
      logger.info("Closing subscription ${subscriptionId} with date ${endDate}")
      hid.execute('SI_USERS_PKG.SI_USER_GOODS_CLOSE', [
        num_N_SUBJ_GOOD_ID : subscriptionId,
        dt_D_END           : endDate,
        b_InvoiceEnd       : Oracle.encodeFlag(setInvoiceDate),
      ])
      logger.info("   Subscription closed successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while closing subscription!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean closeSubscription(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      subscriptionId : null,
      endDate        : DateTimeUtil.now(),
      setInvoiceDate : false
    ], input)
    return closeSubscription(params.subscriptionId, params.endDate, params.setInvoiceDate)
  }

  Boolean closeSubscriptionForce(
    def subscriptionId,
    def endDate = DateTimeUtil.now()
  ) {
    def result = closeSubscription(subscriptionId, endDate, true)

    def invoiceId = getInvoiceIdBySubscription(subscriptionId: subscriptionId, operationDate: endDate)
    if (invoiceId) {
       result = closeInvoice(docId: invoiceId, endDate: endDate)
    }
    return result
  }

  Boolean closeSubscriptionForce(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      subscriptionId : null,
      endDate        : DateTimeUtil.now()
    ], input)
    return closeSubscriptionForce(params.subscriptionId, params.endDate)
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
    def result = true
    def invoices = getInvoicesBySubscription(subscriptionId)
    invoices.each { it ->
      def cancelResult = cancelInvoice(it.n_doc_id)
      result = result && cancelResult
    }
    def deleteResult = deleteSubscription(subscriptionId)
    result = result && deleteResult
    return result
  }
}