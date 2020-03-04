package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Oracle.encodeDateStr

trait PriceOrder {
  private static String PRICE_ORDERS_TABLE = 'SD_V_PRICE_ORDERS_T'
  private static String PRICE_LINES_TABLE  = 'SD_V_PRICE_ORDERS_C'

  /**
   * Get price orders table name
   */
  String getPriceOrdersTable() {
    return PRICE_ORDERS_TABLE
  }

  /**
   * Get price order lines table name
   */
  String getPriceLinesTable() {
    return PRICE_LINES_TABLE
  }

  /**
   * Search for price orders by different fields value
   * @param docId               {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parentDocId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param reasonDocId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param workflowId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param providerId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. Default: current firm id
   * @param stateId             {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param state               {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param operationDate       {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param beginDate           {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param endDate             {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param tags                {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param taxRateId           {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param taxRate             {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param currencyId          {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param currency            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param sumRoundingId       {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param sumRounding         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param quantRoundingId     {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param quantRounding       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param calcDesignProcId    {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param calcDesignProc      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param deferTypeId         {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param deferType           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param schedDeferTypeId    {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param schedDeferTypeId    {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param schedDeferPayDays   {@link Integet}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param unschedDeferPayDays {@link Integet}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit               {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order               {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: D_BEGIN ASC_LINE_NO DESC
   * @return Price order table rows
   */
  List<Map> getPriceOrdersBy(Map input) {
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
      tags                : null,
      limit               : 0,
      order               : [d_begin: 'asc']
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
      where += prepareEntityTagQuery('N_DOC_ID', params.tags)
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
    return hid.getTableData(getPriceOrdersTable(), where: where, order: params.order, limit: params.limit)
  }

  /**
   * Search for one price order by different fields value
   * @param docId               {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parentDocId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param reasonDocId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param workflowId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param providerId          {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. Default: current firm id
   * @param stateId             {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param state               {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param operationDate       {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param beginDate           {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param endDate             {@link java.time.Temporal Any date type}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param tags                {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param taxRateId           {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param taxRate             {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param currencyId          {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param currency            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param sumRoundingId       {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param sumRounding         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param quantRoundingId     {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param quantRounding       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param calcDesignProcId    {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param calcDesignProc      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param deferTypeId         {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param deferType           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param schedDeferTypeId    {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param schedDeferTypeId    {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param schedDeferPayDays   {@link Integet}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param unschedDeferPayDays {@link Integet}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param order               {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: D_BEGIN ASC_LINE_NO DESC
   * @return Price order table row
   */
  Map getPriceOrder(def docId) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return hid.getTableFirst(getPriceOrdersTable(), where: where)
  }

  /**
   * Search for price order lines by different fields value
   * @param docId             {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lineId            {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lineNumber        {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parLineId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodId            {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodTypeId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. Default: not cancelled
   * @param goodType          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param price             {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param priceWoTax        {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param unitId            {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param unit              {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param taxRateId         {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param taxRate           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param currencyId        {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param currency          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param sumRoundingId     {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param sumRounding       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param quantRoundingId   {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param quantRounding     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param quantRating       {@link Integer} with WHERE clause or SELECT query. Optional
   * @param quantRatingUnitId {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param quantRatingUnit   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param quantFirst        {@link Integer} with WHERE clause or SELECT query. Optional
   * @param quantLast         {@link Integer} with WHERE clause or SELECT query. Optional
   * @param quantPrice        {@link Integer} with WHERE clause or SELECT query. Optional
   * @param initPaySum        {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param sumLast           {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param speedVolume       {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param speedUnitId       {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param speedUnit         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param addressId         {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param timeIntervalId    {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param providerId        {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional. Default: current firm id
   * @param priceParam        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param userRem           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit             {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order             {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: N_LINE_NO DESC
   * @return Price order line table rows
   */
  List<Map> getPriceLinesBy(Map input) {
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
    return hid.getTableData(getPriceLinesTable(), where: where, order: params.order, limit: params.limit)
  }

  /**
   * Get price order lines by doc id
   * @param docId {@link java.math.BigInteger BigInteger}
   * @param limit {@link Integer}. Optional. Default: 0 (unlimited)
   * @return Price order line table rows
   */
  List<Map> getPriceLines(def docId, Integer limit = 0) {
    LinkedHashMap where = [
      n_doc_id: docId
    ]
    return hid.getTableData(getPriceLinesTable(), where: where, limit: limit)
  }

  /**
   * Search for one price order line by different fields value
   * @param docId             {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lineId            {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lineNumber        {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parLineId         {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodId            {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param goodTypeId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. Default: not cancelled
   * @param goodType          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param price             {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param priceWoTax        {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param unitId            {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param unit              {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param taxRateId         {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param taxRate           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param currencyId        {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param currency          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param sumRoundingId     {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param sumRounding       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param quantRoundingId   {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param quantRounding     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param quantRating       {@link Integer} with WHERE clause or SELECT query. Optional
   * @param quantRatingUnitId {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param quantRatingUnit   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param quantFirst        {@link Integer} with WHERE clause or SELECT query. Optional
   * @param quantLast         {@link Integer} with WHERE clause or SELECT query. Optional
   * @param quantPrice        {@link Integer} with WHERE clause or SELECT query. Optional
   * @param initPaySum        {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param sumLast           {@link Double}, {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param speedVolume       {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param speedUnitId       {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param speedUnit         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param addressId         {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param timeIntervalId    {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional
   * @param providerId        {@link java.math.BigInteger BigInteger} with WHERE clause or SELECT query. Optional. Default: current firm id
   * @param priceParam        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param userRem           {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit             {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order             {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional. Default: N_LINE_NO DESC
   * @return Price order line table row
   */
  Map getPriceLineBy(Map input) {
    return getPriceLinesBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Get price order line by id
   * @param lineId {@link java.math.BigInteger BigInteger}
   * @return Price order line table row
   */
  Map getPriceLine(def lineId) {
    LinkedHashMap where = [
      n_price_line_id: lineId
    ]
    return hid.getTableFirst(getPriceLinesTable(), where: where)
  }
}