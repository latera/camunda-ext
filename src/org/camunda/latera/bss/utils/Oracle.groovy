package org.camunda.latera.bss.utils

import org.camunda.latera.bss.utils.DateTimeUtil

class Oracle {
  static Object encodeNull(value) {
    if (value != 'NULL' && value != null && value != '') {
      return value
    } else {
      return 'NULL'
    }
  }

  static Object decodeNull(value) {
    if (encodeNull(value) == 'NULL') {
      return null
    }
    return value
  }

  static Object encodeBool(value) {
    Object result = encodeNull(value)
    if (result != 'NULL') {
      return value ? 'Y' : 'N'
    } else {
      return result
    }
  }

  static Boolean decodeBool(value) {
    return value == 'Y'
  }

  static Object encodeFlag(value) {
    Object result = encodeNull(value)
    if (result != 'NULL') {
      return value ? 1 : 0
    } else {
      return result
    }
  }

  static Boolean decodeFlag(value) {
    return value.toString() == '1'
  }

  static Object nvl(nullable, replacement, args = []) {
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

  static Date encodeDate(input) {
    return Date.from(DateTimeUtil.local(input).toInstant())
  }

  static String encodeDateStr(input) {
    return "TO_DATE('${DateTimeUtil.local(input).format(DateTimeUtil.SIMPLE_DATE_TIME_FORMAT)}', '${DateTimeUtil.DATE_TIME_FORMAT_ORACLE}')"
  }
}