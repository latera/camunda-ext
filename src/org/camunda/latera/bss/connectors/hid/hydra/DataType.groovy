package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Numeric.isInteger
import static org.camunda.latera.bss.utils.StringUtil.isString
import static org.camunda.latera.bss.utils.DateTimeUtil.isDate
import static org.camunda.latera.bss.utils.Constants.DATA_TYPE_Varchar
import static org.camunda.latera.bss.utils.Constants.DATA_TYPE_Char
import static org.camunda.latera.bss.utils.Constants.DATA_TYPE_Number
import static org.camunda.latera.bss.utils.Constants.DATA_TYPE_Integer
import static org.camunda.latera.bss.utils.Constants.DATA_TYPE_Date
import static org.camunda.latera.bss.utils.Constants.DATA_TYPE_Ref
import static org.camunda.latera.bss.utils.Constants.DATA_TYPE_Flag
import static org.camunda.latera.bss.utils.Constants.DATA_TYPE_Clob
import static org.camunda.latera.bss.utils.Constants.DATA_TYPE_HTML

trait DataType {

  /**
   * Get string data type ref code
   */
  String getStringType() {
    return getRefCode(getStringTypeId())
  }

  /**
   * Get string data type ref id
   */
  Number getStringTypeId() {
    return DATA_TYPE_Varchar
  }

  /**
   * Get char data type ref code
   */
  String getCharType() {
    return getRefCode(getCharTypeId())
  }

  /**
   * Get char data type ref id
   */
  Number getCharTypeId() {
    return DATA_TYPE_Char
  }

  /**
   * Get float data type ref code
   */
  String getFloatType() {
    return getRefCode(getFloatTypeId())
  }

  /**
   * Get float data type ref id
   */
  Number getFloatTypeId() {
    return DATA_TYPE_Number
  }

  /**
   * Get integer data type ref code
   */
  String getIntegerType() {
    return getRefCode(getIntegerTypeId())
  }

  /**
   * Get integer data type ref id
   */
  Number getIntegerTypeId() {
    return DATA_TYPE_Integer
  }

  /**
   * Get date data type ref code
   */
  String getDateType() {
    return getRefCode(getDateTypeId())
  }

  /**
   * Get date data type ref id
   */
  Number getDateTypeId() {
    return DATA_TYPE_Date
  }

  /**
   * Get datetimt data type ref code
   */
  String getDateTimeType() {
    return getDateType()
  }

  /**
   * Get datetimt data type ref id
   */
  Number getDateTimeTypeId() {
    return getDateTypeId()
  }

  /**
   * Get ref data type ref code
   */
  String getRefType() {
    return getRefCode(getRefTypeId())
  }

  /**
   * Get ref data type ref id
   */
  Number getRefTypeId() {
    return DATA_TYPE_Ref
  }

  /**
   * Get boolean data type ref code
   */
  String getBooleanType() {
    return getRefCode(getBooleanTypeId())
  }

  /**
   * Get boolean data type ref id
   */
  Number getBooleanTypeId() {
    return DATA_TYPE_Flag
  }

  /**
   * Get boolean data type ref code
   *
   * Alias for {@link #getBooleanType()}
   */
  String getBoolType() {
    return getBooleanType()
  }

  /**
   * Get boolean data type ref id
   *
   * Alias for {@link #getBooleanTypeId()}
   */
  Number getBoolTypeId() {
    return getBooleanTypeId()
  }

  /**
   * Get CLOB data type ref code
   */
  String getCLOBType() {
    return getRefCode(getCLOBTypeId())
  }

  /**
   * Get CLOB data type ref id
   */
  Number getCLOBTypeId() {
    return DATA_TYPE_Clob
  }

  /**
   * Get CLOB data type ref code
   *
   * Alias for {@link #getCLOBType()}
   */
  String getClobType() {
    return getCLOBType()
  }

  /**
   * Get CLOB data type ref id
   *
   * Alias for {@link #getCLOBTypeId()}
   */
  Number getClobTypeId() {
    return getCLOBTypeId()
  }

  /**
   * Get HTML data type ref code
   */
  String getHTMLType() {
    return getRefCode(getHTMLTypeId())
  }

  /**
   * Get HTML data type ref id
   */
  Number getHTMLTypeId() {
    return DATA_TYPE_HTML
  }

  /**
   * Get HTML data type ref code
   *
   * Alias for {@link #getHTMLType()}
   */
  String getHtmlType() {
    return getHTMLType()
  }

  /**
   * Get HTML data type ref id
   *
   * Alias for {@link #getHTMLTypeId()}
   */
  Number getHtmlTypeId() {
    return getHTMLTypeId()
  }

  /**
   * Detect data type by passed value
   * @param value Any type
   * @return 'bool', 'refId', 'string', 'date' or 'number'
   */
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