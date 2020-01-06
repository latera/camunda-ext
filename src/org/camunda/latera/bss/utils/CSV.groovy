package org.camunda.latera.bss.utils

import static org.camunda.latera.bss.utils.Numeric.isInteger
import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import static org.camunda.latera.bss.utils.StringUtil.isString
import static org.camunda.latera.bss.utils.StringUtil.trim
import static org.camunda.latera.bss.utils.StringUtil.notEmpty
import static org.camunda.latera.bss.utils.StringUtil.forceIsEmpty
import static org.camunda.latera.bss.utils.StringUtil.forceNvl
import static org.camunda.latera.bss.utils.StringUtil.join
import static org.camunda.latera.bss.utils.ListUtil.isList
import static org.camunda.latera.bss.utils.MapUtil.isMap
import static org.camunda.latera.bss.utils.MapUtil.keysList
import static org.camunda.latera.bss.utils.MapUtil.keysCount
import static org.camunda.latera.bss.utils.MapUtil.merge

class CSV {
  private static String  DEFAULT_DELIMITER = ';'
  private static String  DEFAULT_LINES_DELIMITER = '\n'
  private static Integer DEFAULT_SKIP_LINES = 0
  private static Boolean DEFAULT_WITH_HEADER = true
  private List header
  private List data

  /**
    String delimiter for items in line. Default: ';'.
  */
  String  delimiter

  /**
    String delimiter for lines. Default: '\n'.
  */
  String  linesDelimiter

  /**
    Number of lines to skip from beginning of data. Default 0.
  */
  Integer skipLines

  /**
    Boolean, if true return CSV with header line. Default true.
  */
  Boolean withHeader

  /**
    Constructor with named optional params or Map data input.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#Constructor+for+Map+input"></iframe>
    @param input Map with CSV data. Or named arguments from list below:
    @param data String, List[String], List[Map], or List[List] with CSV data.
    @param delimiter [optional] @see #delimiter
    @param linesDelimiter [optional] @see #linesDelimiter
    @param skipLines [optional] @see #skipLines
    @param withHeader [optional] @see #withHeader
    @param execution DelegateExecution with variables used instead of other [optional] arguments. Variable names the same but with 'csv' prefix, like 'csvLinesDelimiter'.
  */
  CSV(Map input) {
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
    input.remove('execution')
    input.remove('delimiter')
    input.remove('linesDelimiter')
    input.remove('skipLines')
    input.remove('withHeader')

    this.delimiter      = this.delimiter          ?: DEFAULT_DELIMITER
    this.linesDelimiter = this.linesDelimiter     ?: DEFAULT_LINES_DELIMITER
    this.skipLines      = this.skipLines          ?: DEFAULT_SKIP_LINES
    this.withHeader     = this.withHeader != null ? this.withHeader : DEFAULT_WITH_HEADER
    this.header         = []
    this.data           = []

    if (input.header) {
      this.setHeader(input.header)
      input.remove('header')
    }

    if (input.data) {
      this.setData(input.data, skipLines)
    } else {
      this.setData([input], skipLines)
    }
  }

  /**
    Constructor overload for String input.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#Constructor+for+String+input"></iframe>
    @param input String or GStringImpl with CSV data.
  */
  CSV(CharSequence input) {
    this(data: input)
  }

  /**
    Constructor overload for List input.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#Constructor+for+List%5BString%5D+input"></iframe>
    @param input List with CSV data.
  */
  CSV(List input) {
    this(data: input)
  }

  /**
    Parser of CSV data into internal format.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23parseLines+for+List%5BString%5D+input"></iframe>
    @param lines String, List[String], List[Map], or List[List] with CSV data.
    @param skipLines [optional] @see #skipLines
    @returns List[List] with CSV data
  */
  List parseLines(def lines, Integer _skipLines = 0) {
    List result = []
    if (lines) {
      if (isString(lines)) {
        lines = trim(lines.replace(join(header, delimiter), '')).tokenize(linesDelimiter)
      }
      if (isList(lines)) {
        lines.drop(_skipLines).each { def line ->
          List item = []
          if (isString(line) && notEmpty(line)) {
            line = trim(line).split(delimiter)
            if (!data && !header) { // read first line as header if header is not set
              setHeader(line)
              return
            }
            if (notHeader(line)) {
              if (header) {
                header.eachWithIndex { CharSequence column, Integer pos ->
                  if (line.size() > pos) {
                    item += line[pos]
                  } else {
                    item += null
                  }
                }
              }
              result << parseLine(line)
            }
          } else if (isMap(line)) {
            if (!header) {
              setHeader(keysList(line))
            }
            header.each { CharSequence column ->
              item << line[column]
            }
            result << parseLine(item)
          } else if (isList(line)) {
            if (notHeader(line)) {
              line.each { def value ->
                if (isString(value)) {
                  value = forceNvl(value)
                  if (value == 'Y' || value == 'true') {
                    value = true
                  }
                  if (value == 'N' || value == 'false') {
                    value = false
                  }
                } else if (!(value instanceof Boolean)) {
                  value = value.toString()
                }
                item << value
              }
              result << item
            }
          }
        }
      }
    }
    return result
  }

  /**
    Parse CSV data line into internal format.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23parseLine+for+String+input"></iframe>
    @param line String, Map, or List with CSV data.
    @returns List with CSV data
  */
  List parseLine(def line) {
    return parseLines([line], 0)?.getAt(0)
  }

  /**
    Parser of CSV header into internal format.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23parseHeader+for+String+input"></iframe>
    @param line String or List with CSV header, or Map with CSV data.
    @returns List with CSV data
  */
  List parseHeader(def rawHeader) {
    if (isMap(rawHeader)){
      return parseLine(keysList(rawHeader))
    }
    return parseLine(rawHeader)
  }

  /**
    Check if input is a header.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23isHeader+for+String+input"></iframe>
    @param line String or List with CSV header.
    @returns True if input is header, false otherwise.
  */
  Boolean isHeader(def line) {
    if (isString(line)) {
      return forceNvl(line) == join(header, delimiter)
    } else if (isList(line)) {
      return line == header
    }
    return false
  }

  /**
    Check if input is not a header.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23notHeader+for+String+input"></iframe>
    @see #isHeader(def)
  */
  Boolean notHeader(def line) {
    return !isHeader(line)
  }

  /**
    Get internal data in List format.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23getData"></iframe>
    @returns List[List] of CSV data without header.
  */
  List getData() {
    return data
  }

  /**
    Alias for getData()
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23toList+is+alias+for+%23getData"></iframe>
    @see #getData()
  */
  List toList() {
    return getData()
  }

  /**
    Get internal data in Map format.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23getDataMap"></iframe>
    @returns List[Map] of CSV data.
  */
  List getDataMap() {
    List result = []
    data.each { line ->
      LinkedHashMap item = [:]
      header.eachWithIndex { CharSequence column, Integer pos ->
        item[column] = line[pos]
      }
      result << item
    }
    return result
  }

  /**
    Alias for getDataMap()
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23toMap+is+alias+for+%23getDataMap"></iframe>
    @see #getDataMap()
  */
  List toMap() {
    return getDataMap()
  }

  /**
    Replaces internal data with input.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23setData+for+List%5BString%5D+input"></iframe>
    @param lines String, List[String], List[Map], or List[List] with CSV data.
  */
  CSV setData(def lines, Integer _skipLines = 0) {
    clear()
    if (!header && isList(lines)) {
      List linesToParse = lines.drop(_skipLines)
      if (linesToParse.size() > 0) {
        def firstLine = linesToParse[0]
        if (isList(firstLine)) {
          setHeader(firstLine)
          _skipLines = 0
        }
      }
    }
    addLines(lines, _skipLines)
    return this
  }

  /**
    Clear internal data.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23clear"></iframe>
  */
  CSV clear() {
    this.data = []
    return this
  }

  /**
    -- operator overload.
    Alias to clear()
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#--+operator"></iframe>
    @see #clear()
  */
  CSV previous() {
    clear()
    return this
  }

  /**
    ~ operator overload.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%7E+operator"></iframe>
    Alias to clear()
    @see #clear()
  */
  CSV negative() {
    clear()
    return this
  }

  /**
    Get header in internal format.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23getHader"></iframe>
    @returns List[List] of CSV header.
  */
  List getHeader() {
    return header
  }

  /**
    Replaces header with input.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23setHeader+for+String+input"></iframe>
    @param lines String, List[String], List[Map], or List[List] with CSV data.
  */
  CSV setHeader(def line) {
    // FIX: changing header with non-empty CSV causes real pain
    this.header = parseHeader(line)
    return this
  }

  /**
    Add lines to CSV.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23addLines+for+List%5BString%5D+input"></iframe>
    @param lines List[String], List[Map], or List[List] with CSV data.
    @returns List[List] ot internal data.
  */
  List addLines(def lines, Integer _skipLines = 0) {
    data += parseLines(lines, _skipLines)
    return data
  }

  /**
    Add line to CSV.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23addLine+for+String+input"></iframe>
    @param line String, Map, or List with CSV data.
    @returns List[List] ot internal data.
  */
  List addLine(def line) {
    return addLines([line])
  }

  /**
    Plus operator overload.
    Insert, <a href="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%2B+operator+for+Map+input+-+update">update</a> or <a href="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%2B+operator+for+Map+input+-+replace">replace</a> data in CSV.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%2B+operator+for+String+input"></iframe>
    @param item For insert - String, Map, or List with CSV data. For update - Map with Integer keys and [column: newData] Map values. For replace - Map with Integer keys and List or String values.
  */
  CSV plus(def item) {
    if (isMap(item)) {
      Boolean allIntegerKeys = true
      item.each { def key, def value ->
        if (!isInteger(key)) {
          allIntegerKeys = false
        }
      }
      if (allIntegerKeys && header) {
        item.each {def i, def value ->
          i = toIntSafe(i)
          if (data.size() > i) {
            if (isMap(value)) {
              data[i] = parseLine(merge(getAt(i), value)) // csv + [0: [column1: 'val']] - update line with index 0
            } else {
              data[i] = parseLine(value) // csv + [0: ['first', 'second']]  or csv + [0: 'first;second'] - replace line with index 0
            }
          } //else do nothing
        }
      } else {
        addLine(item) // csv + [column: 'val'] - insert new line
      }
    } else {
      if (isList(item) && item.size() > 0) {
        def firstLine = item[0]
        if (isString(firstLine) || isMap(firstLine) || isList(firstLine)) {
          addLines(item) // csv + ['first;second'], csv + [column1:'first', column2: 'second'], csv + [['first', 'second']],
        } else {
          addLine(item) // csv + ['first', 'second']
        }
      } else {
        addLines(item)
      }
    }
    return this
  }

  /**
    << operator overload.
    Insert data to CSV.
    Alias for addLine()
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%3C%3C+operator+for+String+input"></iframe>
    @param line String, Map, or List with CSV line.
    @see #addLine
  */
  CSV leftShift(def line) {
    addLine(line)
    return this
  }

  /**
    [] operator overload.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%5B%5D+operator"></iframe>
    @param i Line number to get.
    @returns Map with selected line
  */
  Map getAt(Integer i) {
    return getDataMap()[i]
  }

  /**
    []= operator overload.
    Replace line by index.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%5B%5D%3D+operator+for+String+input"></iframe>
    @param i Line number to replace.
    @param value String, Map, or List with CSV data.
  */
  CSV putAt(Integer i, def value) {
    data[i] = parseLine(value)
    return this
  }

  private Boolean checkLine(Integer i, Map input) {
    def line = data[i]

    if (input.indexes) {
      if (isList(input.indexes)){
        if (i in input.indexes) {
          return true
        }
      } else (isInteger(input.indexes) && i == input.indexes) {
        return true
      }
    } else if (input.where) {
      Integer _skip = 0
      if (isMap(input.where)) {
        input.where.each { CharSequence key, def value ->
          Integer pos = header.findIndexOf { it == key }
          if (pos >= 0 && forceNvl(line[pos]) == forceNvl(value)) {
            _skip += 1
          }
        }
        if (_skip == keysCount(input.where)) {
          return true
        }
      } else if (isList(input.where)) {
        input.where.eachWithIndex { def value, Integer column ->
          if (forceNvl(line[column]) == forceNvl(value)) {
            _skip += 1
          }
        }
        if (_skip == input.where.size()) {
          return true
        }
      }
    }
    return false
  }

  /**
    Checks if some line or lines exist in CSV.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23isExists+for+Map+input+-+by+index"></iframe>
    @param input Map with 'where' query in Map[String,Object] or List[Object] format or 'indexes' in List[Integer] format to search.
    @returns True if there is any line in data which corresponds to condition, false otherwise.
  */
  Boolean isExists(Map input) {
    for (int i = 0; i < data.size(); i++) {
      if (checkLine(i, input)) {
        return true
      }
    }
    return false
  }

  /**
    Checks if lines with indexes from list exist in CSV.
    @deprecated Use isExistsWithIndex() or 'in' operator instead
    @see #isExistsWithIndex(List)
  */
  Boolean isExists(List indexes) {
    return isExists(indexes: indexes)
  }

  /**
    Checks if lines with indexes from list exist in CSV.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23isExistsWithIndex+for+List+input+-+by+index"></iframe>
    @param input List[Integer] of lines indexes to search
    @returns True if there is any line in data which corresponds to condition, false otherwise.
    @see #isExists(Map)
  */
  Boolean isExistsWithIndex(List indexes) {
    return isExists(indexes)
  }

  /**
    Checks if line with index exist in CSV.
    @deprecated Use isExistsWithIndex() or 'in' operator instead
    @see #isExistsWithIndex(Integer)
  */
  Boolean isExists(Integer index) {
    return isExists([index])
  }

  /**
    Checks if line with index exist in CSV.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23isExistsWithIndex+for+Integer+input+-+by+index"></iframe>
    @param input Integer of line index to search
    @returns True if there is any line in data which corresponds to condition, false otherwise.
    @see #isExists(Map)
  */
  Boolean isExistsWithIndex(Integer index) {
    return isExists(index)
  }

  /**
    Checks if some line or lines exist in CSV.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23isExistsWhere+for+List+input+-+by+line"></iframe>
    @param input Line or its part in List[Object] format to search
    @returns True if there is any line in data which corresponds to condition, false otherwise.
  */
  Boolean isExistsWhere(List where) {
    return isExists(where: where)
  }

  /**
    Checks if some line or lines exist in CSV.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23isExistsWhere+for+Map+input+-+by+query"></iframe>
    @param input Map[String,Object] query format
    @returns True if there is any line in data which corresponds to condition, false otherwise.
  */
  Boolean isExistsWhere(Map where) {
    return isExists(where: where)
  }

  /**
    Checks if some line or lines exist in CSV.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23isExistsWhere+for+String+input+-+by+line"></iframe>
    @param input String line or its part
    @returns True if there is any line in data which corresponds to condition, false otherwise.
  */
  Boolean isExistsWhere(CharSequence where) {
    return isExistsWhere(parseLine(where))
  }

  /**
    in operator overload. Checks if some line or lines exist in CSV.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#in+operator+for+Integer+input+-+by+index"></iframe>
    @param input Map[String,Object] or List[Object] query format Integer line intex to search.
    @returns True if there is any line with this value of column, false otherwise.
    @see #isExists(Map)
    @see #isExistsWhere(Map)
  */
  Boolean isCase(def item) {
    if (isMap(item) || isString(item)) {
      return isExistsWhere(item)
    }
    return isExistsWithIndex(item)
  }

  /**
    Delete line or lines correspords to condition.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23deleteLines+for+Map+input+-+by+index"></iframe>
    @param input Map with 'where' query in Map[String,Object] or List[Object] format or 'indexes' in List[Integer] format to search.
    @returns List[List] of data.
  */
  List deleteLines(Map input) {
    List result = []
    data.eachWithIndex { def line, Integer i ->
      if (!checkLine(i, input)) {
        result << line
      }
    }
    this.data = result
    return data
  }

  /**
    Delete lines correspords to condition.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23deleteLinesByIndex+for+List+input+-+by+index"></iframe>
    @param input List[Integer] of indexes to delete.
    @returns List[List] of data.
  */
  List deleteLinesByIndex(List indexes) {
    return deleteLines(indexes: indexes)
  }

  /**
    Delete line correspords to condition.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23deleteLinesByIndex+for+Integer+input+-+by+index"></iframe>
    @param input Integer of index to delete.
    @returns List[List] of data.
  */
  List deleteLinesByIndex(Integer index) {
    return deleteLinesByIndex([index])
  }

  /**
    Delete line or lines correspords to condition.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23deleteLinesWhere+for+List+input+-+by+line"></iframe>
    @param input List[Object] to search whole line or line by part and delete.
    @returns List[List] of data.
  */
  List deleteLinesWhere(List where) {
    return deleteLines(where: where)
  }

  /**
    Delete line or lines correspords to condition.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23deleteLinesWhere+for+Map+input+-+by+query"></iframe>
    @param input Map[String,Object] to search and delete.
    @returns List[List] of data.
  */
  List deleteLinesWhere(Map where) {
    return deleteLines(where: where)
  }

  /**
    Delete line by full content or lines by some part.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23deleteLinesWhere+for+String+input+-+by+line"></iframe>
    @param input Map[String,Object] to search and delete.
    @returns List[List] of data.
  */
  List deleteLinesWhere(CharSequence where) {
    return deleteLinesWhere(parseLine(where))
  }

  /**
    - operator overload, Delete line or lines correspords to condition.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#-+operator+for+List+input+-+by+index"></iframe>
    @param input Integer, List[Integer] or Map to search.
    @see #deleteLines(Map)
  */
  CSV minus(def item) {
    if (isInteger(item)) {
      deleteLinesByIndex(item)
    } else if (isList(item)) {
      deleteLinesByIndex(item)
    } else if (isMap(item) || isString(item)) {
      deleteLinesWhere(item)
    }
    deleteLinesByIndex([item])
    return this
  }

  /**
    Get String CSV interpretation.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23getCsv+with+header"></iframe>
    @returns String of CSV data.
  */
  String getCsv() {
    List result = []
    if (withHeader) {
      result << join(header, delimiter)
    }
    if (data) {
      data.each { List line ->
        result << join(line.collect { def item ->
          if (isString(item) && forceIsEmpty(item)) {
            return null
          } else if (item == true || item == 'true') {
            return 'Y'
          } else if (item == false || item == 'false') {
            return 'N'
          } else {
            return item
          }
        }, delimiter)
      }
    }
    return join(result, linesDelimiter)
  }

  /**
    Alias of getCsv().
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#%23toString+is+alias+for+%23getCsv"></iframe>
    @see #getCsv()
  */
  String toString() {
    return getCsv()
  }

  /**
    Alias of toString().
    @deprecated Use toString() or 'as' operator instead
    @see #toString()
  */
  String to() {
    return getCsv()
  }

  /**
    Get internal data passed format.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.utils.CSVSpec.html#as+operator+with+String+type+input+is+alias+for+%23toString"></iframe>
    @param target Class convert value to. Possible values - String or List
    @returns String or List[List] of CSV. If withHeader is set, header prepends to String value as first line, but List is always without header.
    @see #getCsv()
    @see #getData()
    @see #withHeader
  */
  def asType(Class target) {
    if (CharSequence in target.getInterfaces()) {
      return toString()
    }
    return toList()
  }
}
