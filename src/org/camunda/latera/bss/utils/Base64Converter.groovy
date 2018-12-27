package org.camunda.latera.bss.utils
import java.util.Base64

class Base64Converter {
  static String to(byte[] bytes) {
    return Base64.getEncoder().encodeToString(bytes)
  }

  static byte[] from(String str) {
    return Base64.getDecoder().decode(str)
  }
}