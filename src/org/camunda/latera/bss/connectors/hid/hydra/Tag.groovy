package org.camunda.latera.bss.connectors.hid.hydra

import static org.camunda.latera.bss.utils.Oracle.encodeBool
import static org.camunda.latera.bss.utils.StringUtil.snakeCase

trait Tag {
  private static String TAGS_TABLE        = 'SI_V_TAGS'
  private static String ENTITY_TAGS_TABLE = 'SI_V_ENTITIES_TAGS'

  String getTagsTable() {
    return TAGS_TABLE
  }

  String getEntityTagsTable() {
    return ENTITY_TAGS_TABLE
  }

  String tagify(CharSequence value) {
    return snakeCase(value)
  }

  List getTagsBy(Map input) {
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

  Map getTagBy(Map input) {
    return getTagsBy(input + [limit: 1])?.getAt(0)
  }

  Map getTagByCode(CharSequence code) {
    return getTagBy(code: code)
  }

  def getTagIdByCode(CharSequence code) {
    return getTagByCode(code)?.n_tag_id
  }

  Map getTag(def tagId) {
    LinkedHashMap where = [
      n_tag_id: tagId
    ]
    return hid.getTableFirst(getTagsTable(), where: where)
  }

  List getEntitiyTagsBy(Map input) {
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

  Map getEntityTagBy(Map input) {
    return getEntitiesTagsBy(input + [limit: 1])?.getAt(0)
  }

  Map getEntityTag(def entityTagId) {
    LinkedHashMap where = [
      n_entity_tag_id: entityTagId
    ]
    return hid.getTableFirst(getEntityTagsTable(), where: where)
  }

  Map putEntityTag(Map input) {
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
    LinkedHashMap params = mergeParams(defaultParams, input)

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

  Map addEntityTag(Map input) {
    return putEntityTag(input)
  }

  Map addEntityTag(def entityId, CharSequence tag) {
    return addEntityTag(entityId: entityId, tag: tag)
  }

  Map addEntityTag(Map input = [:], def entityId) {
    return addEntityTag(input + [entityId: entityId])
  }

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

  Boolean deleteEntityTag(Map input) {
    def entityTagId = getEntityTag(input)?.n_entity_tag_id
    return deleteEntityTag(entityTagId)
  }

  Boolean deleteEntityTag(def entityId, CharSequence tag) {
    return deleteEntityTag(entityId: entityId, tag: tag)
  }
}