package org.camunda.latera.bss.utils

import static org.camunda.latera.bss.utils.DateTimeUtil.*
import static org.camunda.latera.bss.utils.StringUtil.forceNotEmpty

class Oracle {
  static def encodeNull(def value) {
    if (forceNotEmpty(value)) {
      return value
    } else {
      return 'NULL'
    }
  }

  static def decodeNull(def value) {
    if (encodeNull(value) == 'NULL') {
      return null
    }
    return value
  }

  static def encodeBool(def value) {
    def result = encodeNull(value)
    if (result != 'NULL') {
      return value ? 'Y' : 'N'
    } else {
      return result
    }
  }

  static Boolean decodeBool(def value) {
    return value == 'Y'
  }

  static def encodeFlag(def value) {
    def result = encodeNull(value)
    if (result != 'NULL') {
      return value ? 1 : 0
    } else {
      return result
    }
  }

  static Boolean decodeFlag(value) {
    return value.toString() == '1'
  }

  static def nvl(def nullable, def replacement, List args = []) {
    Boolean result = decodeNull(nullable)
    if (result != null) {
      return result
    } else {
      if (replacement.metaClass.respondsTo(replacement, 'call')) {
        return replacement.call(*args)
      } else {
        return replacement
      }
    }
  }

  static Date encodeDate(def input) {
    return Date.from(local(input).toInstant())
  }

  static String encodeDateStr(def input) {
    return "TO_DATE('${format(local(input), SIMPLE_DATE_TIME_FORMAT)}', '${DATE_TIME_FORMAT_ORACLE}')"
  }
}