package org.camunda.latera.bss.connectors.hid.hydra

trait Company {
  private static String COMPANIES_TABLE = 'SI_V_COMPANIES'
  private static String COMPANY_TYPE    = 'SUBJ_TYPE_Company'

  def getCompanyType() {
    return COMPANY_TYPE
  }

  def getCompanyTypeId() {
    return getRefIdByCode(getCompanyType())
  }

  def getCompaniesTable() {
    return COMPANIES_TABLE
  }
  
  LinkedHashMap getCompany(def companyId) {
    LinkedHashMap where = [
      n_subject_id: companyId
    ]
    return hid.getTableFirst(getCompaniesTable(), where: where)
  }

  List getCompaniesBy(LinkedHashMap input) {
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
      stateId   : getSubjectStateOnId()
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
    return hid.getTableData(getCompaniesTable(), where: where)
  }

  LinkedHashMap getCompanyBy(LinkedHashMap input) {
    return getCompaniesBy(input)?.getAt(0)
  }

  Boolean isCompany(String entityType) {
    return entityType == getCompanyType()
  }

  Boolean isCompany(def entityIdOrEntityTypeId) {
    return entityIdOrEntityTypeId == getCompanyTypeId() || getCompany(entityIdOrEntityTypeId) != null
  }

  LinkedHashMap putCompany(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
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
    ], input)
    try {
      logger.info("Putting company named ${params.name} to firm ${params.firmId}")
      LinkedHashMap company = hid.execute('SI_COMPANIES_PKG.SI_COMPANIES_PUT',[
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
      logger.info("   Company ${company.num_N_SUBJECT_ID} was put successfully!")
      return company
    } catch (Exception e){
      logger.error("   Error while putting company!")
      logger.error_oracle(e)
      return null
    }
  }

  def getCompanyAddParamTypeIdByCode(String code) {
    return getSubjectAddParamTypeIdByCode(code, getCompanyTypeId())
  }

  List getCompanyAddParamsBy(LinkedHashMap input) {
    if (input.containsKey('companyId')) {
      input.subjectId = input.companyId
      input.remove('companyId')
    }
    return getSubjectAddParamsBy(input)
  }

  LinkedHashMap getCompanyAddParamBy(LinkedHashMap input) {
    if (input.containsKey('companyId')) {
      input.subjectId = input.companyId
      input.remove('companyId')
    }
    return getSubjectAddParamBy(input)
  }

  Boolean putCompanyAddParam(LinkedHashMap input) {
    if (input.containsKey('companyId')) {
      input.subjectId = input.companyId
      input.remove('companyId')
    }
    return putSubjectAddParam(input)
  }
}