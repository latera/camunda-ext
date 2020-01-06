package org.camunda.latera.bss.utils
import java.nio.charset.Charset
import java.security.SecureRandom
import static org.camunda.latera.bss.utils.Numeric.isNumber
import static org.camunda.latera.bss.utils.DateTimeUtil.isDate

class StringUtil {
  /**
    Check if input is String or GString.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.StringUtilSpec.html#%23isString"></iframe>
    @param input Any object for type check
  */
  static Boolean isString(def input) {
    return (input instanceof CharSequence)
  }

  /**
    Check if input is empty String (w/o space and other symbols) or null.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.StringUtilSpec.html#%23isEmpty"></iframe>
    @param input String to check emptiness
  */
  static Boolean isEmpty(def input) {
    return !notEmpty(input)
  }

  /**
    Check if input is not empty String (w/o space and other symbols) or null.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.StringUtilSpec.html#%23notEmpty"></iframe>
    @param input String to check emptiness
  */
  static Boolean notEmpty(def input) {
    if (input != null) {
      if (isString(input)) {
        return !!(trim(input) as Boolean)
      }
      if (isNumber(input)) {
        return true // 0 is not empty value
      }
      if (isDate(input)) {
        return true // date values cannout be empty
      }
      if (input instanceof Boolean) {
        return true // false is not empty value
      }
      return !!(input as Boolean)
    }
    return false
  }

  /**
    Check if input is empty String (w/o space and other symbols) or null.
    <p>
    Like #isEmpty() but also determine 'null' or 'NULL' as empty strings. Used in HID class.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.StringUtilSpec.html#%23forceIsEmpty"></iframe>
    @param input String to check emptiness
    @see #isEmpty(def)
  */
  static Boolean forceIsEmpty(def input) {
    return !forceNotEmpty(input)
  }


  /**
    Check if input is not empty String (w/o space and other symbols) or null.
    <p>
    Like #notEmpty() but also determine 'null' or 'NULL' as empty strings. Used in HID class.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.StringUtilSpec.html#%23forceNotEmpty"></iframe>
    @param input String to check emptiness
    @see #notEmpty(def)
  */
  static Boolean forceNotEmpty(def input) {
    if (input != null) {
      if (isString(input)) {
        return !!(nvl(input) as Boolean)
      }
      if (isNumber(input)) {
        return true // 0 is not empty value
      }
      if (isDate(input)) {
        return true // date values cannout be empty
      }
      if (input instanceof Boolean) {
        return true // false is not empty value
      }
      return !!(input as Boolean)
    }
    return false
  }

  /**
    Trim spaces or other indent symbols from begin and end of String. Non-string values returns as String except null
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.StringUtilSpec.html#%23trim"></iframe>
    @param input String for trimming
    @see #isEmpty(def)
  */
  static String trim(def input) {
    return input?.toString()?.stripIndent()?.trim()
  }

  /**
    Convert 'null' and 'NULL' Strings to null value
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.StringUtilSpec.html#%23nvl"></iframe>
    @param input String for null coerce
  */
  static String nvl(def input) {
    String result = trim(input)
    if (result == 'null' || result == 'NULL') {
      return null
    }
    return result
  }

  /**
    Convert 'null', 'NULL' and empty Strings to null value
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.StringUtilSpec.html#%23forceNvl"></iframe>
    @param input String for null coerce
    @see #forceIsEmpty(def)
  */
  static String forceNvl(def input) {
    String result = nvl(input)
    if (isEmpty(result)) {
      return null
    }
    return result
  }

  /**
    Convert UTF-8 string to Oracle charset.
    Used by HID class.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.StringUtilSpec.html#%23unicodeToVarchar"></iframe>
    @param input String to convert
  */
  static byte[] unicodeToVarchar(CharSequence input) {
    if (input) {
      return input.getBytes(Charset.forName("ISO-8859-1"))
    }
    return null
  }

  /**
    Convert Oracle string UTF-8 charset.
    Used by HID class.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.StringUtilSpec.html#%23varcharToUnicode"></iframe>
    @param input String to convert
  */
  static String varcharToUnicode(CharSequence input) {
    if (input) {
      return new String((input ?: '').getBytes(Charset.forName("ISO-8859-1")), "UTF-8")
    }
    return null
  }

  /**
    Convert UTF-8 string to CP-1251 charset.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.StringUtilSpec.html#%23unicodeToCP1251"></iframe>
    @param input String to convert
  */
  static byte[] unicodeToCP1251(CharSequence input) {
    if (input) {
      return input.getBytes(Charset.forName("CP1251"))
    }
    return null
  }

  /**
    Convert CP-1251 string UTF-8 charset.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.StringUtilSpec.html#%23cp1251ToUnicode"></iframe>
    @param input String to convert
  */
  static String cp1251ToUnicode(CharSequence input) {
    if (input) {
      return new String((input ?: '').getBytes(Charset.forName("CP1251")), "UTF-8")
    }
    return null
  }

  /**
    Convert snake_case to camelCase or CamelCase
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.StringUtilSpec.html#%23camelize+without+firstUpper"></iframe>
    @param input String for camelize
    @param firstUpper If true return first letter capital otherwise not
  */
  static String camelize(CharSequence input, Boolean firstUpper = false) {
    List words = trim(input).split(/_/);
    String first = firstUpper ? capitalize(words[0]) : decapitalize(words[0]);
    StringBuilder builder = new StringBuilder(first);
    for (int i = 1; i < words.size(); i++) {
      builder.append(capitalize(words[i]))
    }
    return builder.toString()
  }

  /**
    Alias for #camelize with named args.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.StringUtilSpec.html#%23camelize+with+named+arguments+-+without+firstUpper"></iframe>
    @param input String for camelize
    @param firstUpper Whether return first letter capital or not
    @see #camelize(CharSequence,Boolean)
  */
  static String camelize(Map options, CharSequence input) {
    return camelize(input, options.firstUpper)
  }

  /**
    Convert camelCase or CamelCase to snake_case
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.StringUtilSpec.html#%23snakeCase"></iframe>
    @param input String to convert to snake case
  */
  static String snakeCase(CharSequence input) {
    return input.replaceAll(/([A-Z])/,/_$1/).toLowerCase().replaceAll(/^_/,'').toString()
  }

  /**
    Convert first word letter to capital one
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.StringUtilSpec.html#%23capitalize"></iframe>
    @param input String to capitalize
  */
  static String capitalize(CharSequence input){
    return input?.capitalize().toString()
  }

  /**
    Convert first word letters to lower case
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.StringUtilSpec.html#%23decapitalize"></iframe>
    @param input String to decapitalize
  */
  static String decapitalize(CharSequence input){
    if (input.size() == 0) {
      return ''
    } else if (input.size() == 1) {
      return input.toString().toLowerCase()
    } else {
      return (input?.getAt(0).toLowerCase() + input?.substring(1)).toString()
    }
  }

  /**
    Join list or array items with delimiter
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.StringUtilSpec.html#%23join"></iframe>
    @param input List to join
    @param delimiter Delimiter, default ''
  */
  static String join(List input, CharSequence delimiter = ''){
    return input.join(delimiter)
  }

  /**
    Join list or array non-empty, non-null values or non-'null' string items with delimiter
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.StringUtilSpec.html#%23join"></iframe>
    @param input List to join
    @param delimiter Delimiter, default ''
  */
  static String joinNonEmpty(List input, CharSequence delimiter = ''){
    return join(input.findAll{ forceNotEmpty(it) }, delimiter)
  }

  /**
    Generate random string with specified length
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.StringUtilSpec.html#%23random"></iframe>
    @param length String length, default 6
  */
  static String random(Integer length = 6) {
    SecureRandom random = new SecureRandom()
    String result = new BigInteger(130, random).toString(32)
    return result.substring(0, length)
  }
}

