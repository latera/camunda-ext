package org.camunda.latera.bss.connectors

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.latera.bss.http.HTTPRestProcessor
import org.camunda.latera.bss.logging.SimpleLogger

class GoogleMaps {
  String url
  String path
  private String token
  HTTPRestProcessor http
  SimpleLogger logger

  GoogleMaps(DelegateExecution execution) {
    this.logger  =  new SimpleLogger(execution)
    def ENV      =  System.getenv()

    this.url     =  ENV['GOOGLE_MAPS_URL']   ?: 'https://maps.googleapis.com/maps/api'
    this.token   =  ENV['GOOGLE_MAPS_TOKEN'] ?: execution.getVariable('googleMapsToken')

    this.http = new HTTPRestProcessor(
      baseUrl   : url,
      execution : execution
    )
  }

  def sendRequest(Map input, CharSequence method = 'get') {
    if (input.query) {
      input.query += [key: this.token]
    }
    if (input.path) {
      input.path = "${input.path}/json"
    }
    return http.sendRequest(input, method)
  }

  Map geocodeAddress(CharSequence address){
    try {
      return sendRequest(
        'get',
        path: 'geocode',
        query: [
          address: address
        ]
      ).results[0]
    }
    catch (Exception e) {
      logger.error(e)
      return null
    }
  }
}
