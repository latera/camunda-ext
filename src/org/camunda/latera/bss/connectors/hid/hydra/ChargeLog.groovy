package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.DateTimeUtil.local
import static org.camunda.latera.bss.utils.Oracle.encodeDateStr
import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import static org.camunda.latera.bss.utils.Constants.DOC_TYPE_ChargeLog
import static org.camunda.latera.bss.utils.Constants.GM_TYPE_Charged
import static org.camunda.latera.bss.utils.Constants.GM_TYPE_Reserve
import static org.camunda.latera.bss.utils.Constants.GM_TYPE_Cancelled
import static org.camunda.latera.bss.utils.Constants.WFLOW_ChargeLog
import java.time.temporal.Temporal

/**
  * Charge logs specific methods
  */
trait ChargeLog {
  private static String  CHARGE_LOGS_TABLE      = 'SD_V_CHARGE_LOGS_T'
  private static String  GOOD_MOVES_TABLE       = 'SD_V_GOOD_MOVES_T'
  private static String  CHARGE_LOG_LINES_TABLE = 'SD_V_CHARGE_LOGS_C'

  /**
   * Get charge logs table name
   */
  String getChargeLogsTable() {
    return CHARGE_LOGS_TABLE
  }

  /**
   * Get charge log content table name
   */
  String getGoodMovesTable() {
    return GOOD_MOVES_TABLE
  }

  /**
   * Get charge log lines table name
   */
  String getChargeLogLinesTable() {
    return CHARGE_LOG_LINES_TABLE
  }

  /**
   * Get charge log document type ref code
   */
  String getChargeLogType() {
    return getRefCode(getChargeLogTypeId())
  }

  /**
   * Get charge log document type ref id
   */
  Number getChargeLogTypeId() {
    return DOC_TYPE_ChargeLog
  }

  /**
   * Get charge log line state ref code
   */
  String getChargeChargedType() {
    return getRefCode(getChargeChargedTypeId())
  }

  /**
   * Get charge log line state ref id
   */
  Number getChargeChargedTypeId() {
    return GM_TYPE_Charged
  }

  /**
   * Get charge log line Reserved state type ref code
   */
  String getChargeReservedType() {
    return getRefCode(getChargeReservedTypeId())
  }

  /**
   * Get charge log line Reserved state type ref id
   */
  Number getChargeReservedTypeId() {
    return GM_TYPE_Reserve
  }

  /**
   * Get charge log line Canceled state type ref code
   */
  String getChargeCanceledType() {
    return getRefCode(getChargeCanceledTypeId())
  }

  /**
   * Get charge log line Canceled state type ref id
   */
  Number getChargeCanceledTypeId() {
    return GM_TYPE_Cancelled
  }

  /**
   * Get charge log document default workflow code
   */
  String getDefaultChargeLogWorkflow() {
    return getRefCode(getDefaultChargeLogWorkflowId())
  }

  /**
   * Get charge log document default workflow id
   */
  Number getDefaultChargeLogWorkflowId() {
    return WFLOW_ChargeLog
  }

  /**
   * Get charge log by id
   * @param docId {@link java.math.BigInteger BigInteger}
   * @return Ccharge log table row
   */
  Map getChargeLog(def docId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return hid.getTableFirst(getChargeLogsTable(), where: where)
  }

  /**
   * Search for charge logs by different fields value
   * @see Document#getDocumentsBy(Map)
   */
  List getChargeLogsBy(Map input) {
    input.docTypeId = getChargeLogTypeId()
    return getDocumentsBy(input)
  }

  /**
   * Search for one charge log by different fields value
   * @see Document#getDocumentBy(Map)
   */
  Map getChargeLogBy(Map input) {
    input.docTypeId = getChargeLogTypeId()
    return getDocumentBy(input)
  }

  /**
   * Get charge log id for subscription
   * @param subscriptionId {@link java.math.BigInteger BigInteger}
   * @param operationDate {@link java.time.Temporal Any date type}. Optional. Default: current datetime
   * @return Charge log id or null
   */
  Number getChargeLogIdBySubscription(Map input) {
    LinkedHashMap params = mergeParams([
      subscriptionId : null,
      operationDate  : local()
    ], input)
    try {
      def docId = hid.queryFirst("""
      SELECT
        SD_CHARGE_LOGS_PKG_S.GET_CHARGE_LOG_ID_BY_SUBJ_GOOD(
          num_N_SUBJ_GOOD_ID => ${params.subscriptionId},
          dt_D_OPER => ${encodeDateStr(params.operationDate)}
        )
      FROM DUAL""")[0]
      return toIntSafe(docId)
    } catch (Exception e){
      logger.error_oracle(e)
      return null
    }
  }

  /**
   * Get charge log id for subscription
   *
   * Overload with positional args
   * @see #getChargeLogIdBySubscription(Map)
   */
  Number getChargeLogIdBySubscription(def subscriptionId, Temporal operationDate = local()) {
    return getChargeLogIdBySubscription(subscriptionId: subscriptionId, operationDate: operationDate)
  }

  /**
   * Get charge log for subscription
   * @param subscriptionId {@link java.math.BigInteger BigInteger}
   * @param operationDate {@link java.time.Temporal Any date type}. Optional. Default: current datetime
   * @return Charge log table row
   */
  Map getChargeLogBySubscription(Map input) {
    def docId = getChargeLogIdBySubscription(input)
    if (docId == null) {
      return null
    }
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return hid.getTableFirst(getChargeLogsTable(), where: where)
  }

  /**
   * Get charge log for subscription
   *
   * Overload with positional args
   * @see #getChargeLogBySubscription(Map)
   */
  Map getChargeLogBySubscription(def subscriptionId, Temporal operationDate = local()) {
    return getChargeLogBySubscription(subscriptionId: subscriptionId, operationDate: operationDate)
  }

  /**
   * Get charge logs for subscription
   * @param subscriptionId {@link java.math.BigInteger BigInteger}
   * @param stateId        {@link java.math.BigInteger BigInteger}. Optional. Default: not canceled
   * @param state          {@link CharSequence String}. Optional
   * @param operationDate  {@link java.time.Temporal Any date type}. Optional. Default: current datetime
   * @param limit          {@link Integer}. Optional. Default: 0 (unlimited)
   * @return Charge log content table rows
   */
  List<Map> getChargeLogsBySubscription(Map input) {
    LinkedHashMap params = mergeParams([
      subscriptionId : null,
      stateId        : ['not in': [getDocumentStateCanceledId()]],
      operationDate  : null,
      limit          : 0
    ], input)
    LinkedHashMap where = [
      n_subj_good_id: params.subscriptionId
    ]
    if (params.stateId) {
      where.n_doc_state_id = params.stateId
    }
    if (params.operationDate) {
      String oracleDate = encodeDateStr(params.operationDate)
      where[oracleDate] = [between: "d_begin and nvl(d_end, ${oracleDate})"]
    }
    return hid.getTableData(getGoodMovesTable(), where: where, order: params.order, limit: params.limit)
  }

  /**
   * Get charge logs for subscription
   *
   * Overload with positional args
   * @see #getChargeLogsBySubscription(Map)
   */
  List getChargeLogsBySubscription(def subscriptionId, def stateId = ['not in': [getDocumentStateCanceledId()]], def operationDate = null) {
    return getChargeLogsBySubscription(subscriptionId: subscriptionId, stateId: stateId, operationDate: operationDate)
  }

  /**
   * Check if entity or entity type is charge log
   * @param entityOrEntityType {@link java.math.BigInteger BigInteger} or {@link CharSequence String}. Document id, document type ref id or document type ref code
   * @return True if given value is charge log, false otherwise
   */
  Boolean isChargeLog(def entityOrEntityType) {
    if (entityOrEntityType == null) {
      return false
    }

    Number entityIdOrEntityTypeId = toIntSafe(entityOrEntityType)
    if (entityIdOrEntityTypeId != null) {
      return entityIdOrEntityTypeId == getChargeLogTypeId() || getDocument(entityIdOrEntityTypeId)?.n_doc_type_id == getChargeLogTypeId()
    } else {
      return entityOrEntityType == getChargeLogType()
    }
  }

  /**
   * Set charge log end date
   * @param docId         {@link java.math.BigInteger BigInteger}
   * @param endDate       {@link java.time.Temporal Any date type}. Optional. Default: current datetime
   * @param closeReasonId {@link java.math.BigInteger BigInteger}. Optional
   * @param closeReason   {@link CharSequence String}. Optional
   * @return True if charge log end date was changed successfully
   */
  Boolean changeChargeLogEnd(Map input) {
    LinkedHashMap params = mergeParams([
      docId         : null,
      endDate       : null,
      closeReasonId : null
    ], input)
    try {
      logger.info("Changing charge log id ${params.docId} end date to ${params.endDate} with reason ${params.closeReasonId}")
      LinkedHashMap chargeLog = hid.execute('SD_CHARGE_LOGS_PKG.CHANGE_CHARGE_LOG_PERIOD', [
        num_N_DOC_ID          : params.docId,
        dt_D_OPER             : params.endDate,
        num_N_CLOSE_REASON_ID : params.closeReasonId
      ])
      logger.info("   Charge log ${chargeLog.num_N_DOC_ID} end date was changed successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while changing charge log end date!")
      logger.error_oracle(e)
      return false
    }
  }

  /**
   * Set charge log end date
   *
   * Overload with positional args
   * @see #changeChargeLogEnd(Map)
   */
  Boolean changeChargeLogEnd(def docId, Temporal endDate = local(), def closeReasonId = null) {
    return changeChargeLogEnd(docId: docId, endDate: endDate, closeReasonId: closeReasonId)
  }

  /**
   * Close charge log
   * @see #changeChargeLogEnd(Map)
   */
  Boolean closeChargeLog(Map input) {
    return changeChargeLogEnd(input)
  }

  /**
   * Close charge log
   *
   * Overload with positional args
   * @see #closeChargeLog(Map)
   */
  Boolean closeChargeLog(def docId, Temporal endDate = local(), def closeReasonId = null) {
    return changeChargeLogEnd(docId: docId, endDate: endDate, closeReasonId: closeReasonId)
  }

  /**
   * Change charge log state to Canceled
   * @param docId {@link java.math.BigInteger BigInteger}
   * @return True if charge log state was changed successfully
   */
  Boolean cancelChargeLog(def docId) {
    try {
      logger.info("Cancelling charge log id ${docId}")
      hid.execute('SD_CHARGE_LOGS_PKG.CANCEL_CHARGE_LOG', [
        num_N_CHARGE_LOG_ID : docId
      ])
      logger.info("   Charge log was cancelled successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while changing charge log end date!")
      logger.error_oracle(e)
      return false
    }
  }

  /**
   * Search for charge log lines by different fields value
   * @param docId           {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lineId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lineNumber      {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parLineId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subcriptionId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param baseGoodId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param objectId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param moveTypeId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. Default: not cancelled
   * @param moveType        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param unitId          {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param unit            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param unitBaseId      {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param unitBase        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param taxRateId       {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param taxRate         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param currencyId      {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param currency        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param quant           {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param quantBase       {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param price           {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param priceWoTax      {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param addressId       {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param baseSum         {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param baseSumWoTax    {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param sum             {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param sumTax          {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param sumWoTax        {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param discountLineId  {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param discountDocId   {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param priceLineId     {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param priceOrderId    {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param provisionRuleId {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param operationDate   {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param beginDate       {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param endDate         {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit           {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order           {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: N_LINE_NO DESC
   * @return Charge log line table rows
   */
  List<Map> getChargeLogLinesBy(Map input) {
    LinkedHashMap params = mergeParams([
      docId             : null,
      lineId            : null,
      lineNumber        : null,
      parLineId         : null,
      subcriptionId     : null,
      goodId            : null,
      baseGoodId        : null,
      objectId          : null,
      moveTypeId        : ['not in': [getChargeCanceledTypeId()]],
      unitId            : null,
      unitBaseId        : null,
      taxRateId         : null,
      currencyId        : null,
      quant             : null,
      quantBase         : null,
      price             : null,
      priceWoTax        : null,
      addressId         : null,
      baseSum           : null,
      baseSumWoTax      : null,
      sum               : null,
      sumTax            : null,
      sumWoTax          : null,
      discountLineId    : null,
      discountDocId     : null,
      priceLineId       : null,
      priceOrderId      : null,
      provisionRuleId   : null,
      operationDate     : null,
      beginDate         : null,
      endDate           : null,
      limit             : 0,
      order             : [n_line_no: 'asc']
    ], input)
    LinkedHashMap where = [:]

    if (params.docId) {
      where.n_doc_id = params.docId
    }
    if (params.lineId) {
      where.n_price_line_id = params.lineId
    }
    if (params.lineNumber) {
      where.n_line_no = params.lineNumber
    }
    if (params.parLineId) {
      where.n_par_line_id = params.parLineId
    }
    if (params.subscriptionId) {
      where.n_subj_good_id = params.subscriptionId
    }
    if (params.goodId) {
      where.n_good_id = params.goodId
    }
    if (params.baseGoodId) {
      where.n_base_good_id = params.baseGoodId
    }
    if (params.objectId) {
      where.n_object_id = params.objectId
    }
    if (params.moveTypeId) {
      where.n_move_type_id = params.moveTypeId
    }
    if (params.unitId) {
      where.n_unit_id = params.unitId
    }
    if (params.unitBaseId) {
      where.n_unit_id = params.unitId
    }
    if (params.taxRateId) {
      where.n_tax_rate_id = params.taxRateId
    }
    if (params.currencyId) {
      where.n_tax_rate_id = params.currencyId
    }
    if (params.quant) {
      where.n_quant = params.quant
    }
    if (params.quantBase) {
      where.n_quant = params.quantBase
    }
    if (params.price) {
      where.n_price = params.price
    }
    if (params.priceWoTax) {
      where.n_price_wo_tax = params.priceWoTax
    }
    if (params.addressId) {
      where.n_address_id = params.addressId
    }
    if (params.baseSum) {
      where.n_base_sum = params.baseSum
    }
    if (params.baseSumWoTax) {
      where.n_base_sum_wo_tax = params.baseSumWoTax
    }
    if (params.sum) {
      where.n_sum = params.sum
    }
    if (params.sumTax) {
      where.n_sum_tax = params.sumTax
    }
    if (params.sumWoTax) {
      where.n_sum_wo_tax = params.sumWoTax
    }
    if (params.priceLineId) {
      where.n_price_line_id = params.priceLineId
    }
    if (params.priceOrderId) {
      where.n_price_order_doc_id = params.priceOrderId
    }
    if (params.discountLineId) {
      where.n_discount_cert_line_id = params.discountLineId
    }
    if (params.discountDocId) {
      where.n_discount_doc_id = params.discountDocId
    }
    if (params.provisionRuleId) {
      where.n_provision_rule_id = params.provisionRuleId
    }
    if (params.beginDate) {
      where.d_begin = params.beginDate
    }
    if (params.endDate) {
      where.d_end = params.endDate
    }
    if (params.operationDate) {
      where.d_oper = params.operationDate
    }
    return hid.getTableData(getChargeLogLinesTable(), where: where, order: params.order, limit: params.limit)
  }

  /**
   * Get charge log lines by doc id
   * @param docId {@link java.math.BigInteger BigInteger}
   * @param limit {@link Integer}. Optional. Default: 0 (unlimited)
   * @return Charge log line table rows
   */
  List<Map> getChargeLogLines(def docId, Integer limit = 0) {
    LinkedHashMap where = [
      n_doc_id       : docId,
      n_move_type_id : ['not in': [getChargeCanceledTypeId()]]
    ]
    return hid.getTableData(getChargeLogLinesTable(), where: where, limit: limit)
  }

  /**
   * Search for one charge log line by different fields value
   * @param docId           {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lineId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lineNumber      {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parLineId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subcriptionId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param baseGoodId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param objectId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param moveTypeId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. Default: not cancelled
   * @param moveType        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param unitId          {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param unit            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param unitBaseId      {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param unitBase        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param taxRateId       {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param taxRate         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param currencyId      {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param currency        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param quant           {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param quantBase       {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param price           {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param priceWoTax      {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param addressId       {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param baseSum         {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param baseSumWoTax    {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param sum             {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param sumTax          {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param sumWoTax        {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param discountLineId  {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param discountDocId   {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param priceLineId     {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param priceOrderId    {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param provisionRuleId {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param operationDate   {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param beginDate       {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param endDate         {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param order           {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: N_LINE_NO DESC
   * @return Charge log line table row
   */
  Map getChargeLogLineBy(Map input) {
    return getChargeLogLinesBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Get charge log line by id
   * @param lineId {@link java.math.BigInteger BigInteger}
   * @return Charge log line table row
   */
  Map getChargeLogLine(def line) {
    LinkedHashMap where = [
      n_line_id: line
    ]
    return hid.getTableFirst(getChargeLogLinesTable(), where: where)
  }

  /**
   * Add tag to charge log
   * @see Document#addDocumentTag(Map)
   */
  Map addChargeLogTag(Map input) {
    return addDocumentTag(input)
  }

  /**
   * Add tag to charge log
   * @see Document#addDocumentTag(def,CharSequence)
   */
  Map addChargeLogTag(def docId, CharSequence tag) {
    return addChargeLogTag(docId: docId, tag: tag)
  }

  /**
   * Add tag to charge log
   * @see Document#addDocumentTag(Map,def)
   */
  Map addChargeLogTag(Map input = [:], def docId) {
    return addChargeLogTag(input + [docId: docId])
  }

  /**
   * Delete tag from charge log
   * @see Document#deleteDocumentTag(def)
   */
  Boolean deleteChargeLogTag(def docTagId) {
    return deleteDocumentTag(docTagId)
  }

  /**
   * Delete tag from charge log
   * @see Document#deleteDocumentTag(Map)
   */
  Boolean deleteChargeLogTag(Map input) {
    return deleteDocumentTag(input)
  }

  /**
   * Delete tag from charge log
   * @see Document#deleteDocumentTag(def,CharSequence)
   */
  Boolean deleteChargeLogTag(def docId, CharSequence tag) {
    return deleteChargeLogTag(docId: docId, tag: tag)
  }
}
