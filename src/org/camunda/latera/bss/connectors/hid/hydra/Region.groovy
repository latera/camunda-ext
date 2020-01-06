package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.StringUtil.notEmpty
import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import static org.camunda.latera.bss.utils.StringUtil.joinNonEmpty
import static org.camunda.latera.bss.utils.MapUtil.keysList

trait Region {
  private static String REGIONS_TABLE = 'SR_V_REGIONS'
  private static String DEFAULT_REALTY_GOOD_CODE = 'REALTY_House'

  /*
  Structure
    hierarchy:
      level: [items]
  Level can be accessed by name or index. Building is always last level.

  Region tree is a map with fields:
    [
      regionId   : 0, # region id fetched from db just to populate lower levels
      regionType : REGION_TYPE_Building', # same but for region type code
      state      : 'Russia',
      stateType  : 'REGION_TYPE_State',
      oblast     : 'Moskowskaya',
      oblastType : 'REGION_TYPE_Oblast',
      ...
    ]
  So there are level name and also name + 'Type' which called typeName
  */
  private static LinkedHashMap REGION_HIERARCHY = [
    state    : ['REGION_TYPE_State'],
    oblast   : ['REGION_TYPE_Oblast'],
    okrug    : ['REGION_TYPE_Okrug'],
    district : ['REGION_TYPE_District'],
    city     : ['REGION_TYPE_City','REGION_TYPE_UrbanVillage','REGION_TYPE_Settlement', 'REGION_TYPE_Village'],
    street   : ['REGION_TYPE_Street','REGION_TYPE_Avenue','REGION_TYPE_Passage', 'REGION_TYPE_Highway','REGION_TYPE_SideStreet','REGION_TYPE_Seafront','REGION_TYPE_Boulevard', 'REGION_TYPE_Square'],
    building : ['REGION_TYPE_Building']
  ]

  /*
  Structure
    field: messageCode
  messageCode is being translated to current locale ('locale' execution variable)
  */
  private static LinkedHashMap BUILDING_FIELDS = [
    home      : 'Region_Home_Short',
    corpus    : 'Region_Building_Short',
    construct : 'Region_Construct_Short',
    ownership : 'Region_Ownership_Short',
  ]

  String getRegionsTable() {
    return REGIONS_TABLE
  }

  // Get full region hierarchy
  Map getRegionHierarchy() {
    return regionHierarchyOverride ?: REGION_HIERARCHY
  }

  // Get ['REGION_TYPE_State'] by 'state' key
  List getRegionLevel(CharSequence code) {
    return getRegionHierarchy()[code]
  }

  // Get ['REGION_TYPE_State'] by 0 index
  List getRegionLevel(Integer index) {
    return getRegionLevel(getRegionLevelName(index))
  }

  // Get last hierarchy key = 'building'
  String getBuildingLevelName() {
    return getRegionLevelName(-1)
  }

  // Get last hierarchy key = 'building'
  List getBuildingLevel() {
    return getRegionLevel(-1)
  }

  // Get first value from last hierarchy key
  String getDefaultBuildingType() {
    return getRegionLevelFirstItem(getBuildingLevelName())
  }

  // Get region hierarchy excluding last element
  Map getRegionHierarchyWoBuilding() {
    Map hierarchy = [:]
    getRegionLevelNamesWoBuilding().each { name ->
      hierarchy[name] = getRegionLevel(name)
    }
    return hierarchy
  }

  // Get region hierarchy excluding last element
  List getRegionHierarchyWoBuildingFlatten() {
    return getRegionHierarchyWoBuilding().values().flatten()
  }

  // Get ['state', 'oblast', ...] = region hierarchy keys
  List getRegionLevelNames() {
    return keysList(getRegionHierarchy())
  }

  // Get region hierarchy keys excluding last one
  List getRegionLevelNamesWoBuilding() {
    return getRegionLevelNames() - [getBuildingLevelName()]
  }

  // Get 'state' key by index 0
  String getRegionLevelName(Integer index) {
    return getRegionLevelNames()[index]
  }

  // Get ['stateType', 'oblastType', ...] = region hierarchy key types
  List getRegionLevelTypeNames() {
    return getRegionLevelNames()*.concat('Type')
  }

  // Get ['stateType', 'oblastType', ...] = region hierarchy key types
  List getRegionLevelTypeNamesWoBuilding() {
    return getRegionLevelNamesWoBuilding()*.concat('Type')
  }

  // Get 'stateType' key type by key 'state'
  String getRegionLevelTypeName(CharSequence code) {
    return getRegionLevelTypeName(getRegionLevelNum(code))
  }

  // Get 'stateType' key type by index 0
  String getRegionLevelTypeName(Integer index) {
    return getRegionLevelTypeNames()[index]
  }

  // Get 0 index by key 'state'
  Integer getRegionLevelNum(CharSequence code) {
    return getRegionLevelNames().findIndexOf{it == code}
  }

  // Get 'REGION_TYPE_State' by 'state' key and 0 item position  (default)
  String getRegionLevelItem(CharSequence code, Integer index = 0) {
    return getRegionLevel(code)[index]
  }

  // Get 'REGION_TYPE_State' by 0 index and 0 item position (default)
  String getRegionLevelItem(Integer item, Integer index = 0) {
    return getRegionLevel(item)[index]
  }

  // Get 'REGION_TYPE_State' by 'state' key
  String getRegionLevelFirstItem(CharSequence code) {
    return getRegionLevelItem(code, 0)
  }

  // Get 'REGION_TYPE_State' by 0 index
  String getRegionLevelFirstItem(Integer index) {
    return getRegionLevelItem(index, 0)
  }

  // Search hierarchy key by 'REGION_TYPE_State' presence in each item
  String getRegionLevelNameByItem(CharSequence code) {
    String result = null
    getRegionHierarchy().each{ name, values ->
      if (code in values) {
        result = name
      }
    }
    return result
  }

  // Get [building: 'зд.', home: 'д.',]
  Map getBuildingFields(CharSequence buildingType = null) {
    Map result = [
      building: getRefName(getBuildingTypeId(buildingType))
    ]
    BUILDING_FIELDS.each{ key, value ->
      result[key] = getMessageNameByCode(value)
    }
    return result
  }

  // Get 'зд.' by 'building' key
  String getBuildingField(CharSequence code, CharSequence buildingType = null) {
    return getBuildingFields(buildingType)[code]
  }

  // Get 'зд.' by 0 index
  String getBuildingField(Integer index, CharSequence buildingType = null) {
    return getBuildingFields(buildingType)[index]
  }

  // Get ['building', 'home', ...] keys
  List getBuildingFieldNames() {
    return keysList(getBuildingFields())
  }

  // Get id of default building type
  Number getDefaultBuildingTypeId() {
    return getRefIdByCode(getDefaultBuildingType())
  }

  // Get id of default or custom building type
  Number getBuildingTypeId(CharSequence code = null) {
    if (!code) {
      return getDefaultBuildingTypeId()
    }
    return getRefIdByCode(code)
  }

  // Get default reality good
  Map getDefaultRealtyGood() {
    return getGoodBy(code: DEFAULT_REALTY_GOOD_CODE)
  }

  // Get default reality good id
  Number getDefaultRealtyGoodId() {
    return toIntSafe(getDefaultRealtyGood().n_good_id)
  }

  List getRegionsBy(Map input) {
    LinkedHashMap params = mergeParams([
      hierarchyTypeId : getRefIdByCode('HIER_REG_TYPE_Federal'),
      parRegionId     : null,
      realtyGoodId    : null,
      regionId        : null,
      regionTypeId    : null,
      building        : null,
      name            : null,
      code            : null,
      ext             : null,
      construct       : null,
      home            : null,
      ownership       : null,
      zip             : null,
      limit           : 0
    ], input)
    LinkedHashMap where = [:]

    if (params.hierarchyTypeId != null) {
      where.n_hierarchy_type_id = params.hierarchyTypeId
    }
    if (params.parRegionId != null) {
      where.n_par_region_id = params.parRegionId
    }
    if (params.realtyGoodId != null) {
      where.n_realty_good_id = params.realtyGoodId
    }
    if (params.regionId != null) {
      where.n_region_id = params.regionId
    }
    if (params.regionTypeId != null) {
      where.n_region_type_id = params.regionTypeId
    }
    if (params.building) {
      where.vc_building = params.building
    }
    if (params.name) {
      where.vc_name = params.name
    }
    if (params.code) {
      where.vc_code = params.code
    }
    if (params.ext) {
      where.vc_code_ext = params.ext
    }
    if (params.construct) {
      where.vc_construct = params.construct
    }
    if (params.home) {
      where.vc_home = params.home
    }
    if (params.ownership) {
      where.vc_ownership = params.ownership
    }
    if (params.zip) {
      where.vc_zip = params.zip
    }
    return hid.getTableData(getRegionsTable(), where: where, order: params.order, limit: params.limit)
  }

  Map getRegionBy(Map input) {
    return getRegionsBy(input + [limit: 1])?.getAt(0)
  }

  Map getRegion(def regionId) {
    return getRegionBy(regionId: regionId)
  }

  Map getRegionTree(def regionId) {
    //Get region data from database
    String query = """
    SELECT 'vc_region_type', SI_REF_PKG_S.GET_CODE_BY_ID(N_REGION_TYPE_ID),"""
    List fields = hid.getTableColumns(getRegionsTable())
    fields.each{ field ->
      query += """
      '${field.toLowerCase()}', ${field},"""
    }

    List hierarchyFlatten = getRegionHierarchyWoBuildingFlatten()
    hierarchyFlatten.each{ code ->
      query += """
      '${code}', SR_REGIONS_PKG_S.GET_UPPER_REGION_CODE(N_REGION_ID, SI_REF_PKG_S.GET_ID_BY_CODE('${code}')),"""
    }.join(',')


    query = query.replaceAll(/,*$/, '') + """
    FROM ${getRegionsTable()}
    WHERE N_REGION_ID = ${regionId}"""

    LinkedHashMap data = hid.queryFirst(query, true)
    if (!data) {
      return [:]
    }

    LinkedHashMap result = [:]
    hierarchyFlatten.each{ code ->
      if (data[code]) {
        String name = getRegionLevelNameByItem(code)

        result[name] = data[code] //oblast = 'Some value'
        result[getRegionLevelTypeName(name)] = code //oblastType = 'REGION_TYPE_Oblast'
      }
    }

    result.regionType = data.vc_region_type
    if (result.regionType in getBuildingLevel()) {
      result.building     = data.vc_code
      result.buildingType = data.vc_region_type
      result.home         = data.vc_home
      result.corpus       = data.vc_building
      result.construct    = data.vc_construct
    } else {
      result.building     = null
      result.buildingType = null
      result.home         = null
      result.corpus       = null
      result.construct    = null
    }

    return result
  }

  List getRegionItems(Map input) {
    List queryParts = []
    List typeNames = getRegionLevelTypeNamesWoBuilding()
    typeNames.eachWithIndex{ type, i ->
      queryParts << """
      SELECT VC_VALUE, NVL(VC_VALUE_2,'N'), '${input[getRegionLevelName(i)] ?: ''}'
      FROM   ${getRefsTable()}
      WHERE  VC_CODE = '${input[type] ?: ""}'"""
    }

    String regionQuery = joinNonEmpty(queryParts, """
    UNION ALL""")
    return hid.queryDatabase(regionQuery)
  }

  Map putRegion(Map input) {
    LinkedHashMap defaultParams = [
      regionId     : null,
      regionTypeId : null,
      realtyGoodId : null,
      parRegionId  : null,
      code         : null,
      name         : null,
      home         : null,
      building     : null,
      construct    : null,
      ownership    : null
    ]
    try {
      LinkedHashMap existingRegion = [:]
      if (notEmpty(input.regionId)) {
        LinkedHashMap region = getRegion(params.regionId)
        existingRegion = [
          regionId     : region.n_region_id,
          regionTypeId : region.n_region_type_id,
          realtyGoodId : region.n_realty_good_id,
          parRegionId  : region.n_par_region_id,
          code         : region.vc_code,
          name         : region.vc_name,
          home         : region.vc_home,
          building     : region.vc_building,
          construct    : region.vc_construct,
          ownership    : region.vc_ownership
        ]
      }
      LinkedHashMap params = mergeParams(defaultParams, existingRegion + input)

      logger.info("${params.regionId ? 'Updating' : 'Creating'} region with params ${params}")

      LinkedHashMap result = hid.execute('SR_REGIONS_PKG.SR_REGIONS_PUT', [
        num_N_REGION_ID      : params.regionId,
        num_N_REGION_TYPE_ID : params.regionTypeId,
        num_N_PAR_REGION_ID  : params.parRegionId,
        num_N_REALTY_GOOD_ID : params.realtyGoodId,
        vch_VC_CODE          : params.code,
        vch_VC_NAME          : params.name,
        vch_VC_HOME          : params.home,
        vch_VC_BUILDING      : params.building,
        vch_VC_CONSTRUCT     : params.construct,
        vch_VC_OWNERSHIP     : params.ownership
      ])
      logger.info("   Region ${result.num_N_REGION_ID} was ${params.regionId ? 'updated' : 'created'} successfully!")
      return result
    } catch (Exception e){
      logger.error("   Error while ${input.regionId ? 'updating' : 'creating'} region!")
      logger.error_oracle(e)
    }
  }

  Map createRegion(Map input) {
    input.remove('regionId')
    return putRegion(input)
  }

  Map updateRegion(Map input) {
    return putRegion(input)
  }

  Map updateRegion(Map input = [:], def regionId) {
    return updateRegion(input + [regionId: regionId])
  }

  Boolean isRegionEmpty(Map input) {
    Boolean result = true
    List levelNames = getRegionLevelNames()

    (['regionId'] + levelNames).each { name ->
      if (notEmpty(input[name])) {
        result = false
      }
    }
    return result
  }

  Boolean notRegionEmpty(Map input) {
    return !isRegionEmpty(input)
  }

  Number createRegionTree(Map input) {
    logger.info("Trying to create region ${input}")
    def regionId       = 0
    Map existingRegion = [:]
    Map region         = [:]

    List levelNames = getRegionLevelNamesWoBuilding()

    //Create all regions need step by step
    levelNames.each { CharSequence name ->
      String type = getRegionLevelTypeName(name)
      if (input[name]) {
        existingRegion = getRegionBy(
          parRegionId : regionId,
          regionType  : input[type],
          code        : input[name]
        )

        if (existingRegion) {
          regionId = existingRegion.n_region_id
        } else {
          region = putRegion(
            parRegionId : regionId,
            regionType  : input[type],
            code        : input[name]
          )
          if (region) {
            regionId = region.num_N_REGION_ID
          }
        }
      }
    }

    Map buildingFields = [:]
    getBuildingFieldNames().each { field ->
      def value = input[field]
      if (notEmpty(value)) {
        if (field == 'building') {
          buildingFields['code'] = value
        } else if (field == 'corpus') {
          buildingFields['building'] = value
        } else {
          buildingFields[field] = value
        }
      }
    }

    if (buildingFields) {
      region = putRegion([
        parRegionId  : regionId,
        regionTypeId : getBuildingTypeId(buildingFields['buildingType'] ?: buildingFields['regionType']),
        realtyGoodId : buildingFields['buildingGoodId'] ?: (buildingFields['buildingGood'] ? getGoodBy(code: buildingFields['buildingGood'])?.n_good_id : getDefaultRealtyGoodId()),
      ] + buildingFields)
      if (region) {
        regionId = region.num_N_REGION_ID
      }
    }
    return toIntSafe(regionId)
  }
}