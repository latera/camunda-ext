package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.StringUtil.capitalize

trait Group {
  void fetchGroup(Map input = [:]) {
    Map params = [
      prefix : ''
    ] + input

    String prefix = "${capitalize(params.prefix)}Group"

    Map group = hydra.getGroup(order."${prefix}Id")
    order."${prefix}Name" = group?.vc_name
  }

  void fetchCustomerGroup(Map input = [:]) {
    Map params = [
      customerPrefix : '',
      prefix         : ''
    ] + input

    fetchGroup(prefix : "${capitalize(params.customerPrefix)}Customer${capitalize(params.prefix)}")
  }
}