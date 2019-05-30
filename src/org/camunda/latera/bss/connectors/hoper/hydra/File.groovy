package org.camunda.latera.bss.connectors.hoper.hydra

import org.camunda.latera.bss.utils.Base64Converter

trait File {
  private static LinkedHashMap FILE_ENTITY_TYPE = [
    one    : 'file',
    plural : 'files'
  ]

  LinkedHashMap getFileEntityType(def parentType, def id = null) {
    return FILE_ENTITY_TYPE + withParent(parentType) + withId(id)
  }

  LinkedHashMap getSubjectFileEntityType(def subjectId, def id = null) {
    return getFileEntityType(getSubjectEntityType(subjectId), id)
  }

  LinkedHashMap getFileDefaultParams() {
    return [
      name    : '',
      content : [] as byte[]
    ]
  }

  LinkedHashMap getFileParamsMap(LinkedHashMap params) {
    return [
      file_name      : params.name,
      base64_content : params.content ? Base64Converter.to(params.content) : ''
    ]
  }

  LinkedHashMap getFileParams(LinkedHashMap input) {
    def params = getFileDefaultParams() + input
    def data   = getFileParamsMap(params)
    return nvlParams(data)
  }

  List getSubjectFiles(def subjectId, LinkedHashMap input = [:]) {
    def params = getPaginationDefaultParams() + input

    def result = []
    def files  = getEntities(getSubjectFileEntityType(subjectId), params)
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

  LinkedHashMap getSubjectFile(def subjectId, def fileId) {
    def file = getEntity(getSubjectFileEntityType(subjectId), fileId)
    if (file) {
      def result = [
        n_subj_file_id : file.n_subj_file_id,
        file_name      : file.file_name,
        content        : Base64Converter.from(file.base64_content)
      ]
      return result
    }
    return file
  }

  LinkedHashMap getSubjectFile(LinkedHashMap input) {
    return getSubjectFile(input.subjectId, input.fileId)
  }

  LinkedHashMap createSubjectFile(def subjectId, LinkedHashMap input = [:]) {
    LinkedHashMap params = getFileParams(input)
    return createEntity(getSubjectFileEntityType(subjectId), params)
  }

  LinkedHashMap updateSubjectFile(def subjectId, def fileId, LinkedHashMap input) {
    LinkedHashMap params = getFileParams(input)
    return updateEntity(getSubjectFileEntityType(subjectId), fileId, params)
  }

  LinkedHashMap putSubjectFile(def subjectId, LinkedHashMap input) {
    def fileId = input.fileId
    input.remove('fileId')

    if (fileId) {
      return updateSubjectFile(subjectId, fileId, input)
    } else {
      return createSubjectFile(subjectId, input)
    }
  }

  LinkedHashMap putSubjectFile(LinkedHashMap input) {
    def subjectId = input.subjectId
    input.remove('subjectId')
    return putSubjectFile(subjectId, input)
  }

  List putSubjectFiles(def subjectId, List input) {
    def result = []
    input.each { item ->
      result += putSubjectFile(subjectId, item)
    }
    return result
  }

  Boolean deleteSubjectFile(def subjectId, def fileId) {
    return deleteEntity(getSubjectFileEntityType(subjectId), fileId)
  }

  Boolean deleteSubjectFile(LinkedHashMap input) {
    return deleteSubjectFile(input.subjectId, input.fileId)
  }
}