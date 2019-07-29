package org.camunda.latera.bss.connectors.hid.hydra

trait Search {
  Boolean refreshQuickSearch(CharSequence method = 'C') {
    try {
      logger.info("Refreshing all quick search views")
      hid.execute('SS_QUICK_SEARCH_PKG.REFRESH_EVERYTHING', [
        ch_C_METHOD : method
      ])
      logger.info("   Quick search view was refreshed successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while refreshing quick search views!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean refreshMaterialView(CharSequence view, CharSequence method = 'C') {
    try {
      logger.info("Refreshing material view ${view}")
      hid.execute('UTILS_PKG_S.REFRESH_MATERIALIZED_VIEW', [
        vch_VC_VIEW_NAME : view,
        vch_C_METHOD     : method
      ])
      logger.info("   ${view} was refreshed successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while refreshing material view ${view}!")
      logger.error_oracle(e)
      return false
    }
  }
}