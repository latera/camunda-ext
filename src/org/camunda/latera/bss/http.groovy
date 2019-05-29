package org.camunda.latera.bss.http

import java.net.URL
import groovyx.net.http.HttpBuilder
import groovyx.net.http.FromServer
import groovyx.net.http.HttpException
import org.apache.commons.io.IOUtils
import org.camunda.latera.bss.logging.SimpleLogger
import org.camunda.latera.bss.utils.StringUtil
import org.camunda.latera.bss.utils.JSON

class HTTPRestProcessor {
  HttpBuilder httpClient
  String baseUrl
  String basePath
  SimpleLogger logger
  Boolean supressRequestBodyLog
  Boolean supressResponseBodyLog

  HTTPRestProcessor(LinkedHashMap params) {
    this.logger = new SimpleLogger(params.execution)
    params.remove('execution')

    this.baseUrl  = params.baseUrl
    this.basePath = parsePath(this.baseUrl)
    this.supressRequestBodyLog  = false
    this.supressResponseBodyLog = false

    if (params.supressRequestBodyLog  != null) {
      this.supressRequestBodyLog  = params.supressRequestBodyLog
      params.remove('supressRequestBodyLog')
    }
    if (params.supressResponseBodyLog != null) {
      this.supressResponseBodyLog = params.supressResponseBodyLog
      params.remove('supressResponseBodyLog')
    }
    this.httpClient = HttpBuilder.configure {
      request.uri = this.baseUrl.toString()
      params.remove('baseUrl')

      request.contentType = 'application/json'
      response.success responseBlock(false, this.supressRequestBodyLog)
      response.failure responseBlock(true,  this.supressResponseBodyLog)

      client.clientCustomizer { it.followRedirects = true }

      if (StringUtil.notEmpty(params.user) && StringUtil.notEmpty(params.password)) {
        request.auth.basic(params.user, params.password)
        params.remove('user')
        params.remove('password')
      }

      if (params) {
        if (params.client) {
          params.client.each { k,v ->
            client."${k}" = v
          }
          params.remove('client')
        }
        if (params.headers) {
          params.headers.each { k,v ->
            request.headers."${k}" = v
          }
          params.remove('headers')
        }
        params.each { k,v ->
          request."${k}" = v
        }
      }
    }
  }

  def private responseBlock(Boolean failure = false, Boolean supress = false) {
    {FromServer response, Object data ->
      logger.info("Response status: ${response.statusCode}")
      logger.info("Content-Type: ${response.contentType}")
      logger.info("Response data: -----")
      if (data) {
        if (!supress) {
          if (data instanceof byte[]) {
            logger.info(new String(data))
          } else {
            logger.info(data.toString())
          }
        } else {
          logger.info("*Supressing response data*")
        }
      } else {
        logger.info("*Empty response*")
      }
      logger.info("--------------------")

      if (failure) {
        throw new HttpException(response, data)
      } else {
        data
      }
    }
  }

  def sendRequest(Map params, String method = 'get') {
    Boolean supressRequestBody  = false
    Boolean supressResponseBody = false
    if (params.supressRequestBodyLog != null) {
      supressRequestBody = params.supressRequestBodyLog
      params.remove('supressRequestBodyLog')
    } else if (this.supressRequestBodyLog) {
      supressRequestBody = true
    }
    if (params.supressResponseBodyLog != null) {
      supressResponseBody = params.supressResponseBodyLog
      params.remove('supressResponseBodyLog')
    } else if (this.supressResponseBodyLog) {
      supressResponseBody = true
    }

    logger.info("/ Sending HTTP ${method.toUpperCase()} request (${absoluteUrl(params.path)})...")

    if (params.body) {
      params.body = JSON.escape(params.body)
    }
    if (params.body || params.query) {
      logger.info("Request data: ------")
      if (!supressRequestBody) {
        logger.info(params.body ? params.body.toString() : params.query.toString())
      } else {
        logger.info("*Supressing request data*")
      }
      logger.info("--------------------")
    }

    def result = httpClient."${method}" {
      response.success responseBlock(false, supressResponseBody)
      response.failure responseBlock(true,  supressResponseBody)

      if (params.path) {
        request.uri.path = absolutePath(params.path.toString())
        params.remove('path')
      }
      if (params.query) {
        request.uri.query = params.query
        params.remove('query')
      }
      params.each { k,v ->
        request."${k}" = v
      }
    }
    logger.info("\\ HTTP request sent")
    result
  }

  private String parsePath(String rawUrl) {
    // baseUrl http://homs:3000/api
    // return /api
    URL absolute = new URL(rawUrl)
    return absolute.getPath()
  }

  // https://github.com/http-builder-ng/http-builder-ng/issues/210
  private String absolutePath(String path) {
    // baseUrl http://homs:3000/api
    if (StringUtil.isEmpty(path)) {
      return basePath
    }
    // path /task
    if (path.getAt(0) == '/') {
      return path // /task
    }
    // path task
    return "${basePath}/${path}".toString() // /api/task
  }

  private String absoluteUrl(String path) {
    // baseUrl http://homs:3000
    if (StringUtil.isEmpty(path)) {
      return baseUrl
    }
    // path /api
    if (path.getAt(0) == '/') {
      return "${baseUrl}${path}".toString() // http://homs:3000/api
    }
    // path api
    return "${baseUrl}/${path}".toString() // http://homs:3000/api
  }
}