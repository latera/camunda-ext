package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.StringUtil.isEmpty
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
  */
  private static LinkedHashMap REGION_HIERARCHY = [
    state    : ['REGION_TYPE_State'],
    oblast   : ['REGION_TYPE_Oblast','REGION_TYPE_Republic'],
    okrug    : ['REGION_TYPE_Okrug','REGION_TYPE_Territory'],
    district : ['REGION_TYPE_District'],
    city     : ['REGION_TYPE_City','REGION_TYPE_UrbanVillage','REGION_TYPE_Settlement','REGION_TYPE_Village','REGION_TYPE_TheSettlement'],
    street   : ['REGION_TYPE_Street','REGION_TYPE_Avenue','REGION_TYPE_Passage','REGION_TYPE_Highway','REGION_TYPE_SideStreet','REGION_TYPE_Seafront','REGION_TYPE_Boulevard','REGION_TYPE_Square','REGION_TYPE_Line'],
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
    ownership : 'Region_Ownership_Short'
  ]

  /**
   * Get regions table name
   */
  String getRegionsTable() {
    return REGIONS_TABLE
  }

  /**
   * Get full region hierarchy
   * @return Region hierarchy map, e.g.:
   * <pre>
   * {@code
   * [
   *   state    : ['REGION_TYPE_State'],
   *   oblast   : ['REGION_TYPE_Oblast'],
   *   okrug    : ['REGION_TYPE_Okrug'],
   *   district : ['REGION_TYPE_District'],
   *   city     : ['REGION_TYPE_City','REGION_TYPE_UrbanVillage','REGION_TYPE_Settlement', 'REGION_TYPE_Village'],
   *   street   : ['REGION_TYPE_Street','REGION_TYPE_Avenue','REGION_TYPE_Passage', 'REGION_TYPE_Highway','REGION_TYPE_SideStreet','REGION_TYPE_Seafront','REGION_TYPE_Boulevard', 'REGION_TYPE_Square'],
   *   building : ['REGION_TYPE_Building']
   * ]
   * }
   * </pre>
   */
  Map getRegionHierarchy() {
    return regionHierarchyOverride ?: REGION_HIERARCHY
  }

  /**
   * Get region level items by its key
   * @param code {@link CharSequence String}. Region level key, e.g. 'state'
   * @return List[String] of region level items, e.g. {@code ['REGION_TYPE_State']}
   */
  List getRegionLevel(CharSequence code) {
    return getRegionHierarchy()[code]
  }

  /**
   * Get region level items by its index
   * @param index {@link Integer}. Region level index, e.g. 0
   * @return List[String] of region level items, e.g. {@code ['REGION_TYPE_State']}
   */
  List getRegionLevel(Integer index) {
    return getRegionLevel(getRegionLevelName(index))
  }

  /**
   * Get last hierarchy key
   * @return 'building'
   */
  String getBuildingLevelName() {
    return getRegionLevelName(-1)
  }

  /**
   * Get last hierarchy key items
   * @return ['REGION_TYPE_Building']
   */
  List getBuildingLevel() {
    return getRegionLevel(-1)
  }

  /**
   * Get first value from last hierarchy key
   * @return 'REGION_TYPE_Building'
   */
  String getDefaultBuildingType() {
    return getRegionLevelFirstItem(getBuildingLevelName())
  }

  /**
   * Get region hierarchy excluding last element
   * @return Region hierarchy without last level, e.g.:
   * <pre>
   * {@code
   * [
   *   state    : ['REGION_TYPE_State'],
   *   oblast   : ['REGION_TYPE_Oblast'],
   *   okrug    : ['REGION_TYPE_Okrug'],
   *   district : ['REGION_TYPE_District'],
   *   city     : ['REGION_TYPE_City','REGION_TYPE_UrbanVillage','REGION_TYPE_Settlement', 'REGION_TYPE_Village'],
   *   street   : ['REGION_TYPE_Street','REGION_TYPE_Avenue','REGION_TYPE_Passage', 'REGION_TYPE_Highway','REGION_TYPE_SideStreet','REGION_TYPE_Seafront','REGION_TYPE_Boulevard', 'REGION_TYPE_Square']
   * ]
   * }
   * </pre>
   * @see #getRegionHierarchy()
   */
  Map getRegionHierarchyWoBuilding() {
    Map hierarchy = getRegionLevelNamesWoBuilding().collectEntries { CharSequence name ->
      [(name): getRegionLevel(name)]
    }
    return hierarchy
  }

  /**
   * Get flatten region hierarchy excluding last element
   * @return ['REGION_TYPE_State', ..., 'REGION_TYPE_Square']
   * @see #getRegionHierarchyWoBuilding()
   */
  List getRegionHierarchyWoBuildingFlatten() {
    return getRegionHierarchyWoBuilding().values().flatten()
  }

  /**
   * Get region hierarchy keys
   * @return Level keys, e.g. {@code ['state', 'oblast', ...]}
   * @see #getRegionHierarchy()
   */
  List getRegionLevelNames() {
    return keysList(getRegionHierarchy())
  }

  /**
   * Get region hierarchy keys excluding last one
   * @return Level keys, e.g. {@code ['state', 'oblast', ..., 'street']}
   * @see #getRegionLevelNames()
   */
  List getRegionLevelNamesWoBuilding() {
    return getRegionLevelNames() - [getBuildingLevelName()]
  }

  /**
   * Get region hierarchy keys by index
   * @param index {@link Integer}. Region level index, e.g. 0
   * @return Level key, e.g. 'state'
   * @see #getRegionLevelNames()
   */
  String getRegionLevelName(Integer index) {
    return getRegionLevelNames()[index]
  }

  /**
   * Get region hierarchy key types
   * @return Level key types, e.g. {@code ['stateType', 'oblastType', ...]}
   * @see #getRegionHierarchy()
   */
  List getRegionLevelTypeNames() {
    return getRegionLevelNames()*.concat('Type')
  }

  /**
   * Get region hierarchy key types excluding last one
   * @return Level key types, e.g. {@code ['stateType', 'oblastType', ..., 'streetType']}
   * @see #getRegionLevelTypeNames()
   */
  List getRegionLevelTypeNamesWoBuilding() {
    return getRegionLevelNamesWoBuilding()*.concat('Type')
  }

  /**
   * Get region hierarchy key type by key
   * @param code {@link CharSequence String}. Region level key, e.g. 'state'
   * @return Level key type, e.g. 'stateType'
   * @see #getRegionLevelTypeNames()
   */
  String getRegionLevelTypeName(CharSequence code) {
    return getRegionLevelTypeName(getRegionLevelNum(code))
  }

  /**
   * Get region hierarchy key type by index
   * @param index {@link Integer}. Region level index, e.g. 0
   * @return Level key type, e.g. 'stateType'
   * @see #getRegionLevelTypeNames()
   */
  String getRegionLevelTypeName(Integer index) {
    return getRegionLevelTypeNames()[index]
  }

  /**
   * Get region hierarchy level index by key
   * @param code {@link CharSequence String}. Region level key, e.g. 'state'
   * @return Level index, e.g. 0
   * @see #getRegionLevelNames()
   */
  Integer getRegionLevelNum(CharSequence code) {
    return getRegionLevelNames().findIndexOf{it == code}
  }

  /**
   * Get region hierarchy level item by level key and item position
   * @param code     {@link CharSequence String}. Region level key, e.g. 'state'
   * @param position {@link Integer}. Item position, e.g. 0. Default: 0
   * @return Item, e.g. 'REGION_TYPE_State'
   * @see #getRegionLevel(CharSequence)
   */
  String getRegionLevelItem(CharSequence code, Integer position = 0) {
    return getRegionLevel(code)[position]
  }

  /**
   * Get region hierarchy level item by level index and item position
   * @param index    {@link Integer}. Region level index, e.g. 0
   * @param position {@link Integer}. Item position, e.g. 0. Default: 0
   * @return Item, e.g. 'REGION_TYPE_State'
   * @see #getRegionLevel(Integer)
   */
  String getRegionLevelItem(Integer item, Integer position = 0) {
    return getRegionLevel(item)[position]
  }

  /**
   * Get region hierarchy level first item by level key
   * @param code {@link CharSequence String}. Region level key, e.g. 'state'
   * @return Item, e.g. 'REGION_TYPE_State'
   * @see #getRegionLevelItem(CharSequence)
   */
  String getRegionLevelFirstItem(CharSequence code) {
    return getRegionLevelItem(code, 0)
  }

  /**
   * Get region hierarchy level first item by level index
   * @param index {@link Integer}. Region level index, e.g. 0
   * @return Item, e.g. 'REGION_TYPE_State'
   * @see #getRegionLevelItem(Integer)
   */
  String getRegionLevelFirstItem(Integer index) {
    return getRegionLevelItem(index, 0)
  }

  /**
   * Get region hierarchy level key by item
   * @param code {@link CharSequence String}. Item code, e.g. 'REGION_TYPE_State'
   * @return Level key, e.g. 'state'
   * @see #getRegionHierarchy()
   */
  String getRegionLevelNameByItem(CharSequence code) {
    LinkedHashMap hierarchy = getRegionHierarchy()
    for (CharSequence name : keysList(hierarchy)) {
      List values = hierarchy[name]
      String realCode = code.toString()
      if (realCode in values) {
        return name
      }
    }
    return null
  }

  /**
   * Get building fields
   * @param buildingType {@link CharSequence String}. Custom building type code which will be used instead of 'REGION_TYPE_Building'
   * @return Building fields, e.g. {@code [building: 'зд.', home: 'д.', ...]}
   * @see #getBuildingTypeId(CharSequence)
   */
  Map getBuildingFields(CharSequence buildingType = null) {
    Map result = [
      building: getRefName(getBuildingTypeId(buildingType))
    ]
    BUILDING_FIELDS.each { CharSequence key, CharSequence value ->
      result[key] = getMessageNameByCode(value)
    }
    return result
  }

  /**
   * Get building field short name by field code
   * @param code         {@link CharSequence String}. Field code, e.g. 'building'
   * @param buildingType {@link CharSequence String}. Custom building type code which will be used instead of 'REGION_TYPE_Building'
   * @return Building field name, e.g. 'зд.'
   * @see #getBuildingFields(CharSequence)
   */
  String getBuildingField(CharSequence code, CharSequence buildingType = null) {
    return getBuildingFields(buildingType)[code]
  }

  /**
   * Get building field by position
   * @param position     {@link Integer}. Field position, e.g. 0
   * @param buildingType {@link CharSequence String}. Custom building type code which will be used instead of 'REGION_TYPE_Building'
   * @return Building field name, e.g. 'зд.'
   * @see #getBuildingFields(Integer)
   */
  String getBuildingField(Integer position, CharSequence buildingType = null) {
    return getBuildingFields(buildingType)[position]
  }

  /**
   * Get building field codes
   * @return ['building', 'home']
   * @see #getBuildingFields()
   */
  List getBuildingFieldNames() {
    return keysList(getBuildingFields())
  }

  /**
   * Get default building region type id
   * @see #getDefaultBuildingType()
   */
  Number getDefaultBuildingTypeId() {
    return getRefIdByCode(getDefaultBuildingType())
  }

  /**
   * Get building region type id by code
   * @param buildingType {@link CharSequence String}. Custom building type code which will be used instead of 'REGION_TYPE_Building'
   * @see #getDefaultBuildingTypeId()
   */
  Number getBuildingTypeId(CharSequence code = null) {
    if (!code) {
      return getDefaultBuildingTypeId()
    }
    return getRefIdByCode(code)
  }

  /**
   * Get default realty good
   */
  Map getDefaultRealtyGood() {
    return getGoodBy(code: DEFAULT_REALTY_GOOD_CODE)
  }

  /**
   * Get default realty good id
   */
  Number getDefaultRealtyGoodId() {
    return toIntSafe(getDefaultRealtyGood().n_good_id)
  }

  /**
   * Search for regions by different fields value
   * @param hierarchyTypeId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param hierarchyType   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parRegionId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param realtyGoodId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param regionId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param regionTypeId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param regionType      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param building        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ext             {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param construct       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param home            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ownership       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param zip             {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit           {@link Integer}. Optional, default: 0 (unlimited)
   * @param order           {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: [:]
   * @return List[Map] of region table rows
   */
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

  /**
   * Search for one region by different fields value
   * @param hierarchyTypeId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param hierarchyType   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param parRegionId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param realtyGoodId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param regionId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param regionTypeId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param regionType      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param building        {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ext             {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param construct       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param home            {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param ownership       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param zip             {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param order           {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional, default: [:]
   * @return Map with region table row
   */
  Map getRegionBy(Map input) {
    return getRegionsBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Get region by id
   * @param regionId {@link java.math.BigInteger BigInteger}
   * @return Map with region table row
   */
  Map getRegion(def regionId) {
    return getRegionBy(regionId: regionId)
  }

  /**
   * Get region tree
   * @param regionId {@link java.math.BigInteger BigInteger}
   * @return Map with region tree.
   * Region tree is a map with fields:
   * <pre>
   * {@code
   * [
   *   regionId      : 0, # region id fetched from db just to populate lower levels
   *   regionType    : REGION_TYPE_Building', # same but for region type code
   *   state         : 'Russia',
   *   stateType     : 'REGION_TYPE_State',
   *   oblast        : 'Moskowskaya',
   *   oblastType    : 'REGION_TYPE_Oblast',
   *   ...
   *   someField     : 'some value',
   *   someFieldType : 'REGION_TYPE_Some',
   *   ...
   *   building      : '1',
   *   home          : '2',
   *   construct     : '3',
   *   ...
   * ]
   * }
   * </pre>
  * So there are level name and also name + 'Type' which called typeName, and building type-specific fields
   */
  Map getRegionTree(def regionId) {
    String query = """
    SELECT 'vc_region_type', SI_REF_PKG_S.GET_CODE_BY_ID(N_REGION_TYPE_ID),"""
    List fields = hid.getTableColumns(getRegionsTable())
    fields.each { CharSequence field ->
      query += """
      '${field.toLowerCase()}', ${field},"""
    }

    List hierarchyFlatten = getRegionHierarchyWoBuildingFlatten()
    hierarchyFlatten.each { CharSequence code ->
      query += """
      '${code}', SR_REGIONS_PKG_S.GET_UPPER_REGION_CODE(N_REGION_ID, SI_REF_PKG_S.GET_ID_BY_CODE('${code}')),"""
    }

    query = query.replaceAll(/,*$/, '') + """
    FROM ${getRegionsTable()}
    WHERE N_REGION_ID = ${regionId}"""

    LinkedHashMap data = hid.queryFirst(query, true)
    LinkedHashMap result = [:]
    if (isEmpty(data)) {
      return result
    }

    hierarchyFlatten.each { CharSequence code ->
      if (notEmpty(data[code])) {
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

  /**
   * Get region fields with short name position indicator and value
   * @param input {@link LinkedHashMap Map} with region fields except building fields
   * @return List[List[fieldType, isAfter, value]], e.g. {@code [['state', 'N', ''], ..., ['street', 'N', 'ул.']]}
   */
  List getRegionItems(Map input) {
    List queryParts = []
    List typeNames = getRegionLevelTypeNamesWoBuilding()
    typeNames.eachWithIndex { CharSequence type, Integer i ->
      queryParts << """
      SELECT VC_VALUE, NVL(VC_VALUE_2,'N'), '${input[getRegionLevelName(i)] ?: ''}'
      FROM   ${getRefsTable()}
      WHERE  VC_CODE = '${input[type] ?: ""}'"""
    }

    String regionQuery = joinNonEmpty(queryParts, """
    UNION ALL""")
    return hid.queryDatabase(regionQuery)
  }

  /**
   * Create or update region
   * @param regionId     {@link java.math.BigInteger BigInteger}. Optional
   * @param regionTypeId {@link java.math.BigInteger BigInteger}. Optional
   * @param regionType   {@link CharSequence String}. Optional
   * @param realtyGoodId {@link java.math.BigInteger BigInteger}. Optional
   * @param parRegionId  {@link java.math.BigInteger BigInteger}. Optional
   * @param code         {@link CharSequence String}. Optional
   * @param name         {@link CharSequence String}. Optional
   * @param home         {@link CharSequence String}. Optional
   * @param building     {@link CharSequence String}. Optional
   * @param construct    {@link CharSequence String}. Optional
   * @param ownership    {@link CharSequence String}. Optional
   * @return Map with created region (in Oracle API procedure notation)
   */
  private Map putRegion(Map input) {
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

  /**
   * Create region
   * @param regionTypeId {@link java.math.BigInteger BigInteger}. Optional
   * @param regionType   {@link CharSequence String}. Optional
   * @param realtyGoodId {@link java.math.BigInteger BigInteger}. Optional
   * @param parRegionId  {@link java.math.BigInteger BigInteger}. Optional
   * @param code         {@link CharSequence String}. Optional
   * @param name         {@link CharSequence String}. Optional
   * @param home         {@link CharSequence String}. Optional
   * @param building     {@link CharSequence String}. Optional
   * @param construct    {@link CharSequence String}. Optional
   * @param ownership    {@link CharSequence String}. Optional
   * @return Map with created region (in Oracle API procedure notation)
   * @since 1.4
   */
  Map createRegion(Map input) {
    input.remove('regionId')
    return putRegion(input)
  }

  /**
   * Update region
   * @param regionId     {@link java.math.BigInteger BigInteger}
   * @param regionTypeId {@link java.math.BigInteger BigInteger}. Optional
   * @param regionType   {@link CharSequence String}. Optional
   * @param realtyGoodId {@link java.math.BigInteger BigInteger}. Optional
   * @param parRegionId  {@link java.math.BigInteger BigInteger}. Optional
   * @param code         {@link CharSequence String}. Optional
   * @param name         {@link CharSequence String}. Optional
   * @param home         {@link CharSequence String}. Optional
   * @param building     {@link CharSequence String}. Optional
   * @param construct    {@link CharSequence String}. Optional
   * @param ownership    {@link CharSequence String}. Optional
   * @return Map with created region (in Oracle API procedure notation)
   * @deprecated use {@link #updateRegion(Map, def)}
   */
  Map updateRegion(Map input) {
    return putRegion(input)
  }

  /**
   * Update region
   * @param regionId     {@link java.math.BigInteger BigInteger}
   * @param regionTypeId {@link java.math.BigInteger BigInteger}. Optional
   * @param regionType   {@link CharSequence String}. Optional
   * @param realtyGoodId {@link java.math.BigInteger BigInteger}. Optional
   * @param parRegionId  {@link java.math.BigInteger BigInteger}. Optional
   * @param code         {@link CharSequence String}. Optional
   * @param name         {@link CharSequence String}. Optional
   * @param home         {@link CharSequence String}. Optional
   * @param building     {@link CharSequence String}. Optional
   * @param construct    {@link CharSequence String}. Optional
   * @param ownership    {@link CharSequence String}. Optional
   * @return Map with created region (in Oracle API procedure notation)
   * @since 1.4
   */
  Map updateRegion(Map input = [:], def regionId) {
    return updateRegion(input + [regionId: regionId])
  }

  /**
   * Check if region is empty
   * @param input {@link LinkedHashMap Map} with region fields
   * @return True if region fields are null or empty, false otherwise
   * @see #getRegionTree(def)
   */
  Boolean isRegionEmpty(Map input) {
    Boolean result = true
    List levelNames = getRegionLevelNames()

    (['regionId'] + levelNames).each { CharSequence name ->
      if (notEmpty(input[name])) {
        result = false
      }
    }
    return result
  }

  /**
   * Check if region is not empty
   * @see #isRegionEmpty(Map)
   */
  Boolean notRegionEmpty(Map input) {
    return !isRegionEmpty(input)
  }

  /**
   * Create region tree
   * @param input {@link LinkedHashMap Map} with region fields
   * @return Region id of last created (or existing) child region in the tree
   * @see #getRegionTree(def)
   */
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
          region = putRegion([
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

    Map buildingFields = [:]
    getBuildingFieldNames().each { CharSequence field ->
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