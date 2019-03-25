package org.camunda.latera.bss.utils

import java.util.Locale
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class DateTimeUtil {
  static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME
  static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern('yyyy-MM-dd')
  static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern('yyyy-MM-dd HH:mm:ss')
  static final DateTimeFormatter FULL_DATE_FORMAT = DateTimeFormatter.ofPattern('dd MMMM yyyy')
  static final DateTimeFormatter SIMPLE_DATE_FORMAT = DateTimeFormatter.ofPattern('dd.MM.yyyy')
  static final DateTimeFormatter SIMPLE_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern('dd.MM.yyyy HH:mm:ss')
  static final String DATE_TIME_FORMAT_ORACLE = 'DD.MM.YYYY HH24:MI:SS'

  static LocalDateTime now() {
    return LocalDateTime.now()
  }

  static LocalDateTime parse(
    String input,
    def format = this.SIMPLE_DATE_TIME_FORMAT
  ) {
    return LocalDateTime.parse(input, format)
  }

  static String format(
    Date input,
    def format = this.SIMPLE_DATE_TIME_FORMAT,
    String locale = 'en'
  ) {
    return input.format(format.withLocale(new Locale(locale)))
  }

  static String format(
    LocalDateTime input,
    def format = this.SIMPLE_DATE_TIME_FORMAT,
    String locale = 'en'
  ) {
    return input.format(format.withLocale(new Locale(locale)))
  }

  static String format(
    ZonedDateTime input,
    def format = this.SIMPLE_DATE_TIME_FORMAT,
    String locale = 'en'
  ) {
    return input.format(format.withLocale(new Locale(locale)))
  }

  static String format(LinkedHashMap parameters, Date input) {
    def params = [
      format: this.SIMPLE_DATE_TIME_FORMAT,
      locale: 'en'
    ] + parameters
    return format(input, params.format, params.locale)
  }

  static String format(LinkedHashMap parameters, LocalDateTime input) {
    def params = [
      format: this.SIMPLE_DATE_TIME_FORMAT,
      locale: 'en'
    ] + parameters
    return format(input, params.format, params.locale)
  }

  static String format(LinkedHashMap parameters, ZonedDateTime input) {
    def params = [
      format: this.SIMPLE_DATE_TIME_FORMAT,
      locale: 'en'
    ] + parameters
    return format(input, params.format, params.locale)
  }

  static ZonedDateTime atZone(Date input, ZoneId zone) {
    return input.toInstant().atZone(zone)
  }

  static ZonedDateTime atZone(LocalDateTime input = now(), ZoneId zone) {
    return input.atZone(zone)
  }

  static ZonedDateTime local(Date input) {
    return atZone(input, ZoneId.systemDefault())
  }

  static ZonedDateTime local(LocalDateTime input = now()) {
    return atZone(input, ZoneId.systemDefault())
  }

  static ZonedDateTime local(ZonedDateTime input) {
    return input
  }

  static LocalDateTime nextSecond(LocalDateTime input = now()) {
    return input.plusSeconds(1)
  }

  static ZonedDateTime nextSecond(ZonedDateTime input) {
    return input.plusSeconds(1)
  }

  static LocalDateTime prevSecond(LocalDateTime input = now()) {
    return input.minusSeconds(1)
  }

  static ZonedDateTime prevSecond(ZonedDateTime input) {
    return input.minusSeconds(1)
  }

  static LocalDateTime dayBegin(LocalDateTime input = now()) {
    return input.truncatedTo(ChronoUnit.DAYS)
  }

  static ZonedDateTime dayBegin(ZonedDateTime input) {
    return input.truncatedTo(ChronoUnit.DAYS)
  }

  static LocalDateTime monthBegin(LocalDateTime input = now()) {
    return input.truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1)
  }

  static ZonedDateTime monthBegin(ZonedDateTime input) {
    return input.truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1)
  }

  static LocalDateTime dayEnd(LocalDateTime input = now()) {
    return input.truncatedTo(ChronoUnit.DAYS).plusDays(1).minusSeconds(1)
  }

  static ZonedDateTime dayEnd(ZonedDateTime input) {
    return input.truncatedTo(ChronoUnit.DAYS).plusDays(1).minusSeconds(1)
  }

  static LocalDateTime monthEnd(LocalDateTime input = now()) {
    return input.truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1).plusMonths(1).minusSeconds(1)
  }

  static ZonedDateTime monthEnd(ZonedDateTime input) {
    return input.truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1).plusMonths(1).minusSeconds(1)
  }

  static Boolean isDate(input) {
    return (input instanceof Date) || (input instanceof LocalDateTime) || (input instanceof ZonedDateTime)
  }
}
