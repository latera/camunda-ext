package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Oracle.encodeBool
import static org.camunda.latera.bss.utils.Oracle.encodeFlag
import static org.camunda.latera.bss.utils.StringUtil.isEmpty
import static org.camunda.latera.bss.utils.StringUtil.notEmpty
import static org.camunda.latera.bss.utils.DateTimeUtil.local
import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import static org.camunda.latera.bss.utils.Constants.SUBJ_TYPE_User
import static org.camunda.latera.bss.utils.Constants.SUBJ_SERV_ServiceUse
import static org.camunda.latera.bss.utils.Constants.SUBJ_SERV_AppAccess
import static org.camunda.latera.bss.utils.Constants.AUTH_TYPE_LoginPass
import static org.camunda.latera.bss.utils.Constants.PASS_HASH_TYPE_MD5
import static org.camunda.latera.bss.utils.Constants.PASS_HASH_TYPE_SHA1
import static org.camunda.latera.bss.utils.Constants.PASS_HASH_TYPE_SSHA1
import static org.camunda.latera.bss.utils.Constants.PASS_HASH_TYPE_Crypt
import static org.camunda.latera.bss.utils.Constants.PASS_HASH_TYPE_MD5_salty
import static org.camunda.latera.bss.utils.Constants.PASS_HASH_TYPE_SHA1_salty
import static org.camunda.latera.bss.utils.Constants.NETSERV_ARM_Private_Office
import static org.camunda.latera.bss.utils.Constants.NETSERV_ARM_ISP
import static org.camunda.latera.bss.utils.Constants.NETSERV_HID
import java.time.temporal.Temporal

trait Customer {
  private static String CUSTOMERS_TABLE             = 'SI_V_USERS'
  private static String CUSTOMER_NET_SERVICES_TABLE = 'SI_SUBJ_SERVICES'

  String getCustomersTable() {
    return CUSTOMERS_TABLE
  }

  String getCustomerNetServicesTable() {
    return CUSTOMER_NET_SERVICES_TABLE
  }

  String getCustomerType() {
    return getRefCode(getCustomerTypeId())
  }

  Number getCustomerTypeId() {
    return SUBJ_TYPE_User
  }

  String getNetServiceType() {
    return getRefCode(getNetServiceTypeId())
  }

  Number getNetServiceTypeId() {
    return SUBJ_SERV_ServiceUse
  }

  String getApplicationType() {
    return getRefCode(getApplicationTypeId())
  }

  Number getApplicationTypeId() {
    return SUBJ_SERV_AppAccess
  }

  String getAuthLoginPassType() {
    return getRefCode(getAuthLoginPassTypeId())
  }

  Number getAuthLoginPassTypeId() {
    return AUTH_TYPE_LoginPass
  }

  String getPassHashMD5Type() {
    return getRefCode(getPassHashMD5TypeId())
  }

  Number getPassHashMD5TypeId() {
    return PASS_HASH_TYPE_MD5
  }

  String getPassHashSHA1Type() {
    return getRefCode(getPassHashSHA1TypeId())
  }

  Number getPassHashSHA1TypeId() {
    return PASS_HASH_TYPE_SHA1
  }

  String getPassHashSSHA1Type() {
    return getRefCode(getPassHashSSHA1TypeId())
  }

  Number getPassHashSSHA1TypeId() {
    return PASS_HASH_TYPE_SSHA1
  }

  String getPassHashCryptType() {
    return getRefCode(getPassHashCryptTypeId())
  }

  Number getPassHashCryptTypeId() {
    return PASS_HASH_TYPE_Crypt
  }

  String getPassHashMD5SaltyType() {
    return getRefCode(getPassHashMD5SaltyTypeId())
  }

  Number getPassHashMD5SaltyTypeId() {
    return PASS_HASH_TYPE_MD5_salty
  }

  String getPassHashSHA1SaltyType() {
    return getRefCode(getPassHashSHA1SaltyTypeId())
  }

  Number getPassHashSHA1SaltyTypeId() {
    return PASS_HASH_TYPE_SHA1_salty
  }

  String getSelfCareApplication() {
    return getRefCode(getSelfCareApplicationId())
  }

  Number getSelfCareApplicationId() {
    return NETSERV_ARM_Private_Office
  }

  String getISPApplication() {
    return getRefCode(getISPApplicationId())
  }

  Number getISPApplicationId() {
    return NETSERV_ARM_ISP
  }

  String getHIDApplication() {
    return getRefCode(getHIDApplicationId())
  }

  Number getHIDApplicationId() {
    return NETSERV_HID
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
      where += prepareEntityTagQuery('N_CUSTOMER_ID', params.tags)
    }
    return hid.getTableData(getCustomersTable(), where: where, order: params.order, limit: params.limit)
  }

  Map getCustomerBy(Map input) {
    return getCustomersBy(input + [limit: 1])?.getAt(0)
  }

  Boolean isCustomer(def entityOrEntityType) {
    if (entityOrEntityType == null) {
      return false
    }

    Number entityIdOrEntityTypeId = toIntSafe(entityOrEntityType)
    if (entityIdOrEntityTypeId != null) {
      return entityIdOrEntityTypeId == getCustomerTypeId() || getCustomer(entityIdOrEntityTypeId) != null
    } else {
      return entityType == getCustomerType()
    }
  }

  private Map putCustomer(Map input) {
    LinkedHashMap defaultParams = [
      customerId    : null,
      baseSubjectId : null,
      groupId       : null,
      code          : null,
      rem           : null,
      firmId        : getFirmId(),
      resellerId    : getResellerId(),
      stateId       : getSubjectStateOnId()
    ]
    try {
      LinkedHashMap existingCustomer = [:]
      if (isEmpty(input.customerId) && notEmpty(input.subjectId)) {
        input.customerId = input.subjectId
      }
      if (notEmpty(input.customerId)) {
        LinkedHashMap customer = getCustomer(input.customerId)
        existingCustomer += [
          customerId    : customer.n_subject_id,
          baseSubjectId : customer.n_base_subject_id,
          groupId       : customer.n_subj_group_id,
          code          : customer.vc_cpde,
          rem           : customer.vc_rem,
          firmId        : customer.n_firm_id,
          resellerId    : customer.n_reseller_id,
          stateId       : customer.n_subj_state_id
        ]
      }
      LinkedHashMap params = mergeParams(defaultParams, existingCustomer + input)

      logger.info("${params.customerId ? 'Updating' : 'Creating'} customer with params ${params}")
      LinkedHashMap args = [
        num_N_SUBJECT_ID      : params.customerId,
        num_N_FIRM_ID         : params.firmId,
        num_N_BASE_SUBJECT_ID : params.baseSubjectId,
        num_N_SUBJ_STATE_ID   : params.stateId,
        num_N_SUBJ_GROUP_ID   : params.groupId,
        vch_VC_CODE           : params.code,
        vch_VC_REM            : params.rem,
        num_N_RESELLER_ID     : params.resellerId
      ]

      LinkedHashMap result = hid.execute('SI_USERS_PKG.SI_USERS_PUT', args)
      logger.info("   Customer ${result.num_N_SUBJECT_ID} was ${params.customerId ? 'updated' : 'created'} successfully!")
      return result
    } catch (Exception e){
      logger.error("   Error while ${input.customerId ? 'updating' : 'creating'} customer!")
      logger.error_oracle(e)
      return null
    }
  }

  Map createCustomer(Map input) {
    input.remove('customerId')
    return putCustomer(input)
  }

  Map createCustomer(Map input, def baseSubjectId) {
    return createCustomer(input + [baseSubjectId: baseSubjectId])
  }

  Map updateCustomer(Map input = [:], def customerId) {
    return putCustomer(input + [customerId: customerId])
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

  Map addCustomerAddParam(Map input = [:], def customerId) {
    return addSubjectAddParam(input, customerId)
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

  Map addCustomerGroup(Map input = [:], def customerId) {
    return addSubjectGroup(input, customerId)
  }

  Boolean deleteCustomerGroup(def customerId) {
    return deleteSubjectGroup(customerId)
  }

  Boolean deleteCustomerGroup(Map input) {
    if (input.containsKey('customerId')) {
      input.subjectId = input.customerId
      input.remove('customerId')
    }
    return deleteSubjectGroup(input)
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

  private Map putCustomerNetServiceAccess(Map input) {
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
        Boolean passwordIsNeed = notEmpty(params.password)

        if (passwordIsNeed) {
          Boolean passwordIsSet = changeNetServicePassword(subjServId: access.num_N_SUBJ_SERV_ID, newPassword: params.password)
          if (!passwordIsSet) {
            throw new Exception('Error while setting application password!')
          }
          access += [vch_VC_PASS: params.password]
        }
      }
      logger.info("   Customer now have access to net service!")
      return access
    } catch (Exception e){
      logger.error("   Error while providing access to net service!")
      logger.error_oracle(e)
      return null
    }
  }

  Map addCustomerNetServiceAccess(Map input = [:], def customerId) {
    return putCustomerNetServiceAccess(input + [customerId: customerId])
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

  Boolean changeNetServicePassword(Map input = [:], def customerId) {
    return changeNetServicePassword(input + [customerId: customerId])
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

  private Map putCustomerAppAccess(Map input) {
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
        Boolean passwordIsNeed = notEmpty(params.password)

        if (passwordIsNeed) {
          Boolean passwordIsSet = changeAppPassword(subjServId: access.num_N_SUBJ_SERV_ID, newPassword: params.password)
          if (!passwordIsSet) {
            throw new Exception('Error while setting application password!')
          }
          access += [vch_VC_PASS: params.password]
        }
      }
      logger.info("   Customer now have access to application!")
      return access
    } catch (Exception e){
      logger.error("   Error while providing access to application!")
      logger.error_oracle(e)
      return null
    }
  }

  Map addCustomerAppAccess(Map input = [:], def customerId) {
    return putCustomerAppAccess(input + [customerId: customerId])
  }

  Boolean changeAppPassword(Map input) {
    return changeNetServicePassword(input)
  }

  Boolean changeAppPassword(Map input = [:], def customerId) {
    return changeAppPassword(input + [customerId: customerId])
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

  private Map putCustomerSelfCareAccess(Map input) {
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

  Map addCustomerSelfCareAccess(Map input = [:], def customerId) {
    return putCustomerSelfCareAccess(input + [customerId: customerId])
  }

  Boolean changeSelfCarePassword(Map input) {
    return changeAppPassword(input + [appId: getSelfCareApplicationId()])
  }

  Boolean changeSelfCarePassword(Map input = [:], def customerId) {
    return changeSelfCarePassword(input + [customerId: customerId])
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
