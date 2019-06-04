package org.camunda.latera.bss.utils

import org.camunda.latera.bss.utils.Numeric
import org.camunda.latera.bss.utils.StringUtil
import org.camunda.latera.bss.utils.ListUtil
import org.camunda.latera.bss.utils.MapUtil

class CSV {
  static private String  DEFAULT_DELIMITER = ';'
  static private String  DEFAULT_LINES_DELIMITER = '\n'
  static private Integer DEFAULT_SKIP_LINES = 0
  static private Boolean DEFAULT_WITH_HEADER = true
  String  delimiter
  String  linesDelimiter
  Integer skipLines
  Boolean withHeader
  private List header
  private List data

  CSV(LinkedHashMap input) {
    if (input.execution) {
      this.delimiter      = input.execution.getVariable('csvDelimiter')
      this.linesDelimiter = input.execution.getVariable('csvLinesDelimiter')
      this.skipLines      = input.execution.getVariable('csvSkipLines')
      this.withHeader     = input.execution.getVariable('csvWithHeader')
    } else {
      this.delimiter      = input.delimiter
      this.linesDelimiter = input.linesDelimiter
      this.skipLines      = input.skipLines
      this.withHeader     = input.withHeader
    }

    this.delimiter      = this.delimiter      ?: DEFAULT_DELIMITER
    this.linesDelimiter = this.linesDelimiter ?: DEFAULT_LINES_DELIMITER
    this.skipLines      = this.skipLines      ?: DEFAULT_SKIP_LINES
    this.withHeader     = this.withHeader     ?: DEFAULT_WITH_HEADER
    this.header         = []
    this.data           = []

    if (input.header) {
      this.setHeader(input.header)
    }

    if (input.data) {
      this.setData(input.data)
    }
  }

  List parseLines(def lines, Integer _skipLines = skipLines) {
    List result = []
    if (lines) {
      if (StringUtil.isString(lines)) {
        lines = lines.replace(header.join(delimiter), '').stripIndent().trim().tokenize(linesDelimiter)
      }
      if (ListUtil.isList(lines)) {
        lines.drop(skipLines).each { line ->
          List item = []
          if (StringUtil.isString(line) && StringUtil.notEmpty(line)){
            line = StringUtil.trim(line).tokenize(delimiter)
            if (notHeader(line)) {
              if (header) {
                header.eachWithIndex { column, pos ->
                  if (line.size() > pos) {
                    item += line[pos]
                  } else {
                    item += ''
                  }
                }
                result << item
              } else {
                result << line
              }
            }
          } else if (MapUtil.isMap(line)) {
            header.eachWithIndex { column, pos ->
              item << line[column]
            }
            result << item
          } else if (ListUtil.isList(line)) {
            if (notHeader(line)) {
              result << line
            }
          }
        }
      }
    }
    return result
  }

  List parseLine(def line) {
    return parseLines([line], 0)?.getAt(0)
  }

  List parseHeader(def rawHeader) {
    return parseLine(rawHeader)
  }

  Boolean isHeader(def line) {
    if (StringUtil.isString(line)) {
      return line.toString() == header.join(delimiter).toString()
    } else if (ListUtil.isList(line)) {
      return line == header
    }
    return false
  }

  Boolean notHeader(def line) {
    return !isHeader(line)
  }

  List getData() {
    return data
  }

  List toList() {
    return getData()
  }

  List getDataMap() {
    List result = []
    data.each { line ->
      LinkedHashMap item = [:]
      header.eachWithIndex { column, pos ->
        item[column] = line[pos]
      }
      result << item
    }
    return result
  }

  List toMap() {
    return getDataMap()
  }

  void setData(def lines) {
    clear()
    addLines(lines)
  }

  void clear() {
    this.data = []
  }

  def previous() {
    clear()
    return this
  }

  def negative() {
    clear()
    return this
  }

  List getHeader(def line) {
    return header
  }

  void setHeader(def line) {
    header = parseHeader(line)
  }

  List addLines(def lines) {
    data += parseLines(lines)
    return data
  }

  List addLine(def line) {
    return addLines([line])
  }

  def plus(def item) {
    if (MapUtil.isMap(item)) {
      addLine(item)
    } else {
      addLines(item)
    }
    return this
  }

  void leftShift(def line) {
    addLine(line)
  }

  List getAt(Integer i) {
    return data[i]
  }

  void putAt(Integer i, def value) {
    data[i] = parseLine(value)
  }

  Boolean isExists(Map input) {
    List result = []
    data.eachWithIndex { line, i ->
      Boolean exists = false
      if (input.indexes) {
        if (ListUtil.isList(input.indexes) && i in input.indexes) {
          exists = true
        }
      } else if (input.where) {
        Integer _skip = 0
        input.where.eachWithIndex { value, column ->
          if (line[column] == value) {
            _skip += 1
          }
        }
        if (_skip == input.where.size()) {
          exists = true
        }
      }
      if (exists) {
        return true
      }
    }
    return false
  }

  Boolean isCase(def item) {
    if (MapUtil.isMap(item)) {
      return isExistsWhere(item)
    }
    return isExists([item])
  }

  Boolean isExists(List indexes) {
    return isExists(indexes: indexes)
  }

  Boolean isExists(Integer index) {
    return isExists([index])
  }

  Boolean isExistsWhere(List where) {
    return isExists(where: where)
  }

  Boolean isExistsWhere(LinkedHashMap where) {
    return isExists(where: where)
  }

  List deleteLines(Map input) {
    List result = []
    data.eachWithIndex { line, i ->
      Boolean skip = false
      if (input.indexes) {
        if (ListUtil.isList(input.indexes) && input.indexes.contains(i)) {
          skip = true
        }
      } else if (input.where) {
        Integer _skip = 0
        if (MapUtil.isMap(input.where)) {
          input.where.each { key, value ->
            Integer pos = header.findIndexOf { it == key }
            if (pos >= 0 && line[pos].toString() == value.toString()) {
              _skip += 1
            }
          }
          if (_skip == MapUtil.keysCount(input.where)) {
            skip = true
          }
        } else if (ListUtil.isList(input.where)) {
          input.where.eachWithIndex { value, column ->
            if (line[column] == value) {
              _skip += 1
            }
          }
          if (_skip == input.where.size()) {
            skip = true
          }
        }
      }
      if (!skip) {
        result << line
      }
    }
    this.data = result
    return data
  }

  def minus(def item) {
    if (Numeric.isInteger(item)) {
      deleteLinesByIndex(item)
    } else if (ListUtil.isList(item)) {
      deleteLinesByIndex(item)
    } else if (MapUtil.isMap(item)) {
      deleteLinesWhere(item)
    }
    deleteLinesByIndex([item])
    return this
  }

  List deleteLinesByIndex(List indexes) {
    return deleteLines(indexes: indexes)
  }

  List deleteLinesByIndex(Integer index) {
    return deleteLinesByIndex([index])
  }

  List deleteLinesWhere(List where) {
    return deleteLines(where: where)
  }

  List deleteLinesWhere(LinkedHashMap where) {
    return deleteLines(where: where)
  }

  String getCsv() {
    def result = []
    if (withHeader) {
      result << header.join(delimiter)
    }
    if (data) {
      data.each { line ->
        result << line.join(delimiter)
      }
    }
    return result.join(linesDelimiter)
  }

  String toString() {
    return getCsv()
  }

  String to() {
    return getCsv()
  }

  def asType(Class target) {
    if (CharSequence in target.getInterfaces()) {
      return toString()
    }
    return toList()
  }
}
