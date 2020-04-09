package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.StringUtil.capitalize

/**
 * Group helper methods collection
 */
trait Group {
  /**
   * Get group data by id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*GroupId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*GroupName} {@link CharSequence String}</li>
   * </ul>
   * @param prefix {@link CharSequence String}. Group prefix. Optional. Default: empty string
   */
  void fetchGroup(Map input = [:]) {
    Map params = [
      prefix : ''
    ] + input

    String prefix = "${capitalize(params.prefix)}Group"

    Map group = hydra.getGroup(order."${prefix}Id")
    order."${prefix}Name" = group?.vc_name
  }

  /**
   * Get customer group data by id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Customer*GroupId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Customer*GroupName} {@link CharSequence String}</li>
   * </ul>
   * @param prefix         {@link CharSequence String}. Group prefix. Optional. Default: empty string
   * @param customerPrefix {@link CharSequence String}. Customer prefix. Optional. Default: empty string
   */
  void fetchCustomerGroup(Map input = [:]) {
    Map params = [
      customerPrefix : '',
      prefix         : ''
    ] + input

    fetchGroup(prefix : "${capitalize(params.customerPrefix)}Customer${capitalize(params.prefix)}")
  }
}