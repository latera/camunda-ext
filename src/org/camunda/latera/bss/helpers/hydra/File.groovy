package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.StringUtil.capitalize

trait File {
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