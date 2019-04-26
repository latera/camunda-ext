package org.camunda.latera.bss.connectors.hoper.hydra

import org.camunda.latera.bss.utils.Base64Converter

trait Subject {
  LinkedHashMap getSubjectFile(LinkedHashMap input) {
    def params = [
      subjectId : null,
      id        : null
    ] + input
    def file = hoper.sendRequest(path: "subjects/${params.subjectId}/files/${params.id}")?.file
    if (file) {
      file.content = Base64Converter.from(file.base64_content)
    }
    return file
  }

  LinkedHashMap getSubjectFile(def subjectId, def id) {
    return getSubjectFile(subjectId: subjectId, id: id)
  }

  List getSubjectFiles(def subjectId) {
    def files  = hoper.sendRequest(path: "subjects/${subjectId}/files")?.files
    def result = []
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

  LinkedHashMap putSubjectFile(LinkedHashMap input) {
    def params = [
      subjectId : null,
      id        : null,
      name      : '',
      content   : [] as byte[]
    ] + input

    def result = null
    try {
      def file = [
        file_name      : params.name,
        base64_content : params.content ? Base64Converter.to(params.content) : ''
      ]

      logger.info("Putting subject ${params.subjectId} file id ${params.id} and name ${params.name}")

      if (params.id != null) {
        result = hoper.sendRequest(path: "subjects/${params.subjectId}/files/${params.id}", body: [file: file], 'put')?.file
      } else {
        result = hoper.sendRequest(path: "subjects/${params.subjectId}/files", body: [file: file], 'post')?.file
      }
      logger.info("   File ${result.n_subj_file_id} was put successfully!")
    } catch (Exception e) {
      logger.error("   Error while putting subject file")
      logger.error(e)
    }
    return result
  }

  List putSubjectFiles(def subjectId, List input) {
    def result = []
    input.each { item ->
      item.subjectId = subjectId
      result += putSubjectFile(item)
    }
  }

  Boolean deleteSubjectFile(LinkedHashMap input) {
    def params = [
      subjectId : null,
      id        : null
    ] + input

    try {
      logger.info("Deleting subject ${params.subjectId} file id")
      hoper.sendRequest(path: "subjects/${params.subjectId}/files/${params.id}", 'delete')
      logger.info("   File was deleted successfully!")
      return true
    } catch (Exception e) {
      logger.error("   Error while deleting subject file")
      logger.error(e)
      return false
    }
  }

  Boolean deleteSubjectFile(def subjectId, def id) {
    return deleteSubjectFile(subjectId: subjectId, id: id)
  }
}