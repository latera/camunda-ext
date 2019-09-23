package org.camunda.latera.bss.utils
import java.nio.charset.Charset
import java.security.SecureRandom

class StringUtil {
  static Boolean isString(def input) {
    return (input instanceof CharSequence)
  }

  static Boolean isEmpty(def input) {
    return !notEmpty(input)
  }

  static Boolean forceIsEmpty(def input) {
    return !forceNotEmpty(input)
  }

  static String trim(def input) {
    return input?.toString()?.stripIndent()?.trim()
  }

  static String nvl(def input) {
    String result = trim(input)
    if (result == 'null' || result == 'NULL') {
      return null
    }
    return result
  }

  static Boolean notEmpty(def input) {
    if (input) {
      if (isString(input)) {
        return (trim(input) as Boolean)
      }
      return (input as Boolean)
    }
    return false
  }

  static Boolean forceNotEmpty(def input) {
    if (input) {
      if (isString(input)) {
        return (nvl(input) as Boolean)
      }
      return (input as Boolean)
    }
    return false
  }

  static byte[] unicodeToVarchar(CharSequence input) {
    if (input) {
      return input.getBytes(Charset.forName("ISO-8859-1"))
    }
    return null
  }

  static String varcharToUnicode(CharSequence input) {
    if (input) {
      return new String(input.getBytes(Charset.forName("ISO-8859-1")), "UTF-8")
    }
    return null
  }

  static byte[] unicodeToCP1251(CharSequence input) {
    if (input) {
      return input.getBytes(Charset.forName("CP1251"))
    }
    return null
  }

  static String cp1251ToUnicode(CharSequence input) {
    if (input) {
      return new String(input.getBytes(Charset.forName("CP1251")), "UTF-8")
    }
    return null
  }

  static String camelize(CharSequence input, Boolean firstUpper = false) {
    List words = input.split(/_/);
    String first = firstUpper ? capitalize(words[0]) : words[0].toLowerCase();
    StringBuilder builder = new StringBuilder(first);
    for (int i = 1; i < words.size(); i++) {
      builder.append(capitalize(words[i]))
    }
    return builder.toString()
  }

  static String camelize(Map options, CharSequence input) {
    return camelize(input, options.firstUpper)
  }

  static String snakeCase(CharSequence input) {
    return input.replaceAll(/([A-Z])/,/_$1/).toLowerCase().replaceAll(/^_/,'').toString()
  }

  static String capitalize(CharSequence input){
    return input?.capitalize().toString()
  }

  static String decapitalize(CharSequence input){
    if (input.size() == 0) {
      return ''
    } else if (input.size() == 1) {
      return input.toLowerCase().toString()
    } else {
      return (input?.getAt(0).toLowerCase() + input?.substring(1)).toString()
    }
  }

  static String join(List input, CharSequence delimiter = ''){
    return input.join(delimiter)
  }

  static String joinNonEmpty(List input, CharSequence delimiter = ''){
    return join(input.findAll{ forceNotEmpty(it) }, delimiter)
  }

  static String random(Integer length = 6) {
    SecureRandom random = new SecureRandom()
    String result = new BigInteger(130, random).toString(32)
    return result.substring(0, length)
  }
}

