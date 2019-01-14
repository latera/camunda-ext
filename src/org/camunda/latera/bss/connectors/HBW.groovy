package org.camunda.latera.bss.connectors

import groovy.json.JsonOutput
import groovyx.net.http.ContentType
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.latera.bss.logging.SimpleLogger
import org.camunda.latera.bss.http.HTTPRestProcessor

class HBW {
  private HTTPRestProcessor http
  private SimpleLogger logger
  private String homsUser
  private String hbwToken

  HBW(DelegateExecution execution) {
    String homsUrl = execution.getVariable('homsUrl')
    homsUser = execution.getVariable('homsUser')
    hbwToken = execution.getVariable('hbwToken')

    http = new HTTPRestProcessor(execution: execution, baseUrl: homsUrl)
    http.httpClient.auth.basic(homsUser, hbwToken)

    logger = new SimpleLogger(execution)
  }

  Object upload(String name, String base64EncodedData) {
    logger.debug("Uploading file ${name}")
    HashMap body = [
        files: JsonOutput.toJson([
            [name   : name,
             content: base64EncodedData]
        ])]
    http.sendRequest('post', path: '/widget/file_upload', body: body, requestContentType: ContentType.URLENC, supressRequestBodyLog: true)?.getAt(0)
  }
}
