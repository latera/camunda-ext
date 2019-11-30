package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.StringUtil.capitalize
import org.camunda.latera.bss.http.HTTPRestProcessor

trait Spartial {
  void fetchTargetingData(Map input = [:]) {
    Map params = [
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    List addParams  = ['DirectPolarization', 'DirectRFCC', 'Azimuth', 'AntennaPhi', 'AntennaDiameter', 'DirectdB', 'DirectRayNumber', 'DirectEIRP', 'ReversedB', 'ReversedBK', 'ReverseGT']

    addParams.each { CharSequence param ->
      fetchEquipmentAddParam(
        equipmentPrefix : params.equipmentPrefix,
        equipmentSuffix : params.equipmentSuffix,
        param           : param
      )
    }
  }

  void calcTargetingData(Map input = [:]) {
    Map params = [
      address         : '',
      bindAddrType    : 'Serv',
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      spartialService : null
    ] + input

    String equipmentPrefix = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}"
    String prefix = "${equipmentPrefix}${capitalize(params.prefix)}"
    def spartialService = params.spartialService ?: new HTTPRestProcessor(baseUrl: execution.getVariable('spartialServiceUrl'), execution: execution)

    String address = params.address ?: order."${prefix}${params.bindAddrType}Address"
    def antennaDiameter = order."${prefix}AntennaDiameter"
    def spartialServicePath = execution.getVariable('spartialServicePath')

    Map response = spartialService.sendRequest(path: "${spartialServicePath}/targeting_data", body: [address: address, antenna: antennaDiameter], 'post')

    order."${prefix}Longtitude"         = response?.longtitude
    order."${prefix}Latitude"           = response?.latitude
    order."${prefix}Azimuth"            = response?.azimuth
    order."${prefix}AntennaPhi"         = response?.phi
    order."${prefix}DirectdB"           = response?.direct?.dB
    order."${prefix}DirectRayNumber"    = response?.direct?.ray
    order."${prefix}DirectPolarization" = response?.direct?.polarization
    order."${prefix}DirectRFCC"         = response?.direct?.rfcc
    order."${prefix}DirectEIRP"         = response?.direct?.eirp
    order."${prefix}ReversedB"          = response?.reverse?.dB
    order."${prefix}ReversedBK"         = response?.reverse?.dBK
    order."${prefix}ReverseGT"          = response?.reverse?.gt
  }

  Boolean saveTargetingData(Map input = [:]) {
    Map params = [
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    String equipmentPrefix = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}"
    String prefix  = "${equipmentPrefix}${capitalize(params.prefix)}"
    Boolean result = true

    List addParams = ['DirectPolarization', 'DirectRFCC', 'Azimuth', 'AntennaPhi', 'AntennaDiameter', 'DirectdB', 'DirectRayNumber', 'DirectEIRP', 'ReversedB', 'ReversedBK', 'ReverseGT']

    addParams.each { CharSequence param ->
      if (!saveEquipmentAddParam(
        equipmentPrefix : params.equipmentPrefix,
        equipmentSuffix : params.equipmentSuffix,
        param           : param
      )) {
        result = false
      }
    }
    order."${prefix}TargetingDataSet" = result
    return result
  }
}