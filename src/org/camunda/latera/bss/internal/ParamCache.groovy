package org.camunda.latera.bss.internal

import java.util.concurrent.ConcurrentHashMap
import static org.camunda.latera.bss.utils.Numeric.toIntSafe

class ParamCache {
  private Map<String, BigInteger> map;
  private static final ParamCache instance = new ParamCache();

  static ParamCache getInstance() {
    return instance;
  }

  private ParamCache() {
    map = new ConcurrentHashMap<String, BigInteger>();
  }

  void put(CharSequence key, def value) {
    if (key != null && value != null) {
      map[key.toString()] = toIntSafe(value)
    }
  }

  def get(CharSequence key) {
    return map[key.toString()]
  }

  def putAndGet(CharSequence key, def value) {
    put(key, value)
    return get(key)
  }

  def getKey(def value) {
    BigInteger val = toIntSafe(value)
    return map.find{it.value == val}?.key
  }

  String putAndGetKey(CharSequence key, def value) {
    put(key, value)
    return key.toString()
  }
}