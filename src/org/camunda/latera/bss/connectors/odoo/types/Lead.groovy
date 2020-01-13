package org.camunda.latera.bss.connectors.odoo.types

trait Lead {
  private static String LEAD_ENTITY_TYPE = 'crm.lead'

  private String getLeadEntityType() {
    return LEAD_ENTITY_TYPE
  }

  private Map getLeadDefaultParams() {
    return [
      name           : null,
      email          : null,
      emailCC        : null,
      companyName    : null,
      customerId     : null,
      partnerName    : null,
      organizationId : null,
      userId         : null,
      sourceId       : null,
      teamId         : null,
      campaignId     : null,
      stageId        : null,
      type           : null,
      phoneNumber    : null,
      countryId      : null,
      stateId        : null,
      city           : null,
      street         : null,
      street2        : null,
      zip            : null,
      comment        : null,
      active         : true
    ]
  }

  Map getLeadParamsMap(Map params) {
    return [
      contact_name : params.name,
      email_from   : params.email,
      email_cc     : params.emailCC,
      name         : params.companyName ?: params.name,
      partner_id   : params.customerId,
      partner_name : params.companyName ?: params.partnerName,
      company_id   : params.organizationId,
      user_id      : params.userId,
      source_id    : params.sourceId,
      team_id      : params.teamId,
      campaign_id  : params.campaignId,
      stage_id     : params.stageId,
      type         : params.type,
      phone        : params.phoneNumber,
      country_id   : params.countryId,
      state_id     : params.stateId,
      city         : params.city,
      street       : params.street,
      street2      : params.street2,
      zip          : params.zip,
      description  : params.comment,
      active       : params.active
    ]
  }

  private Map getLeadParams(Map input, Map additionalParams = [:]) {
    LinkedHashMap params = getLeadDefaultParams() + input
    return prepareParams(this.&getLeadParamsMap, params, additionalParams)
  }

  Map getLead(def id) {
    return getEntity(getLeadEntityType(), id)
  }

  List getLeadsBy(Map input, Map additionalParams = [:]) {
    LinkedHashMap params = getLeadParams(input, additionalParams)
    return getEntitiesBy(getLeadEntityType(), params)
  }

  Map getLeadBy(Map input, Map additionalParams = [:]) {
    return getLeadsBy(input, additionalParams)?.getAt(0)
  }

  Map createLead(Map input, Map additionalParams = [:]) {
    LinkedHashMap params = getLeadParams(input, additionalParams)
    return createEntity(getLeadEntityType(), params)
  }

  Map updateLead(Map input = [:], def id, Map additionalParams = [:]) {
    LinkedHashMap params = getLeadParams(input, additionalParams)
    return updateEntity(getLeadEntityType(), id, params)
  }

  Boolean deleteLead(def id) {
    return deleteEntity(getLeadEntityType(), id)
  }
}