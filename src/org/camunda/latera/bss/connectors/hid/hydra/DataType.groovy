package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.utils.Oracle
import org.camunda.latera.bss.utils.DateTimeUtil

trait DataType {
  private static String STRING_TYPE  = 'DATA_TYPE_Varchar'
  private static String BOOLEAN_TYPE = 'DATA_TYPE_Char'
  private static String FLOAT_TYPE   = 'DATA_TYPE_Number'
  private static String INTEGER_TYPE = 'DATA_TYPE_Integer'
  private static String DATE_TYPE    = 'DATA_TYPE_Date'
  private static String REF_TYPE     = 'DATA_TYPE_Ref'
  private static String FLAG_TYPE    = 'DATA_TYPE_Flag'
  private static String CLOB_TYPE    = 'DATA_TYPE_Clob'
  private static String HTML_TYPE    = 'DATA_TYPE_HTML'

  def getStringType() {
    return STRING_TYPE
  }

  def getStringTypeId() {
    return getRefIdByCode(getStringType())
  }

  def getBooleanType() {
    return BOOLEAN_TYPE
  }

  def getBooleanTypeId() {
    return getRefIdByCode(getBooleanType())
  }

  def getBoolType() {
    return getBooleanType()
  }

  def getBoolTypeId() {
    return getBooleanTypeId()
  }

  def getFloatType() {
    return FLOAT_TYPE
  }

  def getFloatTypeId() {
    return getRefIdByCode(getFloatType())
  }

  def getIntegerType() {
    return INTEGER_TYPE
  }

  def getIntegerTypeId() {
    return getRefIdByCode(getIntegerType())
  }

  def getDateType() {
    return DATE_TYPE
  }

  def getDateTypeId() {
    return getRefIdByCode(getDateType())
  }

  def getDateTimeType() {
    return getDateType()
  }

  def getDateTimeTypeId() {
    return getDateTypeId()
  }

  def getRefType() {
    return REF_TYPE
  }

  def getRefTypeId() {
    return getRefIdByCode(getRefType())
  }

  def getFlagType() {
    return FLAG_TYPE
  }

  def getFlagTypeId() {
    return getRefIdByCode(getFlagType())
  }

  def getCLOBType() {
    return CLOB_TYPE
  }

  def getCLOBTypeId() {
    return getRefIdByCode(getClobType())
  }

  def getClobType() {
    return getCLOBType()
  }

  def getClobTypeId() {
    return getCLOBTypeId()
  }

  def getHTMLType() {
    return HTML_TYPE
  }

  def getHTMLTypeId() {
    return getRefIdByCode(getHTMLType())
  }

  def getHtmlType() {
    return getHTMLType()
  }

  def getHtmlTypeId() {
    return getHTMLTypeId()
  }

  def detectDataType(def value) {
    if (value instanceof Boolean) {
      return 'bool'
    } else if (value instanceof BigInteger) {
      return 'refId'
    } else if (value instanceof String) {
      return 'string'
    } else if (DateTimeUtil.isDate(value)) {
      return 'date'
    } else {
      return 'number'
    }
  }
}