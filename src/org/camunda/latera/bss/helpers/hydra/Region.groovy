package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.StringUtil.isEmpty
import static org.camunda.latera.bss.utils.StringUtil.notEmpty
import static org.camunda.latera.bss.utils.StringUtil.capitalize
import static org.camunda.latera.bss.utils.StringUtil.decapitalize
import java.util.regex.Pattern

/**
  * Region helper methods collection
  */
trait Region {
  /**
   * Get street address region hierarchy items by region id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*%Entity%*Id} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%RegionId}       {@link java.math.BigInteger BigInteger}. Address region id</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%RegionLevelNum} {@link Integer}. Region level number. See {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionLevelNum(java.lang.CharSequence)}</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%RegionLevel}    {@link CharSequence String}. Region level name. See {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionLevelName(java.lang.Integer)}</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%State}          {@link CharSequence String}. Parent region 'code' field with level 'state'</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%StateType}      {@link CharSequence String}. Parent region type code with level 'state'</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Oblast}         {@link CharSequence String}. Parent region 'code' field with level 'oblast'</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%OblastType}     {@link CharSequence String}. Parent region type code with level 'oblast'</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%District}       {@link CharSequence String}. Parent region 'code' field with level 'district'</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%DistrictType}   {@link CharSequence String}. Parent region type code with level 'district'</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%City}           {@link CharSequence String}. Parent region 'code' field with level 'city'</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%CityType}       {@link CharSequence String}. Parent region type code with level 'city'</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Building}       {@link CharSequence String}. Parent region 'code' field with level 'building'</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%BuildingType}   {@link CharSequence String}. Parent region type code with level 'building'</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Home}           {@link CharSequence String}. Parent region 'home' with level 'building'</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Corpus}         {@link CharSequence String}. Parent region 'corpus' with level 'building'</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Construct}      {@link CharSequence String}. Parent region 'construct' with level 'building'</li>
   *   <li>{@code homsOrderData*%Entity%*%BindAddrType%Ownership}      {@link CharSequence String}. Parent region 'ownership' with level 'building'</li>
   *   <li>other region hierarchy levels - see {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionHierarchy()}</li>
   * </ul>
   * @param prefix        {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param entityPrefix  {@link CharSequence String}. Entity prefix. Optional. Default: empty string
   * @param bindAddrType  {@link CharSequence String}. Entity address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Serv'
   */
  void fetchRegion(Map input = [:]) {
    Map params = [
      bindAddrType : 'Serv',
      entityPrefix : '',
      prefix       : ''
    ] + input

    String prefix       = "${capitalize(params.entityPrefix)}${capitalize(params.prefix)}${params.bindAddrType}"
    String regionPrefix = "${prefix}Region"

    def regionId = order."${regionPrefix}Id"
    if (isEmpty(regionId)) {
      return
    }

    Map data = hydra.getRegionTree(regionId)
    data.each { CharSequence key, def value ->
      String part = capitalize(key)
      order."${prefix}${part}" = value
    }
    String  levelName = hydra.getRegionLevelNameByItem(data?.regionType)
    Integer levelNum  = hydra.getRegionLevelNum(levelName)

    order."${regionPrefix}Level"    = levelName
    order."${regionPrefix}LevelNum" = levelNum
  }

  /**
   * Get base subject street address region hierarchy items by region id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubjectId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%RegionId}       {@link java.math.BigInteger BigInteger}. Address region id</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%RegionLevelNum} {@link Integer}. Region level number. See {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionLevelNum(java.lang.CharSequence)}</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%RegionLevel}    {@link CharSequence String}. Region level name. See {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionLevelName(java.lang.Integer)}</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%State}          {@link CharSequence String}. Parent region 'code' field with level 'state'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%StateType}      {@link CharSequence String}. Parent region type code with level 'state'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Oblast}         {@link CharSequence String}. Parent region 'code' field with level 'oblast'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%OblastType}     {@link CharSequence String}. Parent region type code with level 'oblast'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%District}       {@link CharSequence String}. Parent region 'code' field with level 'district'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%DistrictType}   {@link CharSequence String}. Parent region type code with level 'district'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%City}           {@link CharSequence String}. Parent region 'code' field with level 'city'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%CityType}       {@link CharSequence String}. Parent region type code with level 'city'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Building}       {@link CharSequence String}. Parent region 'code' field with level 'building'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%BuildingType}   {@link CharSequence String}. Parent region type code with level 'building'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Home}           {@link CharSequence String}. Parent region 'home' with level 'building'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Corpus}         {@link CharSequence String}. Parent region 'corpus' with level 'building'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Construct}      {@link CharSequence String}. Parent region 'construct' with level 'building'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Ownership}      {@link CharSequence String}. Parent region 'ownership' with level 'building'</li>
    *   <li>other region hierarchy levels - see {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionHierarchy()}</li>
   * </ul>
   * @param prefix        {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param subjectPrefix {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   * @param bindAddrType  {@link CharSequence String}. Subject-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   */
  void fetchBaseSubjectRegion(Map input = [:]) {
    Map params = [
      bindAddrType  : 'Actual',
      subjectPrefix : '',
      prefix        : ''
    ] + input

    fetchRegion(params + [entityPrefix : "${params.subjectPrefix}BaseSubject"])
  }

  /**
   * Get equipment street address region hierarchy items by region id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubjectId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%RegionId}       {@link java.math.BigInteger BigInteger}. Address region id</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%RegionLevelNum} {@link Integer}. Region level number. See {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionLevelNum(java.lang.CharSequence)}</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%RegionLevel}    {@link CharSequence String}. Region level name. See {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionLevelName(java.lang.Integer)}</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%State}          {@link CharSequence String}. Parent region 'code' field with level 'state'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%StateType}      {@link CharSequence String}. Parent region type code with level 'state'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Oblast}         {@link CharSequence String}. Parent region 'code' field with level 'oblast'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%OblastType}     {@link CharSequence String}. Parent region type code with level 'oblast'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%District}       {@link CharSequence String}. Parent region 'code' field with level 'district'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%DistrictType}   {@link CharSequence String}. Parent region type code with level 'district'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%City}           {@link CharSequence String}. Parent region 'code' field with level 'city'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%CityType}       {@link CharSequence String}. Parent region type code with level 'city'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Building}       {@link CharSequence String}. Parent region 'code' field with level 'building'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%BuildingType}   {@link CharSequence String}. Parent region type code with level 'building'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Home}           {@link CharSequence String}. Parent region 'home' with level 'building'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Corpus}         {@link CharSequence String}. Parent region 'corpus' with level 'building'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Construct}      {@link CharSequence String}. Parent region 'construct' with level 'building'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Ownership}      {@link CharSequence String}. Parent region 'ownership' with level 'building'</li>
   *   <li>other region hierarchy levels - see {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionHierarchy()}</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Subject-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Serv'
   */
  void fetchEquipmentRegion(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Serv',
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    fetchRegion(params + [entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}"])
  }

  /**
   * Convert execution variables with entity address region to Map representation
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*%BindAddrType%RegionId}       {@link java.math.BigInteger BigInteger}. Address region id</li>
   *   <li>{@code homsOrderData*%BindAddrType%RegionLevelNum} {@link Integer}. Region level number. See {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionLevelNum(java.lang.CharSequence)}</li>
   *   <li>{@code homsOrderData*%BindAddrType%RegionLevel}    {@link CharSequence String}. Region level name. See {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionLevelName(java.lang.Integer)}</li>
   *   <li>{@code homsOrderData*%BindAddrType%State}          {@link CharSequence String}. Parent region 'code' field with level 'state'</li>
   *   <li>{@code homsOrderData*%BindAddrType%StateType}      {@link CharSequence String}. Parent region type code with level 'state'</li>
   *   <li>{@code homsOrderData*%BindAddrType%Oblast}         {@link CharSequence String}. Parent region 'code' field with level 'oblast'</li>
   *   <li>{@code homsOrderData*%BindAddrType%OblastType}     {@link CharSequence String}. Parent region type code with level 'oblast'</li>
   *   <li>{@code homsOrderData*%BindAddrType%District}       {@link CharSequence String}. Parent region 'code' field with level 'district'</li>
   *   <li>{@code homsOrderData*%BindAddrType%DistrictType}   {@link CharSequence String}. Parent region type code with level 'district'</li>
   *   <li>{@code homsOrderData*%BindAddrType%City}           {@link CharSequence String}. Parent region 'code' field with level 'city'</li>
   *   <li>{@code homsOrderData*%BindAddrType%CityType}       {@link CharSequence String}. Parent region type code with level 'city'</li>
   *   <li>{@code homsOrderData*%BindAddrType%Building}       {@link CharSequence String}. Parent region 'code' field with level 'building'</li>
   *   <li>{@code homsOrderData*%BindAddrType%BuildingType}   {@link CharSequence String}. Parent region type code with level 'building'</li>
   *   <li>{@code homsOrderData*%BindAddrType%Home}           {@link CharSequence String}. Parent region 'home' with level 'building'</li>
   *   <li>{@code homsOrderData*%BindAddrType%Corpus}         {@link CharSequence String}. Parent region 'corpus' with level 'building'</li>
   *   <li>{@code homsOrderData*%BindAddrType%Construct}      {@link CharSequence String}. Parent region 'construct' with level 'building'</li>
   *   <li>{@code homsOrderData*%BindAddrType%Ownership}      {@link CharSequence String}. Parent region 'ownership' with level 'building'</li>
   *   <li>other region hierarchy levels - see {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionHierarchy()}</li>
   * </ul>
   * @param prefix       {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param bindAddrType {@link CharSequence String}. Entity-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Serv'
   * @return {@code
   *  [
   *    regionId: 1234,
   *    state: 'Россия',
   *    stateType: 'REGION_TYPE_State',
   *    oblast: 'Московская',
   *    oblastType: 'REGION_TYPE_Oblast',
   *    district: 'Химкинский',
   *    districtType: 'REGION_TYPE_District',
   *    city: 'Химки',
   *    cityType: 'REGION_TYPE_City',
   *    street: 'Заводская',
   *    streetType: 'REGION_TYPE_Street',
   *    building: '1',
   *    buildingType: 'REGION_TYPE_Building',
   *    home: '2',
   *    corpus: '3',
   *    construct: '4',
   *    ownership: '5'
   *  ]
   * }
   */
  Map parseRegion(Map input = [:]) {
    Map params = [
      bindAddrType : 'Serv',
      prefix       : ''
    ] + input

    List levelNames     = hydra.getRegionLevelNames()
    List levelTypes     = hydra.getRegionLevelTypeNames()
    List buildingFields = hydra.getBuildingFieldNames()

    String prefix   = "${capitalize(params.entityPrefix)}${capitalize(params.prefix)}${params.bindAddrType}"
    Pattern pattern = Pattern.compile("^${prefix}(RegionId|${levelNames.join('|')}|${levelTypes.join('|')}|${buildingFields.join('|')})\$", Pattern.CASE_INSENSITIVE)

    Map result = [:]
    order.data.each { CharSequence key, def value ->
      def group = (key =~ pattern)
      if (group.size() > 0) {
        String name = decapitalize(group[0][1])
        result[name] = value
        if (notEmpty(value) && name in levelNames && isEmpty(order."${key}Type")) {
          result["${name}Type"] = hydra.getRegionLevelFirstItem(name)
        }
      }
    }
    return result
  }

  /**
   * Convert execution variables with base subject region to Map representation
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%RegionId}       {@link java.math.BigInteger BigInteger}. Address region id</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%RegionLevelNum} {@link Integer}. Region level number. See {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionLevelNum(java.lang.CharSequence)}</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%RegionLevel}    {@link CharSequence String}. Region level name. See {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionLevelName(java.lang.Integer)}</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%State}          {@link CharSequence String}. Parent region 'code' field with level 'state'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%StateType}      {@link CharSequence String}. Parent region type code with level 'state'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Oblast}         {@link CharSequence String}. Parent region 'code' field with level 'oblast'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%OblastType}     {@link CharSequence String}. Parent region type code with level 'oblast'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%District}       {@link CharSequence String}. Parent region 'code' field with level 'district'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%DistrictType}   {@link CharSequence String}. Parent region type code with level 'district'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%City}           {@link CharSequence String}. Parent region 'code' field with level 'city'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%CityType}       {@link CharSequence String}. Parent region type code with level 'city'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Building}       {@link CharSequence String}. Parent region 'code' field with level 'building'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%BuildingType}   {@link CharSequence String}. Parent region type code with level 'building'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Home}           {@link CharSequence String}. Parent region 'home' with level 'building'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Corpus}         {@link CharSequence String}. Parent region 'corpus' with level 'building'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Construct}      {@link CharSequence String}. Parent region 'construct' with level 'building'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Ownership}      {@link CharSequence String}. Parent region 'ownership' with level 'building'</li>
   *   <li>other region hierarchy levels - see {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionHierarchy()}</li>
   * </ul>
   * @param prefix        {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param subjectPrefix {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   * @param bindAddrType  {@link CharSequence String}. Entity-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Serv'
   * @return {@code
   *  [
   *    regionId: 1234,
   *    state: 'Россия',
   *    stateType: 'REGION_TYPE_State',
   *    oblast: 'Московская',
   *    oblastType: 'REGION_TYPE_Oblast',
   *    district: 'Химкинский',
   *    districtType: 'REGION_TYPE_District',
   *    city: 'Химки',
   *    cityType: 'REGION_TYPE_City',
   *    street: 'Заводская',
   *    streetType: 'REGION_TYPE_Street',
   *    building: '1',
   *    buildingType: 'REGION_TYPE_Building',
   *    home: '2',
   *    corpus: '3',
   *    construct: '4',
   *    ownership: '5'
   *  ]
   * }
   */
  Map parseBaseSubjectRegion(Map input = [:]) {
    Map params = [
      bindAddrType  : 'Actual',
      subjectPrefix : '',
      prefix        : ''
    ] + input

    return parseRegion(params + [entityPrefix : "${params.subjectPrefix}BaseSubject"])
  }

  /**
   * Convert execution variables with equipment region to Map representation
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%RegionId}       {@link java.math.BigInteger BigInteger}. Address region id</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%RegionLevelNum} {@link Integer}. Region level number. See {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionLevelNum(java.lang.CharSequence)}</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%RegionLevel}    {@link CharSequence String}. Region level name. See {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionLevelName(java.lang.Integer)}</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%State}          {@link CharSequence String}. Parent region 'code' field with level 'state'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%StateType}      {@link CharSequence String}. Parent region type code with level 'state'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Oblast}         {@link CharSequence String}. Parent region 'code' field with level 'oblast'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%OblastType}     {@link CharSequence String}. Parent region type code with level 'oblast'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%District}       {@link CharSequence String}. Parent region 'code' field with level 'district'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%DistrictType}   {@link CharSequence String}. Parent region type code with level 'district'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%City}           {@link CharSequence String}. Parent region 'code' field with level 'city'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%CityType}       {@link CharSequence String}. Parent region type code with level 'city'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Building}       {@link CharSequence String}. Parent region 'code' field with level 'building'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%BuildingType}   {@link CharSequence String}. Parent region type code with level 'building'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Home}           {@link CharSequence String}. Parent region 'home' with level 'building'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Corpus}         {@link CharSequence String}. Parent region 'corpus' with level 'building'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Construct}      {@link CharSequence String}. Parent region 'construct' with level 'building'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Ownership}      {@link CharSequence String}. Parent region 'ownership' with level 'building'</li>
   *   <li>other region hierarchy levels - see {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionHierarchy()}</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Entity-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Serv'
   * @return {@code
   *  [
   *    regionId: 1234,
   *    state: 'Россия',
   *    stateType: 'REGION_TYPE_State',
   *    oblast: 'Московская',
   *    oblastType: 'REGION_TYPE_Oblast',
   *    district: 'Химкинский',
   *    districtType: 'REGION_TYPE_District',
   *    city: 'Химки',
   *    cityType: 'REGION_TYPE_City',
   *    street: 'Заводская',
   *    streetType: 'REGION_TYPE_Street',
   *    building: '1',
   *    buildingType: 'REGION_TYPE_Building',
   *    home: '2',
   *    corpus: '3',
   *    construct: '4',
   *    ownership: '5'
   *  ]
   * }
   */
  Map parseEquipmentRegion(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Serv',
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    return parseRegion(params + [entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}"])
  }

  /**
   * Determine if execution variables with entity address region are empty
   * <p>
   * For internal usage only
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*%BindAddrType%RegionId}  {@link java.math.BigInteger BigInteger}. Address region id</li>
   *   <li>{@code homsOrderData*%BindAddrType%State}     {@link CharSequence String}. Parent region 'code' field with level 'state'</li>
   *   <li>{@code homsOrderData*%BindAddrType%Oblast}    {@link CharSequence String}. Parent region 'code' field with level 'oblast'</li>
   *   <li>{@code homsOrderData*%BindAddrType%District}  {@link CharSequence String}. Parent region 'code' field with level 'district'</li>
   *   <li>{@code homsOrderData*%BindAddrType%City}      {@link CharSequence String}. Parent region 'code' field with level 'city'</li>
   *   <li>{@code homsOrderData*%BindAddrType%Building}  {@link CharSequence String}. Parent region 'code' field with level 'building'</li>   *   <li>{@code homsOrderData*%BindAddrType%BuildingType}   {@link CharSequence String}. Parent region type code with level 'building'</li>
   *   <li>{@code homsOrderData*%BindAddrType%Home}      {@link CharSequence String}. Parent region 'home' with level 'building'</li>
   *   <li>{@code homsOrderData*%BindAddrType%Corpus}    {@link CharSequence String}. Parent region 'corpus' with level 'building'</li>
   *   <li>{@code homsOrderData*%BindAddrType%Construct} {@link CharSequence String}. Parent region 'construct' with level 'building'</li>
   *   <li>{@code homsOrderData*%BindAddrType%Ownership} {@link CharSequence String}. Parent region 'ownership' with level 'building'</li>
   *   <li>other region hierarchy levels - see {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionHierarchy()}</li>
   * </ul>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*%BindAddrType%AddressEmpty} {@link Boolean}. Same as return value. Set only {@code if write == true}</li>
   * </ul>
   * <p>
   * @param prefix       {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param bindAddrType {@link CharSequence String}. Entity-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Serv'
   * @param write        {@link CharSequence String}. Save result to execution variable or not. Optional. Default: true
   * @return True if entity address region is empty, false otherwise
   */
  Boolean isRegionEmpty(Map input = [:]) {
    Map params = [
      bindAddrType : 'Serv',
      prefix       : '',
      write        : true
    ] + input

    String regionPrefix = "${capitalize(params.entityPrefix)}${capitalize(params.prefix)}${params.bindAddrType}Region"

    Map region     = parseRegion(params)
    Boolean result = hydra.isRegionEmpty(region)
    if (params.write) {
      order."${regionPrefix}Empty" = result
    }
    return result
  }

  /**
   * Determine if execution variables with base subject address region are empty
   * <p>
   * For internal usage only
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%RegionId}  {@link java.math.BigInteger BigInteger}. Address region id</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%State}     {@link CharSequence String}. Parent region 'code' field with level 'state'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Oblast}    {@link CharSequence String}. Parent region 'code' field with level 'oblast'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%District}  {@link CharSequence String}. Parent region 'code' field with level 'district'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%City}      {@link CharSequence String}. Parent region 'code' field with level 'city'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Building}  {@link CharSequence String}. Parent region 'code' field with level 'building'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Home}      {@link CharSequence String}. Parent region 'home' with level 'building'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Corpus}    {@link CharSequence String}. Parent region 'corpus' with level 'building'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Construct} {@link CharSequence String}. Parent region 'construct' with level 'building'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Ownership} {@link CharSequence String}. Parent region 'ownership' with level 'building'</li>
   *   <li>other region hierarchy levels - see {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionHierarchy()}</li>
   * </ul>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*%BindAddrType%AddressEmpty} {@link Boolean}. Same as return value. Set only {@code if write == true}</li>
   * </ul>
   * <p>
   * @param prefix        {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param subjectPrefix {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   * @param bindAddrType  {@link CharSequence String}. Entity-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @param write         {@link CharSequence String}. Save result to execution variable or not. Optional. Default: true
   * @return True if baase subject address region is empty, false otherwise
   */
  Boolean isBaseSubjectRegionEmpty(Map input = [:]) {
    Map params = [
      bindAddrType  : 'Actual',
      subjectPrefix : '',
      prefix        : '',
      write         : true
    ] + input

    return isRegionEmpty(params + [entityPrefix : "${params.subjectPrefix}BaseSubject"])
  }

  /**
   * Determine if execution variables with equipment address region are empty
   * <p>
   * For internal usage only
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%RegionId}  {@link java.math.BigInteger BigInteger}. Address region id</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%State}     {@link CharSequence String}. Parent region 'code' field with level 'state'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Oblast}    {@link CharSequence String}. Parent region 'code' field with level 'oblast'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%District}  {@link CharSequence String}. Parent region 'code' field with level 'district'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%City}      {@link CharSequence String}. Parent region 'code' field with level 'city'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Building}  {@link CharSequence String}. Parent region 'code' field with level 'building'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Home}      {@link CharSequence String}. Parent region 'home' with level 'building'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Corpus}    {@link CharSequence String}. Parent region 'corpus' with level 'building'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Construct} {@link CharSequence String}. Parent region 'construct' with level 'building'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Ownership} {@link CharSequence String}. Parent region 'ownership' with level 'building'</li>
   *   <li>other region hierarchy levels - see {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionHierarchy()}</li>
   * </ul>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*%BindAddrType%AddressEmpty} {@link Boolean}. Same as return value. Set only {@code if write == true}</li>
   * </ul>
   * <p>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Entity-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Serv'
   * @param write           {@link CharSequence String}. Save result to execution variable or not. Optional. Default: true
   * @return True if equipment address region is empty, false otherwise
   */
  Boolean isEquipmentRegionEmpty(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Serv',
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      write           : true
    ] + input

    return isRegionEmpty(params + [entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}"])
  }

  /**
   * Determine if execution variables with entity address region are empty
   *
   * @return True if entity address region is not empty, false otherwise
   * @see #isRegionEmpty(Map)
   */
  Boolean notRegionEmpty(Map input = [:]) {
    return !isRegionEmpty(input)
  }

  /**
   * Determine if execution variables with base subject address region are empty
   *
   * @return True if base subject address region is not empty, false otherwise
   * @see #isBaseSubjectRegionEmpty(Map)
   */
  Boolean notBaseSubjectRegionEmpty(Map input = [:]) {
    return !isBaseSubjectRegionEmpty(input)
  }

  /**
   * Determine if execution variables with equipment address region are empty
   *
   * @return True if equipment address region is not empty, false otherwise
   * @see #isEquipmentRegionEmpty(Map)
   */
  Boolean notEquipmentRegionEmpty(Map input = [:]) {
    return !isEquipmentRegionEmpty(input)
  }

  /**
   * Determine if execution variables with entity address and region are empty
   * <p>
   * For internal usage only
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*%BindAddrType%RegionId}  {@link java.math.BigInteger BigInteger}. Address region id</li>
   *   <li>{@code homsOrderData*%BindAddrType%State}     {@link CharSequence String}. Parent region 'code' field with level 'state'</li>
   *   <li>{@code homsOrderData*%BindAddrType%Oblast}    {@link CharSequence String}. Parent region 'code' field with level 'oblast'</li>
   *   <li>{@code homsOrderData*%BindAddrType%District}  {@link CharSequence String}. Parent region 'code' field with level 'district'</li>
   *   <li>{@code homsOrderData*%BindAddrType%City}      {@link CharSequence String}. Parent region 'code' field with level 'city'</li>
   *   <li>{@code homsOrderData*%BindAddrType%CityType}  {@link CharSequence String}. Parent region type code with level 'city'</li>
   *   <li>{@code homsOrderData*%BindAddrType%Building}  {@link CharSequence String}. Parent region 'code' field with level 'building'</li>
   *   <li>{@code homsOrderData*%BindAddrType%Home}      {@link CharSequence String}. Parent region 'home' with level 'building'</li>
   *   <li>{@code homsOrderData*%BindAddrType%Corpus}    {@link CharSequence String}. Parent region 'corpus' with level 'building'</li>
   *   <li>{@code homsOrderData*%BindAddrType%Construct} {@link CharSequence String}. Parent region 'construct' with level 'building'</li>
   *   <li>{@code homsOrderData*%BindAddrType%Ownership} {@link CharSequence String}. Parent region 'ownership' with level 'building'</li>
   *   <li>other region hierarchy levels - see {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionHierarchy()}</li>
   *   <li>{@code homsOrderData*%BindAddrType%Entrance}  {@link CharSequence String}. Entrance number</li>
   *   <li>{@code homsOrderData*%BindAddrType%Floor}     {@link Integer}. Floor number</li>
   *   <li>{@code homsOrderData*%BindAddrType%Flat}      {@link CharSequence String}. Flat number</li>
   *   <li>{@code homsOrderData*%BindAddrType%Address}   {@link CharSequence String}. Calculated address</li>
   *   <li>{@code homsOrderData*%BindAddrType%AddressId} {@link CharSequence String}. Entity address id. Set only if withId == true</li>
   * </ul>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*%BindAddrType%AddressEmpty} {@link Boolean}. Same as return value. Set only {@code if write == true}</li>
   * </ul>
   * <p>
   * @param prefix       {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param bindAddrType {@link CharSequence String}. Entity-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Serv'
   * @param write        {@link CharSequence String}. Save result to execution variable or not. Optional. Default: true
   * @return True if entity address and region variables are empty, false otherwise
   */
  Boolean isRegionAddressEmpty(Map input = [:]) {
    Map params = [
      bindAddrType : 'Actual',
      entityPrefix : '',
      prefix       : '',
      write        : true
    ] + input

    return isRegionEmpty(params) && isAddressEmpty(params)
  }

  /**
   * Determine if execution variables with base subject address and region are empty
   * <p>
   * For internal usage only
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%RegionId}  {@link java.math.BigInteger BigInteger}. Address region id</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%State}     {@link CharSequence String}. Parent region 'code' field with level 'state'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Oblast}    {@link CharSequence String}. Parent region 'code' field with level 'oblast'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%District}  {@link CharSequence String}. Parent region 'code' field with level 'district'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%City}      {@link CharSequence String}. Parent region 'code' field with level 'city'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Building}  {@link CharSequence String}. Parent region 'code' field with level 'building'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Home}      {@link CharSequence String}. Parent region 'home' with level 'building'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Corpus}    {@link CharSequence String}. Parent region 'corpus' with level 'building'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Construct} {@link CharSequence String}. Parent region 'construct' with level 'building'</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Ownership} {@link CharSequence String}. Parent region 'ownership' with level 'building'</li>
   *   <li>other region hierarchy levels - see {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionHierarchy()}</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Entrance}  {@link CharSequence String}. Entrance number</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Floor}     {@link Integer}. Floor number</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Flat}      {@link CharSequence String}. Flat number</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Address}   {@link CharSequence String}. Calculated address</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%AddressId} {@link CharSequence String}. Entity address id. Set only if withId == true</li>
   * </ul>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%AddressEmpty} {@link Boolean}. Same as return value. Set only {@code if write == true}</li>
   * </ul>
   * <p>
   * @param prefix        {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param subjectPrefix {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   * @param bindAddrType  {@link CharSequence String}. Entity-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Serv'
   * @param write         {@link CharSequence String}. Save result to execution variable or not. Optional. Default: true
   * @return True if base subject address and region variables are empty, false otherwise
   */
  Boolean isBaseSubjectRegionAddressEmpty(Map input = [:]) {
    Map params = [
      bindAddrType  : 'Actual',
      subjectPrefix : '',
      prefix        : '',
      write         : true
    ] + input

    return isRegionAddressEmpty(params + [entityPrefix : "${params.subjectPrefix}BaseSubject"])
  }

  /**
   * Determine if execution variables with equipment address and region are empty
   * <p>
   * For internal usage only
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%RegionId}  {@link java.math.BigInteger BigInteger}. Address region id</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%State}     {@link CharSequence String}. Parent region 'code' field with level 'state'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Oblast}    {@link CharSequence String}. Parent region 'code' field with level 'oblast'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%District}  {@link CharSequence String}. Parent region 'code' field with level 'district'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%City}      {@link CharSequence String}. Parent region 'code' field with level 'city'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Building}  {@link CharSequence String}. Parent region 'code' field with level 'building'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Home}      {@link CharSequence String}. Parent region 'home' with level 'building'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Corpus}    {@link CharSequence String}. Parent region 'corpus' with level 'building'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Construct} {@link CharSequence String}. Parent region 'construct' with level 'building'</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Ownership} {@link CharSequence String}. Parent region 'ownership' with level 'building'</li>
   *   <li>other region hierarchy levels - see {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionHierarchy()}</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Entrance}  {@link CharSequence String}. Entrance number</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Floor}     {@link Integer}. Floor number</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Flat}      {@link CharSequence String}. Flat number</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Address}   {@link CharSequence String}. Calculated address</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%AddressId} {@link CharSequence String}. Entity address id. Set only if withId == true</li>

   * </ul>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%AddressEmpty} {@link Boolean}. Same as return value. Set only {@code if write == true}</li>
   * </ul>
   * <p>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Entity-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Serv'
   * @param write           {@link CharSequence String}. Save result to execution variable or not. Optional. Default: true
   * @return True if equipment address and region variables are empty, false otherwise
   */
  Boolean isEquipmentRegionAddressEmpty(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Serv',
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : '',
      write           : true
    ] + input

    return isRegionAddressEmpty(params + [entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}"])
  }

  /**
   * Determine if execution variables with entity address and region are not empty
   *
   * @return True if entity address and region variables are not empty, false otherwise
   * @see #isRegionAddressEmpty(Map)
   */
  Boolean notRegionAddressEmpty(Map input = [:]) {
    return !isRegionAddressEmpty(input)
  }

  /**
   * Determine if execution variables with base subject address and region are not empty
   *
   * @return True if base subject address and region variables are empty, false otherwise
   * @see #isBaseSubjectRegionAddressEmpty(Map)
   */
  Boolean notBaseSubjectRegionAddressEmpty(Map input = [:]) {
    return !isBaseSubjectRegionAddressEmpty(input)
  }

  /**
   * Determine if execution variables with equipment address and region are not empty
   *
   * @return True if equipment address and region variables are not empty, false otherwise
   * @see #isEquipmentRegionAddressEmpty(Map)
   */
  Boolean notEquipmentRegionAddressEmpty(Map input = [:]) {
    return !isEquipmentRegionAddressEmpty(input)
  }

  /**
   * Create entity address region tree and fill up execution variables
   * <p>
   * For internal usage only
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*%BindAddrType%State}          {@link CharSequence String}. Parent region 'code' field with level 'state'. Optional</li>
   *   <li>{@code homsOrderData*%BindAddrType%StateType}      {@link CharSequence String}. Parent region type code with level 'state'. Optional</li>
   *   <li>{@code homsOrderData*%BindAddrType%Oblast}         {@link CharSequence String}. Parent region 'code' field with level 'oblast'. Optional</li>
   *   <li>{@code homsOrderData*%BindAddrType%OblastType}     {@link CharSequence String}. Parent region type code with level 'oblast'. Optional</li>
   *   <li>{@code homsOrderData*%BindAddrType%District}       {@link CharSequence String}. Parent region 'code' field with level 'district'. Optional</li>
   *   <li>{@code homsOrderData*%BindAddrType%DistrictType}   {@link CharSequence String}. Parent region type code with level 'district'. Optional</li>
   *   <li>{@code homsOrderData*%BindAddrType%City}           {@link CharSequence String}. Parent region 'code' field with level 'city'. Optional</li>
   *   <li>{@code homsOrderData*%BindAddrType%CityType}       {@link CharSequence String}. Parent region type code with level 'city'. Optional</li>
   *   <li>{@code homsOrderData*%BindAddrType%Building}       {@link CharSequence String}. Parent region 'code' field with level 'building'. Optional</li>
   *   <li>{@code homsOrderData*%BindAddrType%BuildingType}   {@link CharSequence String}. Parent region type code with level 'building'. Optional</li>
   *   <li>{@code homsOrderData*%BindAddrType%BuildingGoodId} {@link CharSequence String}. Realty good id for building. Optional</li>
   *   <li>{@code homsOrderData*%BindAddrType%BuildingGood}   {@link CharSequence String}. Realty good code for building. Optional</li>
   *   <li>{@code homsOrderData*%BindAddrType%Home}           {@link CharSequence String}. Parent region 'home' with level 'building'. Optional</li>
   *   <li>{@code homsOrderData*%BindAddrType%Corpus}         {@link CharSequence String}. Parent region 'corpus' with level 'building'. Optional</li>
   *   <li>{@code homsOrderData*%BindAddrType%Construct}      {@link CharSequence String}. Parent region 'construct' with level 'building'. Optional</li>
   *   <li>{@code homsOrderData*%BindAddrType%Ownership}      {@link CharSequence String}. Parent region 'ownership' with level 'building'. Optional</li>
   *   <li>other region hierarchy levels - see {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionHierarchy()}</li>
    * </ul>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*%BindAddrType%RegionId}      {@link java.math.BigInteger BigInteger}. Created region id. Optional</li>
   *   <li>{@code homsOrderData*%BindAddrType%RegionCreated} {@link Boolean}. Same as result value</li>
   * </ul>
   * @param prefix       {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param bindAddrType {@link CharSequence String}. Entity-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @return True if region tree of entity address parts was created successfully, false otherwise
   */
  Boolean createRegionTree(Map input = [:]) {
    Map params = [
      bindAddrType : 'Serv',
      prefix       : ''
    ] + input

    String regionPrefix = "${capitalize(params.entityPrefix)}${capitalize(params.prefix)}${params.bindAddrType}Region"

    Map region = parseRegion(params)
    Boolean shouldCreate = hydra.notRegionEmpty(region)
    Boolean result = false

    if (shouldCreate) {
      def regionId = hydra.createRegionTree(region)
      if (regionId != 0) {
        order."${regionPrefix}Id" = regionId
        result = true
      }
    } else {
      result = true
    }
    order."${regionPrefix}Created" = result
    return result
  }

  /**
   * Create base subject address region tree and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%State}          {@link CharSequence String}. Parent region 'code' field with level 'state'. Optional</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%StateType}      {@link CharSequence String}. Parent region type code with level 'state'. Optional</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Oblast}         {@link CharSequence String}. Parent region 'code' field with level 'oblast'. Optional</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%OblastType}     {@link CharSequence String}. Parent region type code with level 'oblast'. Optional</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%District}       {@link CharSequence String}. Parent region 'code' field with level 'district'. Optional</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%DistrictType}   {@link CharSequence String}. Parent region type code with level 'district'. Optional</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%City}           {@link CharSequence String}. Parent region 'code' field with level 'city'. Optional</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%CityType}       {@link CharSequence String}. Parent region type code with level 'city'. Optional</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Building}       {@link CharSequence String}. Parent region 'code' field with level 'building'. Optional</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%BuildingType}   {@link CharSequence String}. Parent region type code with level 'building'. Optional</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%BuildingGoodId} {@link CharSequence String}. Realty good id for building. Optional</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%BuildingGood}   {@link CharSequence String}. Realty good code for building. Optional</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Home}         {@link CharSequence String}. Parent region 'home' with level 'building'. Optional</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Corpus}         {@link CharSequence String}. Parent region 'corpus' with level 'building'. Optional</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Construct}      {@link CharSequence String}. Parent region 'construct' with level 'building'. Optional</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%Ownership}      {@link CharSequence String}. Parent region 'ownership' with level 'building'. Optional</li>
   *   <li>other region hierarchy levels - see {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionHierarchy()}</li>
    * </ul>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%RegionId}      {@link java.math.BigInteger BigInteger}. Created region id. Optional</li>
   *   <li>{@code homsOrderData*BaseSubject*%BindAddrType%RegionCreated} {@link Boolean}. Same as result value</li>
   * </ul>
   * @param prefix        {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param subjectPrefix {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   * @param bindAddrType  {@link CharSequence String}. Entity-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @return True if region tree of base subject address parts was created successfully, false otherwise
   */
  Boolean createBaseSubjectRegionTree(Map input = [:]) {
    Map params = [
      bindAddrType  : 'Actual',
      subjectPrefix : '',
      prefix        : ''
    ] + input

    return createRegionTree(params + [entityPrefix : "${params.subjectPrefix}BaseSubject"])
  }

  /**
   * Create equipment address region tree and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%State}          {@link CharSequence String}. Parent region 'code' field with level 'state'. Optional</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%StateType}      {@link CharSequence String}. Parent region type code with level 'state'. Optional</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Oblast}         {@link CharSequence String}. Parent region 'code' field with level 'oblast'. Optional</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%OblastType}     {@link CharSequence String}. Parent region type code with level 'oblast'. Optional</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%District}       {@link CharSequence String}. Parent region 'code' field with level 'district'. Optional</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%DistrictType}   {@link CharSequence String}. Parent region type code with level 'district'. Optional</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%City}           {@link CharSequence String}. Parent region 'code' field with level 'city'. Optional</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%CityType}       {@link CharSequence String}. Parent region type code with level 'city'. Optional</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Building}       {@link CharSequence String}. Parent region 'code' field with level 'building'. Optional</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%BuildingType}   {@link CharSequence String}. Parent region type code with level 'building'. Optional</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%BuildingGoodId} {@link CharSequence String}. Realty good id for building. Optional</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%BuildingGood}   {@link CharSequence String}. Realty good code for building. Optional</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Home}           {@link CharSequence String}. Parent region 'home' with level 'building'. Optional</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Corpus}         {@link CharSequence String}. Parent region 'corpus' with level 'building'. Optional</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Construct}      {@link CharSequence String}. Parent region 'construct' with level 'building'. Optional</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%Ownership}      {@link CharSequence String}. Parent region 'ownership' with level 'building'. Optional</li>
   *   <li>other region hierarchy levels - see {@link org.camunda.latera.bss.connectors.hid.hydra.Region#getRegionHierarchy()}</li>
    * </ul>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%RegionId}      {@link java.math.BigInteger BigInteger}. Created region id. Optional</li>
   *   <li>{@code homsOrderData*Equipment**%BindAddrType%RegionCreated} {@link Boolean}. Same as result value</li>
   * </ul>
   * @param prefix          {@link CharSequence String}. Address prefix. Optional. Default: empty string
   * @param equipmentPrefix {@link CharSequence String}. Equipment prefix. Optional. Default: empty string
   * @param equipmentSuffix {@link CharSequence String}. Equipment suffix. Optional. Default: empty string
   * @param bindAddrType    {@link CharSequence String}. Entity-address bind type without 'BIND_ADDR_TYPE_' part. Optional. Default: 'Actual'
   * @return True if region tree of equipment address parts was created successfully, false otherwise
   */
  Boolean createEquipmentRegionTree(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Serv',
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    return createRegionTree(params + [entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}"])
  }
}