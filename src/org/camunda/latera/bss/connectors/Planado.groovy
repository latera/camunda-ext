package org.camunda.latera.bss.connectors

import groovy.json.JsonOutput
import groovyx.net.http.HttpResponseException
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.latera.bss.http.HTTPRestProcessor
import org.camunda.latera.bss.logging.SimpleLogger
import org.apache.http.impl.client.LaxRedirectStrategy
import java.net.URLEncoder

import java.security.MessageDigest

class Planado {
  private String planadoApiKey
  private HTTPRestProcessor http
  private SimpleLogger logger

  Planado(DelegateExecution execution) {
    logger = new SimpleLogger(execution)
    planadoApiKey = execution.getVariable('planadoApiKey')
    http = new HTTPRestProcessor(baseUrl: 'https://api.planadoapp.com/api/v1/', execution: execution)
    http.httpClient.client.setRedirectStrategy(new LaxRedirectStrategy())
  }

  private String __makeExtID(String s) {
    logger.debug("Generating external ID for Planado entity")
    def messageDigest = MessageDigest.getInstance("MD5")
    messageDigest.update(s.getBytes())
    return new BigInteger(1, messageDigest.digest()).toString(16)
  }

  Object getCustomer(String extID) {
    try {
      return http.sendRequest(
          'get',
          path: "clients/${extID}.json",
          headers: ["X-Planado-Api-Token": planadoApiKey]
      )
    }
    catch (HttpResponseException ex) {
      return null
    }
  }

  Object getCustomers() {
    try {
      return http.sendRequest(
          'get',
          path: "clients.json",
          headers: ["X-Planado-Api-Token": planadoApiKey]
      )
    }
    catch (HttpResponseException ex) {
      return null
    }
  }

  void deleteCustomer(String extID) {
    try {
      http.sendRequest(
          "delete",
          path: "clients/${extID}.json",
          headers: ["X-Planado-Api-Token": planadoApiKey]
      )
    }
    catch (HttpResponseException ex) {
      logger.error(ex)
    }
  }

  String createCustomer(Map userData) {
    String extID = __makeExtID(
        [
            userData.firstName,
            userData.middleName,
            userData.lastName,
            userData.addressStreet,
            userData.addressEntrance,
            userData.addressFloor,
            userData.addressApartment,
            userData.phone
        ].findAll { it -> it.toString()?.trim()?.asBoolean() }.join(';')
    )

    if (getCustomer(extID)) {
      logger.debug("User exists")
      return extID
    }

    HashMap payload = [
        external_id : extID,
        organization: false,
        first_name  : userData.firstName,
        middle_name : userData.middleName,
        last_name   : userData.lastName,
        name        : [userData.lastName, userData.firstName].findAll { it -> it.toString()?.trim()?.asBoolean() }.join(' '),
        site_address : [
            formatted  : userData.addressStreet,
            entrance_no: userData.addressEntrance.toString(),
            floor      : userData.addressFloor.toString(),
            apartment  : userData.addressApartment,
            description: userData.addressDescription?:""
        ],
        email       : userData.email,
        cell_phone  : userData.phone
    ]

    if (userData.addressLat && userData.addressLon) payload.site_address.geolocation = [
        latitude : userData.addressLat,
        longitude: userData.addressLon
    ]

    http.sendRequest(
        'post',
        path: 'clients.json',
        body: JsonOutput.toJson(payload),
        headers: ["X-Planado-Api-Token": planadoApiKey],
        requestContentType: "application/json")

    return extID
  }

  String createCompany(
      Map companyData
  ) {
    String extID = __makeExtID(
        [
            companyData.companyName,
            companyData.addressStreet,
            companyData.addressEntrance,
            companyData.addressFloor,
            companyData.addressApartment,
            companyData.phone
        ].findAll { it -> it.toString()?.trim()?.asBoolean() }.join(';')
    )

    if (getCustomer(extID)) {
      logger.debug("Company exists")
      return extID
    }
    HashMap payload = [
        external_id      : extID,
        organization     : true,
        organization_name: companyData.companyName,
        site_address: [
            formatted  : companyData.addressStreet,
            entrance_no: companyData.addressEntrance.toString(),
            floor      : companyData.addressFloor.toString(),
            apartment  : companyData.addressApartment,
            description: companyData.addressDescription?:""
        ],
        email      : companyData.email,
        contacts   : [[
                          type : "phone",
                          name : companyData.companyName,
                          value: companyData.phone,
                          value_normalized: companyData.phone
                      ]]
    ]

    if (companyData.addressLat && companyData.addressLon) payload.site_address.geolocation = [
        latitude : companyData.addressLat,
        longitude: companyData.addressLon
    ]

    http.sendRequest(
        'post',
        path: 'clients.json',
        body: JsonOutput.toJson(payload),
        headers: ["X-Planado-Api-Token": planadoApiKey],
        requestContentType: "application/json")

    return extID
  }

  void deleteJob(Object jobID) {
    try {
      http.sendRequest(
          "delete",
          path: "jobs/${extID}.json",
          headers: ["X-Planado-Api-Token": planadoApiKey]
      )
    }
    catch (HttpResponseException ex) {
      logger.error(ex)
    }
  }

  String createJob(Map jobData) {
    HashMap payload = [
        template_id:          jobData.templateId,
        client_id:            jobData.clientId,
        scheduled_at:         jobData.startDate,
        assignee_subject_id:  jobData.assigneeId
    ]

    def res = http.sendRequest(
        'post',
        path: 'jobs.json',
        body: JsonOutput.toJson(payload),
        headers: ["X-Planado-Api-Token": planadoApiKey],
        requestContentType: "application/json")

    return res?.job_id?:null
  }

  Object getJob(Object jobID) {
    try {
      return http.sendRequest(
          'get',
          path: "jobs/${jobID.toString()}.json",
          headers: ["X-Planado-Api-Token": planadoApiKey]
      )
    }
    catch (HttpResponseException ex) {
      return null
    }
  }

  Object getJobTemplate(Object templateID) {
    try {
      return http.sendRequest(
          'get',
          path: "templates/${templateID.toString()}.json",
          headers: ["X-Planado-Api-Token": planadoApiKey]
      )
    }
    catch (HttpResponseException ex) {
      return null
    }
  }

  Object getUser(String email) {
    try {
      String emailEncoded = URLEncoder.encode(email, "UTF-8")
      return http.sendRequest(
          'get',
          // uri instead of path because of incorrect urlencode behavior for '+'s and '@'s in path
          uri: "https://api.planadoapp.com/api/v1/users/${emailEncoded}.json",
          headers: ["X-Planado-Api-Token": planadoApiKey]
      )
    }
    catch (HttpResponseException ex) {
      return null
    }
  }

  Object getUsers() {
    try {
      return http.sendRequest(
          'get',
          path: "users.json",
          headers: ["X-Planado-Api-Token": planadoApiKey]
      )
    }
    catch (HttpResponseException ex) {
      return null
    }
  }
}
