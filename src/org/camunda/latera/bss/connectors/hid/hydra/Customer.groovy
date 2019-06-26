package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.utils.Oracle
import org.camunda.latera.bss.utils.StringUtil
import org.camunda.latera.bss.utils.DateTimeUtil

trait Customer {
  private static String CUSTOMERS_TABLE             = 'SI_V_USERS'
  private static String CUSTOMER_NET_SERVICES_TABLE = 'SI_SUBJ_SERVICES'
  private static String CUSTOMER_TYPE               = 'SUBJ_TYPE_User'
  private static String NET_SERVICE_TYPE            = 'SUBJ_SERV_ServiceUse'
  private static String APPLICATION_TYPE            = 'SUBJ_SERV_AppAccess'
  private static String AUTH_LOGIN_PASS_TYPE        = 'AUTH_TYPE_LoginPass'
  private static String PASS_HASH_MD5_TYPE          = 'PASS_HASH_TYPE_MD5'
  private static String PASS_HASH_SHA1_TYPE         = 'PASS_HASH_TYPE_SHA1'
  private static String PASS_HASH_SSHA1_TYPE        = 'PASS_HASH_TYPE_SSHA1'
  private static String PASS_HASH_CRYPT_TYPE        = 'PASS_HASH_TYPE_Crypt'
  private static String PASS_HASH_MD5_SALTY_TYPE    = 'PASS_HASH_TYPE_MD5_salty'
  private static String PASS_HASH_SHA1_SALTY_TYPE   = 'PASS_HASH_TYPE_SHA1_salty'
  private static String SELF_CARE_APPLICATION       = 'NETSERV_ARM_Private_Office'
  private static String ISP_APPLICATION             = 'NETSERV_ARM_ISP'
  private static String HID_APPLICATION             = 'NETSERV_HID'

  def getCustomersTable() {
    return CUSTOMERS_TABLE
  }

  def getCustomerType() {
    return CUSTOMER_TYPE
  }

  def getCustomerNetServicesTable() {
    return CUSTOMER_NET_SERVICES_TABLE
  }

  def getCustomerTypeId() {
    return getRefIdByCode(getCustomerType())
  }

  def getNetServiceType() {
    return NET_SERVICE_TYPE
  }

  def getNetServiceTypeId() {
    return getRefIdByCode(getNetServiceType())
  }

  def getApplicationType() {
    return APPLICATION_TYPE
  }

  def getApplicationTypeId() {
    return getRefIdByCode(getApplicationType())
  }

  def getAuthLoginPassType() {
    return AUTH_LOGIN_PASS_TYPE
  }

  def getAuthLoginPassTypeId() {
    return getRefIdByCode(getAuthLoginPassType())
  }

  def getPassHashMD5Type() {
    return PASS_HASH_MD5_TYPE
  }

  def getPassHashMD5TypeId() {
    return getRefIdByCode(getPassHashMD5Type())
  }

  def getPassHashSHA1Type() {
    return PASS_HASH_SHA1_TYPE
  }

  def getPassHashSHA1TypeId() {
    return getRefIdByCode(getPassHashSHA1Type())
  }

  def getPassHashSSHA1Type() {
    return PASS_HASH_SSHA1_TYPE
  }

  def getPassHashSSHA1TypeId() {
    return getRefIdByCode(getPassHashSSHA1Type())
  }

  def getPassHashCryptType() {
    return PASS_HASH_CRYPT_TYPE
  }

  def getPassHashCryptTypeId() {
    return getRefIdByCode(getPassHashCryptType())
  }

  def getPassHashMD5SaltyType() {
    return PASS_HASH_MD5_SALTY_TYPE
  }

  def getPassHashMD5SaltyTypeId() {
    return getRefIdByCode(getPassHashMD5SaltyType())
  }

  def getPassHashSHA1SaltyType() {
    return PASS_HASH_SHA1_SALTY_TYPE
  }

  def getPassHashSHA1SaltyTypeId() {
    return getRefIdByCode(getPassHashSHA1SaltyType())
  }

  def getSelfCareApplication() {
    return SELF_CARE_APPLICATION
  }

  def getSelfCareApplicationId() {
    return getRefIdByCode(getSelfCareApplication())
  }

  def getISPApplication() {
    return ISP_APPLICATION
  }

  def getISPApplicationId() {
    return getRefIdByCode(getISPApplication())
  }

  def getHIDApplication() {
    return HID_APPLICATION
  }

  def getHIDApplicationId() {
    return getRefIdByCode(getHIDApplication())
  }

  LinkedHashMap getCustomer(def customerId) {
    LinkedHashMap where = [
      n_subject_id: customerId
    ]
    return hid.getTableFirst(getCustomersTable(), where: where)
  }

  List getCustomersBy(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      customerId    : null,
      baseSubjectId : null,
      creatorId     : null,
      name          : null,
      code          : null,
      groupId       : null,
      firmId        : getFirmId(),
      resellerId    : getResellerId(),
      stateId       : getSubjectStateOnId(),
      tags          : null
    ], input)
    LinkedHashMap where = [:]

    if (params.customerId) {
      where.n_customer_id = params.customerId
    }
    if (params.baseSubjectId) {
      where.n_base_subject_id = params.baseSubjectId
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
    if (params.groupId) {
      where.n_subj_group_id = params.groupId
    }
    if (params.firmId) {
      where.n_firm_id = params.firmId
    }
    if (params.resellerId) {
      where.n_reseller_id = params.resellerId
    }
    if (params.stateId) {
      where.n_subj_state_id = params.stateId
    }
    if (params.tags) {
      where.t_tags = params.tags
    }
    return hid.getTableData(getCustomersTable(), where: where)
  }

  LinkedHashMap getCustomerBy(LinkedHashMap input) {
    return getCustomersBy(input)?.getAt(0)
  }

  Boolean isCustomer(String entityType) {
    return entityType == getCustomerType()
  }

  Boolean isCustomer(def entityIdOrEntityTypeId) {
    return entityIdOrEntityTypeId == getCustomerTypeId() || getCustomer(entityIdOrEntityTypeId) != null
  }

  LinkedHashMap putCustomer(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      customerId    : null,
      baseSubjectId : null,
      groupId       : null,
      code          : null,
      rem           : null,
      firmId        : getFirmId(),
      resellerId    : getResellerId(),
      stateId       : getSubjectStateOnId()
    ], input)
    try {
      logger.info("Putting customer with params ${params}")
      LinkedHashMap args = [
        num_N_SUBJECT_ID      : params.subjectId,
        num_N_FIRM_ID         : params.firmId,
        num_N_BASE_SUBJECT_ID : params.baseSubjectId,
        num_N_SUBJ_STATE_ID   : params.stateId,
        num_N_SUBJ_GROUP_ID   : params.groupId,
        vch_VC_CODE           : params.code,
        vch_VC_REM            : params.rem
      ]
      if (params.resellerId) {
        args.num_N_RESELLER_ID = resellerId
      }
      LinkedHashMap customer = hid.execute('SI_USERS_PKG.SI_USERS_PUT', args)
      logger.info("   Customer ${customer.num_N_SUBJECT_ID} was put successfully!")
      return customer
    } catch (Exception e){
      logger.error("   Error while putting customer!")
      logger.error_oracle(e)
      return null
    }
  }

  LinkedHashMap createCustomer(LinkedHashMap input) {
    input.remove('customerId')
    return putCustomer(input)
  }

  LinkedHashMap updateCustomer(def customerId, LinkedHashMap input) {
    return putCustomer(input + [customerId: customerId])
  }

  def getCustomerAddParamTypeIdByCode(String code) {
    return getSubjectAddParamTypeIdByCode(code, getCustomerTypeId())
  }

  List getCustomerAddParamsBy(LinkedHashMap input) {
    if (input.containsKey('customerId')) {
      input.subjectId = input.customerId
      input.remove('customerId')
    }
    return getSubjectAddParamsBy(input)
  }

  LinkedHashMap getCustomerAddParamBy(LinkedHashMap input) {
    if (input.containsKey('customerId')) {
      input.subjectId = input.customerId
      input.remove('customerId')
    }
    return getSubjectAddParamBy(input)
  }

  Boolean putCustomerAddParam(LinkedHashMap input) {
    if (input.containsKey('customerId')) {
      input.subjectId = input.customerId
      input.remove('customerId')
    }
    return putSubjectAddParam(input)
  }

  Boolean addCustomerAddParam(LinkedHashMap input) {
    return putCustomerAddParam(input)
  }

  Boolean addCustomerAddParam(def customerId, LinkedHashMap input) {
    return putCustomerAddParam(input + [customerId: customerId])
  }

  Boolean enableCustomer(def customerId) {
    enableSubject(subjectId)
  }

  Boolean suspendCustomer(def customerId) {
    suspendSubject(customerId)
  }

  Boolean disableCustomer(def customerId) {
    disableSubject(customerId)
  }

  List getCustomerGroupsBy(LinkedHashMap input) {
    if (input.containsKey('customerId')) {
      input.subjectId = input.customerId
      input.remove('customerId')
    }
    return getSubjectGroupsBy(input)
  }

  LinkedHashMap getCustomerGroupBy(LinkedHashMap input) {
    if (input.containsKey('customerId')) {
      input.subjectId = input.customerId
      input.remove('customerId')
    }
    return getSubjectGroupBy(input)
  }

  List getCustomerGroups(def customerId) {
    return getSubjectGroups(customerId)
  }

  List getCustomerGroup(def customerId) {
    return getSubjectGroup(customerId)
  }

  LinkedHashMap putCustomerGroup(LinkedHashMap input) {
    if (input.containsKey('customerId')) {
      input.subjectId = input.customerId
      input.remove('customerId')
    }
    return putSubjectGroup(input)
  }

  LinkedHashMap addCustomerGroup(LinkedHashMap input) {
    return putCustomerGroup(input)
  }

  LinkedHashMap addCustomerGroup(def customerId, LinkedHashMap input) {
    return putCustomerGroup(input + [customerId: customerId])
  }

  Boolean deleteCustomerGroup(LinkedHashMap input) {
    if (input.containsKey('customerId')) {
      input.subjectId = input.customerId
      input.remove('customerId')
    }
    return deleteSubjectGroup(input)
  }

  Boolean deleteCustomerGroup(def customerId) {
    return deleteSubjectGroup(customerId)
  }

  List getCustomerNetServicesAccessBy(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      subjServId     : null,
      customerId     : null,
      netServiceId   : null,
      objectId       : null,
      subjServTypeId : getNetServiceTypeId(),
      authTypeId     : getAuthLoginPassTypeId(),
      login          : null,
      password       : null,
      passwordHash   : null,
      hashTypeId     : null
    ], input)
    LinkedHashMap where = [
      c_active: Oracle.encodeBool(true)
    ]

    if (params.subjServId) {
      where.n_subj_serv_id = params.subjServId
    }
    if (params.customerId || params.subjectId) {
      where.n_subject_id = params.customerId ?: params.subjectId
    }
    if (params.netServiceId) {
      where.n_service_id = params.netServiceId
    }
    if (params.objectId) {
      where.n_object_id = params.objectId
    }
    if (params.subjServTypeId) {
      where.n_subj_serv_type_id = params.subjServTypeId
    }
    if (params.authTypeId) {
      where.n_auth_type_id = params.authTypeId
    }
    if (params.login) {
      where.vc_login_real = params.login
    }
    if (params.password) {
      where.vc_pass = password
    }
    if (params.passwordHash) {
      where.vc_hash_pass = passwordHash
    }
    if (params.hashTypeId) {
      where.n_hash_type_id = hashTypeId
    }
    return hid.getTableData(getCustomerNetServicesTable(), where: where)
  }

  LinkedHashMap getCustomerNetServiceAccessBy(LinkedHashMap input) {
    return getCustomerNetServicesAccessBy(input)?.getAt(0)
  }

  LinkedHashMap putCustomerNetServiceAccess(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      subjServId     : null,
      customerId     : null,
      netServiceId   : null,
      objectId       : null,
      subjServTypeId : getNetServiceTypeId(),
      authTypeId     : getAuthLoginPassTypeId(),
      login          : null,
      password       : null
    ], input)
    try {
      logger.info("Putting net service access with params ${params}")
      LinkedHashMap access = hid.execute('SI_SUBJECTS_PKG.SI_SUBJ_SERVICES_PUT', [
        num_N_SUBJ_SERV_ID      : params.subjServId,
        num_N_SUBJECT_ID        : params.customerId,
        num_N_SERVICE_ID        : params.netServiceId,
        num_N_OBJECT_ID         : params.objectId,
        num_N_SUBJ_SERV_TYPE_ID : params.subjServTypeId,
        num_N_AUTH_TYPE_ID      : params.authTypeId,
        vch_VC_LOGIN            : params.login
      ])
      if (access) {
        Boolean passwordIsSet = changeNetServicePassword(subjServId: access.num_N_SUBJ_SERV_ID, newPassword: params.password)
        if (!passwordIsSet) {
          throw new Exception('Error while setting application password!')
        }
      }
      logger.info("   Customer now have access to net service!")
      return access + [vch_VC_PASS: params.password]
    } catch (Exception e){
      logger.error("   Error while providing access to net service!")
      logger.error_oracle(e)
      return null
    }
  }

  LinkedHashMap addCustomerNetServiceAccess(LinkedHashMap input) {
    return putCustomerNetServiceAccess(input)
  }

  LinkedHashMap addCustomerNetServiceAccess(def customerId, LinkedHashMap input) {
    return putCustomerNetServiceAccess(input + [customerId: customerId])
  }

  Boolean changeNetServicePassword(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      subjServId      : null,
      customerId      : null,
      netServiceId    : null,
      objectId        : null,
      subjServTypeId  : getNetServiceTypeId(),
      authTypeId      : getAuthLoginPassTypeId(),
      login           : null,
      oldPassword     : null,
      oldPasswordHash : null,
      newPassword     : null,
      hashTypeId      : null
    ], input)
    try {
      if (params.subjServId == null) {
        def serv = getCustomerNetServicesAccessBy(input)
        if (serv?.n_subj_serv_id) {
          params.subjServId = serv.n_subj_serv_id
        } else {
          logger.info("No net service/app subscription found!")
          return false
        }
      }
      logger.info("Changing net service/app id ${params.netServiceId} subscription id ${params.subjServId} password from ${params.oldPassword ?: params.oldPasswordHash} to ${params.newPassword}")
      hid.execute('SI_SUBJECTS_PKG.SI_SUBJ_SERVICES_CHG_PASS', [
        num_N_SUBJ_SERV_ID   : params.subjServId,
        vch_VC_OLD_PASS      : params.oldPassword,
        vch_VC_OLD_PASS_HASH : params.oldPasswordHash,
        vch_VC_NEW_PASS      : params.newPassword,
        num_N_HASH_TYPE_ID   : params.hashTypeId,
        b_NoPassCheck        : (StringUtil.isEmpty(params.oldPassword) || StringUtil.isEmpty(params.oldPasswordHash))
      ])
      logger.info("   Password for net service/app was changed successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while changing net service/app password!")
      logger.error_oracle(e)
      return false
    }
  }

  List getCustomerAppsAccessBy(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      subjServId     : null,
      customerId     : null,
      applicationId  : null,
      subjServTypeId : getApplicationTypeId(),
      authTypeId     : getAuthLoginPassTypeId(),
      login          : null,
      password       : null,
      passwordHash   : null,
      hashTypeId     : null
    ], input)
    return getCustomerNetServicesAccessBy(params + [netServiceId: params.appId])
  }

  LinkedHashMap getCustomerAppAccessBy(LinkedHashMap input) {
    return getCustomerAppsAccessBy(input)?.getAt(0)
  }

  LinkedHashMap putCustomerAppAccess(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      subjServId     : null,
      customerId     : null,
      applicationId  : null,
      subjServTypeId : getApplicationTypeId(),
      authTypeId     : getAuthLoginPassTypeId(),
      login          : null,
      password       : null
    ], input)
    try {
      logger.info("Putting application access with params ${params}")
      LinkedHashMap access = hid.execute('SI_SUBJECTS_PKG.SI_SUBJ_SERVICES_PUT', [
        num_N_SUBJ_SERV_ID      : params.subjServId,
        num_N_SUBJECT_ID        : params.customerId,
        num_N_SERVICE_ID        : params.applicationId,
        num_N_SUBJ_SERV_TYPE_ID : params.subjServTypeId,
        num_N_AUTH_TYPE_ID      : params.authTypeId,
        vch_VC_LOGIN            : params.login
      ])
      if (access) {
        Boolean passwordIsSet = changeAppPassword(subjServId: access.num_N_SUBJ_SERV_ID, newPassword: params.password)
        if (!passwordIsSet) {
          throw new Exception('Error while setting application password!')
        }
      }
      logger.info("   Customer now have access to application!")
      return access + [vch_VC_PASS: params.password]
    } catch (Exception e){
      logger.error("   Error while providing access to application!")
      logger.error_oracle(e)
      return null
    }
  }

  LinkedHashMap addCustomerAppAccess(LinkedHashMap input) {
    return putCustomerAppAccess(input)
  }

  LinkedHashMap addCustomerAppAccess(def customerId, LinkedHashMap input) {
    return putCustomerAppAccess(input + [customerId: customerId])
  }

  Boolean changeAppPassword(LinkedHashMap input) {
    return changeNetServicePassword(input)
  }

  LinkedHashMap getCustomerSelfCareAccessBy(LinkedHashMap input) {
    input.appId = getSelfCareAppId()
    return getCustomerAppAccessBy(input)
  }

  LinkedHashMap putCustomerSelfCareAccess(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      customerId : null,
      login      : null,
      password   : null,
      force      : false,
      firmId     : getFirmId()
    ], input)
    try {
      logger.info("Providing Self-Care portal access with params ${params}")
      LinkedHashMap access = hid.execute('SI_USERS_PKG.SET_PRIVATE_OFFICE_ACCESS', [
        num_N_USER_ID     : params.customerId,
        num_N_FIRM_ID     : params.firmId,
        b_ForceChangePass : Oracle.encodeFlag(params.force),
        vch_VC_LOGIN      : params.login,
        vch_VC_PASS       : params.password
      ])
      logger.info("   Customer now have access to Self-Care portal!")
      return access
    } catch (Exception e){
      logger.error("   Error while providing access to Self-Care portal!")
      logger.error_oracle(e)
      return null
    }
  }

  LinkedHashMap addCustomerSelfCareAccess(LinkedHashMap input) {
    return putCustomerSelfCareAccess(input)
  }

  LinkedHashMap addCustomerSelfCareAccess(def customerId, LinkedHashMap input) {
    return putCustomerSelfCareAccess(input + [customerId: customerId])
  }

  Boolean changeSelfCarePassword(LinkedHashMap input) {
    return changeNetServicePassword(input)
  }

  Boolean processCustomer(
    def customerId,
    def beginDate = DateTimeUtil.now(),
    def endDate   = null
  ) {
    try {
      logger.info("Processing customer id ${customerId}")
      hid.execute('SD_CHARGE_LOGS_CHARGING_PKG.PROCESS_SUBJECT', [
        num_N_SUBJECT_ID : customerId,
        dt_D_OPER        : beginDate,
        dt_D_OPER_END    : endDate
      ])
      logger.info("   Customer was processed successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while processing customer!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean processCustomer(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      customerId : null,
      beginDate  : DateTimeUtil.now(),
      endDate    : null
    ], input)
    return processCustomer(params.customerId, params.beginDate, params.endDate)
  }
}