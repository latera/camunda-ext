package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Oracle.encodeDateStr
import static org.camunda.latera.bss.utils.Constants.DOC_TYPE_Invoice
import static org.camunda.latera.bss.utils.Constants.WFLOW_Invoice
import static org.camunda.latera.bss.utils.Constants.WFLOW_ProformaInvoice
import static org.camunda.latera.bss.utils.Constants.WFLOW_PrepaymentInvoice

trait Invoice {
  private static String  INVOICES_TABLE      = 'SD_V_INVOICES_T'
  private static String  INVOICE_LINES_TABLE = 'SD_V_INVOICES_C'

  String getInvoicesTable() {
    return INVOICES_TABLE
  }

  String getInvoiceLinesTable() {
    return INVOICE_LINES_TABLE
  }

  String getInvoiceType() {
    return getRefCode(getInvoiceTypeId())
  }

  Number getInvoiceTypeId() {
    return DOC_TYPE_Invoice
  }

  String getDefaultInvoiceWorkflow() {
    return getRefCode(getDefaultInvoiceWorkflowId())
  }

  Number getDefaultInvoiceWorkflowId() {
    return WFLOW_Invoice
  }

  String getDefaultProformaInvoiceWorkflow() {
    return getRefCode(getDefaultProformaInvoiceWorkflowId())
  }

  Number getDefaultProformaInvoiceWorkflowId() {
    return WFLOW_ProformaInvoice
  }

  String getDefaultPrepaymentInvoiceWorkflow() {
    return getRefCode(getDefaultPrepaymentInvoiceWorkflowId())
  }

  Number getDefaultPrepaymentInvoiceWorkflowId() {
    return WFLOW_PrepaymentInvoice
  }

  Map getInvoice(def docId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return hid.getTableFirst(getInvoicesTable(), where: where)
  }

  List getInvoicesBy(Map input) {
    input.docTypeId  = getInvoiceTypeId()
    return getDocumentsBy([providerId: null] + input)
  }

  Map getInvoiceBy(Map input) {
    input.docTypeId = getInvoiceTypeId()
    return getDocumentBy([providerId: null] + input)
  }

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

  Boolean actualizeInvoice(def docId) {
    return actualizeDocument(docId)
  }

  Boolean executeInvoice(def docId) {
    return executeDocument(docId)
  }

  Boolean cancelInvoice(def docId) {
    return cancelDocument(docId)
  }

  List getInvoiceLinesBy(Map input) {
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

  Map getInvoiceLine(def lineId) {
    LinkedHashMap where = [
      n_line_id: lineId
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
