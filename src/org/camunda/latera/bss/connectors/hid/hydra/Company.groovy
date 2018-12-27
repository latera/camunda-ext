package org.camunda.latera.bss.connectors.hid.hydra

trait Company {
  Boolean isCompany(String subjectType) {
    return subjectType == 'SUBJ_TYPE_Company'
  }

  Boolean isCompany(def subjectTypeId) {
    return subjectTypeId == this.getRefCodeById('SUBJ_TYPE_Company')
  }
  
  LinkedHashMap getCompany(subjectId) {
    LinkedHashMap where = [
      n_subject_id: subjectId
    ]
    return this.hid.getTableFirst('SI_V_COMPANIES', where: where)
  }

  LinkedHashMap putCompany(LinkedHashMap input) {
    LinkedHashMap params = this.mergeParams([
      id      :  null,
      name    :  null,
      code    :  null,
      opfId   :  null,
      inn     :  null,
      kpp     :  null,
      rem     :  null,
      groupId :  null,
      firmId  :  100,
      stateId :  this.getRefIdByCode('SUBJ_STATE_On')
    ], input)
    try {
      this.logger.log("Putting company named ${params.name} to firm ${params.firmId}")
      LinkedHashMap companyId = this.hid.execute('SI_COMPANIES_PKG.SI_COMPANIES_PUT',[
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
      this.logger.log("   Company ${companyId} was put successfully!")
      return companyId
    } catch (Exception e){
      this.logger.log("Error while creating company!")
      this.logger.log(e)
      return null
    }
  }
}