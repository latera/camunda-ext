package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.DateTimeUtil.local
import static org.camunda.latera.bss.utils.Oracle.encodeDateStr
import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import java.time.temporal.Temporal

trait ChargeLog {
  private static String  CHARGE_LOGS_TABLE              = 'SD_V_CHARGE_LOGS_T'
  private static String  GOOD_MOVES_TABLE               = 'SD_V_GOOD_MOVES_T'
  private static String  CHARGE_LOG_LINES_TABLE         = 'SD_V_CHARGE_LOGS_C'
  private static String  CHARGE_LOG_TYPE                = 'DOC_TYPE_ChargeLog'
  private static String  CHARGE_CHARGED_TYPE            = 'GM_TYPE_Charged'
  private static String  CHARGE_RESERVED_TYPE           = 'GM_TYPE_Reserve'
  private static String  CHARGE_CANCELED_TYPE           = 'GM_TYPE_Cancelled'
  private static String  DEFAULT_CHARGE_LOG_WORKFLOW    = 'WFLOW_ChargeLog'
  private static Integer DEFAULT_CHARGE_LOG_WORKFLOW_ID = 30021

  String getChargeLogsTable() {
    return CHARGE_LOGS_TABLE
  }

  String getGoodMovesTable() {
    return GOOD_MOVES_TABLE
  }

  String getChargeLogLinesTable() {
    return CHARGE_LOG_LINES_TABLE
  }

  String getChargeLogType() {
    return CHARGE_LOG_TYPE
  }

  Number getChargeLogTypeId() {
    return getRefIdByCode(getChargeLogType())
  }

  String getChargeChargedType() {
    return CHARGE_CHARGED_TYPE
  }

  Number getChargeChargedTypeId() {
    return getRefIdByCode(getChargeChargedType())
  }

  String getChargeReservedType() {
    return CHARGE_RESERVED_TYPE
  }

  Number getChargeReservedTypeId() {
    return getRefIdByCode(getChargeReservedType())
  }

  String getChargeCanceledType() {
    return CHARGE_CANCELED_TYPE
  }

  Number getChargeCanceledTypeId() {
    return getRefIdByCode(getChargeCanceledType())
  }

  String getDefaultChargeLogWorkflow() {
    return DEFAULT_CHARGE_LOG_WORKFLOW
  }

  Number getDefaultChargeLogWorkflowId() {
    return DEFAULT_CHARGE_LOG_WORKFLOW_ID
  }

  Map getChargeLog(def docId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return hid.getTableFirst(getChargeLogsTable(), where: where)
  }

  List getChargeLogsBy(Map input) {
    input.docTypeId = getChargeLogTypeId()
    return getDocumentsBy(input)
  }

  Map getChargeLogBy(Map input) {
    input.docTypeId = getChargeLogTypeId()
    return getDocumentBy(input)
  }

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

  Number getChargeLogIdBySubscription(def subscriptionId, Temporal operationDate = local()) {
    return getChargeLogIdBySubscription(subscriptionId: subscriptionId, operationDate: operationDate)
  }

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

  Map getChargeLogBySubscription(def subscriptionId, Temporal operationDate = local()) {
    return getChargeLogBySubscription(subscriptionId: subscriptionId, operationDate: operationDate)
  }

  List getChargeLogsBySubscription(Map input) {
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

  List getChargeLogsBySubscription(def subscriptionId, def stateId = ['not in': [getDocumentStateCanceledId()]], def operationDate = null) {
    return getChargeLogsBySubscription(subscriptionId: subscriptionId, stateId: stateId, operationDate: operationDate)
  }

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

  Boolean changeChargeLogEnd(def docId, Temporal endDate = local(), def closeReasonId = null) {
    return changeChargeLogEnd(docId: docId, endDate: endDate, closeReasonId: closeReasonId)
  }

  Boolean closeChargeLog(Map input) {
    return changeChargeLogEnd(input)
  }

  Boolean closeChargeLog(def docId, Temporal endDate = local(), def closeReasonId = null) {
    return changeChargeLogEnd(docId: docId, endDate: endDate, closeReasonId: closeReasonId)
  }

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

  List getChargeLogLinesBy(Map input) {
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

  List getChargeLogLines(def docId, Integer limit = 0) {
    LinkedHashMap where = [
      n_doc_id       : docId,
      n_move_type_id : ['not in': [getChargeCanceledTypeId()]]
    ]
    return hid.getTableData(getChargeLogLinesTable(), where: where, limit: limit)
  }

  Map getChargeLogLineBy(Map input) {
    return getChargeLogLinesBy(input + [limit: 1])?.getAt(0)
  }

  Map getChargeLogLine(def line) {
    LinkedHashMap where = [
      n_line_id: line
    ]
    return hid.getTableFirst(getChargeLogLinesTable(), where: where)
  }

  Map addChargeLogTag(Map input) {
    return addDocumentTag(input)
  }

  Map addChargeLogTag(def docId, CharSequence tag) {
    return addChargeLogTag(docId: docId, tag: tag)
  }

  Map addChargeLogTag(Map input = [:], def docId) {
    return addChargeLogTag(input + [docId: docId])
  }

  Boolean deleteChargeLogTag(def docTagId) {
    return deleteDocumentTag(docTagId)
  }

  Boolean deleteChargeLogTag(Map input) {
    return deleteDocumentTag(input)
  }

  Boolean deleteChargeLogTag(def docId, CharSequence tag) {
    return deleteChargeLogTag(docId: docId, tag: tag)
  }
}
