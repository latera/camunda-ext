package org.camunda.latera.bss.utils
import java.nio.charset.Charset

class StringUtil {
  static byte[] unicodeToVarchar(String input) {
    if (input) {
      return input.getBytes(Charset.forName("ISO-8859-1"))
    }
    return null
  }

  static String varcharToUnicode(String input) {
    if (input) {
      return new String(input.getBytes(Charset.forName("ISO-8859-1")), "UTF-8")
    }
    return null
  }

  static byte[] unicodeToCP1251(String input) {
    if (input) {
      return input.getBytes(Charset.forName("CP1251"))
    }
    return null
  }

  static String cp1251ToUnicode(String input) {
    if (input) {
      return new String(input.getBytes(Charset.forName("CP1251")), "UTF-8")
    }
    return null
  }

  static Boolean notEmpty(String input) {
    if (input) {
      return (input?.trim() as boolean)
    }
    return false
  }

  static Boolean isEmpty(String input) {
    return !notEmpty(input)
  }

  static Boolean isString(def input) {
    return (input instanceof CharSequence)
  }

  static Boolean isByteArray(def input) {
    return (input instanceof byte[])
  }
}

