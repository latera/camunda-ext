package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.internal.MessageCache

/**
  * System messages specific methods
  */
trait Message {
  private static String MESSAGES_TABLE = 'SS_MESSAGES'

  /**
   * Get messages table name
   */
  String getMessagesTable() {
    return MESSAGES_TABLE
  }

  /**
   * Get messages code with lang to use in cache
   */
  private String messageCodeWithLang(String code, Number langId = null) {
    return "${code}|${langId ?: getLangId()}"
  }

  /**
   * Get messages code without lang to use in cache
   */
  private String messageCodeWoLang(String code) {
    return code.replaceAll(/^(.*)[|].*$/, '$1')
  }

  /**
   * Get message by id
   * @param messageId {@link java.math.BigInteger BigInteger}
   * @return Message table row
   */
  Map getMessage(def messageId) {
    LinkedHashMap where = [
      n_message_id: messageId
    ]
    return hid.getTableFirst(getMessagesTable(), where: where)
  }

  /**
   * Search for messages by different fields value
   * @param messageId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param langId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. Default: current lang
   * @param lang      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit     {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order     {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional
   * @return Message table rows
   */
  List<Map> getMessagesBy(Map input) {
    LinkedHashMap params = mergeParams([
      messageId : null,
      name      : null,
      code      : null,
      langId    : getLangId(),
      limit     : 0
    ], input)
    LinkedHashMap where = [:]

    if (params.messageId) {
      where.n_message_id = params.messageId
    }
    if (params.name) {
      where.vc_name = params.name
    }
    if (params.code) {
      where.vc_code = params.code
    }
    if (params.langId) {
      where.n_lang_id = params.langId
    }

    List result = hid.getTableData(getMessagesTable(), where: where, order: params.order, limit: params.limit)
    if (result) {
      result.each { Map message ->
        MessageCache.instance.put(messageCodeWithLang(message.vc_code, message.n_lang_id), message.vc_name)
      }
    }
    return result
  }

  /**
   * Search for message by different fields value
   * @param messageId {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param code      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param name      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param langId    {@link java.math.BigInteger BigInteger}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional. Default: current lang
   * @param lang      {@link CharSequence String}, {@link LinkedHashMap Map} with WHERE clause or SELECT query. Optional
   * @param limit     {@link Integer}. Optional. Default: 0 (unlimited)
   * @param order     {@link LinkedHashMap Map} or {@link List} with ORDER clause. Optional
   * @return Message table row
   */
  Map getMessageBy(Map input) {
    return getMessagesBy(input + [limit: 1])?.getAt(0)
  }

  /**
   * Get message by code
   * @param code {@link CharSequence String}
   * @return Message table row
   */
  Map getMessageByCode(CharSequence code) {
    return getMessageBy(code: code)
  }

  /**
   * Get message by name
   * @param name {@link CharSequence String}
   * @return Message table row
   */
  Map getMessageByName(CharSequence name) {
    return getMessageBy(name: name)
  }

  /**
   * Get message name by code
   * @param code {@link CharSequence String}
   * @return Message name
   */
  String getMessageNameByCode(CharSequence code) {
    Number langId = getLangId()
    String name = MessageCache.instance.get(messageCodeWithLang(code, langId))
    if (name) {
      return name
    }

    LinkedHashMap where = [
      vc_code   : code,
      n_lang_id : langId
    ]
    name = hid.getTableFirst(getMessagesTable(), 'vc_name', where)
    return MessageCache.instance.putAndGet(messageCodeWithLang(code, langId), name)
  }

  /**
   * Get message code by name
   * @param name {@link CharSequence String}
   * @return Message code
   */
  String getMessageCodeByName(CharSequence name) {
    String code = MessageCache.instance.getKey(name)
    if (code) {
      return messageCodeWoLang(code)
    }

    Number langId = getLangId()
    LinkedHashMap where = [
      vc_name   : name,
      n_lang_id : langId
    ]
    code = hid.getTableFirst(getMessagesTable(), 'vc_code', where).toString()
    code = MessageCache.instance.putAndGetKey(messageCodeWithLang(code, langId), name)
    return messageCodeWoLang(code)
  }
}