package org.camunda.latera.bss.utils

import groovy.lang.GroovyObject
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.latera.bss.utils.StringUtil
import org.camunda.latera.bss.utils.JSON
import org.camunda.latera.bss.connectors.Minio

class Order implements GroovyObject {
  private DelegateExecution _execution

  Order(DelegateExecution execution) {
    this._execution  = execution
  }

  LinkedHashMap getData() {
    return getData(this._execution)
  }

  def asType(Class target) {
    switch (target) {
      case Map : return getData()
      default  : return this
    }
  }

  static LinkedHashMap getData(DelegateExecution execution) {
    LinkedHashMap data = [:]
    execution.getVariables().each { key, value ->
      if (key =~ /^homsOrderData/ && key != 'homsOrderDataUploadedFile') {
        String dataKey = key.replaceFirst(/^homsOrderData/, '')
        data[StringUtil.decapitalize(dataKey)] = value
      }
    }
    return data
  }

  void saveData(Map data) {
    saveData(data, this._execution)
  }

  static void saveData(Map data, DelegateExecution execution) {
    data.each { key, value ->
      if (key != 'uploadedFile') {
        execution.setVariable("homsOrderData${StringUtil.capitalize(key)}", value)
      }
    }
  }

  static void saveData(DelegateExecution execution, Map data) {
    saveData(execution, data)
  }

  def getProperty(String propertyName) {
    return getValue(propertyName)
  }

  def getAt(CharSequence name) {
    return getValue(name)
  }

  def getVariable(CharSequence name) {
    return getValue(name)
  }

  def getValue(CharSequence name) {
    if (name == 'data') {
      return this.getClass().getData(this._execution)
    }
    return this.getClass().getValue(name, this._execution)
  }

  static def getValue(CharSequence name, DelegateExecution execution) {
    if (name.startsWith('homsOrderData')) {
      return execution.getVariable(name)
    } else {
      return execution.getVariable("homsOrderData${StringUtil.capitalize(name)}")
    }
  }

  static def getValue(DelegateExecution execution, String name) {
    return getValue(name, execution)
  }

  void setProperty(String propertyName, def newValue) {
    setValue(propertyName, newValue)
  }

  void putAt(CharSequence name, def value) {
    setValue(name, value)
  }

  def plus(Map data) {
    setValue(data.name, data.value)
    return this
  }

  void setVariable(CharSequence name, def value) {
    setValue(name, value)
  }

  void setValue(CharSequence name, def value) {
    this.getClass().setValue(name, value, this._execution)
  }

  static void setValue(CharSequence name, def value, DelegateExecution execution) {
    if (StringUtil.isString(value)) {
      value = value.toString()
    }
    if (name.startsWith('homsOrderData')) {
      execution.setVariable(name, value)
    } else {
      execution.setVariable("homsOrderData${StringUtil.capitalize(name)}", value)
    }
  }

  static void setValue(DelegateExecution execution, CharSequence name, def value) {
    setValue(name, value, execution)
  }

  def minus(CharSequence name) {
    removeValue(name)
    return this
  }

  void removeValue(CharSequence name) {
    this.getClass().removeValue(name, this._execution)
  }

  static void removeValue(CharSequence name, DelegateExecution execution) {
    if (name.startsWith('homsOrderData')) {
      execution.removeValue(name)
    } else {
      execution.removeValue("homsOrderData${StringUtil.capitalize(name)}")
    }
  }

  static void removeValue(DelegateExecution execution, CharSequence name) {
    removeValue(name, execution)
  }

  List getFiles() {
    return this.class.getFiles(this._execution)
  }

  static List getFiles(DelegateExecution execution) {
    return JSON.from(execution.getVariable('homsOrderDataFileList') ?: '[]')
  }

  LinkedHashMap getFile(CharSequence name) {
    return this.class.getFile(name, this._execution)
  }

  static LinkedHashMap getFile(CharSequence name, DelegateExecution execution) {
    def files = getFiles(execution)
    files.each { file ->
      if (file.origin_name == name || file.real_name == name) {
        return file
      }
    }
    return null
  }

  List getFilesContent() {
    return getFilesContent(this._execution)
  }

  static List getFilesContent(DelegateExecution execution) {
    def result = []
    def files  = getFiles(execution)
    if (files) {
      def minio  = new Minio(execution)
      files.each { file ->
        result += file + [
          name    : file.origin_name,
          content : minio.getFileContent(file.bucket, file.real_name)
        ]
      }
    }
    return result
  }

  LinkedHashMap getFileContent(CharSequence name) {
    return getFileContent(name, this._execution)
  }

  static LinkedHashMap getFileContent(CharSequence name, DelegateExecution execution) {
    def file = getFile(name, execution)
    if (file) {
      def minio  = new Minio(execution)
      return file + [
        name    : file.origin_name,
        content : minio.getFileContent(file.bucket, file.real_name)
      ]
    } else {
      return null
    }
  }
}