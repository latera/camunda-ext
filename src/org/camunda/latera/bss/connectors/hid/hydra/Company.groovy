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
  
  LinkedHashMap getCompany(subjectId) {
    LinkedHashMap where = [
      n_subject_id: subjectId
    ]
    return hid.getTableFirst(getCompaniesTable(), where: where)
  }

  Boolean isCompany(String subjectType) {
    return subjectType == getCompanyType()
  }

  Boolean isCompany(def subjectIdOrSubjectTypeId) {
    return subjectIdOrSubjectTypeId == getCompanyTypeId() || getCompany(subjectIdOrSubjectTypeId) != null
  }

  LinkedHashMap putCompany(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      subjectId :  null,
      name      :  null,
      code      :  null,
      opfId     :  null,
      inn       :  null,
      kpp       :  null,
      rem       :  null,
      groupId   :  null,
      firmId    :  getFirmId(),
      stateId   :  getSubjectStateOnId()
    ], input)
    try {
      logger.info("Putting company named ${params.name} to firm ${params.firmId}")
      LinkedHashMap company = hid.execute('SI_COMPANIES_PKG.SI_COMPANIES_PUT',[
        num_N_SUBJECT_ID    : params.subjectId,
        num_N_FIRM_ID       : params.firmId,
        num_N_SUBJ_STATE_ID : params.stateId,
        num_N_SUBJ_GROUP_ID : params.groupId,
        vch_VC_CODE         : params.code,
        vch_VC_NAME         : params.name,
        num_N_OPF_ID        : params.opfId,
        vch_VC_INN          : params.inn,
        vch_VC_KPP          : params.kpp,
        vch_VC_REM          : params.rem
      ])
      logger.info("   Company ${company.num_N_SUBJECT_ID} was put successfully!")
      return company
    } catch (Exception e){
      logger.error("Error while putting company!")
      logger.error(e)
      return null
    }
  }
}