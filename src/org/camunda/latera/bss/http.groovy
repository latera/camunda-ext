package org.camunda.latera.bss.http

import groovyx.net.http.RESTClient
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseException
import org.apache.commons.io.IOUtils
import org.camunda.latera.bss.logging.SimpleLogger

class HTTPRestProcessor {
  public RESTClient httpClient
  SimpleLogger logger

  HTTPRestProcessor(parameters) {
    this.httpClient = new RESTClient(parameters.baseUrl)
    this.logger = new SimpleLogger(parameters.execution)

    if (parameters.user && parameters.password) {
      this.httpClient.auth.basic(parameters.user, parameters.password)
    }
  }

  def private responseBlock(Boolean failure=false) {
    {resp, reader ->
      def respStatusLine = resp.statusLine

      logger.log("Response status: ${respStatusLine}", "info")
      logger.log("Response data: -----", "info")
      if (reader) {
        if (reader instanceof InputStreamReader) {
          logger.log(IOUtils.toString(reader), "info")
        } else {
          logger.log(reader.toString(), "info")
        }
      }
      logger.log("--------------------", "info")

      if (failure) {
        throw new HttpResponseException(resp)
      } else {
        reader
      }
    }
  }

  def sendRequest(params, String method) {

    if (!params.requestContentType) {
      params.requestContentType = ContentType.JSON
    }

    logger.log("/ Sending HTTP ${method.toUpperCase()} request (${httpClient.defaultURI}${params.path})...", "info")
    if (params.body) {
      logger.log("Request data: ------", "info")
      logger.log(params.body.toString(), "info")
      logger.log("--------------------", "info")
    }

    httpClient.handler.success = responseBlock(false)
    httpClient.handler.failure = responseBlock(true)

    def result = httpClient."${method}"(params)
    logger.log("\\ HTTP request sent", "info")
    result
  }
}
