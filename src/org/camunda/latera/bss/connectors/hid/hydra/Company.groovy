package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.StringUtil.isEmpty
import static org.camunda.latera.bss.utils.StringUtil.notEmpty
import static org.camunda.latera.bss.utils.Constants.SUBJ_TYPE_Company

trait Company {
  private static String COMPANIES_TABLE = 'SI_V_COMPANIES'

  String getCompaniesTable() {
    return COMPANIES_TABLE
  }

  String getCompanyType() {
    return getRefCode(getCompanyTypeId())
  }

  Number getCompanyTypeId() {
    return SUBJ_TYPE_Company
  }

  Map getCompany(def companyId) {
    LinkedHashMap where = [
      n_subject_id: companyId
    ]
    return hid.getTableFirst(getCompaniesTable(), where: where)
  }

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

  Map getCompanyBy(Map input) {
    return getCompaniesBy(input + [limit: 1])?.getAt(0)
  }

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

  Map createCompany(Map input) {
    input.remove('companyId')
    return putCompany(input)
  }

  Map updateCompany(Map input = [:], def companyId) {
    return putCompany(input + [companyId: companyId])
  }

  Number getCompanyAddParamTypeIdByCode(CharSequence code) {
    return getSubjectAddParamTypeIdByCode(code, getCompanyTypeId())
  }

  List getCompanyAddParamsBy(Map input) {
    if (input.containsKey('companyId')) {
      input.subjectId = input.companyId
      input.remove('companyId')
    }
    return getSubjectAddParamsBy(input)
  }

  Map getCompanyAddParamBy(Map input) {
    if (input.containsKey('companyId')) {
      input.subjectId = input.companyId
      input.remove('companyId')
    }
    return getSubjectAddParamBy(input)
  }

  Map addCompanyAddParam(Map input = [:], def companyId) {
    return addSubjectAddParam(input, companyId)
  }

  Map addCompanyTag(Map input) {
    input.subjectId = input.subjectId ?: input.companyId
    input.remove('companyId')
    return addSubjectTag(input)
  }

  Map addCompanyTag(def companyId, CharSequence tag) {
    return addCompanyTag(companyId: companyId, tag: tag)
  }

  Map addCompanyTag(Map input = [:], def companyId) {
    return addCompanyTag(input + [companyId: companyId])
  }

  Boolean deleteCompanyTag(def companyTagId) {
    return deleteSubjectTag(companyTagId)
  }

  Boolean deleteCompanyTag(Map input) {
    input.subjectId = input.subjectId ?: input.companyId
    input.remove('companyId')
    return deleteSubjectTag(input)
  }

  Boolean deleteCompanyTag(def companyId, CharSequence tag) {
    return deleteCompanyTag(companyId: companyId, tag: tag)
  }

  Boolean refreshCompanies(CharSequence method = 'C') {
    return refreshSubjects(method)
  }
}