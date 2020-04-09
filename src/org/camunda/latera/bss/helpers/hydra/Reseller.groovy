package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.StringUtil.capitalize

/**
 * Reseller helper methods collection
 */
trait Reseller {
  /**
   * Get reseller data by id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*ResellerId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*FirmId}       {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*ResellerCode} {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*ResellerName} {@link CharSequence String}</li>
   * </ul>
   * @param prefix {@link CharSequence String}. Reseller prefix. Optional. Default: empty string
   */
  void fetchReseller(Map input = [:]) {
    Map params = [
      prefix : ''
    ] + input

    String prefix = capitalize(params.prefix)

    Map reseller = hydra.getReseller()
    order."${prefix}FirmId"       = reseller?.n_firm_id
    order."${prefix}ResellerCode" = reseller?.vc_code
    order."${prefix}ResellerName" = reseller?.vc_name
  }

  /**
   * Get reseller technical customer data by id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Reseller*CustomerId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Reseller*CustomerCode}    {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Reseller*CustomerName}    {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Reseller*CustomerGroupId} {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Reseller*CustomerStateId} {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Reseller*CustomerFirmId}  {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * @param prefix         {@link CharSequence String}. Reseller technical customer prefix. Optional. Default: empty string
   * @param resellerPrefix {@link CharSequence String}. Reseller prefix. Optional. Default: empty string
   */
  void fetchResellerCustomer(Map input = [:]) {
    Map params = [
      resellerPrefix : '',
      prefix         : ''
    ] + input

    String resellerPrefix = capitalize(params.resellerPrefix)
    String prefix = capitalize(params.prefix)

    def reseller = hydra.getReseller()
    Map resellerCustomer = hydra.getCustomerBy(resellerId: reseller?.n_base_subject_id)

    order."${resellerPrefix}Reseller${prefix}CustomerId" = resellerCustomer?.n_subject_id
    fetchCustomer(
      prefix        : "${resellerPrefix}Reseller",
      subjectPrefix : "${resellerPrefix}Reseller"
    )
  }

  /**
   * Get reseller additional parameter value and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*ResellerId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Reseller*%Param%Id} {@link java.math.BigInteger BigInteger}. if additional parameter value is ref id</li>
   *   <li>{@code homsOrderData*Reseller*%Param%}   Any type, if additional parameter is not a ref</li>
   * </ul>
   * @param param          {@link CharSequence String}. Additional parameter short code (=variable part name) ('Param' for 'SUBJ_VAL_Param')
   * @param code           {@link CharSequence String}. Additional parameter full code (if it does not start from 'SUBJ_VAL_')
   * @param resellerPrefix {@link CharSequence String}. Reseller prefix. Optional. Default: empty string
   * @param prefix         {@link CharSequence String}. Additional parameter prefix. Optional. Default: empty string
   * @return Additional parameter value
   */
  def fetchResellerAddParam(Map input = [:]) {
    Map params = [
      resellerPrefix : '',
      prefix         : '',
      param          : '',
      code           : ''
    ] + input

    String resellerPrefix = "${capitalize(params.resellerPrefix)}Reseller"
    String prefix = capitalize(params.prefix)
    String param  = capitalize(params.param)

    Map addParam = hydra.getSubjectAddParamBy(
      subjectId : hydra.getReseller()?.n_subject_id,
      param     : params.code ?: "SUBJ_VAL_${param}"
    )
    def (value, valueType) = hydra.getAddParamValue(addParam)
    order."${resellerPrefix}${prefix}${params.code ?: param}${valueType == 'refId' ? 'Id': ''}" = value
    return value
  }
}