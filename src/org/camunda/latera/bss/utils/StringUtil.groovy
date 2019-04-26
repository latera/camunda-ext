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

  static String camelize(String input, Boolean firstUpper = false) {
    def words = input.split(/_/);
    String first = firstUpper ? capitalize(words[0]) : words[0].toLowerCase();
    StringBuilder builder = new StringBuilder(first);
    for (int i = 1; i < words.length; i++) {
      builder.append(capitalize(words[i]))
    }
    return builder.toString()
  }

  static String camelize(LinkedHashMap options, String input) {
    return camelize(input, options.firstUpper)
  }

  static String capitalize(String input){
    return input?.capitalize()
  }

  static String decapitalize(String input){
    if (input.length() == 0) {
      return ''
    } else if (input.length() == 1) {
      return input.toLowerCase()
    } else {
      return (input?.getAt(0).toLowerCase() + input?.substring(1)).toString()
    }
  }

  static Boolean notEmpty(def input) {
    if (input) {
      if (input instanceof String) {
        return (input.trim() as Boolean)
      }
      return (input as Boolean)
    }
    return false
  }

  static Boolean isEmpty(def input) {
    return !notEmpty(input)
  }

  static Boolean isString(def input) {
    return (input instanceof CharSequence)
  }

  static Boolean isByteArray(def input) {
    return (input instanceof byte[])
  }
}

