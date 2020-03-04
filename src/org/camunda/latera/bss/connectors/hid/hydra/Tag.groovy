package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Oracle.encodeBool
import static org.camunda.latera.bss.utils.StringUtil.snakeCase
import static org.camunda.latera.bss.utils.StringUtil.trim
import static org.camunda.latera.bss.utils.MapUtil.isMap

trait Tag {
  private static String TAGS_TABLE        = 'SI_V_TAGS'
  private static String ENTITY_TAGS_TABLE = 'SI_V_ENTITIES_TAGS'

  /**
   * Get tags table name
   */
  String getTagsTable() {
    return TAGS_TABLE
  }

  /**
   * Get entity tags table name
   */
  String getEntityTagsTable() {
    return ENTITY_TAGS_TABLE
  }

  /**
   * Convert input to tag
   * @param value {@link CharSequence String}
   * @return Value converted with removed spaces and converted to snake case
   */
  String tagify(CharSequence value) {
    return snakeCase(value)
  }

  /**
   * Search for tags by different fields value
   * @param tagId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isEditable {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit       {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order       {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional
   * @return Tag table rows
   */
  List<Map> getTagsBy(Map input) {
    LinkedHashMap params = mergeParams([
      tagId       : null,
      code        : null,
      isEditable  : null,
      limit       : 0
    ], input)
    LinkedHashMap where = [:]

    if (params.tagId) {
      where.n_tag_id = params.tagId
    }
    if (params.code) {
      where.vc_code = params.code
    }
    if (params.isEditable != null) {
      where.c_fl_editable = encodeBool(params.isEditable)
    }
    return hid.getTableData(getTagsTable(), where: where, order: params.order, limit: params.limit)
  }

  /**
   * Search for tag by different fields value
   * @param tagId      {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code       {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isEditable {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit      {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order      {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional
   * @return tag table row
   */
  Map getTagBy(Map input) {
    return getTagsBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Search for tag by code
   * @param code {@link CharSequence String}
   * @return tag table row
   */
  Map getTagByCode(CharSequence code) {
    return getTagBy(code: code)
  }

  /**
   * Search for tag id by code
   * @param code {@link CharSequence String}
   * @return Tag id
   */
  def getTagIdByCode(CharSequence code) {
    return getTagByCode(code)?.n_tag_id
  }

  /**
   * Get tag by id
   * @param tagId {@link java.math.BigInteger BigInteger}
   * @return tag table row
   */
  Map getTag(def tagId) {
    LinkedHashMap where = [
      n_tag_id: tagId
    ]
    return hid.getTableFirst(getTagsTable(), where: where)
  }

  /**
   * Prepare inner query to search for entities by tag
   * @param entityPrimaryKey {@link CharSequence String}. Primary key of entity table to join, e.g. n_subject_id
   * @param where {@link CharSequence String} or {@link LinkedHashMap Map} with WHERE for vc_code tag table column, e.g. {@code [in: ['some_tag']]}
   * @return WHERE clause which can be used in getEntitiesBy and getEntityBy methods calls
   */
  Map prepareEntityTagQuery(CharSequence entityPrimaryKey, def where) {
    LinkedHashMap parentWhere = [:]
    // tags: 'some_tag'               -> AND     EXIST (SELECT 1 FROM SI_V_TAGS TT WHERE T.N_SUBJECT_ID = TT.N_ENTITY_ID AND TT.VC_CODE =     '123')
    // tags: ['!=': 'some_tag']       -> AND NOT EXIST (SELECT 1 FROM SI_V_TAGS TT WHERE T.N_SUBJECT_ID = TT.N_ENTITY_ID AND TT.VC_CODE =     '123')
    // tags: [in: ['some_tag']]       -> AND     EXIST (SELECT 1 FROM SI_V_TAGS TT WHERE T.N_SUBJECT_ID = TT.N_ENTITY_ID AND TT.VC_CODE IN   ('123'))
    // tags: ['not in': ['some_tag']] -> AND NOT EXIST (SELECT 1 FROM SI_V_TAGS TT WHERE T.N_SUBJECT_ID = TT.N_ENTITY_ID AND TT.VC_CODE IN   ('123'))
    // tags: [like: '%tag']           -> AND     EXIST (SELECT 1 FROM SI_V_TAGS TT WHERE T.N_SUBJECT_ID = TT.N_ENTITY_ID AND TT.VC_CODE LIKE '%123')
    // tags: ['not like': '%tag']     -> AND NOT EXIST (SELECT 1 FROM SI_V_TAGS TT WHERE T.N_SUBJECT_ID = TT.N_ENTITY_ID AND TT.VC_CODE LIKE '%123')
    LinkedHashMap tagsWhere   = [:]
    LinkedHashMap noTagsWhere = [:]
    if (isMap(where)) {
      where.each { CharSequence key, def value ->
        if (key == '=') {
          tagsWhere.vc_code = value
        } else if (key == '!=') {
          noTagsWhere.vc_code = value
        } else if ('not' in key || '!' in key) {
          String keyWithoutNo = trim(key.replace('not', '').replace('!', ''))
          noTagsWhere.vc_code = ["${keyWithoutNo}": value]
        } else {
          tagsWhere.vc_code = ["${key}": value]
        }
      }
    } else {
      tagsWhere.vc_code = where
    }
    if (notEmpty(tagsWhere)) {
      tagsWhere["T.${entityPrimaryKey}"] = 'TT.N_ENTITY_ID'
      parentWhere['EXIST'] = hid.prepareTableQuery(getEntityTagsTable(), fields: ['1'], where: tagsWhere, tableAlias: 'TT', asMap: false)
    }
    if (notEmpty(noTagsWhere)) {
      noTagsWhere["T.${entityPrimaryKey}"] = 'TT.N_ENTITY_ID'
      parentWhere['NOT EXIST'] = hid.prepareTableQuery(getEntityTagsTable(), fields: ['1'], where: noTagsWhere, tableAlias: 'TT', asMap: false)
    }
    return parentWhere
  }

  /**
   * Search for entity tags by different fields value
   * @param entityTagId  {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param tagId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param entityId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param entityTypeId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param entityType   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lineNumber   {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isEditable   {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit        {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order        {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional
   * @return Entity tag table rows
   */
  List<Map> getEntitiyTagsBy(Map input) {
    LinkedHashMap defaultParams = [
      entityTagId  : null,
      tagId        : null,
      code         : null,
      entityId     : null,
      entityTypeId : null,
      lineNumber   : null,
      isEditable   : null,
      limit        : 0
    ]
    if (input.tag) {
      input.code = input.tag
      input.remove('tag')
    }
    LinkedHashMap params = mergeParams(defaultParams, input)
    LinkedHashMap where = [:]

    if (params.entityTagId) {
      where.n_entity_tag_id = params.entityTagId
    }
    if (params.tagId) {
      where.n_tag_id = params.tagId
    }
    if (params.code) {
      where.vc_code = params.code
    }
    if (params.entityId) {
      where.n_entity_id = params.entityId
    }
    if (params.entityTypeId) {
      where.n_entity_type_id = params.entityTypeId
    }
    if (params.lineNumber) {
      where.n_line_no = params.lineNumber
    }
    if (params.isEditable != null) {
      where.c_fl_editable = encodeBool(params.isEditable)
    }
    return hid.getTableData(getEntityTagsTable(), where: where, order: params.order, limit: params.limit)
  }

  /**
   * Search for entity tag by different fields value
   * @param entityTagId  {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param tagId        {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code         {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param entityId     {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param entityTypeId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param entityType   {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param lineNumber   {@link Integer}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param isEditable   {@link Boolean}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param order        {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional
   * @return Entity tag table row
   */
  Map getEntityTagBy(Map input) {
    return getEntitiyTagsBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Get entity tag by id
   * @param entityTagId {@link java.math.BigInteger BigInteger}
   * @return Entity tag table row
   */
  Map getEntityTag(def entityTagId) {
    LinkedHashMap where = [
      n_entity_tag_id: entityTagId
    ]
    return hid.getTableFirst(getEntityTagsTable(), where: where)
  }

  /**
   * Create or update entity tag
   * @param entityTagId  {@link java.math.BigInteger BigInteger}. Optional
   * @param tagId        {@link java.math.BigInteger BigInteger}. Optional if 'tag' is pased
   * @param tag          {@link CharSequence String}.  Optional if 'tagId' is pased
   * @param code         Alias for 'tag'
   * @param entityId     {@link java.math.BigInteger BigInteger}. Optional
   * @param entityTypeId {@link java.math.BigInteger BigInteger}. Optional if 'entityType' is pased
   * @param entityType   {@link CharSequence String}. Optional if 'entityTypeId' is pased
   * @param lineNumber   {@link Integer}. Optional
   * @return Created or updated entity tag (in Oracle API procedure notation)
   */
  private Map putEntityTag(Map input) {
    LinkedHashMap defaultParams = [
      entityTagId  : null,
      tagId        : null,
      entityId     : null,
      entityTypeId : null,
      lineNumber   : null
    ]

    if (!input.tagId && (input.tag ?: input.code)) {
      input.tagId = getTagIdByCode(input.tag ?: input.code)
      input.remove('tag')
      input.remove('code')
    }

    if (!input.entityTypeId && input.entityId) {
      if (isSubject(input.entityId)) {
        input.entityTypeId = getSubjectEntityTypeId()
      } else if (isDocument(input.entityId)) {
        input.entityTypeId = getDocumentEntityTypeId()
      } else if (isGood(input.entityId)) {
        input.entityTypeId = getGoodEntityTypeId()
      } else if (isObject(input.entityId)) {
        input.entityTypeId = getObjectEntityTypeId()
      }
    }
    LinkedHashMap params = mergeParams(defaultParams, input)

    try {
      logger.info("Tagging entity with params ${params}")
      LinkedHashMap result = hid.execute('SI_TAGS_PKG.SI_ENTITIES_TAGS_PUT', [
        num_N_ENTITY_TAG_ID  : params.entityTagId,
        num_N_TAG_ID         : params.tagId,
        num_N_ENTITY_ID      : params.entityId,
        num_N_ENTITY_TYPE_ID : params.entityTypeId,
        num_N_LINE_NO        : params.lineNumber
      ])
      logger.info("   Entity was tagged successfully!")
      return result
    } catch (Exception e){
      logger.error("   Error while tagging entity!")
      logger.error_oracle(e)
      return null
    }
  }

  /**
   * Add tag to entity
   * @param entityId     {@link java.math.BigInteger BigInteger}
   * @param tagId        {@link java.math.BigInteger BigInteger}. Optional if 'tag' is pased
   * @param tag          {@link CharSequence String}. Optional if 'tagId' is pased
   * @param code         Alias for 'tag'
   * @param entityTypeId {@link java.math.BigInteger BigInteger}. Optional
   * @param entityType   {@link CharSequence String}. Optional
   * @param lineNumber   {@link Integer}. Optional
   * @return Created entity tag (in Oracle API procedure notation)
   */
  Map addEntityTag(Map input) {
    return putEntityTag(input)
  }

  /**
   *  Add tag to entity
   *
   * Overload with tag code instead of id
   * @param entityId {@link java.math.BigInteger BigInteger}
   * @param tag      {@link CharSequence String}
   * @see #addEntityTag(Map)
   */
  Map addEntityTag(def entityId, CharSequence tag) {
    return addEntityTag(entityId: entityId, tag: tag)
  }


  /**
   *  Add tag to entity
   *
   * Overload with mandatory entity id arg
   * @param entityId {@link java.math.BigInteger BigInteger}
   * @param tagId {@link java.math.BigInteger BigInteger}. Optional if 'tag' is pased
   * @param tag   {@link CharSequence String}. Optional if 'tagId' is pased
   * @see #addEntityTag(Map)
   */
  Map addEntityTag(Map input = [:], def entityId) {
    return addEntityTag(input + [entityId: entityId])
  }

  /**
   * Delete tag from entity
   * @param entityTagId {@link java.math.BigInteger BigInteger}
   * @return True if entity tag was deleted successfully, false otherwise
   */
  Boolean deleteEntityTag(def entityTagId) {
    try {
      logger.info("Deleting entity tag ${entityTagId}")
      hid.execute('SI_TAGS_PKG.SI_ENTITIES_TAGS_DEL', [
        num_N_ENTITY_TAG_ID : entityTagId
      ])
      logger.info("   Entity tag was deleted successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while deleting entity tag!")
      logger.error_oracle(e)
      return false
    }
  }

  /**
   * Delete tag from entity
   *
   * Overload for names arguments input
   * @param entityId {@link java.math.BigInteger BigInteger}
   * @param tagId    {@link java.math.BigInteger BigInteger}. Optional if 'tag' is pased
   * @param tag      {@link CharSequence String}. Optional if 'tagId' is pased
   * @see @deleteEntityTag(def)
   */
  Boolean deleteEntityTag(Map input) {
    def entityTagId = getEntityTag(input)?.n_entity_tag_id
    return deleteEntityTag(entityTagId)
  }


  /**
   * Delete tag from entity
   *
   * Overload with eneity id and tag code
   * @param entityId {@link java.math.BigInteger BigInteger}
   * @param tag      {@link CharSequence String}
   * @see @deleteEntityTag(Map)
   */
  Boolean deleteEntityTag(def entityId, CharSequence tag) {
    return deleteEntityTag(entityId: entityId, tag: tag)
  }
}