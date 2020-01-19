package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Oracle.encodeDateStr

trait Bill {
  private static String  BILLS_TABLE                         = 'SD_V_BILLS_T'
  private static String  BILL_LINES_TABLE                    = 'SD_V_BILLS_C'
  private static String  BILL_TYPE                           = 'DOC_TYPE_Bill'
  private static String  DEFAULT_BILL_WORKFLOW               = 'WFLOW_Bill'
  private static Integer DEFAULT_BILL_WORKFLOW_ID            = 60021
  private static String  DEFAULT_ADVANCE_BILL_WORKFLOW       = 'WFLOW_AdvanceBill'
  private static Integer DEFAULT_ADVANCE_BILL_WORKFLOW_ID    = 60022
  private static String  DEFAULT_PREPAYMENT_BILL_WORKFLOW    = 'WFLOW_PrepaymentBill'
  private static Integer DEFAULT_PREPAYMENT_BILL_WORKFLOW_ID = 60023

  /**
   * Get bills table name
   */
  String getBillsTable() {
    return BILLS_TABLE
  }

  /**
   * Get bill lines table name
   */
  String getBillLinesTable() {
    return BILL_LINES_TABLE
  }

  /**
   * Get bill document type ref code
   */
  String getBillType() {
    return BILL_TYPE
  }

  /**
   * Get bill document type ref id
   */
  Number getBillTypeId() {
    return getRefIdByCode(BILL_TYPE)
  }

  /**
   * Get bill document default workflow code
   */
  String getDefaultBillWorkflow() {
    return DEFAULT_BILL_WORKFLOW
  }

  /**
   * Get bill document default workflow id
   */
  Number getDefaultBillWorkflowId() {
    return DEFAULT_BILL_WORKFLOW_ID
  }

  /**
   * Get advance bill document default workflow code
   */
  String getDefaultAdvanceBillWorkflow() {
    return DEFAULT_ADVANCE_BILL_WORKFLOW
  }

  /**
   * Get advance bill document default workflow id
   */
  Number getDefaultAdvanceBillWorkflowId() {
    return DEFAULT_ADVANCE_BILL_WORKFLOW_ID
  }

  /**
   * Get prepaid bill document default workflow code
   */
  String getDefaultPrepaidBillWorkflow() {
    return DEFAULT_PREPAYMENT_BILL_WORKFLOW
  }

  /**
   * Get prepaid bill document default workflow id
   */
  Number getDefaultPrepaidBillWorkflowId() {
    return DEFAULT_PREPAYMENT_BILL_WORKFLOW_ID
  }

  /**
   * Get bill by id
   * @param docId {@link java.math.BigInteger BigInteger}
   * @return Map with bill table row or null
   */
  Map getBill(def docId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return hid.getTableFirst(getBillsTable(), where: where)
  }

  /**
   * Search for bills by different fields value
   * @see #getDocumentsBy(Map)
   */
  List getBillsBy(Map input) {
    input.docId     = input.docId ?: input.billId
    input.docTypeId = getBillTypeId()
    return getDocumentsBy([providerId: null] + input)
  }

  /**
   * Search for one bill by different fields value
   * @see #getDocumentBy(Map)
   */
  Map getBillBy(Map input) {
    input.docId     = input.docId ?: input.billId
    input.docTypeId = getBillTypeId()
    return getDocumentBy([providerId: null] + input)
  }

  /**
   * Check if entity type code is bill
   * @param entityType {@link CharSequence String}. Document type ref code
   * @return True if given value is bill, false otherwise
   */
  Boolean isBill(CharSequence entityType) {
    return entityType == getBillType()
  }

  /**
   * Check if entity id ot entity type id is bill
   * @param entityIdOrEntityTypeId {@link java.math.BigInteger BigInteger}. Document id or document type ref id
   * @return True if given value is bill, false otherwise
   */
  Boolean isBill(def entityIdOrEntityTypeId) {
    return entityIdOrEntityTypeId == getBillTypeId() || getDocument(entityIdOrEntityTypeId).n_doc_type_id == getBillTypeId()
  }

  /**
   * Change bill state to Actual
   * @param docId {@link java.math.BigInteger BigInteger}. Document id
   * @return True if state change was successfull, false otherwise
   */
  Boolean actualizeBill(def docId) {
    return actualizeDocument(docId)
  }

  /**
   * Change bill state to Executed
   * @param docId {@link java.math.BigInteger BigInteger}. Document id
   * @return True if state change was successfull, false otherwise
   */
  Boolean executeBill(def docId) {
    return executeDocument(docId)
  }

  /**
   * Change bill state to Canceled
   * @param docId {@link java.math.BigInteger BigInteger}. Document id
   * @return True if state change was successfull, false otherwise
   */
  Boolean cancelBill(def docId) {
    return cancelDocument(docId)
  }

  /**
   * Search for bill lines by different fields value
   * @param docId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lineId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lineNumber     {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parLineId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param objectId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param moveTypeId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: not cancelled
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
   * @param operationDate  {@link java.time.Temporal Any date type}. Optional, {@link LinkedHashMap Map} with WHERE clause or SELECT query
   * @param beginDate      {@link java.time.Temporal Any date type}. Optional, {@link LinkedHashMap Map} with WHERE clause or SELECT query
   * @param endDate        {@link java.time.Temporal Any date type}. Optional, {@link LinkedHashMap Map} with WHERE clause or SELECT query
   * @param limit          {@link Integer}. Optional, default: 0 (unlimited)
   * @param order          {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: N_LINE_NO DESC
   * @return List[Map] of bill line table rows
   */
  List getBillLinesBy(Map input) {
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
    return hid.getTableData(getBillLinesTable(), where: where, order: params.order, limit: params.limit)
  }

  /**
   * Get bill lines by doc id
   * @param docId {@link java.math.BigInteger BigInteger}
   * @param limit {@link Integer}. Optional, default: 0 (unlimited)
   * @return List[Map] of bill line table rows
   */
  List getBillLines(def docId, Integer limit = 0) {
    LinkedHashMap where = [
      n_doc_id       : docId,
      n_move_type_id : ['not in': [getChargeCanceledTypeId()]]
    ]
    return hid.getTableData(getBillLinesTable(), where: where, limit: limit)
  }

  /**
   * Search for one bill line by different fields value
   * @param docId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lineId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lineNumber     {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parLineId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param objectId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param moveTypeId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: not cancelled
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
   * @param operationDate  {@link java.time.Temporal Any date type}. Optional, {@link LinkedHashMap Map} with WHERE clause or SELECT query
   * @param beginDate      {@link java.time.Temporal Any date type}. Optional, {@link LinkedHashMap Map} with WHERE clause or SELECT query
   * @param endDate        {@link java.time.Temporal Any date type}. Optional, {@link LinkedHashMap Map} with WHERE clause or SELECT query
   * @param order          {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: N_LINE_NO DESC
   * @return Map with bill line table row
   */
  Map getBillLineBy(Map input) {
    return getBillLinesBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Get bill line by id
   * @param lineId {@link java.math.BigInteger BigInteger}
   * @return Map with bill line table row
   */
  Map getBillLine(def lineId) {
    LinkedHashMap where = [
      n_line_id: lineId
    ]
    return hid.getTableFirst(getBillLinesTable(), where: where)
  }
}