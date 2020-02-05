package org.camunda.latera.bss.utils

import static java.lang.reflect.Modifier.isStatic
import static java.lang.reflect.Modifier.isPrivate
import static org.camunda.latera.bss.utils.StringUtil.capitalize
import static org.camunda.latera.bss.utils.Numeric.toIntSafe

class Constants {
  public static final Integer LOG_LEVEL_DEBUG    = 0
  public static final Integer LOG_LEVEL_INFO     = 1
  public static final Integer LOG_LEVEL_WARNING  = 2
  public static final Integer LOG_LEVEL_ERROR    = 3
  public static final Integer LOG_LEVEL_CRITICAL = 4
  public static final Integer LOG_LEVEL_DEFAULT  = 5

  public static final Integer DEFAULT_FIRM = 100

  public static final Integer REF_Unknown = -10

  public static final Integer REF_TYPE_Subject_Type  = 1
  public static final Integer SUBJ_TYPE_Company      = 1001
  public static final Integer SUBJ_TYPE_User         = 2001
  public static final Integer SUBJ_TYPE_Customer     = 2001
  public static final Integer SUBJ_TYPE_DatabaseUser = 3001
  public static final Integer SUBJ_TYPE_Employee     = 4001
  public static final Integer SUBJ_TYPE_Department   = 5001
  public static final Integer SUBJ_TYPE_PayCard      = 11001
  public static final Integer SUBJ_TYPE_Provider     = 14001
  public static final Integer SUBJ_TYPE_Bank         = 16001
  public static final Integer SUBJ_TYPE_Group        = 17001
  public static final Integer SUBJ_TYPE_Person       = 18001
  public static final Integer SUBJ_TYPE_Role         = 19001
  public static final Integer SUBJ_TYPE_Reseller     = 20001

  public static final Integer REF_TYPE_Document_Type        = 2
  public static final Integer DOC_TYPE_SubscriberContract   = 1002
  public static final Integer DOC_TYPE_ContractAPP          = 2002
  public static final Integer DOC_TYPE_ChargeLog            = 3002
  public static final Integer DOC_TYPE_CashWarrant          = 4002
  public static final Integer DOC_TYPE_PaymentOrder         = 5002
  public static final Integer DOC_TYPE_Bill                 = 6002
  public static final Integer DOC_TYPE_PriceOrder           = 7002
  public static final Integer DOC_TYPE_BaseContract         = 9002
  public static final Integer DOC_TYPE_RecordOfBank         = 11002
  public static final Integer DOC_TYPE_TimeIntervals        = 12002
  public static final Integer DOC_TYPE_AddAgreement         = 13002
  public static final Integer DOC_TYPE_CardsIssue           = 14002
  public static final Integer DOC_TYPE_Overdraft            = 15002
  public static final Integer DOC_TYPE_TrafficClasses       = 16002
  public static final Integer DOC_TYPE_Request              = 20002
  public static final Integer DOC_TYPE_PeriodClose          = 21002
  public static final Integer DOC_TYPE_PeriodOpen           = 22002
  public static final Integer DOC_TYPE_CurrExchRates        = 23002
  public static final Integer DOC_TYPE_DataProcRequest      = 24002
  public static final Integer DOC_TYPE_ReconciliationReport = 25002
  public static final Integer DOC_TYPE_SubscrChangePlan     = 26002
  public static final Integer DOC_TYPE_DiscountSpec         = 27002
  public static final Integer DOC_TYPE_DiscountCertificate  = 28002
  public static final Integer DOC_TYPE_PaymentRequest       = 29002

  public static final Integer REF_TYPE_Document_State       = 3
  public static final Integer DOC_STATE_Approval            = 1003
  public static final Integer DOC_STATE_Request             = 2003
  public static final Integer DOC_STATE_Draft               = 3003
  public static final Integer DOC_STATE_Actual              = 4003
  public static final Integer DOC_STATE_Canceled            = 5003
  public static final Integer DOC_STATE_Executed            = 6003
  public static final Integer DOC_STATE_Rejected            = 7003
  public static final Integer DOC_STATE_Enforcement         = 8003
  public static final Integer DOC_STATE_Closed              = 9003
  public static final Integer DOC_STATE_Prepared            = 10003
  public static final Integer DOC_STATE_Processing          = 11003
  public static final Integer DOC_STATE_Dissolved           = 12003
  public static final Integer DOC_STATE_Authorized          = 13003
  public static final Integer DOC_STATE_Suspended           = 14003

  public static final Integer REF_TYPE_Subj_Roles           = 4
  public static final Integer SUBJ_ROLE_Receiver            = 1004
  public static final Integer SUBJ_ROLE_Provider            = 2004
  public static final Integer SUBJ_ROLE_Member              = 3004
  public static final Integer SUBJ_ROLE_ForWho              = 4004
  public static final Integer SUBJ_ROLE_Executor            = 5004
  public static final Integer SUBJ_ROLE_Manager             = 6004
  public static final Integer SUBJ_ROLE_Payer               = 7004
  public static final Integer SUBJ_ROLE_Customer            = 8004

  public static final IntegerREF_TYPE_Account_Type = 42
  public static final Integer ACC_TYPE_Settlement  = 1042
  public static final Integer ACC_TYPE_Personal    = 2042
  public static final Integer ACC_TYPE_Cash        = 3042
  public static final Integer ACC_TYPE_EPurse      = 4042
  public static final Integer ACC_TYPE_PaySys      = 5042
  public static final Integer ACC_TYPE_Reseller    = 6042

  public static final Integer REF_TYPE_Address_Type   = 6
  public static final Integer ADDR_TYPE_FactPlace     = 1006
  public static final Integer ADDR_TYPE_Subnet        = 2006
  public static final Integer ADDR_TYPE_IP            = 3006
  public static final Integer ADDR_TYPE_MAC           = 4006
  public static final Integer ADDR_TYPE_VLAN          = 5006
  public static final Integer ADDR_TYPE_EMail         = 6006
  public static final Integer ADDR_TYPE_URL           = 7006
  public static final Integer ADDR_TYPE_GlobalICQ     = 8006
  public static final Integer ADDR_TYPE_Fax           = 9006
  public static final Integer ADDR_TYPE_Jabber        = 10006
  public static final Integer ADDR_TYPE_TelCode       = 11006
  public static final Integer ADDR_TYPE_TelZone       = 12006
  public static final Integer ADDR_TYPE_Telephone     = 13006
  public static final Integer ADDR_TYPE_SubnetGroup   = 14006
  public static final Integer ADDR_TYPE_DNS_A_Rec     = 15006
  public static final Integer ADDR_TYPE_DNS_CNAME_Rec = 16006
  public static final Integer ADDR_TYPE_Skype         = 17006
  public static final Integer ADDR_TYPE_Subnet6       = 18006
  public static final Integer ADDR_TYPE_IP6           = 19006

  public static final Integer REF_TYPE_Bind_Addr_Type    = 16
  public static final Integer BIND_ADDR_TYPE_Actual      = 1016
  public static final Integer BIND_ADDR_TYPE_HostPort    = 3016
  public static final Integer BIND_ADDR_TYPE_Jur         = 4016
  public static final Integer BIND_ADDR_TYPE_Fixed       = 5016
  public static final Integer BIND_ADDR_TYPE_Serv        = 6016
  public static final Integer BIND_ADDR_TYPE_FixLocation = 7016
  public static final Integer BIND_ADDR_TYPE_Work        = 8016
  public static final Integer BIND_ADDR_TYPE_Mobile      = 9016
  public static final Integer BIND_ADDR_TYPE_Home        = 10016
  public static final Integer BIND_ADDR_TYPE_Post        = 11016
  public static final Integer BIND_ADDR_TYPE_Notice      = 12016
  public static final Integer BIND_ADDR_TYPE_Resource    = 13016

  public static final Integer REF_TYPE_Addr_State = 29
  public static final Integer ADDR_STATE_On       = 1029
  public static final Integer ADDR_STATE_Off      = 2029

  public static final Integer REF_TYPE_Subject_State        = 11
  public static final Integer SUBJ_STATE_Off                = 1011
  public static final Integer SUBJ_STATE_On                 = 2011
  public static final Integer SUBJ_STATE_Locked             = 3011
  public static final Integer SUBJ_STATE_Disabled           = 4011
  public static final Integer SUBJ_STATE_ManuallySuspended  = 5011
  public static final Integer SUBJ_STATE_PaymentsLocked     = 6011
  public static final Integer SUBJ_STATE_CLIssuingSuspended = 7011

  public static final Integer WFLOW_SubscriberContract    = 10021
  public static final Integer WFLOW_ContractAPP           = 20021
  public static final Integer WFLOW_ChargeLog             = 30021
  public static final Integer WFLOW_CashWarrant           = 40021
  public static final Integer WFLOW_PaymentOrder          = 50021
  public static final Integer WFLOW_Bill                  = 60021
  public static final Integer WFLOW_AdvanceBill           = 60022
  public static final Integer WFLOW_PrepaymentBill        = 60023
  public static final Integer WFLOW_PriceOrder            = 70021
  public static final Integer WFLOW_BaseContract          = 90021
  public static final Integer WFLOW_RecordOfBank          = 110021
  public static final Integer WFLOW_TimeIntervals         = 120021
  public static final Integer WFLOW_AddAgreement          = 130021
  public static final Integer WFLOW_CardsIssue            = 140021
  public static final Integer WFLOW_Overdraft             = 150021
  public static final Integer WFLOW_TrafficClasses_IP     = 160021
  public static final Integer WFLOW_TrafficClasses_Voice  = 160022
  public static final Integer WFLOW_Request               = 200021
  public static final Integer WFLOW_PeriodClose           = 210021
  public static final Integer WFLOW_PeriodOpen            = 220021
  public static final Integer WFLOW_CurrExchRates         = 230021
  public static final Integer WFLOW_DataProcRequest       = 240021
  public static final Integer WFLOW_ReconciliationReport  = 250021
  public static final Integer WFLOW_SubscrChangePlan      = 260021
  public static final Integer WFLOW_DiscountSpecification = 270021
  public static final Integer WFLOW_DiscountCertificate   = 280021
  public static final Integer WFLOW_PaymentRequest        = 290021

  public static final Integer REF_TYPE_Good_Move_Type     = 47
  public static final Integer GM_TYPE_Charged             = 1047
  public static final Integer GM_TYPE_Reserve             = 2047
  public static final Integer GM_TYPE_Request             = 3047
  public static final Integer GM_TYPE_Bind                = 4047
  public static final Integer GM_TYPE_Cancelled           = 5047

  public static final Integer REF_TYPE_Subj_Serv_Type = 66
  public static final Integer SUBJ_SERV_AppAccess     = 1066
  public static final Integer SUBJ_SERV_Manage        = 2066
  public static final Integer SUBJ_SERV_ServiceUse    = 3066

  public static final Integer REF_TYPE_Data_Type  = 18
  public static final Integer DATA_TYPE_Varchar   = 1018
  public static final Integer DATA_TYPE_Number    = 2018
  public static final Integer DATA_TYPE_Char      = 3018
  public static final Integer DATA_TYPE_Date      = 4018
  public static final Integer DATA_TYPE_Ref       = 5018
  public static final Integer DATA_TYPE_Flag      = 6018
  public static final Integer DATA_TYPE_Clob      = 7018
  public static final Integer DATA_TYPE_HTML      = 8018
  public static final Integer DATA_TYPE_Integer   = 9018
  public static final Integer DATA_TYPE_Pipelined = 10018
  public static final Integer DATA_TYPE_Object    = 11018

  public static final Integer REF_TYPE_Auth_Type     = 19
  public static final Integer AUTH_TYPE_LoginPass    = 1019
  public static final Integer AUTH_TYPE_SNMP1        = 2019
  public static final Integer AUTH_TYPE_SNMP2        = 3019
  public static final Integer AUTH_TYPE_HTTP         = 4019
  public static final Integer AUTH_TYPE_RSH          = 5019
  public static final Integer AUTH_TYPE_SSH          = 6019
  public static final Integer AUTH_TYPE_Telnet       = 7019
  public static final Integer AUTH_TYPE_Local        = 8019
  public static final Integer AUTH_TYPE_RadiusSecret = 9019
  public static final Integer AUTH_TYPE_DrWeb        = 10019
  public static final Integer AUTH_TYPE_ESET         = 11019

  public static final Integer REF_TYPE_Pass_Hash_Type   = 117
  public static final Integer PASS_HASH_TYPE_MD5        = 1117
  public static final Integer PASS_HASH_TYPE_NT         = 2117
  public static final Integer PASS_HASH_TYPE_MySQL      = 3117
  public static final Integer PASS_HASH_TYPE_LM         = 4117
  public static final Integer PASS_HASH_TYPE_NTLM       = 5117
  public static final Integer PASS_HASH_TYPE_SMD5       = 6117
  public static final Integer PASS_HASH_TYPE_SHA1       = 7117
  public static final Integer PASS_HASH_TYPE_SSHA1      = 8117
  public static final Integer PASS_HASH_TYPE_Crypt      = 9117
  public static final Integer PASS_HASH_TYPE_MD5_salty  = 10117
  public static final Integer PASS_HASH_TYPE_SHA1_salty = 11117

  public static final Integer GOOD_Unknown           = -10
  public static final Integer GOOD_All               = 0
  public static final Integer GOOD_Value             = 1
  public static final Integer GOOD_NetDevice         = 101
  public static final Integer GOOD_NetDeviceSwitch   = 201
  public static final Integer GOOD_Servers           = 11101
  public static final Integer GOOD_ClientEquip       = 21401
  public static final Integer GOOD_PassiveEquip      = 31601
  public static final Integer GOOD_Cable             = 31701
  public static final Integer GOOD_Cable_Copper      = 31801
  public static final Integer GOOD_Cable_Optic       = 32001
  public static final Integer GOOD_Box               = 42201
  public static final Integer GOOD_Cross             = 52501
  public static final Integer GOOD_Serv              = 2
  public static final Integer GOOD_TrafficServ       = 1702
  public static final Integer Good_Spec              = 3
  public static final Integer GOOD_Spec_DevicePort   = 103
  public static final Integer GOOD_Spec_OpticalFiber = 10103
  public static final Integer GOOD_Spec_Units        = 12103
  public static final Integer GOOD_Spec_CrossPort    = 14203
  public static final Integer GOOD_Spec_Aggregator   = 63203
  public static final Integer GOOD_Spec_VirtualPort  = 65403
  public static final Integer Good_Packs             = 4
  public static final Integer Good_NetServ           = 5

  public static final Good_Realty  = 6
  public static final REALTY_House = 106

  public static final Good_Penalties              = 7
  public static final PENALTY_LatePaymentFee      = 107
  public static final PENALTY_EarlyTerminationFee = 207

  public static final Good_Adjustments   = 8
  public static final ADJUSTMENT_Default = 108

  public static final Integer NETSERV_Agent              = 105
  public static final Integer NETSERV_LDAP_GRP           = 205
  public static final Integer NETSERV_App                = 305
  public static final Integer NETSERV_System             = 405
  public static final Integer NETSERV_RADIUS_GRP         = 505
  public static final Integer NETSERV_ARM_ISP            = 1005
  public static final Integer NETSERV_ARM_Private_Office = 1105
  public static final Integer NETSERV_ARM_Migration      = 1205
  public static final Integer NETSERV_HPD                = 1305
  public static final Integer NETSERV_HID                = 1405
  public static final Integer NETSERV_Collector          = 2205
  public static final Integer NETSERV_NetDevManager      = 2305
  public static final Integer NETSERV_DataCollector      = 2405
  public static final Integer NETSERV_Firewall           = 3305
  public static final Integer NETSERV_Notify             = 3605
  public static final Integer NETSERV_VPN                = 4305
  public static final Integer NETSERV_DNS                = 4405
  public static final Integer NETSERV_DHCP               = 4505
  public static final Integer NETSERV_Email              = 4605
  public static final Integer NETSERV_Jabber             = 4705
  public static final Integer NETSERV_Web                = 4805

  public static final Integer REF_TYPE_Entity_Type        = 21
  public static final Integer ENTITY_TYPE_Subject         = 1021
  public static final Integer ENTITY_TYPE_Object          = 2021
  public static final Integer ENTITY_TYPE_Document        = 3021
  public static final Integer ENTITY_TYPE_Ref             = 4021
  public static final Integer ENTITY_TYPE_Good            = 5021
  public static final Integer ENTITY_TYPE_CatalogItem     = 5021
  public static final Integer ENTITY_TYPE_Address         = 6021
  public static final Integer ENTITY_TYPE_Account         = 7021
  public static final Integer ENTITY_TYPE_Job             = 8021
  public static final Integer ENTITY_TYPE_Workflow        = 9021
  public static final Integer ENTITY_TYPE_SubjComment     = 10021
  public static final Integer ENTITY_TYPE_ObjComment      = 11021
  public static final Integer ENTITY_TYPE_DocComment      = 12021
  public static final Integer ENTITY_TYPE_SubjAddParam    = 13021
  public static final Integer ENTITY_TYPE_ObjAddParam     = 14021
  public static final Integer ENTITY_TYPE_DocAddParam     = 15021
  public static final Integer ENTITY_TYPE_Rem             = 16021
  public static final Integer ENTITY_TYPE_Report          = 17021
  public static final Integer ENTITY_TYPE_BulkOperation   = 18021
  public static final Integer ENTITY_TYPE_ServScheme      = 19021
  public static final Integer ENTITY_TYPE_ProfileTemplate = 20021
  public static final Integer ENTITY_TYPE_ConfigTemplate  = 21021
  public static final Integer ENTITY_TYPE_ResCommission   = 22021

  public static final Integer REF_TYPE_Object_State = 40
  public static final Integer OBJ_STATE_Active      = 1040
  public static final Integer OBJ_STATE_NotActive   = 2040
  public static final Integer OBJ_STATE_RegisterOff = 3040

  public static final Integer REF_TYPE_Measure_Unit = 9
  public static final Integer UNIT_Piece            = 1009
  public static final Integer UNIT_Unknown          = 2009
  public static final Integer UNIT_Metr             = 3009
  public static final Integer UNIT_Meter            = 3009

  public static final Integer REF_TYPE_Overdraft_Reasons  = 121
  public static final Integer OVERDRAFT_PromisedPayment   = 1121
  public static final Integer OVERDRAFT_Remainder         = 2121
  public static final Integer OVERDRAFT_SchedDefPayment   = 3121
  public static final Integer OVERDRAFT_UnschedDefPayment = 4121
  public static final Integer OVERDRAFT_Manual            = 5121

  public static final Integer REF_TYPE_Overdraft_End_Reasons = 122
  public static final Integer OVERDRAFT_END_Expired          = 1122
  public static final Integer OVERDRAFT_END_FullRepayment    = 2122
  public static final Integer OVERDRAFT_END_PartRepayment    = 3122
  public static final Integer OVERDRAFT_END_Manual           = 4122

  public static final Integer REF_TYPE_CommentType        = 82
  public static final Integer COMMENT_TYPE_Support        = 1082
  public static final Integer COMMENT_TYPE_Comment        = 2082
  public static final Integer COMMENT_TYPE_PaymentHistory = 3082

  public static final Integer REF_TYPE_Currency = 44
  public static final Integer CURR_Ruble        = 1044
  public static final Integer CURR_USD          = 2044
  public static final Integer CURR_AZN          = 3044
  public static final Integer CURR_AMD          = 4044
  public static final Integer CURR_BYR          = 5044
  public static final Integer CURR_KZT          = 6044
  public static final Integer CURR_KGS          = 7044
  public static final Integer CURR_MDL          = 8044
  public static final Integer CURR_TJS          = 9044
  public static final Integer CURR_TMT          = 10044
  public static final Integer CURR_UZS          = 11044
  public static final Integer CURR_UAH          = 12044
  public static final Integer CURR_GEL          = 13044
  public static final Integer CURR_LTL          = 14044
  public static final Integer CURR_LVL          = 15044
  public static final Integer CURR_EUR          = 17044
  public static final Integer CURR_RUP          = 18044
  public static final Integer CURR_SEK          = 19044
  public static final Integer CURR_GBP          = 20044
  public static final Integer CURR_NOK          = 21044
  public static final Integer CURR_PLN          = 22044
  public static final Integer CURR_CHF          = 23044
  public static final Integer CURR_BGN          = 24044
  public static final Integer CURR_INR          = 25044
  public static final Integer CURR_JPY          = 26044
  public static final Integer CURR_CNY          = 27044
  public static final Integer CURR_NIO          = 28044
  public static final Integer CURR_XPF          = 29044
  public static final Integer CURR_BYN          = 30044
  public static final Integer CURR_PKR          = 31044
  public static final Integer CURR_AFN          = 32044
  public static final Integer CURR_PHP          = 33044
  public static final Integer CURR_IDR          = 34044
  public static final Integer CURR_MYR          = 35044
  public static final Integer CURR_NGN          = 36044
  public static final Integer CURR_BDT          = 37044
  public static final Integer CURR_ECS          = 38044
  public static final Integer CURR_LBP          = 39044
  public static final Integer CURR_TRY          = 40044
  public static final Integer CURR_BND          = 41044
  public static final Integer CURR_VND          = 42044
  public static final Integer CURR_KHR          = 43044
  public static final Integer CURR_LAK          = 44044
  public static final Integer CURR_MMK          = 45044
  public static final Integer CURR_SGD          = 46044
  public static final Integer CURR_THB          = 47044
  public static final Integer CURR_DZD          = 48044
  public static final Integer CURR_EGP          = 49044
  public static final Integer CURR_TND          = 50044
  public static final Integer CURR_LYD          = 51044
  public static final Integer CURR_ZAR          = 52044
  public static final Integer CURR_ETB          = 53044
  public static final Integer CURR_ILS          = 54044
  public static final Integer CURR_AED          = 55044
  public static final Integer CURR_AUD          = 56044
  public static final Integer CURR_BOB          = 57044
  public static final Integer CURR_BRL          = 58044
  public static final Integer CURR_CAD          = 59044
  public static final Integer CURR_CLP          = 60044
  public static final Integer CURR_COP          = 61044
  public static final Integer CURR_CRC          = 62044
  public static final Integer CURR_CZK          = 63044
  public static final Integer CURR_DKK          = 64044
  public static final Integer CURR_GHS          = 65044
  public static final Integer CURR_HKD          = 66044
  public static final Integer CURR_HRK          = 67044
  public static final Integer CURR_HUF          = 68044
  public static final Integer CURR_KES          = 69044
  public static final Integer CURR_KRW          = 70044
  public static final Integer CURR_LKR          = 71044
  public static final Integer CURR_MAD          = 72044
  public static final Integer CURR_MOP          = 73044
  public static final Integer CURR_MXN          = 74044
  public static final Integer CURR_NZD          = 75044
  public static final Integer CURR_PEN          = 76044
  public static final Integer CURR_QAR          = 77044
  public static final Integer CURR_RON          = 78044
  public static final Integer CURR_SAR          = 79044
  public static final Integer CURR_TWD          = 80044
  public static final Integer CURR_TZS          = 81044
  public static final Integer CURR_ALL          = 82044
  public static final Integer CURR_PAB          = 83044
  public static final Integer CURR_ARS          = 84044

  public static final Integer REF_TYPE_Lang = 131
  public static final Integer LANG_Ru       = 1131
  public static final Integer LANG_En       = 2131
  public static final Integer LANG_Ua       = 3131
  public static final Integer LANG_Kz       = 4131
  public static final Integer LANG_Kg       = 5131
  public static final Integer LANG_Az       = 6131
  public static final Integer LANG_Es       = 7131

  static Integer getLang(CharSequence code) {
    switch (capitalize(code)) {
      case 'Russian':
      case 'Ru':
        return LANG_Ru
      case 'English':
      case 'En':
        return LANG_En
      case 'Ukrainian':
      case 'Ua':
        return LANG_Ua
      case 'Kazakh':
      case 'Kz':
        return LANG_Kz
      case 'Kirghiz':
      case 'Kg':
        return LANG_Kg
      case 'Azerbaijani':
      case 'Az':
        return LANG_Az
      case 'Spanish':
      case 'Es':
        return LANG_Es
    }

    return null
  }

  static Integer getConstantByCode(CharSequence code) {
    def lang = getLang(code?.replace('LANG_', ''))
    if (lang) {
      return lang
    }

    def field = Constants.class.declaredFields.find { def it ->
      !isPrivate(it.modifiers) && isStatic(it.modifiers) && it.name == code
    }

    return field?.get()
  }

  static String getConstantCode(def value) {
    BigInteger id = toIntSafe(value)
    def field = Constants.declaredFields.find { def it ->
      !isPrivate(it.modifiers) && isStatic(it.modifiers) && it.get() == id
    }

    return field?.name
  }
}
