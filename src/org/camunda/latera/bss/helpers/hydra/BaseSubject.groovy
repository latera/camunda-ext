package org.camunda.latera.bss.helpers.hydra

import static org.camunda.latera.bss.utils.StringUtil.capitalize
import static org.camunda.latera.bss.utils.StringUtil.isEmpty

/**
  * Base subject helper methods collection
  */
trait BaseSubject {
  /**
   * Get base subject data by id and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubjectId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubjectType}      {@link CharSequence String}</li>
   *   <li>{@code homsOrderData*BaseSubjectIsCompany} {@link Boolean}</li>
   * </ul>
   * @param prefix {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   */
  void fetchBaseSubject(Map input = [:]) {
    Map params = [
      prefix : ''
    ] + input

    String prefix = "${capitalize(params.prefix)}BaseSubject"

    def baseSubjectId = order."${prefix}Id"
    if (isEmpty(baseSubjectId)) {
      return
    }

    Map subject     = hydra.getSubject(baseSubjectId)
    String subjType = hydra.getRefCodeById(subject?.n_subj_type_id)

    order."${prefix}Type"      = subjType
    order."${prefix}IsCompany" = subjType == 'SUBJ_TYPE_Company'
  }

  /**
   * Get base subject additional parameter value and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubjectId} {@link java.math.BigInteger BigInteger}</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*%Param%Id} {@link java.math.BigInteger BigInteger}. if additional parameter value is ref id</li>
   *   <li>{@code homsOrderData*BaseSubject*%Param%}   Any type, if additional parameter is not a ref</li>
   * </ul>
   * @param param             {@link CharSequence String}. Additional parameter short code (=variable part name) ('Param' for 'SUBJ_VAL_Param')
   * @param code              {@link CharSequence String}. Additional parameter full code (if it does not start from 'SUBJ_VAL_')
   * @param baseSubjectPrefix {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   * @param prefix            {@link CharSequence String}. Additional parameter prefix. Optional. Default: empty string
   * @return Additional parameter value
   */
  def fetchBaseSubjectAddParam(Map input = [:]) {
    Map params = [
      baseSubjectPrefix : '',
      prefix            : '',
      param             : '',
      code              : ''
    ] + input

    String baseSubjectPrefix = "${capitalize(params.baseSubjectPrefix)}BaseSubject"
    String prefix = capitalize(params.prefix)
    String param  = capitalize(params.param)

    def baseSubjectId = order."${baseSubjectPrefix}Id"
    if (isEmpty(baseSubjectId)) {
      return
    }

    Map addParam = hydra.getSubjectAddParamBy(
      subjectId : baseSubjectId,
      param     : params.code ?: "SUBJ_VAL_${param}"
    )
    def (value, valueType) = hydra.getAddParamValue(addParam)
    order."${baseSubjectPrefix}${prefix}${params.code ?: param}${valueType == 'refId' ? 'Id': ''}" = value
    return value
  }

  /**
   * Save base subject additional parameter value and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubjectId}         {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*BaseSubject*%Param%Id} {@link java.math.BigInteger BigInteger}. if additional parameter value is ref id</li>
   *   <li>{@code homsOrderData*BaseSubject*%Param%}   Any type, if additional parameter value is not a ref or ref code is used instead</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*%Param%Saved} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param param             {@link CharSequence String}. Additional parameter short code (=variable part name) ('Param' for 'SUBJ_VAL_Param')
   * @param code              {@link CharSequence String}. Additional parameter full code (if it does not start from 'SUBJ_VAL_')
   * @param baseSubjectPrefix {@link CharSequence String}. Base subject prefix. Optional. Default: empty string
   * @param prefix            {@link CharSequence String}. Additional parameter prefix. Optional. Default: empty string
   * @return True if additional parameter value was saved successfully, false otherwise
   */
  Boolean saveBaseSubjectAddParam(Map input = [:]) {
    Map params = [
      baseSubjectPrefix : '',
      prefix            : '',
      param             : '',
      code              : ''
    ] + input

    String baseSubjectPrefix = "${capitalize(params.baseSubjectPrefix)}BaseSubject"
    String prefix     = capitalize(params.prefix)
    String param      = capitalize(params.param)
    def baseSubjectId = order."${baseSubjectPrefix}Id"
    def value         = order."${baseSubjectPrefix}${prefix}${params.code ?: param}" ?: order."${baseSubjectPrefix}${prefix}${params.code ?: param}Id"

    Map addParam = hydra.addSubjectAddParam(
      baseSubjectId,
      param : params.code ?: "SUBJ_VAL_${param}",
      value : value
    )

    Boolean result = false
    if (addParam) {
      result = true
    }

    order."${baseSubjectPrefix}${prefix}${params.code ?: param}Saved" = result
    return result
  }

  /**
   * Delete base subject additional parameter value and fill up execution variables
   * <p>
   * Input execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubjectId}         {@link java.math.BigInteger BigInteger}</li>
   *   <li>{@code homsOrderData*BaseSubject*%Param%Id} {@link java.math.BigInteger BigInteger}. if additional parameter value is ref id</li>
   *   <li>{@code homsOrderData*BaseSubject*%Param%}   Any type, if additional parameter value is not a ref or ref code is used instead</li>
   * </ul>
   * <p>
   * Output execution variables:
   * <ul>
   *   <li>{@code homsOrderData*BaseSubject*%Param%Deleted} {@link Boolean}. Same as return value</li>
   * </ul>
   * @param param             {@link CharSequence String}.  Additional parameter short code (=variable part name) ('Param' for 'SUBJ_VAL_Param')
   * @param code              {@link CharSequence String}.  Additional parameter full code (if it does not start from 'SUBJ_VAL_')
   * @param baseSubjectPrefix {@link CharSequence String}.  Base subject prefix. Optional. Default: empty string
   * @param prefix            {@link CharSequence String}.  Additional parameter prefix. Optional. Default: empty string
   * @param force             {@link Boolean}. For multiple additional parameters. If you need to remove only a value which is equal to one stored in the input execution variable, pass false. Otherwise method will remove additional param value without check
   * @return True if additional parameter value was deleted successfully, false otherwise
   */
  Boolean deleteBaseSubjectAddParam(Map input = [:]) {
    Map params = [
      baseSubjectPrefix : '',
      prefix            : '',
      param             : '',
      code              : '',
      force             : false
    ] + input

    String baseSubjectPrefix = "${capitalize(params.baseSubjectPrefix)}BaseSubject"
    String prefix = capitalize(params.prefix)
    String param  = capitalize(params.param)

    def baseSubjectId = order."${baseSubjectPrefix}Id"
    def value         = order."${baseSubjectPrefix}${prefix}${params.code ?: param}" ?: order."${baseSubjectPrefix}${prefix}${params.code ?: param}Id"

    Boolean result = true

    if (params.force) {
      result = hydra.deleteSubjectAddParam(
        subjectId : baseSubjectId,
        param     : params.code ?: "SUBJ_VAL_${param}"
      )
    } else {
      result = hydra.deleteSubjectAddParam(
        subjectId : baseSubjectId,
        param     : params.code ?: "SUBJ_VAL_${param}",
        value     : value // multiple add param support
      )
    }

    order."${baseSubjectPrefix}${prefix}${params.code ?: param}Deleted" = result
    return result
  }
}