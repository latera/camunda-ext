package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.internal.MessageCache

trait Message {
  private static String MESSAGES_TABLE = 'SS_MESSAGES'

  String getMessagesTable() {
    return MESSAGES_TABLE
  }

  private String messageCodeWithLang(String code, Number langId = null) {
    return "${code}|${langId ?: getLangId()}"
  }

  private String messageCodeWoLang(String code) {
    return code.replaceAll(/^(.*)[|].*$/, '$1')
  }

  Map getMessage(def messageId) {
    LinkedHashMap where = [
      n_message_id: messageId
    ]
    return hid.getTableFirst(getMessagesTable(), where: where)
  }

  List getMessagesBy(Map input) {
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

  Map getMessageBy(Map input) {
    return getMessagesBy(input + [limit: 1])?.getAt(0)
  }

  Map getMessageByCode(CharSequence code) {
    return getMessageBy(code: code)
  }

  Map getMessageByName(CharSequence name) {
    return getMessageBy(name: name)
  }

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