package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.StringUtil.notEmpty
import static org.camunda.latera.bss.utils.Numeric.toIntSafe

trait Region {
  private static String REGIONS_TABLE = 'SR_V_REGIONS'
  private static String BUILDING_TYPE = 'REGION_TYPE_Building'
  private static String DEFAULT_REALTY_GOOD_CODE = 'REALTY_House'
  private static LinkedHashMap REGION_HIERARCHY = [
    state    : ['REGION_TYPE_State'],
    oblast   : ['REGION_TYPE_Oblast'],
    okrug    : ['REGION_TYPE_Okrug'],
    district : ['REGION_TYPE_District'],
    city     : ['REGION_TYPE_City','REGION_TYPE_UrbanVillage','REGION_TYPE_Settlement', 'REGION_TYPE_Village'],
    street   : ['REGION_TYPE_Street','REGION_TYPE_Avenue','REGION_TYPE_Passage', 'REGION_TYPE_Highway','REGION_TYPE_SideStreet','REGION_TYPE_Seafront','REGION_TYPE_Boulevard', 'REGION_TYPE_Square']
  ]
  private static LinkedHashMap REGION_HIERARCHY_WITH_BUILDING = REGION_HIERARCHY + [
    building: [BUILDING_TYPE]
  ]
  private static List REGION_HIERARCHY_FLATTEN = REGION_HIERARCHY.values().flatten()
  private static List REGION_HIERARCHY_FLATTEN_WITH_BUILDING = REGION_HIERARCHY_WITH_BUILDING.values().flatten()
  private static List REGION_NAMES = REGION_HIERARCHY.keySet() as List
  private static List REGION_NAMES_WITH_BUILDING = REGION_HIERARCHY_WITH_BUILDING.keySet() as List
  private static List REGION_TYPES = REGION_NAMES*.concat("Type")
  private static List REGION_TYPES_WITH_BUILDING = REGION_NAMES_WITH_BUILDING*.concat("Type")

  String getRegionsTable() {
    return REGIONS_TABLE
  }

  Map getRegionHierarchy(){
    return REGION_HIERARCHY
  }

  Map getRegionHierarchyWithBuilding(){
    return REGION_HIERARCHY_WITH_BUILDING
  }

  List getRegionHierarchyFlatten(){
    return REGION_HIERARCHY_FLATTEN
  }

  List getRegionHierarchyFlattenWithBuilding(){
    return REGION_HIERARCHY_FLATTEN_WITH_BUILDING
  }

  List getRegionNames(){
    return REGION_NAMES
  }

  List getRegionNamesWithBuilding(){
    return REGION_NAMES_WITH_BUILDING
  }

  List getRegionTypes(){
    return REGION_TYPES
  }

  List getRegionTypesWithBuilding(){
    return REGION_TYPES_WITH_BUILDING
  }

  String getBuildingType(){
    return BUILDING_TYPE
  }

  Number getBuildingTypeId(){
    return getRefIdByCode(BUILDING_TYPE)
  }

  Map getDefaultRealtyGood(){
    return getGoodBy(code: DEFAULT_REALTY_GOOD_CODE)
  }

  Number getDefaultRealtyGoodId(){
    return toIntSafe(getDefaultRealtyGood().n_good_id)
  }

  Map getRegionsBy(Map input) {
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
    return hid.getTableData(getRegionsTable(), where: where)
  }

  Map getRegionBy(Map input) {
    return getRegionsBy(input)?.getAt(0)
  }

  Map getRegion(regionId) {
    return getRegionBy(regionId: regionId)
  }

  Integer getRegionLevelNum(CharSequence code) {
    return REGION_NAMES_WITH_BUILDING.findIndexOf{it == code}
  }

  String getRegionLevelByTypeCode(CharSequence code) {
    String result = null
    REGION_HIERARCHY_WITH_BUILDING.each{ name, values ->
      if (values.contains(code)) {
        result = name
      }
    }
    return result
  }

  Integer getRegionLevelNumByTypeCode(CharSequence code) {
    String name = getRegionLevelByTypeCode(code)
    return getRegionLevelNum(name)
  }

  String getRegionLevelById(regionId) {
    String regionType = getRefCodeById(getRegion(regionId).n_region_type_id)
    return getRegionLevelByTypeCode(regionType)
  }

  Integer getRegionLevelNumById(regionId) {
    String regionType = getRefCodeById(getRegion(regionId).n_region_type_id)
    return getRegionLevelNumByTypeCode(regionType)
  }

  Map getRegionTree(regionId) {
    //Get region data from database
    String query = """
    SELECT 'vc_region_type', SI_REF_PKG_S.GET_CODE_BY_ID(N_REGION_TYPE_ID),"""
    List fields = hid.getTableColumns(getRegionsTable())
    fields.each{ field ->
      query += """
      '${field.toLowerCase()}', ${field},"""
    }

    REGION_HIERARCHY_FLATTEN.each{ typeName ->
      query += """
      '${typeName}', SR_REGIONS_PKG_S.GET_UPPER_REGION_CODE(N_REGION_ID, SI_REF_PKG_S.GET_ID_BY_CODE('${typeName}'))""" + (typeName == REGION_HIERARCHY_FLATTEN.last() ? '' : ',')
    }.join(',')

    query += """
    FROM ${getRegionsTable()}
    WHERE N_REGION_ID = ${regionId}"""

    LinkedHashMap data = hid.queryFirst(query, true)

    if (!data) {
      return [:]
    }

    LinkedHashMap result = [:]
    REGION_HIERARCHY_FLATTEN.each{ code ->
      if (data[code]) {
        String name = getRegionLevelByTypeCode(code)
        Integer index = getRegionLevelNum(name)

        result[name] = data[code] //oblast = 'Some value'
        result[REGION_TYPES[index]] = code //oblastType = 'REGION_TYPE_Oblast'
      }
    }

    result.regionType = data.vc_region_type
    if (result.regionType == getBuildingType()) {
      result.building  = data.vc_code
      result.home      = data.vc_home
      result.corpus    = data.vc_building
      result.construct = data.vc_construct
    } else {
      result.building   = null
      result.home       = null
      result.corpus     = null
      result.construct  = null
    }

    return result
  }

  List getRegionItemsValues(Map input) {
    String regionQuery = ""
    REGION_TYPES.eachWithIndex{ type, i ->
      regionQuery += """
      SELECT VC_VALUE, NVL(VC_VALUE_2,'N'), '${input[REGION_NAMES[i]] ?: ''}'
      FROM   ${getRefsTable()}
      WHERE  VC_CODE = '${input[type] ?: ""}'""" + (type == REGION_TYPES.last() ? '' : """
      UNION ALL""")
    }

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
      construct    : null
    ]
    LinkedHashMap params = mergeParams(defaultParams, input)
    try {
      logger.info("Putting region with params ${params}")

      LinkedHashMap region = hid.execute('SR_REGIONS_PKG.SR_REGIONS_PUT', [
        num_N_REGION_ID      : params.regionId,
        num_N_REGION_TYPE_ID : params.regionTypeId,
        num_N_PAR_REGION_ID  : params.parRegionId,
        num_N_REALTY_GOOD_ID : params.realtyGoodId,
        vch_VC_CODE          : params.code,
        vch_VC_NAME          : params.name,
        vch_VC_HOME          : params.home,
        vch_VC_BUILDING      : params.building,
        vch_VC_CONSTRUCT     : params.construct
      ])
      logger.info("   Region ${region.num_N_REGION_ID} was put successfully!")
      return region
    } catch (Exception e){
      logger.error("   Error while putting region!")
      logger.error_oracle(e)
    }
  }

  Number createRegionTree(Map input) {
    logger.info("Trying to create region ${input}")
    def regionId      = 0
    Integer typeIndex = 0
    LinkedHashMap existingRegion = null
    LinkedHashMap region = null

    //Create all regions need step by step
    for(Integer i = typeIndex; i < REGION_NAMES.toArray().size(); ++i) {
      String name = REGION_NAMES[i]
      String type = REGION_TYPES[i]
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

    if (notEmpty(input.building) || notEmpty(input.home) || notEmpty(input.corpus) || notEmpty(input.construct)) {
      region = putRegion(
        parRegionId  : regionId,
        regionTypeId : getBuildingTypeId(),
        realtyGoodId : getDefaultRealtyGoodId(),
        code         : input.building,
        home         : input.home,
        building     : input.corpus,
        construct    : input.construct
      )
      if (region) {
        regionId = region.num_N_REGION_ID
      }
    }
    return toIntSafe(regionId)
  }
}