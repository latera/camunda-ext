package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.StringUtil.capitalize
import static org.camunda.latera.bss.utils.StringUtil.trim
import static org.camunda.latera.bss.utils.StringUtil.isEmpty

/**
  * Company helper methods collection
  */
trait Company {
  /**
   * Get company data by id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubjectId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubjectName}  {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*BaseSubjectCode}  {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*BaseSubjectINN}   {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*BaseSubjectKPP}   {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*BaseSubjectOPFId} {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*CompanyName}      {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*CompanyCode}      {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*CompanyOGRN}      {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*CompanyOCPO}      {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*CompanyOKVED}     {@link CharSequence String}</li>
   * </ul>
   * @param prefix        {@link CharSequence String}. Company prefix. Optional. Default: empty string
   * @param subjectPrefix {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   */
  void fetchCompany(Map input = [:]) {
    Map params = [
      subjectPrefix : '',
      prefix        : ''
    ] + input

    String subjectPrefix = "${capitalize(params.subjectPrefix)}BaseSubject"
    String prefix = "${capitalize(params.prefix)}Company"

    def baseSubjectId = order."${subjectPrefix}Id"
    if (isEmpty(baseSubjectId)) {
      return
    }

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

  /**
   * Create company and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubjectINN}   {@link CharSequence String}. Optional</li>
   *   <li>{@code homsOrderData*BaseSubjectKPP}   {@link CharSequence String}. Optional</li>
   *   <li>{@code homsOrderData*BaseSubjectOPFId} {@link java.math.BigInteger BigInteger}. Optional</li>
   *   <li>{@code homsOrderData*CompanyName}      {@link CharSequence String}. Optional</li>
   *   <li>{@code homsOrderData*CompanyCode}      {@link CharSequence String}. Optional</li>
   *   <li>{@code homsOrderData*CompanyOGRN}      {@link CharSequence String}. Optional</li>
   *   <li>{@code homsOrderData*CompanyOCPO}      {@link CharSequence String}. Optional</li>
   *   <li>{@code homsOrderData*CompanyOKVED}     {@link CharSequence String}. Optional</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubjectId}      {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*BaseSubjectCreated} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param prefix        {@link CharSequence String}. Company prefix. Optional. Default: empty string
   * @param subjectPrefix {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   * @return True if company was created successfully, false otherwise
   */
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
    String companyName = trim([opfCode, (opfCode ? "\"${companyCode}\"" : companyCode)].join(' '))

    Map company = hydra.createCompany(
      name  : companyName,
      code  : companyCode,
      opfId : opfId,
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