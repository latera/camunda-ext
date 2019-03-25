package org.camunda.latera.bss.utils

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
      return value.toBigDecimal()
    }
    catch (NumberFormatException e) {
      return defaultValue
    }
  }
}
