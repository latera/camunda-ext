package org.camunda.latera.bss.connectors.hid.hydra

trait Company {
  private static String COMPANIES_TABLE = 'SI_V_COMPANIES'
  private static String COMPANY_TYPE    = 'SUBJ_TYPE_Company'

  Boolean isCompany(String subjectType) {
    return subjectType == COMPANY_TYPE
  }

  Boolean isCompany(def subjectTypeId) {
    return subjectTypeId == getRefCodeById(COMPANY_TYPE)
  }
  
  LinkedHashMap getCompany(subjectId) {
    LinkedHashMap where = [
      n_subject_id: subjectId
    ]
    return hid.getTableFirst(COMPANIES_TABLE, where: where)
  }

  LinkedHashMap putCompany(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      id      :  null,
      name    :  null,
      code    :  null,
      opfId   :  null,
      inn     :  null,
      kpp     :  null,
      rem     :  null,
      groupId :  null,
      firmId  :  DEFAULT_FIRM,
      stateId :  getRefIdByCode(DEFAULT_SUBJECT_STATE)
    ], input)
    try {
      logger.info("Putting company named ${params.name} to firm ${params.firmId}")
      LinkedHashMap companyId = hid.execute('SI_COMPANIES_PKG.SI_COMPANIES_PUT',[
        num_N_SUBJECT_ID    : params.id,
        num_N_FIRM_ID       : params.firmId,
        num_N_SUBJ_STATE_ID : params.stateId,
        num_N_SUBJ_GROUP_ID : params.groupId,
        vch_VC_CODE         : params.code,
        vch_VC_NAME         : params.name,
        num_N_OPF_ID        : params.opfId,
        vch_VC_INN          : params.inn,
        vch_VC_KPP          : params.kpp,
        vch_VC_REM          : params.rem
      ]).num_N_SUBJECT_ID
      logger.info("   Company ${companyId} was put successfully!")
      return companyId
    } catch (Exception e){
      logger.error("Error while creating company!")
      logger.error(e)
      return null
    }
  }
}