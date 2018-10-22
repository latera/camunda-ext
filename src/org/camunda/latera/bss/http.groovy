package org.camunda.latera.bss.http

import groovyx.net.http.RESTClient
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseException
import org.apache.commons.io.IOUtils
import org.camunda.latera.bss.logging.Logging
import org.slf4j.Logger

class HTTPRestProcessor {
  public RESTClient httpClient

  HTTPRestProcessor(parameters) {
    this.httpClient = new RESTClient(parameters.baseUrl)
  }

  def private responseBlock(Boolean failure=false, Logger logger = null) {
    {resp, reader ->
      def respStatusLine = resp.statusLine

      Logging.log("Response status: ${respStatusLine}", "info", logger)
      Logging.log("Response data: -----", "info", logger)
      if (reader) {
        if (reader instanceof InputStreamReader) {
          Logging.log(IOUtils.toString(reader), "info", logger)
        } else {
          Logging.log(reader.toString(), "info", logger)
        }
      }
      Logging.log("--------------------", "info", logger)

      if (failure) {
        throw new HttpResponseException(resp)
      } else {
        reader
      }
    }
  }

  def sendRequest(params, String method) {
    Logger logger = params.logger
    params.remove('logger')

    if (!params.requestContentType) {
      params.requestContentType = ContentType.JSON
    }

    Logging.log("/ Sending HTTP ${method.toUpperCase()} request (${httpClient.defaultURI}${params.path})...", "info", logger)
    if (params.body) {
      Logging.log("Request data: ------", "info", logger)
      Logging.log(params.body.toString(), "info", logger)
      Logging.log("--------------------", "info", logger)
    }

    httpClient.handler.success = responseBlock(false, logger)
    httpClient.handler.failure = responseBlock(true, logger)

    def result = httpClient."${method}"(params)
    Logging.log("\\ HTTP request sent", "info", logger)
    result
  }
}
