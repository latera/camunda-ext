package org.camunda.latera.bss.connectors.hoper.hydra

import org.camunda.latera.bss.utils.Base64Converter

trait File {
  private static LinkedHashMap FILE_ENTITY_TYPE = [
    one    : 'file',
    plural : 'files'
  ]

  Map getFileEntityType(def parentType, def id = null) {
    return FILE_ENTITY_TYPE + withParent(parentType) + withId(id)
  }

  Map getSubjectFileEntityType(def subjectId, def id = null) {
    return getFileEntityType(getSubjectEntityType(subjectId), id)
  }

  private Map getFileDefaultParams() {
    return [
      name    : '',
      content : [] as byte[]
    ]
  }

  private Map getFileParamsMap(Map params) {
    return [
      file_name      : params.name,
      base64_content : params.content ? Base64Converter.to(params.content) : ''
    ]
  }

  private Map getFileParams(Map input) {
    LinkedHashMap params = getFileDefaultParams() + input
    LinkedHashMap data   = getFileParamsMap(params)
    return prepareParams(data)
  }

  List getSubjectFiles(def subjectId, Map input = [:]) {
    LinkedHashMap params = getPaginationDefaultParams() + input

    List result = []
    List files  = getEntities(getSubjectFileEntityType(subjectId), params)
    if (files) {
      files.each { it ->
        result.add([
          n_subj_file_id : it.n_subj_file_id,
          file_name      : it.file_name,
          content        : Base64Converter.from(it.base64_content)
        ])
      }
    }
    return result
  }

  Map getSubjectFile(def subjectId, def fileId) {
    LinkedHashMap file = getEntity(getSubjectFileEntityType(subjectId), fileId)
    if (file) {
      LinkedHashMap result = [
        n_subj_file_id : file.n_subj_file_id,
        file_name      : file.file_name,
        content        : Base64Converter.from(file.base64_content)
      ]
      return result
    }
    return file
  }

  Map getSubjectFile(Map input) {
    return getSubjectFile(input.subjectId, input.fileId)
  }

  Map createSubjectFile(def subjectId, Map input = [:]) {
    LinkedHashMap params = getFileParams(input)
    return createEntity(getSubjectFileEntityType(subjectId), params)
  }

  Map createSubjectFile(Map input) {
    def subjectId = input.subjectId
    input.remove('subjectId')
    return createSubjectFile(subjectId, input)
  }

  Map createSubjectFile(Map input, def subjectId) {
    return createSubjectFile(subjectId, input)
  }

  List createSubjectFiles(def subjectId, List input) {
    List result = []
    input.each { Map item ->
      result += createSubjectFile(item, subjectId)
    }
    return result
  }

  Map updateSubjectFile(def subjectId, def fileId, Map input) {
    LinkedHashMap params = getFileParams(input)
    return updateEntity(getSubjectFileEntityType(subjectId), fileId, params)
  }

  Map updateSubjectFile(Map input) {
    def subjectId = input.subjectId
    input.remove('subjectId')
    def fileId = input.fileId
    input.remove('fileId')
    return updateSubjectFile(subjectId, fileId, input)
  }

  Map updateSubjectFile(Map input, def subjectId, def fileId) {
    return updateSubjectFile(subjectId, fileId, input)
  }

  List updateSubjectFiles(def subjectId, List input) {
    List result = []
    input.each { Map item ->
      result += updateSubjectFile(ite + [subjectId: subjectId])
    }
    return result
  }

  Map putSubjectFile(def subjectId, Map input) {
    def fileId = input.fileId
    input.remove('fileId')

    if (fileId) {
      return updateSubjectFile(subjectId, fileId, input)
    } else {
      return createSubjectFile(subjectId, input)
    }
  }

  Map putSubjectFile(Map input) {
    def subjectId = input.subjectId
    input.remove('subjectId')
    return putSubjectFile(subjectId, input)
  }

  Map putSubjectFile(Map input, def subjectId) {
    return putSubjectFile(subjectId, input)
  }

  List putSubjectFiles(def subjectId, List input) {
    List result = []
    input.each { item ->
      result += putSubjectFile(subjectId, item)
    }
    return result
  }

  Boolean deleteSubjectFile(def subjectId, def fileId) {
    return deleteEntity(getSubjectFileEntityType(subjectId), fileId)
  }

  Boolean deleteSubjectFile(Map input) {
    return deleteSubjectFile(input.subjectId, input.fileId)
  }
}