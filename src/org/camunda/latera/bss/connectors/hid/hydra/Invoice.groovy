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

  /**
   * Get invoices table name
   */
  String getInvoicesTable() {
    return INVOICES_TABLE
  }

  /**
   * Get good moves table name
   */
  String getGoodMovesTable() {
    return GOOD_MOVES_TABLE
  }

  /**
   * Get invoice lines table name
   */
  String getInvoiceLinesTable() {
    return INVOICE_LINES_TABLE
  }

  /**
   * Get invoice document type ref code
   */
  String getInvoiceType() {
    return INVOICE_TYPE
  }

  /**
   * Get invoice document type ref id
   */
  Number getInvoiceTypeId() {
    return getRefIdByCode(getInvoiceType())
  }

  /**
   * Get charge log line Charge state type ref code
   */
  String getChargeChargedType() {
    return CHARGE_CHARGED_TYPE
  }

  /**
   * Get charge log line Charge state type ref id
   */
  Number getChargeChargedTypeId() {
    return getRefIdByCode(getChargeChargedType())
  }

  /**
   * Get charge log line Reserved state type ref code
   */
  String getChargeReservedType() {
    return CHARGE_RESERVED_TYPE
  }

  /**
   * Get charge log line Reserved state type ref id
   */
  Number getChargeReservedTypeId() {
    return getRefIdByCode(getChargeReservedType())
  }

  /**
   * Get charge log line Canceled state type ref code
   */
  String getChargeCanceledType() {
    return CHARGE_CANCELED_TYPE
  }

  /**
   * Get charge log line Canceled state type ref id
   */
  Number getChargeCanceledTypeId() {
    return getRefIdByCode(getChargeCanceledType())
  }

  /**
   * Get invoice document default workflow code
   */
  String getDefaultInvoiceWorkflow() {
    return DEFAULT_INVOICE_WORKFLOW
  }

  /**
   * Get invoice document default workflow id
   */
  Number getDefaultInvoiceWorkflowId() {
    return DEFAULT_INVOICE_WORKFLOW_ID
  }

  /**
   * Get invoice by id
   * @param docId {@link java.math.BigInteger BigInteger}
   * @return Map with invoice table row or null
   */
  Map getInvoice(def docId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return hid.getTableFirst(getInvoicesTable(), where: where)
  }

  /**
   * Search for invoices by different fields value
   * @see #getDocumentsBy(Map)
   */
  List getInvoicesBy(Map input) {
    input.docTypeId = getInvoiceTypeId()
    return getDocumentsBy(input)
  }

  /**
   * Search for one invoice by different fields value
   * @see #getDocumentBy(Map)
   */
  Map getInvoiceBy(Map input) {
    input.docTypeId = getInvoiceTypeId()
    return getDocumentBy(input)
  }

  /**
   * Get invoice id for subscription
   * @param subscriptionId {@link java.math.BigInteger BigInteger}
   * @param operationDate {@link java.time.Temporal Any date type}. Optional, default: current date time
   * @return Invoice id or null
   */
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

  /**
   * Get invoice id for subscription
   *
   * Overload with positional args
   * @see #getInvoiceIdBySubscription(Map)
   */
  Number getInvoiceIdBySubscription(def subscriptionId, Temporal operationDate = local()) {
    return getInvoiceIdBySubscription(subscriptionId: subscriptionId, operationDate: operationDate)
  }

  /**
   * Get invoice for subscription
   * @param subscriptionId {@link java.math.BigInteger BigInteger}
   * @param operationDate {@link java.time.Temporal Any date type}. Optional, default: current date time
   * @return Map with invoice table row
   */
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


  /**
   * Get invoice for subscription
   *
   * Overload with positional args
   * @see #getInvoiceBySubscription(Map)
   */
  Map getInvoiceBySubscription(def subscriptionId, Temporal operationDate = local()) {
    return getInvoiceBySubscription(subscriptionId: subscriptionId, operationDate: operationDate)
  }

  /**
   * Get invoices for subscription
   * @param subscriptionId {@link java.math.BigInteger BigInteger}
   * @param stateId        {@link java.math.BigInteger BigInteger}. Optional, default: not canceled
   * @param state          {@link CharSequence String}. Optional
   * @param operationDate  {@link java.time.Temporal Any date type}. Optional, default: current date time
   * @param limit          {@link Integer}. Optional, default: 0 (unlimited)
   * @return List[Map] with good moves table row
   */
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


  /**
   * Get invoices for subscription
   *
   * Overload with positional args
   * @see #getInvoicesBySubscription(Map)
   */
  List getInvoicesBySubscription(def subscriptionId, def stateId = ['not in': [getDocumentStateCanceledId()]], def operationDate = null) {
    return getInvoicesBySubscription(subscriptionId: subscriptionId, stateId: stateId, operationDate: operationDate)
  }

  /**
   * Check if entity type code is invoice
   * @param entityType {@link CharSequence String}. Document type ref code
   * @return True if given value is invoice, false otherwise
   */
  Boolean isInvoice(CharSequence entityType) {
    return entityType == getInvoiceType()
  }

  /**
   * Check if entity id ot entity type id is invoice
   * @param entityIdOrEntityTypeId {@link java.math.BigInteger BigInteger}. Document id or document type ref id
   * @return True if given value is invoice, false otherwise
   */
  Boolean isInvoice(def entityIdOrEntityTypeId) {
    return entityIdOrEntityTypeId == getInvoiceTypeId() || getDocument(entityIdOrEntityTypeId).n_doc_type_id == getInvoiceTypeId()
  }

  /**
   * Set invoice end date
   * @param docId         {@link java.math.BigInteger BigInteger}
   * @param endDate       {@link java.time.Temporal Any date type}. Optional, default: current date time
   * @param closeReasonId {@link java.math.BigInteger BigInteger}. Optional
   * @param closeReason   {@link CharSequence String}. Optional
   * @return True if invoice end date was changed successfully
   */
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

  /**
   * Set invoice end date
   *
   * Overload with positional args
   * @see #changeInvoiceEnd(Map)
   */
  Boolean changeInvoiceEnd(def docId, Temporal endDate = local(), def closeReasonId = null) {
    return changeInvoiceEnd(docId: docId, endDate: endDate, closeReasonId: closeReasonId)
  }

  /**
   * Close invoice
   * @see #changeInvoiceEnd(Map)
   */
  Boolean closeInvoice(Map input) {
    return changeInvoiceEnd(input)
  }

  /**
   * Close invoice
   *
   * Overload with positional args
   * @see #closeInvoice(Map)
   */
  Boolean closeInvoice(def docId, Temporal endDate = local(), def closeReasonId = null) {
    return changeInvoiceEnd(docId: docId, endDate: endDate, closeReasonId: closeReasonId)
  }

  /**
   * Change invoice state to Canceled
   * @param docId {@link java.math.BigInteger BigInteger}
   * @return True if invoice state was changed successfully
   */
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

  /**
   * Search for invoice lines by different fields value
   * @param docId           {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lineId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lineNumber      {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parLineId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subcriptionId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param baseGoodId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param objectId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param moveTypeId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: not cancelled
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
   * @param limit           {@link Integer}. Optional, default: 0 (unlimited)
   * @param order           {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: N_LINE_NO DESC
   * @return List[Map] of invoice line table rows
   */
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

  /**
   * Get invoice lines by doc id
   * @param docId {@link java.math.BigInteger BigInteger}
   * @param limit {@link Integer}. Optional, default: 0 (unlimited)
   * @return List[Map] of invoice line table rows
   */
  List getInvoiceLines(def docId, Integer limit = 0) {
    LinkedHashMap where = [
      n_doc_id       : docId,
      n_move_type_id : ['not in': [getChargeCanceledTypeId()]]
    ]
    return hid.getTableData(getInvoiceLinesTable(), where: where, limit: limit)
  }

  /**
   * Search for one invoice line by different fields value
   * @param docId           {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lineId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lineNumber      {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parLineId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subcriptionId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param baseGoodId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param objectId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param moveTypeId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: not cancelled
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
   * @param order           {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: N_LINE_NO DESC
   * @return Map with invoice line table row
   */
  Map getInvoiceLineBy(Map input) {
    return getInvoiceLinesBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Get invoice line by id
   * @param lineId {@link java.math.BigInteger BigInteger}
   * @return Map with invoice line table row
   */
  Map getInvoiceLine(def line) {
    LinkedHashMap where = [
      n_line_id: line
    ]
    return hid.getTableFirst(getInvoiceLinesTable(), where: where)
  }
}