package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.utils.DateTimeUtil
import org.camunda.latera.bss.utils.Oracle
import java.time.LocalDateTime

trait Invoice {
  private static String INVOICES_TABLE      = 'SD_V_INVOICES_T'
  private static String GOOD_MOVES_TABLE    = 'SD_V_GOOD_MOVES_T'
  private static String INVOICE_LINES_TABLE = 'SD_V_INVOICES_C'
  private static String INVOICE_TYPE        = 'DOC_TYPE_Invoice'
  private static String DEFAULT_INVOICE_WORKFLOW = 'WFLOW_Invoice'

  def getInvoicesTable() {
    return INVOICES_TABLE
  }

  def getGoodMovesTable() {
    return GOOD_MOVES_TABLE
  }

  def getInvoiceLinesTable() {
    return INVOICE_LINES_TABLE
  }

  def getInvoiceType() {
    return INVOICE_TYPE
  }

  def getInvoiceTypeId() {
    return getRefIdByCode(INVOICE_TYPE)
  }

  def getDefaultInvoiceWorkflow() {
    return DEFAULT_INVOICE_WORKFLOW
  }

  def getDefaultInvoiceWorkflowId() {
    return getRefIdByCode(DEFAULT_INVOICE_WORKFLOW_ID)
  }

  LinkedHashMap getInvoice(def docId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return hid.getTableFirst(getInvoicesTable(), where: where)
  }

  List getInvoicesBy(LinkedHashMap input) {
    input.docTypeId = getInvoiceTypeId()
    return getDocumentsBy(input)
  }

  LinkedHashMap getInvoiceBy(LinkedHashMap input) {
    input.docTypeId = getInvoiceTypeId()
    return getDocumentBy(input)
  }

  def getInvoiceIdBySubscription(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      subscriptionId : null,
      operationDate  : DateTimeUtil.now()
    ], input)
    try {
      def docId = hid.queryFirst("""
      SELECT
        SD_INVOICES_PKG_S.GET_INVOICE_ID_BY_SUBJ_GOOD(
          num_N_SUBJ_GOOD_ID => ${params.subscriptionId},
          dt_D_OPER => ${Oracle.encodeDateStr(params.operationDate)}
        )
      FROM DUAL""")[0]
      return docId
    } catch (Exception e){
      logger.error_oracle(e)
      return null
    }
  }

  def getInvoiceIdBySubscription(def subscriptionId, LocalDateTime operationDate = DateTimeUtil.now()) {
    return getInvoiceIdBySubscription(subscriptionId: subscriptionId, operationDate: operationDate)
  }

  LinkedHashMap getInvoiceBySubscription(LinkedHashMap input) {
    def docId = getInvoiceIdBySubscription(input)
    if (docId == null) {
      return null
    }
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return hid.getTableFirst(getInvoicesTable(), where: where)
  }

  LinkedHashMap getInvoiceBySubscription(def subscriptionId, LocalDateTime operationDate = DateTimeUtil.now()) {
    return getInvoiceBySubscription(subscriptionId: subscriptionId, operationDate: operationDate)
  }

  List getInvoicesBySubscription(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      subscriptionId : null,
      stateId        : ['not in': "(${getDocumentStateCanceledId()})"],
      operationDate  : null
    ], input)
    LinkedHashMap where = [
      n_subj_good_id: params.subscriptionId
    ]
    if (params.stateId) {
      where.n_doc_state_id = params.stateId
    }
    if (params.operationDate) {
      String oracleDate = Oracle.encodeDateStr(params.operationDate)
      where[oracleDate] = [BETWEEN: "D_BEGIN AND NVL(D_END, ${oracleDate})"]
    }
    return hid.getTableData(getGoodMovesTable(), where: where)
  }

  List getInvoicesBySubscription(def subscriptionId, def stateId = ['not in': "(${getDocumentStateCanceledId()})"], LocalDateTime operationDate = null) {
    return getInvoicesBySubscription(subscriptionId: subscriptionId, stateId: stateId, operationDate: operationDate)
  }

  Boolean isInvoice(String entityType) {
    return entityType == getInvoiceType()
  }

  Boolean isInvoice(def entityIdOrEntityTypeId) {
    return entityIdOrEntityTypeId == getInvoiceTypeId() || getDocument(entityIdOrEntityTypeId).n_doc_type_id == getInvoiceTypeId()
  }

  Boolean changeInvoiceEnd(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      docId         : null,
      endDate       : null,
      closeReasonId : null
    ], input)
    try {
      logger.info("Changing invoice id ${params.docId} end date to ${params.endDate} with reason ${params.closeReasonId}")
      LinkedHashMap contract = hid.execute('SD_INVOICES_PKG.CHANGE_INVOICE_PERIOD', [
        num_N_DOC_ID          : params.docId,
        dt_D_OPER             : params.endDate,
        num_N_CLOSE_REASON_ID : params.closeReasonId
      ])
      logger.info("   Invoice ${contract.num_N_DOC_ID} end date was changed successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while changing invoice end date!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean changeInvoiceEnd(def docId, LocalDateTime endDate = DateTimeUtil.now(), def closeReasonId = null) {
    return changeInvoiceEnd(docId: docId, endDate: endDate, closeReasonId: closeReasonId)
  }

  Boolean closeInvoice(LinkedHashMap input) {
    return changeInvoiceEnd(input)
  }

  Boolean closeInvoice(def docId, LocalDateTime endDate = DateTimeUtil.now(), def closeReasonId = null) {
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
}