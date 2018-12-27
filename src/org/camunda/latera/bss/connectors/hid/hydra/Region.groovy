package org.camunda.latera.bss.connectors.hid.hydra

trait Region {
  //Regions staff
  static LinkedHashMap REGION_HIERARCHY = [
    state    : ['REGION_TYPE_State'],
    oblast   : ['REGION_TYPE_Oblast'],
    okrug    : ['REGION_TYPE_Okrug'],
    district : ['REGION_TYPE_District'],
    city     : ['REGION_TYPE_City','REGION_TYPE_UrbanVillage','REGION_TYPE_Settlement', 'REGION_TYPE_Village'],
    street   : ['REGION_TYPE_Street','REGION_TYPE_Avenue','REGION_TYPE_Passage', 'REGION_TYPE_Highway','REGION_TYPE_SideStreet','REGION_TYPE_Seafront','REGION_TYPE_Boulevard']
  ]
  static List REGION_HIERARCHY_FLATTEN = REGION_HIERARCHY.values() as List
  static List REGION_NAMES = REGION_HIERARCHY.keySet() as List
  static List REGION_TYPES = REGION_NAMES.each{ it -> it + "Type" }

  LinkedHashMap getRegion(regionId) {
    LinkedHashMap where = [
      n_region_id: regionId
    ]
    return this.hid.getTableFirst('SR_V_REGIONS', where: where)
  }

  LinkedHashMap getRegionTree(regionId) {
    //Get region data from database
    String query = """
    SELECT 'vc_region_type', SI_REF_PKG_S.GET_CODE_BY_ID(R.N_REGION_TYPE_ID),
    """
    List fields = this.hid.getTableColumns('SR_V_REGIONS')
    fields.each{ field ->
      query += """
      '${field}', ${field},
      """
    }

    query += this.REGION_HIERARCHY_FLATTEN.map{ type_name ->
      """
      '${type_name}', SR_REGIONS_PKG_S.GET_UPPER_REGION_CODE(R.N_REGION_ID, SI_REF_PKG_S.GET_ID_BY_CODE('${type_name}'))
      """
    }.join(',')

    query += """
    FROM
      SR_V_REGIONS R
    WHERE
      R.N_REGION_ID = ${regionId}
    """

    LinkedHashMap data = this.hid.queryFirst(query)

    //Replace empty hierarchial values
    LinkedHashMap result = [
      type: this.REGION_HIERARCHY_FLATTEN[0]
    ]
    this.REGION_HIERARCHY_FLATTEN.each{ code ->
      if (data[code]) {
        for (item in this.REGION_HIERARCHY) {
          String name = item.key
          List values = item.value
          if (values.contains(code)) {
            result[name] = data[code] //oblast = 'Some'
            result[this.REGION_TYPES[this.REGION_NAMES.findIndexOf{it == name}]] = name //oblastType = 'REGION_TYPE_Oblast'
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
}