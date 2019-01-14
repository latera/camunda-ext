package org.camunda.latera.bss.util

class Numeric {
  static Object toIntSafe(Object value, Object defaultValue = null) {
    if (!value.respondsTo("toBigInteger")) {
      return defaultValue
    } else try {
      return value.toBigInteger()
    }
    catch (NumberFormatException e) {
      return defaultValue
    }
  }

  static Object toFloatSafe(Object value, Object defaultValue = null) {
    if (!value.respondsTo("toBigDecimal")) {
      return defaultValue
    } else try {
      return value.toBigDecimal()
    }
    catch (NumberFormatException e) {
      return defaultValue
    }
  }
}
