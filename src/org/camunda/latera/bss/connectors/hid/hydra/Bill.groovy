package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Oracle.encodeDateStr
import static org.camunda.latera.bss.utils.Constants.DOC_TYPE_Bill
import static org.camunda.latera.bss.utils.Constants.WFLOW_Bill
import static org.camunda.latera.bss.utils.Constants.WFLOW_AdvanceBill
import static org.camunda.latera.bss.utils.Constants.WFLOW_PrepaymentBill

trait Bill {
  private static String  BILLS_TABLE      = 'SD_V_BILLS_T'
  private static String  BILL_LINES_TABLE = 'SD_V_BILLS_C'

  String getBillsTable() {
    return BILLS_TABLE
  }

  String getBillLinesTable() {
    return BILL_LINES_TABLE
  }

  String getBillType() {
    return getRefCode(getBillTypeId())
  }

  Number getBillTypeId() {
    return DOC_TYPE_Bill
  }

  String getDefaultBillWorkflow() {
    return getRefCode(getDefaultBillWorkflowId())
  }

  Number getDefaultBillWorkflowId() {
    return WFLOW_Bill
  }

  String getDefaultAdvanceBillWorkflow() {
    return getRefCode(getDefaultAdvanceBillWorkflowId())
  }

  Number getDefaultAdvanceBillWorkflowId() {
    return WFLOW_AdvanceBill
  }

  String getDefaultPrepaidBillWorkflow() {
    return getRefCode(getDefaultAdvanceBillWorkflowId())
  }

  Number getDefaultPrepaidBillWorkflowId() {
    return WFLOW_PrepaymentBill
  }

  Map getBill(def docId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return hid.getTableFirst(getBillsTable(), where: where)
  }

  List getBillsBy(Map input) {
    input.docTypeId  = getBillTypeId()
    return getDocumentsBy([providerId: null] + input)
  }

  Map getBillBy(Map input) {
    input.docTypeId = getBillTypeId()
    return getDocumentBy([providerId: null] + input)
  }

  Boolean isBill(def entityOrEntityType) {
    if (entityOrEntityType == null) {
      return false
    }

    Number entityIdOrEntityTypeId = toIntSafe(entityOrEntityType)
    if (entityIdOrEntityTypeId != null) {
      return entityIdOrEntityTypeId == getBillTypeId() || getDocument(entityIdOrEntityTypeId).n_doc_type_id == getBillTypeId()
    } else {
      return entityOrEntityType == getBillType()
    }
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

  List getBillLines(def docId, Integer limit = 0) {
    LinkedHashMap where = [
      n_doc_id       : docId,
      n_move_type_id : ['not in': [getChargeCanceledTypeId()]]
    ]
    return hid.getTableData(getBillLinesTable(), where: where, limit: limit)
  }

  Map getBillLineBy(Map input) {
    return getBillLinesBy(input + [limit: 1])?.getAt(0)
  }

  Map getBillLine(def lineId) {
    LinkedHashMap where = [
      n_line_id: lineId
    ]
    return hid.getTableFirst(getBillLinesTable(), where: where)
  }

  Map addBillTag(Map input) {
    return addDocumentTag(input)
  }

  Map addBillTag(def docId, CharSequence tag) {
    return addBillTag(docId: docId, tag: tag)
  }

  Map addBillTag(Map input = [:], def docId) {
    return addBillTag(input + [docId: docId])
  }

  Boolean deleteBillTag(def docTagId) {
    return deleteDocumentTag(docTagId)
  }

  Boolean deleteBillTag(Map input) {
    return deleteDocumentTag(input)
  }

  Boolean deleteBillTag(def docId, CharSequence tag) {
    return deleteBillTag(docId: docId, tag: tag)
  }
}