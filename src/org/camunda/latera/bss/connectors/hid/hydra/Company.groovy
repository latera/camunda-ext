package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.StringUtil.isEmpty
import static org.camunda.latera.bss.utils.StringUtil.notEmpty
import static org.camunda.latera.bss.utils.Constants.SUBJ_TYPE_Company

trait Company {
  private static String COMPANIES_TABLE = 'SI_V_COMPANIES'

  /**
   * Get companies table name
   */
  String getCompaniesTable() {
    return COMPANIES_TABLE
  }

  /**
   * Get company subject type ref code
   */
  String getCompanyType() {
    return getRefCode(getCompanyTypeId())
  }

  /**
   * Get company subject type ref id
   */
  Number getCompanyTypeId() {
    return SUBJ_TYPE_Company
  }

  /**
   * Get company by id
   * @param companyId {@link java.math.BigInteger BigInteger}
   * @return Map with company table row or null
   */
  Map getCompany(def companyId) {
    LinkedHashMap where = [
      n_subject_id: companyId
    ]
    return hid.getTableFirst(getCompaniesTable(), where: where)
  }

  /**
   * Search for companies by different fields value
   * @param companyId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param regionId  {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param creatorId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param inn       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param kpp       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ocato     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ocfs      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ocogu     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ocopf     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ogrn      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param okved     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param opfId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param opf       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param groupId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param stateId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param state     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param firmId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. default: current firm Id
   * @param tags      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit     {@link Integer}. Optional, default: 0 (unlimited)
   * @param order     {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: [:]
   * @return List[Map] of company table rows
   */
  List getCompaniesBy(Map input) {
    LinkedHashMap params = mergeParams([
      companyId : null,
      regionId  : null,
      creatorId : null,
      name      : null,
      code      : null,
      inn       : null,
      kpp       : null,
      ocato     : null,
      ocfs      : null,
      ocogu     : null,
      ocopf     : null,
      ogrn      : null,
      okved     : null,
      opfId     : null,
      groupId   : null,
      firmId    : getFirmId(),
      stateId   : getSubjectStateOnId(),
      tags      : null,
      limit     : 0
    ], input)
    LinkedHashMap where = [:]

    if (params.companyId) {
      where.n_company_id = params.companyId
    }
    if (params.regionId) {
      where.n_region_id = params.regionId
    }
    if (params.creatorId) {
      where.n_creator_id = params.creatorId
    }
    if (params.name) {
      where.vc_name = params.name
    }
    if (params.code) {
      where.vc_code = params.code
    }
    if (params.inn) {
      where.vc_inn = params.inn
    }
    if (params.kpp) {
      where.vc_kpp = params.kpp
    }
    if (params.ocato) {
      where.vc_ocato = params.ocato
    }
    if (params.ocfs) {
      where.vc_ocfs = params.ocfs
    }
    if (params.ocogu) {
      where.vc_ocogu = params.ocogu
    }
    if (params.ocopf) {
      where.vc_ocopf = params.ocopf
    }
    if (params.ogrn) {
      where.vc_ogrn = params.ogrn
    }
    if (params.okved) {
      where.vc_okved = params.okved
    }
    if (params.opfId) {
      where.n_opf_id = params.opfId
    }
    if (params.groupId) {
      where.n_subj_group_id = params.groupId
    }
    if (params.firmId) {
      where.n_firm_id = params.firmId
    }
    if (params.stateId) {
      where.n_subj_state_id = params.stateId
    }
    if (params.tags) {
      where += prepareEntityTagQuery('N_COMPANY_ID', params.tags)
    }
    return hid.getTableData(getCompaniesTable(), where: where, order: params.order, limit: params.limit)
  }

  /**
   * Search for one company by different fields value
   * @param companyId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param regionId  {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param creatorId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param inn       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param kpp       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ocato     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ocfs      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ocogu     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ocopf     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ogrn      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param okved     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param opfId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param opf       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param groupId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param stateId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param state     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param firmId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. default: current firm Id
   * @param tags      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param order     {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: [:]
   * @return Map with company table row
   */
  Map getCompanyBy(Map input) {
    return getCompaniesBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Check if entity or entity type is company
   * @param entityOrEntityType {@link java.math.BigInteger BigInteger} or {@link CharSequence String}. Subject id, subject type ref id or subject type ref code
   * @return True if given value is company, false otherwise
   */
  Boolean isCompany(def entityOrEntityType) {
    if (entityOrEntityType == null) {
      return false
    }

    Number entityIdOrEntityTypeId = toIntSafe(entityOrEntityType)
    if (entityIdOrEntityTypeId != null) {
      return entityIdOrEntityTypeId == getCompanyTypeId() || getCompany(entityIdOrEntityTypeId) != null
    } else {
      return entityOrEntityType == getCompanyType()
    }
  }

  /**
   * Create or update company
   * @param companyId {@link java.math.BigInteger BigInteger}. Optional
   * @param name      {@link CharSequence String}. Optional
   * @param code      {@link CharSequence String}. Optional
   * @param opfId     {@link java.math.BigInteger BigInteger}. Optional
   * @param opf       {@link CharSequence String}. Optional
   * @param inn       {@link CharSequence String}. Optional
   * @param kpp       {@link CharSequence String}. Optional
   * @param ocato     {@link CharSequence String}. Optional
   * @param ocfs      {@link CharSequence String}. Optional
   * @param ocogu     {@link CharSequence String}. Optional
   * @param ocopf     {@link CharSequence String}. Optional
   * @param ogrn      {@link CharSequence String}. Optional
   * @param okved     {@link CharSequence String}. Optional
   * @param rem       {@link CharSequence String}. Optional
   * @param regionId  {@link java.math.BigInteger BigInteger}. Optional
   * @param groupId   {@link java.math.BigInteger BigInteger}. Optional
   * @param firmId    {@link java.math.BigInteger BigInteger}. Optional. Default: current firm Id
   * @param stateId   {@link java.math.BigInteger BigInteger}. Optional
   * @param state     {@link CharSequence String}. Optional
   * @return Map with created company (in Oracle API procedure notation)
   */
  private Map putCompany(Map input) {
    LinkedHashMap defaultParams = [
      companyId : null,
      name      : null,
      code      : null,
      opfId     : null,
      inn       : null,
      kpp       : null,
      ocato     : null,
      ocfs      : null,
      ocogu     : null,
      ocopf     : null,
      ogrn      : null,
      okved     : null,
      rem       : null,
      regionId  : null,
      groupId   : null,
      firmId    : getFirmId(),
      stateId   : getSubjectStateOnId()
    ]
    try {
      if (isEmpty(input.companyId) && notEmpty(input.subjectId)) {
        input.companyId = input.subjectId
      }

      LinkedHashMap existingCompany = [:]

      if (notEmpty(input.companyId)) {
        LinkedHashMap company = getCompany(input.companyId)
        existingCompany = [
          companyId : company.n_company_id,
          name      : company.vc_name,
          code      : company.vc_code,
          opfId     : company.n_opf_id,
          inn       : company.vc_inn,
          kpp       : company.vc_kpp,
          ocato     : company.vc_ocato,
          ocfs      : company.vc_ocfs,
          ocogu     : company.vc_ocogu,
          ocopf     : company.vc_ocopf,
          ogrn      : company.vc_ogrn,
          okved     : company.vc_okved,
          rem       : company.vc_rem,
          regionId  : company.n_region_id,
          groupId   : company.n_subj_group_id,
          firmId    : company.n_firm_id,
          stateId   : company.n_subj_state_id
        ]
      }
      LinkedHashMap params = mergeParams(defaultParams, existingCompany + input)

      logger.info("${params.companyId ? 'Updating' : 'Creating'} company with params ${params}")
      LinkedHashMap result = hid.execute('SI_COMPANIES_PKG.SI_COMPANIES_PUT',[
        num_N_SUBJECT_ID    : params.companyId,
        num_N_FIRM_ID       : params.firmId,
        num_N_SUBJ_STATE_ID : params.stateId,
        num_N_SUBJ_GROUP_ID : params.groupId,
        num_N_REGION_ID     : params.regionId,
        vch_VC_CODE         : params.code,
        vch_VC_NAME         : params.name,
        num_N_OPF_ID        : params.opfId,
        vch_VC_INN          : params.inn,
        vch_VC_KPP          : params.kpp,
        vch_VC_OCATO        : params.ocato,
        vch_VC_OCFS         : params.ocfs,
        vch_VC_OCOGU        : params.ocogu,
        vch_VC_OCOPF        : params.ocopf,
        vch_VC_OGRN         : params.ogrn,
        vch_VC_OKVED        : params.okved,
        vch_VC_REM          : params.rem
      ])
      logger.info("   Company ${result.num_N_SUBJECT_ID} was ${params.companyId ? 'put' : 'created'} successfully!")
      return result
    } catch (Exception e){
      logger.error("   Error while ${input.companyId ? 'updating' : 'creating'} company!")
      logger.error_oracle(e)
      return null
    }
  }

  /**
   * Create company
   * @param name     {@link CharSequence String}. Optional
   * @param code     {@link CharSequence String}. Optional
   * @param opfId    {@link java.math.BigInteger BigInteger}. Optional
   * @param opf      {@link CharSequence String}. Optional
   * @param inn      {@link CharSequence String}. Optional
   * @param kpp      {@link CharSequence String}. Optional
   * @param ocato    {@link CharSequence String}. Optional
   * @param ocfs     {@link CharSequence String}. Optional
   * @param ocogu    {@link CharSequence String}. Optional
   * @param ocopf    {@link CharSequence String}. Optional
   * @param ogrn     {@link CharSequence String}. Optional
   * @param okved    {@link CharSequence String}. Optional
   * @param rem      {@link CharSequence String}. Optional
   * @param regionId {@link java.math.BigInteger BigInteger}. Optional
   * @param groupId  {@link java.math.BigInteger BigInteger}. Optional
   * @param firmId   {@link java.math.BigInteger BigInteger}. Optional. Default: current firm Id
   * @param stateId  {@link java.math.BigInteger BigInteger}. Optional
   * @param state    {@link CharSequence String}. Optional
   * @return Map with created company (in Oracle API procedure notation)
   */
  Map createCompany(Map input) {
    input.remove('companyId')
    return putCompany(input)
  }

  /**
   * Update company
   * @param companyId{@link java.math.BigInteger BigInteger}
   * @param name     {@link CharSequence String}. Optional
   * @param code     {@link CharSequence String}. Optional
   * @param opfId    {@link java.math.BigInteger BigInteger}. Optional
   * @param opf      {@link CharSequence String}. Optional
   * @param inn      {@link CharSequence String}. Optional
   * @param kpp      {@link CharSequence String}. Optional
   * @param ocato    {@link CharSequence String}. Optional
   * @param ocfs     {@link CharSequence String}. Optional
   * @param ocogu    {@link CharSequence String}. Optional
   * @param ocopf    {@link CharSequence String}. Optional
   * @param ogrn     {@link CharSequence String}. Optional
   * @param okved    {@link CharSequence String}. Optional
   * @param rem      {@link CharSequence String}. Optional
   * @param regionId {@link java.math.BigInteger BigInteger}. Optional
   * @param groupId  {@link java.math.BigInteger BigInteger}. Optional
   * @param firmId   {@link java.math.BigInteger BigInteger}. Optional. Default: current firm Id
   * @param stateId  {@link java.math.BigInteger BigInteger}. Optional
   * @param state    {@link CharSequence String}. Optional
   * @return Map with created company (in Oracle API procedure notation)
   */
  Map updateCompany(Map input = [:], def companyId) {
    return putCompany(input + [companyId: companyId])
  }

  /**
   * Get additional param type id by code
   * @param code {@link CharSequence String}
   * @return Additional param type id
   */
  Number getCompanyAddParamTypeIdByCode(CharSequence code) {
    return getSubjectAddParamTypeIdByCode(code, getCompanyTypeId())
  }

  /**
   * Search for company add params by different fields value
   * @see #getSubjectAddParamsBy(Map)
   */
  List getCompanyAddParamsBy(Map input) {
    if (input.containsKey('companyId')) {
      input.subjectId = input.companyId
      input.remove('companyId')
    }
    return getSubjectAddParamsBy(input)
  }

  /**
   * Search for company one add param by different fields value
   * @see #getSubjectAddParamBy(Map)
   */
  Map getCompanyAddParamBy(Map input) {
    if (input.containsKey('companyId')) {
      input.subjectId = input.companyId
      input.remove('companyId')
    }
    return getSubjectAddParamBy(input)
  }

  /**
   * Add company add param value
   * @see #addSubjectAddParam(Map)
   */
  Map addCompanyAddParam(Map input = [:], def companyId) {
    return addSubjectAddParam(input, companyId)
  }

  /**
   * Add tag to company
   * @see #addSubjectTag(Map)
   */
  Map addCompanyTag(Map input) {
    input.subjectId = input.subjectId ?: input.companyId
    input.remove('companyId')
    return addSubjectTag(input)
  }

  /**
   * Add tag to company
   * @see #addSubjectTag(def,CharSequence)
   */
  Map addCompanyTag(def companyId, CharSequence tag) {
    return addCompanyTag(companyId: companyId, tag: tag)
  }

  /**
   * Add tag to company
   * @see #addSubjectTag(Map,def)
   */
  Map addCompanyTag(Map input = [:], def companyId) {
    return addCompanyTag(input + [companyId: companyId])
  }

  /**
   * Delete tag from company
   * @see #deleteCompanyTag(def)
   */
  Boolean deleteCompanyTag(def companyTagId) {
    return deleteSubjectTag(companyTagId)
  }

  /**
   * Delete tag from company
   * @see #deleteCompanyTag(Map)
   */
  Boolean deleteCompanyTag(Map input) {
    input.subjectId = input.subjectId ?: input.companyId
    input.remove('companyId')
    return deleteSubjectTag(input)
  }

  /**
   * Delete tag from company
   * @see #deleteCompanyTag(def,CharSequence)
   */
  Boolean deleteCompanyTag(def companyId, CharSequence tag) {
    return deleteCompanyTag(companyId: companyId, tag: tag)
  }

  /**
   * Refresh companies quick search material view
   * @see #refreshSubjects(CharSequence)
   */
  Boolean refreshCompanies(CharSequence method = 'C') {
    return refreshSubjects(method)
  }
}