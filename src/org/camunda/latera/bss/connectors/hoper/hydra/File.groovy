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

  Map getContractFileEntityType(def contractId, def id = null) {
    return getFileEntityType(getContractEntityType(contractId), id)
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

  List getSubjectFiles(Map input = [:], def subjectId) {
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

  List getContractFiles(Map input = [:], def contractId) {
    LinkedHashMap params = getPaginationDefaultParams() + input

    List result = []
    List files  = getEntities(getContractFileEntityType(contractId), params)
    if (files) {
      files.each { it ->
        result.add([
          n_doc_file_id : it.n_doc_file_id,
          file_name     : it.file_name,
          content       : Base64Converter.from(it.base64_content)
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

  Map getContractFile(def contractId, def fileId) {
    LinkedHashMap file = getEntity(getContractFileEntityType(contractId), fileId)
    if (file) {
      LinkedHashMap result = [
        n_doc_file_id : file.n_doc_file_id,
        file_name     : file.file_name,
        content       : Base64Converter.from(file.base64_content)
      ]
      return result
    }
    return file
  }

  Map getSubjectFile(Map input) {
    return getSubjectFile(input.subjectId, input.fileId)
  }

  Map getContractFile(Map input) {
    return getContractFile(input.contractId, input.fileId)
  }

  Map createSubjectFile(Map input = [:], def subjectId) {
    LinkedHashMap params = getFileParams(input)
    return createEntity(getSubjectFileEntityType(subjectId), params)
  }

  Map createContractFile(Map input = [:], def contractId) {
    LinkedHashMap params = getFileParams(input)
    return createEntity(getContractFileEntityType(contractId), params)
  }

  List createSubjectFiles(Object[] input = [], def subjectId) {
    List result = []
    input.each { Map item ->
      result += createSubjectFile(item, subjectId)
    }
    return result
  }

  List createContractFiles(Object[] input = [], def contractId) {
    List result = []
    input.each { Map item ->
      result += createContractFile(item, contractId)
    }
    return result
  }

  List createSubjectFiles(def subjectId, List input) {
    return createSubjectFiles(input as Object[], subjectId)
  }

  List createContractFiles(def contractId, List input) {
    return createContractFiles(input as Object[], contractId)
  }

  Map updateSubjectFile(Map input = [:], def subjectId, def fileId) {
    LinkedHashMap params = getFileParams(input)
    return updateEntity(getSubjectFileEntityType(subjectId), fileId, params)
  }

  Map updateContractFile(Map input = [:], def contractId, def fileId) {
    LinkedHashMap params = getFileParams(input)
    return updateEntity(getContractFileEntityType(contractId), fileId, params)
  }

  List updateSubjectFiles(Object[] input = [], def subjectId) {
    List result = []
    input.each { Map item ->
      result += updateSubjectFile(item + [subjectId: subjectId])
    }
    return result
  }

  List updateContractFiles(Object[] input = [], def contractId) {
    List result = []
    input.each { Map item ->
      result += updateContractFile(item + [contractId: contractId])
    }
    return result
  }

  List updateSubjectFiles(def subjectId, List input) {
    return updateSubjectFiles(input as Object[], subjectId)
  }

  List updateContractFiles(def contractId, List input) {
    return updateContractFiles(input as Object[], subjectId)
  }

  Boolean deleteSubjectFile(def subjectId, def fileId) {
    return deleteEntity(getSubjectFileEntityType(subjectId), fileId)
  }

  Boolean deleteContractFile(def contractId, def fileId) {
    return deleteEntity(getContractFileEntityType(contractId), fileId)
  }

  Boolean deleteSubjectFile(Map input) {
    return deleteSubjectFile(input.subjectId, input.fileId)
  }

  Boolean deleteContractFile(Map input) {
    return deleteContractFile(input.contractId, input.fileId)
  }
}
