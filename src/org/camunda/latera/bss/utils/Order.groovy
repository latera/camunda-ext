package org.camunda.latera.bss.utils

import org.camunda.bpm.engine.delegate.DelegateExecution
import static org.camunda.latera.bss.utils.StringUtil.isString
import static org.camunda.latera.bss.utils.StringUtil.capitalize
import static org.camunda.latera.bss.utils.StringUtil.decapitalize
import static org.camunda.latera.bss.utils.Numeric.isNumber
import static org.camunda.latera.bss.utils.Numeric.toIntStrict
import static org.camunda.latera.bss.utils.Numeric.toFloatSafe
import static org.camunda.latera.bss.utils.DateTimeUtil.parseDateTimeAny
import static org.camunda.latera.bss.utils.DateTimeUtil.isDate
import static org.camunda.latera.bss.utils.DateTimeUtil.iso
import static org.camunda.latera.bss.utils.StringUtil.forceNvl as nvlString
import static org.camunda.latera.bss.utils.ListUtil.forceNvl as nvlList
import static org.camunda.latera.bss.utils.ListUtil.parse as parseList
import static org.camunda.latera.bss.utils.MapUtil.parse  as parseMap
import org.camunda.latera.bss.connectors.Minio

class Order implements GroovyObject {
  private DelegateExecution _execution

  Order(DelegateExecution execution) {
    this._execution = execution
  }

  static def getValue(CharSequence name, Boolean raw = false, DelegateExecution execution) {
    if (!name.startsWith('homsOrderData')) {
      name = "homsOrderData${capitalize(name)}"
    }

    if (name.endsWith('UploadedFile') || name.endsWith('FileUpload')) {
      return null
    }
    def result = execution.getVariable(name)
    if (raw) {
      return result
    }

    if (name.endsWith('CSV')) {
      try {
        /* servicesCSV: """
        n_price_line_id;n_quant;vc_rem;c_flag
        123431201;null;test;Y
        123431301;1;;N
        """
        ->
        servicesCSV: new CSV([
          [
            n_price_line_id: 123431201,
            n_quant: null,
            vc_rem: 'test',
            c_flag: true
          ],[
            n_price_line_id: 123431301,
            n_quant: 1,
            vc_rem: null,
            c_flag: false
          ]
        ])
        */
        return new CSV(data: result, execution: execution)
      } catch (Exception e) {}
    }

    if (name.endsWith('JSON') || name.endsWith('Map')) {
      // servicesMap: '{a:1, b:'2'}' -> servicesMap: [a:1, b:'2']
      return parseMap(result)
    }

    if (name.endsWith('List')) {
      // TODO: remove nvlList after CONSULT-3350 be solved
      // serviceList: "[1,2,'3']" -> serviceList: [1, 2, '3']
      return nvlList(parseList(result))
    }

    // '2019-12-31', '31.12.2019', '31.12.2019 23:59:59', '2019-12-31T23:59:59' -> LocalDateTime
    // '2019-12-31T23:59:59+03:00' -> ZonedDateTime
    // but '2019' -> '2019'
    def date = parseDateTimeAny(result)
    if (date) {
      return date
    }

    // '123431201' -> new BigInteger(123431201), but '123431201.0' -> '123431201.0'
    def number = toIntStrict(result)
    if (number != null) {
      return number
    }

    // '0.00' -> new BigDecimal(0.00), '0' -> new BigInteger(0), but '0ASF' -> '0ASF'
    number = toFloatSafe(result)
    if (number != null) {
      return number
    }

    // '', ' ', null, 'null' -> null, otherwise trimmed string is returned
    if (isString(result)) {
      return nvlString(result)
    }

    //boolean and other types remain unchanged
    return result
  }

  static def getValueRaw(CharSequence name, DelegateExecution execution) {
    return getValue(name, execution, true)
  }

  static def getValue(DelegateExecution execution, CharSequence name, Boolean raw = false) {
    return getValue(name, raw, execution)
  }

  static def getValueRaw(DelegateExecution execution, CharSequence name) {
    return getValue(execution, name)
  }

  def getValue(CharSequence name, Boolean raw = false) {
    if (name == 'data') {
      return this.getClass().getData(this._execution)
    }
    return this.getClass().getValue(name, raw, this._execution)
  }

  def getValueRaw(CharSequence name) {
    return getValue(name, true)
  }

  def getProperty(String propertyName) { // ALERT: do not change type to CharSequence, dynamic access will not work
    return getValue(propertyName)
  }

  def getAt(CharSequence name) {
    return getValue(name)
  }

  def getVariable(CharSequence name) {
    return getValue(name)
  }

  def getVariableRaw(CharSequence name) {
    return getValueRaw(name)
  }

  static Map getData(DelegateExecution execution, Boolean raw = false) {
    LinkedHashMap data = [:]
      if (key.startsWith('homsOrderData') && !name.endsWith('UploadedFile') && !name.endsWith('FileUpload')) {
        String _key = decapitalize(key.replaceFirst(/^homsOrderData/, ''))
        data[_key] = getValue(key, raw, execution)
      }
    }
    return data
  }

  static Map getDataRaw(DelegateExecution execution) {
    return getData(execution, true)
  }

  Map getData(Boolean raw = false) {
    return getData(this._execution, raw)
  }

  Map getDataRaw() {
    return getData(true)
  }

  def asType(Class target) {
    switch (target) {
      case Map : return getData()
      default  : return this
    }
  }

  static void setValue(CharSequence name, def value, Boolean raw = false, DelegateExecution execution) {
    if (!name.startsWith('homsOrderData')) {
      name = "homsOrderData${capitalize(name)}"
    }
    if (name.endsWith('UploadedFile') || name.endsWith('FileUpload')) {
      return
    }
    if (!raw) {
      if (name.endsWith('CSV')) {
        try {
        /*
        servicesCSV: new CSV([
          [
            n_price_line_id: 123431201,
            n_quant: null,
            vc_rem: 'test',
            c_flag: true
          ],[
            n_price_line_id: 123431301,
            n_quant: 1,
            vc_rem: null,
            c_flag: false
          ]
        ])
        ->
        servicesCSV: """
        n_price_line_id;n_quant;vc_rem;c_flag
        123431201;null;test;Y
        123431301;1;;N
        """
        */
          value = value as String
        } catch (Exception e) {}
      }
      if (name.endsWith('List') || name.endsWith('JSON') || name.endsWith('Map')) {
        try {
          // servicesMap: [a:1, b:'2'] -> servicesMap: '{a:1, b:'2'}'
          // serviceList: [1, 2, '3']  -> serviceList: "[1,2,'3']"
          value = JSON.to(value)
        } catch (Exception e) {}
      }
      if (isString(value)) {
        // Convert GStringImpl to String
        // "text" -> 'text'
        value = value.toString()
      }
      if (isDate(value)) {
        // ZonedDateTime -> '2019-12-31T23:59:59+03:00'
        // LocalDateTime, LocalDate, Date -> '2019-12-31T23:59:59'
        value = iso(value)
      }
      if (isNumber(value)) {
        // BigDecimal and BigInteger values are stored in serializable format, so convert them to String
        // 12101201 -> '12101201'
        value = value.toString()
      }
    }
    execution.setVariable(name, value)
  }

  static void setValueRaw(CharSequence name, def value, DelegateExecution execution) {
    setValue(name, value, true, execution)
  }

  static void setValue(DelegateExecution execution, CharSequence name, def value, Boolean raw = false) {
    setValue(name, value, raw, execution)
  }

  static void setValueRaw(DelegateExecution execution, CharSequence name, def value) {
    setValue(execution, name, value, true)
  }

  void setValue(CharSequence name, def value, Boolean raw = false) {
    this.getClass().setValue(name, value, raw, this._execution)
  }

  void setValueRaw(CharSequence name, def value) {
    setValue(name, value, true)
  }

  void setProperty(String propertyName, def newValue) { // do not change type to CharSequence, dynamic access will not work
    setValue(propertyName, newValue)
  }

  void putAt(CharSequence name, def value) {
    setValue(name, value)
  }

  Order plus(Map data) {
    setValue(data.name, data.value)
    return this
  }

  void setVariable(CharSequence name, def value, Boolean raw = false) {
    setValue(name, value, raw)
  }

  void setVariableRaw(CharSequence name, def value) {
    setVariable(name, value, true)
  }

  static void saveData(Map data, Boolean raw = false, DelegateExecution execution) {
    data.each { key, value ->
      setValue(key, value, raw, execution)
    }
  }

  static void saveDataRaw(Map data, DelegateExecution execution) {
    saveData(data, execution, true)
  }

  static void saveData(DelegateExecution execution, Map data, Boolean raw = false) {
    saveData(data, raw, execution)
  }

  static void saveDataRaw(DelegateExecution execution, Map data) {
    saveData(execution, data, true)
  }

  void saveData(Map data, Boolean raw = false) {
    saveData(data, raw, this._execution)
  }

  void saveDataRaw(Map data) {
    saveData(data, true, this._execution)
  }

  static void removeValue(CharSequence name, DelegateExecution execution) {
    if (!name.startsWith('homsOrderData')) {
      name = "homsOrderData${capitalize(name)}"
    }
    execution.removeValue(name)
  }

  static void removeValue(DelegateExecution execution, CharSequence name) {
    removeValue(name, execution)
  }

  Order minus(CharSequence name) {
    removeValue(name)
    return this
  }

  void removeValue(CharSequence name) {
    this.getClass().removeValue(name, this._execution)
  }

  static List getFiles(CharSequence prefix = '', DelegateExecution execution) {
    return getValue("${prefix}FileList", execution)
  }

  List getFiles(CharSequence prefix = '') {
    return this.class.getFiles(prefix, this._execution)
  }

  static List setFiles(CharSequence prefix = '', def value, DelegateExecution execution) {
    return setValue("${prefix}FileList", value, execution)
  }

  List setFiles(CharSequence prefix = '', def value) {
    return this.class.setFiles(prefix, value, this._execution)
  }

  static Map getFile(CharSequence name, CharSequence prefix = '', DelegateExecution execution) {
    List files = getFiles(prefix, execution)
    files.each { Map file ->
      if (file.origin_name == name || file.real_name == name) {
        return file
      }
    }
    return null
  }

  Map getFile(CharSequence name, CharSequence prefix = '') {
    return this.class.getFile(name, prefix, this._execution)
  }

  static List getFilesContent(CharSequence prefix = '', DelegateExecution execution) {
    List result = []
    List files  = getFiles(prefix, execution)
    if (files) {
      Minio minio  = new Minio(execution)
      files.each { Map file ->
        result += file + [
          name    : file.origin_name,
          content : minio.getFileContent(file.bucket, file.real_name)
        ]
      }
    }
    return result
  }

  List getFilesContent(CharSequence prefix = '') {
    return getFilesContent(prefix, this._execution)
  }

  static Map getFileContent(CharSequence name, CharSequence prefix = '', DelegateExecution execution) {
    LinkedHashMap file = getFile(name, prefix, execution)
    if (file) {
      Minio minio  = new Minio(execution)
      return file + [
        name    : file.origin_name,
        content : minio.getFileContent(file.bucket, file.real_name)
      ]
    } else {
      return null
    }
  }

  Map getFileContent(CharSequence name, CharSequence prefix = '') {
    return getFileContent(name, prefix, this._execution)
  }
}