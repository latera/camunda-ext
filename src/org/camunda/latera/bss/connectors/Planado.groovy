package org.camunda.latera.bss.connectors

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.latera.bss.http.HTTPRestProcessor
import org.camunda.latera.bss.logging.SimpleLogger

import java.security.MessageDigest

class Planado {
  String url
  private String token
  Integer version
  HTTPRestProcessor http
  SimpleLogger logger

  Planado(DelegateExecution execution) {
    this.logger  =  new SimpleLogger(execution)
    def ENV      =  System.getenv()

    this.url     =  ENV['PLANADO_URL']     ?: 'https://api.planadoapp.com'
    this.version = (ENV['PLANADO_VERSION'] ?: ENV['PLANADO_API_VERSION'] ?: execution.getVariable('planadoVersion') ?: execution.getVariable('planadoApiVersion') ?: 1)?.toInteger()
    this.token   =  ENV['PLANADO_TOKEN']   ?: ENV['PLANADO_API_KEY']     ?: execution.getVariable('planadoToken')   ?: execution.getVariable('planadoApiKey')

    def headers = ['X-Planado-Api-Token': token]
    this.http = new HTTPRestProcessor(
      baseUrl   : url,
      headers   : headers,
      execution : execution
    )
  }

  private String makeExtId(String input) {
    logger.info('Generating externalId for Planado entity')
    def messageDigest = MessageDigest.getInstance("MD5")
    messageDigest.update(input.getBytes())
    return new BigInteger(1, messageDigest.digest()).toString(16)
  }

  private String makeExtId(List input) {
    def str = input.findAll { it -> !it?.isEmpty() }.join(';').toString()
    return makeExtId(str)
  }

  LinkedHashMap getUser(String extId) {
    try {
      return sendRequest(
        'get',
        path: "clients/${extId}.json",
      )
    }
    catch (Exception e) {
      logger.error(e)
      return null
    }
  }

  LinkedHashMap getUsers() {
    try {
      return sendRequest(
        'get',
        path: "clients.json"
      )
    }
    catch (Exception e) {
      logger.error(e)
      return null
    }
  }

  Boolean deleteUser(String extId) {
    try {
      sendRequest(
        "delete",
        path: "clients/${extId}.json"
      )
      return true
    }
    catch (Exception e) {
      logger.error(e)
      return false
    }
  }

  LinkedHashMap createUser(Map data) {
    String extId = data.extId ?: makeExtId([
      data.firstName,
      data.middleName,
      data.lastName,
      data.addressStreet,
      data.addressEntrance,
      data.addressFloor,
      data.addressApartment,
      data.phone
    ])

    logger.info('Checking if user exists')
    def existingUser = getUser(extId)
    if (existingUser) {
      logger.info("User exists")
      return existingUser
    }

    LinkedHashMap payload = [
      external_id   : extId,
      organization  : false,
      first_name    : data.firstName  ?: '',
      middle_name   : data.middleName ?: '',
      last_name     : data.lastName   ?: '',
      name          : [data.lastName, data.firstName].join(' ').trim(),
      site_address  : [
        formatted   : data.addressStreet      ?: '',
        entrance_no : data.addressEntrance    ?: '',
        floor       : data.addressFloor       ?: '',
        apartment   : data.addressApartment   ?: '',
        description : data.addressDescription ?: ''
      ],
      email         : data.email ?: '',
      cell_phone    : data.phone ?: ''
    ]

    if (data.addressLat && data.addressLon) {
      payload.site_address.geolocation = [
        latitude  : data.addressLat,
        longitude : data.addressLon
      ]
    }

    try {
      logger.info('Creating new user')
      return sendRequest(
        'post',
        path: 'clients.json',
        body: payload
      )
    } catch (Exception e) {
      logger.error(e)
      return null
    }
  }

  LinkedHashMap createCompany(Map data) {
    String extId = data.extId ?: makeExtId([
      data.companyName,
      data.addressStreet,
      data.addressEntrance,
      data.addressFloor,
      data.addressApartment,
      data.phone
    ])

    logger.info('Checking if company exists')
    def existingCompany = getUser(extId)
    if (existingCompany) {
      logger.info("Company exists")
      return existingCompany
    }

    LinkedHashMap payload = [
      external_id       : extId,
      organization      : true,
      organization_name : data.companyName     ?: '',
      site_address      : [
        formatted   : data.addressStreet       ?: '',
        entrance_no : data.addressEntrance     ?: '',
        floor       : data.addressFloor        ?: '',
        apartment   : data.addressApartment    ?: '',
        description : data.addressDescription  ?: ''
      ],
      email    : data.email                    ?: '',
      contacts : [[
                  type  : "phone",
                  name  : data.companyName     ?: '',
                  value : data.phone           ?: '',
                  value_normalized: data.phone ?: ''
                ]]
    ]

    if (data.addressLat && data.addressLon) {
      payload.site_address.geolocation = [
        latitude  : data.addressLat,
        longitude : data.addressLon
      ]
    }

    try {
      logger.info('Creating new company')
      return sendRequest(
        'post',
        path: 'clients.json',
        body: payload
      )
    } catch (Exception e) {
      logger.error(e)
      return null
    }
  }

  Boolean deleteJob(String jobId) {
    try {
      sendRequest(
        'delete',
        path: "jobs/${jobId}.json"
      )
      return true
    }
    catch (Exception e) {
      logger.error(e)
      return false
    }
  }

  LinkedHashMap createJob(Map data) {
    if (data.extId && !data.clientId) {
      data.clientId = getUser(data.extId)?.client_id
    }
    LinkedHashMap payload = [
      template_id  : data.templateId,
      client_id    : data.clientId,
      scheduled_at : data.startDate,
      description  : data.description ?: ''
    ]

    try {
      logger.info('Creating new job')
      return sendRequest(
      'post',
      path: 'jobs.json',
      body: payload
    )
    } catch (Exception e) {
      logger.error(e)
      return null
    }
  }

  LinkedHashMap getJob(String jobId) {
    try {
      return sendRequest(
        'get',
        path: "jobs/${jobId}.json"
      )
    }
    catch (Exception e) {
      return null
    }
  }

  LinkedHashMap getJobTemplate(String templateID) {
    try {
      return sendRequest(
        'get',
        path: "templates/${templateID}.json"
      )
    }
    catch (Exception e) {
      return null
    }
  }

  def sendRequest(Map input, String method = 'get') {
    input.path = "/api/v${this.version}/${input.path}".toString()
    return http.sendRequest(input, method)
  }
}
