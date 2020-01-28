package org.camunda.latera.bss.utils

import spock.lang.*
import org.camunda.latera.bss.utils.Constants

class ConstantsSpec extends Specification {
  def "#getLang"() {
    expect:
    Constants.getLang(name) == result

    where:
    name         |result
    'Russian'    |Constants.LANG_Ru
    'Ru'         |Constants.LANG_Ru
    'English'    |Constants.LANG_En
    'En'         |Constants.LANG_En
    'Ukrainian'  |Constants.LANG_Ua
    'Ua'         |Constants.LANG_Ua
    'Kazakh'     |Constants.LANG_Kz
    'Kz'         |Constants.LANG_Kz
    'Kirghiz'    |Constants.LANG_Kg
    'Kg'         |Constants.LANG_Kg
    'Azerbaijani'|Constants.LANG_Az
    'Az'         |Constants.LANG_Az
    'Spanish'    |Constants.LANG_Es
    'Es'         |Constants.LANG_Es
    'Unknown'    |null
    null         |null
  }

  def "#getContstantByCode"() {
    expect:
    Constants.getContstantByCode(code) == result

    where:
    code                |result
    'SUBJ_TYPE_Company' |Constants.SUBJ_TYPE_Company
    'SUBJ_TYPE_Unknown' |null
    null                |null
  }

  def "#getContstantCode"() {
    expect:
    Constants.getContstantCode(code) == result

    where:
    code                       |result
    Constants.SUBJ_TYPE_Company|'SUBJ_TYPE_Company'
    12345                      |null
    null                       |null
  }
}