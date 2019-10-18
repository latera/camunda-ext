package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.StringUtil.capitalize

trait Reseller {
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

  void fetchResellerCustomer(Map input = [:]) {
    Map params = [
      resellerPrefix : '',
      prefix         : ''
    ] + input

    String resellerPrefix = capitalize(params.resellerPrefix)
    String prefix = capitalize(params.prefix)

    def reseller = hydra.getReseller()
    Map resellerCustomer = hydra.getCustomerBy(baseSubjectId: reseller?.n_base_subject_id)

    order."${resellerPrefix}Reseller${prefix}CustomerId" = resellerCustomer?.n_subject_id
    fetchCustomer(
      prefix        : "${resellerPrefix}Reseller",
      subjectPrefix : "${resellerPrefix}Reseller"
    )
  }
}