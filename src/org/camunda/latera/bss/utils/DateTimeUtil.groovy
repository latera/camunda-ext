package org.camunda.latera.bss.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import groovy.time.TimeCategory

class DateTimeUtil {
  static DateTimeFormatter ISOFormat = DateTimeFormatter.ISO_OFFSET_DATE_TIME
  static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern('dd.MM.yyyy')
  static DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern('yyyy-MM-dd HH:mm:ss')
  static DateTimeFormatter simpleDateTimeFormat = DateTimeFormatter.ofPattern('dd.MM.yyyy HH:mm:ss')

  static def now() {
    return LocalDateTime.now()
  }

  static def parse(
    String dt,
    def format = this.simpleDateTimeFormat
  ) {
    return LocalDateTime.parse(dt, format)
  }
}

