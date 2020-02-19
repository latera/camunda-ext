package org.camunda.latera.bss.connectors

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.latera.bss.http.HTTPRestProcessor
import org.camunda.latera.bss.logging.SimpleLogger
import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import static org.camunda.latera.bss.utils.StringUtil.joinNonEmpty
import java.security.MessageDigest
import io.vavr.control.Try

class PlanadoV2 {
  HTTPRestProcessor http
  SimpleLogger logger

  PlanadoV2(DelegateExecution execution) {
    this.logger = new SimpleLogger(execution)
    def ENV     = System.getenv()

    String url   = execution.getVariable('planadoUrl')                                             ?: ENV['PLANADO_URL']   ?: 'https://api.planadoapp.com'
    String token = execution.getVariable('planadoToken') ?: execution.getVariable('planadoApiKey') ?: ENV['PLANADO_TOKEN'] ?: ENV['PLANADO_API_KEY']

    LinkedHashMap headers = ['X-Planado-Api-Token': token]

    this.http = new HTTPRestProcessor(
      baseUrl   : url,
      headers   : headers,
      execution : execution
    )
  }

  private String makeExtId(CharSequence input) {
    logger.info('Generating externalId for Planado entity')
    def messageDigest = MessageDigest.getInstance('MD5')
    messageDigest.update(input.getBytes())
    return new BigInteger(1, messageDigest.digest()).toString(16)
  }

  private String makeExtId(List input) {
    return makeExtId(joinNonEmpty(input, ';'))
  }

  Map getClient(def extId) {
    sendRequest('get', "clients/${extId}")
            .map { r -> r.client }
            .getOrNull()
  }

  Map getClients() {
    sendRequest('get', 'clients').getOrNull()
  }

  Boolean deleteClient(def extId) {
    sendRequest('delete', "clients/${extId}").isSuccess()
  }

  Map findOrCreateClient(Map data) {
    if (!data.contactName) {
      data.contactName = joinNonEmpty([
        data.firstName,
        data.middleName,
        data.lastName
      ], ' ')
    }

    String extId = data.extId ?: makeExtId([
      data.firstName,
      data.middleName,
      data.lastName,
      data.contactName,
      data.addressStreet,
      data.addressEntrance,
      data.addressFloor,
      data.addressApartment,
      data.phone
    ])

    logger.info('Checking if client exists')
    LinkedHashMap existingUser = getClient(extId)

    if (existingUser) {
      logger.info('Client exists')
      return existingUser
    }

    ['phone',
     'addressEntrance',
     'addressFloor',
     'addressApartment',
     'addressLat',
     'addressLon'].each { CharSequence field ->
      if (data[field] != null) {
        data[field] = data[field].toString()
      }
    }

    LinkedHashMap payload = [
      external_id   : extId,
      organization  : false,
      first_name    : data.firstName          ?: '',
      last_name     : data.lastName           ?: '',
      middle_name   : data.middleName         ?: '',
      site_address  : [
        formatted   : data.addressStreet      ?: '',
        apartment   : data.addressApartment   ?: '',
        floor       : data.addressFloor       ?: '',
        entrance_no : data.addressEntrance    ?: '',
        description : data.addressDescription ?: ''
      ],
      contacts : [[
        name  : data.contactName              ?: '',
        value : data.phone                    ?: '',
        type  : 'phone'
      ]]
    ]

    if (data.addressLat && data.addressLon) {
      payload.site_address.geolocation = [
        latitude  : data.addressLat,
        longitude : data.addressLon
      ]
    }

    logger.info('Creating new client')

    sendRequest('post', 'clients', body: payload).getOrNull()
  }

  Map findOrCreateOrganization(Map data) {
    if (!data.contactName) {
      data.contactName = joinNonEmpty([
        data.firstName,
        data.middleName,
        data.lastName
      ], ' ')
    }

    String extId = data.extId ?: makeExtId([
      data.companyName,
      data.contactName,
      data.addressStreet,
      data.addressEntrance,
      data.addressFloor,
      data.addressApartment,
      data.phone
    ])

    logger.info('Checking if company exists')
    LinkedHashMap existingCompany = getClient(extId)

    if (existingCompany) {
      logger.info('Company exists')
      return existingCompany
    }

    ['phone',
     'addressEntrance',
     'addressFloor',
     'addressApartment',
     'addressLat',
     'addressLon'].each { CharSequence field ->
      if (data[field] != null) {
        data[field] = data[field].toString()
      }
    }

    LinkedHashMap payload = [
      external_id       : extId,
      organization      : true,
      organization_name : data.companyName    ?: '',
      site_address      : [
        formatted   : data.addressStreet      ?: '',
        entrance_no : data.addressEntrance    ?: '',
        floor       : data.addressFloor       ?: '',
        apartment   : data.addressApartment   ?: '',
        description : data.addressDescription ?: ''
      ],
      contacts : [[
        name  : data.contactName              ?: '',
        value : data.phone                    ?: '',
        type  : 'phone'
      ]]
    ]

    if (data.addressLat && data.addressLon) {
      payload.site_address.geolocation = [
        latitude  : data.addressLat,
        longitude : data.addressLon
      ]
    }

    logger.info('Creating new company')

    sendRequest('post', 'clients', body: payload).getOrNull()
  }

  Boolean deleteJob(def jobId) {
    sendRequest('delete', "jobs/${jobId}").isSuccess()
  }

  Map createJob(Map data) {
    if (data.extId && !data.clientId) {
      data.clientId = getClient(data.extId)?.client_id
    }

    LinkedHashMap payload = [
      template_id   : toIntSafe(data.templateId),
      client_id     : data.clientId,
      scheduled_at  : data.startDate,
      description   : data.description  ?: '',
      custom_fields : data.customFields ?: []
    ]

    logger.info('Creating new job')

    sendRequest('post', 'jobs', body: payload).getOrNull()
  }

  Map updateJob(Map data, def jobId) {
    logger.info("Updating job ${jobId}")

    LinkedHashMap payload = [:].with {
      if (data.containsKey('description')) {
        description = data.description
      }
      if (data.containsKey('customFields')) {
        custom_fields = data.customFields
      }

      it
    }

    if (payload.size() == 0) {
      throw new Exception("No params for update")
    }

    sendRequest('patch', "jobs/${jobId}", body: payload).getOrNull()
  }

  Map getJob(def jobId) {
    sendRequest('get', "jobs/${jobId}").getOrNull()
  }

  Map getJobTemplate(def templateId) {
    sendRequest('get', "templates/${templateId}").getOrNull()
  }

  Try sendRequest(Map payload = [:], CharSequence method, CharSequence path) {
    payload.path = "/api/v2/${path}".toString()

    return Try.of({ http.sendRequest(payload, method) })
            .onFailure { Exception e -> logger.error(e) }
  }
}
