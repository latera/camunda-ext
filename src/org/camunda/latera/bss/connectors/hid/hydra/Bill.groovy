package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.utils.DateTimeUtil
import org.camunda.latera.bss.utils.Oracle
import java.time.LocalDateTime

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

  def getBillsTable() {
    return BILLS_TABLE
  }

  def getBillLinesTable() {
    return BILL_LINES_TABLE
  }

  def getBillType() {
    return BILL_TYPE
  }

  def getBillTypeId() {
    return getRefIdByCode(BILL_TYPE)
  }

  def getDefaultBillWorkflow() {
    return DEFAULT_BILL_WORKFLOW
  }

  def getDefaultBillWorkflowId() {
    return DEFAULT_BILL_WORKFLOW_ID
  }

  def getDefaultAdvanceBillWorkflow() {
    return DEFAULT_ADVANCE_BILL_WORKFLOW
  }

  def getDefaultAdvanceBillWorkflowId() {
    return DEFAULT_ADVANCE_BILL_WORKFLOW_ID
  }

  def getDefaultPrepaidBillWorkflow() {
    return DEFAULT_PREPAID_BILL_WORKFLOW
  }

  def getDefaultPrepaidBillWorkflowId() {
    return DEFAULT_PREPAYMENT_BILL_WORKFLOW_ID
  }

  LinkedHashMap getBill(def docId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return hid.getTableFirst(getBillsTable(), where: where)
  }

  List getBillsBy(LinkedHashMap input) {
    input.docTypeId  = getBillTypeId()
    return getDocumentsBy([providerId: null] + input)
  }

  LinkedHashMap getBillBy(LinkedHashMap input) {
    input.docTypeId = getBillTypeId()
    return getDocumentBy([providerId: null] + input)
  }

  Boolean isBill(String entityType) {
    return entityType == getBillType()
  }

  Boolean isBill(def entityIdOrEntityTypeId) {
    return entityIdOrEntityTypeId == getBillTypeId() || getDocument(entityIdOrEntityTypeId).n_doc_type_id == getBillTypeId()
  }

  Boolean actualizeBill(def docId) {
    return actualizeDocument(docId)
  }

  Boolean executeBill(def docId) {
    return executeDocument(docId)
  }

  Boolean cancelBill(def docId) {
    return cancelDocument(docId)
  }

  List getBillLinesBy(LinkedHashMap input) {
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
      endDate           : null
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
      String oracleDate = Oracle.encodeDateStr(params.operationDate)
      where[oracleDate] = [BETWEEN: "D_BEGIN AND NVL(D_END, ${oracleDate})"]
    }
    def order = [n_line_no: 'asc']
    return hid.getTableData(getBillLinesTable(), where: where, order: order)
  }

  List getBillLines(def docId) {
    LinkedHashMap where = [
      n_doc_id       : docId,
      n_move_type_id : ['not in': [getChargeCanceledTypeId()]]
    ]
    return hid.getTableData(getBillLinesTable(), where: where)
  }

  LinkedHashMap getBillLineBy(LinkedHashMap input) {
    return getBillLinesBy(input)?.getAt(0)
  }

  LinkedHashMap getBillLine(def lineId) {
    LinkedHashMap where = [
      n_line_id: lineId
    ]
    return hid.getTableFirst(getBillLinesTable(), where: where)
  }
}