package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.DateTimeUtil.local
import static org.camunda.latera.bss.utils.Oracle.encodeDateStr
import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import java.time.temporal.Temporal

trait Invoice {
  private static String  INVOICES_TABLE              = 'SD_V_INVOICES_T'
  private static String  GOOD_MOVES_TABLE            = 'SD_V_GOOD_MOVES_T'
  private static String  INVOICE_LINES_TABLE         = 'SD_V_INVOICES_C'
  private static String  INVOICE_TYPE                = 'DOC_TYPE_Invoice'
  private static String  CHARGE_CHARGED_TYPE         = 'GM_TYPE_Charged'
  private static String  CHARGE_RESERVED_TYPE        = 'GM_TYPE_Reserve'
  private static String  CHARGE_CANCELED_TYPE        = 'GM_TYPE_Cancelled'
  private static String  DEFAULT_INVOICE_WORKFLOW    = 'WFLOW_Invoice'
  private static Integer DEFAULT_INVOICE_WORKFLOW_ID = 30021

  String getInvoicesTable() {
    return INVOICES_TABLE
  }

  String getGoodMovesTable() {
    return GOOD_MOVES_TABLE
  }

  String getInvoiceLinesTable() {
    return INVOICE_LINES_TABLE
  }

  String getInvoiceType() {
    return INVOICE_TYPE
  }

  Number getInvoiceTypeId() {
    return getRefIdByCode(getInvoiceType())
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

  String getDefaultInvoiceWorkflow() {
    return DEFAULT_INVOICE_WORKFLOW
  }

  Number getDefaultInvoiceWorkflowId() {
    return DEFAULT_INVOICE_WORKFLOW_ID
  }

  Map getInvoice(def docId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return hid.getTableFirst(getInvoicesTable(), where: where)
  }

  List getInvoicesBy(Map input) {
    input.docTypeId = getInvoiceTypeId()
    return getDocumentsBy(input)
  }

  Map getInvoiceBy(Map input) {
    input.docTypeId = getInvoiceTypeId()
    return getDocumentBy(input)
  }

  Map getInvoiceByCode(CharSequence code) {
    return getInvoiceBy(code: code)
  }

  Map getInvoiceByName(CharSequence name) {
    return getInvoiceBy(name: name)
  }

  Number getInvoiceIdByCode(CharSequence code) {
    return toIntSafe(getInvoiceByCode(code)?.n_doc_id)
  }

  Number getInvoiceIdByName(CharSequence name) {
    return toIntSafe(getInvoiceByName(name)?.n_doc_id)
  }

  Number getInvoiceIdBySubscription(Map input) {
    LinkedHashMap params = mergeParams([
      subscriptionId : null,
      operationDate  : local()
    ], input)
    try {
      def docId = hid.queryFirst("""
      SELECT
        SD_INVOICES_PKG_S.GET_INVOICE_ID_BY_SUBJ_GOOD(
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

  Number getInvoiceIdBySubscription(def subscriptionId, Temporal operationDate = local()) {
    return getInvoiceIdBySubscription(subscriptionId: subscriptionId, operationDate: operationDate)
  }

  Map getInvoiceBySubscription(Map input) {
    def docId = getInvoiceIdBySubscription(input)
    if (docId == null) {
      return null
    }
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return hid.getTableFirst(getInvoicesTable(), where: where)
  }

  Map getInvoiceBySubscription(def subscriptionId, Temporal operationDate = local()) {
    return getInvoiceBySubscription(subscriptionId: subscriptionId, operationDate: operationDate)
  }

  List getInvoicesBySubscription(Map input) {
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

  List getInvoicesBySubscription(def subscriptionId, def stateId = ['not in': [getDocumentStateCanceledId()]], def operationDate = null) {
    return getInvoicesBySubscription(subscriptionId: subscriptionId, stateId: stateId, operationDate: operationDate)
  }

  Boolean isInvoice(CharSequence entityOrEntityType) {
    return entityOrEntityType == getInvoiceType() || getInvoiceByCode(entityOrEntityType) != null || getInvoiceByName(entityOrEntityType) != null
  }

  Boolean isInvoice(def entityIdOrEntityTypeId) {
    return entityIdOrEntityTypeId == getInvoiceTypeId() || getDocument(entityIdOrEntityTypeId).n_doc_type_id == getInvoiceTypeId()
  }

  Boolean changeInvoiceEnd(Map input) {
    LinkedHashMap params = mergeParams([
      docId         : null,
      endDate       : null,
      closeReasonId : null
    ], input)
    try {
      logger.info("Changing invoice id ${params.docId} end date to ${params.endDate} with reason ${params.closeReasonId}")
      LinkedHashMap invoice = hid.execute('SD_INVOICES_PKG.CHANGE_INVOICE_PERIOD', [
        num_N_DOC_ID          : params.docId,
        dt_D_OPER             : params.endDate,
        num_N_CLOSE_REASON_ID : params.closeReasonId
      ])
      logger.info("   Invoice ${invoice.num_N_DOC_ID} end date was changed successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while changing invoice end date!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean changeInvoiceEnd(def docId, Temporal endDate = local(), def closeReasonId = null) {
    return changeInvoiceEnd(docId: docId, endDate: endDate, closeReasonId: closeReasonId)
  }

  Boolean closeInvoice(Map input) {
    return changeInvoiceEnd(input)
  }

  Boolean closeInvoice(def docId, Temporal endDate = local(), def closeReasonId = null) {
    return changeInvoiceEnd(docId: docId, endDate: endDate, closeReasonId: closeReasonId)
  }

  Boolean cancelInvoice(def docId) {
    try {
      logger.info("Cancelling invoice id ${docId}")
      hid.execute('SD_INVOICES_PKG.CANCEL_CHARGE_LOG', [
        num_N_CHARGE_LOG_ID : docId
      ])
      logger.info("   Invoice was cancelled successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while changing invoice end date!")
      logger.error_oracle(e)
      return false
    }
  }

  List getInvoiceLinesBy(Map input) {
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
    return hid.getTableData(getInvoiceLinesTable(), where: where, order: params.order, limit: params.limit)
  }

  List getInvoiceLines(def docId, Integer limit = 0) {
    LinkedHashMap where = [
      n_doc_id       : docId,
      n_move_type_id : ['not in': [getChargeCanceledTypeId()]]
    ]
    return hid.getTableData(getInvoiceLinesTable(), where: where, limit: limit)
  }

  Map getInvoiceLineBy(Map input) {
    return getInvoiceLinesBy(input + [limit: 1])?.getAt(0)
  }

  Map getInvoiceLine(def line) {
    LinkedHashMap where = [
      n_line_id: line
    ]
    return hid.getTableFirst(getInvoiceLinesTable(), where: where)
  }

  Map addInvoiceTag(Map input) {
    return addDocumentTag(input)
  }

  Map addInvoiceTag(def docId, CharSequence tag) {
    return addInvoiceTag(docId: docId, tag: tag)
  }

  Map addInvoiceTag(Map input = [:], def docId) {
    return addInvoiceTag(input + [docId: docId])
  }

  Boolean deleteInvoiceTag(def docTagId) {
    return deleteDocumentTag(docTagId)
  }

  Boolean deleteInvoiceTag(Map input) {
    return deleteDocumentTag(input)
  }

  Boolean deleteInvoiceTag(def docId, CharSequence tag) {
    return deleteInvoiceTag(docId: docId, tag: tag)
  }
}