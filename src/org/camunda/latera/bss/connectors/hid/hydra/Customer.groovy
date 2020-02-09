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

  /**
   * Get customers table name
   */
  String getCustomersTable() {
    return CUSTOMERS_TABLE
  }

  /**
   * Get customer net services table name
   */
  String getCustomerNetServicesTable() {
    return CUSTOMER_NET_SERVICES_TABLE
  }

  /**
   * Get customer entity type ref code
   */
  String getCustomerType() {
    return getRefCode(getCustomerTypeId())
  }

  /**
   * Get customer entity type ref id
   */
  Number getCustomerTypeId() {
    return SUBJ_TYPE_User
  }

  /**
   * Get net service subscription type ref code
   */
  String getNetServiceType() {
    return getRefCode(getNetServiceTypeId())
  }

  /**
   * Get net service subscription type ref code
   */
  Number getNetServiceTypeId() {
    return SUBJ_SERV_ServiceUse
  }

  /**
   * Get application subscription type ref code
   */
  String getApplicationType() {
    return getRefCode(getApplicationTypeId())
  }

  /**
   * Get application subscription type ref id
   */
  Number getApplicationTypeId() {
    return SUBJ_SERV_AppAccess
  }

  /**
   * Get login+pass auth type ref code
   */
  String getAuthLoginPassType() {
    return getRefCode(getAuthLoginPassTypeId())
  }

  /**
   * Get login+pass auth type ref id
   */
  Number getAuthLoginPassTypeId() {
    return AUTH_TYPE_LoginPass
  }

  /**
   * Get MD5 hash type ref code
   */
  String getPassHashMD5Type() {
    return getRefCode(getPassHashMD5TypeId())
  }

  /**
   * Get MD5 hash type ref id
   */
  Number getPassHashMD5TypeId() {
    return PASS_HASH_TYPE_MD5
  }

  /**
   * Get SHA1 hash type ref code
   */
  String getPassHashSHA1Type() {
    return getRefCode(getPassHashSHA1TypeId())
  }

  /**
   * Get SHA1 hash type ref id
   */
  Number getPassHashSHA1TypeId() {
    return PASS_HASH_TYPE_SHA1
  }

  /**
   * Get SSHA1 hash type ref code
   */
  String getPassHashSSHA1Type() {
    return getRefCode(getPassHashSSHA1TypeId())
  }

  /**
   * Get SSHA1 hash type ref id
   */
  Number getPassHashSSHA1TypeId() {
    return PASS_HASH_TYPE_SSHA1
  }

  /**
   * Get Crypt hash type ref code
   */
  String getPassHashCryptType() {
    return getRefCode(getPassHashCryptTypeId())
  }

  /**
   * Get Crypt hash type ref id
   */
  Number getPassHashCryptTypeId() {
    return PASS_HASH_TYPE_Crypt
  }

  /**
   * Get MD5 with salt hash type ref code
   */
  String getPassHashMD5SaltyType() {
    return getRefCode(getPassHashMD5SaltyTypeId())
  }

  /**
   * Get MD5 with salt hash type ref id
   */
  Number getPassHashMD5SaltyTypeId() {
    return PASS_HASH_TYPE_MD5_salty
  }

  /**
   * Get SHA1 with salt hash type ref code
   */
  String getPassHashSHA1SaltyType() {
    return getRefCode(getPassHashSHA1SaltyTypeId())
  }

  /**
   * Get SHA1 with salt hash type ref id
   */
  Number getPassHashSHA1SaltyTypeId() {
    return PASS_HASH_TYPE_SHA1_salty
  }

  /**
   * Get Self-Care code
   */
  String getSelfCareApplication() {
    return getRefCode(getSelfCareApplicationId())
  }

  /**
   * Get Self-Care id
   */
  Number getSelfCareApplicationId() {
    return NETSERV_ARM_Private_Office
  }

  /**
   * Get Service Provider Console application code
   */
  String getISPApplication() {
    return getRefCode(getISPApplicationId())
  }

  /**
   * Get Service Provider Console application id
   */
  Number getISPApplicationId() {
    return NETSERV_ARM_ISP
  }

  /**
   * Get HID application code
   */
  String getHIDApplication() {
    return getRefCode(getHIDApplicationId())
  }

  /**
   * Get HID application id
   */
  Number getHIDApplicationId() {
    return NETSERV_HID
  }

  /**
   * Get customer by id
   * @param customerId {@link java.math.BigInteger BigInteger}
   * @return Map with customer table row or null
   */
  Map getCustomer(def customerId) {
    LinkedHashMap where = [
      n_subject_id: customerId
    ]
    return hid.getTableFirst(getCustomersTable(), where: where)
  }

  /**
   * Search for customers by different fields value
   * @param customerId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param baseSubjectId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param creatorId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param groupId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param resellerId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: current reseller id
   * @param stateId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param state         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param firmId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. default: current firm Id
   * @param tags          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit         {@link Integer}. Optional, default: 0 (unlimited)
   * @param order         {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: [:]
   * @return List[Map] of customer table rows
   */
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

  /**
   * Search for customer by different fields value
   * @param customerId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param baseSubjectId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param creatorId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param groupId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param resellerId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional, default: current reseller id
   * @param stateId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param state         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param firmId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. default: current firm Id
   * @param tags          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit         {@link Integer}. Optional, default: 0 (unlimited)
   * @param order         {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: [:]
   * @return Map with of customer table row
   */
  Map getCustomerBy(Map input) {
    return getCustomersBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Check if entity or entity type is customer
   * @param entityOrEntityType {@link java.math.BigInteger BigInteger} or {@link CharSequence String}. Subject id, subject type ref id or subject type ref code
   * @return True if given value is customer, false otherwise
   */
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

  /**
   * Create or update customer
   * @param customerId    {@link java.math.BigInteger BigInteger}. Optional
   * @param baseSubjectId {@link java.math.BigInteger BigInteger}. Optional
   * @param groupId       {@link java.math.BigInteger BigInteger}. Optional
   * @param code          {@link CharSequence String}. Optional
   * @param rem           {@link CharSequence String}. Optional
   * @param firmId        {@link java.math.BigInteger BigInteger}. Optional. Default: current firm Id
   * @param resellerId    {@link java.math.BigInteger BigInteger}. Optional. Default: current reseller id
   * @param stateId       {@link java.math.BigInteger BigInteger}. Optional. Default: active subject state
   * @param state         {@link CharSequence String}. Optional
   * @return Map with created or updated customer (in Oracle API procedure notation)
   */
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

  /**
   * Create customer
   * @param baseSubjectId {@link java.math.BigInteger BigInteger}
   * @param groupId       {@link java.math.BigInteger BigInteger}
   * @param code          {@link CharSequence String}. Optional
   * @param rem           {@link CharSequence String}. Optional
   * @param firmId        {@link java.math.BigInteger BigInteger}. Optional. Default: current firm Id
   * @param resellerId    {@link java.math.BigInteger BigInteger}. Optional. Default: current reseller id
   * @param stateId       {@link java.math.BigInteger BigInteger}. Optional. Default: active subject state
   * @param state         {@link CharSequence String}. Optional
   * @return Map with created customer (in Oracle API procedure notation)
   */
  Map createCustomer(Map input) {
    input.remove('customerId')
    return putCustomer(input)
  }

  /**
   * Create customer
   *
   * Alias for with mandatory baseSubjectId field
   * @see #createCustomer(Map)}
   */
  Map createCustomer(Map input, def baseSubjectId) {
    return createCustomer(input + [baseSubjectId: baseSubjectId])
  }

  /**
   * Update customer
   * @param customerId    {@link java.math.BigInteger BigInteger}
   * @param baseSubjectId {@link java.math.BigInteger BigInteger}. Optional
   * @param groupId       {@link java.math.BigInteger BigInteger}. Optional
   * @param code          {@link CharSequence String}. Optional
   * @param rem           {@link CharSequence String}. Optional
   * @param resellerId    {@link java.math.BigInteger BigInteger}. Optional. Default: current reseller id
   * @param stateId       {@link java.math.BigInteger BigInteger}. Optional. Default: active subject state
   * @param state         {@link CharSequence String}. Optional
   * @return Map with updated customer (in Oracle API procedure notation)
   */
  Map updateCustomer(Map input = [:], def customerId) {
    return putCustomer(input + [customerId: customerId])
  }

  /**
   * Get customer additional param type id by code
   * @param code {@link CharSequence String}
   * @return Additional param type id
   */
  Number getCustomerAddParamTypeIdByCode(CharSequence code) {
    return getSubjectAddParamTypeIdByCode(code, getCustomerTypeId())
  }

  /**
   * Search for customer add params by different fields value
   * @see #getSubjectAddParamsBy(Map)
   */
  List getCustomerAddParamsBy(Map input) {
    if (input.containsKey('customerId')) {
      input.subjectId = input.customerId
      input.remove('customerId')
    }
    return getSubjectAddParamsBy(input)
  }

  /**
   * Search for customer one add param by different fields value
   * @see #getSubjectAddParamBy(Map)
   */
  Map getCustomerAddParamBy(Map input) {
    if (input.containsKey('customerId')) {
      input.subjectId = input.customerId
      input.remove('customerId')
    }
    return getSubjectAddParamBy(input)
  }

  /**
   * Add customer add param value
   * @see #addSubjectAddParam(Map)
   */
  Map addCustomerAddParam(Map input = [:], def customerId) {
    return addSubjectAddParam(input, customerId)
  }

  /**
   * Change customer state to Active
   * @see #enableSubject(def)
   */
  Boolean enableCustomer(def customerId) {
    return enableSubject(customerId)
  }

  /**
   * Change customer state to Suspended
   * @see #enableSubject(def)
   */
  Boolean suspendCustomer(def customerId) {
    return suspendSubject(customerId)
  }

  /**
   * Change customer state to Disabled
   * @see #enableSubject(def)
   */
  Boolean disableCustomer(def customerId) {
    return disableSubject(customerId)
  }

  /**
   * Search for customer groups
   * @see #getSubjectGroupsBy(Map)
   */
  List getCustomerGroupsBy(Map input) {
    if (input.containsKey('customerId')) {
      input.subjectId = input.customerId
      input.remove('customerId')
    }
    return getSubjectGroupsBy(input)
  }

  /**
   * Search for customer group
   * @see #getSubjectGroupBy(Map)
   */
  Map getCustomerGroupBy(Map input) {
    if (input.containsKey('customerId')) {
      input.subjectId = input.customerId
      input.remove('customerId')
    }
    return getSubjectGroupBy(input)
  }

  /**
   * Get customer groups
   * @see #getSubjectGroups(def)
   */
  List getCustomerGroups(def customerId) {
    return getSubjectGroups(customerId)
  }

  /**
   * Get customer group
   * @see #getSubjectGroup(def)
   */
  Map getCustomerGroup(def customerId) {
    return getSubjectGroup(customerId)
  }

  /**
   * Add group to customer
   * @see #addSubjectGroup(Map,def)
   */
  Map addCustomerGroup(Map input = [:], def customerId) {
    return addSubjectGroup(input, customerId)
  }

  /**
   * Delete group from customer
   * @see #deleteSubjectGroup(def)
   */
  Boolean deleteCustomerGroup(def customerId) {
    return deleteSubjectGroup(customerId)
  }

  /**
   * Delete group from customer
   *
   * Overload for searching and deleting customer group
   * @see #deleteSubjectGroup(Map)
   */
  Boolean deleteCustomerGroup(Map input) {
    if (input.containsKey('customerId')) {
      input.subjectId = input.customerId
      input.remove('customerId')
    }
    return deleteSubjectGroup(input)
  }

  /**
   * Search for net service subscriptions by different fields value
   * @param subjServId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param customerId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param netServiceId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param netService     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param objectId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subjServTypeId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subjServType   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param authTypeId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param authType       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param login          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param password       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param passwordHash   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param hashTypeId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param hashType       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit          {@link Integer}. Optional, default: 0 (unlimited)
   * @param order          {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: [:]
   * @return List[Map] of net service subscription table rows
   */
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
    if (params.netServiceId || params.applicationId || params.appId) {
      where.n_service_id = params.netServiceId ?: params.applicationId ?: params.applId
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

  /**
   * Search for net service subscription by different fields value
   * @param subjServId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param customerId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param netServiceId   {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param netService     {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param objectId       {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subjServTypeId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param subjServType   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param authTypeId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param authType       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param login          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param password       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param passwordHash   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param hashTypeId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param hashType       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param order          {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: [:]
   * @return Map with net servise subscription table row
   */
  Map getCustomerNetServiceAccessBy(Map input) {
    return getCustomerNetServicesAccessBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Create or update net service subscription
   * @param subjServId     {@link java.math.BigInteger BigInteger}. Optional
   * @param customerId     {@link java.math.BigInteger BigInteger}. Optional
   * @param netServiceId   {@link java.math.BigInteger BigInteger}. Optional if 'netService' arg is passed
   * @param netService     {@link CharSequence String}. Optional if 'netServiceId' arg is passed
   * @param objectId       {@link java.math.BigInteger BigInteger}. Optional
   * @param subjServTypeId {@link java.math.BigInteger BigInteger}. Optional if 'subjServType' arg is passed
   * @param subjServType   {@link CharSequence String}. Optional if 'subjServTypeId' arg is passed
   * @param authTypeId     {@link java.math.BigInteger BigInteger}. Optional if 'authType' arg is passed
   * @param authType       {@link CharSequence String}. Optional if 'authTypeId' arg is passed
   * @param login          {@link CharSequence String}. Optional
   * @param password       {@link CharSequence String}. Optional
   * @return Map with created or updated net service subscription (in Oracle API procedure notation)
   */
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
      if (access && notEmpty(params.password)) {
        Boolean passwordIsSet = changeNetServicePassword(subjServId: access.num_N_SUBJ_SERV_ID, newPassword: params.password)
        if (!passwordIsSet) {
          throw new Exception('Error while setting application password!')
        }
        access += [vch_VC_PASS: params.password]
      }
      logger.info("   Customer now have access to net service!")
      return access
    } catch (Exception e){
      logger.error("   Error while providing access to net service!")
      logger.error_oracle(e)
      return null
    }
  }

  /**
   * Create net service subscription
   * @param customerId     {@link java.math.BigInteger BigInteger}
   * @param netServiceId   {@link java.math.BigInteger BigInteger}. Optional if 'netService' arg is passed
   * @param netService     {@link CharSequence String}. Optional if 'netServiceId' arg is passed
   * @param objectId       {@link java.math.BigInteger BigInteger}. Optional
   * @param subjServTypeId {@link java.math.BigInteger BigInteger}. Optional if 'subjServType' arg is passed
   * @param subjServType   {@link CharSequence String}. Optional if 'subjServTypeId' arg is passed
   * @param authTypeId     {@link java.math.BigInteger BigInteger}. Optional if 'authType' arg is passed
   * @param authType       {@link CharSequence String}. Optional if 'authTypeId' arg is passed
   * @param login          {@link CharSequence String}. Optional
   * @param password       {@link CharSequence String}. Optional
   * @return Map with created net service subscription (in Oracle API procedure notation)
   */
  Map addCustomerNetServiceAccess(Map input = [:], def customerId) {
    return putCustomerNetServiceAccess(input + [customerId: customerId])
  }

  /**
   * Change net service subscription password
   * @param subjServId      {@link java.math.BigInteger BigInteger}. Optional
   * @param customerId      {@link java.math.BigInteger BigInteger}. Optional
   * @param netServiceId    {@link java.math.BigInteger BigInteger}. Optional if 'netService' arg is passed
   * @param netService      {@link CharSequence String}. Optional if 'netServiceId' arg is passed
   * @param objectId        {@link java.math.BigInteger BigInteger}. Optional
   * @param subjServTypeId  {@link java.math.BigInteger BigInteger}. Optional if 'subjServType' arg is passed
   * @param subjServType    {@link CharSequence String}. Optional if 'subjServTypeId' arg is passed
   * @param authTypeId      {@link java.math.BigInteger BigInteger}. Optional if 'authType' arg is passed
   * @param authType        {@link CharSequence String}. Optional if 'authTypeId' arg is passed
   * @param login           {@link CharSequence String}. Optional
   * @param oldPassword     {@link CharSequence String}. Optional, pass null if you don't want to check old password
   * @param oldPasswordHash {@link CharSequence String}. Optional, pass null if you don't want to check old password hash
   * @param newPassword     {@link CharSequence String}
   * @param hashTypeId      {@link java.math.BigInteger BigInteger}. Optional if 'hashType' arg is passed
   * @param hashType        {@link CharSequence String}. Optional if 'hashTypeId' arg is passed
   * @return True if password was changed successfully, false otherwise
   */
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

  /**
   * Change net service subscription password
   *
   * Overload for searching and deleting net service subscription
   * @param customerId      {@link java.math.BigInteger BigInteger}
   * @param netServiceId    {@link java.math.BigInteger BigInteger}. Optional if 'netService' arg is passed
   * @param netService      {@link CharSequence String}. Optional if 'netServiceId' arg is passed
   * @param objectId        {@link java.math.BigInteger BigInteger}. Optional
   * @param subjServTypeId  {@link java.math.BigInteger BigInteger}. Optional if 'subjServType' arg is passed
   * @param subjServType    {@link CharSequence String}. Optional if 'subjServTypeId' arg is passed
   * @param authTypeId      {@link java.math.BigInteger BigInteger}. Optional if 'authType' arg is passed
   * @param authType        {@link CharSequence String}. Optional if 'authTypeId' arg is passed
   * @param login           {@link CharSequence String}. Optional
   * @param oldPassword     {@link CharSequence String}. Optional, pass null if you don't want to check old password
   * @param oldPasswordHash {@link CharSequence String}. Optional, pass null if you don't want to check old password hash
   * @param newPassword     {@link CharSequence String}
   * @param hashTypeId      {@link java.math.BigInteger BigInteger}. Optional if 'hashType' arg is passed
   * @param hashType        {@link CharSequence String}. Optional if 'hashTypeId' arg is passed
   * @return True if password was changed successfully, false otherwise
   */
  Boolean changeNetServicePassword(Map input = [:], def customerId) {
    return changeNetServicePassword(input + [customerId: customerId])
  }

  /**
   * Delete net service subscription
   * @param subjServId {@link java.math.BigInteger BigInteger}
   * @return True if net service subscription was deleted successfully, false otherwise
   */
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

  /**
   * Delete net service subscription
   *
   * Overload for searching and deleting net service subscription
   * @param subjServId     {@link java.math.BigInteger BigInteger}. Optional
   * @param customerId     {@link java.math.BigInteger BigInteger}. Optional
   * @param netServiceId   {@link java.math.BigInteger BigInteger}. Optional
   * @param netService     {@link CharSequence String}. Optional
   * @param objectId       {@link java.math.BigInteger BigInteger}. Optional
   * @param subjServTypeId {@link java.math.BigInteger BigInteger}. Optional
   * @param subjServType   {@link CharSequence String}. Optional
   * @param authTypeId     {@link java.math.BigInteger BigInteger}. Optional
   * @param authType       {@link CharSequence String}. Optional
   * @param login          {@link CharSequence String}. Optional
   * @param password       {@link CharSequence String}. Optional
   * @param passwordHash   {@link CharSequence String}. Optional
   * @param hashTypeId     {@link java.math.BigInteger BigInteger}. Optional
   * @return True if net service subscription was deleted successfully, false otherwise
   */
  Boolean deleteCustomerNetServiceAccess(Map input) {
    def subjServId = getCustomerNetServiceAccessBy(input)?.n_subj_serv_id
    return deleteCustomerNetServiceAccess(subjServId)
  }

  /**
   * Search for application subscriptions by different fields value
   * @param subjServId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param customerId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param applicationId  {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param application    {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param appId          Alias for 'applicationId'
   * @param login          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param password       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param passwordHash   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param hashTypeId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param hashType       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit          {@link Integer}. Optional, default: 0 (unlimited)
   * @param order          {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: [:]
   * @return List[Map] of application subscription table rows
   */
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

  /**
   * Search for application subscription by different fields value
   * @param subjServId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param customerId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param applicationId  {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param application    {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param login          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param password       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param passwordHash   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param hashTypeId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param hashType       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit          {@link Integer}. Optional, default: 0 (unlimited)
   * @param order          {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: [:]
   * @return Map with application subscription table row
   */
  Map getCustomerAppAccessBy(Map input) {
    return getCustomerAppsAccessBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Create or update application subscription
   * @param subjServId     {@link java.math.BigInteger BigInteger}. Optional
   * @param customerId     {@link java.math.BigInteger BigInteger}. Optional
   * @param applicationId  {@link java.math.BigInteger BigInteger}. Optional if 'application' arg is passed
   * @param application    {@link CharSequence String}. Optional if 'applicationId' arg is passed
   * @param login          {@link CharSequence String}. Optional
   * @param password       {@link CharSequence String}. Optional
   * @return Map with created or updated net service subscription (in Oracle API procedure notation)
   */
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
      if (access && notEmpty(password)) {
        Boolean passwordIsSet = changeAppPassword(subjServId: access.num_N_SUBJ_SERV_ID, newPassword: params.password)
        if (!passwordIsSet) {
          throw new Exception('Error while setting application password!')
        }
        access += [vch_VC_PASS: params.password]
      }
      logger.info("   Customer now have access to application!")
      return access
    } catch (Exception e){
      logger.error("   Error while providing access to application!")
      logger.error_oracle(e)
      return null
    }
  }

  /**
   * Create application subscription
   * @param customerId     {@link java.math.BigInteger BigInteger}
   * @param applicationId  {@link java.math.BigInteger BigInteger}. Optional if 'application' arg is passed
   * @param application    {@link CharSequence String}. Optional if 'applicationId' arg is passed
   * @param login          {@link CharSequence String}. Optional
   * @param password       {@link CharSequence String}. Optional
   * @return Map with created application subscription (in Oracle API procedure notation)
   */
  Map addCustomerAppAccess(Map input = [:], def customerId) {
    return putCustomerAppAccess(input + [customerId: customerId])
  }

  /**
   * Change application subscription password
   * @param subjServId      {@link java.math.BigInteger BigInteger}. Optional
   * @param customerId      {@link java.math.BigInteger BigInteger}. Optional
   * @param applicationId   {@link java.math.BigInteger BigInteger}. Optional if 'application' arg is passed
   * @param application     {@link CharSequence String}. Optional if 'applicationId' arg is passed
   * @param login           {@link CharSequence String}. Optional
   * @param oldPassword     {@link CharSequence String}. Optional, pass null if you don't want to check old password
   * @param oldPasswordHash {@link CharSequence String}. Optional, pass null if you don't want to check old password hash
   * @param newPassword     {@link CharSequence String}
   * @param hashTypeId      {@link java.math.BigInteger BigInteger}. Optional if 'hashType' arg is passed
   * @param hashType        {@link CharSequence String}. Optional if 'hashTypeId' arg is passed
   * @return True if password was changed successfully, false otherwise
   */
  Boolean changeAppPassword(Map input) {
    return changeNetServicePassword(input)
  }

  /**
   * Change applicationsubscription password
   *
   * Overload with mandatory customerId arg
   * @see #changeAppPassword(Map)
   */
  Boolean changeAppPassword(Map input = [:], def customerId) {
    return changeAppPassword(input + [customerId: customerId])
  }

  /**
   * Delete application subscription
   * @param subjServId {@link java.math.BigInteger BigInteger}
   * @return True if application subscription was deleted successfully, false otherwise
   */
  Boolean deleteCustomerAppAccess(def subjServId) {
    return deleteCustomerNetServiceAccess(subjServId)
  }

  /**
   * Delete application subscription
   *
   * Overload for searching and deleting app subscription
   * @param subjServId     {@link java.math.BigInteger BigInteger}. Optional
   * @param customerId     {@link java.math.BigInteger BigInteger}. Optional
   * @param applicationId  {@link java.math.BigInteger BigInteger}. Optional
   * @param application    {@link CharSequence String}. Optional
   * @param login          {@link CharSequence String}. Optional
   * @param password       {@link CharSequence String}. Optional
   * @param passwordHash   {@link CharSequence String}. Optional
   * @param hashTypeId     {@link java.math.BigInteger BigInteger}. Optional
   * @return True if application subscription was deleted successfully, false otherwise
   */
  Boolean deleteCustomerAppAccess(Map input) {
    def subjServId = getCustomerAppAccessBy(input)?.n_subj_serv_id
    return deleteCustomerAppAccess(subjServId)
  }

  /**
   * Search for Self-Care subscription by different fields value
   * @param subjServId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param customerId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param login          {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param password       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param passwordHash   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param hashTypeId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param hashType       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit          {@link Integer}. Optional, default: 0 (unlimited)
   * @param order          {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: [:]
   * @return Map with Self-Care subscription table row
   */
  Map getCustomerSelfCareAccessBy(Map input) {
    input.applicationId = getSelfCareAppId()
    return getCustomerAppAccessBy(input)
  }

  /**
   * Create or update Self-Care subscription
   * @param customerId {@link java.math.BigInteger BigInteger}. Optional
   * @param login      {@link CharSequence String}. Optional
   * @param password   {@link CharSequence String}. Optional
   * @param force      {@link Boolean}. Replace old password with new one of not. Optional, default: false
   * @param firmId     {@link java.math.BigInteger BigInteger}. Optional. Default: current firm Id
   * @return Map with created or updated Self-Care subscription (in Oracle API procedure notation)
   */
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

  /**
   * Create Self-Care subscription
   * @param customerId {@link java.math.BigInteger BigInteger}
   * @param login      {@link CharSequence String}. Optional
   * @param password   {@link CharSequence String}. Optional
   * @param force      {@link Boolean}. Replace old password with new one of not. Optional, default: false
   * @param firmId     {@link java.math.BigInteger BigInteger}. Optional. Default: current firm Id
   * @return Map with created application subscription (in Oracle API procedure notation)
   */
  Map addCustomerSelfCareAccess(Map input = [:], def customerId) {
    return putCustomerSelfCareAccess(input + [customerId: customerId])
  }

  /**
   * Change Self-Care subscription password
   * @param subjServId      {@link java.math.BigInteger BigInteger}. Optional
   * @param customerId      {@link java.math.BigInteger BigInteger}. Optional
   * @param login           {@link CharSequence String}. Optional
   * @param oldPassword     {@link CharSequence String}. Optional, pass null if you don't want to check old password
   * @param oldPasswordHash {@link CharSequence String}. Optional, pass null if you don't want to check old password hash
   * @param newPassword     {@link CharSequence String}
   * @param hashTypeId      {@link java.math.BigInteger BigInteger}. Optional if 'hashType' arg is passed
   * @param hashType        {@link CharSequence String}. Optional if 'hashTypeId' arg is passed
   * @return True if password was changed successfully, false otherwise
   */
  Boolean changeSelfCarePassword(Map input) {
    return changeAppPassword(input + [applicationId: getSelfCareApplicationId()])
  }

  /**
   * Change Self-Care subscription password
   *
   * Overload with mandatory customerId arg
   * @param customerId      {@link java.math.BigInteger BigInteger}
   * @param login           {@link CharSequence String}. Optional
   * @param oldPassword     {@link CharSequence String}. Optional, pass null if you don't want to check old password
   * @param oldPasswordHash {@link CharSequence String}. Optional, pass null if you don't want to check old password hash
   * @param newPassword     {@link CharSequence String}
   * @param hashTypeId      {@link java.math.BigInteger BigInteger}. Optional if 'hashType' arg is passed
   * @param hashType        {@link CharSequence String}. Optional if 'hashTypeId' arg is passed
   * @return True if password was changed successfully, false otherwise
   */
  Boolean changeSelfCarePassword(Map input = [:], def customerId) {
    return changeSelfCarePassword(input + [customerId: customerId])
  }

  /**
   * Delete Self-Care subscription by customerId
   * @param customerId {@link java.math.BigInteger BigInteger}
   * @return True if Self-Care subscription was deleted successfully, false otherwise
   */
  Boolean deleteCustomerSelfCareAccess(def customerId) {
    def subjServId = getCustomerAppAccessBy(customerId: customerId, applicationId: getSelfCareApplicationId())?.n_subj_serv_id
    return deleteCustomerAppAccess(subjServId)
  }

  /**
   * Add tag to customer
   * @see #addSubjectTag(Map)
   */
  Map addCustomerTag(Map input) {
    input.subjectId = input.subjectId ?: input.customerId
    input.remove('customerId')
    return addSubjectTag(input)
  }

  /**
   * Add tag to customer
   * @see #addSubjectTag(def,CharSequence)
   */
  Map addCustomerTag(def customerId, CharSequence tag) {
    return addCustomerTag(customerId: customerId, tag: tag)
  }

  /**
   * Add tag to customer
   * @see #addSubjectTag(Map,def)
   */
  Map addCustomerTag(Map input = [:], def customerId) {
    return addCustomerTag(input + [customerId: customerId])
  }

  /**
   * Delete tag from customer
   * @see #deleteSubjectTag(def)
   */
  Boolean deleteCustomerTag(def customerTagId) {
    return deleteSubjectTag(customerTagId)
  }

  /**
   * Delete tag from customer
   * @see #deleteSubjectTag(Map)
   */
  Boolean deleteCustomerTag(Map input) {
    input.subjectId = input.subjectId ?: input.customerId
    input.remove('customerId')
    return deleteSubjectTag(input)
  }

  /**
   * Delete tag from customer
   * @see #deleteSubjectTag(def,CharSequence)
   */
  Boolean deleteCustomerTag(def customerId, CharSequence tag) {
    return deleteCustomerTag(customerId: customerId, tag: tag)
  }

  /**
   * Issue charge logs for a customer
   * @param customerId {@link java.math.BigInteger BigInteger}
   * @param beginDate  {@link java.time.Temporal Any date type}. Optional, default: current datetime
   * @param endDate    {@link java.time.Temporal Any date type}. Optional
   * @return True if charge logs were issued successfully, false otherwise
   */
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

  /**
   * Issue charge logs for a customer
   *
   * Overload with named args
   * @see processCustomer(def,Temporal,Temporal)
   */
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
