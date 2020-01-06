package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.StringUtil.capitalize
import static org.camunda.latera.bss.utils.StringUtil.trim

trait Company {
  void fetchCompany(Map input = [:]) {
    Map params = [
      subjectPrefix : '',
      prefix        : ''
    ] + input

    String subjectPrefix = "${capitalize(params.subjectPrefix)}BaseSubject"
    String prefix = "${capitalize(params.prefix)}Company"

    def baseSubjectId = order."${subjectPrefix}Id" ?: [is: 'null']
    Map company = hydra.getCompany(baseSubjectId)
    String opfCode = ''

    if (company?.n_opf_id) {
      opfCode = hydra.getOpfCode(company?.n_opf_id)
    }
    String code = trim("${company?.vc_code}".replace('""', '"').replace('--', ''))
    String name = trim([opfCode, (opfCode ? "\"${code}\"" : code)].join(' '))

    order."${subjectPrefix}Code"  = code
    order."${subjectPrefix}Name"  = name
    order."${subjectPrefix}INN"   = company?.vc_inn
    order."${subjectPrefix}KPP"   = company?.vc_kpp
    order."${subjectPrefix}OPFId" = company?.n_opf_id
    order."${prefix}Name"         = name
    order."${prefix}Code"         = code
    order."${prefix}OGRN"         = company?.vc_ogrn
    order."${prefix}OCPO"         = company?.vc_ocpo
    order."${prefix}OKVED"        = company?.vc_okved
  }

  Boolean createCompany(Map input = [:]) {
    Map params = [
      subjectPrefix : '',
      prefix        : ''
    ] + input

    String subjectPrefix = "${capitalize(params.subjectPrefix)}BaseSubject"
    String prefix = "${capitalize(params.prefix)}Company"

    def opfId = order."${subjectPrefix}OPFId"
    String opfCode = ''

    if (opfId) {
      opfCode = hydra.getOpfCode(opfId)
    }
    String companyCode = trim(order."${prefix}Code")
    String name = trim([opfCode, (opfCode ? "\"${companyCode}\"" : companyCode)].join(' '))

    Map company = hydra.createCompany(
      name  : name.toString(),
      code  : order."${prefix}Code",
      opfId : order."${subjectPrefix}OPFId",
      inn   : trim(order."${subjectPrefix}INN"),
      kpp   : trim(order."${subjectPrefix}KPP"),
      ogrn  : trim(order."${prefix}OGRN"),
      ocpo  : trim(order."${prefix}OCPO"),
      okved : trim(order."${prefix}OKVED")
    )
    Boolean result = false
    if (company) {
      order."${subjectPrefix}Id" = company.num_N_SUBJECT_ID
      result = true
    }
    order."${subjectPrefix}Created" = result
    return result
  }
}