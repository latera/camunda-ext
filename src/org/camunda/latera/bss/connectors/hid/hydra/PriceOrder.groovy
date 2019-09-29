package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Oracle.encodeDateStr

trait PriceOrder {
  private static String PRICE_ORDERS_TABLE = 'SD_V_PRICE_ORDERS_T'
  private static String PRICE_LINES_TABLE  = 'SD_V_PRICE_ORDERS_C'

  String getPriceOrdersTable() {
    return PRICE_ORDERS_TABLE
  }

  String getPriceLinesTable() {
    return PRICE_LINES_TABLE
  }

  List getPriceOrdersBy(Map input) {
    LinkedHashMap params = mergeParams([
      docId               : null,
      parentDocId         : null,
      reasonDocId         : null,
      workflowId          : null,
      providerId          : getFirmId(),
      stateId             : getDocumentStateActualId(),
      operationDate       : null,
      beginDate           : null,
      endDate             : null,
      number              : null,
      tags                : null,
      taxRateId           : null,
      currencyId          : null,
      sumRoundingId       : null,
      quantRoundingId     : null,
      calcDesignProcId    : null,
      deferTypeId         : null,
      schedDeferTypeId    : null,
      schedDeferPayDays   : null,
      unschedDeferPayDays : null,
      limit               : 0
    ], input)
    LinkedHashMap where = [:]

    if (params.docId) {
      where.n_doc_id = params.docId
    }
    if (params.parentDocId) {
      where.n_parent_doc_id = params.parentDocId
    }
    if (params.reasonDocId) {
      where.n_reason_doc_id = params.reasonDocId
    }
    if (params.workflowId) {
      where.n_workflow_id = params.workflowId
    }
    if (params.providerId) {
      where['_EXISTS'] = [
        """
        (SELECT 1
         FROM  ${getDocumentSubjectsTable()} DS
         WHERE DS.N_DOC_ID      = T.N_DOC_ID
         AND   DS.N_DOC_ROLE_ID = ${getProviderRoleId()}
         AND   DS.N_SUBJECT_ID  = ${params.providerId})"""
      ]
    }
    if (params.stateId) {
      where.n_doc_state_id = params.stateId
    }
    if (params.number) {
      where.vc_doc_no = params.number
    }
    if (params.name) {
      where.vc_doc_name = params.name
    }
    if (params.code) {
      where.vc_doc_code = params.code
    }
    if (params.beginDate) {
      where.d_begin = params.beginDate
    }
    if (params.endDate) {
      where.d_end = params.endDate
    }
    if (params.tags) {
      where.t_tags = params.tags
    }
    if (params.operationDate) {
      String oracleDate = encodeDateStr(params.operationDate)
      where[oracleDate] = [between: "d_begin and nvl(d_end, ${oracleDate})"]
    }
    if (params.taxRateId) {
      where.n_tax_rate_id = params.taxRateId
    }
    if (params.currencyId) {
      where.n_tax_rate_id = params.currencyId
    }
    if (params.sumRoundingId) {
      where.n_sum_rounding_id = params.sumRoundingId
    }
    if (params.quantRoundingId) {
      where.n_quant_rounding_id = params.quantRoundingId
    }
    if (params.calcDesignProcId) {
      where.n_calc_design_proc_id = params.calcDesignProcId
    }
    if (params.deferTypeId) {
      where.n_defer_type_id = params.deferTypeId
    }
    if (params.schedDeferTypeId) {
      where.n_sched_defer_type_id = params.schedDeferTypeId
    }
    if (params.schedDeferPayDays) {
      where.n_sched_defer_pay_days = params.schedDeferPayDays
    }
    if (params.unschedDeferPayDays) {
      where.n_unsched_defer_pay_days = params.unschedDeferPayDays
    }
    LinkedHashMap order = [d_begin: 'asc']
    return hid.getTableData(getPriceOrdersTable(), where: where, order: order, limit: params.limit)
  }

  Map getPriceOrder(def docId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return hid.getTableFirst(getPriceOrdersTable(), where: where)
  }

  List getPriceLinesBy(Map input) {
    LinkedHashMap params = mergeParams([
      docId             : null,
      lineId            : null,
      lineNumber        : null,
      parLineId         : null,
      goodId            : null,
      goodTypeId        : null,
      price             : null,
      priceWoTax        : null,
      unitId            : null,
      taxRateId         : null,
      currencyId        : null,
      sumRoundingId     : null,
      quantRoundingId   : null,
      quantRating       : null,
      quantRatingUnitId : null,
      quantFirst        : null,
      quantLast         : null,
      quantPrice        : null,
      initPaySum        : null,
      sumLast           : null,
      speedVolume       : null,
      speedUnitId       : null,
      addressId         : null,
      timeIntervalId    : null,
      providerId        : null,
      priceParam        : null,
      userRem           : null,
      limit             : 0
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
    if (params.goodTypeId) {
      where.n_good_type_id = params.goodTypeId
    }
    if (params.price) {
      where.n_price = params.price
    }
    if (params.priceWoTax) {
      where.n_price_wo_tax = params.priceWoTax
    }
    if (params.unitId) {
      where.n_unit_id = params.unitId
    }
    if (params.taxRateId) {
      where.n_tax_rate_id = params.taxRateId
    }
    if (params.currencyId) {
      where.n_tax_rate_id = params.currencyId
    }
    if (params.sumRoundingId) {
      where.n_sum_rounding_id = params.sumRoundingId
    }
    if (params.quantRoundingId) {
      where.n_quant_rounding_id = params.quantRoundingId
    }
    if (params.quantRating) {
      where.n_quant_rating = params.quantRating
    }
    if (params.quantRatingUnitId) {
      where.n_quant_rating_unit_id = params.quantRatingUnitId
    }
    if (params.quantFirst) {
      where.n_quant_first = params.quantFirst
    }
    if (params.quantLast) {
      where.n_quant_last = params.quantLast
    }
    if (params.quantPrice) {
      where.n_quant_price = params.quantPrice
    }
    if (params.initPaySum) {
      where.n_init_pay_sum = params.initPaySum
    }
    if (params.sumLast) {
      where.n_sum_last = params.sumLast
    }
    if (params.speedVolume) {
      where.n_speed_volume = params.speedVolume
    }
    if (params.speedUnitId) {
      where.n_speed_unit_id = params.speedUnitId
    }
    if (params.addressId) {
      where.n_address_id = params.addressId
    }
    if (params.timeIntervalId) {
      where.n_time_interval_id = params.timeIntervalId
    }
    if (params.providerId) {
      where.n_provider_id = params.providerId
    }
    if (params.priceParam) {
      where.vc_price_param_value = params.priceParam
    }
    if (params.userRem) {
      where.vc_user_rem = params.userRem
    }
    LinkedHashMap order = [n_line_no: 'asc']
    return hid.getTableData(getPriceLinesTable(), where: where, order: order, limit: params.limit)
  }

  List getPriceLines(def docId, Integer limit = 0) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return hid.getTableData(getPriceLinesTable(), where: where, limit: limit)
  }

  Map getPriceLineBy(Map input) {
    return getPriceLinesBy(input + [limit: 1])?.getAt(0)
  }

  Map getPriceLine(def lineId) {
    LinkedHashMap where = [
      n_price_line_id: lineId
    ]
    return hid.getTableFirst(getPriceLinesTable(), where: where)
  }
}