package org.camunda.latera.bss.internal

import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap
import static org.camunda.latera.bss.utils.Numeric.toIntSafe

class RefCache {
  private Map<String, BigInteger> map;
  private static final RefCache instance = new RefCache();

  static RefCache getInstance() {
    return instance;
  }

  private RefCache() {
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