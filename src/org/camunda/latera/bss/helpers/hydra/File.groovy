package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.StringUtil.capitalize

/**
 * File helper methods collection
 */
trait File {
  /**
   * Attach files to a customer and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*CustomerId} {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*FileList}  {@link List}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Customer*Files*Attached} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param prefix         {@link CharSequence String}. Result value prefix. Optional. Default: empty string
   * @param customerPrefix {@link CharSequence String}. Customer prefix. Optional. Default: empty string
   * @param filesPrefix    {@link CharSequence String}. File field prefix. Optional. Default: empty string
   * @return True if files from list were successfully attached to a customer, false otherwise
   */
  Boolean attachCustomerFiles(Map input = [:]) {
    Map params = [
      customerPrefix : '',
      filesPrefix    : '',
      prefix         : ''
    ] + input

    String customerPrefix = "${capitalize(params.customerPrefix)}Customer"
    String filesPrefix    = capitalize(params.filesPrefix)
    String prefix         = "${customerPrefix}${filesPrefix}Files${capitalize(params.prefix)}"

    List attachments = order.getFilesContent(filesPrefix)
    List files = hoper.createSubjectFiles(order."${customerPrefix}Id", attachments)

    Boolean result = false
    if (files.size() == attachments.size() && (files.findAll { it == null }).size() == 0) {
      result = true
    }
    order."${prefix}Attached" = result
    return result
  }
}