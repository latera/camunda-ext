package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.StringUtil.capitalize

trait BaseSubject {
  void fetchBaseSubject(Map input = [:]) {
    Map params = [
      prefix : ''
    ] + input

    String prefix = "${capitalize(params.prefix)}BaseSubject"

    Map subject     = hydra.getSubject(order."${prefix}Id")
    String subjType = hydra.getRefCodeById(subject?.n_subj_type_id)

    order."${prefix}Type"      = subjType
    order."${prefix}IsCompany" = subjType == 'SUBJ_TYPE_Company'
  }
}