package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Oracle.encodeBool
import static org.camunda.latera.bss.utils.Oracle.encodeFlag
import static org.camunda.latera.bss.utils.StringUtil.isEmpty
import static org.camunda.latera.bss.utils.DateTimeUtil.local
import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import java.time.temporal.Temporal

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

  String getCustomersTable() {
    return CUSTOMERS_TABLE
  }

  String getCustomerNetServicesTable() {
    return CUSTOMER_NET_SERVICES_TABLE
  }

  String getCustomerType() {
    return CUSTOMER_TYPE
  }

  Number getCustomerTypeId() {
    return getRefIdByCode(getCustomerType())
  }

  String getNetServiceType() {
    return NET_SERVICE_TYPE
  }

  Number getNetServiceTypeId() {
    return getRefIdByCode(getNetServiceType())
  }

  String getApplicationType() {
    return APPLICATION_TYPE
  }

  Number getApplicationTypeId() {
    return getRefIdByCode(getApplicationType())
  }

  String getAuthLoginPassType() {
    return AUTH_LOGIN_PASS_TYPE
  }

  Number getAuthLoginPassTypeId() {
    return getRefIdByCode(getAuthLoginPassType())
  }

  String getPassHashMD5Type() {
    return PASS_HASH_MD5_TYPE
  }

  Number getPassHashMD5TypeId() {
    return getRefIdByCode(getPassHashMD5Type())
  }

  String getPassHashSHA1Type() {
    return PASS_HASH_SHA1_TYPE
  }

  Number getPassHashSHA1TypeId() {
    return getRefIdByCode(getPassHashSHA1Type())
  }

  String getPassHashSSHA1Type() {
    return PASS_HASH_SSHA1_TYPE
  }

  Number getPassHashSSHA1TypeId() {
    return getRefIdByCode(getPassHashSSHA1Type())
  }

  String getPassHashCryptType() {
    return PASS_HASH_CRYPT_TYPE
  }

  Number getPassHashCryptTypeId() {
    return getRefIdByCode(getPassHashCryptType())
  }

  String getPassHashMD5SaltyType() {
    return PASS_HASH_MD5_SALTY_TYPE
  }

  Number getPassHashMD5SaltyTypeId() {
    return getRefIdByCode(getPassHashMD5SaltyType())
  }

  String getPassHashSHA1SaltyType() {
    return PASS_HASH_SHA1_SALTY_TYPE
  }

  Number getPassHashSHA1SaltyTypeId() {
    return getRefIdByCode(getPassHashSHA1SaltyType())
  }

  String getSelfCareApplication() {
    return SELF_CARE_APPLICATION
  }

  Number getSelfCareApplicationId() {
    return getRefIdByCode(getSelfCareApplication())
  }

  String getISPApplication() {
    return ISP_APPLICATION
  }

  Number getISPApplicationId() {
    return getRefIdByCode(getISPApplication())
  }

  String getHIDApplication() {
    return HID_APPLICATION
  }

  Number getHIDApplicationId() {
    return getRefIdByCode(getHIDApplication())
  }

  Map getCustomer(def customerId) {
    LinkedHashMap where = [
      n_subject_id: customerId
    ]
    return hid.getTableFirst(getCustomersTable(), where: where)
  }

  List getCustomersBy(Map input) {
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
      tags          : null,
      limit         : 0
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
    return hid.getTableData(getCustomersTable(), where: where, order: params.order, limit: params.limit)
  }

  Map getCustomerBy(Map input) {
    return getCustomersBy(input + [limit: 1])?.getAt(0)
  }

  Map getCustomerByCode(CharSequence code) {
    return getCustomerBy(code: code)
  }

  Map getCustomerByName(CharSequence name) {
    return getCustomerBy(name: name)
  }

  Number getCustomerIdByCode(CharSequence code) {
    return toIntSafe(getCustomerByCode(code)?.n_subject_id)
  }

  Number getCustomerIdByName(CharSequence name) {
    return toIntSafe(getCustomerByName(name)?.n_subject_id)
  }

  Boolean isCustomer(CharSequence entityOrEntityType) {
    return entityType == getCustomerType() || getCustomerByCode(entityOrEntityType) != null || getCustomerByName(entityOrEntityType) != null
  }

  Boolean isCustomer(def entityIdOrEntityTypeId) {
    return entityIdOrEntityTypeId == getCustomerTypeId() || getCustomer(entityIdOrEntityTypeId) != null
  }

  Map putCustomer(Map input) {
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

  Map createCustomer(Map input) {
    input.remove('customerId')
    return putCustomer(input)
  }

  Map updateCustomer(Map input) {
    return putCustomer(input)
  }

  Map updateCustomer(def customerId, Map input) {
    return putCustomer(input + [customerId: customerId])
  }

  Map updateCustomer(Map input, def customerId) {
    return updateCustomer(customerId, input)
  }

  Number getCustomerAddParamTypeIdByCode(CharSequence code) {
    return getSubjectAddParamTypeIdByCode(code, getCustomerTypeId())
  }

  List getCustomerAddParamsBy(Map input) {
    if (input.containsKey('customerId')) {
      input.subjectId = input.customerId
      input.remove('customerId')
    }
    return getSubjectAddParamsBy(input)
  }

  Map getCustomerAddParamBy(Map input) {
    if (input.containsKey('customerId')) {
      input.subjectId = input.customerId
      input.remove('customerId')
    }
    return getSubjectAddParamBy(input)
  }

  Map putCustomerAddParam(Map input) {
    if (input.containsKey('customerId')) {
      input.subjectId = input.customerId
      input.remove('customerId')
    }
    return putSubjectAddParam(input)
  }

  Map addCustomerAddParam(Map input) {
    return putCustomerAddParam(input)
  }

  Map addCustomerAddParam(def customerId, Map input) {
    return putCustomerAddParam(input + [customerId: customerId])
  }

  Map addCustomerAddParam(Map input, def customerId) {
    return addCustomerAddParam(customerId, input)
  }

  Boolean enableCustomer(def customerId) {
    enableSubject(customerId)
  }

  Boolean suspendCustomer(def customerId) {
    suspendSubject(customerId)
  }

  Boolean disableCustomer(def customerId) {
    disableSubject(customerId)
  }

  List getCustomerGroupsBy(Map input) {
    if (input.containsKey('customerId')) {
      input.subjectId = input.customerId
      input.remove('customerId')
    }
    return getSubjectGroupsBy(input)
  }

  Map getCustomerGroupBy(Map input) {
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

  Map putCustomerGroup(Map input) {
    if (input.containsKey('customerId')) {
      input.subjectId = input.customerId
      input.remove('customerId')
    }
    return putSubjectGroup(input)
  }

  Map addCustomerGroup(Map input) {
    return putCustomerGroup(input)
  }

  Map addCustomerGroup(def customerId, Map input) {
    return putCustomerGroup(input + [customerId: customerId])
  }

  Map addCustomerGroup(Map input, def customerId) {
    return addCustomerGroup(customerId, input)
  }

  Boolean deleteCustomerGroup(Map input) {
    if (input.containsKey('customerId')) {
      input.subjectId = input.customerId
      input.remove('customerId')
    }
    return deleteSubjectGroup(input)
  }

  Boolean deleteCustomerGroup(def customerId) {
    return deleteSubjectGroup(customerId)
  }

  List getCustomerNetServicesAccessBy(Map input) {
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
      hashTypeId     : null,
      limit          : 0
    ], input)
    LinkedHashMap where = [
      c_active: encodeBool(true)
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
    return hid.getTableData(getCustomerNetServicesTable(), where: where, order: params.order, limit: params.limit)
  }

  Map getCustomerNetServiceAccessBy(Map input) {
    return getCustomerNetServicesAccessBy(input + [limit: 1])?.getAt(0)
  }

  Map putCustomerNetServiceAccess(Map input) {
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

  Map addCustomerNetServiceAccess(Map input) {
    return putCustomerNetServiceAccess(input)
  }

  Map addCustomerNetServiceAccess(def customerId, Map input) {
    return putCustomerNetServiceAccess(input + [customerId: customerId])
  }

  Map addCustomerNetServiceAccess(Map input, def customerId) {
    return addCustomerNetServiceAccess(customerId, input)
  }

  Boolean changeNetServicePassword(Map input) {
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
        LinkedHashMap serv = getCustomerNetServicesAccessBy(input)
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
        b_NoPassCheck        : (isEmpty(params.oldPassword) || isEmpty(params.oldPasswordHash))
      ])
      logger.info("   Password for net service/app was changed successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while changing net service/app password!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean deleteCustomerNetServiceAccess(def subjServId) {
    try {
      logger.info("Deleting customer net service subscription id ${subjServId}")
      hid.execute('SI_SUBJECTS_PKG.SI_SUBJ_SERVICES_DEL', [
        num_N_SUBJ_SERV_ID : subjServId
      ])
      logger.info("   Net service subscription was deleted successfully!")
      return true
    } catch (Exception e){
      logger.error("  Error while deleting net service subscription!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean deleteCustomerNetServiceAccess(Map input) {
    def subjServId = getCustomerNetServiceAccessBy(input)?.n_subj_serv_id
    return deleteCustomerNetServiceAccess(subjServId)
  }

  List getCustomerAppsAccessBy(Map input) {
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
    return getCustomerNetServicesAccessBy(params + [netServiceId: params.applicationId ?: params.appId])
  }

  Map getCustomerAppAccessBy(Map input) {
    return getCustomerAppsAccessBy(input + [limit: 1])?.getAt(0)
  }

  Map putCustomerAppAccess(Map input) {
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

  Map addCustomerAppAccess(Map input) {
    return putCustomerAppAccess(input)
  }

  Map addCustomerAppAccess(def customerId, Map input) {
    return putCustomerAppAccess(input + [customerId: customerId])
  }

  Map addCustomerAppAccess(Map input, def customerId) {
    return addCustomerAppAccess(customerId, input)
  }

  Boolean changeAppPassword(Map input) {
    return changeNetServicePassword(input)
  }

  Boolean deleteCustomerAppAccess(def subjServId) {
    return deleteCustomerNetServiceAccess(subjServId)
  }

  Boolean deleteCustomerAppAccess(Map input) {
    def subjServId = getCustomerAppAccessBy(input)?.n_subj_serv_id
    return deleteCustomerAppAccess(subjServId)
  }

  Map getCustomerSelfCareAccessBy(Map input) {
    input.appId = getSelfCareAppId()
    return getCustomerAppAccessBy(input)
  }

  Map putCustomerSelfCareAccess(Map input) {
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
        b_ForceChangePass : encodeFlag(params.force),
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

  Map addCustomerSelfCareAccess(Map input) {
    return putCustomerSelfCareAccess(input)
  }

  Map addCustomerSelfCareAccess(def customerId, Map input) {
    return putCustomerSelfCareAccess(input + [customerId: customerId])
  }

  Map addCustomerSelfCareAccess(Map input, def customerId) {
    return addCustomerSelfCareAccess(customerId, input)
  }

  Boolean changeSelfCarePassword(Map input) {
    return changeAppPassword(input + [appId: getSelfCareApplicationId()])
  }

  Boolean deleteCustomerSelfCareAccess(def customerId) {
    def subjServId = getCustomerAppAccessBy(customerId: customerId, appId: getSelfCareApplicationId())?.n_subj_serv_id
    return deleteCustomerAppAccess(subjServId)
  }
  
  Map addCustomerTag(Map input) {
    input.subjectId = input.subjectId ?: input.customerId
    input.remove('customerId')
    return addSubjectTag(input)
  }

  Map addCustomerTag(def customerId, CharSequence tag) {
    return addCustomerTag(customerId: customerId, tag: tag)
  }

  Map addCustomerTag(Map input = [:], def customerId) {
    return addCustomerTag(input + [customerId: customerId])
  }

  Boolean deleteCustomerTag(def customerTagId) {
    return deleteSubjectTag(customerTagId)
  }

  Boolean deleteCustomerTag(Map input) {
    input.subjectId = input.subjectId ?: input.customerId
    input.remove('customerId')
    return deleteSubjectTag(input)
  }

  Boolean deleteCustomerTag(def customerId, CharSequence tag) {
    return deleteCustomerTag(customerId: customerId, tag: tag)
  }

  Boolean processCustomer(
    def customerId,
    Temporal beginDate = local(),
    Temporal endDate   = null
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

  Boolean processCustomer(Map input) {
    LinkedHashMap params = mergeParams([
      customerId : null,
      beginDate  : local(),
      endDate    : null
    ], input)
    return processCustomer(params.customerId, params.beginDate, params.endDate)
  }

  Boolean refreshCustomers(CharSequence method = 'C') {
    return refreshSubjects(method)
  }
}