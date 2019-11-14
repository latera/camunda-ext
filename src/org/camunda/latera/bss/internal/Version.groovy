package org.camunda.latera.bss.internal

import groovy.transform.EqualsAndHashCode
import static org.camunda.latera.bss.utils.StringUtil.trim
import static org.camunda.latera.bss.utils.StringUtil.isString
import static org.camunda.latera.bss.utils.Numeric.isInteger
import static org.camunda.latera.bss.utils.Numeric.isIntegerStrict
import static org.camunda.latera.bss.utils.Numeric.isFloat
import static org.camunda.latera.bss.utils.Numeric.isNumber
import static org.camunda.latera.bss.utils.Numeric.round
import static org.camunda.latera.bss.utils.Numeric.toIntSafe
import static org.camunda.latera.bss.utils.Numeric.toFloatSafe

@EqualsAndHashCode
class Version implements Comparable {
  /**
    First item of Hydra version (e.g. 5 for 5.1.2.4)
  */
  Integer main

  /**
    Second item of Hydra version (e.g. 1 for 5.1.2.4)
  */
  Integer major

  /**
    Third item of Hydra version (e.g. 2 for 5.1.2.4)
  */
  Integer minor

  /**
    Forth item of Hydra version (e.g. 4 for 5.1.2.4)
  */
  Integer mod
  private Integer scale


  /**
    Constructor with separated version items.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.internal.VersionSpec.html#Default+constructor"></iframe>
    @param main @see #main
    @param major @see #major
    @param minor @see #minor
    @param mod @see #mod
  */
  Version(def main, def major, def minor = null, def mod = null) {
    if (main != null) {
      this.main = toIntSafe(main)
      if (isIntegerStrict(main)) { // 5
        this.major = toIntSafe(major)
      } else if (isFloat(main)) { // 5.0, 5.1
        this.major = toIntSafe((round(toFloatSafe(main), 1) - this.main)*10) // 0, 1
        //DON'T USE MORE THAN 1 DIGIT AFTER DECIMAL POINT
      }
    } else if (major != null) {
      this.main = toIntSafe(major)
      if (isIntegerStrict(major)) { // 5
        this.major = 0
      } else if (isFloat(major)) { // 5.0, 5.1
        this.major = toIntSafe((round(toFloatSafe(major), 1) - this.main)*10) // 0, 1
        //DON'T USE MORE THAN 1 DIGIT AFTER DECIMAL POINT
      }
    }
    this.minor = toIntSafe(minor)
    this.mod   = toIntSafe(mod)
    setScale()
  }

  /**
    Constructor overload with named arguments.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.internal.VersionSpec.html#Constructor+for+Number+input"></iframe>
    @param input Map with named arguments.
    @see Version(def)
  */
  Version(Map input) {
    this(input.main, input.major, input.minor, input.mod)
  }

  /**
    Constructor overload for Number input.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.internal.VersionSpec.html#Constructor+for+Number+input"></iframe>
    @param input Number with version data.
    @see Version(def)
  */
  Version(Number input) {
    this(main: input)
  }

  /**
    Constructor overload for String input.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.internal.VersionSpec.html#Constructor+for+String+input"></iframe>
    @param input String or GStringImpl with version data.
    @see Version(def)
  */
  Version(CharSequence input) {
    List parts = trim(input).tokenize('.')
    if (parts.size() > 0) {
      this.main = toIntSafe(parts[0])
      if (parts.size() > 1) {
        this.major = toIntSafe(parts[1])
        if (parts.size() > 2) {
          this.minor = toIntSafe(parts[2])
          if (parts.size() > 3) {
            this.mod = toIntSafe(parts[3])
          }
        }
      }
    }
    setScale()
  }

  private setScale() {
    scale = 0
    if (mod != null) {
      scale = 4
    } else {
      if (minor != null) {
        scale = 3
      } else {
        if (major != null) {
          scale = 2
        } else {
          if (main != null) {
            scale = 1
          }
        }
      }
    }
    if (scale == 0) {
      throw new Exception('Version should have at least one number')
    }
  }

  private Integer toInteger() {
    return toIntSafe(main*1000000000 + (major ?: 0)*1000000 + (minor ?: 0)*1000 + (mod ?: 0))
  }

  private Integer toIntegerRound(Version v) {
    return toIntSafe(this.main*1000000000 + (this.major != null && v.major != null ? this.major : 0)*1000000 + (this.major != null && v.major != null && this.minor != null && v.minor != null ? this.minor : 0)*1000 + (this.major != null && v.major != null && this.minor != null && v.minor != null && this.mod != null && v.mod != null ? this.mod : 0))
  }

  /**
    Get version as string.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.internal.VersionSpec.html#%23toString"></iframe>
    @returns String with version.
  */
  String toString() {
    String result = "${main}"
    if (scale > 1) {
      result += ".${major ?: 0}"
      if (scale > 2) {
        result += ".${minor ?: 0}"
        if (scale > 3) {
          result += ".${mod}"
        }
      }
    }
    return result
  }

  /**
    Get version as integer of decimal number (only main.major, without other parts).
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.internal.VersionSpec.html#%23toNumber"></iframe>
    @returns BigInteger or BigDecimal with version.
  */
  Number toNumber() {
    if (major == null) {
      return main
    }
    return toFloatSafe("${this.main}.${this.major}")
  }

  /**
    Compare two versions.
    <p>
    Examples:
    <iframe style="width:100%;height:200px;border:none;" src="/camunda-ext/test-reports/org.camunda.latera.bss.internal.VersionSpec.html#%23compareTo"></iframe>
    @param input Version, Number or String with version to compare.
    @returns 0 if source satisfies target (5.1.2 == 5.1), -1 if source less than target (5.0.0 < 5.1) or 1 if source more than target (5.1.2 > 5.0)
  */
  int compareTo(def v) {
    if (isString(v) || isNumber(v)) {
      v = new Version(v)
    }
    Integer v1 = toIntegerRound(v)
    Integer v2 = v.toInteger()
    int result = v1.compareTo(v2)

    if (result == 0 && scale < v.scale) { // 5.0 == 5 but 5 < 5.0 because 5.0 is more precise
      return -1
    }
    return result
  }
}