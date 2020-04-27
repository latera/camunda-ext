package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.DateTimeUtil.local
import static org.camunda.latera.bss.utils.StringUtil.capitalize
import static org.camunda.latera.bss.utils.StringUtil.trim
import static org.camunda.latera.bss.utils.StringUtil.isEmpty

/**
 * Individual (person) helper methods collection
 */
trait Individual {
  /**
   * Get individual (person) data by id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubjectId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubjectName}            {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*BaseSubjectCode}            {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*BaseSubjectINN}             {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*BaseSubjectKPP}             {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*BaseSubjectOPFId}           {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*IndividualFirstName}        {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*IndividualSecondName}       {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*IndividualLastName}         {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*IndividualGender}           {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*IndividualBirthDate}        {@link java.time.LocalDateTime LocalDateTime}</li>
   *   <li>{@code homsOrderData*IndividualIdentType}        {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*IndividualIdentSerial}      {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*IndividualIdentNumber}      {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*IndividualIssuedAuthor}     {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*IndividualIssuedDate}       {@link java.time.LocalDateTime LocalDateTime}</li>
   *   <li>{@code homsOrderData*IndividualIssuedDepartment} {@link CharSequence String}</li>
   * </ul
   * @param prefix        {@link CharSequence String}. Individual (person) prefix. Optional. Default: empty strin
   * @param subjectPrefix {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   */
  void fetchIndividual(Map input = [:]) {
    Map params = [
      subjectPrefix : '',
      prefix        : ''
    ] + input

    String subjectPrefix = "${capitalize(params.subjectPrefix)}BaseSubject"
    String prefix = "${capitalize(params.prefix)}Individual"

    def baseSubjectId = order."${subjectPrefix}Id"
    if (isEmpty(baseSubjectId)) {
      return
    }

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
    order."${prefix}Gender"                = person?.n_sex_id
    order."${prefix}BirthDate"             = person?.d_birth ? local(person.d_birth) : null
    order."${prefix}BirthPlace"            = personPrivate?.vc_birth_place
    order."${prefix}IdentType"             = personPrivate?.n_doc_auth_type_id
    order."${prefix}IdentSerial"           = personPrivate?.vc_doc_serial
    order."${prefix}IdentNumber"           = personPrivate?.vc_doc_no
    order."${prefix}IdentIssuedAuthor"     = personPrivate?.vc_document
    order."${prefix}IdentIssuedDate"       = personPrivate?.d_doc ? local(personPrivate.d_doc) : null
    order."${prefix}IdentIssuedDepartment" = personPrivate?.vc_doc_department
  }

  /**
   * Create individual (person) and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubjectINN}             {@link CharSequence String}. Optional</li>
   *   <li>{@code homsOrderData*BaseSubjectKPP}             {@link CharSequence String}. Optional</li>
   *   <li>{@code homsOrderData*BaseSubjectOPFId}           {@link java.math.BigInteger BigInteger}. Optional</li>
   *   <li>{@code homsOrderData*IndividualFirstName}        {@link CharSequence String}. Optional</li>
   *   <li>{@code homsOrderData*IndividualSecondName}       {@link CharSequence String}. Optional</li>
   *   <li>{@code homsOrderData*IndividualLastName}         {@link CharSequence String}. Optional</li>
   *   <li>{@code homsOrderData*IndividualGender}           {@link java.math.BigInteger BigInteger}. Optional</li>
   *   <li>{@code homsOrderData*IndividualBirthDate}        {@link java.time.Temporal Any date type}. Optional</li>
   *   <li>{@code homsOrderData*IndividualIdentType}        {@link java.math.BigInteger BigInteger}. Optional</li>
   *   <li>{@code homsOrderData*IndividualIdentSerial}      {@link CharSequence String}. Optional</li>
   *   <li>{@code homsOrderData*IndividualIdentNumber}      {@link CharSequence String}. Optional</li>
   *   <li>{@code homsOrderData*IndividualIssuedAuthor}     {@link CharSequence String}. Optional</li>
   *   <li>{@code homsOrderData*IndividualIssuedDate}       {@link java.time.Temporal Any date type}. Optional</li>
   *   <li>{@code homsOrderData*IndividualIssuedDepartment} {@link CharSequence String}. Optional</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubjectId}      {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*BaseSubjectCreated} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param prefix        {@link CharSequence String}. Individual (person) prefix. Optional. Default: empty string
   * @param subjectPrefix {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   * @return True if individual (person) was created successfully, false otherwise
   */
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
      sexId         : order."${prefix}Gender",
      docTypeId     : order."${prefix}IdentType",
      docSerial     : order."${prefix}IdentSerial",
      docNumber     : order."${prefix}IdentNumber",
      docDate       : order."${prefix}IdentIssuedDate",
      docDepartment : order."${prefix}IdentIssuedDepartment",
      docAuthor     : order."${prefix}IdentIssuedAuthor",
      inn           : order."${subjectPrefix}INN",
      kpp           : order."${subjectPrefix}KPP",
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