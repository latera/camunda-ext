package org.camunda.latera.bss.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DateTimeUtil {
  static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME
  static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern('dd.MM.yyyy')
  static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern('yyyy-MM-dd HH:mm:ss')
  static final DateTimeFormatter SIMPLE_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern('dd.MM.yyyy HH:mm:ss')

  static def now() {
    return LocalDateTime.now()
  }

  static def parse(
    String dt,
    def format = this.SIMPLE_DATE_TIME_FORMAT
  ) {
    return LocalDateTime.parse(dt, format)
  }
}

