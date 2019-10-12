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
    Example:
    <pre>
    {@code
    String str =
    """
    a;b;c
    1;2;3
    """
    List listOfStrings = [
      "a;b;c",
      "1;2;3"
    ]
    List listOfMaps = [
      [a:1,  b:2,  c:3],
      [a:10, b:20, c:30]
    ]
    List listOfLists = [
      ['a', 'b', 'c'],
      [1,    2,    3]
    ]
    def csv = new CSV(
      delimiter : ';',
      linesDelimiter : '\n',
      data : str // or listOfString, of listOfMaps, or listOfLists
    )

    // or use execution variables for fetching options
    def csv = new CSV(
      execution: execution,
      data : str // or listOfString, of listOfMaps, or listOfLists
    )

    Map map = [
      a: 1,
      b: 2,
      c: 3
    ]
    def csv = new CSV(map) // or use default params
    }</pre>

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

    this.delimiter      = this.delimiter      ?: DEFAULT_DELIMITER
    this.linesDelimiter = this.linesDelimiter ?: DEFAULT_LINES_DELIMITER
    this.skipLines      = this.skipLines      ?: DEFAULT_SKIP_LINES
    this.withHeader     = this.withHeader     ?: DEFAULT_WITH_HEADER
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
    Constructor overload for List input.
    <p>
    Example:
    <pre>
    {@code
    String str =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(str)
    }</pre>

    @param input String or GStringImpl with CSV data.
  */
  CSV(CharSequence input) {
    this(data: input)
  }

  /**
    Constructor overload for String input.
    <p>
    Example:
    <pre>
    {@code
    List listOfStrings = [
      "a;b;c",
      "1;2;3"
    ]
    List listOfMaps = [
      [a:1,  b:2,  c:3],
      [a:10, b:20, c:30]
    ]
    List listOfLists = [
      ['a', 'b', 'c'],
      [1,    2,    3]
    ]
    def csv = new CSV(listOfStrings) // or listOfMaps, or listOfLists
    }</pre>

    @param input List with CSV data.
  */
  CSV(List input) {
    this(data: input)
  }

  /**
    Parser of CSV data into internal format.
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
            List mapKeys = keysList(line)
            if (!header) {
              setHeader(mapKeys)
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
                  if (value == 'Y') {
                    value = true
                  }
                  if (value == 'N') {
                    value = false
                  }
                } else {
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
    Example:
    <pre>
    {@code
    String str =
    """
    1;2;3
    """
    Map map = [
      a: 1,
      b: 2,
      c: 3
    ]
    List list = [
      [1, 2, 3]
    ]
    csv.parseLine(str) // or map, or list
    }</pre>

    @param line String, Map, or List with CSV data.
    @returns List with CSV data
  */
  List parseLine(def line) {
    return parseLines([line], 0)?.getAt(0)
  }

  /**
    Parser of CSV header into internal format.
    <p>
    Example:
    <pre>
    {@code
    String str =
    """
    a;b;c
    """
    Map map = [
      a: 1,
      b: 2,
      c: 3
    ]
    List list = [
      ['a', 'b', 'c']
    ]
    csv.parseLine(str) // or map, or list
    }</pre>

    @param line String or List with CSV header, or Map with CSV data.
    @returns List with CSV data
  */
  List parseHeader(def rawHeader) {
    return parseLine(rawHeader)
  }

  /**
    Check if input is a header.
    <p>
    Example:
    <pre>
    {@code
    Map data = [
      a: 1,
      b: 2
    ]
    def csv = new CSV(data)
    assert csv.isHeader("a;b") == true
    assert csv.isHeader(['a','b']) == true
    assert csv.isHeader(['a']) == false
    }</pre>

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
    @see #isHeader
  */
  Boolean notHeader(def line) {
    return !isHeader(line)
  }

  /**
    Get internal data in List format.
    <p>
    Example:
    <pre>
    {@code
    String data =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(data)
    assert csv.data == [
      [1, 2, 3]
    ]
    }</pre>

    @returns List[List] of CSV data without header.
  */
  List getData() {
    return data
  }

  /**
    Alias for getData()
    @see #getData
  */
  List toList() {
    return getData()
  }

  /**
    Get internal data in Map format.
    <p>
    Example:
    <pre>
    {@code
    String data =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(data)
    assert csv.dataMap == [
      [
        a: 1,
        b: 2,
        c: 3
      ]
    ]
    }</pre>

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
    @see #getDataMap
  */
  List toMap() {
    return getDataMap()
  }

  /**
    Replaces internal data with input.
    <p>
    Example:
    <pre>
    {@code
    String data =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(data)
    csv.data = [
      [
        a: 1,
        b: null,
        c: 5
      ]
    ]
    assert csv.data == [
      [1, null, 5]
    ]
    }</pre>

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
    Example:
    <pre>
    {@code
    String data =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(data)
    csv.clear()
    assert csv.dataMap == []
    }</pre>
  */
  CSV clear() {
    this.data = []
    return this
  }

  /**
    -- operator overload.
    Alias to clear()
    <p>
    Example:
    <pre>
    {@code
    String data =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(data)
    csv--
    assert csv.dataMap == []
    }</pre>
    @see #clear
  */
  CSV previous() {
    clear()
    return this
  }

  /**
    - operator overload.
    Alias to clear()
    Alias to clear()
    <p>
    Example:
    <pre>
    {@code
    String data =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(data)
    -csv
    assert csv.dataMap == []
    }</pre>
    @see #clear
  */
  CSV negative() {
    clear()
    return this
  }

  /**
    Get header in internal format.
    <p>
    Example:
    <pre>
    {@code
    String data =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(data)
    assert csv.header == ['a', 'b', 'c']
    }</pre>

    @returns List[List] of CSV header.
  */
  List getHeader() {
    return header
  }

  /**
    Replaces header with input.
    <p>
    Example:
    <pre>
    {@code
    String data =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(data)
    csv.header = ['a', 'b', 'c', 'd']
    assert csv.dataMap == [
      [
        a: 1,
        b: 2,
        c: 3,
        d: null
      ]
    ]
    }</pre>

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
    Example:
    <pre>
    {@code
    String data =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(data)
    csv.addLines([
      [
        a: 10,
        b: 20,
        c: 30
      ]
    ])
    assert csv.dataMap == [
      [
        a: 1,
        b: 2,
        c: 3
      ],
      [
        a: 10,
        b: 20,
        c: 30
      ]
    ]
    }</pre>

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
    Example:
    <pre>
    {@code
    String data =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(data)
    csv.addLine([
      a: 10,
      b: 20,
      c: 30
    ])
    assert csv.dataMap == [
      [
        a: 1,
        b: 2,
        c: 3
      ],
      [
        a: 10,
        b: 20,
        c: 30
      ]
    ]
    }</pre>

    @param line String, Map, or List with CSV data.
    @returns List[List] ot internal data.
  */
  List addLine(def line) {
    return addLines([line])
  }

  /**
    Plus operator overload.
    Insert, update or replace data in CSV.
    <p>
    Example:
    <pre>
    {@code
    String data =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(data)
    csv += [
      a: 10,
      b: 20,
      c: 30
    ] // insert line
    assert csv.dataMap == [
      [
        a: 1,
        b: 2,
        c: 3
      ],
      [
        a: 10,
        b: 20,
        c: 30
      ]
    ]

    csv += [100, 200, 300] // insert line
    assert csv.dataMap == [
      [
        a: 1,
        b: 2,
        c: 3
      ],
      [
        a: 10,
        b: 20,
        c: 30
      ],
      [
        a: 100,
        b: 200,
        c: 300
      ]
    ]

    csv += "5;10;15" // insert line
    assert csv.dataMap == [
      [
        a: 1,
        b: 2,
        c: 3
      ],
      [
        a: 10,
        b: 20,
        c: 30
      ],
      [
        a: 100,
        b: 200,
        c: 300
      ],
      [
        a: 5,
        b: 10,
        c: 15
      ]
    ]

    csv += [
      0: [a: 5]
    ] // update line columns
    assert csv.dataMap == [
      [
        a: 5,
        b: 2,
        c: 3
      ],
      [
        a: 10,
        b: 20,
        c: 30
      ],
      [
        a: 100,
        b: 200,
        c: 300
      ],
      [
        a: 5,
        b: 10,
        c: 15
      ]
    ]

    csv += [
      0: '3;4;5'
    ] // replace line
    assert csv.dataMap == [
      [
        a: 3,
        b: 4,
        c: 5
      ],
      [
        a: 10,
        b: 20,
        c: 30
      ],
      [
        a: 100,
        b: 200,
        c: 300
      ],
      [
        a: 5,
        b: 10,
        c: 15
      ]
    ]
    }</pre>

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
              data[i] = parseLine(merge(getAt(i), value)) // csv + [0: [column: 'val']] - update line with index 0
            } else {
              data[i] = parseLine(value) // csv + [0: ['first', 'second']]  or csv + [0: 'first;second'] - replace line with index 0
            }
          } //else do nothing
        }
      } else {
        addLine(item) // csv + [column: 'val'] - insert new line
      }
    } else {
      addLines(item)
    }
    return this
  }

  /**
    << operator overload.
    Insert data to CSV.
    <p>
    Example:
    <pre>
    {@code
    String data =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(data)
    csv << [
      a: 10,
      b: 20,
      c: 30
    ] // insert line
    assert csv.dataMap == [
      [
        a: 1,
        b: 2,
        c: 3
      ],
      [
        a: 10,
        b: 20,
        c: 30
      ]
    ]

    csv << [100, 200, 300] // insert line
    assert csv.dataMap == [
      [
        a: 1,
        b: 2,
        c: 3
      ],
      [
        a: 10,
        b: 20,
        c: 30
      ],
      [
        a: 100,
        b: 200,
        c: 300
      ]
    ]

    csv << "5;10;15" // insert line
    assert csv.dataMap == [
      [
        a: 1,
        b: 2,
        c: 3
      ],
      [
        a: 10,
        b: 20,
        c: 30
      ],
      [
        a: 100,
        b: 200,
        c: 300
      ],
      [
        a: 5,
        b: 10,
        c: 15
      ]
    ]
    }</pre>

    @param line String, Map, or List with CSV line.
  */
  CSV leftShift(def line) {
    addLine(line)
    return this
  }

  /**
    [] operator overload.
    Get CSV line (as Map) by index.
    <p>
    Example:
    <pre>
    {@code
    String data =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(data)
    assert csv[0] == [
      a: 1,
      b: 2,
      c: 3
    ]
    }</pre>

    @param i Line number to get.
    @returns Map with selected line
  */
  Map getAt(Integer i) {
    return getDataMap()[i]
  }

  /**
    []= operator overload.
    Replace line (as Map) by index.
    <p>
    Example:
    <pre>
    {@code
    String data =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(data)
    csv[0] == [
      a: 3,
      b: 4,
      c: 5
    ]
    assert csv.dataMap == [
      [
        a: 3,
        b: 4,
        c: 5
      ]
    ]
    }</pre>

    @param i Line number to replace.
    @param value String, Map, or List with CSV data.
  */
  CSV putAt(Integer i, def value) {
    data[i] = parseLine(value)
    return this
  }

  /**
    Checks if line satisfies some condition.
    <p>
    Example:
    <pre>
    {@code
    String data =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(data)
    assert csv.isExist(where: [a: 1]) // by column name and value
    assert !csv.isExist(where: [b: null])
    assert csv.isExist(where: [1]) // by line or its part
    assert !csv.isExist(where: [0])
    assert csv.isExist(indexes: [0]) // by index
    assert csv.isExist(indexes: [0, 1]) // any of index from list should exist
    assert !csv.isExist(indexes: [1])
    }</pre>

    @param input Map with 'where' query in Map[String,Object] or List[Object] format or 'indexes' in List[Integer] format to search.
    @returns True if there is any line in data which corresponds to condition, false otherwise.
  */
  Boolean checkLine(Integer i, Map input) {
    def line = data[i]
    Boolean ok = false

    if (input.indexes) {
      if (isList(input.indexes) && i in input.indexes) {
        ok = true
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
          ok = true
        }
      } else if (isList(input.where)) {
        input.where.eachWithIndex { def value, Integer column ->
          if (forceNvl(line[column]) == forceNvl(value)) {
            _skip += 1
          }
        }
        if (_skip == input.where.size()) {
          ok = true
        }
      }
    }
    return ok
  }

  /**
    Checks if some line or lines exist in CSV.
    <p>
    Example:
    <pre>
    {@code
    String data =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(data)
    assert csv.isExist(where: [a: 1]) // by column name and value
    assert !csv.isExist(where: [b: null])
    assert csv.isExist(where: [1]) // by line or its part
    assert !csv.isExist(where: [0])
    assert csv.isExist(indexes: [0]) // by index
    assert csv.isExist(indexes: [0, 1]) // any of index from list should exist
    assert !csv.isExist(indexes: [1])
    }</pre>

    @param input Map with 'where' query in Map[String,Object] or List[Object] format or 'indexes' in List[Integer] format to search.
    @returns True if there is any line in data which corresponds to condition, false otherwise.
  */
  Boolean isExists(Map input) {
    List result = []
    data.eachWithIndex { def line, Integer i ->
      if (checkLine(i, input)) {
        return true
      }
    }
    return false
  }

  /**
    in operator overload. Checks if some line or lines exist in CSV.
    <p>
    Example:
    <pre>
    {@code
    String data =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(data)
    assert 0 in csv // by index
    assert [a: 1] in csv // by query
    assert !([b: null] in csv)
    }</pre>

    @param inputMap[String,Object] or List[Object] query format Integer line intex to search.
    @returns True if there is any line with this value of column, false otherwise.
    @see #isExists(Map)
    @see #isExistsWhere(Map)
  */
  Boolean isCase(def item) {
    if (isMap(item)) {
      return isExistsWhere(item)
    } else if (isList(item)) {
      return isExists(item)
    }
    return isExists([item])
  }

  /**
    Checks if lines with indexes from list exist in CSV.
    <p>
    Example:
    <pre>
    {@code
    String data =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(data)
    assert csv.isExist([0]) // by index
    assert csv.isExist([0, 1]) // any of index from list should exist
    assert !csv.isExist([1])
    }</pre>

    @param input List[Integer] of lines indexes to search
    @returns True if there is any line in data which corresponds to condition, false otherwise.
    @see #isExists(Map)
  */
  Boolean isExists(List indexes) {
    return isExists(indexes: indexes)
  }

  /**
    Checks if line with index exist in CSV. Alias
    <p>
    Example:
    <pre>
    {@code
    String data =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(data)
    assert csv.isExist(0) // by index
    assert !csv.isExist(1)
    }</pre>

    @param input Integer of line index to search
    @returns True if there is any line in data which corresponds to condition, false otherwise.
    @see #isExists(Map)
  */
  Boolean isExists(Integer index) {
    return isExists([index])
  }

  /**
    Checks if some line or lines exist in CSV.
    <p>
    Example:
    <pre>
    {@code
    String data =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(data)
    assert csv.isExistWhere([1,2]) // by line or its part
    assert !csv.isExistWhere([2])
    }</pre>

    @param input Line or its part in List[Object] format to search
    @returns True if there is any line in data which corresponds to condition, false otherwise.
  */
  Boolean isExistsWhere(List where) {
    return isExists(where: where)
  }

  /**
    Checks if some line or lines exist in CSV.
    <p>
    Example:
    <pre>
    {@code
    String data =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(data)
    assert csv.isExist(where: [a: 1]) // by query
    assert !csv.isExist(where: [b: null])
    assert csv.isExist(indexes: [0]) // by index
    assert !csv.isExist(indexes: [1])
    }</pre>

    @param input Map[String,Object] query format
    @returns True if there is any line in data which corresponds to condition, false otherwise.
  */
  Boolean isExistsWhere(Map where) {
    return isExists(where: where)
  }

  /**
    Delete line or lines correspords to condition.
    <p>
    Example:
    <pre>
    {@code
    String data =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(data)
    csv.deleteLines(where: [a: 1]) // by column name and value
    assert csv.dataMap == []

    def csv = new CSV(data)
    csv.deleteLines(where: [1]) // by line or its part
    assert csv.dataMap == []

    def csv = new CSV(data)
    csv.deleteLines(indexes: [0]) // by index
    assert csv.dataMap == []
    }</pre>

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
    - operator oevrload, Delete line or lines correspords to condition.
    <p>
    Example:
    <pre>
    {@code
    String data =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(data)
    csv - [a: 1] // by column name and value
    assert csv.dataMap == []

    def csv = new CSV(data)
    csv - 0 // by index
    assert csv.dataMap == []

    def csv = new CSV(data)
    csv - [0,1] // multiple lines by their indexes
    assert csv.dataMap == []
    }</pre>

    @param input Integer, List[Integer] or Map to search.
    @see #deleteLines
  */
  CSV minus(def item) {
    if (isInteger(item)) {
      deleteLinesByIndex(item)
    } else if (isList(item)) {
      deleteLinesByIndex(item)
    } else if (isMap(item)) {
      deleteLinesWhere(item)
    }
    deleteLinesByIndex([item])
    return this
  }

  /**
    Delete lines correspords to condition.
    <p>
    Example:
    <pre>
    {@code
    String data =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(data)
    csv.deleteLinesByIndex([0])
    assert csv.dataMap == []
    }</pre>

    @param input List[Integer] of indexes to delete.
    @returns List[List] of data.
  */
  List deleteLinesByIndex(List indexes) {
    return deleteLines(indexes: indexes)
  }

  /**
    Delete line correspords to condition.
    <p>
    Example:
    <pre>
    {@code
    String data =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(data)
    csv.deleteLinesByIndex(0)
    assert csv.dataMap == []
    }</pre>

    @param input Integer of index to delete.
    @returns List[List] of data.
  */
  List deleteLinesByIndex(Integer index) {
    return deleteLinesByIndex([index])
  }

  /**
    Delete line or lines correspords to condition.
    <p>
    Example:
    <pre>
    {@code
    String data =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(data)
    csv.deleteLinesWhere([1]) // by line or its part
    assert csv.dataMap == []
    }</pre>

    @param input List[Object] to search whole line or line by part and delete.
    @returns List[List] of data.
  */
  List deleteLinesWhere(List where) {
    return deleteLines(where: where)
  }

  /**
    Delete line or lines correspords to condition.
    <p>
    Example:
    <pre>
    {@code
    String data =
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(data)
    csv.deleteLinesWhere(a: 1)
    assert csv.dataMap == []
    }</pre>

    @param input Map[String,Object] to search and delete.
    @returns List[List] of data.
  */
  List deleteLinesWhere(Map where) {
    return deleteLines(where: where)
  }

  /**
    Get String CSV interpretation.
    <p>
    Example:
    <pre>
    {@code
    List header = ['a', 'b', 'c']
    List data = [
      [1, 2, 3]
    ]
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(
      header    : header,
      data      : data,
      delimiter : ';'
    )
    assert csv.getCsv() ==
    """
    a;b;c
    1;2;3
    """
    }</pre>

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
          } else if (item == true) {
            return 'Y'
          } else if (item == false) {
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
    @see #getCsv
  */
  String toString() {
    return getCsv()
  }

  /**
    Alias of toString().
    @deprecated Use toString() or 'as' operator instead
    @see #toString
  */
  String to() {
    return getCsv()
  }

  /**
    Get internal data passed format.
    <p>
    Example:
    <pre>
    {@code
    List header = ['a', 'b', 'c']
    List data = [
      [1, 2, 3]
    ]
    """
    a;b;c
    1;2;3
    """
    def csv = new CSV(
      header    : header,
      data      : data,
      delimiter : ';'
    )
    assert csv as String ==
    """
    a;b;c
    1;2;3
    """
    assert csv as List == [
      [1,2,3]
    ]
    }</pre>

    @param target Class convert value to. Possible values - String or List
    @returns String or List[List] of CSV. If withHeader is set, header prepends to String value as first line, but List is always without header.
    @see #getCsv
    @see #getData
    @see #withHeader
  */
  def asType(Class target) {
    if (CharSequence in target.getInterfaces()) {
      return toString()
    }
    return toList()
  }
}
