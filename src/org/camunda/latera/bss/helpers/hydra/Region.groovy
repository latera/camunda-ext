package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.StringUtil.isEmpty
import static org.camunda.latera.bss.utils.StringUtil.notEmpty
import static org.camunda.latera.bss.utils.StringUtil.capitalize
import static org.camunda.latera.bss.utils.StringUtil.decapitalize
import java.util.regex.Pattern

trait Region {
  void fetchRegion(Map input = [:]) {
    Map params = [
      bindAddrType : 'Serv',
      entityPrefix : '',
      prefix       : ''
    ] + input

    String prefix       = "${capitalize(params.entityPrefix)}${capitalize(params.prefix)}${params.bindAddrType}"
    String regionPrefix = "${prefix}Region"

    Map data = hydra.getRegionTree(order."${regionPrefix}Id")
    data.each { CharSequence key, def value ->
      String part = capitalize(key)
      order."${prefix}${part}" = value
    }
    String  levelName = hydra.getRegionLevelNameByItem(data?.regionType)
    Integer levelNum  = hydra.getRegionLevelNum(levelName)

    order."${regionPrefix}Level"    = levelName
    order."${regionPrefix}LevelNum" = levelNum
  }

  void fetchBaseSubjectRegion(Map input = [:]) {
    Map params = [
      bindAddrType  : 'Actual',
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : ''
    ] + input

    fetchRegion(params + [entityPrefix : "${params.subjectPrefix}BaseSubject${params.subjectSuffix}"])
  }

  void fetchEquipmentRegion(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Serv',
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    fetchRegion(params + [entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}"])
  }

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

  Map parseBaseSubjectRegion(Map input = [:]) {
    Map params = [
      bindAddrType  : 'Actual',
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : ''
    ] + input

    return parseRegion(params + [entityPrefix : "${params.subjectPrefix}BaseSubject${params.subjectSuffix}"])
  }

  Map parseEquipmentRegion(Map input = [:]) {
    Map params = [
      bindAddrType    : 'Serv',
      equipmentPrefix : '',
      equipmentSuffix : '',
      prefix          : ''
    ] + input

    return parseRegion(params + [entityPrefix : "${params.equipmentPrefix}Equipment${params.equipmentSuffix}"])
  }

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

  Boolean isBaseSubjectRegionEmpty(Map input = [:]) {
    Map params = [
      bindAddrType  : 'Actual',
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : '',
      write         : true
    ] + input

    return isRegionEmpty(params + [entityPrefix : "${params.subjectPrefix}BaseSubject${params.subjectSuffix}"])
  }

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

  Boolean notRegionEmpty(Map input = [:]) {
    return !isRegionEmpty(input)
  }

  Boolean notBaseSubjectRegionEmpty(Map input = [:]) {
    return !isBaseSubjectRegionEmpty(input)
  }

  Boolean notEquipmentRegionEmpty(Map input = [:]) {
    return !isEquipmentRegionEmpty(input)
  }

  Boolean isRegionAddressEmpty(Map input = [:]) {
    Map params = [
      bindAddrType : 'Actual',
      entityPrefix : '',
      prefix       : '',
      write        : true
    ] + input

    return isRegionEmpty(params) && isAddressEmpty(params)
  }

  Boolean isBaseSubjectRegionAddressEmpty(Map input = [:]) {
    Map params = [
      bindAddrType  : 'Actual',
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : '',
      write         : true
    ] + input

    return isRegionAddressEmpty(params + [entityPrefix : "${params.subjectPrefix}BaseSubject${params.subjectSuffix}"])
  }

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

  Boolean notRegionAddressEmpty(Map input = [:]) {
    return !isRegionAddressEmpty(input)
  }

  Boolean notBaseSubjectRegionAddressEmpty(Map input = [:]) {
    return !isBaseSubjectRegionAddressEmpty(input)
  }

  Boolean notEquipmentRegionAddressEmpty(Map input = [:]) {
    return !isEquipmentRegionAddressEmpty(input)
  }

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

  Boolean createBaseSubjectRegionTree(Map input = [:]) {
    Map params = [
      bindAddrType  : 'Actual',
      subjectPrefix : '',
      subjectSuffix : '',
      prefix        : ''
    ] + input

    return createRegionTree(params + [entityPrefix : "${params.subjectPrefix}BaseSubject${params.subjectSuffix}"])
  }

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