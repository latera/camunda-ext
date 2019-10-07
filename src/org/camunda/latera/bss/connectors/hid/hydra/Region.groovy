package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.utils.StringUtil

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

  def getRegionsTable() {
    return REGIONS_TABLE
  }

  def getRegionHierarchy(){
    return regionHierarchyOverride ?: REGION_HIERARCHY
  }

  def getRegionHierarchyWithBuilding(){
    return getRegionHierarchy() + [
      building: [getBuildingType()]
    ]
  }

  def getRegionHierarchyFlatten(){
    return getRegionHierarchy().values().flatten()
  }

  def getRegionHierarchyFlattenWithBuilding(){
    return getRegionHierarchyWithBuilding().values().flatten()
  }

  def getRegionNames(){
    return getRegionHierarchy().keySet() as List
  }

  def getRegionNamesWithBuilding(){
    return getRegionHierarchyWithBuilding().keySet() as List
  }

  def getRegionTypes(){
    return getRegionNames()*.concat('Type')
  }

  def getRegionTypesWithBuilding(){
    return getRegionNamesWithBuilding()*.concat('Type')
  }

  def getBuildingType(){
    return BUILDING_TYPE
  }

  def getBuildingTypeId(){
    return getRefIdByCode(getBuildingType())
  }

  def getDefaultRealtyGood(){
    return getGoodBy(code: DEFAULT_REALTY_GOOD_CODE)
  }

  def getDefaultRealtyGoodId(){
    return getDefaultRealtyGood().n_good_id
  }

  LinkedHashMap getRegionsBy(LinkedHashMap input) {
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

  LinkedHashMap getRegionBy(LinkedHashMap input) {
    return getRegionsBy(input)?.getAt(0)
  }

  LinkedHashMap getRegion(regionId) {
    return getRegionBy(regionId: regionId)
  }

  Integer getRegionLevelNum(String code) {
    return getRegionNamesWithBuilding().findIndexOf{it == code}
  }

  String getRegionLevelByTypeCode(String code) {
    String result = null
    Map regionHierarchy = getRegionHierarchyWithBuilding()
    regionHierarchy.each { String name, List values ->
      if (values.contains(code)) {
        result = name
      }
    }
    return result
  }

  Integer getRegionLevelNumByTypeCode(String code) {
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

  LinkedHashMap getRegionTree(regionId) {
    //Get region data from database
    String query = """
    SELECT 'vc_region_type', SI_REF_PKG_S.GET_CODE_BY_ID(N_REGION_TYPE_ID),"""
    List fields = hid.getTableColumns(getRegionsTable())
    fields.each{ field ->
      query += """
      '${field.toLowerCase()}', ${field},"""
    }

    List regionHierarchyFlatten = getRegionHierarchyFlatten()
    List regionTypes = getRegionTypes()
    regionHierarchyFlatten.each{ typeName ->
      query += """
      '${typeName}', SR_REGIONS_PKG_S.GET_UPPER_REGION_CODE(N_REGION_ID, SI_REF_PKG_S.GET_ID_BY_CODE('${typeName}'))""" + (typeName == regionHierarchyFlatten.last() ? '' : ',')
    }.join(',')

    query += """
    FROM ${getRegionsTable()}
    WHERE N_REGION_ID = ${regionId}"""

    LinkedHashMap data = hid.queryFirst(query, true)

    if (!data) {
      return [:]
    }

    LinkedHashMap result = [:]
    regionHierarchyFlatten.each { String code ->
      if (data[code]) {
        String name = getRegionLevelByTypeCode(code)
        Integer index = getRegionLevelNum(name)

        result[name] = data[code] //oblast = 'Some value'
        result[regionTypes[index]] = code //oblastType = 'REGION_TYPE_Oblast'
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

  List getRegionItemsValues(
    LinkedHashMap input
  ) {
    String regionQuery = ""
    List regionNames = getRegionNames()
    List regionTypes = getRegionTypes()
    regionTypes.eachWithIndex{ type, i ->
      regionQuery += """
      SELECT VC_VALUE, NVL(VC_VALUE_2,'N'), '${input[regionNames[i]] ?: ''}'
      FROM   ${getRefsTable()}
      WHERE  VC_CODE = '${input[type] ?: ""}'""" + (type == regionTypes.last() ? '' : """
      UNION ALL""")
    }

    return hid.queryDatabase(regionQuery)
  }

  def putRegion(LinkedHashMap input) {
    def defaultParams = [
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

      def region = hid.execute('SR_REGIONS_PKG.SR_REGIONS_PUT', [
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

  def createRegionTree(
    LinkedHashMap input
  ) {
    logger.info("Trying to create region ${input}")
    def regionId      = 0
    Integer typeIndex = 0
    LinkedHashMap existingRegion = null
    LinkedHashMap region = null

    List regionNames = getRegionNames()
    List regionTypes = getRegionTypes()
    //Create all regions need step by step
    for(Integer i = typeIndex; i < regionNames.toArray().length; ++i) {
      String name = regionNames[i]
      String type = regionTypes[i]
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

    if (StringUtil.notEmpty(input.building) || StringUtil.notEmpty(input.home) || StringUtil.notEmpty(input.corpus) || StringUtil.notEmpty(input.construct)) {
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
    return regionId
  }
}