package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.StringUtil.capitalize
import static org.camunda.latera.bss.utils.Numeric.toFloatSafe
import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import org.camunda.latera.bss.http.HTTPRestProcessor

trait Spartial {
  void fetchTargetingData(Map input = [:]) {
    Map params = [
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    String equipmentPrefix = "${capitalize(params.equipmentPrefix)}Equipment${capitalize(params.equipmentSuffix)}"
    String prefix = "${equipmentPrefix}${capitalize(params.prefix)}"

    def equipmentId   = order."${prefix}Id" ?: [is: null]
    List stringParams  = ['DirectPolarization', 'DirectRFCC']
    List numericParams = ['Azimuth', 'AntennaPhi', 'AntennaDiameter', 'DirectdB', 'DirectRayNumber', 'DirectEIRP', 'ReversedB', 'ReversedBK', 'ReverseGT']

    (stringParams + numericParams).each { it ->
      Map addParam = hydra.getEquipmentAddParamBy(
        equipmentId : equipmentId ?: [is: 'null'],
        param       : "EQUIP_ADD_PARAM_${it}"
      )
      def value = null
      if (stringParams.contains(it)) {
        value = addParam?.vc_value
      } else {
        value = addParam?.n_value?.replace(',', '.')
        if (value?.contains('.')) {
          value = toFloatSafe(value)
        } else {
          value = toIntSafe(value)
        }
      }
      order."${prefix}${it}" = value
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
    String prefix = "${equipmentPrefix}${capitalize(params.prefix)}"

    def equipmentId    = order."${prefix}Id"
    List stringParams  = ['DirectPolarization', 'DirectRFCC']
    List numericParams = ['Azimuth', 'AntennaPhi', 'AntennaDiameter', 'DirectdB', 'DirectRayNumber', 'DirectEIRP', 'ReversedB', 'ReversedBK', 'ReverseGT']
    Boolean result     = true

    (stringParams + numericParams).each { it ->
      def value = order."${prefix}${it}"
      Map inp = [
        equipmentId : equipmentId,
        param       : "EQUIP_ADD_PARAM_${it}"
      ]
      inp[stringParams.contains(it) ? 'string' : 'number'] = value

      Map res = hydra.putEquipmentAddParam(inp)
      if (!res) {
        result = false
      }
    }

    order."${prefix}TargetingDataSet" = result
    return result
  }
}