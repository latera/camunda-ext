package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Numeric.isInteger
import static org.camunda.latera.bss.utils.StringUtil.isString
import static org.camunda.latera.bss.utils.DateTimeUtil.isDate

trait DataType {
  private static String STRING_TYPE  = 'DATA_TYPE_Varchar'
  private static String CHAR_TYPE    = 'DATA_TYPE_Char'
  private static String FLOAT_TYPE   = 'DATA_TYPE_Number'
  private static String INTEGER_TYPE = 'DATA_TYPE_Integer'
  private static String DATE_TYPE    = 'DATA_TYPE_Date'
  private static String REF_TYPE     = 'DATA_TYPE_Ref'
  private static String BOOLEAN_TYPE = 'DATA_TYPE_Flag'
  private static String CLOB_TYPE    = 'DATA_TYPE_Clob'
  private static String HTML_TYPE    = 'DATA_TYPE_HTML'

  String getStringType() {
    return STRING_TYPE
  }

  Number getStringTypeId() {
    return getRefIdByCode(getStringType())
  }

  String getCharType() {
    return CHAR_TYPE
  }

  Number getCharTypeId() {
    return getRefIdByCode(getCharType())
  }

  String getFloatType() {
    return FLOAT_TYPE
  }

  Number getFloatTypeId() {
    return getRefIdByCode(getFloatType())
  }

  String getIntegerType() {
    return INTEGER_TYPE
  }

  Number getIntegerTypeId() {
    return getRefIdByCode(getIntegerType())
  }

  String getDateType() {
    return DATE_TYPE
  }

  Number getDateTypeId() {
    return getRefIdByCode(getDateType())
  }

  String getDateTimeType() {
    return getDateType()
  }

  Number getDateTimeTypeId() {
    return getDateTypeId()
  }

  String getRefType() {
    return REF_TYPE
  }

  Number getRefTypeId() {
    return getRefIdByCode(getRefType())
  }

  String getBooleanType() {
    return BOOLEAN_TYPE
  }

  Number getBooleanTypeId() {
    return getRefIdByCode(getBooleanType())
  }

  String getBoolType() {
    return getBooleanType()
  }

  Number getBoolTypeId() {
    return getBooleanTypeId()
  }

  String getCLOBType() {
    return CLOB_TYPE
  }

  Number getCLOBTypeId() {
    return getRefIdByCode(getClobType())
  }

  String getClobType() {
    return getCLOBType()
  }

  Number getClobTypeId() {
    return getCLOBTypeId()
  }

  String getHTMLType() {
    return HTML_TYPE
  }

  Number getHTMLTypeId() {
    return getRefIdByCode(getHTMLType())
  }

  String getHtmlType() {
    return getHTMLType()
  }

  Number getHtmlTypeId() {
    return getHTMLTypeId()
  }

  String detectDataType(def value) {
    if (value instanceof Boolean) {
      return 'bool'
    } else if (isInteger(value)) {
      return 'refId'
    } else if (isString(value)) {
      return 'string'
    } else if (isDate(value)) {
      return 'date'
    } else {
      return 'number'
    }
  }
}