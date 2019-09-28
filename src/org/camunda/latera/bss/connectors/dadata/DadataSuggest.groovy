package org.camunda.latera.bss.connectors

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.latera.bss.http.HTTPRestProcessor
import org.camunda.latera.bss.logging.SimpleLogger
import org.camunda.latera.bss.utils.Numeric
import java.text.DecimalFormat
import static org.camunda.latera.bss.utils.MapUtil.snakeCaseKeys
import static org.camunda.latera.bss.utils.MapUtil.deepSnakeCaseKeys

class DadataSuggest {
  String url
  String version
  private String token
  private String secret
  HTTPRestProcessor http
  SimpleLogger logger

  DadataSuggest(DelegateExecution execution) {
    this.logger  =  new SimpleLogger(execution)
    def ENV      =  System.getenv()

    this.url     =  ENV['DADATA_SUGGEST_URL']     ?: 'https://suggestions.dadata.ru/suggestions/api'
    this.version =  ENV['DADATA_SUGGEST_VERSION'] ?: execution.getVariable('dadataSuggestVersion') ?: '4.1'
    this.token   =  ENV['DADATA_SUGGEST_TOKEN']   ?: ENV['DADATA_TOKEN']?: execution.getVariable('dadataSuggestToken') ?: execution.getVariable('dadataToken')

    LinkedHashMap headers = [
      'Authorization' : "Token ${token}"
    ]
    this.http = new HTTPRestProcessor(
      baseUrl   : "${url}/${version.replace('.', '_')}/rs",
      headers   : headers,
      execution : execution
    )
  }

  private List suggestList(Map data, CharSequence type, CharSequence input){
    try {
      def body = [
        query: input
      ]
      if (data.limit) {
        if (data.limit > 1) {
          body += [count: data.limit]
        }
        data.remove('limit')
      }
      if (data.filterBy) {
        body += [filters: snakeCaseKeys(data.filterBy)]
        data.remove('filterBy')
      }
      if (data.location) {
        data.locations = [data.location]
      }
      body += deepSnakeCaseKeys(data)

      return http.sendRequest(
        'post',
        path: "suggest/${type}",
        body: body
      ).suggestions
    }
    catch (Exception e) {
      logger.error(e)
      return []
    }
  }

  private List suggestList(CharSequence type, CharSequence input, Map data = [:]){
    return suggestList(type, input, data)
  }

  private Map suggestData(Map data, CharSequence type, CharSequence input){
    data.limit = 1
    return suggestList(data, type, input)?.getAt(0)?.data
  }

  private Map suggestData(CharSequence type, CharSequence input, Map data = [:]){
    return suggestData(data, type, input)
  }

  private Map getData(CharSequence type, def id){
    try {
      return http.sendRequest(
        'get',
        path: "findById/${type}",
        query: [
          query: id.toString()
        ]
      ).suggestions
    }
    catch (Exception e) {
      logger.error(e)
      return null
    }
  }

  List suggestAddresses(Map data, CharSequence input) {
    return suggestList('address', input, data + [limit: limit])
  }

  List suggestAddresses(CharSequence input, Integer limit = 10) {
    return suggestAddresses(input, limit: limit)
  }

  Map suggestAddress(Map data, CharSequence input) {
    return suggestData('address', input, data)
  }

  Map suggestAddress(CharSequence input, Map data = [:]) {
    return suggestAddress(input, data)
  }

  List suggestNames(Map data, CharSequence input) {
    return suggestList('fio', input, data)
  }

  List suggestNames(CharSequence input, Integer limit = 10) {
    return suggestNames(input, limit: limit)
  }

  Map suggestName(Map data, CharSequence input) {
    return suggestData('fio', input, data)
  }

  Map suggestName(CharSequence input, Map data = [:]) {
    return suggestName(data, input)
  }

  List suggestCompanies(Map data, CharSequence input) {
    return suggestList('party', input, data)
  }

  List suggestCompanies(CharSequence input, Integer limit = 10) {
    return suggestCompanies(input, limit: limit)
  }

  Map suggestCompany(Map data, CharSequence input) {
    return suggestData('party', input, data)
  }

  Map suggestCompany(CharSequence input, Map data = [:]) {
    return suggestCompany(data, input)
  }

  List suggestBanks(Map data, CharSequence input) {
    return suggestList('bank', input, limit)
  }

  List suggestBanks(CharSequence input, Integer limit = 10) {
    return suggestBanks(input, limit: limit)
  }

  Map suggestBank(Map data, CharSequence input) {
    return suggestData('bank', input, data)
  }

  Map suggestBank(CharSequence input, Map data = [:]) {
    return suggestBank(data, input)
  }

  List suggestEmails(Map data, CharSequence input) {
    return suggestList('email', input, data)
  }

  List suggestEmails(CharSequence input, Integer limit = 10) {
    return suggestEmails(input, limit: limit)
  }

  Map suggestEmail(Map data, CharSequence input) {
    return suggestData('email', input, data)
  }

  Map suggestEmail(CharSequence input, Map data = [:]) {
    return suggestEmail(data, input)
  }

  List suggestPassports(Map data, CharSequence input) {
    return suggestList('fms_unit', input, data)
  }

  List suggestPassports(CharSequence input, Integer limit = 10) {
    return suggestPassports(input, limit: limit)
  }

  Map suggestPassport(Map data, CharSequence input) {
    return suggestData('fms_unit', input, data)
  }

  Map suggestPassport(CharSequence input, Map data = [:]) {
    return suggestPassport(data, input)
  }

  List suggestCountries(Map data, CharSequence input) {
    return suggestList('country', input, data)
  }

  List suggestCountries(CharSequence input, Integer limit = 10) {
    return suggestCountries(input, limit: limit)
  }

  Map suggestCountry(Map data, CharSequence input) {
    return suggestData('country', input, data)
  }

  Map suggestCountry(CharSequence input, Map data = [:]) {
    return suggestCountry(data, input)
  }

  Map getCountry(def id) {
    return getData('country', id)
  }

  List suggestCurrencies(Map data, CharSequence input) {
    return suggestList('currency', input, data)
  }

  List suggestCurrencies(CharSequence input, Integer limit = 10) {
    return suggestCurrencies(input, limit: limit)
  }

  Map suggestCurrency(Map data, CharSequence input) {
    return suggestData('currency', input, data)
  }

  Map suggestCurrency(CharSequence input, Map data = [:]) {
    return suggestCurrency(data, input)
  }

  Map getCurrency(def id) {
    return getData('currency', id)
  }
}
