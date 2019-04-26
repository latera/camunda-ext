package org.camunda.latera.bss.utils

import java.io.ByteArrayOutputStream
import java.io.InputStreamReader

class IO {
  static def getBytes(
    InputStreamReader input
  ) {
        
    ByteArrayOutputStream buffer = new ByteArrayOutputStream()
    int nRead;
    byte[] data = new byte[1024]
    while ((nRead = input.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, nRead)
    }
 
    buffer.flush()
    byte[] byteArray = buffer.toByteArray()
    return byteArray
  }
}