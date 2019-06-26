package org.camunda.latera.bss.connectors.otrs.types

import org.camunda.latera.bss.utils.Base64Converter

trait Ticket {
  private static String TICKET_ENTITY_TYPE = 'Ticket'

  def getTicketEntityType() {
    return TICKET_ENTITY_TYPE
  }

  LinkedHashMap getTicketDefaultParams() {
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

  LinkedHashMap getTicketParamsMap(LinkedHashMap params) {
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

  LinkedHashMap getTicketArticleDefaultParams() {
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

  LinkedHashMap getTicketArticleParamsMap(LinkedHashMap params) {
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

  LinkedHashMap getTicketDynamicFieldDefaultParams() {
    return [
      name  : null,
      value : null
    ]
  }

  LinkedHashMap getTicketDynamicFieldParamsMap(LinkedHashMap params) {
    return [
      Name  : params.name,
      Value : params.value
    ]
  }

  LinkedHashMap getTicketAttachmentDefaultParams() {
    return [
      name        : '',
      content     : [] as byte[],
      contentType : 'application/octet-stream'
    ]
  }

  LinkedHashMap getTicketAttachmentParamsMap(LinkedHashMap params) {
    return [
      Content     : params.content ? Base64Converter.to(params.content) : '',
      ContentType : params.contentType,
      Filename    : params.name
    ]
  }

  LinkedHashMap getTicketParams(LinkedHashMap input, List attachments = [], LinkedHashMap dynamicFields = [:], LinkedHashMap additionalParams = [:]) {
    def ticketParams  = getTicketDefaultParams() + input
    def articleParams = getTicketArticleDefaultParams() + input
    def ticket  = convertParams(prepareParams(getTicketParamsMap(ticketParams)) + convertKeys(additionalParams))
    def article = convertParams(prepareParams(getTicketArticleParamsMap(articleParams)))

    List attachmentList = []
    if (attachments) {
      attachments.each { it ->
        def attachment = getTicketAttachmentDefaultParams() + it
        attachmentList += getTicketAttachmentParamsMap(attachment)
      }
    }

    List dynamicFieldsList = []
    if (dynamicFields) {
      dynamicFields.each { key, value ->
        def field = getTicketDynamicFieldDefaultParams() + [
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

  LinkedHashMap getTicket(def id) {
    return getEntity(getTicketEntityType(), id)
  }

  LinkedHashMap createTicket(LinkedHashMap input, List attachments = [], LinkedHashMap dynamicFields = [:], LinkedHashMap additionalParams = [:]) {
    LinkedHashMap params = getTicketParams(input, attachments, dynamicFields, additionalParams)
    return createEntity(getTicketEntityType(), params)
  }

  LinkedHashMap updateTicket(def id, LinkedHashMap input, List attachments = [], LinkedHashMap dynamicFields = [:], LinkedHashMap additionalParams = [:]) {
    LinkedHashMap params = getTicketParams(input, attachments, dynamicFields, additionalParams)
    return updateEntity(getTicketEntityType(), id, params)
  }

  LinkedHashMap updateTicket(LinkedHashMap input, def id, List attachments = [], LinkedHashMap dynamicFields = [:], LinkedHashMap additionalParams = [:]) {
    return updateTicket(id, input, attachments, dynamicFields, additionalParams)
  }

  LinkedHashMap addTicketFile(def id, LinkedHashMap file) {
    return addTicketFiles(id, [file])
  }

  LinkedHashMap addTicketFile(LinkedHashMap file, def id) {
    return addTicketFile(id, file)
  }

  LinkedHashMap addTicketFile(def id, String name, byte[] content) {
    return addTicketFile(id, name: name, content: content)
  }

  LinkedHashMap addTicketFiles(def id, List attachments) {
    return updateTicket(id, [:], attachments)
  }

  LinkedHashMap updateTicketArticle(def id, LinkedHashMap input) {
    return updateTicket(id, input)
  }

  LinkedHashMap updateTicketArticle(LinkedHashMap input, def id) {
    return updateTicketArticle(id, input)
  }

  LinkedHashMap updateTicketDynamicField(def id, def name, def value = null) {
    def dynamicFields = [[name: value]]
    return updateTicket(id, [:], [], dynamicFields)
  }

  LinkedHashMap updateTicketDynamicField(LinkedHashMap input, def id) {
    updateTicketDynamicField(id, input.name, input.value)
  }

  LinkedHashMap updateTicketDynamicFields(LinkedHashMap input, def id) {
    input.each { key, value ->
      updateTicketDynamicField(id, key, value)
    }
  }

  Boolean deleteTicket(def id) {
    return deleteEntity(getTicketEntityType(), id)
  }
}