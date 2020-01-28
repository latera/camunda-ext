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
  String getStringType() {
    return getRefCode(getStringTypeId())
  }

  Number getStringTypeId() {
    return DATA_TYPE_Varchar
  }

  String getCharType() {
    return getRefCode(getCharTypeId())
  }

  Number getCharTypeId() {
    return DATA_TYPE_Char
  }

  String getFloatType() {
    return getRefCode(getFloatTypeId())
  }

  Number getFloatTypeId() {
    return DATA_TYPE_Number
  }

  String getIntegerType() {
    return getRefCode(getIntegerTypeId())
  }

  Number getIntegerTypeId() {
    return DATA_TYPE_Integer
  }

  String getDateType() {
    return getRefCode(getDateTypeId())
  }

  Number getDateTypeId() {
    return DATA_TYPE_Date
  }

  String getDateTimeType() {
    return getDateType()
  }

  Number getDateTimeTypeId() {
    return getDateTypeId()
  }

  String getRefType() {
    return getRefCode(getRefTypeId())
  }

  Number getRefTypeId() {
    return DATA_TYPE_Ref
  }

  String getBooleanType() {
    return getRefCode(getBooleanTypeId())
  }

  Number getBooleanTypeId() {
    return DATA_TYPE_Flag
  }

  String getBoolType() {
    return getBooleanType()
  }

  Number getBoolTypeId() {
    return getBooleanTypeId()
  }

  String getCLOBType() {
    return getRefCode(getCLOBTypeId())
  }

  Number getCLOBTypeId() {
    return DATA_TYPE_Clob
  }

  String getClobType() {
    return getCLOBType()
  }

  Number getClobTypeId() {
    return getCLOBTypeId()
  }

  String getHTMLType() {
    return getRefCode(getHTMLTypeId())
  }

  Number getHTMLTypeId() {
    return DATA_TYPE_HTML
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