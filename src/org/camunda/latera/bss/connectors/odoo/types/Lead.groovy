package org.camunda.latera.bss.connectors.odoo.types

trait Lead {
  private static String LEAD_ENTITY_TYPE = 'crm.lead'

  def getLeadEntityType() {
    return LEAD_ENTITY_TYPE
  }

  LinkedHashMap getLeadDefaultParams() {
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
      phoneNumber    : null,
      countryId      : null,
      stateId        : null,
      city           : null,
      street         : null,
      street2        : null,
      zip            : null,
      comment        : null
    ]
  }

  LinkedHashMap getLeadParamsMap(LinkedHashMap params) {
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
      phone        : params.phoneNumber,
      country_id   : params.countryId,
      state_id     : params.stateId,
      city         : params.city,
      street       : params.street,
      street2      : params.street2,
      zip          : params.zip,
      description  : params.comment
    ]
  }

  LinkedHashMap getLeadParams(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    def params = getLeadDefaultParams() + input
    def where  = getLeadParamsMap(params)
    return nvlParams(where + convertKeys(additionalParams))
  }

  LinkedHashMap getLead(def id) {
    return getEntity(getLeadEntityType(), id)
  }

  List getLeadsBy(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    LinkedHashMap params = getLeadParams(input, additionalParams)
    return getEntitiesBy(getLeadEntityType(), params)
  }

  LinkedHashMap getLeadBy(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    return getLeadsBy(input, additionalParams)?.getAt(0)
  }

  LinkedHashMap createLead(LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    LinkedHashMap params = getLeadParams(input, additionalParams)
    return createEntity(getLeadEntityType(), params)
  }

  LinkedHashMap updateLead(def id, LinkedHashMap input, LinkedHashMap additionalParams = [:]) {
    LinkedHashMap params = getLeadParams(input, additionalParams)
    return updateEntity(getLeadEntityType(), id, params)
  }

  LinkedHashMap updateLead(LinkedHashMap input, def id, LinkedHashMap additionalParams = [:]) {
    return updateLead(id, input, additionalParams)
  }

  Boolean deleteLead(def id) {
    return deleteEntity(getLeadEntityType(), id)
  }
}