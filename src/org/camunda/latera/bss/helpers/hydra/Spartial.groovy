package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.StringUtil.capitalize
import org.camunda.latera.bss.http.HTTPRestProcessor

/**
  * Spartial helper methods collection
  * @deprecated Use in-process class instead
  */
trait Spartial {
  /**
   * Get equipment satellite targeting data and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Azimuth}            {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*AntennaPhi}         {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*AntennaDiameter}    {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*DirectPolarization} {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*DirectRFCC}         {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*DirectdB}           {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*DirectRayNumber}    {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*DirectEIRP}         {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*ReversedB}          {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*ReversedBK}         {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*ReverseGT}          {@link CharSequence String}</li>
   * </ul>
   * @param prefix           {@link CharSequence String}. Targeting data variables prefix. Optional. Default: empty string
   * @param equipmentPrefix  {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix  {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   */
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

  /**
   * Get equipment satellite targeting data and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id}                     {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Address} {@link CharSequence String}. Address is used to determine targeting data. Used only if {@code address} argument is empty</li>
   *   <li>{@code homsOrderData*Equipment*AntennaDiameter}        {@link CharSequence String}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Azimuth}            {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*AntennaPhi}         {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*DirectPolarization} {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*DirectRFCC}         {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*DirectdB}           {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*DirectRayNumber}    {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*DirectEIRP}         {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*ReversedB}          {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*ReversedBK}         {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*Equipment*ReverseGT}          {@link CharSequence String}</li>
   * </ul>
   * @param address            {@link CharSequence String}. Address is used to determine targeting data. Optional. Default: empty string
   * @param bindAddrType       {@link CharSequence String}. Equipment-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Serv'
   * @param spartialService    {@link org.camunda.latera.bss.http.HTTPRestProcessor}. Instance of spartial REST service which is used to get targeting data. Optional. Default: null
   * @param spartialServiceUrl {@link CharSequence String}. URL of spartial REST service which is used if {@code spartialService == null}
   * @param prefix             {@link CharSequence String}. Targeting data variables prefix. Optional. Default: empty string
   * @param equipmentPrefix    {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix    {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   */
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

  /**
   * Save equipment satellite targeting data and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*Id}                 {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*Equipment*Azimuth}            {@link CharSequence String}. Optional</li>
   *   <li>{@code homsOrderData*Equipment*AntennaPhi}         {@link CharSequence String}. Optional</li>
   *   <li>{@code homsOrderData*Equipment*AntennaDiameter}    {@link CharSequence String}. Optional</li>
   *   <li>{@code homsOrderData*Equipment*DirectPolarization} {@link CharSequence String}. Optional</li>
   *   <li>{@code homsOrderData*Equipment*DirectRFCC}         {@link CharSequence String}. Optional</li>
   *   <li>{@code homsOrderData*Equipment*DirectdB}           {@link CharSequence String}. Optional</li>
   *   <li>{@code homsOrderData*Equipment*DirectRayNumber}    {@link CharSequence String}. Optional</li>
   *   <li>{@code homsOrderData*Equipment*DirectEIRP}         {@link CharSequence String}. Optional</li>
   *   <li>{@code homsOrderData*Equipment*ReversedB}          {@link CharSequence String}. Optional</li>
   *   <li>{@code homsOrderData*Equipment*ReversedBK}         {@link CharSequence String}. Optional</li>
   *   <li>{@code homsOrderData*Equipment*ReverseGT}          {@link CharSequence String}. Optional</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment*TargetingDataSet}   {@link CharSequence String}</li>
   * </ul>
   * @param prefix           {@link CharSequence String}. Targeting data variables prefix. Optional. Default: empty string
   * @param equipmentPrefix  {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix  {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @return True if targeting data was saved successfully, false otherwise
   */
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