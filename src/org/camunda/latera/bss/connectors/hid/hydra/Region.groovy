package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.utils.StringUtil

trait Region {
  private static String REGIONS_TABLE = 'SR_V_REGIONS'
  private static LinkedHashMap REGION_HIERARCHY = [
    state    : ['REGION_TYPE_State'],
    oblast   : ['REGION_TYPE_Oblast'],
    okrug    : ['REGION_TYPE_Okrug'],
    district : ['REGION_TYPE_District'],
    city     : ['REGION_TYPE_City','REGION_TYPE_UrbanVillage','REGION_TYPE_Settlement', 'REGION_TYPE_Village'],
    street   : ['REGION_TYPE_Street','REGION_TYPE_Avenue','REGION_TYPE_Passage', 'REGION_TYPE_Highway','REGION_TYPE_SideStreet','REGION_TYPE_Seafront','REGION_TYPE_Boulevard']
  ]
  private static List REGION_HIERARCHY_FLATTEN = REGION_HIERARCHY.values().flatten()
  private static List REGION_NAMES = REGION_HIERARCHY.keySet() as List
  private static List REGION_TYPES = REGION_NAMES*.concat("Type")
  private static String BUILDING_TYPE = 'REGION_TYPE_Building'
  private static String DEFAULT_REALTY_GOOD_CODE = 'REALTY_House'

  def getBuildingType(){
    return getRefIdByCode(BUILDING_TYPE)
  }

  def getDefaultRealtyGood(){
    return getGoodByCode(DEFAULT_REALTY_GOOD_CODE)
  }

  def getDefaultRealtyGoodId(){
    return getDefaultRealtyGood().n_good_id
  }

  LinkedHashMap getRegion(regionId) {
    LinkedHashMap where = [
      n_region_id: regionId
    ]
    return hid.getTableFirst(REGIONS_TABLE, where: where)
  }

  LinkedHashMap getRegionByCodeTypeAndParent(
    def parRegionId,
    def regionTypeId,
    String code
  ) {
    LinkedHashMap where = [
      n_par_region_id  : parRegionId,
      n_region_type_id : regionTypeId,
      vc_code          : code,
    ]
    return hid.getTableFirst(REGIONS_TABLE, where: where)
  }

  LinkedHashMap getRegionByCodeTypeAndParent(LinkedHashMap input) {
    def params = mergeParams([
      parRegionId  : null,
      regionTypeId : null,
      code         : null
    ], input)
    return getRegionByCodeTypeAndParent(params.parRegionId, params.regionTypeId, params.code)
  }

  LinkedHashMap getRegionTree(regionId) {
    //Get region data from database
    String query = """
    SELECT 'vc_region_type', SI_REF_PKG_S.GET_CODE_BY_ID(N_REGION_TYPE_ID),"""
    List fields = hid.getTableColumns(REGIONS_TABLE)
    fields.each{ field ->
      query += """
      '${field.toLowerCase()}', ${field},"""
    }

    REGION_HIERARCHY_FLATTEN.each{ typeName ->
      query += """
      '${typeName}', SR_REGIONS_PKG_S.GET_UPPER_REGION_CODE(N_REGION_ID, SI_REF_PKG_S.GET_ID_BY_CODE('${typeName}'))""" + (typeName == REGION_HIERARCHY_FLATTEN.last() ? '' : ',')
    }.join(',')

    query += """
    FROM ${REGIONS_TABLE}
    WHERE N_REGION_ID = ${regionId}"""

    LinkedHashMap data = hid.queryFirst(query, true)

    if (!data) {
      return {}
    }

    //Replace empty hierarchial values
    LinkedHashMap result = [
      type: REGION_HIERARCHY_FLATTEN[0]
    ]
    REGION_HIERARCHY_FLATTEN.each{ code ->
      if (data[code]) {
        REGION_HIERARCHY.each{ name, values ->
          if (values.contains(code)) {
            result[name] = data[code] //oblast = 'Some'
            result[REGION_TYPES[REGION_NAMES.findIndexOf{it == name}]] = code //oblastType = 'REGION_TYPE_Oblast'
          }
        }
      }
    }

    if (data.vc_region_type){
      result.type = data.vc_region_type
    }

    result.building  = data.vc_building
    result.home      = data.vc_home
    result.corpus    = data.vc_corpus
    result.construct = data.vc_construct

    return result
  }

  List getRegionItemsValues(
    LinkedHashMap input
  ) {
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

  def putRegion(LinkedHashMap input) {
    def defaultParams = [
      regionId     :  null,
      regionTypeId :  null,
      realtyGoodId :  null,
      parRegionId  :  null,
      code         :  null,
      name         :  null,
      home         :  null,
      building     :  null,
      construct    :  null
    ]
    LinkedHashMap params = mergeParams(defaultParams, input)
    try {
      logger.info("Putting region type ${params.regionTypeId} with parent ${params.parRegionId}, code ${params.code}, name ${params.name}, home ${params.home}, building ${params.building}, construct ${params.construct} and realty ${params.realtyGoodId}")

      def region = hid.execute('SR_REGIONS_PKG.SR_REGIONS_PUT', [
        num_N_REGION_ID       : params.regionId,
        num_N_REGION_TYPE_ID  : params.regionTypeId,
        num_N_PAR_REGION_ID   : params.parRegionId,
        num_N_REALTY_GOOD_ID  : params.realtyGoodId,
        vch_VC_CODE           : params.code,
        vch_VC_NAME           : params.name,
        vch_VC_HOME           : params.home,
        vch_VC_BUILDING       : params.building,
        vch_VC_CONSTRUCT      : params.construct
      ])
      logger.info("   Region ${region.num_N_REGION_ID} was put successfully!")
      return region
    } catch (Exception e){
      logger.error("Error while putting region!")
      logger.error(e)
    }
  }

  def createRegionTree(
    LinkedHashMap input
  ) {
    logger.info("Trying to create region ${input}")
    def regionId = 0
    Integer typeIndex = 0

    //Create all regions need step by step
    for(Integer i = typeIndex; i < REGION_NAMES.toArray().length; ++i) {
      String name = REGION_NAMES[i]
      String type = REGION_TYPES[i]
      if (input[name]) {
        LinkedHashMap existingRegion = getRegionByCodeTypeAndParent(
          parRegionId : regionId,
          regionType  : input[type],
          code        : input[name]
        )

        if(existingRegion) {
          regionId = existingRegion.n_region_id
        } else {
          LinkedHashMap region = putRegion([
            parRegionId : regionId,
            regionType  : input[type],
            code        : input[name]
          ])
          if (region) {
            regionId = region.num_N_REGION_ID
          }
        }
      }
    }

    if (getRefIdByCode(input.type) == getBuildingType()) {
      LinkedHashMap region = putRegion([
        parRegionId  : regionId,
        regionTypeId : getBuildingType(),
        realtyGoodId : getDefaultRealtyGoodId(),
        home         : input.home,
        building     : input.building,
        construct    : input.construct
      ])
      if (region) {
        regionId = region.num_N_REGION_ID
      }
    }
    return regionId
  }
}