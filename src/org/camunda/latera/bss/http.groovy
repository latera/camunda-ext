package org.camunda.latera.bss.http

import groovyx.net.http.RESTClient
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseException
import org.apache.commons.io.IOUtils
import org.camunda.latera.bss.logging.SimpleLogger

class HTTPRestProcessor {
  public RESTClient httpClient
  SimpleLogger logger

  HTTPRestProcessor(Map parameters) {
    this.httpClient = new RESTClient(parameters.baseUrl)
    this.logger = new SimpleLogger(parameters.execution)
  }

  def private responseBlock(Boolean failure=false, Boolean supress=false) {
    {resp, reader ->
      def respStatusLine = resp.statusLine

      logger.log("Response status: ${respStatusLine}", "info")
      logger.log("Response data: -----", "info")
      if (!supress && reader) {
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

  def sendRequest(Map params, String method) {

    if (!params.requestContentType) {
      params.requestContentType = ContentType.JSON
    }

    logger.log("/ Sending HTTP ${method.toUpperCase()} request (${httpClient.defaultURI}${params.path})...", "info")
    if (params.body) {
      logger.log("Request data: ------", "info")
      if (!params.supressRequestBodyLog) {
        logger.log(params.body.toString(), "info")
      }
      logger.log("--------------------", "info")
    }

    httpClient.handler.success = responseBlock(false, params.supressResponseBodyLog)
    httpClient.handler.failure = responseBlock(true, params.supressResponseBodyLog)

    params.remove('supressRequestBodyLog')
    params.remove('supressResponseBodyLog')

    def result = httpClient."${method}"(params)
    logger.log("\\ HTTP request sent", "info")
    result
  }
}
