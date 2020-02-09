package org.camunda.latera.bss.connectors.hid.hydra

trait Search {

  /**
   * Refresh all quick search material views
   * @param method {@link CharSequence String} from list: 'C', 'F', 'P', '?'
   * @see <a href="https://docs.oracle.com/database/121/DWHSG/refresh.htm#DWHSG8366">Oracle documentation</a>
   * @return True if quick search was updated successfully, false otherwise
   */
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

  /**
   * Refresh quick search material view
   * @param view   {@link CharSequence String}
   * @param method {@link CharSequence String} from list: 'C', 'F', 'P', '?'
   * @see <a href="https://docs.oracle.com/database/121/DWHSG/refresh.htm#DWHSG8366">Oracle documentation</a>
   * @return True if material view was updated successfully, false otherwise
   */
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