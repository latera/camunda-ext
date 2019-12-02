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

  def fetchBaseSubjectAddParam(Map input = [:]) {
    Map params = [
      baseSubjectPrefix : '',
      prefix            : '',
      param             : '',
      code              : ''
    ] + input

    String baseSubjectPrefix = "${capitalize(params.baseSubjectPrefix)}BaseSubject"
    String prefix = capitalize(params.prefix)
    String param  = capitalize(params.param)

    def baseSubjectId = order."${baseSubjectPrefix}Id" ?: [is: 'null']
    Map addParam = hydra.getSubjectAddParamBy(
      subjectId : baseSubjectId,
      param     : params.code ?: "SUBJ_VAL_${param}"
    )
    def (value, valueType) = hydra.getAddParamValue(addParam)
    order."${baseSubjectPrefix}${prefix}${params.code ?: param}${valueType == 'refId' ? 'Id': ''}" = value
    return value
  }

  Boolean saveBaseSubjectAddParam(Map input = [:]) {
    Map params = [
      baseSubjectPrefix : '',
      prefix            : '',
      param             : '',
      code              : ''
    ] + input

    String baseSubjectPrefix = "${capitalize(params.baseSubjectPrefix)}BaseSubject"
    String prefix     = capitalize(params.prefix)
    String param  = capitalize(params.param)
    def baseSubjectId = order."${baseSubjectPrefix}Id"
    def value         = order."${baseSubjectPrefix}${prefix}${params.code ?: param}" ?: order."${baseSubjectPrefix}${prefix}${params.code ?: param}Id"

    Map addParam = hydra.addCustomerAddParam(
      subjectId : baseSubjectId,
      param     : params.code ?: "SUBJ_VAL_${param}",
      value     : value
    )
    Boolean result = false
    if (addParam) {
      result = true
    }
    order."${baseSubjectPrefix}${prefix}${params.code ?: param}Saved" = result
    return result
  }

  Boolean deleteBaseSubjectAddParam(Map input = [:]) {
    Map params = [
      baseSubjectPrefix : '',
      prefix            : '',
      param             : '',
      code              : '',
      force             : false
    ] + input

    String baseSubjectPrefix = "${capitalize(params.baseSubjectPrefix)}BaseSubject"
    String prefix = capitalize(params.prefix)
    String param  = capitalize(params.param)

    def baseSubjectId = order."${baseSubjectPrefix}Id"
    def value         = order."${baseSubjectPrefix}${prefix}${params.code ?: param}" ?: order."${baseSubjectPrefix}${prefix}${params.code ?: param}Id"

    Boolean result = true

    if (params.force) {
      result = hydra.deleteSubjectAddParam(
        subjectId : baseSubjectId,
        param     : params.code ?: "SUBJ_VAL_${param}"
      )
    } else {
      result = hydra.deleteSubjectAddParam(
        subjectId : baseSubjectId,
        param     : params.code ?: "SUBJ_VAL_${param}",
        value     : value // multiple add param support
      )
    }

    order."${baseSubjectPrefix}${prefix}${params.code ?: param}Deleted" = result
    return result
  }
}