package org.camunda.latera.bss.connectors.hid.hydra

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

  LinkedHashMap getRegion(regionId) {
    LinkedHashMap where = [
      n_region_id: regionId
    ]
    return hid.getTableFirst(REGIONS_TABLE, where: where)
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

    LinkedHashMap data = hid.queryFirst(query)

    //Replace empty hierarchial values
    LinkedHashMap result = [
      type: REGION_HIERARCHY_FLATTEN[0]
    ]
    REGION_HIERARCHY_FLATTEN.each{ code ->
      if (data[code]) {
        REGION_HIERARCHY.each { name, values ->
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
    result.entrance  = null
    result.floor     = null
    result.flat      = null

    return result
  }

  List getRegionItemsValues(
    LinkedHashMap input
  ) {
    String regionQuery = ""
    REGION_TYPES.eachWithIndex{ type, i -> 
      regionQuery += """
      SELECT VC_VALUE, NVL(VC_VALUE_2,'N'), '${input[REGION_NAMES[i]] ?: ''}'
      FROM   SI_V_REF
      WHERE  VC_CODE = '${input[type] ?: ""}'""" + (type == REGION_TYPES.last() ? '' : """
      UNION ALL""")
    }

    return hid.queryDatabase(regionQuery, false)
  }
}