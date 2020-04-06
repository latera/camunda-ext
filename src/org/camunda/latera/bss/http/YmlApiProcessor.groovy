package org.camunda.latera.bss.http;

import org.yaml.snakeyaml.Yaml

class YmlApiProcessor {
  public LinkedHashMap config

  YmlApiProcessor() {
    Yaml parser = new Yaml()
    this.config = parser.load(("/camunda/lib/yml_api.yml" as File).text)
  }

  public LinkedHashMap sendRequest(Map params, CharSequence method = 'get') {
    String uriParams = ""

    if (params.body) {
      uriParams = params.body.collect{ key, value -> "$key=$value" }.join('&')
    }

    if (params.query) {
      uriParams = params.query.collect{ key, value -> "$key=$value" }.join('&')
    }

    LinkedHashMap result = this.config.requests[method][params.path][uriParams]

    if (result) {
      return result
    } else {
      return [:]
    }
  }
}
