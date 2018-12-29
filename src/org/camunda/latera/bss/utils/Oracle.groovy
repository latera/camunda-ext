package org.camunda.latera.bss.utils

class Oracle {
  static Object encodeNull(value) {
    if (value != 'NULL' && value != null) {
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
    return value == 1
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
}