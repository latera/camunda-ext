package org.camunda.latera.bss.utils

class IO {
  static byte[] getBytes(def stream) {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream()
    int nRead;
    byte[] data = new byte[1024]
    while ((nRead = stream.read(data, 0, data.size())) != -1) {
      buffer.write(data, 0, nRead)
    }

    buffer.flush()
    byte[] byteArray = buffer.toByteArray()
    return byteArray
  }

  static InputStream getStream(byte[] bytes) {
    return new ByteArrayInputStream(bytes)
  }

  static InputStreamReader getStreamReader(byte[] bytes) {
    return new InputStreamReader(getStream(bytes))
  }
}