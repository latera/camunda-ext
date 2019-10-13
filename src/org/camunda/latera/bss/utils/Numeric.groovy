package org.camunda.latera.bss.utils

import java.math.*
import static org.camunda.latera.bss.utils.StringUtil.isString

class Numeric {
  /**
    Convert input to BigInt or return default value.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.NumericSpec.html#%23toIntSafe+without+default+value"></iframe>
    @param value Any object to convert
    @param defaultValue If not possible to convert, value to return. Default null
  */
  static def toIntSafe(def value, def defaultValue = null) {
    if (!value.respondsTo("toBigInteger")) {
      return defaultValue
    }

    try {
      return value.toBigInteger()
    }
    catch (NumberFormatException e) {
      return defaultValue
    }
  }

  /**
    Convert input to BigDecimal or return default value.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.NumericSpec.html#%23toFloatSafe+without+default+value"></iframe>
    @param value Any object to convert
    @param defaultValue If not possible to convert, value to return. Default null
  */
  static def toFloatSafe(def value, def defaultValue = null) {
    if (!value.respondsTo("toBigDecimal")) {
      return defaultValue
    }

    try {
      if (isString(value)) {
        value = value.replace(',', '.')
      }
      return value.toBigDecimal()
    }
    catch (NumberFormatException e) {
      return defaultValue
    }
  }

  /**
    Check whether input is Integer or not.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.NumericSpec.html#%23isInteger"></iframe>
    @param value Any object to check
  */
  static Boolean isInteger(def value) {
    return toIntSafe(value, null) != null
  }

  /**
    Check whether input is Float or not.
    <p>
    Alias for #isInteger()
    @param value Any object to check
    @see #isInteger(def)
  */
  static Boolean isInt(def value) {
    return isInteger(value)
  }

  /**
    Check whether input is Float or not.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.NumericSpec.html#%23isFloat"></iframe>
    @param value Any object to check
  */
  static Boolean isFloat(def value) {
    return toFloatSafe(value, null) != null
  }

  /**
    Check whether input is Number (Integer or Float) or not.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.NumericSpec.html#%23isNumber"></iframe>
    @param value Any object to check
  */
  static Boolean isNumber(def value) {
    return isInteger(value) || isFloat(value)
  }

  /**
    Round number to specified digits after delimiter.
    <p>
    If argument cannot be converted into BigDecimal, 0 returned.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.NumericSpec.html#%23round"></iframe>
    @param number Float to round
    @param digits Digits number. Default 2
  */
  static BigDecimal round(def number, Integer digits = 2) {
    return toFloatSafe(number, new BigDecimal(0)).setScale(digits, BigDecimal.ROUND_HALF_UP)
  }

  /**
    Return maximal value between two numbers.
    <p>
    If argument cannot be converted into BigDecimal, 0 is used instead.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.NumericSpec.html#%23max"></iframe>
    @param first First number
    @param second Second number
  */
  static BigDecimal max(def first, def second) {
    return toFloatSafe(first, new BigDecimal(0)).max(toFloatSafe(second, new BigDecimal(0)))
  }

  /**
    Return minimal value between two numbers.
    <p>
    If argument cannot be converted into BigDecimal, 0 is used instead.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.NumericSpec.html#%23min"></iframe>
    @param first First number
    @param second Second number
  */
  static BigDecimal min(def first, def second) {
    return toFloatSafe(first, new BigDecimal(0)).min(toFloatSafe(second, new BigDecimal(0)))
  }
}
