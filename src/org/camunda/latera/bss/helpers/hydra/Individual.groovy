package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.DateTimeUtil.local
import static org.camunda.latera.bss.utils.StringUtil.capitalize
import static org.camunda.latera.bss.utils.StringUtil.trim

trait Individual {
  void fetchIndividual(Map input = [:]) {
    Map params = [
      subjectPrefix : '',
      prefix        : ''
    ] + input

    String subjectPrefix = "${capitalize(params.subjectPrefix)}BaseSubject"
    String prefix = "${capitalize(params.prefix)}Individual"

    def baseSubjectId = order."${subjectPrefix}Id" ?: [is: 'null']
    Map person        = hydra.getPerson(baseSubjectId)
    Map personPrivate = hydra.getPersonPrivate(baseSubjectId)
    String opfCode = ''

    if (person?.n_opf_id) {
      opfCode = hydra.getOpfCode(person?.n_opf_id)
    }

    String code = trim([person?.vc_surname ?: '', person?.vc_first_name ?: '', person?.vc_second_name ?: ''].join(' ').replace('""', '"').replace('--', ''))
    String name = trim([opfCode, (opfCode ? "\"${code}\"" : code)].join(' '))

    order."${subjectPrefix}Name"           = name
    order."${subjectPrefix}Code"           = code
    order."${subjectPrefix}INN"            = person?.vc_inn
    order."${subjectPrefix}KPP"            = person?.vc_kpp
    order."${subjectPrefix}OPFId"          = person?.n_opf_id
    order."${prefix}FirstName"             = person?.vc_first_name
    order."${prefix}SecondName"            = person?.vc_second_name
    order."${prefix}LastName"              = person?.vc_surname
    order."${prefix}GenderId"              = person?.n_sex_id
    order."${prefix}BirthDate"             = person?.d_birth ? local(person.d_birth) : null
    order."${prefix}BirthPlace"            = personPrivate?.vc_birth_place
    order."${prefix}IdentTypeId"           = personPrivate?.n_doc_auth_type_id
    order."${prefix}IdentSerial"           = personPrivate?.vc_doc_serial
    order."${prefix}IdentNumber"           = personPrivate?.vc_doc_no
    order."${prefix}IdentIssuedAuthor"     = personPrivate?.vc_document
    order."${prefix}IdentIssuedDate"       = personPrivate?.d_doc ? local(personPrivate.d_doc) : null
    order."${prefix}IdentIssuedDepartment" = personPrivate?.vc_doc_department
  }

  Boolean createIndividual(Map input = [:]) {
    Map params = [
      subjectPrefix : '',
      prefix        : ''
    ] + input

    String subjectPrefix = "${capitalize(params.subjectPrefix)}BaseSubject"
    String prefix = "${capitalize(params.prefix)}Individual"

    Map individual = hydra.createPerson(
      firstName     : order."${prefix}FirstName",
      secondName    : order."${prefix}SecondName" ?: order."${prefix}MiddleName",
      lastName      : order."${prefix}LastName",
      sex           : order."${prefix}Gender",
      sexId         : order."${prefix}GenderId",
      docType       : order."${prefix}IdentType",
      docTypeId     : order."${prefix}IdentTypeId",
      docSerial     : order."${prefix}IdentSerial",
      docNumber     : order."${prefix}IdentNumber",
      docDate       : order."${prefix}IdentIssuedDate",
      docDepartment : order."${prefix}IdentIssuedDepartment",
      docAuthor     : order."${prefix}IdentIssuedAuthor",
      inn           : order."${subjectPrefix}INN",
      kpp           : order."${subjectPrefix}KPP",
      opf           : order."${subjectPrefix}OPF",
      opfId         : order."${subjectPrefix}OPFId",
      birthDate     : order."${prefix}BirthDate",
      birthPlace    : order."${prefix}BirthPlace"
    )
    Boolean result = false
    if (individual) {
      order."${subjectPrefix}Id" = individual.num_N_SUBJECT_ID
      result = true
    }
    order."${subjectPrefix}Created" = result
    return result
  }
}