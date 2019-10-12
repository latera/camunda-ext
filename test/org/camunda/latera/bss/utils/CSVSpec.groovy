package org.camunda.latera.bss.utils

import spock.lang.*
import org.camunda.latera.bss.utils.CSV

class CSVSpec extends Specification {
  def "Constructor for String input"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)

    expect:
    csv as String == str
  }

  def "Constructor for List[String] input"() {
    given:
    List listOfStrings = ["a;b;c", "1;2;3"]
    def csv = new CSV(listOfStrings)

    expect:
    csv as String == "a;b;c\n1;2;3"
  }

  def "Constructor for Map input"() {
    given:
    Map map = [a: 1, b: 2, c: 3]
    def csv = new CSV(map)

    expect:
    csv as String == "a;b;c\n1;2;3"
  }

  def "Constructor for List[Map] input"() {
    given:
    List listOfMaps = [[a:1, b:2, c:3]]
    def csv = new CSV(listOfMaps)

    expect:
    csv as String == "a;b;c\n1;2;3"
  }

  def "Constructor for List[List] input"() {
    given:
    List listOfLists = [
      ['a', 'b', 'c'],
      [1,    2,    3]
    ]
    def csv = new CSV(listOfLists)

    expect:
    csv as String == "a;b;c\n1;2;3"
  }

  def "Constructor with named args input"() {
    given:
    String str = "a;b;c\n1;2;3"
    String delimiter = ";"
    String linesDelimiter = "\n"
    def csv = new CSV(
      delimiter      : delimiter,
      linesDelimiter : linesDelimiter,
      data           : str
    )

    expect:
    csv as String == str
  }

  def "#parseLines for List[String] input"() {
    given:
    String header = "a;b;c"
    def csv = new CSV(header)

    expect:
    csv.parseLines(["1;2;3"]) == [['1', '2', '3']]
  }

  def "#parseLines for List[Map] input"() {
    given:
    String header = "a;b;c"
    def csv = new CSV(header)

    expect:
    csv.parseLines([[a:1, b:2, c:3]]) == [['1', '2', '3']]
  }

  def "#parseLines for List[List] input"() {
    given:
    String header = "a;b;c"
    def csv = new CSV(header)

    expect:
    csv.parseLines([[1, 2, 3]]) == [['1', '2', '3']]
  }

  def "#parseLine for String input"() {
    given:
    String header = "a;b;c"
    def csv = new CSV(header)

    expect:
    csv.parseLine("1;2;3") == ['1', '2', '3']
  }

  def "#parseLine for Map input"() {
    given:
    String header = "a;b;c"
    def csv = new CSV(header)

    expect:
    csv.parseLine([a:1, b:2, c:3]) == ['1', '2', '3']
  }

  def "#parseLine for List input"() {
    given:
    String header = "a;b;c"
    def csv = new CSV(header)

    expect:
    csv.parseLine([1, 2, 3]) == ['1', '2', '3']
  }

  def "#parseHeader for String input"() {
    given:
    String delimiter = ";"
    def csv = new CSV(
      delimiter: delimiter
    )

    expect:
    csv.parseHeader("a;b;c") == ['a', 'b', 'c']
  }

  def "#parseHeader for Map input"() {
    given:
    String delimiter = ";"
    def csv = new CSV(
      delimiter: delimiter
    )

    expect:
    csv.parseHeader(a:null, b:null, c:null) == ['a', 'b', 'c']
  }

  def "#parseHeader for List input"() {
    given:
    String delimiter = ";"
    def csv = new CSV(
      delimiter: delimiter
    )

    expect:
    csv.parseHeader(['a', 'b', 'c']) == ['a', 'b', 'c']
  }

  def "#isHeader for String input"() {
    given:
    String header = "a;b;c"
    def csv = new CSV(header)

    expect:
    csv.isHeader("a;b;c")
    !csv.isHeader("a;b")
  }

  def "#isHeader for List input"() {
    given:
    String header = "a;b;c"
    def csv = new CSV(header)

    expect:
    csv.isHeader(['a', 'b', 'c'])
    !csv.isHeader(['a', 'b'])
  }

  def "#notHeader for String input"() {
    given:
    String header = "a;b;c"
    def csv = new CSV(header)

    expect:
    csv.notHeader("a;b")
    !csv.notHeader("a;b;c")
  }

  def "#notHeader for List input"() {
    given:
    String header = "a;b;c"
    def csv = new CSV(header)

    expect:
    csv.notHeader(['a', 'b'])
    !csv.notHeader(['a', 'b', 'c'])
  }

  def "#getData"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)

    expect:
    csv.getData() == [['1', '2', '3']]
    csv.data == [['1', '2', '3']]
  }

  def "#toList is alias for #getData"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)

    expect:
    csv.toList() == csv.data
  }

  def "#getDataMap"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)

    expect:
    csv.getDataMap() == [[a:'1', b:'2', c:'3']]
    csv.dataMap == [[a:'1', b:'2', c:'3']]
  }

  def "#toMap is alias for #getDataMap"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)

    expect:
    csv.toMap() == csv.dataMap
  }

  def "#setData for List[String] input"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.data = ["3;4;5"]

    expect:
    csv.data == [['3', '4', '5']]
  }

  def "#setData for List[Map] input"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.data = [[a:3, b:4, c:5]]

    expect:
    csv.data == [['3', '4', '5']]
  }

  def "#setData for List[List] input"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.data = [[3, 4, 5]]

    expect:
    csv.data == [['3', '4', '5']]
  }

  def "#setData with skipLines"() {
    given:
    Integer skipLines = 1
    String str = "someline\na;b;c\n1;2;3"
    def csv = new CSV(
      skipLines : skipLines,
      data      : str
    )

    expect:
    csv as String == "a;b;c\n1;2;3"
  }

  def "#clear"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.clear()

    expect:
    csv.data == []
  }

  def "-- operator"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv--

    expect:
    csv.data == []
  }

  def "~ operator"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    -csv

    expect:
    csv.data == []
  }

  def "#getHeader"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)

    expect:
    csv.getHeader() == ['a', 'b', 'c']
    csv.header == ['a', 'b', 'c']
  }

  def "#setHeader for String input"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.header = "a;b;c;d"

    expect:
    csv.dataMap == [[a:'1', b:'2', c:'3', d:null]]
  }

  def "#setHeader for Map input"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.header = [a:1, b:2, c:3, d:4]

    expect:
    csv.dataMap == [[a:'1', b:'2', c:'3', d:null]]
  }

  def "#setHeader for List input"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.header = ['a', 'b', 'c', 'd']

    expect:
    csv.dataMap == [[a:'1', b:'2', c:'3', d:null]]
  }

  def "#getDelimiter"() {
    given:
    String delimiter = ";"
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(
      delimiter : delimiter,
      data      : str
    )

    expect:
    csv.getDelimiter() == ";"
    csv.delimiter == ";"
  }

  def "#setDelimiter"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.delimiter = ","

    expect:
    csv.delimiter == ","
    csv as String == "a,b,c\n1,2,3"
  }

  def "#getLinesDelimiter"() {
    given:
    String linesDelimiter = "\n"
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(
      linesDelimiter : linesDelimiter,
      data           : str
    )

    expect:
    csv.getLinesDelimiter() == "\n"
    csv.linesDelimiter == "\n"
  }

  def "#setLinesDelimiter"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.linesDelimiter = ","

    expect:
    csv.linesDelimiter == ","
    csv as String == "a;b;c,1;2;3"
  }

  def "#addLines for List[String] input"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.addLines(["3;4;5"])

    expect:
    csv.data == [['1', '2', '3'], ['3', '4', '5']]
  }

  def "#addLines for List[Map] input"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.addLines([[a:3, b:4, c:5]])

    expect:
    csv.data == [['1', '2', '3'], ['3', '4', '5']]
  }

  def "#addLines for List[List] input"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.addLines([[3 ,4 ,5]])

    expect:
    csv.data == [['1', '2', '3'], ['3', '4', '5']]
  }

  def "#addLine for String input"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.addLine("3;4;5")

    expect:
    csv.data == [['1', '2', '3'], ['3', '4', '5']]
  }

  def "#addLine for Map input"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.addLine(a:3, b:4, c:5)

    expect:
    csv.data == [['1', '2', '3'], ['3', '4', '5']]
  }

  def "#addLine for List input"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.addLine([3 ,4 ,5])

    expect:
    csv.data == [['1', '2', '3'], ['3', '4', '5']]
  }

  def "<< operator for String input"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv << "3;4;5"

    expect:
    csv.data == [['1', '2', '3'], ['3', '4', '5']]
  }

  def "<< operator for Map input"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv << [a:3, b:4, c:5]

    expect:
    csv.data == [['1', '2', '3'], ['3', '4', '5']]
  }

  def "<< operator for List input"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv << [3 ,4 ,5]

    expect:
    csv.data == [['1', '2', '3'], ['3', '4', '5']]
  }

  def "+ operator for String input"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv += "3;4;5"

    expect:
    csv.data == [['1', '2', '3'], ['3', '4', '5']]
  }

  def "+ operator for List input"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv += [3, 4, 5]

    expect:
    csv.data == [['1', '2', '3'], ['3', '4', '5']]
  }

  def "+ operator for Map input - insert"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv += [a:3, b:4, c:5]

    expect:
    csv.data == [['1', '2', '3'], ['3', '4', '5']]
  }

  def "+ operator for Map input - update"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv += [0: [a:3]]

    expect:
    csv.data == [['3', '2', '3']]
  }

  def "+ operator for Map input - replace"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv += [0: "3;4;5"]

    expect:
    csv.data == [['3', '4', '5']]
  }

  def "[] operator"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)

    expect:
    csv[0] == [a:'1', b:'2', c:'3']
  }

  def "[]= operator for String input"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv[0] = "3;4;5"

    expect:
    csv.data == [['3', '4', '5']]
  }

  def "[]= operator for Map input"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv[0] = [a:3, b:4, c:5]

    expect:
    csv.data == [['3', '4', '5']]
  }

  def "[]= operator for List input"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv[0] = [3, 4, 5]

    expect:
    csv.data == [['3', '4', '5']]
  }

  def "#isExists for Map input - by index"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)

    expect:
    csv.isExists(indexes: [0])
    !csv.isExists(indexes: [1])
  }

  def "#isExists for Map input - by any of indexes list"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)

    expect:
    csv.isExists(indexes: [0, 1])
    !csv.isExists(indexes: [10, 50])
  }

  def "#isExists for Map input - by query"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)

    expect:
    csv.isExists(where: [a: 1])
    !csv.isExists(where: [b: null])
  }

  def "#isExists for Map input - by line"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)

    expect:
    csv.isExists(where: [1, 2, 3])
    !csv.isExists(where: [0, 2, 3])
  }

  def "#isExists for Map input - by line part"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)

    expect:
    csv.isExists(where: [1, 2])
    !csv.isExists(where: [0, 2])
  }

  def "#isExistsWithIndex for List input - by index"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)

    expect:
    csv.isExists([0])
    !csv.isExists([1])
  }

  def "#isExistsWithIndex for List input - by any of indexes list"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)

    expect:
    csv.isExists([0, 1])
    !csv.isExists([10, 50])
  }

  def "#isExistsWithIndex for Integer input - by index"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)

    expect:
    csv.isExists(0)
    !csv.isExists(1)
  }

  def "#isExistsWhere for List input - by line"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)

    expect:
    csv.isExistsWhere([1, 2, 3])
    !csv.isExistsWhere([0, 2, 3])
  }

  def "#isExistsWhere for List input - by line part"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)

    expect:
    csv.isExistsWhere([1, 2])
    !csv.isExistsWhere([0, 2])
  }

  def "#isExistsWhere for Map input - by query"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)

    expect:
    csv.isExistsWhere(a: 1)
    !csv.isExistsWhere(b: null)
  }

  def "#isExistsWhere for String input - by line"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)

    expect:
    csv.isExistsWhere("1;2;3")
    !csv.isExistsWhere("0;2;3")
  }

  def "#isExistsWhere for String input - by line part"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)

    expect:
    csv.isExistsWhere("1;2")
    !csv.isExistsWhere("0;2")
  }

  def "in operator for Integer input - by index"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)

    expect:
    0 in csv
    !(1 in csv)
  }

  def "in operator for Map input - by query"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)

    expect:
    [a: 1] in csv
    !([b: null] in csv)
  }

  def "in operator for String input - by line"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)

    expect:
    "1;2;3" in csv
    !("0;2;3" in csv)
  }

  def "in operator for String input - by line part"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)

    expect:
    "1;2" in csv
    !("0;2" in csv)
  }

  def "#deleteLines for Map input - by index"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.deleteLines(indexes: [0])

    expect:
    csv.data == []
  }

  def "#deleteLines for Map input - by any of indexes list"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.deleteLines(indexes: [0, 1])

    expect:
    csv.data == []
  }

  def "#deleteLines for Map input - by query"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.deleteLines(where: [a: 1])

    expect:
    csv.data == []
  }

  def "#deleteLines for Map input - by line"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.deleteLines(where: [1, 2, 3])

    expect:
    csv.data == []
  }

  def "#deleteLines for Map input - by line part"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.deleteLines(where: [1, 2])

    expect:
    csv.data == []
  }

  def "#deleteLinesByIndex for List input - by index"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.deleteLinesByIndex([0])

    expect:
    csv.data == []
  }

  def "#deleteLinesByIndex for List input - by any of indexes list"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.deleteLinesByIndex([0, 1])

    expect:
    csv.data == []
  }

  def "#deleteLinesByIndex for Integer input - by index"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.deleteLinesByIndex(0)

    expect:
    csv.data == []
  }

  def "#deleteLinesWhere for List input - by line"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.deleteLinesWhere([1, 2, 3])

    expect:
    csv.data == []
  }

  def "#deleteLinesWhere for List input - by line part"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.deleteLinesWhere([1, 2])

    expect:
    csv.data == []
  }

  def "#deleteLinesWhere for Map input - by query"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.deleteLinesWhere(a: 1)

    expect:
    csv.data == []
  }

  def "#deleteLinesWhere for String input - by line"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.deleteLinesWhere("1;2;3")

    expect:
    csv.data == []
  }

  def "#deleteLinesWhere for String input - by line part"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv.deleteLinesWhere("1;2")

    expect:
    csv.data == []
  }

  def "- operator for List input - by index"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv -= [0]

    expect:
    csv.data == []
  }

  def "- operator for List input - by any of indexes list"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv -= [0, 1]

    expect:
    csv.data == []
  }

  def "- operator for Integer input - by index"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv -= 0

    expect:
    csv.data == []
  }

  def "- operator for Map input - by query"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv -= [a: 1]

    expect:
    csv.data == []
  }

  def "- operator for String input - by line"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv -= "1;2;3"

    expect:
    csv.data == []
  }

  def "- operator for String input - by line part"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)
    csv -= "1;2"

    expect:
    csv.data == []
  }

  def "#getCsv with header"() {
    given:
    String str = "a;b;c\n1;2;3"
    String delimiter = ";"
    String lineDelimiter = "\n"
    Boolean withHeader = true
    def csv = new CSV(
      data          : str,
      delimiter     : delimiter,
      lineDelimiter : lineDelimiter,
      withHeader    : withHeader
    )

    expect:
    csv.getCsv() == str
    csv.csv == str
  }

  def "#getCsv without header"() {
    given:
    String str = "a;b;c\n1;2;3"
    String delimiter = ";"
    String lineDelimiter = "\n"
    Boolean withHeader = false
    def csv = new CSV(
      data          : str,
      delimiter     : delimiter,
      lineDelimiter : lineDelimiter,
      withHeader    : withHeader
    )

    expect:
    csv.getCsv() == "1;2;3"
    csv.csv == "1;2;3"
  }

  def "#toString is alias for #getCsv"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)

    expect:
    csv.csv == csv.toString()
  }

  def "as operator with String type input is alias for #toString"() {
    given:
    String str = "a;b;c\n1;2;3"
    def csv = new CSV(str)

    expect:
    csv as String == str
  }

  def "as operator with List type input with header"() {
    given:
    String str = "a;b;c\n1;2;3"
    String delimiter = ";"
    String lineDelimiter = "\n"
    Boolean withHeader = true
    def csv = new CSV(
      data          : str,
      delimiter     : delimiter,
      lineDelimiter : lineDelimiter,
      withHeader    : withHeader
    )

    expect:
    csv as List == [['1', '2', '3']]
  }

  def "as operator with List type input without header"() {
    given:
    String str = "a;b;c\n1;2;3"
    String delimiter = ";"
    String lineDelimiter = "\n"
    Boolean withHeader = false
    def csv = new CSV(
      data          : str,
      delimiter     : delimiter,
      lineDelimiter : lineDelimiter,
      withHeader    : withHeader
    )

    expect:
    csv as List == [['1', '2', '3']]
  }
}