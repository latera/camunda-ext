package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Oracle.encodeDateStr
import static org.camunda.latera.bss.utils.Constants.DOC_TYPE_Invoice
import static org.camunda.latera.bss.utils.Constants.WFLOW_Invoice
import static org.camunda.latera.bss.utils.Constants.WFLOW_ProformaInvoice
import static org.camunda.latera.bss.utils.Constants.WFLOW_PrepaymentInvoice

/**
 * Invoice specific methods
 */
trait Invoice {
  private static String INVOICES_TABLE      = 'SD_V_INVOICES_T'
  private static String INVOICE_LINES_TABLE = 'SD_V_INVOICES_C'

  /**
   * Get invoices table name
   */
  String getInvoicesTable() {
    return INVOICES_TABLE
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
    return getRefCode(getInvoiceTypeId())
  }

  /**
   * Get invoice document type ref id
   */
  Number getInvoiceTypeId() {
    return DOC_TYPE_Invoice
  }

  /**
   * Get invoice document default workflow code
   */
  String getDefaultInvoiceWorkflow() {
    return getRefCode(getDefaultInvoiceWorkflowId())
  }

  /**
   * Get invoice document default workflow id
   */
  Number getDefaultInvoiceWorkflowId() {
    return WFLOW_Invoice
  }

  /**
   * Get advance invoice document default workflow code
   */
  String getDefaultProformaInvoiceWorkflow() {
    return getRefCode(getDefaultProformaInvoiceWorkflowId())
  }

  /**
   * Get advance invoice document default workflow id
   */
  Number getDefaultProformaInvoiceWorkflowId() {
    return WFLOW_ProformaInvoice
  }

  /**
   * Get prepaid invoice document default workflow code
   */
  String getDefaultPrepaymentInvoiceWorkflow() {
    return getRefCode(getDefaultPrepaymentInvoiceWorkflowId())
  }

  /**
   * Get prepaid invoice document default workflow id
   */
  Number getDefaultPrepaymentInvoiceWorkflowId() {
    return WFLOW_PrepaymentInvoice
  }

  /**
   * Get invoice by id
   * @param docId {@link java.math.BigInteger BigInteger}
   * @return Invoice table row
   */
  Map getInvoice(def docId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return hid.getTableFirst(getInvoicesTable(), where: where)
  }

  /**
   * Search for invoices by different fields value
   * @see Document#getDocumentsBy(java.util.Map)
   */
  List getInvoicesBy(Map input) {
    input.docId     = input.docId ?: input.invoiceId
    input.docTypeId = getInvoiceTypeId()
    return getDocumentsBy([providerId: null] + input)
  }

  /**
   * Search for one invoice by different fields value
   * @see Document#getDocumentBy(java.util.Map)
   */
  Map getInvoiceBy(Map input) {
    input.docId     = input.docId ?: input.invoiceId
    input.docTypeId = getInvoiceTypeId()
    return getDocumentBy([providerId: null] + input)
  }

  /**
   * Check if entity or entity type is invoice
   * @param entityOrEntityType {@link java.math.BigInteger BigInteger} or {@link CharSequence String}. Document id, document type ref id or document type ref code
   * @return True if given value is invoice, false otherwise
   */
  Boolean isInvoice(def entityOrEntityType) {
    if (entityOrEntityType == null) {
      return false
    }

    Number entityIdOrEntityTypeId = toIntSafe(entityOrEntityType)
    if (entityIdOrEntityTypeId != null) {
      return entityIdOrEntityTypeId == getInvoiceTypeId() || getDocument(entityIdOrEntityTypeId).n_doc_type_id == getInvoiceTypeId()
    } else {
      return entityOrEntityType == getInvoiceType()
    }
  }

  /**
   * Change invoice state to Actual
   * @param docId {@link java.math.BigInteger BigInteger}. Document id
   * @return True if state change was successfull, false otherwise
   */
  Boolean actualizeInvoice(def docId) {
    return actualizeDocument(docId)
  }

  /**
   * Change invoice state to Executed
   * @param docId {@link java.math.BigInteger BigInteger}. Document id
   * @return True if state change was successfull, false otherwise
   */
  Boolean executeInvoice(def docId) {
    return executeDocument(docId)
  }

  /**
   * Change invoice state to Canceled
   * @param docId {@link java.math.BigInteger BigInteger}. Document id
   * @return True if state change was successfull, false otherwise
   */
  Boolean cancelInvoice(def docId) {
    return cancelDocument(docId)
  }

  /**
   * Search for invoice lines by different fields value
   * @param docId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lineId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lineNumber     {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parLineId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param objectId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param moveTypeId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. Default: not cancelled
   * @param moveType       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param unitId         {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param unit           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param unitBaseId     {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param unitBase       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param taxRateId      {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param taxRate        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param currencyId     {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param currency       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param quant          {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param quantBase      {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param price          {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param priceWoTax     {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param addressId      {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param sum            {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param sumTax         {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param sumWoTax       {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param discountLineId {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param discountDocId  {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param operationDate  {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param beginDate      {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param endDate        {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit          {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order          {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: N_LINE_NO DESC
   * @return Invoice line table rows
   */
  List<Map> getInvoiceLinesBy(Map input) {
    LinkedHashMap params = mergeParams([
      docId             : null,
      lineId            : null,
      lineNumber        : null,
      parLineId         : null,
      goodId            : null,
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
      sum               : null,
      sumTax            : null,
      sumWoTax          : null,
      discountLineId    : null,
      discountDocId     : null,
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
    if (params.goodId) {
      where.n_good_id = params.goodId
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
    if (params.sum) {
      where.n_sum = params.sum
    }
    if (params.sumTax) {
      where.n_sum_tax = params.sumTax
    }
    if (params.sumWoTax) {
      where.n_sum_wo_tax = params.sumWoTax
    }
    if (params.discountLineId) {
      where.n_discount_cert_line_id = params.discountLineId
    }
    if (params.discountDocId) {
      where.n_discount_doc_id = params.discountDocId
    }
    if (params.beginDate) {
      where.d_begin = params.beginDate
    }
    if (params.endDate) {
      where.d_end = params.endDate
    }
    if (params.operationDate) {
      String oracleDate = encodeDateStr(params.operationDate)
      where[oracleDate] = [between: "d_begin and nvl(d_end, ${oracleDate})"]
    }
    return hid.getTableData(getInvoiceLinesTable(), where: where, order: params.order, limit: params.limit)
  }

  /**
   * Get invoice lines by doc id
   * @param docId {@link java.math.BigInteger BigInteger}
   * @param limit {@link Integer}. Optional. Default: 0 (unlimited)
   * @return Invoice line table rows
   */
  List<Map> getInvoiceLines(def docId, Integer limit = 0) {
    LinkedHashMap where = [
      n_doc_id       : docId,
      n_move_type_id : ['not in': [getChargeCanceledTypeId()]]
    ]
    return hid.getTableData(getInvoiceLinesTable(), where: where, limit: limit)
  }

  /**
   * Search for one invoice line by different fields value
   * @param docId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lineId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lineNumber     {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parLineId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param objectId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param moveTypeId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. Default: not cancelled
   * @param moveType       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param unitId         {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param unit           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param unitBaseId     {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param unitBase       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param taxRateId      {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param taxRate        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param currencyId     {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param currency       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param quant          {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param quantBase      {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param price          {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param priceWoTax     {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param addressId      {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param sum            {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param sumTax         {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param sumWoTax       {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param discountLineId {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param discountDocId  {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param operationDate  {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param beginDate      {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param endDate        {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param order          {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: N_LINE_NO DESC
   * @return Invoice line table row
   */
  Map getInvoiceLineBy(Map input) {
    return getInvoiceLinesBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Get invoice line by id
   * @param lineId {@link java.math.BigInteger BigInteger}
   * @return Invoice line table row
   */
  Map getInvoiceLine(def lineId) {
    LinkedHashMap where = [
      n_line_id: lineId
    ]
    return hid.getTableFirst(getInvoiceLinesTable(), where: where)
  }

  /**
   * Add tag to invoice
   * @see Document#addDocumentTag(java.util.Map)
   */
  Map addInvoiceTag(Map input) {
    return addDocumentTag(input)
  }

  /**
   * Add tag to invoice
   * @see Document#addDocumentTag(def, java.lang.CharSequence)
   */
  Map addInvoiceTag(def docId, CharSequence tag) {
    return addInvoiceTag(docId: docId, tag: tag)
  }

  /**
   * Add tag to invoice
   * @see Document#addDocumentTag(java.util.Map, def)
   */
  Map addInvoiceTag(Map input = [:], def docId) {
    return addInvoiceTag(input + [docId: docId])
  }

  /**
   * Delete tag from invoice
   * @see Document#deleteDocumentTag(def)
   */
  Boolean deleteInvoiceTag(def docTagId) {
    return deleteDocumentTag(docTagId)
  }

  /**
   * Delete tag from invoice
   * @see Document#deleteDocumentTag(java.util.Map)
   */
  Boolean deleteInvoiceTag(Map input) {
    return deleteDocumentTag(input)
  }

  /**
   * Delete tag from invoice
   * @see Document#deleteDocumentTag(def, java.lang.CharSequence)
   */
  Boolean deleteInvoiceTag(def docId, CharSequence tag) {
    return deleteInvoiceTag(docId: docId, tag: tag)
  }
}
