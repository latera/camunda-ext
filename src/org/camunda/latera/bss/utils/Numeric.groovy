package org.camunda.latera.bss.utils

import java.math.*
import org.camunda.latera.bss.utils.StringUtil

class Numeric {
  static def toIntSafe(def value, def defaultValue = null) {
    if (!value.respondsTo("toBigInteger")) {
      return defaultValue
    } else try {
      return value.toBigInteger()
    }
    catch (NumberFormatException e) {
      return defaultValue
    }
  }

  static def toFloatSafe(def value, def defaultValue = null) {
    if (!value.respondsTo("toBigDecimal")) {
      return defaultValue
    } else try {
      if (StringUtil.isString(value)) {
        value = value.replace(',', '.')
      }
      return value.toBigDecimal()
    }
    catch (NumberFormatException e) {
      return defaultValue
    }
  }

  static def isInteger(def value) {
    return toIntSafe(value, null) != null
  }

  static def isInt(def value) {
    return isInteger(value)
  }

  static def isFloat(def value) {
    return toFloatSafe(value, null) != null
  }

  static def isNumber(def value) {
    return isInteger(value) || isFloat(value)
  }

  static BigDecimal round(def number, Integer digits = 2) {
    return toFloatSafe(number, 0).setScale(digits, BigDecimal.ROUND_HALF_UP)
  }

  static BigDecimal max(def first, def second) {
    return toFloatSafe(first, 0).max(toFloatSafe(second, 0))
  }

  static BigDecimal min(def first, def second) {
    return toFloatSafe(first, 0).min(toFloatSafe(second, 0))
  }
}
