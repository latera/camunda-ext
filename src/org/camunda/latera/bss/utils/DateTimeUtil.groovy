package org.camunda.latera.bss.utils

import java.util.Locale
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.Temporal
import java.time.temporal.ChronoUnit

class DateTimeUtil {
  static final DateTimeFormatter ISO_FORMAT       = DateTimeFormatter.ISO_OFFSET_DATE_TIME
  static final DateTimeFormatter ISO_FORMAT_NO_TZ = DateTimeFormatter.ISO_LOCAL_DATE_TIME
  static final DateTimeFormatter DATE_FORMAT      = DateTimeFormatter.ofPattern('yyyy-MM-dd')
  static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern('yyyy-MM-dd HH:mm:ss')
  static final DateTimeFormatter FULL_DATE_FORMAT = DateTimeFormatter.ofPattern('dd MMMM yyyy')
  static final DateTimeFormatter SIMPLE_DATE_FORMAT      = DateTimeFormatter.ofPattern('dd.MM.yyyy')
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

  static LocalDateTime parseIsoNoTZ(
    String input,
    def format = this.ISO_FORMAT_NO_TZ
  ) {
    return ZonedDateTime.parse(input, format)
  }

  static ZonedDateTime parseIso(
    String input,
    def format = this.ISO_FORMAT
  ) {
    return ZonedDateTime.parse(input, format)
  }

  static String format(
    Date input,
    def format = this.SIMPLE_DATE_FORMAT,
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
      format: this.SIMPLE_DATE_FORMAT,
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

  static String iso(Date input) {
    return local(input).format(this.ISO_FORMAT_NO_TZ)
  }

  static String iso(LocalDate input) {
    return input.format(this.ISO_FORMAT_NO_TZ)
  }

  static String iso(LocalDateTime input) {
    return input.format(this.ISO_FORMAT_NO_TZ)
  }

  static String iso(ZonedDateTime input) {
    return input.format(this.ISO_FORMAT)
  }

  static ZonedDateTime atZone(Date input, ZoneId zone) {
    return input.toInstant().atZone(zone)
  }

  static ZonedDateTime atZone(LocalDate input , ZoneId zone) {
    return input.atStartOfDay().atZone(zone)
  }

  static ZonedDateTime atZone(LocalDateTime input = now(), ZoneId zone) {
    return input.atZone(zone)
  }

  static ZonedDateTime local(Date input) {
    return atZone(input, ZoneId.systemDefault())
  }

  static ZonedDateTime local(LocalDate input) {
    return atZone(input, ZoneId.systemDefault())
  }

  static ZonedDateTime local(LocalDateTime input = now()) {
    return atZone(input, ZoneId.systemDefault())
  }

  static ZonedDateTime local(ZonedDateTime input) {
    return input.withZoneSameInstant(ZoneId.systemDefault())
  }

  static Temporal nextSecond(Temporal input) {
    return input.plusSeconds(1)
  }

  static ZonedDateTime nextSecond() {
    return nextSecond(local())
  }

  static Temporal prevSecond(Temporal input) {
    return input.minusSeconds(1)
  }

  static ZonedDateTime prevSecond() {
    return prevSecond(local())
  }

  static LocalDateTime dayBegin(Date input) {
    return local(input).toLocalDateTime()
  }

  static Temporal dayBegin(Temporal input) {
    return input.truncatedTo(ChronoUnit.DAYS)
  }

  static ZonedDateTime dayBegin() {
    return dayBegin(local())
  }

  static Temporal monthBegin(Temporal input) {
    return input.truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1)
  }

  static ZonedDateTime monthBegin() {
    return monthBegin(local())
  }

  static Temporal dayEnd(Temporal input) {
    return input.truncatedTo(ChronoUnit.DAYS).plusDays(1).minusSeconds(1)
  }

  static ZonedDateTime dayEnd() {
    return dayEnd(local())
  }

  static Temporal monthEnd(Temporal input) {
    return input.truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1).plusMonths(1).minusSeconds(1)
  }

  static Integer secondsBetween(Temporal firstDate, Temporal secondDate) {
    return firstDate.until(secondDate, ChronoUnit.SECONDS)
  }

  static Integer minutesBetween(Temporal firstDate, Temporal secondDate) {
    return firstDate.until(secondDate, ChronoUnit.MINUTES)
  }

  static Integer hoursBetween(Temporal firstDate, Temporal secondDate) {
    return firstDate.until(secondDate, ChronoUnit.HOURS)
  }

  static Integer daysBetween(Temporal firstDate, Temporal secondDate) {
    return firstDate.until(secondDate, ChronoUnit.DAYS)
  }

  static Integer weeksBetween(Temporal firstDate, Temporal secondDate) {
    return firstDate.until(secondDate, ChronoUnit.WEEKS)
  }

  static Integer monthsBetween(Temporal firstDate, Temporal secondDate) {
    return firstDate.until(secondDate, ChronoUnit.MONTHS)
  }

  static Integer yearsBetween(Temporal firstDate, Temporal secondDate) {
    return firstDate.until(secondDate, ChronoUnit.YEARS)
  }

  static Boolean isDate(input) {
    return (input instanceof Date) || (input instanceof LocalDate) || (input instanceof LocalDateTime) || (input instanceof ZonedDateTime)
  }

  static Boolean isDateTime(input) {
    return (input instanceof LocalDateTime) || (input instanceof ZonedDateTime)
  }
}
