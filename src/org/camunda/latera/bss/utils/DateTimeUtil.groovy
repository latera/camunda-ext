package org.camunda.latera.bss.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.Temporal
import java.time.temporal.ChronoUnit

class DateTimeUtil {
  static DateTimeFormatter ISO_FORMAT              = DateTimeFormatter.ISO_OFFSET_DATE_TIME
  static DateTimeFormatter ISO_FORMAT_NO_TZ        = DateTimeFormatter.ISO_LOCAL_DATE_TIME
  static DateTimeFormatter ISO_DATE_FORMAT         = DateTimeFormatter.ISO_OFFSET_DATE
  static DateTimeFormatter ISO_DATE_FORMAT_NO_TZ   = DateTimeFormatter.ISO_LOCAL_DATE
  static DateTimeFormatter DATE_FORMAT             = DateTimeFormatter.ofPattern('yyyy-MM-dd')
  static DateTimeFormatter DATE_TIME_FORMAT        = DateTimeFormatter.ofPattern('yyyy-MM-dd HH:mm:ss')
  static DateTimeFormatter FULL_DATE_FORMAT        = DateTimeFormatter.ofPattern('dd MMMM yyyy')
  static DateTimeFormatter DAY_FORMAT              = DateTimeFormatter.ofPattern('dd')
  static DateTimeFormatter MONTH_FORMAT            = DateTimeFormatter.ofPattern('MM')
  static DateTimeFormatter MONTH_FULL_FORMAT       = DateTimeFormatter.ofPattern('MMMM')
  static DateTimeFormatter YEAR_FORMAT             = DateTimeFormatter.ofPattern('yyyy')
  static DateTimeFormatter SIMPLE_DATE_FORMAT      = DateTimeFormatter.ofPattern('dd.MM.yyyy')
  static DateTimeFormatter SIMPLE_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern('dd.MM.yyyy HH:mm:ss')
  static String DATE_TIME_FORMAT_ORACLE            = 'DD.MM.YYYY HH24:MI:SS'

  static LocalDateTime now() {
    return LocalDateTime.now()
  }

  static def parseDateTimeAny(def input) {
    def result = null
    try {
      result = parseDateTimeIso(input)
    } catch (Exception e) {
      result = null
    }
    if (result) {
      return result
    }

    try {
      result = parseDateTime(input)
    } catch (Exception e) {
      result = null
    }
    if (result) {
      return result
    }

    try {
      result = parseDate(input)
    } catch (Exception e) {
      result = null
    }

    return result
  }

  static def parseDateTimeIso(CharSequence input) {
    def result = null
    try {
      result = parseIso(input)
    } catch (Exception e) {
      result = null
    }
    if (result) {
      return result
    }

    try {
      result = parseIsoNoTZ(input)
    } catch (Exception e) {
      result = null
    }
    return result
  }

  static def parseDateTime(CharSequence input) {
    def result = null
    [
      DATE_TIME_FORMAT,
      SIMPLE_DATE_TIME_FORMAT
    ].each {format ->
      if (!result) {
        try {
          result = parse(input, format)
        } catch (Exception e) {
          result = null
        }
      }
    }
    return result
  }

  static LocalDateTime parse(
    CharSequence input,
    def format = this.SIMPLE_DATE_TIME_FORMAT
  ) {
    return LocalDateTime.parse(input, format)
  }

  static LocalDateTime parseDate(CharSequence input) {
    def result = null
    [
      DATE_FORMAT,
      SIMPLE_DATE_FORMAT
    ].each { def format ->
      if (!result) {
        try {
          result = parse(input, format)
        } catch (Exception e) {
          result = null
        }
      }
    }
    return result
  }

  static LocalDateTime parseIsoNoTZ(CharSequence input) {
    def result = null
    [
      ISO_FORMAT_NO_TZ,
      ISO_DATE_FORMAT_NO_TZ
    ].each { def format ->
      if (!result) {
        try {
          result = parse(input, format)
        } catch (Exception e) {
          result = null
        }
      }
    }
    return result
  }

  static ZonedDateTime parseIso(CharSequence input) {
    def result = null
    [
      ISO_FORMAT,
      ISO_DATE_FORMAT
    ].each { def format ->
      if (!result) {
        try {
          result = ZonedDateTime.parse(input, format)
        } catch (Exception e) {
          result = null
        }
      }
    }
    return result
  }

  static String format(
    Date input,
    def format = this.SIMPLE_DATE_FORMAT,
    CharSequence locale = 'en'
  ) {
    return local(input).format(format.withLocale(new Locale(locale)))
  }

  static String format(
    LocalDateTime input,
    def format = this.SIMPLE_DATE_TIME_FORMAT,
    CharSequence locale = 'en'
  ) {
    return input.format(format.withLocale(new Locale(locale)))
  }

  static String format(
    ZonedDateTime input,
    def format = this.SIMPLE_DATE_TIME_FORMAT,
    CharSequence locale = 'en'
  ) {
    return input.format(format.withLocale(new Locale(locale)))
  }

  static String format(LinkedHashMap parameters, Date input) {
    LinkedHashMap params = [
      format: this.SIMPLE_DATE_FORMAT,
      locale: 'en'
    ] + parameters
    return format(input, params.format, params.locale)
  }

  static String format(LinkedHashMap parameters, LocalDateTime input) {
    LinkedHashMap params = [
      format: this.SIMPLE_DATE_TIME_FORMAT,
      locale: 'en'
    ] + parameters
    return format(input, params.format, params.locale)
  }

  static String format(LinkedHashMap parameters, ZonedDateTime input) {
    LinkedHashMap params = [
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

  static Temporal nextMinute(Temporal input) {
    return input.plusMinutes(1)
  }

  static ZonedDateTime nextMinute() {
    return nextMinute(local())
  }

  static Temporal prevMinute(Temporal input) {
    return input.minusMinutes(1)
  }

  static ZonedDateTime prevMinute() {
    return prevMinute(local())
  }

  static Temporal nextHour(Temporal input) {
    return input.plusHours(1)
  }

  static ZonedDateTime nextHour() {
    return nextHour(local())
  }

  static Temporal prevHour(Temporal input) {
    return input.minusHours(1)
  }

  static ZonedDateTime prevHour() {
    return prevHour(local())
  }

  static Temporal nextDay(Temporal input) {
    return input.plusDays(1)
  }

  static ZonedDateTime nextDay() {
    return nextDay(local())
  }

  static ZonedDateTime tomorrow() {
    return nextDay()
  }

  static Temporal prevDay(Temporal input) {
    return input.minusDays(1)
  }

  static ZonedDateTime prevDay() {
    return prevDay(local())
  }

  static Temporal nextWeek(Temporal input) {
    return input.plusWeeks(1)
  }

  static ZonedDateTime nextWeek() {
    return nextWeek(local())
  }

  static Temporal prevWeek(Temporal input) {
    return input.minusWeeks(1)
  }

  static ZonedDateTime prevWeek() {
    return prevWeek(local())
  }

  static Temporal nextMonth(Temporal input) {
    return input.plusMonths(1)
  }

  static ZonedDateTime nextMonth() {
    return nextMonth(local())
  }

  static Temporal prevMonth(Temporal input) {
    return input.minusMonths(1)
  }

  static ZonedDateTime prevMonth() {
    return prevMonth(local())
  }

  static Temporal nextYear(Temporal input) {
    return input.plusYears(1)
  }

  static ZonedDateTime nextYear() {
    return nextYear(local())
  }

  static Temporal prevYear(Temporal input) {
    return input.minusYears(1)
  }

  static ZonedDateTime prevYear() {
    return prevYear(local())
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

  static ZonedDateTime monthEnd() {
    return monthEnd(local())
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

  static Boolean isDate(def input) {
    return (input instanceof Date) || (input instanceof Temporal)
  }

  static Boolean isDateStr(CharSequence input) {
    return parseDate(input) != null
  }

  static Boolean isDateTime(def input) {
    return (input instanceof LocalDateTime) || (input instanceof ZonedDateTime)
  }

  static Boolean isDateTimeStr(CharSequence input) {
    return parseDateTime(input) != null
  }

  static Boolean isDateTimeIso(CharSequence input) {
    return parseDateTimeIso(input) != null
  }
}
