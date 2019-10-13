package org.camunda.latera.bss.connectors.otrs.types

import org.camunda.latera.bss.utils.Base64Converter

trait Ticket {
  private String TICKET_ENTITY_TYPE = 'Ticket'

  private String getTicketEntityType() {
    return TICKET_ENTITY_TYPE
  }

  private Map getTicketDefaultParams() {
    return [
      title         : null,
      queueId       : null,
      lockId        : null,
      typeId        : null,
      serviceId     : null,
      slaId         : null,
      stateId       : null,
      priorityId    : null,
      ownerId       : null,
      responsibleId : null,
      pendingTime   : null
    ]
  }

  private Map getTicketParamsMap(Map params) {
    return [
      Title         : params.title,
      QueueID       : params.queueId,
      Queue         : params.queue,
      LockID        : params.lockId,
      Lock          : params.lock,
      TypeID        : params.typeId,
      Type          : params.type,
      ServiceID     : params.serviceId,
      Service       : params.service,
      SLAID         : params.slaId,
      SLA           : params.sla,
      StateID       : params.stateId,
      State         : params.state,
      PriorityID    : params.priorityId,
      Priority      : params.priority,
      OwnerID       : params.ownerId,
      Owner         : params.owner,
      ResponsibleID : params.responsibleId,
      Responsible   : params.responsible,
      PendingTime   : params.pendingTime
    ]
  }

  private Map getTicketArticleDefaultParams() {
    return [
      communicationChannelId           : null,
      isVisibleForCustomer             : null,
      senderTypeId                     : null,
      autoResponseType                 : null,
      from                             : null,
      subject                          : null,
      body                             : null,
      bodyContentType                  : 'text/plain; charset=utf8',
      bodyCharset                      : null,
      bodyMimeType                     : null,
      noAgentNotify                    : null,
      forceNotificationToUserIds       : null,
      excludeNotificationToUserIds     : null,
      excludeMuteNotificationToUserIds : null
    ]
  }

  private Map getTicketArticleParamsMap(Map params) {
    return [
      CommunicationChannelID          : params.communicationChannelId,
      CommunicationChannel            : params.communicationChannel,
      IsVisibleForCustomer            : params.isVisibleForCustomer,
      SenderTypeID                    : params.senderTypeId,
      SenderType                      : params.senderType,
      AutoResponseType                : params.autoResponseType,
      From                            : params.from,
      Subject                         : params.subject,
      Body                            : params.body,
      ContentType                     : params.bodyContentType,
      Charset                         : params.bodyCharset,
      MimeType                        : params.bodyMimeType,
      NoAgentNotify                   : params.noAgentNotify,
      ForceNotificationToUserID       : params.forceNotificationToUserIds,
      ExcludeNotificationToUserID     : params.excludeNotificationToUserIds,
      ExcludeMuteNotificationToUserID : params.excludeMuteNotificationToUserIds
    ]
  }

  private Map getTicketDynamicFieldDefaultParams() {
    return [
      name  : null,
      value : null
    ]
  }

  private Map getTicketDynamicFieldParamsMap(Map params) {
    return [
      Name  : params.name,
      Value : params.value
    ]
  }

  private Map getTicketAttachmentDefaultParams() {
    return [
      name        : '',
      content     : [] as byte[],
      contentType : 'application/octet-stream'
    ]
  }

  private Map getTicketAttachmentParamsMap(Map params) {
    return [
      Content     : params.content ? Base64Converter.to(params.content) : '',
      ContentType : params.contentType,
      Filename    : params.name
    ]
  }

  private Map getTicketParams(Map input, List attachments = [], Map dynamicFields = [:], Map additionalParams = [:]) {
    LinkedHashMap ticketParams  = getTicketDefaultParams() + input
    LinkedHashMap articleParams = getTicketArticleDefaultParams() + input
    LinkedHashMap ticket  = convertParams(prepareParams(getTicketParamsMap(ticketParams)) + convertKeys(additionalParams))
    LinkedHashMap article = convertParams(prepareParams(getTicketArticleParamsMap(articleParams)))

    List attachmentList = []
    if (attachments) {
      attachments.each { it ->
        LinkedHashMap attachment = getTicketAttachmentDefaultParams() + it
        attachmentList += getTicketAttachmentParamsMap(attachment)
      }
    }

    List dynamicFieldsList = []
    if (dynamicFields) {
      dynamicFields.each { key, value ->
        LinkedHashMap field = getTicketDynamicFieldDefaultParams() + [
          name  : key,
          value : value
        ]
        dynamicFieldsList += getTicketDynamicFieldParamsMap(field)
      }
    }

    LinkedHashMap result = [:]
    if (ticket) {
      result.Ticket = ticket
    }
    if (article) {
      result.Article = article
    }
    if (attachmentList) {
      result.Attachment = attachmentList
    }
    if (dynamicFieldsList) {
      result.DynamicField = dynamicFieldsList
    }
    return result
  }

  Map getTicket(def id) {
    return getEntity(getTicketEntityType(), id)
  }

  Map createTicket(Map input, List attachments = [], Map dynamicFields = [:], Map additionalParams = [:]) {
    LinkedHashMap params = getTicketParams(input, attachments, dynamicFields, additionalParams)
    return createEntity(getTicketEntityType(), params)
  }

  Map updateTicket(def id, Map input, List attachments = [], Map dynamicFields = [:], Map additionalParams = [:]) {
    LinkedHashMap params = getTicketParams(input, attachments, dynamicFields, additionalParams)
    return updateEntity(getTicketEntityType(), id, params)
  }

  Map updateTicket(Map input, List attachments = [], Map dynamicFields = [:], Map additionalParams = [:]) {
    def id = input.id ?: input.ticketId
    input.remove('id')
    input.remove('ticketId')
    return updateTicket(id, input, attachments, dynamicFields, additionalParams)
  }

  Map updateTicket(Map input, def id, List attachments = [], Map dynamicFields = [:], Map additionalParams = [:]) {
    return updateTicket(id, input, attachments, dynamicFields, additionalParams)
  }

  Map addTicketFile(def id, Map file) {
    return addTicketFiles(id, [file])
  }

  Map addTicketFile(Map file, def id) {
    return addTicketFile(id, file)
  }

  Map addTicketFile(Map input) {
    def id = input.id ?: input.ticketId
    input.remove('id')
    input.remove('ticketId')
    return addTicketFile(id, input)
  }

  Map addTicketFile(def id, CharSequence name, byte[] content) {
    return addTicketFile(id, name: name, content: content)
  }

  Map addTicketFiles(def id, List attachments) {
    return updateTicket(id, [:], attachments)
  }

  Map updateTicketArticle(def id, Map input) {
    return updateTicket(id, input)
  }

  Map updateTicketArticle(Map input) {
    def id = input.id ?: input.ticketId
    input.remove('id')
    input.remove('ticketId')
    return updateTicketArticle(id, input)
  }

  Map updateTicketArticle(Map input, def id) {
    return updateTicketArticle(id, input)
  }

  Map updateTicketDynamicField(def id, CharSequence name, def value = null) {
    List dynamicFields = [["${name}": value]]
    return updateTicket(id, [:], [], dynamicFields)
  }

  Map updateTicketDynamicField(Map input) {
    def id = input.id ?: input.ticketId
    input.remove('id')
    input.remove('ticketId')
    return updateTicketDynamicField(id, input)
  }

  Map updateTicketDynamicField(Map input, def id) {
    updateTicketDynamicField(id, input.name, input.value)
  }

  Map updateTicketDynamicFields(Map input, def id) {
    input.each { key, value ->
      updateTicketDynamicField(id, key, value)
    }
  }

  Map updateTicketDynamicFields(Map input) {
    def id = input.id ?: input.ticketId
    input.remove('id')
    input.remove('ticketId')
    return updateTicketDynamicFields(id, input)
  }

  Boolean deleteTicket(def id) {
    return deleteEntity(getTicketEntityType(), id)
  }
}