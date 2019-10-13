package org.camunda.latera.bss.utils

class Base64Converter {
  static String to(byte[] bytes) {
    return Base64.getEncoder().encodeToString(bytes)
  }

  static String to(CharSequence str) {
    return Base64.getEncoder().encodeToString(str.getBytes())
  }

  static byte[] from(CharSequence str) {
    return Base64.getDecoder().decode(str)
  }
}